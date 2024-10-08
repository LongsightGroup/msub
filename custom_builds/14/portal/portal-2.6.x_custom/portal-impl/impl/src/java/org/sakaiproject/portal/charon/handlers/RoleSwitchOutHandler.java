/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.Web;

public class RoleSwitchOutHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "role-switch-out";

	public static final String EVENT_ROLESWAP_EXIT = "roleswap.exit";

	public RoleSwitchOutHandler()
	{
		setUrlFragment(RoleSwitchOutHandler.URL_FRAGMENT);
	}
	
	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		if (parts == null || req == null || res == null || session == null)
			throw new IllegalStateException("null pointers while swapping out of student view");
		if ((parts.length > 2) && "role-switch-out".equals(parts[1]))
		{
			try
			{
				String siteUrl = req.getContextPath() + "/site"
						+ Web.makePath(parts, 2, parts.length);
				// Make sure to add the parameters such as panel=Main
				String queryString = req.getQueryString();
				if (queryString != null)
				{
					siteUrl = siteUrl + "?" + queryString;
				}
				portalService.setResetState("true"); // flag the portal to reset
				SecurityService.clearUserEffectiveRole(SiteService.siteReference(parts[2]));
				
				// Post an event
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_ROLESWAP_EXIT, null, parts[2], false, NotificationService.NOTI_NONE));				

				res.sendRedirect(siteUrl);
				return RESET_DONE;
			}
			catch (Exception ex)
			{
				throw new PortalHandlerException(ex);
			}
		}
		else
		{
			return NEXT;
		}
	}

}
