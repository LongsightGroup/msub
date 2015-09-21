/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.rsmart.course_site_removal.impl;

import com.rsmart.course_site_removal.intf.CourseSiteRemovalService;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.metaobj.security.AuthorizationFacade;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;



/**
 * This class is an implementation of the auto site removal service interface.
 */
public class CourseSiteRemovalServiceImpl extends HibernateDaoSupport implements CourseSiteRemovalService {
   // logger
   private final transient Log  logger = LogFactory.getLog(getClass());

   // class members
   private static final long ONE_DAY_IN_MS = 1000L * 60L * 60L * 24L;    // one day in ms = 1000ms/s · 60s/m · 60m/h · 24h/day


   // sakai services
   private AuthorizationFacade     authorizationFacade;
   private CourseManagementService courseManagementService;
   private FunctionManager         functionManager;
   private IdManager               idManager;
   private SecurityService         securityService;
   private SiteService             siteService;
   private List <String>removeRoles = new ArrayList<String>();
   private String roles = "Student";
   private boolean inactivateMembers = false;

   /**
    * called by the spring framework.
    */
   public void destroy() {
      logger.debug("destroy()");
       // parse comma separated string into a list, this way we can set it via local.properties
        if (getRoles() != null && getRoles().length() > 0) {
            String[] items = roles.split(",");
            removeRoles.addAll(Arrays.asList(items));
        }
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
    * returns the instance of the AuthorizationFacade injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the AuthorizationFacade injected by the spring framework specified in the components.xml file via IoC.
    */
   public AuthorizationFacade getAuthorizationFacade() {
      return authorizationFacade;
   }

   /**
    * called by the spring framework to initialize the authorizationFacade data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param authorizationFacade   the implementation of the AuthorizationFacade interface provided by the spring framework.
    */
   public void setAuthorizationFacade(AuthorizationFacade authorizationFacade) {
      this.authorizationFacade = authorizationFacade;
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
    * returns the instance of the IdManager injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the IdManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public IdManager getIdManager() {
      return idManager;
   }

   /**
    * called by the spring framework to initialize the idManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param idManager   the implementation of the IdManager interface provided by the spring framework.
    */
   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
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
    * via the <i>rsmart.course_site_removal.num_days_after_term_ends</i> property.  After the specified period has elapsed, this invoking this service will either
    * remove or unpublish the course site, depending on the value of the <i>rsmart.course_site_removal.action</i> sakai property.
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
            if (academicSession.getEndDate().getTime() < expirationDate.getTime()) {

               // get a list of all published course sites in ascending creation date order which are associated with the specified academic session
               Hashtable<String, String> propertyCriteria = new Hashtable<String, String>();
               propertyCriteria.put("term_eid", academicSession.getEid());
               List<Site>                sites            = (List<Site>)siteService.getSites(SelectionType.NON_USER, "course", null, propertyCriteria, SortType.CREATED_ON_ASC, null);

               for(Site site : sites) {
                  site.loadAll();
                  String siteTermEid = site.getProperties().getProperty("term_eid");
                  if (siteTermEid != null && siteTermEid.length() > 0 && siteTermEid.trim().equals(academicSession.getEid().trim())){
                     // see if this service has already removed/unpublished this course site once before.
                     // if it has, then someone has manually published the site, and wants the course to be published.
                     // so don't switch it back to being unpublished - just leave it as published.
                     ResourceProperties siteProperties = site.getProperties();
                     String             siteProperty   = siteProperties.getProperty(SITE_PROPRTY_COURSE_SITE_REMOVAL);
                    if (siteProperty == null) {
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
                            siteProperties.addProperty(SITE_PROPRTY_COURSE_SITE_REMOVAL, "set");
                            site.setPublished(false);
                            siteService.save(site);
                            updateActiveStatus(site);
                        }
                        numSitesRemoved++;

                    }
                  }
               }
            }
         }
      } catch (Exception ex) {
         logger.error(ex);
      }
      return numSitesRemoved;
   }

    private void updateActiveStatus(Site site) {
        if (isInactivateMembers() == false) return;

        Set set = site.getMembers();
        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            Member member = (Member) iter.next();
            if (isRemoveRole(member.getRole().getId())) {
                member.setActive(false);
                site.addMember(member.getUserId(), member.getRole().getId(), false, member.isProvided());

            }
        }
        // Save Site Membership
        try {
            siteService.saveSiteMembership(site);
        } catch (Exception e) {
            logger.error("failure updating site memberships for site[" + site.getId() + "] during unpublish job", e);
        }
    }

    public boolean isRemoveRole(String roleId) {
        if (removeRoles != null && removeRoles.size() > 0) {
            for (int i = 0; i < removeRoles.size(); i++) {
                if (removeRoles.get(i).toString().equals(roleId)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
    * check whether the current user has the given permission for the specified site.
    * <br/><br/>
    * @param permission  the permission the user wishes to perform and whose permission must be checked against the user's role.
    * @param siteId      id of the site to check permission for.
    * <br/><br/>
    * @return  whether the current user has the given permission for the specified site.
    * <br/><br/>
    * @throws IllegalArgumentException    if the specified permission is not a supported permission.
    */
   private boolean checkPermission(String permission, String siteId) throws IllegalArgumentException {
      Id      id            = idManager.getId(siteId);
      boolean hasPermission = false;

      if (permission == null || permission.trim().length() == 0) {
         throw new IllegalArgumentException("The permission can not be null or empty.");
      } else if (securityService.isSuperUser()) {
         hasPermission = true;
      } else if (permission.equals(PERMISSION_COURSE_SITE_REMOVAL)) {
         hasPermission = authorizationFacade.isAuthorized(permission, id);
      } else {
         throw new IllegalArgumentException(permission + " is not a supported permission for course site removal.  It must be " + PERMISSION_COURSE_SITE_REMOVAL + ".");
      }
      return hasPermission;
   }

    public List<String> getRemoveRoles() {
        return removeRoles;
    }

    public void setRemoveRoles(List<String> removeRoles) {
        this.removeRoles = removeRoles;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isInactivateMembers() {
        return inactivateMembers;
    }

    public void setInactivateMembers(boolean inactivateMembers) {
        this.inactivateMembers = inactivateMembers;
    }
}
