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

package com.rsmart.content.google.mock;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.exception.ServerOverloadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Jan 29, 2010
 * Time: 9:20:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockGoogleContentResource
    extends MockGoogleContentEntity
    implements ContentResource
{
    private static final Log
        LOG = LogFactory.getLog(MockGoogleContentResource.class);

    private final static String
        DATA = "This is a test file.";

    private byte
        content[] = null;

    protected String
        mimeType = "text/plain";

    public MockGoogleContentResource (MockGoogleContentHostingHandler chh,
                                      ContentCollection parent,
                                      String relativePath)
    {
        super (chh, parent, relativePath);
    }

    public MockGoogleContentResource (MockGoogleContentResourceEdit gcre)
    {
        super((MockGoogleContentHostingHandler)gcre.getContentHandler(),
              gcre.getParent(), gcre.getRelativePath());

        setValues(gcre);
    }

    public void setValues(MockGoogleContentResource gcr)
    {
        
    }

    public boolean isResource()
    {
        return true;
    }

    public boolean isCollection()
    {
        return false;
    }

    public String getResourceType() 
    {
        return "com.rsmart.content.google.type.GoogleDocumentType";
    }

    public int getContentLength()
    {
        return DATA.length();
    }

    public String getContentType()
    {
        return mimeType;
    }

    public byte[] getContent()
        throws ServerOverloadException
    {
        return DATA.getBytes();
    }

    public InputStream streamContent()
        throws ServerOverloadException
    {
        ByteArrayInputStream
            bais = new ByteArrayInputStream(DATA.getBytes());

        return bais;
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isAvailable() {
        return true;
    }
}
