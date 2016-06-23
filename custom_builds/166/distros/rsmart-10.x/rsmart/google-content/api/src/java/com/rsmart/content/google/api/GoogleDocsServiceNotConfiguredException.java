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
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 6, 2011
 * Time: 9:08:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleDocsServiceNotConfiguredException
    extends GoogleDocsException
{
    public GoogleDocsServiceNotConfiguredException()
    {
        super();
    }

    public GoogleDocsServiceNotConfiguredException(String msg)
    {
        super(msg);
    }

    public GoogleDocsServiceNotConfiguredException(Throwable t)
    {
        super(t);
    }

    public GoogleDocsServiceNotConfiguredException(String msg, Throwable t)
    {
        super(msg, t);
    }
}