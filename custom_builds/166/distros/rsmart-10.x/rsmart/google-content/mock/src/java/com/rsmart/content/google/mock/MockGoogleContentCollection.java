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

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Collection;
import java.util.Stack;

/**
 * User: duffy
 * Date: Jan 29, 2010
 * Time: 9:18:58 AM
 */
public class MockGoogleContentCollection
    extends MockGoogleContentEntity
    implements ContentCollection
{
    private static final Log
        LOG = LogFactory.getLog(MockGoogleContentCollection.class);

    public MockGoogleContentCollection (MockGoogleContentHostingHandler chh,
                                        ContentCollection parent,
                                        String relativePath)
    {
        super(chh, parent, relativePath);
    }

    public MockGoogleContentCollection (MockGoogleContentCollectionEdit gcce)
    {
        super ((MockGoogleContentHostingHandler)gcce.getContentHandler(),
                gcce.getParent(), gcce.getRelativePath());

        setValues(gcce);
    }

    public void setValues (MockGoogleContentCollection gcc)
    {

    }

    public List getMembers() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getMemberResources() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getBodySizeK() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getMemberCount() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getGroups() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getGroupObjects() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AccessMode getAccess() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getInheritedGroups() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection getInheritedGroupObjects() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public AccessMode getInheritedAccess() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Time getReleaseDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Time getRetractDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isHidden() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAvailable() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentCollection getContainingCollection() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isResource() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isCollection() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getResourceType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentHostingHandler getContentHandler() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setContentHandler(ContentHostingHandler chh) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentEntity getVirtualContentEntity() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setVirtualContentEntity(ContentEntity ce) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ContentEntity getMember(String nextId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrl(boolean relative) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrl() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getReference() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUrl(String rootProperty) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getReference(String rootProperty) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceProperties getProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Element toXml(Document doc, Stack stack) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
