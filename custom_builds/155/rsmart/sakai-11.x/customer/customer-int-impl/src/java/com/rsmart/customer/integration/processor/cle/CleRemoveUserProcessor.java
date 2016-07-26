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

package com.rsmart.customer.integration.processor.cle;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;

import java.util.List;
import java.util.Iterator;

/**
 * CLE User Processor
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CleRemoveUserProcessor extends BaseCsvFileProcessor {

	/** UDS */
	private UserDirectoryService uds;

	/** Update Allowed Flag */
	private boolean lookupByUserId = false;
   private SiteService siteService;
   private SessionManager sessionManager;

   private static final Log logger = LogFactory.getLog(BaseCsvFileProcessor.class);


   /**
	 * Get Title
	 *
	 * @return String
	 */
	public String getProcessorTitle() {
		return "SIS Remove User Processor";
	}

	/**
	 * Process Row
	 *
	 * @param data
	 */
	public void processRow(String[] data, ProcessorState state) throws Exception {
      UserEdit userEdit = null;
      String id = data[0];

      // if we have a userEdit id assume we are looking userEdit up by this id
      if (lookupByUserId) {
         // Lookup by user id
         try {
            userEdit = uds.editUser(id);
         }
         catch(UserNotDefinedException ex) {
         }
      } else {
         // Lookup by EID
         try {
            User user = uds.getUserByEid(id);
            userEdit = uds.editUser(user.getId());
         }
         catch(UserNotDefinedException ex) {
         }
      }

      // handles case where user has been removed from a provider or the system
      // but the memberships have not been cleaned out
      if  (userEdit == null) {
         logger.info("can't find user with id: " + id + " temporarily creating them.");
         userEdit = getUserDirectoryService().addUser(id, id);
      }

      Session sakaiSession = sessionManager.getCurrentSession();
      String adminUser = sakaiSession.getUserId();

      sakaiSession.setUserId(userEdit.getId());
      sakaiSession.setUserEid(userEdit.getEid());

      try {
         // remove any memberships for this user
         // collect the user's sites
         List sites = siteService.getSites(
               org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null,
               null, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
               null);
         for (Iterator i=sites.iterator(); i.hasNext();) {
            Site site = (Site) i.next();            
            site.removeMember(userEdit.getId());
            logger.debug("removing user:" + userEdit.getId() + " from site: " + site.getReference());
         }
      } finally {
         sakaiSession.setUserId(adminUser);
         sakaiSession.setUserEid(adminUser);
      }

      //remove the user
      getUserDirectoryService().removeUser(userEdit);
      logger.debug("removing user:" + userEdit.getId());

	}


	/**
	 * Get User Directory Service
	 *
	 * @return
	 */
	public UserDirectoryService getUserDirectoryService() {
		return this.uds;
	}

	/**
	 * Set User Directory Service
	 *
	 * @param uds
	 */
	public void setUserDirectoryService(UserDirectoryService uds) {
		this.uds = uds;
	}

   public boolean isLookupByUserId() {
      return lookupByUserId;
   }

   public void setLookupByUserId(boolean lookupByUserId) {
      this.lookupByUserId = lookupByUserId;
   }

   public SiteService getSiteService() {
      return siteService;
   }

   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   public SessionManager getSessionManager() {
      return sessionManager;
   }

   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }
}