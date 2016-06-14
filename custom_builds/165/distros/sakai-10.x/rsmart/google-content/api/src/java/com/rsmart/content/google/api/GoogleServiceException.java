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
 * Represents an error which has occurred communicating with Google Docs.
 * 
 * User: duffy
 * Date: Mar 30, 2010
 * Time: 12:44:27 PM
 */
public class GoogleServiceException
    extends GoogleDocsException
{
    public GoogleServiceException()
    {
        super();
    }

    public GoogleServiceException (String message)
    {
        super(message);
    }

    public GoogleServiceException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public GoogleServiceException(Throwable t)
    {
        super(t);
    }
}
