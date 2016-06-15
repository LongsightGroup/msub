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

import com.shadedgoogle.api.client.http.GenericUrl;
import com.shadedgoogle.api.services.drive.model.File;
import com.rsmart.content.google.api.GoogleDocumentType;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.time.api.Time;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.Collection;

import com.rsmart.content.google.api.GoogleDocsService;
import com.rsmart.content.google.api.GoogleDocDescriptor;
import com.rsmart.content.google.api.GoogleDocsException;

/**
 * User: duffy
 * Date: Jan 14, 2010
 * Time: 11:05:49 AM
 */
public class GoogleContentResource
    extends GoogleContentEntity
    implements ContentResource
{
    private static final Log
        LOG = LogFactory.getLog (GoogleContentResource.class);

    private ContentResource
        resource = null;

    private GoogleDocDescriptor
        gdEntity = null;

    public GoogleContentResource (ContentResource cr, GoogleDocsService svc)
    {
        super (svc);

        this.resource = cr;

        // information about the document is stored as an XML document in the content of the
        //  underlying ContentResource
        processDescriptor();
    }

    /**
     * Parses the XML descriptor to get information about the underlying Google document.
     *
     * @throws GoogleDocsException
     */
    private final void processDescriptor()
    {

        byte
            xml[] = null;

        //get the XML descriptor
        try
        {
            xml = resource.getContent();
        }
        catch (Exception e)
        {
            gdEntity = new GoogleDocDescriptor();

            gdEntity.setTitle ("ERROR - Could not processe Google descriptor");

            LOG.error ("Failed to read XML descriptor for Google Doc: " + e.getMessage(), e);

            gdEntity.setProcessingException(new GoogleDocsException("Could not parse descriptor", e));

	        return;
        }

        try
        {
            gdEntity = getGoogleDocsService().xmlToDescriptor(new String(xml));
        }
        catch (GoogleDocsException gde)
        {
            gdEntity = new GoogleDocDescriptor();

            gdEntity.setTitle ("ERROR - Could not processe Google descriptor");

            LOG.error ("Failed to parse XML descriptor for Google Doc: " + gde.getMessage(), gde);

            gdEntity.setProcessingException(gde);
        }
    }

    private void verifyDescriptorProcessed()
    {
        if (gdEntity == null || !gdEntity.wasProcessedSuccessfully())
        {
            processDescriptor();
        }
    }

    public ContentEntity getMember(String nextId)
    {
        return null;
    }

    public long getContentLength()
    {
        verifyDescriptorProcessed();
        
        final GoogleDocsService
            google = getGoogleDocsService();

        final GoogleDocDescriptor
            entity = getGoogleDocsEntity();

        if (entity == null)
        {
            LOG.error ("No GoogleDocDescriptor stored in content");

            return -1;
        }

        try
        {
            return google.getDocumentLength(entity);
        }
        catch (GoogleDocsException e)
        {
            LOG.error ("Error retreiving document length", e);
            throw new RuntimeException ("Error retrieving document length", e);
        }
    }

    public String getContentType()
    {
        verifyDescriptorProcessed();

        final GoogleDocsService
            google = getGoogleDocsService();

        final GoogleDocDescriptor
            entity = getGoogleDocsEntity();

        if (entity == null)
        {
            LOG.error ("No GoogleDocDescriptor stored in content");

            //return a sensible default
            return "application/binary";
        }

        try
        {
            return google.getDocumentMimeType(entity);
        }
        catch (GoogleDocsException e)
        {
            LOG.error ("Error retreiving document MIME type", e);
        }

        return "application/binary";
    }

    public String getExportMimeType(){
        verifyDescriptorProcessed();

        final GoogleDocDescriptor entity = getGoogleDocsEntity();

        final GoogleDocsService google = getGoogleDocsService();

        if (entity == null) {
            LOG.error ("No GoogleDocDescriptor stored in content");

            //return a sensible default
            return "application/pdf";
        }

        return google.getExportMimeTypeForEntity(entity);
    }

    public byte[] getContent()
        throws ServerOverloadException
    {
        verifyDescriptorProcessed();

        byte
            data[] = null;
        long
            length = getContentLength();
        int
            tot = 0,
            bytesRead = 0,
            readLen = 2048;

        if (length > Integer.MAX_VALUE)
        {
            LOG.error ("content length is longer than maximum int size. Content cannot be buffered in an array. No appropriate exception can be thrown from getContent()");
        }

        if (length < 0)
        {
            data = new byte[2048];
        }
        else
        {
            data = new byte[(int)length];
        }

        InputStream
            stream = streamContent();

        if (stream != null)
        {
            try
            {
                while ((bytesRead = stream.read(data, tot - 1, readLen)) > 0)
                {
                    tot += bytesRead;

                    //resize array in case we did not get the actual length
                    if (tot + readLen > data.length)
                    {
                        byte
                            newData[] = new byte[data.length + readLen];

                        System.arraycopy(data, 0, newData, 0, data.length);

                        data = newData;
                    }
                }
            }
            catch (IOException e)
            {
                LOG.error ("Error reading content into byte array", e);
                return new byte[0];
            }
        }

        return data;
    }

    public InputStream streamContent()
        throws ServerOverloadException
    {
        verifyDescriptorProcessed();

        final GoogleDocsService
            google = getGoogleDocsService();

        final GoogleDocDescriptor
            entity = getGoogleDocsEntity();

        if (entity == null)
        {
            LOG.error ("No GoogleDocDescriptor stored in content");

            return null;
        }

        InputStream
            stream = null;

        try
        {
            stream = google.getDocumentInputStream(entity);
        }
        catch (GoogleDocsException e)
        {
            LOG.error ("Could not retrieve input stream for Google Document content", e);

            throw new RuntimeException ("Error fetching document stream", e);
        }

        return stream;
    }

    public InputStream streamDriveContent() throws ServerOverloadException {
        verifyDescriptorProcessed();

        final GoogleDocsService google = getGoogleDocsService();

        final GoogleDocDescriptor entity = getGoogleDocsEntity();

        if (entity == null) {
            LOG.error("No GoogleDocDescriptor stored in content");
            return null;
        }

        InputStream stream = null;

        try {
            File file = google.getDriveService(entity.getOwnerId()).files().get(entity.getDocId()).execute();

            if (file == null) {
                LOG.error("Could not find file in Google Drive with title: " + entity.getTitle());
                return null;
            }

            String downloadUrl;

            if (file.getDownloadUrl() != null) {
                downloadUrl = file.getDownloadUrl();
            } else {
                downloadUrl = google.getDriveService(entity.getOwnerId()).files().get(entity.getDocId()).execute().getExportLinks().get(getExportMimeType());
            }

            if (downloadUrl != null && !"".equals(downloadUrl)) {
                stream = google.getDriveService(entity.getOwnerId()).getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl)).execute().getContent();
            }

        } catch (IOException e) {
            LOG.error("Could not retrieve input stream for Google Document content", e);
            throw new RuntimeException("Error fetching document stream", e);
        }

        return stream;
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
        return GoogleDocumentType.TYPE_ID;
    }

    protected GoogleDocDescriptor getGoogleDocsEntity()
    {
        return gdEntity;
    }

    public String getUrl()
    {
       return resource.getUrl().replaceFirst("content","scorm");
    }

    public String getReference()
    {
       return resource.getReference();
    }

    public String getUrl(String rootProperty)
    {
       return resource.getUrl(rootProperty);
    }

    public String getReference(String rootProperty)
    {
       return resource.getReference(rootProperty);
    }

    public String getId()
    {
       return resource.getId();
    }

    public ResourceProperties getProperties()
    {
       return resource.getProperties();
    }

    public Element toXml(Document doc, Stack stack)
    {
       return resource.toXml(doc, stack);
    }

    public Collection getGroups()
    {
       return resource.getGroups();
    }

    public Collection getGroupObjects()
    {
       return resource.getGroupObjects();
    }

    public AccessMode getAccess()
    {
       return resource.getAccess();
    }

    public Collection getInheritedGroups()
    {
       return resource.getInheritedGroups();
    }

    public Collection getInheritedGroupObjects()
    {
       return resource.getInheritedGroupObjects();
    }

    public AccessMode getInheritedAccess()
    {
       return resource.getInheritedAccess();
    }

    public Time getReleaseDate()
    {
       return resource.getReleaseDate();
    }

    public Time getRetractDate()
    {
       return resource.getRetractDate();
    }

    public boolean isHidden()
    {
       return resource.isHidden();
    }

    public boolean isAvailable()
    {
       return resource.isAvailable();
    }

    public ContentCollection getContainingCollection()
    {
       return resource.getContainingCollection();
    }

    public ContentHostingHandler getContentHandler()
    {
       return resource.getContentHandler();
    }

    public void setContentHandler(ContentHostingHandler chh)
    {
       resource.setContentHandler(chh);
    }

    public ContentEntity getVirtualContentEntity()
    {
       return resource.getVirtualContentEntity();
    }

    public void setVirtualContentEntity(ContentEntity ce)
    {
       resource.setVirtualContentEntity(ce);
    }

    public String getUrl(boolean relative)
    {
       return resource.getUrl(relative);
    }

}
