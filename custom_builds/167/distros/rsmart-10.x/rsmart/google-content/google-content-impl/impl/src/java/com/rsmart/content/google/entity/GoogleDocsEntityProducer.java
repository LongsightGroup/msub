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

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.impl.EntityManagerComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Stack;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.rsmart.content.google.api.GoogleDocsService;

/**
 * User: duffy
 * Date: Mar 3, 2010
 * Time: 9:29:55 AM
 */
public class GoogleDocsEntityProducer
    implements EntityProducer
{
    private static final Log
        LOG = LogFactory.getLog(GoogleDocsEntityProducer.class);
    private static final String
        GOOGLE_LABEL                = "google";
    public static final String
        REFERENCE_ROOT              = Entity.SEPARATOR + GOOGLE_LABEL;
    private HttpAccess
        httpAccess = null;
    private GoogleDocsService
        google = null;

    public void init()
    {
        LOG.info("init()");

        try
        {
            EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
        }
        catch (Exception e)
        {
            LOG.error("Google Docs Entity Producer not registered!", e);
        }
    }

    public void setGoogleDocsService (GoogleDocsService svc)
    {
        google = svc;
    }

    public GoogleDocsService getGoogleDocsService ()
    {
        return google;
    }
    
    public String getLabel()
    {
        return GOOGLE_LABEL;
    }

    public boolean willArchiveMerge()
    {
        return false;
    }

    public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
    {
        return null;
    }

    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
    {
        return null;
    }

    public boolean parseEntityReference(String reference, Reference ref)
    {
        String id = null;
        String context = "";

        // for content hosting resources and collections
        if (reference.startsWith(REFERENCE_ROOT + Entity.SEPARATOR + "content"))
        {
            LOG.debug ("reference passed to GoogleDocsEntityProducer.parseEntityReference: " + reference);

            // parse out our label
            id = reference.replaceFirst(REFERENCE_ROOT, "");

            // need to call parseEntityReference on the content producer to mangle the reference appropriately
            //   to handle aliases - like converting /user/user1 to /user/<UID>
            org.sakaiproject.entity.api.EntityManager
                eMgr = EntityManager.getInstance();
            EntityProducer
                contentProducer = null;

            try
            {
                contentProducer = ((EntityManagerComponent)eMgr).getEntityProducer(reference, ref);
            }
            catch (ClassCastException cce)
            {
                List<EntityProducer>
                    eProds = eMgr.getEntityProducers();

                for (EntityProducer prod : eProds)
                {
                    if ("content".equals(prod.getLabel()))
                    {
                        contentProducer = prod;
                        break;
                    }
                }
            }

            if (contentProducer == null)
            {
                LOG.error ("could not find underlying EntityProducer - unable to ensure aliases in reference are properly parsed");
            }
            else
            {
                if (contentProducer.parseEntityReference(id, ref))
                {
                    LOG.debug ("underlying content entity producer altered reference to: " + id);
                }
                else
                {
                    LOG.error ("underlying content entity producer does not recognize reference");
                }
            }

            //now convert to the local reference
            id = reference.replaceFirst(Entity.SEPARATOR + "content", "");

            LOG.debug ("local reference for resoure is: " + id);
        }

        // not mine
        else
        {
            return false;
        }

        ref.set(REFERENCE_ROOT, null, id, null, context);

        // because short refs or id/eid or alias processing may recognize a reference that is not the real reference,
        // update the ref's string to reflect the real reference
//        ref.updateReference(REFERENCE_ROOT + Entity.SEPARATOR + "content" + Entity.SEPARATOR + id);

        return true;
    }

    public String getEntityDescription(Reference ref)
    {
        return "Google Document";
    }

    public ResourceProperties getEntityResourceProperties(Reference ref)
    {
        ResourceProperties
            rp = getEntity(ref).getProperties();

        rp.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);

        return rp;
    }

    public Entity getEntity(Reference ref)
    {
        try
        {
            ContentResource
               rv = org.sakaiproject.content.cover.ContentHostingService.getResource(ref.getId());

            return new GoogleContentResource(rv, getGoogleDocsService());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public String getEntityUrl(Reference ref)
    {
        return ServerConfigurationService.getAccessUrl() +
                ref.getReference();
    }

    public Collection getEntityAuthzGroups(Reference ref, String userId)
    {
        return null;
    }

    public HttpAccess getHttpAccess()
    {
        return httpAccess;
    }

    public void setHttpAccess(HttpAccess httpAccess)
    {
        this.httpAccess = httpAccess;
    }
}
