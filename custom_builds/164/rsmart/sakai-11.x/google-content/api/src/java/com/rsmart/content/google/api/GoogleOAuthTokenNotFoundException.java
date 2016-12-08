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
 * Thrown when an OAuth token cannot be found for a user or document.
 * 
 * User: duffy
 * Date: Mar 24, 2010
 * Time: 2:09:21 PM
 */
public class GoogleOAuthTokenNotFoundException
    extends GoogleDocsException
{
    public GoogleOAuthTokenNotFoundException ()
    {
        super ();
    }

    public GoogleOAuthTokenNotFoundException (String message)
    {
        super (message);
    }

    public GoogleOAuthTokenNotFoundException (Exception e)
    {
        super (e);
    }

    public GoogleOAuthTokenNotFoundException (String message, Exception e)
    {
        super (message, e);
    }
}
