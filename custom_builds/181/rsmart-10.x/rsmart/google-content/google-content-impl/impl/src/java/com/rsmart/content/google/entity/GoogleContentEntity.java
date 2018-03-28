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

package com.rsmart.content.google.entity;

import org.sakaiproject.content.api.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rsmart.content.google.api.GoogleDocsService;

/**
 * Represents a link to a Google Document wrapped to allow the ContentHostingService to manage and display the link.
 *
 * User: duffy
 * Date: Jan 14, 2010
 * Time: 10:03:16 AM
 */
public abstract class GoogleContentEntity
    implements ContentEntity
{
    private static final Log
        LOG = LogFactory.getLog(GoogleContentEntity.class);

    private GoogleDocsService
        docsService = null;

    /**
     * Accepts the underlying ContentResource from the ContentHostingService and a reference
     * to the GoogleDocsService used to resolve further information about the document. The
     * ContentResource must have an XML descriptor in its content which provides information
     * for connecting to Google to get and manage the document.
     *
     * @param svc
     */
    public GoogleContentEntity (GoogleDocsService svc)
    {
        this.docsService = svc;
    }

    protected GoogleDocsService getGoogleDocsService()
    {
        return docsService;
    }

}
