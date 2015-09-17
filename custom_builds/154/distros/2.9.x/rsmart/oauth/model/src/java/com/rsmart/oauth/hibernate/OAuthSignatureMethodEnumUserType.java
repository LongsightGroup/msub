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

package com.rsmart.oauth.hibernate;

import com.rsmart.oauth.api.OAuthSignatureMethod;
import org.sakaiproject.springframework.orm.hibernate.EnumUserType;

/**
 * This custom user type was created based on the EnumUserType class modelled at:
 *      http://community.jboss.org/wiki/UserTypeforpersistinganEnumwithaVARCHARcolumn
 * 
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 5:01:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthSignatureMethodEnumUserType extends EnumUserType<OAuthSignatureMethod>
{
    public OAuthSignatureMethodEnumUserType()
    {
        super(OAuthSignatureMethod.class);
    }
}
