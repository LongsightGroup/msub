/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
package org.theospi.portfolio.migration;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.metaobj.shared.mgt.MetaobjEntityManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.exception.PermissionException;




/**
 * this quartz job iterates through the resources looking for forms (files with a mime-type of "application/x-osp") and sets the property
 * "sakai:reference-root" to a value of "metaobj".
 */
public class UpdateFormPropsJob implements Job {
   // logger
   protected final transient Log logger = LogFactory.getLog(getClass());

   // sakai services (injected via the spring frameworks IoC)
   private ContentHostingService contentHostingService;
   private SessionManager        sessionManager;



   /**
    * sets the property "sakai:reference-root" to a value of "metaobj" for all forms in content hosting (resources).
    * <br/><br/>
    * @param context   quartz job execution context.
    * <br/><br/>
    * @throws JobExecutionException   if the job fails to run.
    */
   public void execute(JobExecutionContext context) throws JobExecutionException {
      logger.info("Quartz job started: "+this.getClass().getName());

      // become the 'admin user'
      Session sakaiSession = sessionManager.getCurrentSession();
      String   userId      = sakaiSession.getUserId();
      sakaiSession.setUserId ("admin");
      sakaiSession.setUserEid("admin");

      // get a list of all the resources
      List<ContentResource> resources = (List<ContentResource>)contentHostingService.getAllResources("/");
      logger.info("found " + resources.size() + " resource files");

      // add a resource property for each form
      int numForms        = 0;
      int numFormsUpdated = 0;

      for (ContentResource resource : resources) {
         try {
            if (resource.getContentType().equalsIgnoreCase("application/x-osp")) {
               numForms++;
               contentHostingService.addProperty(resource.getId(), ContentHostingService.PROP_ALTERNATE_REFERENCE, MetaobjEntityManager.METAOBJ_ENTITY_PREFIX);
               numFormsUpdated += 1;
            }
         } catch (PermissionException ex) {
            // when a user submits a cell in a matrix for evaluation, a row will be added to the content_resource_lock table which prevents anyone from modifying
            // the submitted resources associated with that cell.  This causes exceptions to be thrown when trying to add the "sakai:reference-root" property
            // with a value of "metaobj" to the resource.
            logger.warn("Unable to add the property " + ContentHostingService.PROP_ALTERNATE_REFERENCE + " to the resource " + resource.getId() + " because it is locked in the content_resource_lock table.");
         } catch (Exception ex) {
            logger.warn("Failed to update properties for resource " + resource.getId(), ex);
         }
      }
      logger.info("updated " + numFormsUpdated + " of " + numForms + " forms.  " + (numForms - numFormsUpdated) + " forms could not be updated.");

      // revert back to the original logged in user
      sakaiSession.setUserEid(userId);
      sakaiSession.setUserId (userId);

      logger.info("update form properties quartz job finished");
   }

    /**
    * called by the spring framework to initialize the contentHostingService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param contentHostingService   the implementation of the ContentHostingService interface provided by the spring framework.
    */
   public void setContentHostingService(ContentHostingService contentHostingService) {
      this.contentHostingService = contentHostingService;
   }

   /**
    * called by the spring framework to initialize the sessionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param sessionManager   the implementation of the SessionManager interface provided by the spring framework.
    */
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }
}

