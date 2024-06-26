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

package org.sakaiproject.portal.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;

/**
 * This interface represents a portal and is used mainly by portal handlers that
 * will not know the details of the portal implimentation.
 * 
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public interface Portal
{

	/**
	 * Error response modes.
	 */
	public static final int ERROR_SITE = 0;

	public static final int ERROR_GALLERY = 1;

	public static final int ERROR_WORKSITE = 2;

	/**
	 * Parameter value to allow anonymous users of gallery mode to be sent to
	 * the gateway site as anonymous user (like the /portal URL) instead of
	 * making them log in (like worksite, site, and tool URLs).
	 */
	public static final String PARAM_FORCE_LOGIN = "force.login";

	public static final String PARAM_FORCE_LOGOUT = "force.logout";

	/**
	 * ThreadLocal attribute set while we are processing an error.
	 */
	public static final String ATTR_ERROR = "org.sakaiproject.portal.error";

	/**
	 * Session attribute root for storing a site's last page visited - just
	 * append the site id.
	 */
	public static final String ATTR_SITE_PAGE = "sakai.portal.site.";

	/**
	 * Session variable passing a maximized URL between a portlet and the portal
	 */
	public static final String ATTR_MAXIMIZED_URL = "sakai:maximized-url";

	/**
	 * The default portal name is none is specified.
	 */
	public static final String DEFAULT_PORTAL_CONTEXT = "charon";

	/**
	 * Configuration option indicaing support for frame set mode.
	 */
	public static final String FRAMESET_SUPPORT = "portal.frameset";

	/**
	 * Configuration option to enable/disable state reset on navigation change
	 */
	public static final String CONFIG_AUTO_RESET = "portal.experimental.auto.reset";
	
    /**
	* Configuration option for default number of site tabs to display to users
	*/
	public static final String CONFIG_DEFAULT_TABS = "portal.default.tabs";

	/**
	 * Names of tool config/registration attributes that control the rendering
	 * of the tool's titlebar
	 */
	public static final String TOOLCONFIG_SHOW_RESET_BUTTON = "reset.button";

	public static final String TOOLCONFIG_SHOW_HELP_BUTTON = "help.button";

	public static final String TOOLCONFIG_HELP_DOCUMENT_ID = "help.id";

	public static final String TOOLCONFIG_HELP_DOCUMENT_URL = "help.url";

	/**
	 * Tool property used to indicate if JSR_168 tools are to be pre-rendered
	 * as they are being placed in the context.
	 */
	public static final String JSR_168_PRE_RENDER = "sakai:portlet-pre-render";

	/**
	 * Tool property used to indicate if a tool prefers a maximized view
	 * with minimal portal navigation.
	 */
	public static final String PREFER_MAXIMIZE = "sakai:prefer-maximize";
	
	/** 
	 * Parameter to signal that we should force the request back to the normal portal view
	 * rather than the PDA portal view.
	 */
	public static final String FORCE_CLASSIC_REQ_PARAM = "force.classic";
	
	/**
	 * Name of cookie that is set to signal what view we want
	 * It is an abstract name to we can later set other preferred modes into same cookie if desired
	 */
	public static final String PORTAL_MODE_COOKIE_NAME = "sakai_portal_mode";
	
	/**
	 * Value that this cookie will have if we are forcing classic mode
	 */
	public static final String FORCE_CLASSIC_COOKIE_VALUE = "classic";
	
	/**
	 * prepare the response and send it to the render engine
	 * 
	 * @param rcontext
	 * @param res
	 * @param template
	 * @param contentType
	 * @throws IOException
	 */
	void sendResponse(PortalRenderContext rcontext, HttpServletResponse res,
			String template, String contentType) throws IOException;

	/**
	 * get the placement for the request
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param placementId
	 * @param doPage
	 * @return
	 * @throws ToolException
	 */
	String getPlacement(HttpServletRequest req, HttpServletResponse res, Session session,
			String placementId, boolean doPage) throws ToolException;

	/**
	 * perform login
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param returnPath
	 * @param skipContainer
	 * @throws ToolException
	 */
	void doLogin(HttpServletRequest req, HttpServletResponse res, Session session,
			String returnPath, boolean skipContainer) throws ToolException;

	/**
	 * Process a logout
	 * 
	 * @param req
	 *        Request object
	 * @param res
	 *        Response object
	 * @param session
	 *        Current session
	 * @param returnPath
	 *        if not null, the path to use for the end-user browser redirect
	 *        after the logout is complete. Leave null to use the configured
	 *        logged out URL.
	 * @throws ToolException
	 */
	void doLogout(HttpServletRequest req, HttpServletResponse res, Session session,
			String returnPath) throws ToolException;

	/**
	 * get a new render context from the render engine
	 * 
	 * @param siteType
	 * @param title
	 * @param skin
	 * @param request
	 * @return
	 */
	PortalRenderContext startPageContext(String siteType, String title, String skin,
			HttpServletRequest request);

	/**
	 * perform a redirect if logged out
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	boolean redirectIfLoggedOut(HttpServletResponse res) throws IOException;

	/**
	 * get the portal page URL base on the tool supplied
	 * 
	 * @param siteTool
	 * @return
	 */
	String getPortalPageUrl(ToolConfiguration siteTool);

	/**
	 * populate the model with error status
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param mode
	 * @throws ToolException
	 * @throws IOException
	 */
	void doError(HttpServletRequest req, HttpServletResponse res, Session session,
			int mode) throws ToolException, IOException;

	/**
	 * forward to a portal url
	 * 
	 * @param tool
	 * @param req
	 * @param res
	 * @param siteTool
	 * @param skin
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @throws IOException
	 * @throws ToolException
	 */
	void forwardPortal(ActiveTool tool, HttpServletRequest req, HttpServletResponse res,
			ToolConfiguration siteTool, String skin, String toolContextPath,
			String toolPathInfo) throws ToolException, IOException;

	/**
	 * setup in preparation for a forward
	 * 
	 * @param req
	 * @param res
	 * @param p
	 * @param skin
	 */
	void setupForward(HttpServletRequest req, HttpServletResponse res, Placement p,
			String skin) throws ToolException;

	/**
	 * include the model section that relates to the bottom of the page.
	 * 
	 * @param rcontext
	 */
	void includeBottom(PortalRenderContext rcontext);

	/**
	 * work out the type of the site based on the site id.
	 * 
	 * @param siteId
	 * @return
	 */
	String calcSiteType(String siteId);

	/**
	 * include the part od the view tree needed to render login
	 * 
	 * @param rcontext
	 * @param req
	 * @param session
	 */
	void includeLogin(PortalRenderContext rcontext, HttpServletRequest req,
			Session session);

	/**
	 * forward the request to a tool
	 * 
	 * @param tool
	 * @param req
	 * @param res
	 * @param siteTool
	 * @param skin
	 * @param toolContextPath
	 * @param toolPathInfo
	 * @throws ToolException
	 */
	void forwardTool(ActiveTool tool, HttpServletRequest req, HttpServletResponse res,
			Placement placement, String skin, String toolContextPath, String toolPathInfo)
			throws ToolException;

	/**
	 * get the site id for the user
	 * 
	 * @param userId
	 * @return
	 */
	String getUserEidBasedSiteId(String userId);

	

	/**
	 * populate the view tree for the model
	 * 
	 * @param req
	 * @param res
	 * @param session
	 * @param siteId
	 * @param toolId
	 * @param toolContextPath
	 * @param prefix
	 * @param doPages
	 * @param resetTools
	 * @param includeSummary
	 * @param expandSite
	 * @return
	 * @throws ToolException
	 * @throws IOException
	 */
	PortalRenderContext includePortal(HttpServletRequest req, HttpServletResponse res,
			Session session, String siteId, String toolId, String toolContextPath,
			String prefix, boolean doPages, boolean resetTools, boolean includeSummary,
			boolean expandSite) throws ToolException, IOException;

	/**
	 * include the tool part of the view tree
	 * 
	 * @param res
	 * @param req
	 * @param placement
	 * @return
	 * @throws IOException
	 */
	Map includeTool(HttpServletResponse res, HttpServletRequest req,
			ToolConfiguration placement) throws IOException;

	/**
	 * Get the context name of the portal. This is the name used to identify the
	 * portal implimentation in the portal service and to other parts of the
	 * system. Typically portals will be registered with the portal service
	 * using a name and render engines and PortalHandlers will connect to named
	 * portals.
	 * 
	 * @return
	 */
	String getPortalContext();

	/**
	 * Get the servlet context associated with the portal
	 * 
	 * @return
	 */
	ServletContext getServletContext();

	/**
         * Look at the user agent and add Mobile Browser related material to 
	 * the context.
	 */
 	void setupMobileDevice(HttpServletRequest req, PortalRenderContext rcontext);


	/**
	 * Return the sub sites below a particular site
	 * Map.
	 * @param rcontext
	 * @param req
	 * @param siteId
	 * @param toolContextPath
	 * @param prefix
	 * @param loggedIn
	 */
 	void includeSubSites(PortalRenderContext rcontext, HttpServletRequest req,
			Session session, String siteId, String toolContextPath, 
			String prefix, boolean resetTools );

	/**
	 * Get a the page Filter Implementation
	 * @return
	 */
	PageFilter getPageFilter();
	
	/**
	 * Set page Filter
	 *
	 */
	void setPageFilter(PageFilter pageFilter);

	/**
	 * @return
	 */
	PortalSiteHelper getSiteHelper();

	/**
	 * @return
	 */
	SiteNeighbourhoodService getSiteNeighbourhoodService();

	/**
         * Indicate if a placement is a JSR-168 placement
	 * @return
	 */
        public boolean isPortletPlacement(Placement placement);

}
