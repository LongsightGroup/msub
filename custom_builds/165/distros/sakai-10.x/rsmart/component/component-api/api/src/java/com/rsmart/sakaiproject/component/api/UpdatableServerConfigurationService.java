package com.rsmart.sakaiproject.component.api;

import org.sakaiproject.component.api.ServerConfigurationService;

import java.io.InputStream;
import java.util.List;

import com.rsmart.sakaiproject.component.model.ToSiteType;
import com.rsmart.sakaiproject.component.model.ToToolDef;
import com.rsmart.sakaiproject.component.model.ToCategory;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 5:35:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UpdatableServerConfigurationService extends ServerConfigurationService {
   
   public void testUpdateTools(InputStream toolFile);
   
   public void updateTools(InputStream toolFile);
   
   public boolean isUseDb();
   
   public List<ToSiteType> getSiteTypes();
   
   public ToSiteType getSiteType(String id);
   
   public ToCategory getCategory(String id);
   
   public void saveSiteType(ToSiteType siteType);
   
   public void saveCategory(ToCategory category);

   public void saveNewCategory(String siteId, ToCategory category);
   
   public void moveTool(String fromCat, String toCat, String toolId);
}
