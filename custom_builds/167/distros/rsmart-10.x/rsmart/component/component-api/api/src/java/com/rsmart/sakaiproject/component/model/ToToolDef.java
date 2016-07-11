package com.rsmart.sakaiproject.component.model;

import org.sakaiproject.metaobj.shared.model.IdentifiableObject;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 6:01:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToToolDef extends IdentifiableObject {
   
   private String toolId;
   private ToCategory parentCategory;
   private boolean requiredTool = false;
   private boolean defaultTool = false;

   public String getToolId() {
      return toolId;
   }

   public void setToolId(String toolId) {
      this.toolId = toolId;
   }

   public boolean isRequiredTool() {
      return requiredTool;
   }

   public void setRequiredTool(boolean requiredTool) {
      this.requiredTool = requiredTool;
   }

   public boolean isDefaultTool() {
      return defaultTool;
   }

   public void setDefaultTool(boolean defaultTool) {
      this.defaultTool = defaultTool;
   }

   public ToCategory getParentCategory() {
      return parentCategory;
   }

   public void setParentCategory(ToCategory parentCategory) {
      this.parentCategory = parentCategory;
   }
}
