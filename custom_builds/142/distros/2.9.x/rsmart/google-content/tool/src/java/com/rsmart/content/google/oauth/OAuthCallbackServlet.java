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

package com.rsmart.content.google.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.component.cover.ComponentManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.io.IOException;

import com.rsmart.content.google.api.GoogleDocsService;
import com.rsmart.content.google.api.GoogleDocsException;

/**
 * User: duffy
 * Date: Feb 3, 2010
 * Time: 10:06:29 AM
 */
public class OAuthCallbackServlet
    extends HttpServlet
{
    private final static Log
        LOG = LogFactory.getLog (OAuthCallbackServlet.class);

    public static final String
        OAUTH_CALLBACK_RETURN_URL = "oauth.callback.returnURL",
        OAUTH_CALLBACK_SUCCESS = "oauth.callback.success",
        OAUTH_CALLBACK_ERROR = "oauth.callback.error";

    private GoogleDocsService
        google = null;

    public void init(ServletConfig cfg)
        throws ServletException
    {
        google = (GoogleDocsService) ComponentManager.get(GoogleDocsService.class.getName());
    }

    private static final Session session()
    {
        return SessionManager.getCurrentSession();
    }

    public static final void reset()
    {
        final Session
            session = session();
        
        session.removeAttribute(OAUTH_CALLBACK_RETURN_URL);
        session.removeAttribute(OAUTH_CALLBACK_SUCCESS);
        session.removeAttribute(OAUTH_CALLBACK_ERROR);
    }

    public static final void setReturnURL (String returnUrl)
    {
        session().setAttribute(OAUTH_CALLBACK_RETURN_URL, returnUrl);
    }

    public static final String getReturnURL ()
    {
        final Session
            session = session();

        if (session == null) return null;

        return (String) session.getAttribute(OAUTH_CALLBACK_RETURN_URL);
    }

    public static final boolean isGoogleAuthnSuccessful()
    {
        final Session
            session = session();

        return (session != null && session.getAttribute(OAUTH_CALLBACK_SUCCESS) != null);
    }

    public static final void setGoogleAuthnSuccessful(boolean success)
    {
        if (success)
        {
            session().setAttribute(OAUTH_CALLBACK_SUCCESS, OAUTH_CALLBACK_SUCCESS);
        }
        else
        {
            session().removeAttribute(OAUTH_CALLBACK_SUCCESS);
        }
    }

    public static final void setGoogleAuthnError (String error)
    {
        session().setAttribute(OAUTH_CALLBACK_ERROR, error);
    }

    public static final String getGoogleAuthnError ()
    {
        return (String) session().getAttribute(OAUTH_CALLBACK_ERROR);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String
			returnURL = getReturnURL();
        boolean
            success = true;

        if (returnURL == null)
        {
            LOG.error ("post-login return URL was not set, redirecting to error page");
            final ToolSession
                toolSession = SessionManager.getCurrentToolSession();
            final ToolConfiguration
                toolConf = SiteService.findTool (toolSession.getId());

            returnURL = ServerConfigurationService.getString("portalPath", "/portal") + "/site/" + toolConf.getSiteId();
        }

        try
        {
            final String
                queryString = request.getQueryString();

            google.processAuthorizedAccessToken(queryString);
        }
        catch (GoogleDocsException e)
        {
            LOG.error("Error processing results of Google OAuth", e);

            success = false;
            setGoogleAuthnError(e.getMessage());
        }

        setGoogleAuthnSuccessful(success);

		response.sendRedirect(response.encodeRedirectURL(returnURL));
    }

    @Override
    public String getServletInfo()
    {
        return "OAuth Callback Endpoint";
    }

}
