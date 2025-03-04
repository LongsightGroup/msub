/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/ui/PrivateMessageManagerImpl.java $
 * $Id: PrivateMessageManagerImpl.java 26236 2011-04-11 19:52:43Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.TestUtil;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PrivateMessageManagerImpl extends HibernateDaoSupport implements
    PrivateMessageManager
{

  private static final Log LOG = LogFactory
      .getLog(PrivateMessageManagerImpl.class);

  private static final String QUERY_AGGREGATE_COUNT = "findAggregatePvtMsgCntForUserInContext";  
  private static final String QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT = "findPrvtMsgsByUserTypeContext";
  private static final String QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS = "findPrivateMessageByIdWithRecipients";
  
  private AreaManager areaManager;
  private MessageForumsMessageManager messageManager;
  private MessageForumsForumManager forumManager;
  private MessageForumsTypeManager typeManager;
  private IdManager idManager;
  private SessionManager sessionManager;  
  private EmailService emailService;
  private ContentHostingService contentHostingService;
  
  
  private static final String MESSAGES_TITLE = "pvt_message_nav";// Mensajes-->Messages/need to be modified to support internationalization
  
  private static final String PVT_RECEIVED = "pvt_received";     // Recibidos ( 0 mensajes )-->Received ( 8 messages - 8 unread )
  private static final String PVT_SENT = "pvt_sent";             // Enviados ( 0 mensajes )--> Sent ( 0 message )
  private static final String PVT_DELETED = "pvt_deleted";       // Borrados ( 0 mensajes )-->Deleted ( 0 message )
  private static final String PVT_DRAFTS = "pvt_drafts";
 
  /** String ids for email footer messsage */
  private static final String EMAIL_FOOTER1 = "pvt_email_footer1";
  private static final String EMAIL_FOOTER2 = "pvt_email_footer2";
  private static final String EMAIL_FOOTER3 = "pvt_email_footer3";
  private static final String EMAIL_FOOTER4 = "pvt_email_footer4";

  public void init()
  {
	LOG.info("init()");
    ;
  }
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

  public boolean getPrivateAreaEnabled()
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPrivateAreaEnabled()");
    }
        
    return areaManager.isPrivateAreaEnabled();

  }

  public void setPrivateAreaEnabled(boolean value)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("setPrivateAreaEnabled(value: " + value + ")");
    }

  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#isPrivateAreaEnabled()
   */
  public boolean isPrivateAreaEnabled()
  {
    return areaManager.isPrivateAreaEnabled();
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#getPrivateMessageArea()
   */
  public Area getPrivateMessageArea()
  {
    return areaManager.getPrivateArea();    
  }
  
  public Area getPrivateMessageArea(String siteId)
  {
    return areaManager.getPrivateArea(siteId);    
  }


  public void savePrivateMessageArea(Area area)
  {
    areaManager.saveArea(area);
  }

  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#initializePrivateMessageArea(org.sakaiproject.api.app.messageforums.Area)
   */
  
  
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList){
	  return initializePrivateMessageArea(area, aggregateList, getCurrentUser());
  }
  
  //==============Need to be modified to support localization huxt
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList, String userId){
	  return initializePrivateMessageArea(area, aggregateList, userId, getContextId());
  }  
  
  public PrivateForum initializePrivateMessageArea(Area area, List aggregateList, String userId, String siteId)
  {

   // String userId = getCurrentUser();
    
    aggregateList.clear();
    aggregateList.addAll(initializeMessageCounts(userId, siteId));
    
    getHibernateTemplate().lock(area, LockMode.NONE);
    
    PrivateForum pf;

    /** create default user forum/topics if none exist */
    if ((pf = forumManager.getPrivateForumByOwnerArea(userId, area)) == null)
    {      
      /** initialize collections */
      //getHibernateTemplate().initialize(area.getPrivateForumsSet());
            
      pf = forumManager.createPrivateForum(getResourceBundleString(MESSAGES_TITLE), userId);
      
      //area.addPrivateForum(pf);
      //pf.setArea(area);
      //areaManager.saveArea(area);
      
      PrivateTopic receivedTopic = forumManager.createPrivateForumTopic(PVT_RECEIVED, true,false,
          userId, pf.getId());     

      PrivateTopic sentTopic = forumManager.createPrivateForumTopic(PVT_SENT, true,false,
          userId, pf.getId());      

      PrivateTopic deletedTopic = forumManager.createPrivateForumTopic(PVT_DELETED, true,false,
          userId, pf.getId());      

      PrivateTopic draftTopic = forumManager.createPrivateForumTopic(PVT_DRAFTS, true,false,
          userId, pf.getId());
    
      /** save individual topics - required to add to forum's topic set */
      forumManager.savePrivateForumTopic(receivedTopic, userId, siteId);
      forumManager.savePrivateForumTopic(sentTopic, userId, siteId);
      forumManager.savePrivateForumTopic(deletedTopic, userId, siteId);
      forumManager.savePrivateForumTopic(draftTopic);
      
      pf.addTopic(receivedTopic);
      pf.addTopic(sentTopic);
      pf.addTopic(deletedTopic);
      pf.addTopic(draftTopic);
      pf.setArea(area);  
      
      PrivateForum oldForum;
      if ((oldForum = forumManager.getPrivateForumByOwnerAreaNull(userId)) != null)
      {
    		oldForum = initializationHelper(oldForum, userId);
//    		getHibernateTemplate().initialize(oldForum.getTopicsSet());
    		List pvtTopics = oldForum.getTopics();
    		
    		for(int i=0; i<pvtTopics.size(); i++)//reveived deleted sent
    		{
    			PrivateTopic currentTopic = (PrivateTopic) pvtTopics.get(i);
    			if(currentTopic != null)
    			{
    				if(!currentTopic.getTitle().equals(PVT_RECEIVED) && !currentTopic.getTitle().equals(PVT_SENT) && !currentTopic.getTitle().equals(PVT_DELETED) 
    						&& !currentTopic.getTitle().equals(PVT_DRAFTS) && area.getContextId().equals(currentTopic.getContextId()))
    				{
    					currentTopic.setPrivateForum(pf);
    		      forumManager.savePrivateForumTopic(currentTopic, userId, siteId);
    					pf.addTopic(currentTopic);
    				}
    			}
    		}
    		if(oldForum.getAutoForward() != null)
    		{
    			pf.setAutoForward(oldForum.getAutoForward());
    		}
    		if(oldForum.getAutoForwardEmail() != null)
    		{
    			pf.setAutoForwardEmail(oldForum.getAutoForwardEmail());
    		}      
      }
      
      forumManager.savePrivateForum(pf, userId);            
      
    }    
    else{      
       //getHibernateTemplate().initialize(pf.getTopicsSet());   
    	 pf = forumManager.getPrivateForumByOwnerAreaWithAllTopics(userId, area);
    }
   
    return pf;
  }
  
  public PrivateForum initializationHelper(PrivateForum forum){
	  return initializationHelper(forum, getCurrentUser());
  }
  
  public PrivateForum initializationHelper(PrivateForum forum, String userId){
    
    /** reget to load topic foreign keys */
  	//PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNull(getCurrentUser());
    //getHibernateTemplate().initialize(pf.getTopicsSet());    
  	PrivateForum pf = forumManager.getPrivateForumByOwnerAreaNullWithAllTopics(userId);
    return pf;
  }

  public PrivateForum initializationHelper(PrivateForum forum, Area area){
	  return initializationHelper(forum, area, getCurrentUser());
  }
  
  public PrivateForum initializationHelper(PrivateForum forum, Area area, String userId){
    
    /** reget to load topic foreign keys */
  	//PrivateForum pf = forumManager.getPrivateForumByOwnerArea(getCurrentUser(), area);
    //getHibernateTemplate().initialize(pf.getTopicsSet());    
  	PrivateForum pf = forumManager.getPrivateForumByOwnerAreaWithAllTopics(userId, area);
    return pf;
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message)
   */
  public void savePrivateMessage(Message message)
  {
    messageManager.saveMessage(message);
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#savePrivateMessage(org.sakaiproject.api.app.messageforums.Message, boolean logEvent)
   */
  public void savePrivateMessage(Message message, boolean logEvent)
  {
	  messageManager.saveMessage(message, logEvent);
  }
  
  public Message getMessageById(Long id)
  {
    return messageManager.getMessageById(id);
  }

  //Attachment
  public Attachment createPvtMsgAttachment(String attachId, String name)
  {
    try
    {
      Attachment attach = messageManager.createAttachment();

      attach.setAttachmentId(attachId);

      attach.setAttachmentName(name);

      ContentResource cr = contentHostingService.getResource(attachId);
      attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(
          cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = "";
      char[] oneChar = new char[1];
      for (int i = 0; i < tempString.length(); i++)
      {
        if (tempString.charAt(i) != ' ')
        {
          oneChar[0] = tempString.charAt(i);
          String concatString = new String(oneChar);
          newString = newString.concat(concatString);
        }
        else
        {
          newString = newString.concat("%20");
        }
      }
      //tempString.replaceAll(" ", "%20");
      //attach.setAttachmentUrl(newString);
      attach.setAttachmentUrl("/url");

      return attach;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // Himansu: I am not quite sure this is what you want... let me know.
  // Before saving a message, we need to add all the attachmnets to a perticular message
  public void addAttachToPvtMsg(PrivateMessage pvtMsgData,
      Attachment pvtMsgAttach)
  {
    pvtMsgData.addAttachment(pvtMsgAttach);
  }

  // Required for editing multiple attachments to a message. 
  // When you reply to a message, you do have option to edit attachments to a message
  public void removePvtMsgAttachment(Attachment o)
  {
    o.getMessage().removeAttachment(o);
  }

  public Attachment getPvtMsgAttachment(Long pvtMsgAttachId)
  {
    return messageManager.getAttachmentById(pvtMsgAttachId);
  }

  public int getTotalNoMessages(Topic topic)
  {
    return messageManager.findMessageCountByTopicId(topic.getId());
  }

  public int getUnreadNoMessages(Topic topic)
  {
    return messageManager.findUnreadMessageCountByTopicId(topic.getId());
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#saveAreaAndForumSettings(org.sakaiproject.api.app.messageforums.Area, org.sakaiproject.api.app.messageforums.PrivateForum)
   */
  public void saveAreaAndForumSettings(Area area, PrivateForum forum)
  {

    /** method calls placed in this function to participate in same transaction */
           
    saveForumSettings(forum);

    /** need to evict forum b/c area saves fk on forum (which places two objects w/same id in session */
    //getHibernateTemplate().evict(forum);
    
    if (isInstructor() || isEmailPermit()){
      savePrivateMessageArea(area);
    }
    
  }

  public void saveForumSettings(PrivateForum forum)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("saveForumSettings(forum: " + forum + ")");
    }

    if (forum == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    forumManager.savePrivateForum(forum);
  }

  /**
   * Topic Folder Setting
   */
  public boolean isMutableTopicFolder(String parentTopicId)
  {
    return false;
  }
  
  public void createTopicFolderInForum(PrivateForum pf, String folderName)
  {
    String userId = getCurrentUser();
    PrivateTopic createdTopic = forumManager.createPrivateForumTopic(folderName, true,true,
        userId, pf.getId()); 
    
    /** set context and type to differentiate user topics within sites */
    createdTopic.setContextId(getContextId());
    createdTopic.setTypeUuid(typeManager.getUserDefinedPrivateTopicType());
    
    forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopic);   
    forumManager.savePrivateForum(pf);  
  }

  public void createTopicFolderInTopic(PrivateForum pf, PrivateTopic parentTopic, String folderName)
  {
    String userId = getCurrentUser();
    PrivateTopic createdTopic = forumManager.createPrivateForumTopic(folderName, true,true,
        userId, pf.getId()); 
    createdTopic.setParentTopic(parentTopic);
    forumManager.savePrivateForumTopic(createdTopic);
    pf.addTopic(createdTopic);    
    forumManager.savePrivateForum(pf);  
  }

  public void renameTopicFolder(PrivateForum pf, String topicUuid, String newName)
  {
    String userId = getCurrentUser();
    List pvtTopics= pf.getTopics();
    for (Iterator iter = pvtTopics.iterator(); iter.hasNext();)
    {
      PrivateTopic element = (PrivateTopic) iter.next();
      if(element.getUuid().equals(topicUuid))
      {
        element.setTitle(newName);
        element.setModifiedBy(userId);
        element.setModified(new Date());
        
        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE, getEventMessage(element), false));
      }      
    }
    forumManager.savePrivateForum(pf);
  }

  public void deleteTopicFolder(PrivateForum pf,String topicUuid)
  {
    List pvtTopics= pf.getTopics();
    for (Iterator iter = pvtTopics.iterator(); iter.hasNext();)
    {
      PrivateTopic element = (PrivateTopic) iter.next();
      if(element.getUuid().equals(topicUuid))
      {
        pf.removeTopic(element);

        EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE, getEventMessage(element), false));
        break;
      }
    }
    forumManager.savePrivateForum(pf);  
  }

  /**
   * Return Topic based on uuid 
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#getTopicByIdWithMessages(java.lang.Long)
   */
  public Topic getTopicByUuid(final String topicUuid)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getTopicByIdWithMessages(final Long" + topicUuid + ")");
    }
    return forumManager.getTopicByUuid(topicUuid);
  }
  
  public static final String PVTMSG_MODE_RECEIVED = "pvt_received";
  public static final String PVTMSG_MODE_SENT = "pvt_sent";
  public static final String PVTMSG_MODE_DELETE = "pvt_deleted";
  public static final String PVTMSG_MODE_DRAFT = "pvt_drafts";

  public void movePvtMsgTopic(PrivateMessage message, Topic oldTopic, Topic newTopic)
  {
    List recipients= message.getRecipients();
    //get new topic type uuid
    String newTopicTypeUuid=getTopicTypeUuid(newTopic.getTitle());
    //get pld topic type uuid
    String oldTopicTypeUuid=getTopicTypeUuid(oldTopic.getTitle());
    
    //now set the recipiant with new topic type uuid
    for (Iterator iter = recipients.iterator(); iter.hasNext();)
    {
      PrivateMessageRecipient element = (PrivateMessageRecipient) iter.next();
      LOG.debug("element.getTypeUuid(): "+element.getTypeUuid()+", oldTopicTypeUuid: "+oldTopicTypeUuid+", element.getUserId(): "+element.getUserId()+ ", getCurrentUser(): "+getCurrentUser());
      if (element.getTypeUuid().equals(oldTopicTypeUuid) && (element.getUserId().equals(getCurrentUser())))
      {
        element.setTypeUuid(newTopicTypeUuid);
      }
    }
    savePrivateMessage(message, false);
    
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#createPrivateMessage(java.lang.String)
   */
  public PrivateMessage createPrivateMessage(String typeUuid)
  {
    PrivateMessage message = new PrivateMessageImpl();
    message.setUuid(idManager.createUuid());
    message.setTypeUuid(typeUuid);
    message.setCreated(new Date());
    message.setCreatedBy(getCurrentUser());

    LOG.debug("message " + message.getUuid() + " created successfully");
    return message;
  }

  public boolean hasNextMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    boolean next = false;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (next)
        {
          return true;
        }
        if (m.getId().equals(message.getId()))
        {
          next = true;
        }
      }
    }

    // if we get here, there is no next message
    return false;
  }

  public boolean hasPreviousMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    PrivateMessage prev = null;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (m.getId().equals(message.getId()))
        {
          // need to check null because we might be on the first message
          // which means there is no previous one
          return prev != null;
        }
        prev = (PrivateMessage) m;
      }
    }

    // if we get here, there is no previous message
    return false;
  }

  public PrivateMessage getNextMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    boolean next = false;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (next)
        {
          return (PrivateMessage) m;
        }
        if (m.getId().equals(message.getId()))
        {
          next = true;
        }
      }
    }

    // if we get here, there is no next message
    return null;
  }

  public PrivateMessage getPreviousMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    PrivateMessage prev = null;
    if (message != null && message.getTopic() != null
        && message.getTopic().getMessages() != null)
    {
      for (Iterator iter = message.getTopic().getMessages().iterator(); iter
          .hasNext();)
      {
        Message m = (Message) iter.next();
        if (m.getId().equals(message.getId()))
        {
          return prev;
        }
        prev = (PrivateMessage) m;
      }
    }

    // if we get here, there is no previous message
    return null;
  }

  public List getMessagesByTopic(String userId, Long topicId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getReceivedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getReceivedPrivateMessageType(),
        orderField, order);
  }

  public List getSentMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getSentPrivateMessageType(),
        orderField, order);    
  }

  public List getDeletedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getDeletedPrivateMessageType(),
        orderField, order);
  }

  public List getDraftedMessages(String orderField, String order)
  {
    return getMessagesByType(typeManager.getDraftPrivateMessageType(),
        orderField, order);
  }
    
  public PrivateMessage initMessageWithAttachmentsAndRecipients(PrivateMessage msg){
    
    PrivateMessage pmReturn = (PrivateMessage) messageManager.getMessageByIdWithAttachments(msg.getId());    
    getHibernateTemplate().initialize(pmReturn.getRecipients());
    return pmReturn;
  }
  
  /**
   * helper method to get messages by type
   * @param typeUuid
   * @return message list
   */
  public List getMessagesByType(final String typeUuid, final String orderField,
      final String order)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByType(typeUuid:" + typeUuid + ", orderField: "
          + orderField + ", order:" + order + ")");
    }

    //    HibernateCallback hcb = new HibernateCallback() {
    //      public Object doInHibernate(Session session) throws HibernateException, SQLException {
    //        Criteria messageCriteria = session.createCriteria(PrivateMessageImpl.class);
    //        Criteria recipientCriteria = messageCriteria.createCriteria("recipients");
    //        
    //        Conjunction conjunction = Expression.conjunction();
    //        conjunction.add(Expression.eq("userId", getCurrentUser()));
    //        conjunction.add(Expression.eq("typeUuid", typeUuid));        
    //        
    //        recipientCriteria.add(conjunction);
    //        
    //        if ("asc".equalsIgnoreCase(order)){
    //          messageCriteria.addOrder(Order.asc(orderField));
    //        }
    //        else if ("desc".equalsIgnoreCase(order)){
    //          messageCriteria.addOrder(Order.desc(orderField));
    //        }
    //        else{
    //          LOG.debug("getMessagesByType failed with (typeUuid:" + typeUuid + ", orderField: " + orderField +
    //              ", order:" + order + ")");
    //          throw new IllegalArgumentException("order must have value asc or desc");          
    //        }
    //        
    //        //todo: parameterize fetch mode
    //        messageCriteria.setFetchMode("recipients", FetchMode.EAGER);
    //        messageCriteria.setFetchMode("attachments", FetchMode.EAGER);
    //        
    //        return messageCriteria.list();        
    //      }
    //    };

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);
        Query qOrdered = session.createQuery(q.getQueryString() + " order by "
            + orderField + " " + order);

        qOrdered.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        qOrdered.setParameter("typeUuid", typeUuid, Hibernate.STRING);
        qOrdered.setParameter("contextId", getContextId(), Hibernate.STRING);
        return qOrdered.list();
      }
    };

    return (List) getHibernateTemplate().execute(hcb);        
  }
  
  /**
   * FOR SYNOPTIC TOOL:
   * 	helper method to get messages by type
   * 	needed to pass contextId since could be in MyWorkspace
   * 
   * @param typeUuid
   * 			The type of forum it is (Private or Topic)
   * @param contextId
   * 			The site id whose messages are needed
   * 
   * @return message list
   */
  public List getMessagesByTypeByContext(final String typeUuid, final String contextId)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByTypeForASite(typeUuid:" + typeUuid + ")");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);

        q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
        q.setParameter("typeUuid", typeUuid, Hibernate.STRING);
        q.setParameter("contextId", contextId, Hibernate.STRING);
        return q.list();
      }
    };

    return (List) getHibernateTemplate().execute(hcb);        
  }
  
  public List getMessagesByTypeByContext(final String typeUuid, final String contextId, final String userId, final String orderField,
	      final String order){
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getMessagesByTypeForASite(typeUuid:" + typeUuid + ")");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_USER_TYPE_AND_CONTEXT);
        Query qOrdered = session.createQuery(q.getQueryString() + " order by "
                + orderField + " " + order);
        qOrdered.setParameter("userId", userId, Hibernate.STRING);
        qOrdered.setParameter("typeUuid", typeUuid, Hibernate.STRING);
        qOrdered.setParameter("contextId", contextId, Hibernate.STRING);
        return qOrdered.list();
      }
    };

    return (List) getHibernateTemplate().execute(hcb);        
  }


    /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findMessageCount(java.lang.String)
   */
  public int findMessageCount(String typeUuid, List aggregateList)
  {    
    if (LOG.isDebugEnabled())
    {
      LOG.debug("findMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      LOG.error("findMessageCount failed with typeUuid: null");
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      LOG.error("findMessageCount failed with aggregateList: null");
      throw new IllegalStateException("aggregateList is null");
    }
    
    int totalCount = 0;
    for (Iterator i = aggregateList.iterator(); i.hasNext();){
      Object[] element = (Object[]) i.next();
      /** filter on type */
      if (typeUuid.equals(element[1])){        
        /** add read/unread message types */        
        totalCount += ((Integer) element[2]).intValue(); 
      }      
    }
            
    return totalCount;
  }
  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#findUnreadMessageCount(java.lang.String)
   */
  public int findUnreadMessageCount(String typeUuid, List aggregateList)
  {    
    if (LOG.isDebugEnabled())
    {
      LOG.debug("findUnreadMessageCount executing with typeUuid: " + typeUuid);
    }

    if (typeUuid == null)
    {
      LOG.error("findUnreadMessageCount failed with typeUuid: null");
      throw new IllegalArgumentException("Null Argument");
    }    
    
    if (aggregateList == null)
    {
      LOG.error("findMessageCount failed with aggregateList: Null");
      throw new IllegalStateException("aggregateList is null");
    }
    
    int unreadCount = 0;
    for (Iterator i = aggregateList.iterator(); i.hasNext();){
      Object[] element = (Object[]) i.next();
      /** filter on type and read status*/
      if (!typeUuid.equals(element[1]) || Boolean.TRUE.equals(element[0])){
        continue;
      }
      else{        
        unreadCount = ((Integer) element[2]).intValue();
        break;
      }      
    }
            
    return unreadCount;    
  }
  

  private List initializeMessageCounts(final String userId, final String contextId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("initializeMessageCounts executing");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_AGGREGATE_COUNT);        
        q.setParameter("contextId", contextId, Hibernate.STRING);
        q.setParameter("userId", userId, Hibernate.STRING);
        return q.list();
      }
    };
        
    return (List) getHibernateTemplate().execute(hcb);        
  }


  /**
   * FOR SYNOPTIC TOOL:
   * 	Returns a list of all sites this user is in along with a count of his/her
   * 	unread messages
   * 
   * @return
   * 	List of site id, count of unread message pairs
   */
  public List getPrivateMessageCountsForAllSites() {
	  HibernateCallback hcb = new HibernateCallback() {
		  public Object doInHibernate(Session session) throws HibernateException,
	  	 	SQLException
	  	 {
			  Query q = session.getNamedQuery("findUnreadPvtMsgCntByUserForAllSites");
			  q.setParameter("userId", getCurrentUser(), Hibernate.STRING);
			  return q.list();
	  	 }
	  };
  
	  return (List) getHibernateTemplate().execute(hcb);
	  
  }

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#deletePrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.lang.String)
   */
  public void deletePrivateMessage(PrivateMessage message, String typeUuid)
  {

    String userId = getCurrentUser();

    if (LOG.isDebugEnabled())
    {
      LOG.debug("deletePrivateMessage(message:" + message + ", typeUuid:"
          + typeUuid + ")");
    }

    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /**
     *  create PrivateMessageRecipient to search
     */
    PrivateMessageRecipient pmrReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.TRUE, false);

    PrivateMessageRecipient pmrNonReadSearch = new PrivateMessageRecipientImpl(
        userId, typeUuid, getContextId(), Boolean.FALSE , false);

    int indexDelete = -1;
    int indexRead = pvtMessage.getRecipients().indexOf(pmrReadSearch);
    if (indexRead != -1)
    {
      indexDelete = indexRead;
    }
    else
    {
      int indexNonRead = pvtMessage.getRecipients().indexOf(pmrNonReadSearch);
      if (indexNonRead != -1)
      {
        indexDelete = indexNonRead;
      }
      else
      {
        LOG
            .error("deletePrivateMessage -- cannot find private message for user: "
                + userId + ", typeUuid: " + typeUuid);
      }
    }

    if (indexDelete != -1)
    {
      PrivateMessageRecipient pmrReturned = (PrivateMessageRecipient) pvtMessage
          .getRecipients().get(indexDelete);

      if (pmrReturned != null)
      {

        /** check for existing deleted message from user */
        PrivateMessageRecipient pmrDeletedSearch = new PrivateMessageRecipientImpl(
            userId, typeManager.getDeletedPrivateMessageType(), getContextId(),
            Boolean.TRUE, false);

        int indexDeleted = pvtMessage.getRecipients().indexOf(pmrDeletedSearch);

        if (indexDeleted == -1)
        {
        	boolean prevReadStatus = pmrReturned.getRead();
        	pmrReturned.setRead(Boolean.TRUE);

        	String contextId = getContextId();
        	if(!prevReadStatus){
        		decrementMessagesSynopticToolInfo(userId, contextId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        	}

        	pmrReturned.setTypeUuid(typeManager.getDeletedPrivateMessageType());
        }
        else
        {
          pvtMessage.getRecipients().remove(indexDelete);
        }
      }
    }
  }
  
  public void decrementMessagesSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
		try {
			SynopticMsgcntrManagerCover.decrementMessagesSynopticToolInfo(userId, siteId);
		} catch (HibernateOptimisticLockingFailureException holfe) {

			// failed, so wait and try again
			try {
				Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				System.out
						.println("PrivateMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				holfe.printStackTrace();
			} else {
				System.out
						.println("PrivateMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				decrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
			}
		}

	}

  public void incrementMessagesSynopticToolInfo(String userId, String siteId , int numOfAttempts) {
		try {
			SynopticMsgcntrManagerCover.incrementMessagesSynopticToolInfo(userId, siteId);
		} catch (HibernateOptimisticLockingFailureException holfe) {

			// failed, so wait and try again
			try {
				Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				System.out
						.println("PrivateMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
				holfe.printStackTrace();
			} else {
				System.out
						.println("PrivateMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
								+ numOfAttempts);
				incrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
			}
		}

	}

  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#sendPrivateMessage(org.sakaiproject.api.app.messageforums.PrivateMessage, java.util.Set, boolean)
   */
  public void sendPrivateMessage(PrivateMessage message, Map<User, Boolean> recipients, boolean asEmail)
  {

     try
     {
     if (LOG.isDebugEnabled())
     {
       LOG.debug("sendPrivateMessage(message: " + message + ", recipients: "
           + recipients + ")");
     }

     if (message == null || recipients == null)
     {
       throw new IllegalArgumentException("Null Argument");
     }

     if (recipients.size() == 0 && !message.getDraft().booleanValue())
     {
       /** for no just return out
         throw new IllegalArgumentException("Empty recipient list");
       **/
       return;
     }

     String currentUserAsString = getCurrentUser();
     User currentUser = UserDirectoryService.getCurrentUser();
     List recipientList = new UniqueArrayList();

    /** test for draft message */
    if (message.getDraft().booleanValue())
    {
      PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
      		currentUserAsString, typeManager.getDraftPrivateMessageType(),
          getContextId(), Boolean.TRUE, false);

       recipientList.add(receiver);
       message.setRecipients(recipientList);
       savePrivateMessage(message, false);
       return;
     }

     //build the message body
     List additionalHeaders = new ArrayList(1);
     additionalHeaders.add("Content-Type: text/html");


     /** determines if default in sakai.properties is set, if not will make a reasonable default */
     String defaultEmail = "postmaster@" + ServerConfigurationService.getServerName();
     String systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);

     if (!ServerConfigurationService.getBoolean("msgcntr.notification.user.real.from", false)) {
         systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);
     } else  {
         if (currentUser.getEmail() != null)
             systemEmail = currentUser.getEmail();
         else
             systemEmail = ServerConfigurationService.getString("msgcntr.notification.from.address", defaultEmail);
     }

    String bodyString = buildMessageBody(message);

     Area currentArea = null;
     List<PrivateForum> privateForums = null;
     Map<String, PrivateForum> pfMap = null;

         currentArea = getAreaByContextIdAndTypeId(typeManager.getPrivateMessageAreaType());

         //this is fairly inneficient and should realy be a convenience method to lookup
         // the users who want to forward their messages
         privateForums = currentArea.getPrivateForums();

    	//create a map for efficient lookup for large sites
				    	pfMap = new HashMap<String, PrivateForum>();
    		    	for (int i = 0; i < privateForums.size(); i++) {
    		PrivateForum pf1 = (PrivateForum)privateForums.get(i);
    		pfMap.put(pf1.getOwner(), pf1);
    			}

         boolean forwardingEnabled = false;
         List<InternetAddress> fAddresses = new ArrayList<InternetAddress>();
         //this only needs to be done if the message is not being sent
    	for (Iterator<Entry<User, Boolean>> i = recipients.entrySet().iterator(); i.hasNext();)
    	{
    		Entry<User, Boolean> entrySet = (Entry<User, Boolean>) i.next();
    		User u = (User) entrySet.getKey();
    		Boolean bcc = (Boolean) entrySet.getValue();
             String userId = u.getId();



    		//boolean forwardingEnabled = false;
    		//String forwardAddress = null;

    		PrivateForum pf = null;
    		if (pfMap.containsKey(userId))
    			pf = pfMap.get(userId);
    
             if (pf != null && pf.getAutoForward().booleanValue() && pf.getAutoForwardEmail() != null){
                 forwardingEnabled = true;
                 fAddresses.add(new InternetAddress(pf.getAutoForwardEmail()));
                 //forwardAddress = pf.getAutoForwardEmail();
             }
             if( pf == null)
             {
                 //only check for default settings if the pf is null
                 PrivateForum oldPf = forumManager.getPrivateForumByOwnerAreaNull(userId);
				if (oldPf != null && oldPf.getAutoForward().booleanValue() && oldPf.getAutoForwardEmail() != null) {
                     //forwardAddress = oldPf.getAutoForwardEmail();
                     forwardingEnabled = true;
                     fAddresses.add(new InternetAddress(oldPf.getAutoForwardEmail()));
                 }
             }

             /** determine if current user is equal to recipient */
             Boolean isRecipientCurrentUser =
                 (currentUserAsString.equals(userId) ? Boolean.TRUE : Boolean.FALSE);


                 PrivateMessageRecipientImpl receiver = new PrivateMessageRecipientImpl(
                         userId, typeManager.getReceivedPrivateMessageType(), getContextId(),
    					isRecipientCurrentUser, bcc);
                 recipientList.add(receiver);
             }

         if (asEmail)
         {
         //send as 1 action to all recipients
         //we need to add som headers
         additionalHeaders.add("From: " + systemEmail);
         additionalHeaders.add("Subject: " + message.getTitle());
    	emailService.sendToUsers(recipients.keySet(), additionalHeaders, bodyString);
         }

     if(forwardingEnabled)
     {
         InternetAddress fAddressesArr[] = new InternetAddress[fAddresses.size()];
         fAddressesArr = fAddresses.toArray(fAddressesArr);
         emailService.sendMail(new InternetAddress(systemEmail), fAddressesArr, message.getTitle(),
                 bodyString, fAddressesArr, null, additionalHeaders);
     }



     /** add sender as a saved recipient */
     PrivateMessageRecipientImpl sender = new PrivateMessageRecipientImpl(
             currentUserAsString, typeManager.getSentPrivateMessageType(),
        getContextId(), Boolean.TRUE, false);

     recipientList.add(sender);

     message.setRecipients(recipientList);

     savePrivateMessage(message, false);
   }
     catch (MessagingException e)
     {
         LOG.warn("PrivateMessageManagerImpl.sendPrivateMessage: exception: " + e.getMessage(), e);
     }
   }


  private String buildMessageBody(PrivateMessage message) {
	  User currentUser = UserDirectoryService.getCurrentUser();
	  StringBuilder body = new StringBuilder(message.getBody());

	  body.insert(0, "From: " + currentUser.getDisplayName() + "<p/>"); 

	  // need to determine if there are "hidden" recipients to this message.
	  // If so, we need to replace them with "Undisclosed Recipients"
	  // Identifying them is tricky now because hidden users are identified by having their
	  // names in parentheses at the end of the list. At some point, the usernames were also added to
	  // this in parentheses (although this may be overridden via a property). 
	  // So to fix this going forward, the hidden users will be indicated by brackets [],
	  // but we still need to handle the old data with hidden users in parens
	  String sendToString = message.getRecipientsAsText();
	  
	  if (sendToString.indexOf(PrivateMessage.HIDDEN_RECIPIENTS_START) > 0) {
	      sendToString = sendToString.substring(0, sendToString.indexOf(PrivateMessage.HIDDEN_RECIPIENTS_START));
	      
	      // add "Undisclosed Recipients" in place of the hidden users
	      sendToString = sendToString.trim();
	      if (sendToString.length() > 0) {
	          sendToString += "; ";
	      } 
	      sendToString += getResourceBundleString("pvt_HiddenRecipients");
	  } else {
	      // we may have parens around a list of names with eid in parens
	      if (ServerConfigurationService.getBoolean("msg.displayEid", true)) {
	          String originalSendTo = sendToString;
	          sendToString = sendToString.replaceAll("\\([^)]+\\(.*", "");
	          
	          // add "Undisclosed Recipients" in place of the hidden users
	          if (!sendToString.equals(originalSendTo)) {
	              sendToString = sendToString.trim();
	              if (sendToString.length() > 0) {
	                  sendToString += "; ";
	              }
	              sendToString += getResourceBundleString("pvt_HiddenRecipients");
	          }
	      } else {
	          // the old data just has the hidden users in parens
	          if (sendToString.indexOf("(") > 0) {
	              sendToString = sendToString.substring(0, sendToString.indexOf("("));
	              
	              // add "Undisclosed Recipients" in place of the hidden users
	              sendToString = sendToString.trim();
	              if (sendToString.length() > 0) {
	                  sendToString += "; ";
	              }
	              sendToString += getResourceBundleString("pvt_HiddenRecipients");
	          }
	          
	      }
	  }

	  body.insert(0, "To: " + sendToString + "<p/>");

	  if (message.getAttachments() != null && message.getAttachments().size() > 0) {

		  body.append("<br/><br/>");
		  for (Iterator iter = message.getAttachments().iterator(); iter.hasNext();) {
			  Attachment attachment = (Attachment) iter.next();
			  //body.append("<a href=\"" + attachment.getAttachmentUrl() +
			  //"\">" + attachment.getAttachmentName() + "</a><br/>");            
			  body.append("<a href=\"" + messageManager.getAttachmentUrl(attachment.getAttachmentId()) +
					  "\">" + attachment.getAttachmentName() + "</a><br/>");            
		  }
	  }

	  String siteTitle = null;
	  try{
		  siteTitle = SiteService.getSite(getContextId()).getTitle();
	  }
	  catch (IdUnusedException e){
		  LOG.error(e.getMessage(), e);
	  }

	  String thisPageId = "";
	  ToolSession ts = sessionManager.getCurrentToolSession();
	  if (ts != null)
	  {
		  ToolConfiguration tool = SiteService.findTool(ts.getPlacementId());
		  if (tool != null)
		  {
			  thisPageId = tool.getPageId();
		  }
	  }

	  String footer = "<p>----------------------<br>" +
	  getResourceBundleString(EMAIL_FOOTER1) + " " + ServerConfigurationService.getString("ui.service") +
	  " " + getResourceBundleString(EMAIL_FOOTER2) + " \"" +
	  siteTitle + "\" " + getResourceBundleString(EMAIL_FOOTER3) + "\n" +
	  getResourceBundleString(EMAIL_FOOTER4) +
	  " <a href=\"" +
	  ServerConfigurationService.getPortalUrl() + 
	  "/site/" + ToolManager.getCurrentPlacement().getContext() +
	  "/page/" + thisPageId+
	  "\">";


	  footer += siteTitle + "</a>.</p>";                      
	  body.append(footer);

	  String bodyString = body.toString();
	  return bodyString;
  }


  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#markMessageAsReadForUser(org.sakaiproject.api.app.messageforums.PrivateMessage)
   */
  public void markMessageAsReadForUser(final PrivateMessage message)
  {
	  markMessageAsReadForUser(message, getContextId());
   }

  /**
   * FOR SYNOPTIC TOOL:
   * 	Need to pass in contextId also
   */
  public void markMessageAsReadForUser(final PrivateMessage message, final String contextId)
  {
	  markMessageAsReadForUser(message, contextId, getCurrentUser(), ToolManager.getCurrentTool().getId());
	  
  }
  
  public void markMessageAsReadForUser(final PrivateMessage message, final String contextId, final String userId, String toolId)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("markMessageAsReadForUser(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }


    /** fetch recipients for message */
    PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

    /** create PrivateMessageRecipientImpl to search for recipient to update */
    PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
        userId, typeManager.getReceivedPrivateMessageType(), contextId,
        Boolean.FALSE, false);

    List recipientList = pvtMessage.getRecipients();

    if (recipientList == null || recipientList.size() == 0)
    {
      LOG.error("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
      throw new RuntimeException("markMessageAsReadForUser(message: " + message
          + ") has empty recipient list");
    }
    
    int recordIndex = -1;
    for(int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
    	if(((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
    		recordIndex = i;
    		if (! ((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).getRead()) {
    			((PrivateMessageRecipientImpl) recipientList.get(recordIndex)).setRead(Boolean.TRUE);
    		}
    	}      
    }
    
    if(recordIndex != -1){
		decrementMessagesSynopticToolInfo(searchRecipient.getUserId(), contextId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		EventTrackingService.post(EventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(pvtMessage, toolId, userId, contextId), false));
    }
  }

  
  /**
   * @see org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager#markMessageAsReadForUser(org.sakaiproject.api.app.messageforums.PrivateMessage)
   */

  public void markMessageAsUnreadForUser(final PrivateMessage message)

  {
	  markMessageAsUnreadForUser(message, getContextId());
  }


  /**
   * FOR SYNOPTIC TOOL:
   * 	Need to pass in contextId also
   */

  public void markMessageAsUnreadForUser(final PrivateMessage message,
		  final String contextId) {

	  if (LOG.isDebugEnabled()) {
		  LOG.debug("markMessageAsUnreadForUser(message: " + message + ")");
	  }

	  if (message == null) {
		  throw new IllegalArgumentException("Null Argument");
	  }

	  final String userId = getCurrentUser();

	  /** fetch recipients for message */

	  PrivateMessage pvtMessage = getPrivateMessageWithRecipients(message);

	  /** create PrivateMessageRecipientImpl to search for recipient to update */
	  PrivateMessageRecipientImpl searchRecipient = new PrivateMessageRecipientImpl(
			  userId, typeManager.getReceivedPrivateMessageType(), contextId,
			  Boolean.TRUE, false);

	  List recipientList = pvtMessage.getRecipients();

	  if (recipientList == null || recipientList.size() == 0)
	  {
		  LOG.error("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");
		  throw new RuntimeException("markMessageAsUnreadForUser(message: " + message
				  + ") has empty recipient list");

	  }

	  int recordIndex = -1;
	  for(int i  = 0; i < pvtMessage.getRecipients().size(); i++) {
		  if(((PrivateMessageRecipientImpl) pvtMessage.getRecipients().get(i)).getUserId().equals(searchRecipient.getUserId())){
			  recordIndex = i;
			  if (((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
					  .getRead()) {
				  ((PrivateMessageRecipientImpl) recipientList.get(recordIndex))
				  .setRead(Boolean.FALSE);				  
			  }
		  }      
	  }
	  if(recordIndex != -1){
		  Site currentSite;
		  try {
			  //TODO is this only used to prevent increments if the site doesn't exit? DH
			  currentSite = SiteService.getSite(contextId);
			  incrementMessagesSynopticToolInfo(searchRecipient
					  .getUserId(), contextId,
					  SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
		  } catch (IdUnusedException e) {
			  e.printStackTrace();
		  }
		  
		  EventTrackingService.post(EventTrackingService.newEvent(
				  DiscussionForumService.EVENT_MESSAGES_UNREAD,
				  getEventMessage(pvtMessage), false));
	  }
	}

  

  private PrivateMessage getPrivateMessageWithRecipients(
      final PrivateMessage message)
  {

    if (LOG.isDebugEnabled())
    {
      LOG.debug("getPrivateMessageWithRecipients(message: " + message + ")");
    }

    if (message == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    HibernateCallback hcb = new HibernateCallback()
    {
      public Object doInHibernate(Session session) throws HibernateException,
          SQLException
      {
        Query q = session.getNamedQuery(QUERY_MESSAGES_BY_ID_WITH_RECIPIENTS);
        q.setParameter("id", message.getId(), Hibernate.LONG);
        return q.uniqueResult();
      }
    };

    PrivateMessage pvtMessage = (PrivateMessage) getHibernateTemplate()
        .execute(hcb);

    if (pvtMessage == null)
    {
      LOG.error("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
      throw new RuntimeException("getPrivateMessageWithRecipients(message: " + message
          + ") could not find message");
    }

    return pvtMessage;
  }
    
  
  public List searchPvtMsgs(String typeUuid, String searchText,Date searchFromDate, Date searchToDate, 
      boolean searchByText, boolean searchByAuthor, boolean searchByBody, boolean searchByLabel, boolean searchByDate)
  {    
    return messageManager.findPvtMsgsBySearchText(typeUuid, searchText,searchFromDate, searchToDate, 
        searchByText,searchByAuthor,searchByBody,searchByLabel,searchByDate);
  }

  public String getAuthorString() {
      String authorString = getCurrentUser();
      
      try
      {
        authorString = UserDirectoryService.getUser(authorString).getSortName();

      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      
      return authorString;
   }
   
  private String getCurrentUser()
  {
    if (TestUtil.isRunningTests())
    {
      return "test-user";
    }
    return sessionManager.getCurrentSessionUserId();
  }

  public AreaManager getAreaManager()
  {
    return areaManager;
  }

  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
  }

  public MessageForumsMessageManager getMessageManager()
  {
    return messageManager;
  }

  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }

  public void setTypeManager(MessageForumsTypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public void setSessionManager(SessionManager sessionManager)
  {
    this.sessionManager = sessionManager;
  }

  public void setIdManager(IdManager idManager)
  {
    this.idManager = idManager;
  }
  
  public void setForumManager(MessageForumsForumManager forumManager)
  {
    this.forumManager = forumManager;
  }

  public void setEmailService(EmailService emailService)
  {
    this.emailService = emailService;
  }
    
  
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(UserDirectoryService.getCurrentUser());
  }
  
  public boolean isSectionTA()
  {
    LOG.debug("isSectionTA()");
    return isSectionTA(UserDirectoryService.getCurrentUser());
  }

  /**
   * Check if the given user has site.upd access
   * 
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }
  
  private boolean isSectionTA(User user) {
      if (user != null)
          return SecurityService.unlock(user, "section.role.ta", getContextSiteId());
        else
          return false;
  }
  
  public boolean isEmailPermit() {
	  LOG.debug("isEmailPermit()");
	  return isEmailPermit(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isEmailPermit(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isEmailPermit(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, "msg.emailout", getContextSiteId());
    else
      return false;
  }
  
  
  public boolean isAllowToFieldGroups() {
	  LOG.debug("isAllowToFieldGroups()");
	  return isAllowToFieldGroups(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isAllowToFieldGroups(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isAllowToFieldGroups(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_GROUPS, getContextSiteId());
    else
      return false;
  }
  
  public boolean isAllowToFieldAllParticipants() {
	  LOG.debug("isAllowToFieldAllParticipants()");
	  return isAllowToFieldAllParticipants(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isAllowToFieldAllParticipants(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isAllowToFieldAllParticipants(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ALL_PARTICIPANTS, getContextSiteId());
    else
      return false;
  }
  
  public boolean isAllowToFieldRoles() {
	  LOG.debug("isAllowToFieldRoles()");
	  return isAllowToFieldRoles(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isAllowToFieldRoles(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isAllowToFieldRoles(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ROLES, getContextSiteId());
    else
      return false;
  }
  
  public boolean isAllowToViewHiddenGroups() {
	  LOG.debug("isAllowToViewHiddenGroups()");
	  return isAllowToViewHiddenGroups(UserDirectoryService.getCurrentUser());
  }
  
  private boolean isAllowToViewHiddenGroups(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isAllowToViewHiddenGroups(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, DefaultPermissionsManager.MESSAGE_FUNCTION_VIEW_HIDDEN_GROUPS, getContextSiteId());
    else
      return false;
  } 
  

  /**
   * @return siteId
   */
  public String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");

    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }

  public String getContextId()
  {

    LOG.debug("getContextId()");

    if (TestUtil.isRunningTests())
    {
      return "01001010";
    }
    else
    {
    	//org.sakaiproject.tool.api.ToolManager manager = getInstance();
      return ToolManager.getCurrentPlacement().getContext();
    }
  }  
     
  //Helper class
  public String getTopicTypeUuid(String topicTitle)
  {
    String topicTypeUuid;

    if(PVTMSG_MODE_RECEIVED.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getReceivedPrivateMessageType();
    }
    else if(PVTMSG_MODE_SENT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getSentPrivateMessageType();
    }
    else if(PVTMSG_MODE_DELETE.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDeletedPrivateMessageType();
    }
    else if(PVTMSG_MODE_DRAFT.equals(topicTitle))
    {
      topicTypeUuid=typeManager.getDraftPrivateMessageType();
    }
    else
    {
      topicTypeUuid=typeManager.getCustomTopicType(topicTitle);
    }
return topicTypeUuid;
  }
  
  public Area getAreaByContextIdAndTypeId(final String typeId) {
    LOG.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
    HibernateCallback hcb = new HibernateCallback() {
        public Object doInHibernate(Session session) throws HibernateException, SQLException {
            Query q = session.getNamedQuery("findAreaByContextIdAndTypeId");
            q.setParameter("contextId", getContextId(), Hibernate.STRING);
            q.setParameter("typeId", typeId, Hibernate.STRING);
            return q.uniqueResult();
        }
    };

    return (Area) getHibernateTemplate().execute(hcb);
  }
  
  /**
   * Gets Strings from Message Bundle (specifically for titles)
   * TODO: pull directly from bundle instead of using areaManager
   * 		as an intermediary
   * 
   * @param key
   * 			Message bundle key for String wanted
   * 
   * @return
   * 			String requested or "[missing key: key]" if not found
   */
  public String getResourceBundleString(String key) 
  {
//	 ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);

      return areaManager.getResourceBundleString(key);
  }

  /**
   * Constructs the event message string
   */
  private String getEventMessage(Object object) {
	  return getEventMessage(object, ToolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
	}
  
  private String getEventMessage(Object object, String toolId, String userId, String contextId) {
  	String eventMessagePrefix = "";
  	
  		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
  			eventMessagePrefix = "/messagesAndForums/site/";
  		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
  			eventMessagePrefix = "/messages/site/";
  		else
  			eventMessagePrefix = "/forums/site/";
  	
  	return eventMessagePrefix + contextId + "/" + object.toString() + "/" + userId;
  }

}
