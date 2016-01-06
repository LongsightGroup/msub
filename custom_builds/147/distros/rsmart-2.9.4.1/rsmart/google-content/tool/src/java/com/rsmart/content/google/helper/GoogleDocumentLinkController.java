/*
 * Copyright 2011 The rSmart Group
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
 * Contributor(s): duffy
 */

package com.rsmart.content.google.helper;

import com.rsmart.content.google.api.GoogleDocsServiceNotConfiguredException;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
import org.sakaiproject.site.cover.SiteService;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.Web;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.metaobj.shared.model.DateBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.rsmart.content.google.api.GoogleDocsService;
import com.rsmart.content.google.api.GoogleDocDescriptor;
import com.rsmart.content.google.api.GoogleDocsException;
import com.rsmart.content.google.api.GoogleDocumentType;
import com.rsmart.content.google.api.GoogleOAuthConfigurationException;
import com.rsmart.content.google.oauth.OAuthCallbackServlet;

/**
 * User: duffy
 * Date: Feb 1, 2010
 * Time: 1:44:45 PM
 */
public class GoogleDocumentLinkController
    extends AbstractWizardFormController
{
    private final static Log
        LOG = LogFactory.getLog (GoogleDocumentLinkController.class);

    public final static String
        KEY_REDIRECTURL         = "googleOAuthURL",
        KEY_GOOGLE              = "google",
        KEY_KEYS                = "folderKeys",
        KEY_FOLDERS             = "folders",
        KEY_DOC_TYPE_MAP        = "docTypes",
        KEY_COMMANDBEAN         = "command",
        KEY_COPYRIGHTVALUES     = "copyrightValues",
        KEY_DEFAULT_TYPE        = "defaultType",
        KEY_ERROR_MSG           = "errorMessage",
        PARENT_ENTITY           = "parent",
        VIEW_AUTHENTICATE       = "authenticate",
        VIEW_SELECTFILE         = "selectFile",
        VIEW_FINISHLINK         = "finishLink",
        VIEW_PROPERTIES         = "editProperties",
        VIEW_WORKSHEET          = "selectWorksheet",
        VIEW_OAUTH_ERROR        = "error/tokenCallbackError";

    private GoogleDocsService
        google = null;

    private ContentHostingService
        contentHostingService = null;

    private WorksiteManager
        worksiteManager = null;

    private String
        oAuthCallbackPath = null;

    private int defaultNotification = NotificationService.NOTI_NONE;

    private static ResourceBundle
        rb = ResourceBundle.getBundle("com.rsmart.content.google.helper.Messages");

    public GoogleDocumentLinkController()
    {
        setCommandName (KEY_COMMANDBEAN);
        setPages (new String[] {VIEW_AUTHENTICATE, VIEW_SELECTFILE, VIEW_FINISHLINK, VIEW_PROPERTIES,
                                VIEW_WORKSHEET, VIEW_OAUTH_ERROR});
    }

    public void setWorksiteManager (WorksiteManager wsMgr)
    {
        worksiteManager = wsMgr;
    }

    public WorksiteManager getWorksiteManager()
    {
        return worksiteManager;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        // code borrowed from sakai's VmServlet.setVmStdRef() method

        // form the skin based on the current site, and the defaults as configured
        //String skinRoot = ServerConfigurationService.getString("skin.root", "/sakai-shared/css/");
        String skinRoot = ServerConfigurationService.getString("skin.repo", "/library/skin");
        String skin = ServerConfigurationService.getString("skin.default", "default");

        Id siteId = getWorksiteManager().getCurrentWorksiteId();

        if (siteId != null)
        {
            String siteSkin = SiteService.getSiteSkin(siteId.getValue());

            if (siteSkin != null) {
                skin = siteSkin;
            }

            request.setAttribute("sakai_skin_base", skinRoot + "/tool_base.css");
            request.setAttribute("sakai_skin", skinRoot + "/" + skin + "/tool.css");

            //TODO figure out if this is still needed
            // form the portal root for the skin - removing the .css and adding "portalskins" before
            int pos = skin.indexOf(".css");
            if (pos != -1)
            {
                skin = skin.substring(0, pos);
            }

            request.setAttribute("sakai_portalskin", skinRoot + "portalskins" + "/" + skin + "/");
            request.setAttribute("sakai_skin_id", skin);
        }

        if (!getGoogleDocsService().isOAuthProviderEnabled())
        {
            throw new GoogleOAuthConfigurationException ("OAuth Provider for Google Docs is not enabled");
        }
        
        return super.handleRequest(request, response);
    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest, ServletRequestDataBinder binder)
        throws Exception
    {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy/MM/dd"), true));
    }

    public GoogleDocsService getGoogleDocsService()
    {
        return google;
    }

    public void setGoogleDocsService (GoogleDocsService docsService)
    {
        google = docsService;
    }

    public ContentHostingService getContentHostingService()
    {
        return contentHostingService;
    }

    public void setContentHostingService (ContentHostingService chs)
    {
        contentHostingService = chs;
    }

    public void setOauthCallbackPath (String path)
    {
        oAuthCallbackPath = path;
    }

    protected Object formBackingObject(HttpServletRequest httpServletRequest)
        throws Exception
    {
        CommandBean
            command = null;

        GoogleHelperState
            state = GoogleHelperState.getState();

        command = state.getCommandBean();

        if (command == null)
        {
            command = new CommandBean();

            state.setCommandBean(command);
        }

        return command;
    }

    public String getOauthCallbackPath ()
    {
        return oAuthCallbackPath;
    }


    protected int getInitialPage(HttpServletRequest req)
    {
        final GoogleHelperState
            state = GoogleHelperState.getState();

        if (!state.isAuthenticatedWithGoogle())
        {
            LOG.debug("state reflects no google oauth token - checking in DB");
            try {
                if (google.userOAuthTokenExists())
                {
                    LOG.debug("oauth token exists");
                    state.setAuthenticatedWithGoogle(true);
                    return 1;
                }
                else
                {
                    if (!state.isFirstAuthenticationAttempt())
                    {
                        if (!OAuthCallbackServlet.isGoogleAuthnSuccessful())
                        {
                            state.setFirstAuthenticationAttempt(true);
                            return 5;
                        }
                        return 0;
                    }
                }
            }
            catch (GoogleDocsException e)
            {
                LOG.error("Error determining if user has an OAuth Token for Google: ", e);
                // log an error!
            }
        }

        return 0;
    }

    private void assertGoogleDocsServiceConfigured (final GoogleDocsService google)
        throws GoogleDocsException
    {
        if (google == null)
        {
            LOG.error("Google Docs service was not provided");
            throw new GoogleDocsServiceNotConfiguredException ("Google Docs service not provided");
        }

        if (!google.isEnabled())
        {
            LOG.error("Google Docs service is not enabled");
            throw new GoogleDocsServiceNotConfiguredException ("Google Docs service is not enabled");
        }

        if (!google.isInitialized())
        {
            LOG.error("Google Docs service is not initialized");
            throw new GoogleDocsServiceNotConfiguredException ("Google Docs service is not initialized");
        }

        if (!google.isOAuthProviderRegistered())
        {
            LOG.error("Google OAuth provider is not configured");
            throw new GoogleOAuthConfigurationException("Google OAuth provider is not configured");
        }

    }

    protected Map referenceData(HttpServletRequest req, int page)
         throws Exception
    {
        final Map
            model = new HashMap();

        final GoogleDocsService
            google = getGoogleDocsService();

        assertGoogleDocsServiceConfigured(google);

        model.put(KEY_GOOGLE, google);

        if (page == 0)
        {
            OAuthCallbackServlet.reset();

            final StringBuilder oauthDoneURL = new StringBuilder();

            URL
                url = null;

            if (ServerConfigurationService.getBoolean("google-content.useOAuth2", false)) {
                try
                {
                    url = google.createOAuth2RequestURL(prepareCallbackUrl(req));
                }
                catch (GoogleDocsException e)
                {
                    LOG.debug("unexpected error populating data for next page", e);
                    throw e;
                }
            } else {
                try
                {
                    url = google.createOAuthRequestURL(prepareCallbackUrl(req));
                }
                catch (GoogleDocsException e)
                {
                    LOG.debug("unexpected error populating data for next page", e);
                    throw e;
                }
            }

            final Placement
                p = ToolManager.getCurrentPlacement();
            final Tool
                t = ToolManager.getCurrentTool();
            final ToolSession
                toolSession = SessionManager.getCurrentToolSession();
            final ResourceToolActionPipe
                pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

            oauthDoneURL.append(ServerConfigurationService.getPortalUrl()).append("/directtool/").append(p.getId());

            /*
                CLE-9034 if we got here from the filepicker we can't depend on the tool ID the way we would like.
                Instead the tool ID is the id of the tool that spawned the filepicker helper which in turn
                spawned the google content helper. So instead we attempt to forge a directtool link based on the URL
                used to get to the filepicker helper in the first place.
             */

            //first we tweak the request object so it will give us the context portion of the URL which was requested originally
            String
                nativeURISetting = (String)req.getAttribute("sakai.request.native.url");

            req.removeAttribute("sakai.request.native.url");

            String
                contextPath = req.getContextPath();
            int
                filePickerOffset = -1;

            //see if we came from the filepicker - if so add the filepicker back to the URL
            if ((filePickerOffset = contextPath.indexOf("sakai.filepicker.helper")) > 0)
            {
                oauthDoneURL.append("/sakai.filepicker.helper");
            }

            //return the request object to its state regarding the context portion of the URL
            req.setAttribute("sakai.request.native.url", nativeURISetting);

            oauthDoneURL.append("?selectedItemId=").append(pipe.getContentEntity().getId());
            oauthDoneURL.append("&rt_action=com.rsmart.content.google.api.GoogleDocumentType:create");
            oauthDoneURL.append("&sakai_action=doDispatchAction");

            LOG.debug ("oauth request URL: " + url);
            LOG.debug("oauth done URL: " + oauthDoneURL.toString());

            OAuthCallbackServlet.setReturnURL(oauthDoneURL.toString());

            model.put(KEY_REDIRECTURL, url.toString());

            String deauthorizeString = rb.getString("instructions.deauthorize");
            deauthorizeString = deauthorizeString.replace("{0}", ToolManager.getTool("com.rsmart.oauth.token").getTitle());
            model.put("deauthorizeString", deauthorizeString);

            GoogleHelperState.getState().setFirstAuthenticationAttempt(false);
        }
        else if (page == 1)
        {
            try
            {
                final Map <String, List<GoogleDocDescriptor>> folders;

                if (ServerConfigurationService.getBoolean("google-content.useOAuth2", false)) {
                    folders = google.getDriveDirectoryTreeForUser();
                } else {
                    folders = google.getDirectoryTreeForUser();
                }

                model.put(KEY_FOLDERS, folders);
            }
            catch (GoogleDocsException e)
            {
                LOG.error ("error retrieving directory tree for user", e);
                throw e;
            }

            final ToolSession
                toolSession = SessionManager.getCurrentToolSession();
            final ResourceToolActionPipe
                pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
            final ContentEntity
                ce = pipe.getContentEntity();

            model.put(PARENT_ENTITY, ce);
        }
        else if (page == 2)
        {
            final GoogleHelperState
                state = GoogleHelperState.getState();

            model.put(KEY_DEFAULT_TYPE, google.getDefaultMimeTypeForType(state.getCommandBean().getType()));
            
            try
            {
                model.put(KEY_DOC_TYPE_MAP, google.getMimeTypesForType(state.getCommandBean().getType()));
            }
            catch (GoogleDocsException e)
            {
                LOG.error ("error retrieving available mime types for document", e);
                throw e;
            }
        }
        else if (page == 3)
        {
            final List<String>
                copyrightValues = new LinkedList<String>();

            final String
                countStr = rb.getString("copyrighttype.count");
            int
                count = 0;
            try
            {
                count = Integer.decode(countStr);
            }
            catch (NumberFormatException nfe)
            {
                LOG.error ("Could not read count of copyright types from message bundle", nfe);
                return model;
            }

            for (int i = 1; i < count + 1; i++)
            {
                String
                    value = rb.getString ("copyrighttype." + i);

                copyrightValues.add(value);
            }

            model.put (KEY_COPYRIGHTVALUES, copyrightValues);
        }
        else if (page == 5)
        {
            model.put (KEY_ERROR_MSG, OAuthCallbackServlet.getGoogleAuthnError());
        }
        return model;
    }

    private String prepareCallbackUrl(HttpServletRequest request) {

        StringBuilder callbackBuilder = new StringBuilder(Web.serverUrl(request));

        callbackBuilder.append(request.getContextPath());
        callbackBuilder.append(getOauthCallbackPath());

        OAuthCallbackServlet.setCallbackUrl(callbackBuilder.toString());

        return callbackBuilder.toString();
    }

    @Override
    protected void validatePage(Object o, Errors errors, int i)
    {
        super.validatePage(o, errors, i);
    }

    @Override
    protected int getTargetPage(HttpServletRequest request, Object command, Errors errors, int currentPage)
    {
        // if this is a CSV or TSV export, the user must select the worksheet to use for the export
        if (currentPage == 2)
        {
            CommandBean
                cb = (CommandBean)command;

            if ("csv".equals(cb.getExportType()) || "tsv".equals(cb.getExportType()))
            {
                return 4;
            }
        }

        return super.getTargetPage(request, command, errors, currentPage);
    }

    @Override
    protected void onBind(HttpServletRequest req, Object command, BindException be)
        throws Exception
    {
        LOG.debug ("onBind()");

        CommandBean
            cBean = (CommandBean) command;

        DateBean
            start = cBean.getStartDateBean(),
            end = cBean.getEndDateBean();

        Date
            startDate = start.getDate(),
            endDate = end.getDate();

        if (LOG.isDebugEnabled())
        {
            DateFormat
                df = DateFormat.getDateInstance();

            if (startDate != null)
                LOG.debug ("startDate is: " + df.format(startDate));
            if (endDate != null)
                LOG.debug ("endDate is: " + df.format(endDate));
        }
        if (start != null)
        {
            cBean.setStartDate(start.getDate());
        }
        if (end != null)
            cBean.setEndDate(end.getDate());
    }

    protected ModelAndView processFinish(HttpServletRequest req,
                                         HttpServletResponse resp,
                                         Object command,
                                         BindException e)
        throws Exception
    {
        final ContentHostingService
            chs = getContentHostingService();
        final GoogleDocsService
            google = getGoogleDocsService();
        final ToolSession
            toolSession = SessionManager.getCurrentToolSession();
        final ResourceToolActionPipe
            pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
        final CommandBean
            cBean = (CommandBean)command;
        final GoogleDocDescriptor
            googleDoc = cBean.getDocDescriptor();
        final ContentEntity
            contentEntity = pipe.getContentEntity();

        cBean.setOwnerId(SessionManager.getCurrentSessionUserId());

        final String
            name = googleDoc.getTitle(),
            descriptorXML = google.descriptorToXML(googleDoc),
            copyright = cBean.getCopyright();

        final ContentResourceEdit
           resource = chs.addResource(contentEntity.getId(), name, googleDoc.getExportType(), ContentHostingService.MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);

        final ResourcePropertiesEdit
           properties = resource.getPropertiesEdit();

        properties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
        if (copyright != null && copyright.length() > 0)
        {
            properties.addProperty(ResourceProperties.PROP_COPYRIGHT, copyright);
            if (cBean.isCopyrightAlertSelected())
                properties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString()); 
        }
        properties.addProperty(chs.PROP_ALTERNATE_REFERENCE, GoogleDocumentType.REFERENCE_ROOT);
        resource.setContent(descriptorXML.getBytes());
        resource.setContentType(googleDoc.getExportType());
        resource.setResourceType(GoogleDocumentType.TYPE_ID);

        boolean
            hidden = !cBean.isShowSelected();
        Time
            releaseTime = null,
            retractTime = null;

        if(!hidden)
        {
            Date
                release = cBean.getStartDate(),
                retract = cBean.getEndDate();

            if (cBean.isReleaseSelected() && release != null)
            {
                releaseTime = TimeService.newTime(release.getTime());
            }
            if (cBean.isRetractSelected() && retract != null)
            {
                retractTime = TimeService.newTime(retract.getTime());
            }
        }

        resource.setAvailability(hidden, releaseTime, retractTime);

        String
            description = cBean.getDescription();

        if (description != null && description.trim().length() > 0)
            properties.addProperty(ResourceProperties.PROP_DESCRIPTION, cBean.getDescription());


        getContentHostingService().commitResource(resource, defaultNotification);

        pipe.setActionCanceled(false);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true);

        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
        toolSession.removeAttribute(ResourceToolAction.STARTED);
        final Tool
            tool = ToolManager.getCurrentTool();
        final String
            url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
        toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

        try
        {
            LOG.debug("finished Google Document Link helper; redirecting to " + url);
            GoogleHelperState.clear();
            resp.sendRedirect(url);
        }
        catch (IOException ioe)
        {
            LOG.warn("IOException", ioe);
        }

        return null;

    }

    @Override
    protected ModelAndView processCancel(HttpServletRequest req,
                                         HttpServletResponse resp,
                                         Object command,
                                         BindException e)
        throws Exception
    {
        final ToolSession
            toolSession = SessionManager.getCurrentToolSession();
        final ResourceToolActionPipe
            pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

        pipe.setActionCanceled(true);
        pipe.setErrorEncountered(false);
        pipe.setActionCompleted(true);

        toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
        toolSession.removeAttribute(ResourceToolAction.STARTED);
        final Tool
            tool = ToolManager.getCurrentTool();
        final String
            url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
        toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

        try
        {
            LOG.debug("Cancelled Document Link helper; redirecting to " + url);
            GoogleHelperState.clear();
            resp.sendRedirect(url);
        }
        catch (IOException ioe)
        {
            LOG.warn("IOException", ioe);
        }

        return null;
    }

    public void setDefaultNotification(int defaultNotification) {
        this.defaultNotification = defaultNotification;
    }
}