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

package com.rsmart.oauth.provider.tool.util;

import com.rsmart.oauth.api.BaseOAuthProvider;
import com.rsmart.oauth.api.OAuthProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OAuthAdminToolState
{
    private static final Log
        LOG = LogFactory.getLog(OAuthAdminToolState.class);

    public static final String
        OAUTH_ADMIN_TOOL_STATE = OAuthAdminToolState.class.getName();

    private Set<OAuthProvider>
        providers = null;

    private BaseOAuthProvider
        currentProvider = null;

    private ProviderErrors
        providerErrors = null;

    private String
        newAdditionalHeaderKey,
        newAdditionalHeaderValue;

    public OAuthAdminToolState ()
    {
        reset();
    }

    public void reset()
    {
        currentProvider = new BaseOAuthProvider();
        providerErrors = new ProviderErrors();
        providers = null;
        newAdditionalHeaderKey = null;
        newAdditionalHeaderValue = null;
    }

    public void setProviders (Set<OAuthProvider> providers)
    {
        if (providers == null)
            this.providers = null;
        else
        {
            this.providers = new HashSet<OAuthProvider>(providers.size());
            this.providers.addAll(providers);
        }
    }

    public Set<OAuthProvider> getProviders()
    {
        return providers;
    }

    public void setProviderErrors(ProviderErrors errors)
    {
        providerErrors = errors;
    }

    public ProviderErrors getProviderErrors()
    {
        return providerErrors;
    }

    public void setCurrentProvider (BaseOAuthProvider provider)
    {
        this.currentProvider = provider;
    }

    public void setCurrentProvider (OAuthProvider provider)
    {
        currentProvider.setAccessTokenURL(provider.getAccessTokenURL());

        final Map<String, String>
            headers = provider.getAdditionalHeaders();

        if (headers != null)
        {
            currentProvider.setAdditionalHeaders(headers);
        }

        currentProvider.setConsumerKey(provider.getConsumerKey());
        currentProvider.setDescription(provider.getDescription());
        currentProvider.setEnabled(provider.isEnabled());
        currentProvider.setHmacSha1SharedSecret(provider.getHmacSha1SharedSecret());
        currentProvider.setProviderName(provider.getProviderName());
        currentProvider.setRealm(provider.getRealm());
        currentProvider.setRequestTokenURL(provider.getRequestTokenURL());
        currentProvider.setRsaSha1Key(provider.getRsaSha1Key());
        currentProvider.setSignatureMethod(provider.getSignatureMethod());
        currentProvider.setUserAuthorizationURL(provider.getUserAuthorizationURL());
        currentProvider.setUUID(provider.getUUID());
    }

    public BaseOAuthProvider getCurrentProvider()
    {
        return currentProvider;
    }

    public String getNewAdditionalHeaderKey() {
        return newAdditionalHeaderKey;
    }

    public void setNewAdditionalHeaderKey(String newAdditionalHeaderKey) {
        this.newAdditionalHeaderKey = newAdditionalHeaderKey;
    }

    public String getNewAdditionalHeaderValue() {
        return newAdditionalHeaderValue;
    }

    public void setNewAdditionalHeaderValue(String newAdditionalHeaderValue) {
        this.newAdditionalHeaderValue = newAdditionalHeaderValue;
    }

    private static final ToolSession session()
    {
        final ToolSession
            session = SessionManager.getCurrentToolSession();

        if (session == null)
        {
            LOG.fatal("No tool session found; cannot manage OAuthAdminToolState object");
        }

        return session;
    }

    public static final OAuthAdminToolState getState()
    {
        final ToolSession
            session = session();

        if (session == null)
        {
            return null;
        }

        OAuthAdminToolState
            state = (OAuthAdminToolState) session.getAttribute(OAUTH_ADMIN_TOOL_STATE);

        if (state == null)
        {
            state = new OAuthAdminToolState();

            session.setAttribute(OAUTH_ADMIN_TOOL_STATE, state);
        }

        return state;
    }

    public static final void clear()
    {
        final ToolSession
            session = session();

        if (session != null)
        {
            session.removeAttribute(OAUTH_ADMIN_TOOL_STATE);
        }
    }

}