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
 *
 * Contributor(s): jbush
 */

package com.rsmart.sakai.user.impl;


import org.sakaiproject.user.api.User;
import org.sakaiproject.user.impl.DbUserService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.exception.IdUnusedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Dec 12, 2006
 * Time: 7:28:48 PM
 *
 * <p>Automatically adds a user to specific site(s) when they logon on as specified in the sakai.properties file.  Expects
 * something in the following form. </p>
 *
 * <p>autopopulate.all = Role, siteid</p>
 *
 * <p>You can specifiy more than one autopopulate.all property in the normal sakai way, eg:</p>
 *
 * autopopulate.all.count = 2<br/>
 * autopopulate.all = Student, siteid1, siteid2 <br/>
 * autopopulate.all = Technical Support, siteid3 <br/>
 *
 * <p>If the user is already a member of the site, nothing is done.  If site does not exist nothing is done.</p>
 *
 */
abstract public class AutoPopulatingUserService extends DbUserService {
   private static Log logger = LogFactory.getLog(AutoPopulatingUserService.class);

   public User authenticate(String eid, String password) {
      User user = super.authenticate(eid, password);
      if (user != null) {
         try {

            processAutoPopulate(user.getId());
         } catch (Exception e) {
            logger.error("unable to autopopulate: ", e);
         }
      }
      return user;
   }

   /**
    *
    * @param userId
    */
   protected void processAutoPopulate(String userId) throws Exception {

      String[] roleSitesString = ServerConfigurationService.getStrings("autopopulate.all");
      if (roleSitesString == null) {
         String roleSites = ServerConfigurationService.getString("autopopulate.all");
         if (roleSites == null) return;
         roleSitesString = new String[]{roleSites};
      }

      int lines = roleSitesString.length;

      //for each role
      for (int g = 0; g < lines; g++) {
         String[] line = roleSitesString[g].split(",");

         if (line == null || line.length < 2) {
            continue;
         }

         String role = line[0].trim();
         int f = line.length;

         //for each site
         for (int d = 1; d < f; d++) {

            String siteId = line[d].trim();
            Site site = null;

            try {
                site = org.sakaiproject.site.cover.SiteService.getSite(siteId);
            } catch (IdUnusedException e) {
                logger.error("failed to autopopulate user onto site: " + siteId + " no such site exists");
                continue;
            }

             if (site == null) {
                continue;
             }

            //TODO validate role in site

            if (site.getMember(userId) == null) {
               //then lets add this user for the above role
               site.addMember(userId, role, true, false);

               Session sakaiSession = SessionManager.getCurrentSession();
               String currentUserId = sakaiSession.getUserId();

               sakaiSession.setUserId("admin");
               sakaiSession.setUserEid("admin");

               org.sakaiproject.site.cover.SiteService.saveSiteMembership(site);

               sakaiSession.setUserId(currentUserId);
               sakaiSession.setUserEid(currentUserId);

            }
         }

      }

   }

}


