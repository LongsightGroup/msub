/**
 * Copyright (c) 2015 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.coursemanagement.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseSiteRemovalService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;


/**
 * This class is an implementation of the auto site removal service interface.
 */
public class CourseSiteRemovalServiceImpl extends HibernateDaoSupport implements CourseSiteRemovalService {

   // logger
   private final transient Log logger = LogFactory.getLog(getClass());

   // class members
   private static final long ONE_DAY_IN_MS = 1000L * 60L * 60L * 24L;    // one day in ms = 1000ms/s · 60s/m · 60m/h · 24h/day

   // sakai services
   private CourseManagementService courseManagementService;
   private FunctionManager functionManager;
   private SecurityService securityService;
   private SiteService siteService;

   /**
    * called by the spring framework.
    */
   public void destroy() {
      logger.debug("destroy()");

       // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated, this method registers the permissions necessary to invoke the course site removal service.
    */
   public void init() {
      logger.debug("init()");

      // register permissions with sakai
      functionManager.registerFunction(PERMISSION_COURSE_SITE_REMOVAL);
   }

   /**
    * returns the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    */
   public CourseManagementService getCourseManagementService() {
      return courseManagementService;
   }

   /**
    * called by the spring framework to initialize the courseManagementService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param courseManagementService   the implementation of the CourseManagementService interface provided by the spring framework.
    */
   public void setCourseManagementService(CourseManagementService courseManagementService) {
      this.courseManagementService = courseManagementService;
   }
   /**
    * returns the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public FunctionManager getFunctionManager() {
      return functionManager;
   }

   /**
    * called by the spring framework to initialize the functionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param functionManager   the implementation of the FunctionManager interface provided by the spring framework.
    */
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }

   /**
    * returns the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SecurityService getSecurityService() {
      return securityService;
   }

   /**
    * called by the spring framework to initialize the securityService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param securityService   the implementation of the SecurityService interface provided by the spring framework.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * returns the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SiteService getSiteService() {
      return siteService;
   }

   /**
    * called by the spring framework to initialize the siteService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param siteService   the implementation of the SiteService interface provided by the spring framework.
    */
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   /**
    * removes\\unpublishes course sites whose terms have ended and a specified number of days have passed.
    * Once a term has ended, the course sites for that term remain available for a specified number of days, whose duration is specified in sakai.properties
    * via the <i>course_site_removal_service.num_days_after_term_ends</i> property.  After the specified period has elapsed, this invoking this service will either
    * remove or unpublish the course site, depending on the value of the <i>course_site_removal_service.action</i> sakai property.
    * </br></br>
    * @param action                 whether to delete the course site or to simply unpublish it.
    * @param numDaysAfterTermEnds   number of days after a term ends when course sites expire.
    * </br></br>
    * @return the number of course sites that were removed\\unpublished.
    */

    public int removeCourseSites(CourseSiteRemovalService.Action action, int numDaysAfterTermEnds) {

       logger.info("removeCourseSites(" + action + " course sites, " + numDaysAfterTermEnds + " days after the term ends)");
       Date today           = new Date();
       Date expirationDate  = new Date(today.getTime() - numDaysAfterTermEnds * ONE_DAY_IN_MS);
       int  numSitesRemoved = 0;

       try {
         // get the list of the academic term(s)
         List<AcademicSession> academicSessions = courseManagementService.getAcademicSessions();

         for(AcademicSession academicSession : academicSessions) {
            // see if the academic session ended more than the specified number of days ago
            //
            if (academicSession.getEndDate().getTime() < expirationDate.getTime()) {
               // get a list of all published course sites in ascending creation date order which are associated with the specified academic session
               Hashtable<String, String> propertyCriteria = new Hashtable<String, String>();
               propertyCriteria.put("term_eid", academicSession.getEid());
System.out.println("zz2: " + academicSession.getEid());
                List<String> sites = (List<String>)siteService.getSiteIds(SelectionType.ANY, "course", null, propertyCriteria, SortType.CREATED_ON_ASC, null);
System.out.println("zz3: " + java.util.Arrays.toString(sites.toArray()));

                for(String siteId : sites) {
                     // see if this service has already removed/unpublished this course site once before.
                     // if it has, then someone has manually published the site, and wants the course to be published.
                     // so don't switch it back to being unpublished - just leave it as published.
                    Site site = siteService.getSite(siteId);
                    ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
                     String siteProperty   = siteProperties.getProperty(SITE_PROPERTY_COURSE_SITE_REMOVAL);
                     if (!"set".equals(siteProperty)) {
                        // check permissions

                        if (!checkPermission(PERMISSION_COURSE_SITE_REMOVAL, site.getId())) {
                            logger.error("You do not have permission to " + action + " the " + site.getTitle() + " course site (" + site.getId() + ").");
                        } else if (action == CourseSiteRemovalService.Action.remove) {
                            // remove the course site
                            logger.debug(action + "removing course site " + site.getTitle() + " (" + site.getId() + ").");
                            siteService.removeSite(site);
                        } else {
                            // unpublish the course site
                            logger.debug("unpublishing course site " + site.getTitle() + " (" + site.getId() + ").");
                            siteProperties.addProperty(SITE_PROPERTY_COURSE_SITE_REMOVAL, "set");
                            site.setPublished(false);
                            siteService.save(site);
                        }
                        numSitesRemoved++;

                    }
               }
            }
         }
       } catch (Exception ex) {
         logger.error(ex);
       }
       return numSitesRemoved;
    }


    private boolean checkPermission(String lock, String reference)
    {
        return securityService.unlock(lock, reference);
    }

}
