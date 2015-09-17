package com.rsmart.admin.customizer.tool;

import java.util.Map;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 5, 2009
 * Time: 12:53:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SiteTypeListForm {
   
   private String siteTypeExpanded;
   private String categoryExpanded;
   private String movingToolId;
   private String renamingCatId;
   private String catToolMovedTo;
   private String catToolMovedFrom;
   private String newCategoryName;

   public String getSiteTypeExpanded() {
      return siteTypeExpanded;
   }

   public void setSiteTypeExpanded(String siteTypeExpanded) {
      this.siteTypeExpanded = siteTypeExpanded;
   }

   public String getCategoryExpanded() {
      return categoryExpanded;
   }

   public void setCategoryExpanded(String categoryExpanded) {
      this.categoryExpanded = categoryExpanded;
   }

   public String getMovingToolId() {
      return movingToolId;
   }

   public void setMovingToolId(String movingToolId) {
      this.movingToolId = movingToolId;
   }

   public String getCatToolMovedTo() {
      return catToolMovedTo;
   }

   public void setCatToolMovedTo(String catToolMovedTo) {
      this.catToolMovedTo = catToolMovedTo;
   }

   public String getCatToolMovedFrom() {
      return catToolMovedFrom;
   }

   public void setCatToolMovedFrom(String catToolMovedFrom) {
      this.catToolMovedFrom = catToolMovedFrom;
   }

   public Map getModel() {
      Map returned = new Hashtable();
      
      addToMap(returned, "catToolMovedTo", getCatToolMovedTo());
      addToMap(returned, "catToolMovedFrom", getCatToolMovedFrom());
      addToMap(returned, "siteTypeExpanded", getSiteTypeExpanded());
      addToMap(returned, "categoryExpanded", getCategoryExpanded());
      addToMap(returned, "movingToolId", getMovingToolId());
      addToMap(returned, "renamingCatId", getRenamingCatId());
      addToMap(returned, "newCategoryName", getNewCategoryName());
      
      return returned;
   }

   public String getRenamingCatId() {
      return renamingCatId;
   }

   public void setRenamingCatId(String renamingCatId) {
      this.renamingCatId = renamingCatId;
   }

   public String getNewCategoryName() {
      return newCategoryName;
   }

   public void setNewCategoryName(String newCategoryName) {
      this.newCategoryName = newCategoryName;
   }

   protected void addToMap(Map model, String key, String value) {
      if (value != null && value.length() > 0) {
         model.put(key, value);
      }
   }
   
}
