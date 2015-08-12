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

package com.rsmart.content.google.mock;

import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.oauth.api.OAuthProvider;

/**
 * User: duffy
 * Date: Mar 25, 2010
 * Time: 3:17:24 PM
 */
public class MockOAuthTokenImpl
    implements OAuthToken
{
    private String uuid;
    private String userId;
    private String oAuthToken;
    private String oAuthTokenSecret;

    private OAuthProvider oAuthProvider;

    public void setOAuthProvider(OAuthProvider oAuthProvider) {
        this.oAuthProvider = oAuthProvider;
    }

    public void setOAuthToken(String oAuthToken) {
        this.oAuthToken = oAuthToken;
    }

    public void setOAuthTokenSecret(String oAuthTokenSecret) {
        this.oAuthTokenSecret = oAuthTokenSecret;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUserId() {
        return userId;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getTokenValue() {
        return oAuthToken;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getTokenSecret() {
        return oAuthTokenSecret;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public OAuthProvider getProvider() {
        return oAuthProvider;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
