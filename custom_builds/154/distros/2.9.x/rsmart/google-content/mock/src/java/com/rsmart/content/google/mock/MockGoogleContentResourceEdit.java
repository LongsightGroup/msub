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

import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * User: duffy
 * Date: Jan 29, 2010
 * Time: 10:18:46 AM
 */
public class MockGoogleContentResourceEdit
    extends MockGoogleContentResource
    implements ContentResourceEdit
{
    private static final Log
        LOG = LogFactory.getLog (MockGoogleContentResourceEdit.class);

    private boolean
        activeEdit = false;

    BaseResourcePropertiesEdit
        propEdit = null;

    public MockGoogleContentResourceEdit(MockGoogleContentHostingHandler chh, MockGoogleContentCollection parent, String relativePath)
    {
        super(chh, parent, relativePath);
    }

    public MockGoogleContentResourceEdit(MockGoogleContentResource res)
    {
        super((MockGoogleContentHostingHandler) res.getContentHandler(), res.getParent(), res.getRelativePath());

        setValues(res);
    }

    public void setContentLength(int length)
    {
        LOG.error("should not be called");
    }

    public void setContentType(String type)
    {
        mimeType = type;
    }

    public void setContent(byte[] content)
    {
        LOG.error ("should not be called");
    }

    public void setContent(InputStream stream)
    {
        LOG.error ("should not be called");
    }

    public boolean isActiveEdit()
    {
        return activeEdit;
    }

    public ResourcePropertiesEdit getPropertiesEdit()
    {
        if (propEdit == null)
        {
            propEdit = new BaseResourcePropertiesEdit();

            return propEdit;
        }
        else return null;
    }

    public void clearGroupAccess()
        throws InconsistentException, PermissionException
    {
        
    }

    public void setGroupAccess(Collection groups)
        throws InconsistentException, PermissionException
    {
    }

    public void setPublicAccess()
        throws InconsistentException, PermissionException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearPublicAccess()
        throws InconsistentException, PermissionException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setReleaseDate(Time time)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRetractDate(Time time)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setHidden()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAvailability(boolean hidden, Time releaseDate, Time retractDate)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setResourceType(String string)
    {

    }
}
