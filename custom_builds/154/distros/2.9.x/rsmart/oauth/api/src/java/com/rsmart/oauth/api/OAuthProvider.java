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

import java.util.Map;

/**
 * This interface represents an OAuth server as defined by the OAuth Draft specification here:
 *
 * http://tools.ietf.org/html/draft-hammer-oauth-08#page-5
 *
 * OAuthProvider is meant to represent the authority which responds to requests for data on behalf of a user.
 * This implementation extends the notion of an OAuth sever by including available scopes which allows finer
 * grained distinction between authorized actions by the OAuth server and client.  Scope is an extension made
 * by Google (http://code.google.com/apis/accounts/docs/OAuth_ref.html#RequestToken) to accommodate the creation
 * of OAuth access tokens which provide access to only select services within a realm.
 *
 * User: Duffy Gillman <duffy@rsmart.com>
 * Date: Jan 20, 2010
 * Time: 4:34:27 PM
 */
public interface OAuthProvider
{
    /**
     * Uniquely identifies this OAuthProvider within the OAuthTokenService.
     *
     * @return uuid
     */
    public String getUUID();

    /**
     * The name to use for this provider in any user interface.
     *
     * @return provider name
     */
    public String getProviderName();

    /**
     * Describes this OAuth provider for use in user interfaces.
     * 
     * @return
     */
    public String getDescription();

    public String getRequestTokenURL();

    public String getUserAuthorizationURL();

    public String getAccessTokenURL();
    
    /**
     * Optional realm parameter for OAuth requests to this provider as defined by OAuth specification
     * (http://tools.ietf.org/html/draft-hammer-oauth-08#page-28).
     *
     * @return realm
     */
    public String getRealm();

    /**
     * Returns the signature method configured for this provider. As of version 1.0 of the OAuth protocol this value
     * may be PLAINTEXT, HMAC-SHA1, or RSA_SHA1.
     * 
     * @return
     */
    public OAuthSignatureMethod getSignatureMethod();

    /**
     * Returns the shared secret arranged with the OAuth provider for use with HMAC-SHA1 signature method only.
     *
     * @return
     */
    public String getHmacSha1SharedSecret();

    /**
     * Returns the String representation of the PKCS8 formatted private key for generating an RSA signature to be
     * used with the RSA-SHA1 signature method only.
     *
     * @return
     */
    public String getRsaSha1Key();

    /**
     * Consumer key registered for use when making requests to this provider.
     *
     * @return consumer key
     */
    public String getConsumerKey();

    /**
     * Consumer secret registered for authentication with this provider.
     * <b>Deprecated</b>: use getHmacSha1SharedSecret()
     *
     * @deprecated
     * @return consumer secret
     */
    public String getConsumerSecret();

    /**
     * Retrieves additional OAuth header properties for Provider
     *
     * @return
     */
    public Map<String, String> getAdditionalHeaders();

    public boolean isEnabled();

}
