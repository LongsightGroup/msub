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

import com.rsmart.oauth.api.OAuthProvider;

import java.util.Set;
import java.util.Map;

/**
 * User: duffy
 * Date: Mar 25, 2010
 * Time: 3:05:02 PM
 */
public class MockOAuthProviderImpl
    implements OAuthProvider
{
    private String
        uuid;
    private String name;
    private String description;
    private String oAuthRequestTokenURL;
    private String oAuthAuthorizationURL;
    private String oAuthAccessTokenURL;
    private String oAuthRealm;
    private String oAuthConsumerKey;
    private String oAuthConsumerSecret;
    private Map<String, String> additionalHeaders;

    public void setDescription (String desc)
    {
        this.description = desc;
    }

    public String getDescription()
    {
        return description;
    }
    public void setOAuthConsumerKey(String oAuthConsumerKey) {
        this.oAuthConsumerKey = oAuthConsumerKey;
    }

    public void setOAuthConsumerSecret(String oAuthConsumerSecret) {
        this.oAuthConsumerSecret = oAuthConsumerSecret;
    }

    public void setOAuthRealm(String oAuthRealm) {
        this.oAuthRealm = oAuthRealm;
    }

    public String getAccessTokenURL() {
        return oAuthAccessTokenURL;
    }

    public void setOAuthAccessTokenURL(String oAuthAccessTokenURL) {
        this.oAuthAccessTokenURL = oAuthAccessTokenURL;
    }

    public void setOAuthUserAuthorizationURL(String oAuthAuthorizationURL) {
        this.oAuthAuthorizationURL = oAuthAuthorizationURL;
    }

    public String getUserAuthorizationURL()
    {
        return oAuthAuthorizationURL;
    }

    public String getRequestTokenURL() {
        return oAuthRequestTokenURL;
    }

    public void setOAuthRequestTokenURL(String oAuthRequestTokenURL) {
        this.oAuthRequestTokenURL = oAuthRequestTokenURL;
    }

    public void setUUID(String uuid)
    {
        this.uuid = uuid;
    }
    
    public String getUUID() {
        return uuid;
    }

    public void setProviderName(String name)
    {
        this.name = name;
    }

    public String getProviderName()
    {
        return name;
    }

    public String getConsumerKey() {
        return oAuthConsumerKey;
    }

    public String getConsumerSecret() {
        return oAuthConsumerSecret;
    }

    public String getRealm() {
        return oAuthRealm;
    }

    public Map<String, String> getAdditionalHeaders()
    {
        return additionalHeaders;
    }

    public void setAdditionalHeaders(Map<String, String> headers)
    {
        additionalHeaders = headers;
    }

}
