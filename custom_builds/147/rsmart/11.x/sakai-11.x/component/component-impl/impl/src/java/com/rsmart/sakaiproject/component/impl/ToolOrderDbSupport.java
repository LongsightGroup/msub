package com.rsmart.sakaiproject.component.impl;

import com.rsmart.sakaiproject.component.model.ToSiteType;
import com.rsmart.sakaiproject.component.model.ToCategory;
import com.rsmart.sakaiproject.component.model.ToToolDef;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.metaobj.shared.mgt.IdManager;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 5:37:40 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ToolOrderDbSupport extends HibernateDaoSupport {
    
   protected abstract IdManager idManager();

   public void clearDb() {
      getHibernateTemplate().deleteAll(getSiteTypes());
      getHibernateTemplate().flush();
   }
   
   public boolean hasCategories() {
      return getSiteTypes().size() != 0;
   }

   public List<ToSiteType> getSiteTypes() {
      return getHibernateTemplate().loadAll(ToSiteType.class);
   }
   
   public ToSiteType getSiteType(String id) {
      return (ToSiteType) getHibernateTemplate().load(ToSiteType.class, 
         idManager().getId(id));
   }
   
   public void saveSiteType(ToSiteType siteType) {
      getHibernateTemplate().saveOrUpdate(siteType);
   }

   public ToCategory getCategory(String id) {
      return (ToCategory) getHibernateTemplate().load(ToCategory.class, 
         idManager().getId(id));
   }

   public void saveCategory(ToCategory category) {
      getHibernateTemplate().saveOrUpdate(category);
   }
   
   public ToToolDef getTool(String id, String catId) {
      Collection col = getHibernateTemplate().find("from ToToolDef t where t.toolId = ? AND t.parentCategory.id = ?",
         new Object[]{id,idManager().getId(catId)});
      return (ToToolDef) col.iterator().next();
   }


   public void saveTool(ToToolDef tool) {
      getHibernateTemplate().saveOrUpdate(tool);
   }
}
