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

package com.rsmart.oauth.provider.tool.api;

import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.persistence.PersistenceException;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Mar 31, 2010
 * Time: 1:43:45 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OAuthProviderService
{

    /**
     * This method deletes providers...example will be like "google"
     * @param uuid
     */
    public void deleteProvider(String uuid) throws PersistenceException, NoSuchObjectException;

    public Set<OAuthProvider> getProviders() throws PersistenceException;

    /**
     * This method deletes multiple providers by taking a List of Providers
     *
     *
     * @param deleteProviders
     */
    public void deleteProviders(Set<String> deleteProviders);

    public void addProvider(OAuthProvider provider) throws PersistenceException;
    
    /**
     *
     * @param uuid
     * @return
     * @throws PersistenceException
     */
    public OAuthProvider getProviderByUUID(String uuid) throws PersistenceException;

    /**
     * Update Auth Providers
     * @param provider
     * @throws NoSuchObjectException
     * @throws PersistenceException
     */
    public void updateOAuthProvider(OAuthProvider provider)
        throws NoSuchObjectException, PersistenceException;

    public boolean providerNameExists (String name);

    /**
     * Deletes additional header from the OAuth Provider.
     *
     * @param uuid
     * @param paramName
     * @return
     */
    public void removeAdditionalHeader (String uuid, String paramName);
}
