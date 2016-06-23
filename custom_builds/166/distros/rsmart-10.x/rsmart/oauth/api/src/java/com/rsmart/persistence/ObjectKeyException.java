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
 * User: duffy
 * Date: Jan 21, 2010
 * Time: 2:04:49 PM
 */
public class ObjectKeyException
    extends PersistenceException
{
    private String
        objectType,
        keyTypes[];
    private Object
        keys[];

    public ObjectKeyException()
    {
        super();
    }

    public ObjectKeyException(String msg)
    {
        super(msg);
    }

    public ObjectKeyException(Throwable t)
    {
        super(t);
    }

    public ObjectKeyException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public void setObjectType (String t)
    {
        objectType = t;
    }

    public String getObjectType()
    {
        return objectType;
    }

    public void setKeyTypes(String kTypes[])
    {
        keyTypes = new String[kTypes.length];

        System.arraycopy (kTypes, 0, keyTypes, 0, kTypes.length);
    }

    public String[] getKeyTypes()
    {
        return keyTypes;
    }

    public void setKeys (Object k[])
    {
        keys = new Object[k.length];

        System.arraycopy(k, 0, keys, 0, k.length);
    }

    public String getTypeAndKeysMessage ()
    {
        final StringBuilder
            sb = new StringBuilder();

        if (objectType != null && objectType.length() > 0)
        {
            sb.append ("type: '").append(objectType).append("'");
        }

        if (keys != null)
        {
            String delim="";
            for (int i = 0; i < keys.length; i++)
            {
                sb.append(delim).append("'").append(keys[i]).append("'");

                if (keyTypes != null && i < keyTypes.length)
                {
                    sb.append(":").append(keyTypes[i]);
                }

                delim = ",";
            }
        }

        if (sb.length() > 0)
            return sb.toString();
        else
            return null;

    }
}
