/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005-2013 The Sakai Foundation.
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

package org.sakaiproject.portlets;

import java.lang.Integer;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.util.Map;
import java.util.Set;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.UrlValidator;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.WindowState;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.component.cover.ServerConfigurationService;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;

// Velocity
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.app.VelocityEngine;

/**
 * a simple PortletIFrame Portlet
 */
public class PortletIFrame extends GenericPortlet {

	private static final Log M_log = LogFactory.getLog(PortletIFrame.class);

	// This is old-style internationalization (i.e. not dynamic based
	// on user preference) to do that would make this depend on
	// Sakai Unique APIs. :(
	// private static ResourceBundle rb =  ResourceBundle.getBundle("iframe");
	protected static ResourceLoader rb = new ResourceLoader("iframe");

	protected final FormattedText validator = new FormattedText();

	private final VelocityHelper vHelper = new VelocityHelper();

	VelocityEngine vengine = null;

	private PortletContext pContext;

    private static long xframeCache = 3600*1000*2;

    private static long xframeLoad = 8000;

	// TODO: Perhaps these constancts should come from portlet.xml

	/** The source URL, in config and context. */
	protected final static String SOURCE = "source";

	/** The value in context for the source URL to actually used, as computed from special and URL. */
	protected final static String URL = "url";

	/** The height, in config and context. */
	protected final static String HEIGHT = "height";

	/** The custom height from user input * */
	protected final static String CUSTOM_HEIGHT = "customNumberField";

	protected final String POPUP = "popup";
	protected final String MAXIMIZE = "maximize";

	protected final static String TITLE = "title";

	private static final String FORM_PAGE_TITLE = "title-of-page";

	private static final int MAX_TITLE_LENGTH = 99;

    private static final int MAX_SITE_INFO_URL_LENGTH = 255;

	private static String ALERT_MESSAGE = "sakai:alert-message";

    /** The Annotated URL Tool's url attribute, in config and context. */
    protected final static String TARGETPAGE_URL = "TargetPageUrl";

    /** The Annotated URL Tool's name attribute, in config and context. */
    protected final static String TARGETPAGE_NAME = "TargetPageName";

    /** The Annotated URL Tool's text attribute, in config and context. */
    protected final static String ANNOTATED_TEXT = "desp";

    /** The special attribute in config and context. */
    protected final static String SPECIAL = "special";

    /** Special value for site. */
    protected final static String SPECIAL_SITE = "site";

   /** Special value for Annotated URL Tool. */
    protected final static String SPECIAL_ANNOTATEDURL = "annotatedurl";

    /** Special value for myworkspace. */
    protected final static String SPECIAL_WORKSPACE = "workspace";

    /** Special value for worksite. */
    protected final static String SPECIAL_WORKSITE = "worksite";

    /** Support an external url defined in sakai.properties, in config and context. */
    protected final static String SAKAI_PROPERTIES_URL_KEY = "sakai.properties.url.key";

    /** If set, always hide the OPTIONS button */
    protected final static String HIDE_OPTIONS = "hide.options";

    private final static String PASS_PID = "passthroughPID";

    /**
     * Expand macros to insert session information into the URL?
     */
    private final static String MACRO_EXPANSION       = "expandMacros";

    /** Macro name: Site id (GUID) */
    protected static final String MACRO_SITE_ID             = "${SITE_ID}";
    /** Macro name: User id */
    protected static final String MACRO_USER_ID             = "${USER_ID}";
    /** Macro name: User enterprise id */
    protected static final String MACRO_USER_EID            = "${USER_EID}";
    /** Macro name: First name */
    protected static final String MACRO_USER_FIRST_NAME     = "${USER_FIRST_NAME}";
    /** Macro name: Last name */
    protected static final String MACRO_USER_LAST_NAME      = "${USER_LAST_NAME}";
    /** Macro name: Role */
    protected static final String MACRO_USER_ROLE           = "${USER_ROLE}";
    /** Macro name: Session */
    protected static final String MACRO_SESSION_ID          = "${SESSION_ID}";

    private static final String MACRO_CLASS_SITE_PROP = "SITE_PROP:";
   
    private static final String IFRAME_ALLOWED_MACROS_PROPERTY = "iframe.allowed.macros";

    private static final String MACRO_DEFAULT_ALLOWED = "${USER_ID},${USER_EID},${USER_FIRST_NAME},${USER_LAST_NAME},${SITE_ID},${USER_ROLE}";

    private static final String IFRAME_XFRAME_CACHETIME = "iframe.xframe.cachetime";
    private static final String IFRAME_XFRAME_CACHETIME_DEFAULT = "7200000";

    private static final String XFRAME_LAST_TIME = "xframe-last-time";
    private static final String XFRAME_LAST_STATUS = "xframe-last-status";

    private static final String IFRAME_XFRAME_LOADTIME = "iframe.xframe.loadtime";
    private static final String IFRAME_XFRAME_LOADTIME_DEFAULT = "8000";

    private static ArrayList allowedMacrosList;
    static
    {
        String xframeCacheS = 
            ServerConfigurationService.getString(IFRAME_XFRAME_CACHETIME, IFRAME_XFRAME_CACHETIME_DEFAULT);
        try { 
            xframeCache = Long.parseLong(xframeCacheS);
        } catch (NumberFormatException nfe) {
            xframeCache = 7200000;
        }

        String xframeLoadS = 
            ServerConfigurationService.getString(IFRAME_XFRAME_LOADTIME, IFRAME_XFRAME_LOADTIME_DEFAULT);
        try { 
            xframeLoad = Long.parseLong(xframeLoadS);
        } catch (NumberFormatException nfe) {
            xframeLoad = 8000;
        }

        allowedMacrosList = new ArrayList();

        final String allowedMacros =
            ServerConfigurationService.getString(IFRAME_ALLOWED_MACROS_PROPERTY, MACRO_DEFAULT_ALLOWED);

        String parts[] = allowedMacros.split(",");

        if(parts != null) {

            for(int i = 0; i < parts.length; i++) {

                allowedMacrosList.add(parts[i]);

            }

        }
    }

 /** Choices of pixels displayed in the customization page */
    public String[] ourPixels = { "300px", "450px", "600px", "750px", "900px", "1200px", "1800px", "2400px" };


	// If the property is final, the property wins.  If it is not final,
	// the portlet preferences take precedence.
	public String getTitleString(RenderRequest request)
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getTitle();
	}

	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		pContext = config.getPortletContext();
		try {
			vengine = vHelper.makeEngine(pContext);
		}
		catch(Exception e)
		{
			throw new PortletException("Cannot initialize Velocity ", e);
		}
		M_log.info("iFrame Portlet vengine="+vengine+" rb="+rb);
	}

	private void addAlert(ActionRequest request,String message) {
		PortletSession pSession = request.getPortletSession(true);
		pSession.setAttribute(ALERT_MESSAGE, message);
	}

	private void sendAlert(RenderRequest request, Context context) {
		PortletSession pSession = request.getPortletSession(true);
		String str = (String) pSession.getAttribute(ALERT_MESSAGE);
		pSession.removeAttribute(ALERT_MESSAGE);
		if ( str != null && str.length() > 0 ) context.put("alertMessage", validator.escapeHtml(str, false));
	}

	// Render the portlet - this is not supposed to change the state of the portlet
	// Render may be called many times so if it changes the state - that is tacky
	// Render will be called when someone presses "refresh" or when another portlet
	// onthe same page is handed an Action.
	public void doView(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			response.setContentType("text/html");

			// System.out.println("==== doView called ====");

			PrintWriter out = response.getWriter();
			Placement placement = ToolManager.getCurrentPlacement();
            Properties config = getAllProperties(placement);

			response.setTitle(placement.getTitle());
			String source = config.getProperty(SOURCE);
			if ( source == null ) source = "";
			String height = config.getProperty(HEIGHT);
			if ( height == null ) height = "1200px";
            String sakaiPropertiesUrlKey = config.getProperty(SAKAI_PROPERTIES_URL_KEY);
            String hideOptions = config.getProperty(HIDE_OPTIONS);

            String special = getSpecial(config);

			boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
			boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));

            // set the pass_pid parameter
            String passPidStr = config.getProperty(PASS_PID, "false");
            boolean passPid = "true".equalsIgnoreCase(passPidStr);

            // Set the macro expansion
            String macroExpansionStr = config.getProperty(MACRO_EXPANSION, "true");
            boolean macroExpansion = ! ( "false".equalsIgnoreCase(macroExpansionStr));

            // Compute the URL
            String url = sourceUrl(special, source, placement.getContext(), macroExpansion, passPid, placement.getId(), sakaiPropertiesUrlKey);

            //System.out.println("special="+special+" source="+source+" pgc="+placement.getContext()+" macroExpansion="+macroExpansion+" passPid="+passPid+" PGID="+placement.getId()+" sakaiPropertiesUrlKey="+sakaiPropertiesUrlKey+" url="+url);

			if ( url != null && url.trim().length() > 0 ) {
                popup = popup || popupXFrame(placement, url);
				Context context = new VelocityContext();

                Session session = SessionManager.getCurrentSession();
                String csrfToken = (String) session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
                if ( csrfToken != null ) context.put("sakai_csrf_token", csrfToken);
				context.put("tlang", rb);
				context.put("validator", validator);
				context.put("source",url);
				context.put("height",height);
				sendAlert(request,context);
				context.put("popup", Boolean.valueOf(popup));
				context.put("maximize", Boolean.valueOf(maximize));
				context.put("placement", placement.getId().replaceAll("[^a-zA-Z0-9]","_"));
				context.put("loadTime", new Long(xframeLoad));

                // TODO: state.setAttribute(TARGETPAGE_URL,config.getProperty(TARGETPAGE_URL));
                // TODO: state.setAttribute(TARGETPAGE_NAME,config.getProperty(TARGETPAGE_NAME));

				vHelper.doTemplate(vengine, "/vm/main.vm", context, out);
			} else {
				out.println("Not yet configured");
			}

            // TODO: state.setAttribute(EVENT_ACCESS_WEB_CONTENT, config.getProperty(EVENT_ACCESS_WEB_CONTENT));
            // TODO: state.setAttribute(EVENT_REVISE_WEB_CONTENT, config.getProperty(EVENT_REVISE_WEB_CONTENT));

			// System.out.println("==== doView complete ====");
		}

    // Determine if we should pop up due to an X-Frame-Options : [SAMEORIGIN]
    public boolean popupXFrame(Placement placement, String url) 
    {
        if ( xframeCache < 1 ) return false;
        if ( ! ( url.startsWith("http://") || url.startsWith("https://") ) ) return false;

        Date date = new Date();
        long nowTime = date.getTime();
        
        String lastTimeS = placement.getPlacementConfig().getProperty(XFRAME_LAST_TIME);
        long lastTime = -1;
        try {
            lastTime = Long.parseLong(lastTimeS);
        } catch (NumberFormatException nfe) {
            lastTime = -1;
        }

        M_log.debug("lastTime="+lastTime+" nowTime="+nowTime);

        if ( lastTime > 0 && nowTime < lastTime + xframeCache ) {
            String lastXF = placement.getPlacementConfig().getProperty(XFRAME_LAST_STATUS);
            M_log.debug("Status from placement="+lastXF);
            return "true".equals(lastXF);
        }

        placement.getPlacementConfig().setProperty(XFRAME_LAST_TIME, String.valueOf(nowTime));
        boolean retval = false;
        try {
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection con =
                (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");

            Map headerfields = con.getHeaderFields();
            Set headers = headerfields.entrySet(); 
            for(Iterator i = headers.iterator(); i.hasNext();) { 
                Map.Entry map = (Map.Entry)i.next();
                String key = (String) map.getKey();
                if ( key == null ) continue;
                key = key.toLowerCase();
                if ( ! "x-frame-options".equals(key) ) continue;

                // Since the valid entries are SAMEORIGIN, DENY, or ALLOW-URI
                // we can pretty much assume the answer is "not us" if the header
                // is present
                retval = true;
                break;
            }

        }
        catch (Exception e) {
            // Fail pretty silently because this could be pretty chatty with bad urs and all
            M_log.debug(e.getMessage());
            retval = false;
        }
        placement.getPlacementConfig().setProperty(XFRAME_LAST_STATUS, String.valueOf(retval));
        placement.save();
        M_log.debug("Retrieved="+url+" XFrame="+retval);
        return retval;
    }

	public void doEdit(RenderRequest request, RenderResponse response)
		throws PortletException, IOException 
    {

			// System.out.println("==== doEdit called ====");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);

			Context context = new VelocityContext();
            Session session = SessionManager.getCurrentSession();
            String csrfToken = (String) session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
            if ( csrfToken != null ) context.put("sakai_csrf_token", csrfToken);
			context.put("tlang", rb);
			context.put("validator", validator);
			sendAlert(request,context);

			PortletURL url = response.createActionURL();
			context.put("actionUrl", url.toString());
			context.put("doCancel", "sakai.cancel");
			context.put("doUpdate", "sakai.update");

			Placement placement = ToolManager.getCurrentPlacement();
            Properties config = getAllProperties(placement);
            String special = getSpecial(config);
			context.put("title", validator.escapeHtml(placement.getTitle(), false));
			String source = placement.getPlacementConfig().getProperty(SOURCE);
			if ( source == null ) source = "";
			if ( special == null ) context.put("source",source);
			String height = placement.getPlacementConfig().getProperty(HEIGHT);
			if ( height == null ) height = "1200px";
			context.put("height",height);

			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
			if ( toolConfig != null )
			{
				try
				{
					Site site = SiteService.getSite(toolConfig.getSiteId());
					String siteId = site.getId();
					SitePage page = site.getPage(toolConfig.getPageId());

					// if this is the only tool on that page, update the page's title also
					if ((page.getTools() != null) && (page.getTools().size() == 1))
					{
						context.put("showPopup", Boolean.TRUE);
						boolean popup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));
						context.put("popup", Boolean.valueOf(popup));

						boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));
						context.put("maximize", Boolean.valueOf(maximize));

						context.put("pageTitleEditable", Boolean.TRUE);
						context.put("page_title",  validator.escapeHtml(page.getTitle(), false));
					}
				}
				catch (Throwable e)
				{
				}
			}

		    if (special == null)
		    {
			    context.put("heading", rb.getString("gen.custom"));
		    }
		    // set the heading based on special
		    else
		    {
			    if (SPECIAL_SITE.equals(special))
			    {
				    context.put("heading", rb.getString("gen.custom.site"));
			    }
    
			    else if (SPECIAL_WORKSPACE.equals(special))
			    {
				    context.put("heading", rb.getString("gen.custom.workspace"));
			    }
    
			    else if (SPECIAL_WORKSITE.equals(special))
			    {
				    context.put("heading", rb.getString("gen.custom.worksite"));

				    // for worksite, also include the Site's infourl and description
				    try
				    {
					    Site s = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
					    String siteId = s.getId();

					    String infoUrl = StringUtils.trimToNull(s.getInfoUrl());
					    if (infoUrl != null)
					    {
						    context.put("info_url", FormattedText.escapeHtmlFormattedTextarea(infoUrl));
					    }

					    String description = StringUtils.trimToNull(s.getDescription());
					    if (description != null)
					    {
	                        description = FormattedText.escapeHtmlFormattedTextarea(description);
						    context.put("description", description);
					    }
				    }
				    catch (Throwable e)
				    {
				    }
			    }
			    else if (SPECIAL_ANNOTATEDURL.equals(special))
			    {
				
				    context.put("heading", rb.getString("gen.custom.annotatedurl"));

				    // for Annotated URL Tool page, also include the description
				    try
				    {		
					    String desp = config.getProperty(ANNOTATED_TEXT);
					    context.put("description", desp);
				    }
				    catch (Throwable e)
				    {
				    }
			    }

			    else
			    {
				    context.put("heading", rb.getString("gen.custom"));
			    }
		    }

		    boolean selected = false;
		    for (int i = 0; i < ourPixels.length; i++)
		    {
			    if (height.equals(ourPixels[i]))
			    {
				    selected = true;
				    continue;
			    }
		    }
		    if (!selected)
		    {
			    String[] strings = height.trim().split("px");
			    context.put("custom_height", strings[0]);
			    height = rb.getString("gen.heisomelse");
		    }
		    context.put("height", height);

	    	// TODO: tracking event
		
		    // output the max limit 
		    context.put("max_length_title", MAX_TITLE_LENGTH);
		    context.put("max_length_info_url", MAX_SITE_INFO_URL_LENGTH);

            String template = "/vm/edit.vm";
            if (SPECIAL_SITE.equals(special)) template = "/vm/edit-site.vm";
            if (SPECIAL_WORKSITE.equals(special)) template = "/vm/edit-site.vm";
            if (SPECIAL_ANNOTATEDURL.equals(special)) template = "/vm/edit-annotatedurl.vm";
            // System.out.println("EDIT TEMP="+template+" special="+special);

			vHelper.doTemplate(vengine, template, context, out);

			// System.out.println("==== doEdit done ====");
		}

	public void doHelp(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			// System.out.println("==== doHelp called ====");
			// sendToJSP(request, response, "/help.jsp");
			JSPHelper.sendToJSP(pContext, request, response, "/help.jsp");
			// System.out.println("==== doHelp done ====");
		}

	// Process action is called for action URLs / form posts, etc
	// Process action is called once for each click - doView may be called many times
	// Hence an obsession in process action with putting things in session to 
	// Send to the render process.
	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			// System.out.println("==== processAction called ====");

			PortletSession pSession = request.getPortletSession(true);

			// Our first challenge is to figure out which action we want to take
			// The view selects the "next action" either as a URL parameter
			// or as a hidden field in the POST data - we check both

			String doCancel = request.getParameter("sakai.cancel");
			String doUpdate = request.getParameter("sakai.update");

			// Our next challenge is to pick which action the previous view
			// has told us to do.  Note that the view may place several actions
			// on the screen and the user may have an option to pick between
			// them.  Make sure we handle the "no action" fall-through.

			pSession.removeAttribute("error.message");

			if ( doCancel != null ) {
				response.setPortletMode(PortletMode.VIEW);
			} else if ( doUpdate != null ) {
				processActionEdit(request, response);
			} else {
				// System.out.println("Unknown action");
				response.setPortletMode(PortletMode.VIEW);
			}

			// System.out.println("==== End of ProcessAction  ====");
		}

	public void processActionEdit(ActionRequest request, ActionResponse response)
		throws PortletException, IOException 
		{
			// TODO: Check Role

			// Stay in EDIT mode unless we are successful
			response.setPortletMode(PortletMode.EDIT);

			// get the site toolConfiguration, if this is part of a site.
			Placement placement = ToolManager.getCurrentPlacement();
			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
            Properties config = getAllProperties(placement);
            String special = getSpecial(config);

            // Get and verify the source
			String source = StringUtils.trimToEmpty(request.getParameter("source"));

            // If this is a normal placement (i.e. not special)
            if ( special == null ) {
                if (StringUtils.isBlank(source))
                {
                    addAlert(request, rb.getString("gen.url.empty"));
                    return;
                }

                if ((!source.startsWith("/")) && (source.indexOf("://") == -1))
                {
                    source = "http://" + source;
                }

                // Validate the url
                UrlValidator urlValidator = new UrlValidator();
                if (!urlValidator.isValid(source))
                {
                    addAlert(request, rb.getString("gen.url.invalid"));
                    return;
                }
            }

            // update state
			if ( source == null ) source = "";
            placement.getPlacementConfig().setProperty(SOURCE, source);

            // site info url 
            String infoUrl = StringUtils.trimToNull(request.getParameter("infourl"));
            if (infoUrl != null && infoUrl.length() > MAX_SITE_INFO_URL_LENGTH)
            {
                addAlert(request, rb.getString("gen.info.url.toolong"));
                return;
            }

			String height = request.getParameter(HEIGHT);
			if (height.equals(rb.getString("gen.heisomelse")))
			{
				String customHeight = request.getParameter(CUSTOM_HEIGHT);
				if ((customHeight != null) && (!customHeight.equals("")))
				{
					if (!checkDigits(customHeight))
					{
						addAlert(request,rb.getString("java.alert.pleentval"));
						return;
					}
					height = customHeight + "px";
					placement.getPlacementConfig().setProperty(HEIGHT, height);
				}
				else
				{
					addAlert(request,rb.getString("java.alert.pleentval"));
					return;
				}
			}
			else
			{
				placement.getPlacementConfig().setProperty(HEIGHT, height);
			}


			// title
			String title = request.getParameter(TITLE);
			if (StringUtils.isBlank(title))
			{
				addAlert(request,rb.getString("gen.tootit.empty"));
				return;			
				// SAK-19515 check for LENGTH of tool title
			} 
			else if (title.length() > MAX_TITLE_LENGTH)
			{
				addAlert(request,rb.getString("gen.tootit.toolong"));
				return;			
			}
			placement.setTitle(title);

			try
			{
				Site site = SiteService.getSite(toolConfig.getSiteId());
				SitePage page = site.getPage(toolConfig.getPageId());
				page.setTitleCustom(true);

				// for web content tool, if it is a site page tool, and the only tool on the page, update the page title / popup.
				if (toolConfig != null && ! SPECIAL_WORKSITE.equals(special) && ! SPECIAL_WORKSPACE.equals(special) )
				{
					// if this is the only tool on that page, update the page's title also
					if ((page.getTools() != null) && (page.getTools().size() == 1))
					{
						String newPageTitle = request.getParameter(FORM_PAGE_TITLE);

						if (StringUtils.isBlank(newPageTitle))
						{
							addAlert(request,rb.getString("gen.pagtit.empty"));
							return;		
						}
						else if (newPageTitle.length() > MAX_TITLE_LENGTH)
						{
							addAlert(request,rb.getString("gen.pagtit.toolong"));
							return;			
						}
						page.setTitle(newPageTitle);				
					}
				}

				SiteService.save(site);
			}
			catch (Exception ignore)
			{
				M_log.warn("doConfigure_update: " + ignore);
			}

			// popup and maximize
			String spop = request.getParameter("popup");
			if ( ! "true".equals(spop) ) spop = "false";
			placement.getPlacementConfig().setProperty(POPUP, spop);
			String smax = request.getParameter("maximize");
			if ( ! "true".equals(smax) ) smax = "false";
			placement.getPlacementConfig().setProperty(MAXIMIZE, smax);

            // Make sure we re-check X-Frame-Options
            placement.getPlacementConfig().setProperty(XFRAME_LAST_STATUS, "");
            placement.getPlacementConfig().setProperty(XFRAME_LAST_TIME, "-1");
			placement.save();

            // Handle the infoUrl
            if (SPECIAL_WORKSITE.equals(special))
            {
                if ((infoUrl != null) && (infoUrl.length() > 0) && (!infoUrl.startsWith("/")) && (infoUrl.indexOf("://") == -1))
                {
                    infoUrl = "http://" + infoUrl;
                }
                String description = StringUtils.trimToNull(request.getParameter("description"));
                description = FormattedText.processEscapedHtml(description);
    
                // update the site info
                try
                {
                    SiteService.saveSiteInfo(ToolManager.getCurrentPlacement().getContext(), description, infoUrl);
                }
                catch (Throwable e)
                {
                    M_log.warn("doConfigure_update: " + e);
                }
            }

			response.setPortletMode(PortletMode.VIEW);
		}

	/** Valid digits for custom height from user input **/
	protected static final String VALID_DIGITS = "0123456789";

	/**
	 * Check if the string from user input contains any characters other than digits
	 * 
	 * @param height
	 *        String from user input
	 * @return True if all are digits. Or False if any is not digit.
	 */
	private boolean checkDigits(String height)
	{
		for (int i = 0; i < height.length(); i++)
		{
			if (VALID_DIGITS.indexOf(height.charAt(i)) == -1) return false;
		}
		return true;
	}

	/**
	 * Get the special type of this placement, compensating for legacy patterns
	 */
	protected String getSpecial(Properties config)
    {
        String special = config.getProperty(SPECIAL);
        // check for an older way the ChefWebPagePortlet took parameters, converting to our "special" values
        if (special == null)
        {
            if ("true".equals(config.getProperty("site")))
            {
                special = SPECIAL_SITE;
            }
            else if ("true".equals(config.getProperty("workspace")))
            {
                special = SPECIAL_WORKSPACE;
            }
            else if ("true".equals(config.getProperty("worksite")))
            {
                special = SPECIAL_WORKSITE;
            }
            else if ("true".equals(config.getProperty("annotatedurl")))
            {
                special = SPECIAL_ANNOTATEDURL;
            }
        }
        return special;
    }

	/**
	 * Compute the actual URL we will used, based on the configuration special and source URLs
	 */
	protected String sourceUrl(String special, String source, String context, boolean macroExpansion, boolean passPid, String pid, String sakaiPropertiesUrlKey)
	{
		String rv = StringUtils.trimToNull(source);

		// if marked for "site", use the site intro from the properties
		if (SPECIAL_SITE.equals(special))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("server.info.url"));
		}

		// if marked for "workspace", use the "user" site info from the properties
		else if (SPECIAL_WORKSPACE.equals(special))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("myworkspace.info.url"));
		}

		// if marked for "worksite", use the setting from the site's definition
		else if (SPECIAL_WORKSITE.equals(special))
		{
			// set the url to the site of this request's config'ed url
			try
			{
				// get the site's info URL, if defined
				Site s = SiteService.getSite(context);
				rv = StringUtils.trimToNull(s.getInfoUrlFull());

				// compute the info url for the site if it has no specific InfoUrl
				if (rv == null)
				{
					// access will show the site description or title...
					rv = ServerConfigurationService.getAccessUrl() + s.getReference();
				}
			}
			catch (Exception any)
			{
			}
		} 
		
		else if (sakaiPropertiesUrlKey != null && sakaiPropertiesUrlKey.length() > 1)
		{
			// set the url to a string defined in sakai.properties
			rv = StringUtils.trimToNull(ServerConfigurationService.getString(sakaiPropertiesUrlKey));
		}
		

		// if it's not special, and we have no value yet, set it to the webcontent instruction page, as configured
		if (rv == null || rv.equals("http://") || rv.equals("https://"))
		{
			rv = StringUtils.trimToNull(getLocalizedURL("webcontent.instructions.url"));
		}

		if (rv != null)
		{
			// accept a partial reference url (i.e. "/content/group/sakai/test.gif"), convert to full url
			rv = convertReferenceUrl(rv);

			// pass the PID through on the URL, IF configured to do so
			if (passPid)
			{
				if (rv.indexOf("?") < 0)
				{
					rv = rv + "?";
				}
				else
				{
					rv = rv + "&";
				}

				rv = rv + "pid=" + pid;
			}

			if (macroExpansion)
			{
				rv = doMacroExpansion(rv);
			}
		}

		return rv;
	}

    /** Construct and return localized filepath, if it exists
     **/
    private String getLocalizedURL(String property) {
        String filename = ServerConfigurationService.getString(property);
        if ( filename == null || filename.trim().length()==0 )
            return filename;
        else
            filename = filename.trim();

        int extIndex = filename.lastIndexOf(".") >= 0 ? filename.lastIndexOf(".") : filename.length()-1;
        String ext = filename.substring(extIndex);
        String doc = filename.substring(0,extIndex);

        Locale locale = new ResourceLoader().getLocale();

        if (locale != null){
            // check if localized file exists for current language/locale/variant
            String localizedFile = doc + "_" + locale.toString() + ext;
            String filePath = getPortletConfig().getPortletContext().getRealPath(".."+localizedFile);
            if ( (new File(filePath)).exists() )
                return localizedFile;

            // otherwise, check if localized file exists for current language
            localizedFile = doc + "_" + locale.getLanguage() + ext;
            filePath = getPortletConfig().getPortletContext().getRealPath(".."+localizedFile);
            if ( (new File(filePath)).exists() )
                return localizedFile;
        }
        return filename;
    }

	/**
	 * If the url is a valid reference, convert it to a URL, else return it unchanged.
	 */
	protected String convertReferenceUrl(String url)
	{
		// make a reference
		Reference ref = EntityManager.newReference(url);

		// if it didn't recognize this, return it unchanged
		if (ref.isKnownType())
		{
			// return the reference's url
			String refUrl = ref.getUrl();
			if (refUrl != null)
			{
				return refUrl;
			}
		}

		return url;
	}

	/**
	 * Get the current user id
	 * @throws SessionDataException
	 * @return User id
	 */
	private String getUserId() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getUserId();
	}
	
	/**
	 * Get the current session id
	 * @throws SessionDataException
	 * @return Session id
	 */
	private String getSessionId() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getId();
	}
	

	/**
	 * Get the current user eid
	 * @throws SessionDataException
	 * @return User eid
	 */
	private String getUserEid() throws SessionDataException
	{
		Session session = SessionManager.getCurrentSession();

		if (session == null)
		{
			throw new SessionDataException("No current user session");
		}
		return session.getUserEid();
	}
	

	/**
	 * Get current User information
	 * @throws IdUnusedException, SessionDataException
	 * @return {@link User} data
	 * @throws UserNotDefinedException 
	 */
	private User getUser() throws IdUnusedException, SessionDataException, UserNotDefinedException
	{
		
		return UserDirectoryService.getUser(this.getUserId());
	}

	/**
	 * Get the current site id
	 * @throws SessionDataException
	 * @return Site id (GUID)
	 */
	private String getSiteId() throws SessionDataException
	{
		Placement placement = ToolManager.getCurrentPlacement();

		if (placement == null)
		{
			throw new SessionDataException("No current tool placement");
		}
		return placement.getContext();
	}

	/**
	 * Fetch the user role in the current site
	 * @throws IdUnusedException, SessionDataException
	 * @return Role
	 * @throws GroupNotDefinedException 
	 */
	private String getUserRole() throws IdUnusedException, SessionDataException, GroupNotDefinedException
	{
		AuthzGroup 	group;
		Role 				role;

		group = AuthzGroupService.getAuthzGroup("/site/" + getSiteId());
		if (group == null)
		{
			throw new SessionDataException("No current group");
		}

		role = group.getUserRole(this.getUserId());
		if (role == null)
		{
			throw new SessionDataException("No current role");
		}
		return role.getId();
	}

	/**
	 * Get a site property by name
	 *
	 * @param name Property name
	 * @throws IdUnusedException, SessionDataException
	 * @return The property value (null if none)
	 */
	private String getSiteProperty(String name) throws IdUnusedException, SessionDataException
	{
		Site site;

		site = SiteService.getSite(getSiteId());
		return site.getProperties().getProperty(name);
	}

	/**
	 * Lookup value for requested macro name
	 */
	private String getMacroValue(String macroName)
	{
		try
		{
			if (macroName.equals(MACRO_USER_ID))
			{
				return this.getUserId();
			}
			if (macroName.equals(MACRO_USER_EID))
			{
				return this.getUserEid();
			}
			if (macroName.equals(MACRO_USER_FIRST_NAME))
			{
				return this.getUser().getFirstName();
			}
			if (macroName.equals(MACRO_USER_LAST_NAME))
			{
				return this.getUser().getLastName();
			}

			if (macroName.equals(MACRO_SITE_ID))
			{
				return getSiteId();
			}
			if (macroName.equals(MACRO_USER_ROLE))
			{
				return this.getUserRole();
			}
			if (macroName.equals(MACRO_SESSION_ID))
			{
				return this.getSessionId();
			}

			if (macroName.startsWith("${"+MACRO_CLASS_SITE_PROP)) 
			{
				macroName = macroName.substring(2); // Remove leading "${"
				macroName = macroName.substring(0, macroName.length()-1); // Remove trailing "}" 
				
				// at this point we have "SITE_PROP:some-property-name"
				// separate the property name from the prefix then return the property value
				String[] sitePropertyKey = macroName.split(":");
				
				if (sitePropertyKey != null && sitePropertyKey.length > 1) {	
				
					String sitePropertyValue = getSiteProperty(sitePropertyKey[1]);
	
					return (sitePropertyValue == null) ? "" : sitePropertyValue;
				
				}
			}
		}
		catch (Throwable throwable)
		{
			return "";
		}
		/*
		 * An unsupported macro: use the original text "as is"
		 */
		return macroName;
	}

	/**
	 * Expand one macro reference
	 * @param text Expand macros found in this text
	 * @param macroName Macro name
	 */
	private void expand(StringBuilder sb, String macroName)
	{
		int index;

		/*
		 * Replace every occurance of the macro in the parameter list
		 */
		index = sb.indexOf(macroName);
		while (index != -1)
		{
			String  macroValue = URLEncoder.encode(getMacroValue(macroName));

			sb.replace(index, (index + macroName.length()), macroValue);
			index = sb.indexOf(macroName, (index + macroValue.length()));
		}
	}

	/**
	 * Expand macros, inserting session and site information
	 * @param originalText Expand macros found in this text
	 * @return [possibly] Updated text
	 */
	private String doMacroExpansion(String originalText)
	{
		StringBuilder  sb;

		/*
		 * Quit now if no macros are embedded in the text
		 */
		if (originalText.indexOf("${") == -1)
		{
			return originalText;
		}
		/*
		 * Expand each macro
		 */
		sb = new StringBuilder(originalText);

		Iterator i = allowedMacrosList.iterator();
		
		while(i.hasNext()) {
			
			String macro = (String) i.next();
		
			expand(sb, macro);
			
		}

		return sb.toString();
	}

    // Work around lack of final config values in placementConfig();
    private Properties getAllProperties(Placement placement)
    {
        Properties config = placement.getTool().getRegisteredConfig();
        Properties mconfig = placement.getPlacementConfig();
        for ( Object okey : mconfig.keySet() ) {
            String key = (String) okey;
            config.setProperty(key,mconfig.getProperty(key));
        }
        return config;
    }

    /**
     * Note a "local" problem (we failed to get session or site data)
     */
    private static class SessionDataException extends Exception
    {
        public SessionDataException(String text)
        {
            super(text);
        }
    }
}
