/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageCommentImpl;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageGroupImpl;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItemAttributeImpl;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntryImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswerImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseImpl;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotals;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotalsImpl;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.SimpleStudentPageImpl;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEval;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalImpl;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResultImpl;
import org.sakaiproject.lessonbuildertool.SimplePageProperty;
import org.sakaiproject.lessonbuildertool.SimplePagePropertyImpl;

import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateTemplate;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

public class SimplePageToolDaoImpl extends HibernateDaoSupport implements SimplePageToolDao {
	private static Log log = LogFactory.getLog(SimplePageToolDaoImpl.class);

	private ToolManager toolManager;
	private SecurityService securityService;
	private SqlService sqlService;
	private static String SITE_UPD = "site.upd";

        // part of HibernateDaoSupport; this is the only context in which it is OK
        // to modify the template configuration
	protected void initDao() throws Exception {
		super.initDao();
		getHibernateTemplate().setCacheQueries(true);
		log.info("initDao template " + getHibernateTemplate());
		SimplePageItemImpl.setSimplePageToolDao(this);
	}

	// the permissions model here is preliminary. I'm not convinced that all the code in
	// upper layers checks where it should, so the Dao is supplying an extra layer of
	// protection. As far as I can tell, any database change should be done by
	// someone with update privs, except that add or update to the log is done on
	// behalf of normal people. I've checked all the code that does save or update for
	// log entries and it looks OK.

	public boolean canEditPage() {
		String ref = null;
		// no placement, startup testing, should be an advisor in place
		try {
			ref = "/site/" + toolManager.getCurrentPlacement().getContext();
		} catch (java.lang.NullPointerException ignore) {
			ref = "";
		}
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
	}
	
	public boolean canEditPage(long pageId) {
		boolean canEdit = canEditPage();
		// forced comments have a pageid of -1, because they are associated with
		// more than one page. But the student can't edit them anyway, so fail it
		if(!canEdit && pageId != -1L) {
			SimplePage page = getPage(pageId);
			String owner = page.getOwner();
			String group = page.getGroup();
			if (group != null)
			    group = "/site/" + page.getSiteId() + "/group/" + group;
			String currentUser = UserDirectoryService.getCurrentUser().getId();
			if (currentUser != null) {
			    if (group == null && currentUser.equals(owner))
				canEdit = true;
			    else if (group != null && AuthzGroupService.getUserRole(currentUser, group) != null)
				canEdit = true;
			}
		}
		
		return canEdit;
	}
	
	public boolean canEditPage(SimplePage page) {
		boolean canEdit = canEditPage();
		// forced comments have a pageid of -1, because they are associated with
		// more than one page. But the student can't edit them anyway, so fail it
		if(!canEdit && page != null) {
			String owner = page.getOwner();
			String group = page.getGroup();
			if (group != null)
			    group = "/site/" + page.getSiteId() + "/group/" + group;
			String currentUser = UserDirectoryService.getCurrentUser().getId();
			if (currentUser != null) {
			    if (group == null && currentUser.equals(owner))
				canEdit = true;
			    else if (group != null && AuthzGroupService.getUserRole(currentUser, group) != null)
				canEdit = true;
			}
		}
		return canEdit;
	}

	public void setSecurityService(SecurityService service) {
		securityService = service;
	}

	public void setSqlService(SqlService service) {
		sqlService = service;
	}

	public void setToolManager(ToolManager service) {
		toolManager = service;
	}

	public List<SimplePageItem> findItemsOnPage(long pageId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);
		
		Collections.sort(list, new Comparator<SimplePageItem>() {
			public int compare(SimplePageItem a, SimplePageItem b) {
				return Integer.valueOf(a.getSequence()).compareTo(b.getSequence());
			}
		});
		
		return list;
	}

	public void flush() {
	    getHibernateTemplate().flush();
	}

	public List<SimplePageItem> findItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = sqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b,SAKAI_SITE_PAGE c where a.siteId = ? and a.parent is null and a.pageId = b.sakaiId and b.type = 2 and b.pageId = 0 and a.toolId = c.PAGE_ID order by c.SITE_ORDER", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}

	public List<SimplePageItem> findDummyItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = sqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.sakaiId = '/dummy'", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}


	public List<SimplePageItem> findTextItemsInSite(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;
	    List<String> ids = sqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.type = 5", fields, null);

	    List<SimplePageItem> result = new ArrayList<SimplePageItem>();
	    
	    if (result != null) {
		for (String id: ids) {
		    SimplePageItem i = findItem(new Long(id));
		    result.add(i);
		}
	    }
	    return result;
	}

    public PageData findMostRecentlyVisitedPage(final String userId, final String toolId) {
    	Object [] fields = new Object[4];
    	fields[0] = userId;
    	fields[1] = toolId;
    	fields[2] = userId;
    	fields[3] = toolId;
    	
    	List<PageData> rv = sqlService.dbRead("select a.itemId, a.id, b.sakaiId, b.name from lesson_builder_log a, lesson_builder_items b where a.userId=? and a.toolId=? and a.lastViewed = (select max(lastViewed) from lesson_builder_log where userId=? and toolId = ?) and a.itemId = b.id", fields, new SqlReader() {
    		public Object readSqlResultRecord(ResultSet result) {
    			try {
    				PageData ret = new PageData();
    				ret.itemId = result.getLong(1);
    				ret.pageId = result.getLong(3);
    				ret.name = result.getString(4);
    				
    				return ret;
    			} catch (SQLException e) {
    				log.warn("findMostRecentlyVisitedPage: " + toolId + " : " + e);
    				return null;
    			}
    		}
    	});


    	if (rv != null && rv.size() > 0)
    		return rv.get(0);
    	else
    		return null;
	}

	public SimplePageItem findItem(long id) {
	    
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("id", id));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public SimplePageProperty findProperty(String attribute) {
	    
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageProperty.class).add(Restrictions.eq("attribute", attribute));
		
		List<SimplePageProperty> list = null;
		try {
		    list = getHibernateTemplate().findByCriteria(d);
		} catch (org.hibernate.ObjectNotFoundException e) {
		    return null;
		}

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

        public SimplePageProperty makeProperty(String attribute, String value) {
	    return new SimplePagePropertyImpl(attribute, value);
	}

	public List<SimplePageComment> findComments(long commentWidgetId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("itemId", commentWidgetId));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItems(List<Long> commentItemIds) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItemsByAuthor(List<Long> commentItemIds, String author) {
		if ( commentItemIds == null || commentItemIds.size() == 0)
		    return new ArrayList<SimplePageComment>();

		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.in("itemId", commentItemIds))
				.add(Restrictions.eq("author", author));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnItemByAuthor(long commentWidgetId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
		    .add(Restrictions.eq("itemId", commentWidgetId))
		    .add(Restrictions.eq("author", author));

		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public List<SimplePageComment> findCommentsOnPageByAuthor(long pageId, String author) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class)
			.add(Restrictions.eq("pageId", pageId))
			.add(Restrictions.eq("author", author));
		
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public SimplePageComment findCommentById(long commentId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("id", commentId));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageComment findCommentByUUID(String commentUUID) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageComment.class).add(Restrictions.eq("UUID", commentUUID));
		List<SimplePageComment> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimplePageItem findCommentsToolBySakaiId(String sakaiId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", sakaiId));
		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);
		
		// We loop through and check type here in-case something else has the same
		// sakaiId, and to prevent creating a new index for something that probably
		// doesn't really need it.  There shouldn't be more than a couple of matches
		// with different types.
		for(SimplePageItem item : list) {
			if(item.getType() == SimplePageItem.COMMENTS) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<SimplePageItem> findItemsBySakaiId(String sakaiId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", sakaiId));
		return getHibernateTemplate().findByCriteria(d);
	}
	
    // find the student's page. In theory we keep them from doing a second page. With
    // group pages that means students in more than one group can only do one. So return the first
    // Different versions if item is controlled by group or not. That lets us use simple
    // hibernate queries and maximum caching
	public SimpleStudentPage findStudentPage(long itemId, String owner) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId))
			.add(Restrictions.eq("owner", owner)).add(Restrictions.eq("deleted", false));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
    // groups is set of groups to search. 
    // null groups means there are no permitted groups, so the answer is obviously null
	public SimpleStudentPage findStudentPage(long itemId, Collection<String> groups) {
		if (groups == null || groups.size() == 0) // no possible groups, so no result
		    return null;

		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId))
			.add(Restrictions.in("group", groups)).add(Restrictions.eq("deleted", false));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}

	
	public SimpleStudentPage findStudentPage(long id) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("id", id));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public SimpleStudentPage findStudentPageByPageId(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		if(list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public List<SimpleStudentPage> findStudentPages(long itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("itemId", itemId));
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}
	
	public SimplePageItem findItemFromStudentPage(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimpleStudentPage.class).add(Restrictions.eq("pageId", pageId));
	
		List<SimpleStudentPage> list = getHibernateTemplate().findByCriteria(d);
	
		if(list.size() > 0) {
			return findItem(list.get(0).getItemId());
		}else {
			return null;
		}
	}

	public SimplePageItem findTopLevelPageItemBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id))
		    .add(Restrictions.eq("pageId", 0L))
		    .add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public List<SimplePageItem> findPageItemsBySakaiId(String id) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("sakaiId", id)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		return list;
	}

	public List findControlledResourcesBySakaiId(String id, String siteId) {
	    Object [] fields = new Object[2];
	    fields[0] = id;
	    fields[1] = siteId;
	    List ids = sqlService.dbRead("select a.id from lesson_builder_items a, lesson_builder_pages b where a.sakaiId = ? and ( a.type=1 or a.type=7) and a.prerequisite = 1 and a.pageId = b.pageId and b.siteId = ?", fields, null);
	    return ids;

	}


	public SimplePageItem findNextPageItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1)).
		    add(Restrictions.eq("type",SimplePageItem.PAGE));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}

	public SimplePageItem findNextItemOnPage(long pageId, int sequence) {
	        DetachedCriteria d = DetachedCriteria.forClass(SimplePageItem.class).add(Restrictions.eq("pageId", pageId)).
		    add(Restrictions.eq("sequence", sequence+1));

		List<SimplePageItem> list = getHibernateTemplate().findByCriteria(d);

		if (list == null || list.size() < 1)
		    return null;
			
		return list.get(0);
	}
	
	public List<SimplePageQuestionAnswer> findAnswerChoices(SimplePageItem question) {
		List<SimplePageQuestionAnswer> ret = new ArrayList<SimplePageQuestionAnswer>();

		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return ret;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    SimplePageQuestionAnswer newAnswer = new SimplePageQuestionAnswerImpl((Long)answer.get("id"),
			       (String)answer.get("text"), (Boolean) answer.get("correct"));
		    ret.add(newAnswer);
		}
		return ret;
	}
	
	public boolean hasCorrectAnswer(SimplePageItem question) {
		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return false;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if ((Boolean) answer.get("correct"))
			return true;
		}
		return false;
	}

	public SimplePageQuestionAnswer findAnswerChoice(SimplePageItem question, long answerId) {
		// find new id number, max + 1
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return null;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if (answerId == (Long)answer.get("id")) {
			SimplePageQuestionAnswer newAnswer = new SimplePageQuestionAnswerImpl(answerId,(String)answer.get("text"), (Boolean) answer.get("correct"));
			return newAnswer;
		    }
		}
		return null;
	}
	
	public SimplePageQuestionResponse findQuestionResponse(long questionId, String userId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("questionId", questionId))
        		.add(Restrictions.eq("userId", userId));

        List<SimplePageQuestionResponse> list = getHibernateTemplate().findByCriteria(d);
        if(list != null && list.size() > 0) {
        	return list.get(0);
        }else {
        	return null;
        }
	}
	
	public SimplePageQuestionResponse findQuestionResponse(long responseId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("id", responseId));

        List<SimplePageQuestionResponse> list = getHibernateTemplate().findByCriteria(d);
        if(list != null && list.size() > 0) {
        	return list.get(0);
        }else {
        	return null;
        }
	}
	
	public List<SimplePageQuestionResponse> findQuestionResponses(long questionId) {
        DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponse.class).add(Restrictions.eq("questionId", questionId));

        List<SimplePageQuestionResponse> list = getHibernateTemplate().findByCriteria(d);
        return list;
	}

	public void getCause(Throwable t, List<String>elist) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		log.warn("error saving or updating: " + t.toString());
		elist.add(t.getLocalizedMessage());
	}

	public boolean saveItem(Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		
		/*
		 * This checks a lot of conditions:
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry or question response, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
	    if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
	    			&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& !(o instanceof SimplePageLogEntry || o instanceof SimplePageQuestionResponse)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}

		try {
		    getHibernateTemplate().save(o);
		    
		    if (o instanceof SimplePageItem) {
			SimplePageItem i = (SimplePageItem)o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/item/" + i.getId(), true));
		    } else if (o instanceof SimplePage) {
			SimplePage i = (SimplePage)o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.create", "/lessonbuilder/page/" + i.getPageId(), true));
		    } 

		    if(o instanceof SimplePageItem || o instanceof SimplePage) {
		    	updateStudentPage(o);
		    }
		    
		    return true;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
		    getCause(e, elist);
		    return false;
		} catch (org.hibernate.exception.DataException e) {
		    getCause(e, elist);
		    return false;
		} catch (DataAccessException e) {
		    getCause(e, elist);
		    return false;
		}
	}

    // for use within copytransfer. We don't need to do permissions, and it probably
    // doesn't make sense to log every item created
	public boolean quickSaveItem(Object o) {
		try {
			Object id = getHibernateTemplate().save(o);
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			log.warn("Hibernate could not save: " + e.toString());
			return false;
		}
	}

	public boolean deleteItem(Object o) {
		/*
		 * If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 * permissions on it. If the item isn't SimplePageItem or SimplePage, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(!(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
				&& (o instanceof SimplePage || o instanceof SimplePageItem)) {
			return false;
		}

		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/page/" + i.getPageId(), true));
		} else if(o instanceof SimplePageComment) {
			SimplePageComment i = (SimplePageComment) o;
			EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.delete", "/lessonbuilder/comment/" + i.getId(), true));
		}

		try {
			getHibernateTemplate().delete(o);
			return true;
		} catch (DataAccessException e) {
			try {
				
				/* If we have multiple objects of the same item, you must merge them
				 * before deleting.  If the first delete fails, we merge and try again.
				 */
				getHibernateTemplate().delete(getHibernateTemplate().merge(o));
				
				return true;
			}catch(DataAccessException ex) {
				ex.printStackTrace();
				log.warn("Hibernate could not delete: " + e.toString());
				return false;
			}
		}
	}

	public boolean update(Object o, List<String>elist, String nowriteerr, boolean requiresEditPermission) {
		/*
		 * This checks a lot of conditions:
		 * 1) If o is SimplePageItem or SimplePage, it makes sure it gets the right page and checks the
		 *    permissions on it.
		 * 2) If it's a log entry, it lets it go.
		 * 3) If requiresEditPermission is set to false, it lets it go.
		 * 
		 * Essentially, if any of those say that the edit is fine, it won't throw the error.
		 */
		if(requiresEditPermission && !(o instanceof SimplePageItem && canEditPage(((SimplePageItem)o).getPageId()))
				&& !(o instanceof SimplePage && canEditPage((SimplePage)o))
		   		&& !(o instanceof SimplePageLogEntry || o instanceof SimplePageQuestionResponse)
				&& !(o instanceof SimplePageGroup)) {
			elist.add(nowriteerr);
			return false;
		}
		
		if (o instanceof SimplePageItem) {
		    SimplePageItem i = (SimplePageItem)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.update", "/lessonbuilder/item/" + i.getId(), true));
		} else if (o instanceof SimplePage) {
		    SimplePage i = (SimplePage)o;
		    EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.update", "/lessonbuilder/page/" + i.getPageId(), true));
		}

		try {
			if(!(o instanceof SimplePageLogEntry)) {
				getHibernateTemplate().merge(o);
			}else {
				// Updating seems to always update the timestamp on the log correctly,
				// while merging doesn't always get it right.  However, it's possible that
				// update will fail, so we do both, in order of preference.
				try {
					getHibernateTemplate().update(o);
				}catch(DataAccessException ex) {
					log.warn("Wasn't able to update log entry, timing might be a bit off.");
					getHibernateTemplate().merge(o);
				}
			}
		    
		    if(o instanceof SimplePageItem || o instanceof SimplePage) {
		    	updateStudentPage(o);
		    }
		    
		    return true;
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
		    getCause(e, elist);
		    return false;
		} catch (org.hibernate.exception.DataException e) {
		    getCause(e, elist);
		    return false;
		} catch (DataAccessException e) {
		    getCause(e, elist);
		    return false;
		}
	}

    // ditto for update
	public boolean quickUpdate(Object o) {
		try {
			getHibernateTemplate().merge(o);
			return true;
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Long getTopLevelPageId(String toolId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("toolId", toolId)).add(Restrictions.isNull("parent"));

		List list = getHibernateTemplate().findByCriteria(d);

		if (list.size() > 1) {
			log.warn("Problem finding which page we should be on.  Doing the best we can.");
		}

		if (list != null && list.size() > 0) {
			return ((SimplePage) list.get(0)).getPageId();
		} else {
			return null;
		}
	}

	public SimplePage getPage(long pageId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("pageId", pageId));

		List l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
			return (SimplePage) l.get(0);
		} else {
			return null;
		}
	}

	public List<SimplePage> getSitePages(String siteId) {
	    DetachedCriteria d = DetachedCriteria.forClass(SimplePage.class).add(Restrictions.eq("siteId", siteId)).add(Restrictions.isNull("owner"));

		List<SimplePage> l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
		    return l;
		} else {
		    return null;
		}
	}

	public SimplePageLogEntry getLogEntry(String userId, long itemId, Long studentPageId) {
		if(studentPageId.equals(-1L)) studentPageId = null;
		
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId))
				.add(Restrictions.eq("itemId", itemId));
		
		if(studentPageId != null) {
			d.add(Restrictions.eq("studentPageId", studentPageId));
		}else {
			d.add(Restrictions.isNull("studentPageId"));
		}

		List l = getHibernateTemplate().findByCriteria(d);
		
		if (l != null && l.size() > 0) {
			return (SimplePageLogEntry) l.get(0);
		} else {
			return null;
		}
	}
	
	// owner not currently used. would need group as well
        public boolean isPageVisited(long pageId, String userId, String owner) {
	    // if this is a student page, it's most likely the top level, so do that query first
	    if (owner != null) {
		Object [] fields = new Object[3];
		fields[0] = pageId;
		fields[1] = pageId;
		fields[2] = userId;
		List<String> ones = sqlService.dbRead("select 1 from lesson_builder_student_pages a, lesson_builder_log b where a.pageId=? and a.itemId = b.itemId and b.studentPageId=? and b.userId=?", fields, null);
		if (ones != null && ones.size() > 0)
		    return true;
	    }

	    Object [] fields = new Object[2];
	    fields[0] = Long.toString(pageId);
	    fields[1] = userId;
	    List<String> ones = sqlService.dbRead("select 1 from lesson_builder_items a, lesson_builder_log b where a.sakaiId=? and a.type=2 and a.id=b.itemId and b.userId=?", fields, null);
	    if (ones != null && ones.size() > 0)
		return true;
	    else
		return false;
	}

	public List<SimplePageLogEntry> getStudentPageLogEntries(long itemId, String userId) {		
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageLogEntry.class).add(Restrictions.eq("userId", userId))
				.add(Restrictions.eq("itemId", itemId))
				.add(Restrictions.isNotNull("studentPageId"));

		List<SimplePageLogEntry> entries = getHibernateTemplate().findByCriteria(d);
		
		return entries;
	}

	public List<String> findUserWithCompletePages(Long itemId){
		Object [] fields = new Object[1];
		fields[0] = itemId;

		List<String> users = sqlService.dbRead("select a.userId from lesson_builder_log a where a.itemId = ? and a.complete = true", fields, null);

		return users;
	}

	public SimplePageGroup findGroup(String itemId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageGroup.class).add(Restrictions.eq("itemId", itemId));

		List l = getHibernateTemplate().findByCriteria(d);

		if (l != null && l.size() > 0) {
			return (SimplePageGroup) l.get(0);
		} else {
			return null;
		}
	}

	public SimplePage makePage(String toolId, String siteId, String title, Long parent, Long topParent) {
		return new SimplePageImpl(toolId, siteId, title, parent, topParent);
	}

	public SimplePageItem makeItem(long id, long pageId, int sequence, int type, String sakaiId, String name) {
		return new SimplePageItemImpl(id, pageId, sequence, type, sakaiId, name);
	}

	public SimplePageItem makeItem(long pageId, int sequence, int type, String sakaiId, String name) {
		return new SimplePageItemImpl(pageId, sequence, type, sakaiId, name);
	}


	public SimplePageGroup makeGroup(String itemId, String groupId, String groups, String siteId) {
		return new SimplePageGroupImpl(itemId, groupId, groups, siteId);
	}

	public SimplePageQuestionResponse makeQuestionResponse(String userId, long questionId) {
		return new SimplePageQuestionResponseImpl(userId, questionId);
	}

	public SimplePageLogEntry makeLogEntry(String userId, long itemId, Long studentPageId) {
		return new SimplePageLogEntryImpl(userId, itemId, studentPageId);
	}

	public SimplePageComment makeComment(long itemId, long pageId, String author, String comment, String UUID, boolean html) {
		return new SimplePageCommentImpl(itemId, pageId, author, comment, UUID, html);
	}
	
	public SimpleStudentPage makeStudentPage(long itemId, long pageId, String title, String author, String group) {
		return new SimpleStudentPageImpl(itemId, pageId, title, author, group);
	}
	
    // answer is stored as id [int], text, correct [boolean]

	public SimplePageQuestionAnswer makeQuestionAnswer(String text, boolean correct) {
		return new SimplePageQuestionAnswerImpl(0, text, correct);
	}
	
	
    // only implemented for existing questions, i.e. questions with id numbers
	public boolean deleteQuestionAnswer(SimplePageQuestionAnswer questionAnswer, SimplePageItem question) {
		if(!canEditPage(question.getPageId())) {
		    log.warn("User tried to edit question on page without edit permission. PageId: " + question.getPageId());
		    return false;
		}

		// getId returns long, so this can never be null
		Long delid = questionAnswer.getId();

		// find new id number, max + 1
		// JSON uses Long for integer values
		List answers = (List)question.getJsonAttribute("answers");
		Long max = -1L;
		if (answers == null)
		    return false;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    if (delid.equals(answer.get("id"))) {
			answers.remove(a);
			return true;
		    }
		}

		return false;
	}
	
       // methods above are historical. I leave them for completeness. THere are the ones actually used:

	public void clearQuestionAnswers(SimplePageItem question) {
		question.setJsonAttribute("answers", null);
	}

	public Long maxQuestionAnswer(SimplePageItem question)  {
		Long max = 0L;
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null)
		    return max;
		for (Object a: answers) {
		    Map answer = (Map) a;
		    Long i = (Long)answer.get("id");
		    if (i > max)
			max = i;
		}
		return max;
	}

	public Long addQuestionAnswer(SimplePageItem question, Long id, String text, Boolean isCorrect) {
		// no need to check security. that happens when item is saved
		
		List answers = (List)question.getJsonAttribute("answers");
		if (answers == null) {
		    answers = new JSONArray();
		    question.setJsonAttribute("answers", answers);
		    if (id <= 0L)
			id = 1L;
		} else if (id <= 0L) {
		    Long max = 0L;
		    for (Object a: answers) {
			Map answer = (Map) a;
			Long i = (Long)answer.get("id");
			if (i > max)
			    max = i;
		    }
		    id = max + 1;
		}
		
		// create and add the json form of the answer
		Map newAnswer = new JSONObject();
		newAnswer.put("id", id);
		newAnswer.put("text", text);
		newAnswer.put("correct", isCorrect);
		answers.add(newAnswer);

		return id;
	}
	
  
	public void clearPeerEvalRows(SimplePageItem question) {
		question.setJsonAttribute("rows", null);
	}

	public Long maxPeerEvalRow(SimplePageItem question)  {
		Long max = 0L;
		List rows = (List)question.getJsonAttribute("rows");
		if (rows == null)
		    return max;
		for (Object a: rows) {
		    Map row = (Map) a;
		    Long i = (Long)row.get("id");
		    if (i > max)
			max = i;
		}
		return max;
	}

	public void addPeerEvalRow(SimplePageItem question, Long id, String text) {
		// no need to check security. that happens when item is saved
		
		List rows = (List)question.getJsonAttribute("rows");
		if (rows == null) {
		  	rows = new JSONArray();
		    question.setJsonAttribute("rows", rows);
		    if(id <= 0L) 
		    	id = 1L;
		    }else if (id <= 0L) {
		    	Long max = 0L;
		    	for (Object r: rows) {
		    		Map row =(Map) r;
		    		Long i = (Long)row.get("id");
		    		if(i > max)
		    			max = i;
		    	}
		    	id = max +1; 
		    }
		Map newRow = new JSONObject();
		newRow.put("id", id);
		newRow.put("rowText",text);
		rows.add(newRow);

	}
	
	public SimplePageItem copyItem(SimplePageItem old) {
		SimplePageItem item =  new SimplePageItemImpl();
		item.setPageId(old.getPageId());
		item.setSequence(old.getSequence());
		item.setType(old.getType());
		item.setSakaiId(old.getSakaiId());
		item.setName(old.getName());
		item.setHtml(old.getHtml());
		item.setDescription(old.getDescription());
		item.setHeight(old.getHeight());
		item.setWidth(old.getWidth());
		item.setAlt(old.getAlt());
		item.setNextPage(old.getNextPage());
		item.setFormat(old.getFormat());
		item.setRequired(old.isRequired());
		item.setAlternate(old.isAlternate());
		item.setPrerequisite(old.isPrerequisite());
		item.setSubrequirement(old.getSubrequirement());
		item.setRequirementText(old.getRequirementText());
		item.setSameWindow(old.isSameWindow());
		item.setAnonymous(old.isAnonymous());
		item.setShowComments(old.getShowComments());
		item.setForcedCommentsAnonymous(old.getForcedCommentsAnonymous());
		item.setAttributeString(old.getAttributeString()); // copy via json
		item.setShowPeerEval(old.getShowPeerEval());

		//		Map<String, SimplePageItemAttributeImpl> attrs = ((SimplePageItemImpl)old).getAttributes();
		//		if (attrs != null) {
		//		    Collection<SimplePageItemAttributeImpl> attributes = attrs.values();
		//		    if (attributes.size() > 0) {
		//			for (SimplePageItemAttributeImpl attr: attributes) {
		//			    item.setAttribute(attr.getAttr(), attr.getValue());
		//}
		//		    }
		//		}

		return item;
	}

    // phase 2 of copy after save, we need item number here
	public SimplePageItem copyItem2(SimplePageItem old, SimplePageItem item) {
	       
		syncQRTotals(item);

		return item;
	}
	
	private void updateStudentPage(Object o) {
		SimplePage page;
		
		if(o instanceof SimplePageItem) {
			SimplePageItem item = (SimplePageItem) o;
			page = getPage(item.getPageId());
		}else if(o instanceof SimplePage) {
			page = (SimplePage) o;
		}else {
			return;
		}
		
		if(page != null && page.getTopParent() != null) {
			SimpleStudentPage studentPage = findStudentPage(page.getTopParent());
			if(studentPage != null) {
				studentPage.setLastUpdated(new Date());
				quickUpdate(studentPage);
			}
		}
	}

	public Map JSONParse(String s) {
	    return (JSONObject)JSONValue.parse(s);
	}

	public Map newJSONObject() {
	    return new JSONObject();
	}

	public List newJSONArray() {
	    return new JSONArray();
	}

	public SimplePageQuestionResponseTotals makeQRTotals(long qid, long rid) {
	    return new SimplePageQuestionResponseTotalsImpl(qid, rid);
	}

	public List<SimplePageQuestionResponseTotals> findQRTotals(long questionId) {
		DetachedCriteria d = DetachedCriteria.forClass(SimplePageQuestionResponseTotals.class).add(Restrictions.eq("questionId", questionId));
		List<SimplePageQuestionResponseTotals> list = getHibernateTemplate().findByCriteria(d);
		
		return list;
	}

	public void incrementQRCount(long questionId, long responseId) {
	    Object [] fields = new Object[2];
	    fields[0] = questionId;
	    fields[1] = responseId;
	    sqlService.dbWrite("update lesson_builder_qr_totals set respcount = respcount + 1 where questionId = ? and responseId = ?", fields);
	}

	public void syncQRTotals(SimplePageItem item) {
	    if (item.getType() != SimplePageItem.QUESTION || ! "multipleChoice".equals(item.getAttribute("questionType")))
		return;
	    
	    Map<Long, SimplePageQuestionResponseTotals> oldTotals = new HashMap<Long, SimplePageQuestionResponseTotals>();
	    List<SimplePageQuestionResponseTotals> oldQrTotals = findQRTotals(item.getId());
	    for (SimplePageQuestionResponseTotals total: oldQrTotals)
		oldTotals.put(total.getResponseId(), total);

	    for (SimplePageQuestionAnswer answer: findAnswerChoices(item)) {
		Long id = answer.getId();
		if (oldTotals.get(id) != null)
		    oldTotals.remove(id);  // in both old and new, done with it
		else {
		    // in new but not old, add it
		    SimplePageQuestionResponseTotals total = makeQRTotals(item.getId(), id);
		    quickSaveItem(total);
		}
	    }

	    // entries that were in old list but not new one, remove them
	    for (Long rid: oldTotals.keySet()) {
		deleteItem(oldTotals.get(rid));
	    }
	}

	public SimplePagePeerEval findPeerEval(long itemId) {
		SimplePageItem item = findItem(itemId);
	
		List rows = (List)item.getJsonAttribute("rows");
		if (rows == null)
		    return null;
		for (Object a: rows) {
		    Map row = (Map) a;
		}
		return null;
	}

	public List<SimplePagePeerEvalResult> findPeerEvalResult(long pageId,String userId,String gradee) 
	 //List<String> ids = sqlService.dbRead("select b.id from lesson_builder_pages a,lesson_builder_items b where a.siteId = ? and a.pageId = b.pageId and b.sakaiId = '/dummy'", fields, null);
	{
		DetachedCriteria d = DetachedCriteria.forClass(SimplePagePeerEvalResult.class)
			    .add(Restrictions.eq("pageId", pageId))
			    .add(Restrictions.eq("grader", userId))
			    .add(Restrictions.eq("gradee", gradee));
				

			List<SimplePagePeerEvalResult> list = getHibernateTemplate().findByCriteria(d);
			List<SimplePagePeerEvalResult> newList= new ArrayList<SimplePagePeerEvalResult>();
			
			for(SimplePagePeerEvalResult eval: list){
				if(eval.getSelected())
					newList.add(eval);
			}
			
			return newList;
	}

	public SimplePagePeerEvalResult makePeerEvalResult(long pageId, String gradee,String grader, String rowText, int columnValue){
		return new SimplePagePeerEvalResultImpl(pageId, gradee, grader, rowText, columnValue);
	}
	
	public List<SimplePagePeerEvalResult> findPeerEvalResultByOwner(long pageId,String pageOwner){
		DetachedCriteria d = DetachedCriteria.forClass(SimplePagePeerEvalResult.class)
			    .add(Restrictions.eq("pageId", pageId))
			    .add(Restrictions.eq("gradee", pageOwner));

		List<SimplePagePeerEvalResult> list = getHibernateTemplate().findByCriteria(d);
		List<SimplePagePeerEvalResult> newList= new ArrayList<SimplePagePeerEvalResult>();
		
		for(SimplePagePeerEvalResult eval: list){
			if(eval.getSelected())
				newList.add(eval);
		}
		
		return newList;
	}

	public List<SimplePageItem>findGradebookItems(final String gradebookUid) {

	    String hql = "select item from org.sakaiproject.lessonbuildertool.SimplePageItem item, org.sakaiproject.lessonbuildertool.SimplePage page where item.pageId = page.pageId and page.siteId = :site and (item.gradebookId is not null or item.altGradebook is not null)";
	    return getHibernateTemplate().findByNamedParam(hql, "site", gradebookUid);
	}
	    
	public List<SimplePage>findGradebookPages(final String gradebookUid) {

	    String hql = "select page from org.sakaiproject.lessonbuildertool.SimplePage page where page.siteId = :site and (page.gradebookPoints is not null)";
	    return getHibernateTemplate().findByNamedParam(hql, "site", gradebookUid);
	}

	// items in lesson_builder_groups for specified site, map of itemId to groups
	public Map<String,String> getExternalAssigns(String siteId) {

	    DetachedCriteria d = DetachedCriteria.forClass(SimplePageGroup.class)
		.add(Restrictions.eq("siteId", siteId));

	    List<SimplePageGroup> list = getHibernateTemplate().findByCriteria(d);

	    Map<String,String>ret = new HashMap<String,String>();	
	    for (SimplePageGroup group: list)
		ret.put(group.getItemId(), group.getGroups());

	    return ret;
	    
	}
    
    // return 1 if we found something, 0 if not
    // this is only going to find something once per site, typically.
    // so it's best to optimize for the normal case that nothing is there
    // initialy this called
    // dbWriteCount("delete from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields, null, null, false);
    // but that doesn't exist in 2.8.

	public int clearNeedsFixup(String siteId) {
	    Object [] fields = new Object[1];
	    fields[0] = siteId;

	    List<String> needsList = sqlService.dbRead("select VALUE from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields, null);
	    
	    // normal case -- no flag
	    if (needsList == null || needsList.size() == 0)
		return 0;
	    

	    // there is a flag, do something more carefully avoiding race conditions
	    //   There is a possible timing issue if someone copies data into the site after the
	    // last test. If so, we'll get it next time someone uses the site.
	    // we need to be provably sure that if the flag is set, this code returns 1 exactly once.
	    // I believe that is the case.

	    int retval = 0;
	    Connection conn = null;
	    boolean wasCommit = true;

	    try {
		conn = sqlService.borrowConnection();
		needsList = sqlService.dbRead(conn, "select VALUE from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup' for update", fields, null);
		wasCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		if (needsList != null && needsList.size() > 0) {
		    retval = 1;
		    sqlService.dbWrite(conn, "delete from SAKAI_SITE_PROPERTY where SITE_ID=? and NAME='lessonbuilder-needsfixup'", fields);
		}
		
		conn.commit();

	// I don't think we need to handle errrors explicitly. They will result in
	// returning 0, which is about the best we can do
	    } catch (Exception e) {
	    } finally {

		if (conn != null) {

		    try {
			conn.setAutoCommit(wasCommit);
		    } catch (Exception e) {
			System.out.println("transact: (setAutoCommit): " + e);
		    }
  
		    sqlService.returnConnection(conn);
		}

	    }

	    return retval;

	}

}
