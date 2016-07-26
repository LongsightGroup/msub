package com.rsmart.sakaiproject.component.impl;

import com.rsmart.sakaiproject.component.api.UpdatableServerConfigurationService;
import com.rsmart.sakaiproject.component.model.ToCategory;
import com.rsmart.sakaiproject.component.model.ToSiteType;
import com.rsmart.sakaiproject.component.model.ToToolDef;
import org.sakaiproject.component.impl.BasicConfigurationService;

import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 5:36:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DbConfigurationService extends BasicConfigurationService 
   implements UpdatableServerConfigurationService {
   
   private boolean useDb = true;
   
   /**
    * @return the DbActiveToolDbSupport collaborator.
    */
   protected abstract ToolOrderDbSupport dbSupport();


   public void testUpdateTools(InputStream toolFile) {
      try {
         clearToolOrder();
         super.loadToolOrder(toolFile);
      }
      finally {
         reloadFromDb();
      }
   }

   public void updateTools(InputStream toolFile) {
      dbSupport().clearDb();
      initToolOrder(toolFile);
   }


   /**
    * Load this single file as a registration file, loading tools and locks.
    *
    * @param in The Stream to load
    */
   protected void loadToolOrder(InputStream in) {
      if (!isUseDb()) {
         super.loadToolOrder(in);
         return;
      }

      if (dbSupport().hasCategories()) {
         // load from db here
         reloadFromDb();
      } else {
         initToolOrder(in);   
      }
   }

   protected void reloadFromDb() {
      clearToolOrder();

      List<ToSiteType> siteTypes = dbSupport().getSiteTypes();
      for (ToSiteType siteType : siteTypes) {
         loadDbSiteType(siteType);
      }
   }

   protected void clearToolOrder() {
      m_toolOrders.clear();
      m_toolsRequired.clear();
      m_defaultTools.clear();
      m_toolCategoriesList.clear();
      m_toolCategoriesMap.clear();
      m_toolToToolCategoriesMap.clear();
   }

   protected void loadDbSiteType(ToSiteType toSiteType) {
      String name = toSiteType.getName();
      List<String> order = new Vector<String>();
      m_toolOrders.put(name, order);
      
      List required = new Vector();
      m_toolsRequired.put(name, required);
      List defaultTools = new Vector();
      m_defaultTools.put(name, defaultTools);
      
      List<String> toolCategories = new Vector();
      m_toolCategoriesList.put(name, toolCategories);
      
      Map<String, List<String>> toolCategoryMappings = new HashMap();
      m_toolCategoriesMap.put(name, toolCategoryMappings);
      
      Map<String, String> toolToCategoryMap = new HashMap();
      m_toolToToolCategoriesMap.put(name, toolToCategoryMap);
      
      for (ToCategory category : toSiteType.getCategories()) {
         List<String> tools = new ArrayList<String>();
         
         if (!category.isUncategorized()) {
            toolCategories.add(category.getName());
            toolCategoryMappings.put(category.getName(), tools);
         }
         
         for (ToToolDef tool : category.getTools()) {
             if (tool != null) {
                if (!category.isUncategorized()) {
                   toolToCategoryMap.put(tool.getToolId(), category.getName());
                }
                order.add(tool.getToolId());
                if (tool.isDefaultTool()) {
                   defaultTools.add(tool.getToolId());
                }
                if (tool.isRequiredTool()) {
                   required.add(tool.getToolId());
                }
                tools.add(tool.getToolId());
             }
         }
      }
   }

   protected void initToolOrder(InputStream in) {
      clearToolOrder();
      super.loadToolOrder(in);
      // save to db here
      for (Iterator<String> i=m_toolOrders.keySet().iterator();i.hasNext();) {
         processSiteType(i.next());   
      }
   }

   protected void processSiteType(String siteTypeKey) {
      ToSiteType siteType = new ToSiteType();
      siteType.setName(siteTypeKey);
      siteType.setCategories(new ArrayList<ToCategory>());
      
      List<String> toolIds = getToolOrder(siteTypeKey);
      
      for (Iterator<String> i=toolIds.iterator();i.hasNext();) {
         processToolId(i.next(), siteType);   
      }
      dbSupport().saveSiteType(siteType);
   }

   protected void processToolId(String toolId, ToSiteType siteType) {
      Map<String, String> toolCategories = getToolToCategoryMap(siteType.getName());
      ToToolDef toolDef = createTool(toolId, siteType.getName());
      
      int lastIndex = siteType.getCategories().size() - 1;
      
      ToCategory lastCategory = null;

      if (lastIndex >= 0) {
         lastCategory = siteType.getCategories().get(lastIndex);
      }
      
      // does it have a category?
      String category = toolCategories.get(toolId);
      if (category == null) {
         ToCategory newCategory = createCategory(siteType, null);
         newCategory.getTools().add(toolDef);
      }
      else if (lastCategory != null && !lastCategory.isUncategorized() && 
         category.equals(lastCategory.getName())) {
         lastCategory.getTools().add(toolDef);   
      }
      else {
         ToCategory newCategory = createCategory(siteType, category);
         newCategory.getTools().add(toolDef);   
      }
   }

   protected ToToolDef createTool(String toolId, String siteType) {
      ToToolDef tool = new ToToolDef();
      tool.setToolId(toolId);
      tool.setDefaultTool(getDefaultTools(siteType).contains(toolId));
      tool.setRequiredTool(getToolsRequired(siteType).contains(toolId));
      return tool;
   }

   protected ToCategory createCategory(ToSiteType siteType, String category) {
      ToCategory newCategory = new ToCategory();
      if (category == null) {
         newCategory.setUncategorized(true);
      } else {
         newCategory.setName(category);
         newCategory.setUncategorized(false);
      }

      newCategory.setTools(new ArrayList<ToToolDef>());
      siteType.getCategories().add(newCategory);
      return newCategory;
   }

   public void saveNewCategory(String siteId, ToCategory category) {
      ToSiteType type = dbSupport().getSiteType(siteId);
      createCategory(type, category.getName());
      dbSupport().saveSiteType(type);
   }

   public void moveTool(String fromCat, String toCat, String toolId) {
      ToToolDef oldTool = null;
      if (fromCat != null && fromCat.length() > 0) {
         oldTool = dbSupport().getTool(toolId, fromCat);
         ToCategory from = getCategory(fromCat);
         boolean f = from.getTools().remove(oldTool);
         dbSupport().saveCategory(from);
         dbSupport().getHibernateTemplate().delete(oldTool);
      }

      ToToolDef tool = new ToToolDef();
      tool.setToolId(toolId);
      
      if (oldTool != null) {
         tool.setRequiredTool(oldTool.isRequiredTool());   
         tool.setDefaultTool(oldTool.isDefaultTool());   
      }
      
      ToCategory cat = getCategory(toCat);
      
      cat.getTools().add(tool);
      saveCategory(cat);
      dbSupport().getHibernateTemplate().flush();
   }

   public boolean isUseDb() {
      return useDb;
   }

   public void setUseDb(boolean useDb) {
      this.useDb = useDb;
   }
   
   public List<ToSiteType> getSiteTypes() {
      List<ToSiteType> types = dbSupport().getSiteTypes();
      
      for (ToSiteType type : types) {
         for (ToCategory cat : type.getCategories()) {
            cat.getTools().size(); // lazy load them
         }
      }
      
      return types;
   }

   public ToSiteType getSiteType(String id) {
      return dbSupport().getSiteType(id);
   }

   public ToCategory getCategory(String id) {
      return dbSupport().getCategory(id); 
   }

   public void saveSiteType(ToSiteType siteType) {
      dbSupport().saveSiteType(siteType);
      reloadFromDb();
   }

   public void saveCategory(ToCategory category) {
      dbSupport().saveCategory(category);
      reloadFromDb();
   }
}
