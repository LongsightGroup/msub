package com.rsmart.scorm;

import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.types.FileUploadType;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;

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

public class ScormContentType extends FileUploadType {
   protected String typeId = SCORM_TYPE;
   public static final String SCORM_TYPE = "scorm.type";
   public static final String SCORM_HELPER_ID = "rsmart.scorm.helper";

   private ResourceTypeRegistry resourceTypeRegistry;
   protected UserDirectoryService userDirectoryService;

   public ScormContentType(){
   }

   public void init(){
      super.typeId = "scorm.type";

      this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");

      actions.put(ResourceToolAction.CREATE, new ScormFileUploadCreateAction());
      //actions.put(ResourceToolAction.ACCESS_CONTENT, new FileUploadAccessAction());
      actions.put(ResourceToolAction.REVISE_CONTENT, new FileUploadReviseAction());
      actions.put(ResourceToolAction.REPLACE_CONTENT, new FileUploadReplaceAction());
      actions.put(ResourceToolAction.ACCESS_PROPERTIES, new FileUploadViewPropertiesAction());
      actions.put(ResourceToolAction.REVISE_METADATA, new FileUploadPropertiesAction());
      actions.put(ResourceToolAction.DUPLICATE, new FileUploadDuplicateAction());
      actions.put(ResourceToolAction.COPY, new FileUploadCopyAction());
      actions.put(ResourceToolAction.MOVE, new FileUploadMoveAction());
      actions.put(ResourceToolAction.DELETE, new FileUploadDeleteAction());
      // initialize actionMap with an empty List for each ActionType
      for(ResourceToolAction.ActionType type : ResourceToolAction.ActionType.values())
      {
         actionMap.put(type, new ArrayList<ResourceToolAction>());
      }

      // for each action in actions, add a link in actionMap
      Iterator<String> it = actions.keySet().iterator();
      while(it.hasNext())
      {
         String id = it.next();
         ResourceToolAction action = actions.get(id);
         List<ResourceToolAction> list = actionMap.get(action.getActionType());
         if(list == null)
         {
            list = new ArrayList<ResourceToolAction>();
            actionMap.put(action.getActionType(), list);
         }
         list.add(action);
      }

      if (ServerConfigurationService.getBoolean("enable.scorm", false)) {      
         getResourceTypeRegistry().register(this);
      }

   }

   // "property" categories: title (always required), description, copyright/licensing, access (groups, public), email-notification, availability
   public String getId() {
      return typeId;
   }

   public String getLabel() {
      return "scorm_type";
   }


   public ResourceTypeRegistry getResourceTypeRegistry() {
      return resourceTypeRegistry;
   }

   public void setResourceTypeRegistry(ResourceTypeRegistry resourceTypeRegistry) {
      this.resourceTypeRegistry = resourceTypeRegistry;
   }

   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   public class ScormFileUploadCreateAction extends FileUploadType.FileUploadCreateAction {

      public String getLabel()
      {
         return "Upload Content Package";
      }

      public String getHelperId() {
         return SCORM_HELPER_ID;
      }

      public List getRequiredPropertyKeys() {
         List keys = super.getRequiredPropertyKeys();
         keys.add(org.sakaiproject.content.api.ContentHostingService.PROP_ALTERNATE_REFERENCE);
         return keys;
      }

   }
}
