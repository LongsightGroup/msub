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

import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.rsmart.content.google.api.GoogleDocDescriptor;

/**
 * User: duffy
 * Date: Feb 10, 2010
 * Time: 3:08:46 PM
 */
public class GoogleHelperState
{
    private static final Log
        LOG = LogFactory.getLog(GoogleHelperState.class);

    public static final String
        GOOGLE_STATE                = "com.rsmart.content.google.helper.GoogleHelperState";

    private boolean
        googleAuthed = false,
        firstAuthAttempt = true;

    private CommandBean
        cBean = null;

    private static final ToolSession session()
    {
        final ToolSession
            session = SessionManager.getCurrentToolSession();

        if (session == null)
        {
            LOG.fatal("No tool session found; cannot manage GoogleHelperState object");
        }

        return session;
    }

    public static final GoogleHelperState getState()
    {
        final ToolSession
            session = session();

        if (session == null)
        {
            return null;
        }

        GoogleHelperState
            state = (GoogleHelperState) session.getAttribute(GOOGLE_STATE);

        if (state == null)
        {
            state = new GoogleHelperState();

            session.setAttribute(GOOGLE_STATE, state);
        }

        return state;
    }

    public static final void clear()
    {
        final ToolSession
            session = session();

        if (session != null)
        {
            session.removeAttribute(GOOGLE_STATE);
        }
    }

    public boolean isAuthenticatedWithGoogle()
    {
        return googleAuthed;
    }

    public void setAuthenticatedWithGoogle(boolean auth)
    {
        googleAuthed = auth;
    }

    public boolean isFirstAuthenticationAttempt()
    {
        return firstAuthAttempt;
    }

    public void setFirstAuthenticationAttempt(boolean first)
    {
        firstAuthAttempt = first;
    }

    public void setCommandBean (CommandBean cb)
    {
        cBean = cb;
    }

    public CommandBean getCommandBean()
    {
        return cBean;
    }
}
