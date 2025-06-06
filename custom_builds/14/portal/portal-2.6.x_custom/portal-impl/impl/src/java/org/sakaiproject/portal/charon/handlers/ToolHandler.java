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

package org.sakaiproject.portal.charon.handlers;

import java.io.IOException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.portal.api.PortalRenderContext;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.Web;


/**
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 * 
 */
public class ToolHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "tool";

	public ToolHandler()
	{
		setUrlFragment(ToolHandler.URL_FRAGMENT);
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		return doGet(parts, req, res, session);
	}

	@Override
	public int doGet(String[] parts, HttpServletRequest req, HttpServletResponse res,
			Session session) throws PortalHandlerException
	{
		// recognize and dispatch the 'tool' option: [1] = "tool", [2] =
		// placement id (of a site's tool placement), rest for the tool
		if ((parts.length > 2) && (parts[1].equals(getUrlFragment())))
		{
			try
			{
				// Resolve the placements of the form
				// /portal/tool/sakai.resources?sakai.site=~csev
				String toolPlacement = portal.getPlacement(req, res, session, parts[2],
						false);
				if (toolPlacement == null)
				{
					return ABORT;
				}
				parts[2] = toolPlacement;

				doTool(req, res, session, parts[2], req.getContextPath()
						+ req.getServletPath() + Web.makePath(parts, 1, 3), Web.makePath(
						parts, 3, parts.length));
				return END;
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

	public void doTool(HttpServletRequest req, HttpServletResponse res, Session session,
			String placementId, String toolContextPath, String toolPathInfo)
			throws ToolException, IOException
	{

		if (portal.redirectIfLoggedOut(res)) return;

		// find the tool from some site
		ToolConfiguration siteTool = SiteService.findTool(placementId);
		if (siteTool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// Reset the tool state if requested
		if ("true".equals(req.getParameter(portalService.getResetStateParam()))
				|| "true".equals(portalService.getResetState()))
		{
			Session s = SessionManager.getCurrentSession();
			ToolSession ts = s.getToolSession(placementId);
			ts.clearAttributes();
		}

		// find the tool registered for this
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		if (tool == null)
		{
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		// permission check - visit the site (unless the tool is configured to
		// bypass)
		Site site = null;
		if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL)
		{
			try
			{
				site = SiteService.getSiteVisit(siteTool.getSiteId());
			}
			catch (IdUnusedException e)
			{
				portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				return;
			}
			catch (PermissionException e)
			{
				// if not logged in, give them a chance
				if (session.getUserId() == null)
				{
					portal.doLogin(req, res, session, req.getPathInfo(), false);
				}
				else
				{
					portal.doError(req, res, session, Portal.ERROR_WORKSITE);
				}
				return;
			}
		}
		
		// Check to see if the tool is visible
		if(!isToolVisible(site, siteTool)) {
			portal.doError(req, res, session, Portal.ERROR_WORKSITE);
			return;
		}

		if ( portal.isPortletPlacement(siteTool) ) 
		{
	
	                String siteType = portal.calcSiteType(siteTool.getSiteId());

       			// form a context sensitive title
	                String title = ServerConfigurationService.getString("ui.service") + " : "
                                + site.getTitle() + " : " + siteTool.getTitle();

                	PortalRenderContext rcontext = portal.startPageContext(siteType, title, 
				siteTool.getSkin(), req);

			Map m = portal.includeTool(res, req, siteTool);
			rcontext.put("tool", m);

                	portal.sendResponse(rcontext, res, "tool", null);

		} else {
			portal.forwardTool(tool, req, res, siteTool, siteTool.getSkin(), toolContextPath,
				toolPathInfo);
		}
	}
	
	/**
	 * Method to check if a tool is visible for a user in a site, based on KNL-428
	 * @param site
	 * @param toolConfig
	 * @return
	 */
	private boolean isToolVisible(Site site, ToolConfiguration toolConfig) {
		
		//no way to check, so allow access. It's then up to the tool to control permissions
		if(site == null || toolConfig == null) {
			return true;
		}
		
		String toolPermissionsStr = toolConfig.getConfig().getProperty("functions.require");

		//no special permissions required, it's visible
		if (toolPermissionsStr == null || toolPermissionsStr.trim().length() == 0) {
			return true; 
		}
		
		//check each set, if multiple permissions in the set, must have all.
		String[] toolPermissionsSets = toolPermissionsStr.split("\\|");
		for (int i = 0; i < toolPermissionsSets.length; i++){
			String[] requiredPermissions = toolPermissionsSets[i].split(","); 
			boolean allowed = true;
			for (int j = 0; j < requiredPermissions.length; j++) {
				//since all in a set are required, if we are missing just one permission, set false, break and continue to check next set
				//as that set may override and allow access
				if (!SecurityService.unlock(requiredPermissions[j].trim(), site.getReference())){
					allowed = false;
					break;
				}
			}
			//if allowed, we have matched the entire set so are satisfied
			//otherwise we will check the next set
			if(allowed) {
				return true;
			}
		}
		
		//no sets were completely matched
		return false;
	}

}
