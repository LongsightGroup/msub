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

import com.rsmart.oauth.api.OAuthTokenService;
import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.persistence.DataIntegrityException;
import com.rsmart.persistence.PersistenceException;
import com.rsmart.persistence.NoSuchObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/**
 * User: duffy
 * Date: Mar 25, 2010
 * Time: 2:57:26 PM
 */
public class MockOAuthTokenServiceImpl
    implements OAuthTokenService
{
    private HashMap<String, OAuthProvider>
        providers = new HashMap<String, OAuthProvider>(),
        providersByName = new HashMap<String, OAuthProvider>();
    private HashMap<String, OAuthToken>
        tokens = new HashMap<String, OAuthToken>();
    private HashMap<String, HashSet<OAuthToken>>
        tokensByProvider = new HashMap<String, HashSet<OAuthToken>>();

    private int
        providerId = 1000,
        tokenId = 1000;

    public OAuthProvider createOAuthProvider (String providerName,
                                              String requestTokenURL,
                                              String userAuthorizationURL,
                                              String accessTokenURL,
                                              String realm,
                                              String consumerKey,
                                              String consumerSecret)
        throws DataIntegrityException, PersistenceException
    {
        return createOAuthProvider(providerName, requestTokenURL, userAuthorizationURL, accessTokenURL,
                                   realm, consumerKey, consumerSecret, new HashMap<String, String>());
    }

    public OAuthProvider createOAuthProvider (String providerName,
                                              String requestTokenURL,
                                              String userAuthorizationURL,
                                              String accessTokenURL,
                                              String realm,
                                              String consumerKey,
                                              String consumerSecret,
                                              Map<String, String> additionalHeaders)
        throws DataIntegrityException, PersistenceException
    {
        MockOAuthProviderImpl
            provider = new MockOAuthProviderImpl();
        String
            uuid = String.valueOf(providerId++);

        provider.setUUID(uuid);
        provider.setProviderName(providerName);
        provider.setOAuthRequestTokenURL(requestTokenURL);
        provider.setOAuthUserAuthorizationURL(userAuthorizationURL);
        provider.setOAuthAccessTokenURL(accessTokenURL);
        provider.setOAuthRealm(realm);
        provider.setOAuthConsumerKey(consumerKey);
        provider.setOAuthConsumerSecret(consumerSecret);
        provider.setAdditionalHeaders(additionalHeaders);

        providers.put(uuid, provider);
        providersByName.put (providerName, provider);

        return provider;
    }

    public Set<OAuthProvider> getAllOAuthProviders()
        throws PersistenceException
    {
        HashSet<OAuthProvider>
            result = new HashSet<OAuthProvider>();

        result.addAll(providers.values());

        return result;
    }

    public OAuthProvider getOAuthProvider(String s)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthProvider
            provider = providers.get(s);

        if (provider == null)
            throw new NoSuchObjectException();

        return provider;
    }

    public OAuthProvider getOAuthProviderByName(String s)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthProvider
            provider = providersByName.get(s);

        if (provider == null)
            throw new NoSuchObjectException();

        return provider;
    }

    public void updateOAuthProvider(OAuthProvider oAuthProvider)
        throws NoSuchObjectException, PersistenceException
    {
        String
            uuid = oAuthProvider.getUUID();

        if (!providers.containsKey(uuid))
        {
            throw new NoSuchObjectException ("No such provider exists");
        }

        providers.put(uuid, oAuthProvider);
        providersByName.put (oAuthProvider.getProviderName(), oAuthProvider);
    }

    public void setAdditionalHeader(String providerUUID, String key, String value)
        throws NoSuchObjectException, DataIntegrityException, PersistenceException
    {
        if (!providers.containsKey(providerUUID))
        {
            throw new NoSuchObjectException ("No such provider exists");
        }

        OAuthProvider
            provider = providers.get(providerUUID);

        Map<String, String>
            headers = provider.getAdditionalHeaders();

        headers.put(key, value);
    }

    public void setAdditionalHeaders(String providerUUID, Map<String, String> headers)
        throws NoSuchObjectException, PersistenceException
    {
        if (!providers.containsKey(providerUUID))
        {
            throw new NoSuchObjectException ("No such provider exists");
        }

        MockOAuthProviderImpl
            provider = (MockOAuthProviderImpl)providers.get(providerUUID);

        provider.setAdditionalHeaders(headers);
    }

    public void deleteOAuthProvider(String s)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthProvider
            provider = providers.get(s);

        if (provider == null)
            throw new NoSuchObjectException();

        HashSet<OAuthToken>
            tokens = tokensByProvider.get(provider.getUUID());

        for (OAuthToken token : tokens)
        {
            tokens.remove(token.getUUID());
        }

        tokensByProvider.remove(provider.getUUID());
        
        providersByName.remove(provider);
        providers.remove(provider);
    }

    public void removeAdditionalHeader(String providerUUID, String key)
        throws NoSuchObjectException, PersistenceException
    {
        if (!providers.containsKey(providerUUID))
        {
            throw new NoSuchObjectException ("No such provider exists");
        }

        OAuthProvider
            provider = providers.get(providerUUID);

        Map<String, String>
            headers = provider.getAdditionalHeaders();

        headers.remove(key);
    }

    public void removeAdditionalHeaders(String providerUUID)
        throws NoSuchObjectException, PersistenceException
    {
        if (!providers.containsKey(providerUUID))
        {
            throw new NoSuchObjectException ("No such provider exists");
        }

        OAuthProvider
            provider = providers.get(providerUUID);

        Map<String, String>
            headers = provider.getAdditionalHeaders();

        headers.clear();
    }

    public void deleteOAuthProviders(Set<String> uuids)
        throws NoSuchObjectException, PersistenceException
    {
        for (String uuid : uuids)
        {
            deleteOAuthProvider(uuid);
        }
    }

    public OAuthToken createOAuthToken(String providerUUID, String token, String tokenSecret, String userId)
        throws DataIntegrityException, PersistenceException
    {
        MockOAuthTokenImpl
            t = new MockOAuthTokenImpl();

        String
            uuid = String.valueOf(tokenId++);

        OAuthProvider
            provider = providers.get(providerUUID);

        if (provider == null)
        {
            throw new DataIntegrityException ("no provider");
        }

        t.setUUID(uuid);
        t.setOAuthProvider(provider);
        t.setOAuthToken(token);
        t.setOAuthTokenSecret(tokenSecret);
        t.setUserId(userId);

        tokens.put(uuid, t);

        HashSet<OAuthToken>
            providerTokens = tokensByProvider.get(provider.getUUID());

        if (providerTokens == null)
        {
            providerTokens = new HashSet<OAuthToken>();

            tokensByProvider.put(provider.getUUID(), providerTokens);
        }

        providerTokens.add(t);
        
        return t;
    }

    public OAuthToken getOAuthToken(String uuid)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthToken
            token = tokens.get(uuid);

        if (token == null)
            throw new NoSuchObjectException();

        return token;
    }

    public OAuthToken getOAuthToken(String provider, String user)
        throws NoSuchObjectException, PersistenceException
    {
        HashSet<OAuthToken>
            providerTokens = tokensByProvider.get(provider);

        if (user == null | user.length() < 1)
        {
            throw new PersistenceException ("A user must be supplied to getTokenValue (provider, user)");
        }
        if (providerTokens == null)
        {
            throw new NoSuchObjectException("no tokens for the supplied provider");
        }

        for (OAuthToken token : providerTokens)
        {
            if (user.equals(token.getUserId()))
                return token;
        }

        throw new NoSuchObjectException ("no token for that user");
    }

    public Set<OAuthToken> findOAuthTokensForProvider(String s)
        throws NoSuchObjectException, PersistenceException
    {
        return tokensByProvider.get(s);
    }

    public Set<OAuthToken> findOAuthTokensForUser(String s)
        throws NoSuchObjectException, PersistenceException
    {
        HashSet<OAuthToken>
            results = new HashSet<OAuthToken>();

        for (OAuthToken token : tokens.values())
        {
            if (s.equals(token.getUserId()))
                results.add(token);
        }

        return results;
    }

    public void deleteOAuthToken(String uuid)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthToken
            token = tokens.get(uuid);

        if (token == null)
        {
            throw new NoSuchObjectException();
        }

        tokensByProvider.remove(token.getProvider().getUUID());
    }

    public void deleteOAuthTokensForUser(String s)
        throws NoSuchObjectException, PersistenceException
    {
        HashSet<String>
            deleteUUIDs = new HashSet<String>();

        for (OAuthToken token : tokens.values())
        {
            if (s.equals (token.getUserId()))
            {
                deleteUUIDs.add(token.getUUID());
            }
        }

        for (String uuid : deleteUUIDs)
        {
            deleteOAuthToken(uuid);
        }
    }

    public void deleteOAuthTokensForProvider(String s)
        throws NoSuchObjectException, PersistenceException
    {
        OAuthProvider
            provider = providers.get(s);

        if (provider == null)
            throw new NoSuchObjectException ("No provider with uuid: " + s);

        HashSet<OAuthToken>
            providerTokens = tokensByProvider.get(s);

        if (providerTokens != null)
        {
            for (OAuthToken token : providerTokens)
            {
                tokens.remove(token.getUUID());
            }

            tokensByProvider.remove(s);
        }
    }
}
