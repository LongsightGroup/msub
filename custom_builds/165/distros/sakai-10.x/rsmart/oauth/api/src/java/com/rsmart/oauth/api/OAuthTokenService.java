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

import com.rsmart.persistence.PersistenceException;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.persistence.DataIntegrityException;

import java.util.Map;
import java.util.Set;

/**
 * User: duffy
 * Date: Jan 19, 2010
 * Time: 4:44:51 PM
 */
public interface OAuthTokenService
{

    /**
     * Registers a new OAuthProvider.  This method creates an OAuthProvider without additional headers to include
     * in OAuth requests.
     *
     * @param providerName
     * @param description
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @deprecated
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret)
        throws DataIntegrityException, PersistenceException;

    /**
     * Registers a new OAuthProvider.  This method creates an OAuthProvider without additional headers to include
     * in OAuth requests.
     *
     * @param providerName
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @param enabled
     *
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret,
                                              boolean enabled)
        throws DataIntegrityException, PersistenceException;

    /**
     * Registers a new OAuthProvider.  This version of createOAuthProvider includes additionalHeaders, a (key, value)
     * Map which contains additional headers to include in OAuth requests as may be required by some providers.
     *
     * @param providerName
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @param additionalHeaders
     * @deprecated
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret,
                                              Map<String, String> additionalHeaders)
        throws DataIntegrityException, PersistenceException;

    /**
     * Registers a new OAuthProvider.  This version of createOAuthProvider includes additionalHeaders, a (key, value)
     * Map which contains additional headers to include in OAuth requests as may be required by some providers.
     *
     * @param providerName
     * @param requestTokenURL
     * @param userAuthorizationURL
     * @param accessTokenURL
     * @param realm
     * @param consumerKey
     * @param hmacSha1SharedSecret
     * @param rsaSha1Key
     * @param signatureMethod
     * @param additionalHeaders
     * @param enabled
     *
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String requestTokenURL,
                                              String userAuthorizationURL,
                                              String accessTokenURL,
                                              String realm,
                                              String consumerKey,
                                              String hmacSha1SharedSecret,
                                              String rsaSha1Key,
                                              OAuthSignatureMethod signatureMethod,
                                              Map<String, String> additionalHeaders,
                                              boolean enabled)
            throws DataIntegrityException, PersistenceException;

    /**
     * Registers a new OAuthProvider.  This version of createOAuthProvider includes additionalHeaders, a (key, value)
     * Map which contains additional headers to include in OAuth requests as may be required by some providers.
     *
     * @param providerName
     * @param description
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @param additionalHeaders
     * @param enabled
     *
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthProvider createOAuthProvider (String providerName,
                                              String description,
                                              String authUrl,
                                              String tokenUrl,
                                              String clientId,
                                              String clientSecret,
                                              Map<String, String> additionalHeaders,
                                              boolean enabled)
        throws DataIntegrityException, PersistenceException;
    /**
     * Retrieves the set of all OAuthProviders which have been registered.
     * 
     * @throws PersistenceException
     */
    public Set<OAuthProvider> getAllOAuthProviders()
        throws PersistenceException;

    /**
     * Retrieves an OAuthProvider by its UUID.
     *
     * @param uuid
     * @return
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public OAuthProvider getOAuthProvider (String uuid)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Retrieves an OAuthProvider by its name.
     *
     * @param providerName
     * @return
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public OAuthProvider getOAuthProviderByName (String providerName)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Stores changes to a OAuthProvider object.
     *
     * @param provider
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void updateOAuthProvider (OAuthProvider provider)
        throws NoSuchObjectException, PersistenceException;


    /**
     *  Stores the changes to a OAuthProvider object.
     * @param providerName
     * @param description
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @param additionalHeaders
     * @deprecated
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String authUrl,
                                    String tokenUrl,
                                    String clientId,
                                    String clientSecret,
                                    Map<String, String> additionalHeaders)
            throws NoSuchObjectException, PersistenceException;

    /**
     *  Stores the changes to a OAuthProvider object.
     * @param providerName
     * @param requestTokenURL
     * @param userAuthorizationURL
     * @param accessTokenURL
     * @param realm
     * @param consumerKey
     * @param hmacSha1SharedSecret
     * @param rsaSha1Key
     * @param signatureMethod
     * @param additionalHeaders
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String requestTokenURL,
                                    String userAuthorizationURL,
                                    String accessTokenURL,
                                    String realm,
                                    String consumerKey,
                                    String hmacSha1SharedSecret,
                                    String rsaSha1Key,
                                    OAuthSignatureMethod signatureMethod,
                                    Map<String, String> additionalHeaders,
                                    boolean enabled)
            throws NoSuchObjectException, PersistenceException;

    /**
     *  Stores the changes to a OAuthProvider object.
     * @param uuid
     * @param providerName
     * @param description
     * @param authUrl
     * @param tokenUrl
     * @param clientId
     * @param clientSecret
     * @param additionalHeaders
     * @param enabled
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void updateOAuthProvider(String uuid,
                                    String providerName,
                                    String description,
                                    String authUrl,
                                    String tokenUrl,
                                    String clientId,
                                    String clientSecret,
                                    Map<String, String> additionalHeaders,
                                    boolean enabled)
            throws NoSuchObjectException, PersistenceException;

    /**
     * Adds a single additional header to a provider.
     *
     * @param providerUUID
     * @param key
     * @param value
     * @throws NoSuchObjectException
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public void setAdditionalHeader (String providerUUID, String key, String value)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Replaces all additional headers for a provider with the supplied map.
     *
     * @param providerUUID
     * @param headers
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void setAdditionalHeaders (String providerUUID, Map<String, String> headers)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes an OAuthProvider.
     *
     * @param uuid
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void deleteOAuthProvider (String uuid) 
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes a single additional header from an OAuthProvider.
     * 
     * @param providerUUID
     * @param key
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void removeAdditionalHeader (String providerUUID, String key)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes additional headers from an OAuthProvider.
     *
     * @param uuid
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void removeAdditionalHeaders (String uuid)
        throws NoSuchObjectException, PersistenceException;
    
    /**
     * Deletes the OAuthProviders indicated by the set of UUIDs passed in to this method.
     * The entire operation will fail with a NoSuchObjectException if any of the UUIDs does
     * not exist in the database.
     *
     * @param uuids
     */
    public void deleteOAuthProviders (Set<String> uuids)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Creates a new OAuthToken.
     *
     * @param providerUUID
     * @param token
     * @param tokenSecret
     * @param userId
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthToken createOAuthToken (String providerUUID, String token, String tokenSecret, String userId)
        throws DataIntegrityException, PersistenceException;

    /**
     * Creates a new OAuthToken.
     *
     * @param providerUUID
     * @param token
     * @param userId
     * @return
     * @throws DataIntegrityException
     * @throws PersistenceException
     */
    public OAuthToken createOAuth2Token (String providerUUID, String token, String userId)
            throws DataIntegrityException, PersistenceException;

    /**
     * Retrieve an OAuthToken by UUID.
     *
     * @param tokenUUID
     * @return OAuthToken referenced by the supplied uuid
     * @throws OAuthTokenException
     */
    public OAuthToken getOAuthToken (String tokenUUID)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Retrieve the OAuthToken associated with the given provider, scope and user.  If more than one such token exists
     * this method will fail.  Scope is an optional parameter and may be null.
     *
     * @param providerUUID
     * @param userId
     * @return OAuthToken
     * @throws OAuthTokenException
     */
    public OAuthToken getOAuthToken (String providerUUID, String userId)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Retrieves all OAuthTokens for a given OAuthProvider.
     *
     * @param providerUUID
     * @return
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public Set<OAuthToken> findOAuthTokensForProvider (String providerUUID)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Retrieves all OAuthTokens for a given user.
     *
     * @param userId
     * @return
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public Set<OAuthToken> findOAuthTokensForUser (String userId)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes the OAuthToken referenced by the given uuid.
     *
     * @param tokenUUID
     * @throws OAuthTokenException
     */
    public void deleteOAuthToken (String tokenUUID)
        throws NoSuchObjectException, PersistenceException;

    public void deleteOAuthTokens (Set<String> tokenUUIDs)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes all OAuthTokens for a given user Id.
     * 
     * @param userId
     * @throws OAuthTokenException
     */
    public void deleteOAuthTokensForUser (String userId)
        throws NoSuchObjectException, PersistenceException;

    /**
     * Removes all OAuthTokens for a given provider.
     *
     * @param providerUUID
     * @throws OAuthTokenException
     */
    public void deleteOAuthTokensForProvider (String providerUUID)
        throws NoSuchObjectException, PersistenceException;

    /**
     *
     * @param providerName
     * @Exception
     * @return
     */
    public boolean providerExists(String providerName)
        throws PersistenceException;

    public boolean getProviderStatus (String providerName)
        throws PersistenceException;

    public void setProviderStatus (String providerName, boolean status)
        throws NoSuchObjectException, PersistenceException;
}