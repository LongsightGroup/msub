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

/**
 * Contains the configuration, authorization and identity information required to manage and retrieve
 * a particular document from Google Docs.
 *
 * User: duffy
 * Date: Feb 25, 2010
 * Time: 12:50:02 PM
 */
public class GoogleDocDescriptor
{    
    private String
        docId,
        title,
        type,
        exportType,
        link,
        ownerId,
        versionId;
    private int
        worksheetCount = 0,
        worksheet = -1;
    private GoogleDocsException
        processingException = null;

    public boolean wasProcessedSuccessfully()
    {
        return (processingException == null);
    }

    public GoogleDocsException getProcessingException()
    {
        return processingException;
    }

    public void setProcessingException (GoogleDocsException gde)
    {
        processingException = gde;
    }

    public void clearProcessingException ()
    {
        processingException = null;
    }
    
    public String getVersionId()
    {
        return versionId;
    }

    public void setVersionId(String versionId)
    {
        this.versionId = versionId;
    }

    public int getWorksheet()
    {
        return worksheet;
    }

    public void setWorksheet(int worksheet)
    {
        this.worksheet = worksheet;
    }

    public int getWorksheetCount()
    {
        return worksheetCount;
    }

    public void setWorksheetCount(int worksheetCount)
    {
        this.worksheetCount = worksheetCount;
    }

    public void setDocId (String id)
    {
        docId = id;
    }

    public String getDocId()
    {
        return docId;
    }

    public String getResourceId()
    {
        return type + ":" + docId;
    }
    
    public void setTitle (String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setLink (String link)
    {
        this.link = link;
    }

    public String getLink()
    {
        return link;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public String getOwnerId()
    {
        return ownerId;
    }
    
    public boolean isFolder()
    {
        return "application/vnd.google-apps.folder".equals(type) || "folder".equals(type);
    }

    public void setExportType (String exportType)
    {
        this.exportType = exportType;
    }

    public String getExportType ()
    {
        return exportType;
    }
}
