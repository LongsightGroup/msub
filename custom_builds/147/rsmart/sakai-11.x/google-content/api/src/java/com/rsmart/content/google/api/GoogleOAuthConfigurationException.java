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

package com.rsmart.content.google.api;

/**
 * Represents an incorrect or missing configuration for the Google OAuth provider.
 * 
 * User: duffy
 * Date: Mar 24, 2010
 * Time: 2:14:59 PM
 */
public class GoogleOAuthConfigurationException
    extends GoogleDocsException
{
    public GoogleOAuthConfigurationException()
    {
        super ();
    }

    public GoogleOAuthConfigurationException(String message)
    {
        super (message);
    }

    public GoogleOAuthConfigurationException(Exception e)
    {
        super (e);
    }

    public GoogleOAuthConfigurationException(String message, Exception e)
    {
        super (message, e);
    }
}
