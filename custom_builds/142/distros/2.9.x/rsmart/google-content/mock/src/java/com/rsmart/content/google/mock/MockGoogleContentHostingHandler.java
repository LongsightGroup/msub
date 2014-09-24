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

import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.io.InputStream;

/**
 * User: duffy
 * Date: Jan 29, 2010
 * Time: 9:06:42 AM
 */
public class MockGoogleContentHostingHandler
    implements ContentHostingHandler
{
    private MockGoogleContentResource
        mockResource = null;

    private HashMap<String, MockGoogleContentEntity>
        openEdits = new HashMap<String, MockGoogleContentEntity>();

    private HashMap<String, MockGoogleContentEntity>
        entities = new HashMap<String, MockGoogleContentEntity>();

    private HashMap<String, List<MockGoogleContentEntity>>
        children = new HashMap<String, List<MockGoogleContentEntity>>();

    private static final Log
        LOG = LogFactory.getLog(MockGoogleContentHostingHandler.class);

    public MockGoogleContentHostingHandler()
    {
    }

    public void init()
        throws Exception
    {
    }

    public MockGoogleContentResource createNewResource (ContentCollection parent, String path)
    {
        return new MockGoogleContentResource (this, parent, path);
    }

    public void cancel(ContentCollectionEdit edit)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");
        openEdits.remove(edit.getId());
    }

    public void cancel(ContentResourceEdit edit)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentResourceEdit)");
        openEdits.remove(edit.getId());
    }

    private final void addChildren (MockGoogleContentCollection mgcc)
    {
        for (Object entObj : mgcc.getMembers())
        {
            MockGoogleContentEntity
                entity = (MockGoogleContentEntity) entObj;

            addChild(entity);
        }
    }

    private final void addChild (MockGoogleContentEntity mgce)
    {
        String
            parentId = mgce.getParent().getId();
        
        List<MockGoogleContentEntity>
            kids = children.get(parentId);

        if (kids == null)
        {
            children.put (parentId, kids);
        }

        kids.add(mgce);
    }

    private final void removeChild (String parentId, MockGoogleContentEntity mgce)
    {
        List<MockGoogleContentEntity>
            kids = getChildren(parentId);

        if (kids != null)
        {
            kids.remove(mgce);
        }
    }

    private final List<MockGoogleContentEntity> getChildren (String id)
    {
        return children.get(id);
    }

    public void commit(ContentCollectionEdit edit)
    {
        LOG.debug (this.getClass().getName() + ".commit(ContentCollectionEdit)");

        if (!openEdits.containsKey(edit.getId()))
            throw new IllegalStateException ("not a valid edit");

        MockGoogleContentCollectionEdit
            gcce = (MockGoogleContentCollectionEdit) edit;

        MockGoogleContentCollection
            coll = (MockGoogleContentCollection)entities.get(gcce.getId());

        if (coll == null)
        {
            coll = new MockGoogleContentCollection (gcce);

            entities.put(coll.getId(), coll);
            addChildren(coll);
        }
        else
        {
            coll.setValues(gcce);
        }

        openEdits.remove(edit.getId());
    }

    public void commit(ContentResourceEdit edit)
    {
        LOG.debug (this.getClass().getName() + ".commit(ContentResourceEdit)");

        if (!openEdits.containsKey(edit.getId()))
            throw new IllegalStateException ("not a valid edit");

        MockGoogleContentResourceEdit
            gcre = (MockGoogleContentResourceEdit)edit;

        MockGoogleContentResource
            res = (MockGoogleContentResource)entities.get(gcre.getId());

        if (res == null)
        {
            res = new MockGoogleContentResource (gcre);

            entities.put(res.getId(), res);
        }
        else
        {
            res.setValues(gcre);
        }

        openEdits.remove(edit.getId());
    }

    public void commitDeleted(ContentResourceEdit edit, String uuid)
    {
        LOG.debug (this.getClass().getName() + ".commitDeleted(ContentResourceEdit, String)");

        if (!openEdits.containsKey(edit.getId()))
            throw new IllegalStateException ("not a valid edit");

        MockGoogleContentResourceEdit
            mgcre = (MockGoogleContentResourceEdit) edit;

        String
           id = mgcre.getId();

        entities.remove(id);
        openEdits.remove(id);
    }

    public List getCollections(ContentCollection collection)
    {
        LOG.debug (this.getClass().getName() + ".getCollections(ContentCollection)");

        ArrayList<MockGoogleContentCollection>
            colls = new ArrayList<MockGoogleContentCollection>();

        for (Object collObj : getFlatResources(collection))
        {
            if (collObj instanceof MockGoogleContentCollection)
            {
                colls.add((MockGoogleContentCollection)collObj);
            }
        }

        return colls;
    }

    public ContentCollectionEdit getContentCollectionEdit(String id)
    {
        LOG.debug (this.getClass().getName() + ".getContentCollectionEdit(String)");

        MockGoogleContentCollectionEdit
            mgcce = null;

        MockGoogleContentEntity
            entity = entities.get(id);

        if (entity != null && entity instanceof MockGoogleContentCollection)
        {
            mgcce = new MockGoogleContentCollectionEdit((MockGoogleContentCollection)entity);

            openEdits.put(id, mgcce);
        }

        return mgcce;
    }

    public ContentResourceEdit getContentResourceEdit(String id)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        MockGoogleContentResourceEdit
            mgcre = null;

        MockGoogleContentEntity
            entity = entities.get(id);

        if (entity != null && entity instanceof MockGoogleContentResource)
        {
            mgcre = new MockGoogleContentResourceEdit((MockGoogleContentResource)entity);

            openEdits.put(id, mgcre);
        }

        return mgcre;
    }

    public List getFlatResources(ContentEntity ce)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        ArrayList<String>
            list = new ArrayList<String>();

        if (ce == null || !(ce instanceof MockGoogleContentCollection))
            return list;

        MockGoogleContentCollection
            coll = (MockGoogleContentCollection)ce;

        Stack <MockGoogleContentCollection>
            stack = new Stack<MockGoogleContentCollection> ();

        stack.push(coll);

        while (!stack.isEmpty())
        {
            coll = stack.pop();

            List<MockGoogleContentEntity>
                kids = children.get(coll.getId());

            for (MockGoogleContentEntity entity : kids)
            {
                list.add(entity.getId());

                if (entity instanceof MockGoogleContentCollection)
                {
                    stack.push((MockGoogleContentCollection)entity);
                }
            }
        }

        return list;
    }

    public byte[] getResourceBody(ContentResource resource)
        throws ServerOverloadException
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        return resource.getContent();
    }

    public List getResources(ContentCollection collection)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        List<MockGoogleContentEntity>
            kids = children.get(collection.getId());
        List<String>
            ids = new ArrayList<String>();

        for (MockGoogleContentEntity kid : kids)
        {
            ids.add(kid.getId());
        }

        return ids;
    }

    public ContentEntity getVirtualContentEntity(ContentEntity edit, String finalId)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        return edit.getVirtualContentEntity();
    }

    public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeCollection(ContentCollectionEdit edit)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        String
            id = edit.getId();

        if (!openEdits.containsKey(id))
        {
            throw new IllegalArgumentException("not a valid edit");
        }

        entities.remove(id);
        openEdits.remove(id);
    }

    public void removeResource(ContentResourceEdit edit) {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");
        String
            id = edit.getId();

        if (!openEdits.containsKey(id))
        {
            throw new IllegalArgumentException("not a valid edit");
        }

        entities.remove(id);
        openEdits.remove(id);
    }

    public InputStream streamResourceBody(ContentResource resource)
        throws ServerOverloadException
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        return resource.streamContent();
    }

    public int getMemberCount(ContentEntity ce)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");
        if (ce instanceof MockGoogleContentCollection)
        {
            MockGoogleContentCollection
                coll = (MockGoogleContentCollection)ce;

            return coll.getMemberCount();
        }

        return 0;
    }

    public Collection<String> getMemberCollectionIds(ContentEntity ce)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        if (ce instanceof MockGoogleContentCollection)
        {
            List<MockGoogleContentEntity>
                kids = children.get(ce.getId());

            List<String>
                ids = new ArrayList<String>();

            for (MockGoogleContentEntity kid : kids)
            {
                if (kid instanceof MockGoogleContentCollection)
                {
                    ids.add(kid.getId());
                }
            }

            return ids;
        }
        return null;
    }

    public Collection<String> getMemberResourceIds(ContentEntity ce)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        if (ce instanceof MockGoogleContentCollection)
        {
            List<MockGoogleContentEntity>
                kids = children.get(ce.getId());

            List<String>
                ids = new ArrayList<String>();

            for (MockGoogleContentEntity kid : kids)
            {
                if (kid instanceof MockGoogleContentResource)
                {
                    ids.add(kid.getId());
                }
            }

            return ids;
        }
        return null;
    }

    private String moveEntity (MockGoogleContentEntity entity, String new_id)
    {
        if (!openEdits.containsKey(entity.getId()))
            throw new IllegalStateException ("not a valid edit");

        if (!entities.containsKey(new_id))
            throw new IllegalStateException ("not a valid parent id");

        MockGoogleContentEntity
            mgce = (MockGoogleContentEntity)entity;

        ContentCollection
            parent = mgce.getParent();

        String
            parentId = parent.getId();

        if (parentId != null)
        {
            removeChild (parentId, mgce);
        }

        MockGoogleContentCollection
            newParent = (MockGoogleContentCollection) entities.get(new_id);
        
        mgce.setParent(newParent);

        addChild (mgce);

        openEdits.remove(mgce.getId());

        return null;
    }

    public String moveResource(ContentResourceEdit thisResource, String new_id)
    {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        return moveEntity((MockGoogleContentEntity)thisResource, new_id);
    }

    public String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");

        return moveEntity((MockGoogleContentEntity)thisCollection, new_folder_id);
    }

    public void setResourceUuid(String resourceId, String uuid) {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");
    }

    public void getUuid(String id) {
        LOG.debug (this.getClass().getName() + ".cancel(ContentCollectionEdit)");
    }
}
