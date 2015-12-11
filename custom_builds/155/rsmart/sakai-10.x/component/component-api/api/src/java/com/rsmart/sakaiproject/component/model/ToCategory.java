package com.rsmart.sakaiproject.component.model;

import org.sakaiproject.metaobj.shared.model.IdentifiableObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 5:59:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToCategory extends IdentifiableObject {
   
   private String name;
   private List<ToToolDef> tools;
   private boolean uncategorized = false;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ToToolDef> getTools() {
      return tools;
   }

   public void setTools(List<ToToolDef> tools) {
      this.tools = tools;
   }

   public boolean isUncategorized() {
      return uncategorized;
   }

   public void setUncategorized(boolean uncategorized) {
      this.uncategorized = uncategorized;
   }
}
