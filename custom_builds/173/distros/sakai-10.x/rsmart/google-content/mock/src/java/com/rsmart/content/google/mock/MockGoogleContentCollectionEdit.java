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

import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;

import java.util.Map;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Feb 5, 2010
 * Time: 9:16:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockGoogleContentCollectionEdit
    extends MockGoogleContentCollection
    implements ContentCollectionEdit
{
    public MockGoogleContentCollectionEdit(MockGoogleContentHostingHandler chh,
                                           ContentCollection parent,
                                           String relativePath)
    {
        super(chh, parent, relativePath);
    }

    public MockGoogleContentCollectionEdit(MockGoogleContentCollection entity)
    {
        super((MockGoogleContentHostingHandler) entity.getContentHandler(), entity.getParent(), entity.getRelativePath());
        setValues(entity);
    }

    public void setPriorityMap(Map map) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isActiveEdit() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourcePropertiesEdit getPropertiesEdit() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearGroupAccess() throws InconsistentException, PermissionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setGroupAccess(Collection collection) throws InconsistentException, PermissionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setPublicAccess() throws InconsistentException, PermissionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearPublicAccess() throws InconsistentException, PermissionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setReleaseDate(Time time) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setRetractDate(Time time) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setHidden() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAvailability(boolean b, Time time, Time time1) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setResourceType(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
