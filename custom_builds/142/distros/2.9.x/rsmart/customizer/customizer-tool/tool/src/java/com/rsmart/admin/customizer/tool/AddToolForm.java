package com.rsmart.admin.customizer.tool;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 9, 2009
 * Time: 10:18:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddToolForm {
   
   private String siteType;
   private String siteTypeId;
   private List<String> addToolsToSites;
   private List<String> addToolsToType;
   private Map<String, Boolean> selectedTools;

   public String getSiteType() {
      return siteType;
   }

   public void setSiteType(String siteType) {
      this.siteType = siteType;
   }

   public List<String> getAddToolsToSites() {
      return addToolsToSites;
   }

   public void setAddToolsToSites(List<String> addToolsToSites) {
      this.addToolsToSites = addToolsToSites;
   }

   public List<String> getAddToolsToType() {
      return addToolsToType;
   }

   public void setAddToolsToType(List<String> addToolsToType) {
      this.addToolsToType = addToolsToType;
   }

   public Map<String, Boolean> getSelectedTools() {
      return selectedTools;
   }

   public void setSelectedTools(Map<String, Boolean> selectedTools) {
      this.selectedTools = selectedTools;
   }

   public String getSiteTypeId() {
      return siteTypeId;
   }

   public void setSiteTypeId(String siteTypeId) {
      this.siteTypeId = siteTypeId;
   }
}
