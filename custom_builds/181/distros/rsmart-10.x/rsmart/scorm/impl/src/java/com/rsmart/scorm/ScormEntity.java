package com.rsmart.scorm;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.content.api.*;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Stack;
import java.util.Collection;/*
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

public class ScormEntity implements ContentEntity {
   private ContentResource resource;

   public ScormEntity(ContentResource resource){
      this.resource = resource;

   }
   public String getUrl() {
      return resource.getUrl().replaceFirst("content","scorm");
   }

   public String getReference() {
      return resource.getReference();
   }

   public String getUrl(String rootProperty) {
      return resource.getUrl(rootProperty);
   }

   public String getReference(String rootProperty) {
      return resource.getReference(rootProperty);
   }

   public String getId() {
      return resource.getId();
   }

   public ResourceProperties getProperties() {
      return resource.getProperties();
   }

   public Element toXml(Document doc, Stack stack) {
      return resource.toXml(doc, stack);
   }

   public Collection getGroups() {
      return resource.getGroups();
   }

   public Collection getGroupObjects() {
      return resource.getGroupObjects();
   }

   public AccessMode getAccess() {
      return resource.getAccess();
   }

   public Collection getInheritedGroups() {
      return resource.getInheritedGroups();
   }

   public Collection getInheritedGroupObjects() {
      return resource.getInheritedGroupObjects();
   }

   public AccessMode getInheritedAccess() {
      return resource.getInheritedAccess();
   }

   public Time getReleaseDate() {
      return resource.getReleaseDate();
   }

   public Time getRetractDate() {
      return resource.getRetractDate();
   }

   public boolean isHidden() {
      return resource.isHidden();
   }

   public boolean isAvailable() {
      return resource.isAvailable();
   }

   public ContentCollection getContainingCollection() {
      return resource.getContainingCollection();
   }

   public boolean isResource() {
      return resource.isResource();
   }

   public boolean isCollection() {
      return resource.isCollection();
   }

   public String getResourceType() {
      return resource.getResourceType();
   }

   public ContentHostingHandler getContentHandler() {
      return resource.getContentHandler();
   }

   public void setContentHandler(ContentHostingHandler chh) {
      resource.setContentHandler(chh);
   }

   public ContentEntity getVirtualContentEntity() {
      return resource.getVirtualContentEntity();
   }

   public void setVirtualContentEntity(ContentEntity ce) {
      resource.setVirtualContentEntity(ce);
   }

   public ContentEntity getMember(String nextId) {
      return resource.getMember(nextId);
   }

   public String getUrl(boolean relative) {
      return resource.getUrl(relative);
   }
}
