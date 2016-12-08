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

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Stack;

/**
 * User: duffy
 * Date: Jan 29, 2010
 * Time: 9:14:08 AM
 */
public abstract class MockGoogleContentEntity
    implements ContentEntity
{
    private static final Log
        LOG = LogFactory.getLog(MockGoogleContentEntity.class);

    private MockGoogleContentHostingHandler
        chh = null;

    private ContentCollection
        parent = null;

    private String
        relativePath = null,
        googleId = null;

    public MockGoogleContentEntity (MockGoogleContentHostingHandler chh,
                                    ContentCollection parent,
                                    String relativePath)
    {
        this.chh = chh;
        this.parent = parent;
        this.relativePath = relativePath;
    }

    public void setGoogleId (String id)
    {
        googleId = id;
    }

    public String getGoogleId ()
    {
        return googleId;
    }

    public ContentCollection getParent()
    {
        return parent;
    }

    public void setParent (ContentCollection p)
    {
        parent = p;
    }

    public String getRelativePath()
    {
        return parent.getUrl(true);
    }
    
    private String join(String base, String extension)
    { // joins two strings with precisely one / between them
        while (base.length() > 0 && base.charAt(base.length() - 1) == '/')
            base = base.substring(0, base.length() - 1);
        while (extension.length() > 0 && extension.charAt(0) == '/')
            extension = extension.substring(1);
        return base + "/" + extension;
    }

    public ContentCollection getContainingCollection()
    {
        LOG.debug (this.getClass().getName() + ".getContainingCollection()");

        return parent;
    }
 
    public ContentHostingHandler getContentHandler()
    {
        LOG.debug (this.getClass().getName() + ".getContentHandler()");
        return chh;
    }

    public void setContentHandler(ContentHostingHandler chh)
    {
        LOG.debug (this.getClass().getName() + ".setContentHandler(ContentHostingHandler)");
        //no-op, this should not be called
    }

    public ContentEntity getVirtualContentEntity()
    {
        LOG.debug (this.getClass().getName() + ".getVirtualContentEntity()");
        return this;
    }

    public void setVirtualContentEntity(ContentEntity ce)
    {
        LOG.debug (this.getClass().getName() + ".setVirtualContentEntity(ContentEntity)");
        //no-op
    }

    public String getUrl(boolean relative)
    {
        LOG.debug (this.getClass().getName() + ".getUrl(boolean)");

        return join(parent.getUrl(relative), relativePath);
    }

    public String getUrl()
    {
        LOG.debug (this.getClass().getName() + ".getUrl()");

        return getUrl(false);
    }

    public String getUrl(String rootProperty)
    {
        LOG.debug (this.getClass().getName() + ".getUrl(String)");

        return join(parent.getUrl(rootProperty), relativePath);
    }

    public Collection getGroups()
    {
        LOG.debug (this.getClass().getName() + ".getGroups()");

        return parent.getGroups();
    }

    public Collection getGroupObjects()
    {
        LOG.debug (this.getClass().getName() + ".getGroupObjects()");

        return parent.getGroupObjects();
    }

    public AccessMode getAccess()
    {
        LOG.debug (this.getClass().getName() + ".getAccess()");

        return parent.getAccess();
    }

    public Collection getInheritedGroups()
    {
        LOG.debug (this.getClass().getName() + ".getInheritedGroups()");

        return parent.getInheritedGroups();
    }

    public Collection getInheritedGroupObjects()
    {
        LOG.debug (this.getClass().getName() + ".getInheritedGroupObjects()");

        return parent.getInheritedGroupObjects();
    }

    public AccessMode getInheritedAccess()
    {
        LOG.debug (this.getClass().getName() + ".getInheritedAccess()");

        return parent.getInheritedAccess();
    }

    public String getReference()
    {
        LOG.debug (this.getClass().getName() + ".getReference()");

        return getUrl();
    }

    public String getReference(String rootProperty)
    {
        LOG.debug (this.getClass().getName() + ".getReference(String)");

        return getUrl(rootProperty);
    }

    public String getId()
    {
        LOG.debug (this.getClass().getName() + ".getId()");

        return getUrl();
    }

/**** WTF??? ****/
    public ContentEntity getMember(String nextId)
    {
        LOG.debug (this.getClass().getName() + ".getMember(String)");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Time getReleaseDate()
    {
        LOG.debug (this.getClass().getName() + ".getReleaseDate()");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Time getRetractDate() {
        LOG.debug (this.getClass().getName() + ".getRetractDate()");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceProperties getProperties()
    {
        LOG.debug (this.getClass().getName() + ".getProperties()");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Element toXml(Document doc, Stack stack)
    {
        LOG.debug (this.getClass().getName() + ".toXml(Document, Stack)");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
