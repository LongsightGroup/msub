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

package com.rsmart.oauth.token.tool.util;

import com.rsmart.oauth.api.OAuthToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 19, 2011
 * Time: 4:39:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthTokenAdminState
{
    private static Log
        LOG = LogFactory.getLog(OAuthTokenAdminState.class);
    private static String
        OAUTH_TOKEN_ADMIN_STATE = OAuthTokenAdminState.class.getName();
    private Set<OAuthToken>
        userTokens;
    
    public OAuthTokenAdminState()
    {
        reset();
    }

    public void reset()
    {
        userTokens = new HashSet<OAuthToken>();
    }

    public String getUserId()
    {
        return session().getUserId();
    }

    public void setUserTokens(Set<OAuthToken> tokens)
    {
        userTokens.clear();
        userTokens.addAll(tokens);
    }

    public Set<OAuthToken> getUserTokens()
    {
        return userTokens;
    }

    private static final ToolSession session()
    {
        final ToolSession
            session = SessionManager.getCurrentToolSession();

        if (session == null)
        {
            LOG.fatal("No tool session found; cannot manage OAuthTokenAdminState object");
        }

        return session;
    }

    public static final OAuthTokenAdminState getState()
    {
        final ToolSession
            session = session();

        if (session == null)
        {
            return null;
        }

        OAuthTokenAdminState
            state = (OAuthTokenAdminState) session.getAttribute(OAUTH_TOKEN_ADMIN_STATE);

        if (state == null)
        {
            state = new OAuthTokenAdminState();

            session.setAttribute(OAUTH_TOKEN_ADMIN_STATE, state);
        }

        return state;
    }

    public static final void clear()
    {
        final ToolSession
            session = session();

        if (session != null)
        {
            session.removeAttribute(OAUTH_TOKEN_ADMIN_STATE);
        }
    }


}
