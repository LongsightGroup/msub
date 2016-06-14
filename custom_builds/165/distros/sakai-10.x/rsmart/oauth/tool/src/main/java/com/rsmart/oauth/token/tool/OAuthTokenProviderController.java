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

package com.rsmart.oauth.token.tool;

import com.rsmart.oauth.token.tool.util.OAuthTokenAdminState;
import com.rsmart.oauth.api.OAuthTokenService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jun 23, 2010
 * Time: 10:30:41 AM
 * To change this template use File | Settings | File Templates.
 */


public class OAuthTokenProviderController
    extends SimpleFormController
{

    private static String
        STATE_KEY = "oAuthTokenAdminState";

    private OAuthTokenService
        tokenMgr = null;

    OAuthTokenProviderController()
    {
        setCommandClass(OAuthTokenAdminState.class);
    }

    public void setOAuthTokenService (OAuthTokenService tokenSvc)
    {
        tokenMgr = tokenSvc;
    }

    public OAuthTokenService getOAuthTokenService ()
    {
        return tokenMgr;
    }
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
        throws Exception
    {
        OAuthTokenAdminState
            newState = (OAuthTokenAdminState) command,
            state = OAuthTokenAdminState.getState();

        state.setUserTokens (tokenMgr.findOAuthTokensForUser(state.getUserId()));

        return new ModelAndView(getSuccessView(), STATE_KEY, state);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        super.handleRequestInternal(request, response);

        String
            uuids = request.getParameter("uuid");
        OAuthTokenAdminState
            state = OAuthTokenAdminState.getState();

        if (uuids != null)
        {
            tokenMgr.deleteOAuthTokens(addValuesToSet(uuids));
        }

        state.setUserTokens (tokenMgr.findOAuthTokensForUser(state.getUserId()));

        return new ModelAndView(getSuccessView(), STATE_KEY, state);
    }

    private Set addValuesToSet(String uuids)
    {
        Set
            parsedValues = new HashSet();
        String[]
            spiltValues = uuids.split(",");

        for (int i = 0; i < spiltValues.length; i++)
        {
            parsedValues.add(spiltValues[i]);
        }

        return parsedValues;
    }
}
