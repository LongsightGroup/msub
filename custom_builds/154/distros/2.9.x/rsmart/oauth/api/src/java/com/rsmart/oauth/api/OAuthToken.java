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

/**
 * This represents a valid access token granted by an OAuthProvider validated by an end user on that provider.
 * It associates the token with the local user account which initiated the creation of the token.  This token
 * contains the token string and token secret string required to access data on the user's behalf from the
 * OAuthProvider.
 *
 * User: duffy
 * Date: Jan 19, 2010
 * Time: 4:45:11 PM
 */
public interface OAuthToken
{
    /**
     * Uniquely identifies this OAuthToken within the OAuthTokenService.
     *
     * @return uuid
     */
    public String getUUID();

    /**
     * The local user who created this access token.
     *
     * @return user id
     */
    public String getUserId();

    /**
     * The token string to use when requesting data on behalf of the user.
     *
     * @return token string
     */
    public String getTokenValue();

    /**
     * The token secret string to use when requesting data on behalf of the user.
     *
     * @return token secret string
     */
    public String getTokenSecret();

    /**
     * OAuthProvider with which this token was created.
     *
     * @return OAuthProvider
     */
    public OAuthProvider getProvider();
}
