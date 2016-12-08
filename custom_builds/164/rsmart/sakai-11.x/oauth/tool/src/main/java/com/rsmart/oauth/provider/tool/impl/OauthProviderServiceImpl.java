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

package com.rsmart.oauth.provider.tool.impl;

import com.rsmart.oauth.provider.tool.api.OAuthProviderService;
import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthSignatureMethod;
import com.rsmart.oauth.api.OAuthTokenService;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.persistence.PersistenceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * This class is the middleTier between the hibernateImpl and the front end
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Mar 31, 2010
 * Time: 10:48:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class OauthProviderServiceImpl
    implements OAuthProviderService
{

    private static final Log
        log = LogFactory.getLog(OauthProviderServiceImpl.class);

    private OAuthTokenService
        oAuthTokenService;

    public OAuthTokenService getOAuthTokenService()
    {
        return oAuthTokenService;
    }

    public void setOAuthTokenService(OAuthTokenService oAuthTokenService)
    {
        this.oAuthTokenService = oAuthTokenService;
    }


    public Set<OAuthProvider> getProviders()
        throws PersistenceException
    {
        return oAuthTokenService.getAllOAuthProviders();
    }

    public void addProvider (OAuthProvider provider , boolean oAuth2Enabled)
        throws PersistenceException
    {
        //To change body of implemented methods use File | Settings | File Templates.
        String providerName = provider.getProviderName();
        String description = provider.getDescription();
        boolean enabled = provider.isEnabled();
        Map parameters = provider.getAdditionalHeaders();

        if (oAuth2Enabled) {
            String authUrl = provider.getAuthUrl();
            String tokenUrl = provider.getTokenUrl();
            String clientId = provider.getClientId();
            String clientSecret = provider.getClientSecret();

            try {
                oAuthTokenService.createOAuthProvider(providerName, description, authUrl, tokenUrl, clientId, clientSecret, parameters, enabled);
            } catch (PersistenceException die) {
                log.error("Error in " + this.getClass().getName() + "in method: saveProviders)" + "Token didn't save successfully: " + die.getMessage());
            }

        } else {
            String requestTokenURL = provider.getRequestTokenURL();
            String userAuthorizationURL = provider.getUserAuthorizationURL();
            String accessTokenURL = provider.getAccessTokenURL();
            String realm = provider.getRealm();
            String consumerKey = provider.getConsumerKey();
            String hmacSha1SharedSecret = provider.getHmacSha1SharedSecret();
            String rsaSha1Key = provider.getRsaSha1Key();
            OAuthSignatureMethod signatureMethod = provider.getSignatureMethod();//OAuthSignatureMethod.HMAC_SHA1;

            try {
                oAuthTokenService.createOAuthProvider(providerName, description, requestTokenURL, userAuthorizationURL,
                        accessTokenURL, realm, consumerKey, hmacSha1SharedSecret,
                        rsaSha1Key, signatureMethod, parameters, enabled);
            } catch (PersistenceException die) {
                log.error("Error in " + this.getClass().getName() + "in method: saveProviders)" + "Token didn't save successfully: " + die.getMessage());
            }
        }
    }

    /**
     *  This method deletes the provider from the
     * TABLE: OAUTH_PROVIDER
     * @param uuid
     */
    public void deleteProvider(String uuid)
    {

        try
        {
            oAuthTokenService.deleteOAuthProvider(uuid);
        }
        catch (NoSuchObjectException oae)
        {
            log.error("Error in "+ this.getClass().getName() + "in method: deleteProviders)" + "Provider" + uuid +"uuid object doesn't exist: " + oae.getMessage() );
        }
        catch (PersistenceException pe)
        {
            log.error("Error in "+ this.getClass().getName() + "in method: deleteProviders)" + "Provider" + uuid + " Did not delete successfully: " + pe.getMessage() );    
        }
    }

    /**
     * This method deletes multiple providers by taking a List of Providers from the
     * TABLE: OAUTH_PROVIDER
     *
     * @param deleteProviders
     */
    public void deleteProviders(Set<String> deleteProviders)
    {

        try
        {
            oAuthTokenService.deleteOAuthProviders(deleteProviders);
        }
        catch (NoSuchObjectException oae)
        {
            log.error("Error in "+ this.getClass().getName() + "in method: deleteProviders)" + "Group Provider delete Failed: Objects Not Found: " + oae.getMessage() );
        }
        catch (PersistenceException pe)
        {
            log.error("Error in "+ this.getClass().getName() + "in method: deleteProviders)" + "Group Provider delete Failed: SqlException: " + pe.getMessage() );
        }
    }

    public OAuthProvider getProviderByUUID (String uuid)
        throws PersistenceException
    {
        return oAuthTokenService.getOAuthProvider(uuid);
    }

    public void updateOAuthProvider(OAuthProvider provider, boolean oAuth2Enabled)
        throws NoSuchObjectException, PersistenceException
    {
        try
        {
            String uuid         = provider.getUUID();
            String providerName = provider.getProviderName();
            String description = provider.getDescription();
            boolean enabled = provider.isEnabled();

            Map additionalParameters = provider.getAdditionalHeaders();

            if (oAuth2Enabled) {
                String authUrl = provider.getAuthUrl();
                String tokenUrl = provider.getTokenUrl();
                String clientId = provider.getClientId();
                String clientSecret = provider.getClientSecret();

                oAuthTokenService.updateOAuthProvider(uuid, providerName, description, authUrl, tokenUrl, clientId, clientSecret, additionalParameters, enabled);
            } else {
                String requestTokenURL=provider.getRequestTokenURL();
                String userAuthorizationURL=provider.getUserAuthorizationURL();
                String accessTokenURL=provider.getAccessTokenURL();
                String realm=provider.getRealm();
                String consumerKey=provider.getConsumerKey();
                String hmacSha1SharedSecret=provider.getHmacSha1SharedSecret();
                String rsaSha1Key=provider.getRsaSha1Key();
                OAuthSignatureMethod signingMethod = provider.getSignatureMethod();//OAuthSignatureMethod.HMAC_SHA1;

                oAuthTokenService.updateOAuthProvider(uuid, providerName, description, requestTokenURL,
                        userAuthorizationURL,accessTokenURL, realm, consumerKey,
                        hmacSha1SharedSecret, rsaSha1Key, signingMethod, additionalParameters,
                        enabled);

            }
        }
        catch (NoSuchObjectException nE)
        {
            log.info("Error in " + this.getClass().getName() + "in method: updateOAuthProvider" + "Provider ID Exception" + nE.getMessage() );

        }
        catch (PersistenceException pE)
        {
            log.info("Error in " + this.getClass().getName() + "in method: updateOAuthProvider Provider ID Exception" + pE.getMessage() );
        }
    }

    public boolean providerNameExists (String name)
    {
        boolean doesProviderNameExist = true;

        try{
            doesProviderNameExist = oAuthTokenService.providerExists(name);

        }catch (Exception e){
            log.info("Error in " + this.getClass().getName() + "In method checkIfProvidersNameExist" + e.getMessage());
        }

           return doesProviderNameExist;
    }


    public void removeAdditionalHeader (String uuid, String paramKey)
    {
        try
        {
            oAuthTokenService.removeAdditionalHeader(uuid, paramKey);
        }
        catch (PersistenceException e)
        {
            log.error ("Removing additional header parameter failed for provider: " + uuid + " and key: " + paramKey, e);
        }
    }
    
}