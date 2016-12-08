/*
 * Copyright 2008 The rSmart Group
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
 * Contributor(s): jbush
 */

package com.rsmart.sakaiproject.content.impl;

import org.theospi.portfolio.security.impl.AllowAllSecurityAdvisor;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.*;
import org.sakaiproject.event.cover.NotificationService;

import java.util.List;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: bbiltimier
 * Date: May 2, 2007
 * Time: 8:57:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContentLoaderService {
    private SecurityService securityService;
    private List resourceList;
    private ContentHostingService contentHosting;

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public List getResourceList() {
        return resourceList;
    }

    public void setResourceList(List resourceList) {
        this.resourceList = resourceList;
    }

    public ContentHostingService getContentHosting() {
        return contentHosting;
    }

    public void setContentHosting(ContentHostingService contentHosting) {
        this.contentHosting = contentHosting;
    }

    protected void init() {

        getSecurityService().pushAdvisor(new AllowAllSecurityAdvisor());

        org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();
        String userId = sakaiSession.getUserId();
        sakaiSession.setUserId("admin");
        sakaiSession.setUserEid("admin");

        try {
            for (Iterator i = getResourceList().iterator(); i.hasNext();) {
                ResourceBean resource = (ResourceBean) i.next();
                createResource(resource.getLocation(),
                        resource.getName(), resource.getDescription(), resource.getType(), resource.getDestinationFolder());
            }

        } finally {
            getSecurityService().popAdvisor();
            sakaiSession.setUserEid(userId);
            sakaiSession.setUserId(userId);
        }
    }

    protected void createResource(String resourceLocation,
                                  String name, String description, String type, String destination) {
        ByteArrayOutputStream bos = loadResource(resourceLocation);
        ResourcePropertiesEdit resourceProperties = getContentHosting().newResourceProperties();
        resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
        resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
        resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, "UTF-8");

        String folder = destination;


        try {
            ContentCollectionEdit collection = getContentHosting().addCollection(folder);
            collection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "system");
            getContentHosting().commitCollection(collection);

        }
        catch (IdUsedException e) {
            // ignore... it is already there.

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            String id = folder + name;
            ContentResource resource = getContentHosting().getResource(id);
        }
        catch (TypeException e) {
            // ignore, must be new
        }
        catch (IdUnusedException e) {
            // must be new  - add it
            try {
                getContentHosting().addResource(name, folder, 100, type,
                        bos.toByteArray(), resourceProperties, NotificationService.NOTI_NONE);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        catch (PermissionException e) {
            // ignore, must be new
        }


    }

    protected ByteArrayOutputStream loadResource(String name) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream is = getClass().getResourceAsStream(name);

        try {
            int c = is.read();
            while (c != -1) {
                bos.write(c);
                c = is.read();
            }
            bos.flush();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            }
            catch (IOException e) {
                //can't do anything now..
            }
        }
        return bos;
    }
}
