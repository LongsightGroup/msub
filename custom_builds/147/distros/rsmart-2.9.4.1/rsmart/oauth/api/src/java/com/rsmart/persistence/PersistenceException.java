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

package com.rsmart.persistence;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 1:01:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceException extends Exception
{
    public PersistenceException()
    {
        super();
    }

    public PersistenceException(String msg)
    {
        super(msg);
    }

    public PersistenceException(Throwable t)
    {
        super(t);
    }

    public PersistenceException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public String getMessage()
    {
        final String
            details = getDetails(),
            msg = super.getMessage();

        final StringBuilder
            sb = new StringBuilder();

        final boolean
            msgExists = (msg != null && msg.length() > 0);

        if (details != null && details.length() > 0)
        {
            sb.append(details);

            if (msgExists)
                sb.append(": ");
        }

        if (msgExists)
            sb.append(msg);

        return sb.toString();
    }

    public String getDetails()
    {
        return null;
    }
}
