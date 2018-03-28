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
 * Root exception type for Google Docs integration to Sakai. Use this to generically handle all Google related
 * exceptions. Otherwise, handle specific subclasses for particular exceptional events.
 * 
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 28, 2010
 * Time: 10:08:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleDocsException extends Exception
{
    public GoogleDocsException ()
    {
        super();
    }

    public GoogleDocsException (String msg)
    {
        super(msg);
    }

    public GoogleDocsException (Throwable t)
    {
        super(t);
    }

    public GoogleDocsException (String msg, Throwable t)
    {
        super(msg, t);
    }
}
