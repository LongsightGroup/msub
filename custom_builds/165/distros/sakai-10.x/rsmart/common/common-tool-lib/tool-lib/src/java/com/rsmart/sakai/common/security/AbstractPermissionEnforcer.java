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
package com.rsmart.sakai.common.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.metaobj.security.impl.sakai.AuthnManager;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;



/**
 * <p>Checks that the current agent has permission in the current site.  Throws a PermissionException if the check fails,
 * otherwise lets the method call continue.</p>
 *
 * <p>To use inject the permission (function) name and wire to an advisor and target.</p>
 */
public class AbstractPermissionEnforcer {
   protected final Log logger = LogFactory.getLog(getClass());

   private UserDirectoryService userDirectoryService;
   private String               permission;


   protected void checkPermission() throws PermissionException {
      String currentSiteId = getWorksiteManager().getCurrentWorksiteId().getValue();
      User   user          = userDirectoryService.getCurrentUser();

      if (!SecurityService.unlock(permission, SiteService.siteReference(currentSiteId))) {
         throw new PermissionException(user.getId(), permission, currentSiteId);
      }
      logger.debug("agent [" + user.getId() + "] has permission [" + permission + "] in site [" + currentSiteId + "]");
   }

   public void setPermission(String permission) {
      this.permission = permission;
   }

   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   private WorksiteManager getWorksiteManager() {
      return (WorksiteManager)ComponentManager.get(WorksiteManager.class);
   }

   public AuthnManager getAuthnManager() {
      return (AuthnManager)ComponentManager.getInstance().get(AuthnManager.class.getName());
   }
}
