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

package com.rsmart.oauth.hibernate;

import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.oauth.api.OAuthProvider;

/**
 * User: duffy
 * Date: Jan 20, 2010
 * Time: 3:31:42 PM
 */
public class OAuthTokenHibernateImpl
    implements OAuthToken
{

    private String
        uuid,
        userId,
        oAuthToken,
        oAuthTokenSecret;

    private OAuthProvider
        oAuthProvider;

    public void setUUID (String uuid)
    {
        this.uuid = uuid;
    }

    public String getUUID ()
    {
        return uuid;
    }
    
    public void setUserId (String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setTokenValue(String token)
    {
        oAuthToken = token;
    }

    public String getTokenValue()
    {
        return oAuthToken;
    }

    public void setTokenSecret (String secret)
    {
        oAuthTokenSecret = secret;
    }

    public String getTokenSecret()
    {
        return oAuthTokenSecret;
    }

    public void setProvider (OAuthProvider provider)
    {
        oAuthProvider = provider;
    }
    
    public OAuthProvider getProvider()
    {
        return oAuthProvider;
    }

    public boolean equals(Object o)
    {
        if (!OAuthTokenHibernateImpl.class.isAssignableFrom(o.getClass()))
            return false;

        final OAuthTokenHibernateImpl
            that = (OAuthTokenHibernateImpl)o;

        return (this == that || (uuid == null && that.uuid == null) || uuid.equals(that.uuid));
    }

    public int hashCode()
    {
        if (uuid == null)
            return super.hashCode();

        return uuid.hashCode();
    }

}
