/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/DefaultPermissionsManagerImpl.java $
 * $Id: DefaultPermissionsManagerImpl.java 13692 2009-04-24 16:14:32Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class DefaultPermissionsManagerImpl 
    implements DefaultPermissionsManager 
{
  private static final Log LOG = LogFactory.getLog(DefaultPermissionsManagerImpl.class);
  //Dependency injected
  private FunctionManager functionManager;
  private AuthzGroupService authzGroupService;

  
  public void init()
  {
     LOG.info("init()");
     
     
     Collection registered = functionManager.getRegisteredFunctions(DefaultPermissionsManager.MESSAGE_FUNCTION_PREFIX);
     if (!registered.contains(DefaultPermissionsManager.MESSAGE_FUNCTION_EMAIL)) {
         functionManager.registerFunction(DefaultPermissionsManager.MESSAGE_FUNCTION_EMAIL);
     }
     if (!registered.contains(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ALL_PARTICIPANTS)) {
         functionManager.registerFunction(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ALL_PARTICIPANTS);
     }
     if (!registered.contains(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_GROUPS)) {
         functionManager.registerFunction(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_GROUPS);
     }
     if (!registered.contains(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ROLES)) {
         functionManager.registerFunction(DefaultPermissionsManager.MESSAGE_FUNCTION_ALLOW_TO_FIELD_ROLES);
     }     
     if (!registered.contains(DefaultPermissionsManager.MESSAGE_FUNCTION_VIEW_HIDDEN_GROUPS)) {
         functionManager.registerFunction(DefaultPermissionsManager.MESSAGE_FUNCTION_VIEW_HIDDEN_GROUPS);
     }

     
     
/*    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_NEW_FORUM);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_NEW_TOPIC);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_NEW_RESPONSE);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_NEW_RESPONSE_TO_RESPONSE);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_MOVE_POSTINGS);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_CHANGE_SETTINGS);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_POST_TO_GRADEBOOK);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_READ);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_REVISE_ANY);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_REVISE_OWN);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_DELETE_ANY);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_DELETE_OWN);
    functionManager.registerFunction(DefaultPermissionsManager.FUNCTION_MARK_AS_READ);*/
  }
  /**
   * @param functionManager The functionManager to set.
   */
  public void setFunctionManager(FunctionManager functionManager)
  {
    this.functionManager = functionManager;
  }
  
  /**
   * @param authzGroupService The authzGroupService to set.
   */
  public void setAuthzGroupService(AuthzGroupService authzGroupService)
  {
    this.authzGroupService = authzGroupService;
  }
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isNewForum(java.lang.String)
   */
  public boolean isNewForum(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_NEW_FORUM);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isNewTopic(java.lang.String)
   */
  public boolean isNewTopic(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_NEW_TOPIC);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isNewResponse(java.lang.String)
   */
  public boolean isNewResponse(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_NEW_RESPONSE);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isResponseToResponse(java.lang.String)
   */
  public boolean isResponseToResponse(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_NEW_RESPONSE_TO_RESPONSE);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isMovePostings(java.lang.String)
   */
  public boolean isMovePostings(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_MOVE_POSTINGS);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isChangeSettings(java.lang.String)
   */
  public boolean isChangeSettings(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_CHANGE_SETTINGS);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isPostToGradeBook(java.lang.String)
   */
  public boolean isPostToGradebook(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_POST_TO_GRADEBOOK);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isRead(java.lang.String)
   */
  public boolean isRead(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_READ);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isReviseAny(java.lang.String)
   */
  public boolean isReviseAny(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_REVISE_ANY);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isReviseOwn(java.lang.String)
   */
  public boolean isReviseOwn(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_REVISE_OWN);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isDeleteAny(java.lang.String)
   */
  public boolean isDeleteAny(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_DELETE_ANY);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isDeleteOwn(java.lang.String)
   */
  public boolean isDeleteOwn(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_DELETE_OWN);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.DefaultPermissionsManager#isMarkAsRead(java.lang.String)
   */
  public boolean isMarkAsRead(String role)
  {
    return hasPermission(role, DefaultPermissionsManager.FUNCTION_MARK_AS_READ);
  }
  
  private boolean hasPermission(String role, String permission)
  {
    Collection realmList = new ArrayList();
    realmList.add(getContextSiteId());
    AuthzGroup authzGroup=null;
    try
    {
      authzGroup  = authzGroupService.getAuthzGroup("!site.helper");
    }
    catch (Exception e)
    {
     LOG.info("No site helper template found");
    }    
    if(authzGroup!=null)
    {
      realmList.add(authzGroup.getId());
    }
    Set allowedFunctions = authzGroupService.getAllowedFunctions(role, realmList);
    return allowedFunctions.contains(permission);
  }
  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }
}
