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

package com.rsmart.sakaiproject.tool.api;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.metaobj.shared.model.IdentifiableObject;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Mar 27, 2007
 * Time: 11:47:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbTool extends IdentifiableObject {

   private String toolId;
   private String title;
   private String description;
   private String home;
   private String accessSecurity;
   private Set categories;
   private Set keywords;
   private Map mutableConfig;
   private Map finalConfig;

   private static final String ACCESS_SECURITY_PORTAL = "portal";
   private static final String ACCESS_SECURITY_TOOL = "tool";


   public DbTool() {

   }

   public DbTool(Tool tool) {
      copyFrom(tool);
   }

   public void copyFrom(Tool tool) {
      setToolId(tool.getId());
      setTitle(tool.getTitle());
      setDescription(tool.getDescription());
      setHome(tool.getHome());
      setAccessSecurity(tool.getAccessSecurity());

      setCategories(copySet(getCategories(), tool.getCategories()));
      setKeywords(copySet(getKeywords(), tool.getKeywords()));
      setMutableConfig(copyMap(getMutableConfig(), tool.getMutableConfig()));
      setFinalConfig(copyMap(getFinalConfig(), tool.getFinalConfig()));
   }

   protected Map copyMap(Map to, Properties from) {
      if (to != null) {
        List toRemove = new ArrayList();
        toRemove.addAll(to.keySet());                     

        for (Map.Entry entry : from.entrySet()) {
           to.put(entry.getKey(), entry.getValue());
           toRemove.remove(entry.getKey());
        }

        for (Object key : toRemove) {
           to.remove(key);
        }
     }
     else {
        to = new HashMap(from);
     }

      return to;
   }

   protected Set copySet(Set to, Set from) {
      if (to != null) {
         to.clear();
         to.addAll(from);
      }
      else {
         to = new HashSet(from);
      }

      return to;
   }

   public String getToolId() {
      return toolId;
   }

   public void setToolId(String toolId) {
      this.toolId = toolId;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getAccessSecurity() {
      return accessSecurity;
   }

   public void setAccessSecurity(String accessSecurity) {
      this.accessSecurity = accessSecurity;
   }

   public Set getCategories() {
      return categories;
   }

   public void setCategories(Set categories) {
      this.categories = categories;
   }

   public Set getKeywords() {
      return keywords;
   }

   public void setKeywords(Set keywords) {
      this.keywords = keywords;
   }

   public Map getMutableConfig() {
      return mutableConfig;
   }

   public void setMutableConfig(Map mutableConfig) {
      this.mutableConfig = mutableConfig;
   }

   public Map getFinalConfig() {
      return finalConfig;
   }

   public void setFinalConfig(Map finalConfig) {
      this.finalConfig = finalConfig;
   }

   public String getHome() {
      return home;
   }

   public void setHome(String home) {
      this.home = home;
   }

   public void setAccessSecurity(Tool.AccessSecurity accessSecurity) {
      if (Tool.AccessSecurity.PORTAL == accessSecurity) {
         setAccessSecurity(ACCESS_SECURITY_PORTAL);
      }
      else if (Tool.AccessSecurity.TOOL == accessSecurity) {
         setAccessSecurity(ACCESS_SECURITY_TOOL);
      }
   }

   public Tool.AccessSecurity getAccessSecurityObject() {
      if (ACCESS_SECURITY_PORTAL.equals(getAccessSecurity())) {
         return Tool.AccessSecurity.PORTAL;
      }
      else if (ACCESS_SECURITY_TOOL.equals(getAccessSecurity())) {
         return Tool.AccessSecurity.TOOL;
      }

      return Tool.AccessSecurity.PORTAL;
   }

}
