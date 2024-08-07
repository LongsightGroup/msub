/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.component.app.podcasts;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastPermissionsService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

public class PodcastPermissionsServiceImpl implements PodcastPermissionsService {

	private Log LOG = LogFactory.getLog(PodcastPermissionsServiceImpl.class);
	
	private ContentHostingService contentHostingService;

	/**
	 * Determines if authenticated user has 'read' access to podcast collection folder
	 * 
	 * @param id
	 * 			The id for the podcast collection folder
	 * 
	 * @return
	 * 		TRUE - has read access, FALSE - does not
	 */
	public boolean allowAccess(String id) {
		return contentHostingService.allowGetCollection(id);
	}

	/**
	 * Determine whether user and update the site
	 * 
	 * @param siteId
	 *           The siteId for the site to test
	 * 
	 * @return 
	 * 			True if can update, False otherwise
	 */
	public boolean canUpdateSite() {
		return canUpdateSite(getSiteId());

	}

	/**
	 * Determine whether user and update the site
	 * 
	 * @param siteId
	 *            	The siteId for the site to test
	 * 
	 * @return True 
	 * 				True if can update, False otherwise
	 */
	public boolean canUpdateSite(String siteId) {
			return SecurityService.unlock(UPDATE_PERMISSIONS, "/site/"+ siteId);
	}
	
	/**
	 * Returns TRUE if current user has function (permission) passed in or site.upd, FALSE otherwise.
	 */
	public boolean hasPerm(String function, String resourceId) {
		return hasPerm(function, resourceId, getSiteId());
	}
	
	/**
	 * Returns TRUE if current user has function (permission) passed in or site.upd, FALSE otherwise.
	 */
	public boolean hasPerm(String function, String resourceId, String siteId) {
		if (canUpdateSite(siteId)) {
			return true;
		}
		else {
			if (resourceId != null) {
				return SecurityService.unlock(function, "/content" + resourceId);
			}
		}
	
		return false;
	}
	
	/**
	 * Determine if this entity has been restricted to specific group(s)
	 */
	public boolean isGrouped(GroupAwareEntity entity) {
		return entity.getAccess().equals(AccessMode.GROUPED);
	}

	/**
	 * Determine if current user can access this group restricted entity
	 */
	public boolean canAccessViaGroups(Collection groups, String siteId) {
		final String userId = SessionManager.getCurrentSessionUserId();
		
		Site site = null;
		try {
			site = SiteService.getSite(siteId);
		}
		catch (IdUnusedException e) {
			// Mucho Weirdness since called from within tool which should
			//   pass the correct id. But if an error
			return false;
		}
		
		for (Iterator groupIter = groups.iterator(); groupIter.hasNext();) {
			final String currentGroupId = (String) groupIter.next();
	        final Group currentGroup = site.getGroup(currentGroupId);

	        if (currentGroup != null) {
	        	final Member member = currentGroup.getMember(userId);

	        	if (member != null && member.getUserId().equals(userId)) {
	        		return true;
	        	}
	        }
		}
		
		return false;		
	}

	public boolean isResourceHidden(ContentEntity podcastResource, Date tempDate) {
		return podcastResource.isHidden() 
				|| (podcastResource.getRetractDate() != null 
						&& podcastResource.getRetractDate().getTime() <= TimeService.newTime().getTime())
				|| (tempDate != null && tempDate.getTime() >= TimeService.newTime().getTime());
	}


	///// Utility methods ////////
	
	/**
	 * Retrieve the site id
	 */
	private String getSiteId() {
		return ToolManager.getCurrentPlacement().getContext();
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

}
