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
 * Reports an attempt to create a duplicate object in violation of a uniquity constraint.
 * 
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 1:33:05 PM
 */
public class DataIntegrityException
    extends ObjectKeyException
{
    public DataIntegrityException()
    {
        super();
    }

    public DataIntegrityException(String msg)
    {
        super(msg);
    }

    public DataIntegrityException(Throwable t)
    {
        super(t);
    }

    public DataIntegrityException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public String getDetails()
    {
        final String
            keyDetails = getTypeAndKeysMessage();

        if (keyDetails != null && keyDetails.length() > 0)
        {
            return new StringBuilder("Operation violates integrity constraintst [")
                        .append(keyDetails)
                        .append("]")
                        .toString();
        }

        return null;
    }
}
