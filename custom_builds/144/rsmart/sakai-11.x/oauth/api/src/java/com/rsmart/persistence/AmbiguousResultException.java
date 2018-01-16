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
 * Reports that more than one result was obtained where only one was expected.
 *
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 1:16:53 PM
 */
public class AmbiguousResultException
    extends ObjectKeyException
{
    private int
        resultCount = -1;

    public AmbiguousResultException()
    {
        super();
    }

    public AmbiguousResultException(String msg)
    {
        super(msg);
    }

    public AmbiguousResultException(Throwable t)
    {
        super(t);
    }

    public AmbiguousResultException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public void setResultCount (int ct)
    {
        resultCount = ct;
    }

    public String getDetails()
    {
        final StringBuilder
            sb = new StringBuilder("Ambiguous result obtained");
        final String
            keyDetails = getTypeAndKeysMessage();

        if (keyDetails != null && keyDetails.length() > 0)
        {
            sb.append (" [").append(keyDetails).append("]");
        }

        sb.append(".");
        if (resultCount > 1)
        {
            sb.append(resultCount).append(" items found");
        }

        return sb.toString();
    }
}
