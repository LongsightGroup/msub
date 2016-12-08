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

import org.sakaiproject.entity.api.Entity;

/**
 * User: duffy
 * Date: Mar 2, 2010
 * Time: 2:49:20 PM
 */
public interface GoogleDocumentType
{
    public static String
        TYPE_ID             = "com.rsmart.content.google.api.GoogleDocumentType",
        REFERENCE_ROOT      = Entity.SEPARATOR + "google";
}
