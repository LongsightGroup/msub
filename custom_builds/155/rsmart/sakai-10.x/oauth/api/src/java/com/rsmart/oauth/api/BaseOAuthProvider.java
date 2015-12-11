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

package com.rsmart.oauth.api;

import java.util.HashMap;
import java.util.Map;

public class BaseOAuthProvider
    implements OAuthProvider
{
    protected String
        uuid,
        providerName,
        description,
        authUrl,
        tokenUrl,
        clientId,
        clientSecret,
        requestTokenURL,
        userAuthorizationURL,
        accessTokenURL,
        realm,
        hmacSha1SharedSecret,
        rsaSha1Key,
        consumerKey;

    protected OAuthSignatureMethod
        signatureMethod = OAuthSignatureMethod.HMAC_SHA1;

    protected Map<String, String>
        additionalHeaders = new HashMap<String, String>();

    protected boolean
        enabled = false;

    public String getAccessTokenURL()
    {
        return accessTokenURL;
    }

    public void setAccessTokenURL(String accessTokenURL)
    {
        this.accessTokenURL = accessTokenURL;
    }

    public String getHmacSha1SharedSecret()
    {
        return hmacSha1SharedSecret;
    }

    public void setHmacSha1SharedSecret(String hmacSha1SharedSecret)
    {
        this.hmacSha1SharedSecret = hmacSha1SharedSecret;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public String getRequestTokenURL()
    {
        return requestTokenURL;
    }

    public void setRequestTokenURL(String requestTokenURL)
    {
        this.requestTokenURL = requestTokenURL;
    }

    public String getRsaSha1Key()
    {
        return rsaSha1Key;
    }

    public void setRsaSha1Key(String rsaSha1Key)
    {
        this.rsaSha1Key = rsaSha1Key;
    }

    public OAuthSignatureMethod getSignatureMethod()
    {
        return signatureMethod;
    }

    public void setSignatureMethod(OAuthSignatureMethod signatureMethod)
    {
        this.signatureMethod = signatureMethod;
    }

    public String getUserAuthorizationURL()
    {
        return userAuthorizationURL;
    }

    public void setUserAuthorizationURL(String userAuthorizationURL)
    {
        this.userAuthorizationURL = userAuthorizationURL;
    }

    public String getConsumerKey()
    {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey)
    {
        this.consumerKey = consumerKey;
    }

    /**
     * @deprecated
     * @return
     */
    public String getConsumerSecret()
    {
        return getHmacSha1SharedSecret();
    }

    /**
     * @deprecated
     * @param secret
     */
    public void setConsumerSecret (String secret)
    {
        setHmacSha1SharedSecret(secret);
    }

    public Map<String, String> getAdditionalHeaders()
    {
        return additionalHeaders;
    }

    public void setAdditionalHeaders(Map<String, String> additionalHeaders)
    {
        this.additionalHeaders = additionalHeaders;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthUrl(String authUrl) {
        this.authUrl = authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    public String getUUID()
    {
        return uuid;
    }

    public void setUUID(String uuid)
    {
        this.uuid = uuid;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean e)
    {
        enabled = e;
    }
}
