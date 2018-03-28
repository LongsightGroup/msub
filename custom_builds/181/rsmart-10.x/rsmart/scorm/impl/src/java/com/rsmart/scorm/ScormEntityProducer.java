package com.rsmart.scorm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.entity.cover.EntityManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
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

public class ScormEntityProducer implements EntityProducer {

   protected final Log logger = LogFactory.getLog(getClass());
   private boolean m_shortRefs = true;
   private boolean m_siteAlias = true;
   private static final String SCORM_LABEL = "scorm";
   public final static String REFERENCE_ROOT = Entity.SEPARATOR + SCORM_LABEL;
   private HttpAccess httpAccess;
   public void init() {
      logger.info("init()");
      try {
         EntityManager.registerEntityProducer(this, Entity.SEPARATOR + SCORM_LABEL);
      } catch (Exception e) {
         logger.warn("Error registering Scorm Entity Producer", e);
      }
   }

   public String getLabel() {
      return SCORM_LABEL;
   }

   public boolean willArchiveMerge() {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

	public boolean parseEntityReference(String reference, Reference ref)
	{
		String id = null;
		String context = "";

        if (reference.startsWith(REFERENCE_ROOT)) {
            // parse out the local resource id
            if (reference.startsWith(REFERENCE_ROOT + Entity.SEPARATOR + "attachment")) {
                id = reference.replaceFirst(REFERENCE_ROOT, "");
                ref.updateReference(reference);
                ref.set(REFERENCE_ROOT + Entity.SEPARATOR + "attachment", null, id, null, context);
            }
            else{
                id = reference.replaceFirst(REFERENCE_ROOT + Entity.SEPARATOR + "content", "");
                ref.set(REFERENCE_ROOT, null, id, null, context);
                ref.updateReference(REFERENCE_ROOT + id);
            }

        }

        // doesn't refer to scormcloud type
        else {
            return false;
        }



		return true;
	}

   public String getEntityDescription(Reference ref) {
      return "Scorm Content";  
   }

   public ResourceProperties getEntityResourceProperties(Reference ref) {
      ResourceProperties rp = getEntity(ref).getProperties();
      rp.addProperty(org.sakaiproject.content.api.ContentHostingService.PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
      return rp;
   }

   public Entity getEntity(Reference ref) {
      try {
         ContentResource rv = ContentHostingService.getResource(ref.getId());
         return new ScormEntity(rv);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public String getEntityUrl(Reference ref) {
      return ServerConfigurationService.getAccessUrl() + ref.getReference();
   }

   public Collection getEntityAuthzGroups(Reference ref, String userId) {
      return null;
   }

   public HttpAccess getHttpAccess() {
      return httpAccess;
   }

   public void setHttpAccess(HttpAccess httpAccess) {
      this.httpAccess = httpAccess;
   }
}
