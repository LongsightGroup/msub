package com.rsmart.admin.customizer.tool;

import com.rsmart.sakaiproject.component.model.ToSiteType;
import com.rsmart.sakaiproject.component.model.ToToolDef;
import com.rsmart.sakaiproject.component.model.ToCategory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 30, 2008
 * Time: 10:37:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteTypeForm {
   
   ToSiteType siteType;
   List<ToolCategoryForm> toolCategories;
   String realm;
   
   
   boolean expanded;

/*
2. Move a tool to a new category within a selected site type
4. Rename a category within a site type
5. add a category within a site type
6. set function.require for a tool globally
7. add a tool to all sites of a selected type
*/


   public SiteTypeForm(ToSiteType siteType) {
      this.siteType = siteType;
      this.toolCategories = new ArrayList();

      for (int i=0;i<siteType.getCategories().size();i++) {
         ToCategory cat = siteType.getCategories().get(i);
         ToolCategoryForm tc = new ToolCategoryForm(cat);

         tc.setMoveUp(calculateMoveUp(siteType.getCategories(), i));
         tc.setMoveDown(i<siteType.getCategories().size() - 1);

         toolCategories.add(tc);
      }
   }

   protected boolean calculateMoveUp(List<ToCategory> categories, int i) {
      i--;
      while (i >= 0) {
         ToCategory prev = categories.get(i);
         if (!prev.isUncategorized()) {
            return true;
         }
         i--;
      }
      
      return false;
   }

   public SiteTypeForm() {
   }

   public ToSiteType getSiteType() {
      return siteType;
   }

   public void setSiteType(ToSiteType siteType) {
      this.siteType = siteType;
   }

   public List<ToolCategoryForm> getToolCategories() {
      return toolCategories;
   }

   public void setToolCategories(List<ToolCategoryForm> toolCategories) {
      this.toolCategories = toolCategories;
   }

   public boolean isExpanded() {
      return expanded;
   }

   public void setExpanded(boolean expanded) {
      this.expanded = expanded;
   }

   public String getRealm() {
      return realm;
   }

   public void setRealm(String realm) {
      this.realm = realm;
   }
   
   public boolean isRealmName() {
      return getRealm() != null && getRealm().length() > 0;
   }
}


