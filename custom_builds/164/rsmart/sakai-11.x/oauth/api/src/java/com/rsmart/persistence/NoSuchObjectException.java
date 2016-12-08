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
 * Exception to indicate that an Object could not be found with the given search keys.  A standard message will be
 * produced identifying the search inputs if setObjectSearchKeys and setObjectType are provided details of the query.
 *
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 1:03:11 PM
 */
public class NoSuchObjectException
    extends ObjectKeyException
{

    public NoSuchObjectException()
    {
        super();
    }

    public NoSuchObjectException(String msg)
    {
        super(msg);
    }

    public NoSuchObjectException(Throwable t)
    {
        super(t);
    }

    public NoSuchObjectException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public String getDetails()
    {
        final String
            keyDetails = getTypeAndKeysMessage();

        if (keyDetails != null)
        {
            final StringBuilder
                sb = new StringBuilder();

            sb.append("Object not found [").append(keyDetails).append("]");

            return sb.toString();
        }

        return null;
    }
}
