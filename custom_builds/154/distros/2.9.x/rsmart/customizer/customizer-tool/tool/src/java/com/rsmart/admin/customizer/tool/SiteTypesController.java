package com.rsmart.admin.customizer.tool;

import org.sakaiproject.metaobj.utils.mvc.impl.servlet.AbstractFormController;
import org.sakaiproject.metaobj.utils.mvc.intf.LoadObjectController;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Tool;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import com.rsmart.admin.customizer.api.CustomizerService;
import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;
import com.rsmart.sakaiproject.component.model.ToSiteType;
import com.rsmart.sakaiproject.component.model.ToToolDef;
import com.rsmart.sakaiproject.component.model.ToCategory;
import com.rsmart.sakaiproject.component.api.UpdatableServerConfigurationService;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 30, 2008
 * Time: 10:35:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteTypesController extends AbstractFormController implements LoadObjectController {
   protected CustomizerService customizerService;
   private AuthzGroupService authzGroupService;
   private UpdatableServerConfigurationService updatableServerConfigurationService;
   private IdManager idManager;
   private Map<String, String> siteTypeToRealmMap;

   public Object fillBackingObject(Object incomingModel, Map request, Map session, Map application) throws Exception {
         
      return incomingModel;
   }
   
   public Map referenceData(Map request, Object command, Errors errors) {
      Map model = super.referenceData(request, command, errors);      
   
      List<ToSiteType> siteTypes = getCustomizerService().updatableServerConfigurationService().getSiteTypes();
      List<SiteTypeForm> decoratedTypes = new ArrayList();
      
      for (ToSiteType type : siteTypes) {
         SiteTypeForm siteTypeForm = new SiteTypeForm(type);
         setRealm(siteTypeForm);
         decoratedTypes.add(siteTypeForm);
         if (siteTypeForm.getSiteType().getId().getValue().equals(request.get("siteTypeExpanded"))) {
            model.put("expandedSiteType", siteTypeForm);
            
            if ("new".equals(request.get("renamingCatId"))) {
               // add blank cat here
               ToolCategoryForm blankCat = new ToolCategoryForm(new ToCategory());
               blankCat.getCategory().setId(getIdManager().getId("new"));
               siteTypeForm.getToolCategories().add(blankCat);
            }
            
            List<ToToolDef> uncatTools = getUncatTools(type);
            
            if (uncatTools.size() > 0) {
               ToolCategoryForm uncat = new ToolCategoryForm(new ToCategory());
               uncat.getCategory().setUncategorized(true);
               uncat.setTools(uncatTools);
               siteTypeForm.getToolCategories().add(uncat);
            }
         }
      }
      
      model.put("siteTypes", decoratedTypes);
      
      return model;
   }

   protected List<ToToolDef> getUncatTools(ToSiteType type) {
      Set categories = new HashSet();
      categories.add(type.getName());
      Set<Tool> toolRegistrations = ToolManager.findTools(categories, null);
      
      List<ToToolDef> tools = new ArrayList();
      
      for (Tool tool : toolRegistrations) {
         boolean found = false;
         
         for (ToCategory cat : type.getCategories()) {
            for (ToToolDef catTool : cat.getTools()) {
                if (catTool == null) {
                    continue;
                }
               if ((catTool.getToolId() != null) && catTool.getToolId().equals(tool.getId())) {
                  found = true;
                  break;
               }
            }
            
            if (found) {
               break;
            }
         }
         
         if (!found) {
            ToToolDef toolDef = new ToToolDef();
            toolDef.setToolId(tool.getId());
            tools.add(toolDef);
         }
      }
      
      return tools;
   }

   protected void setRealm(SiteTypeForm siteTypeForm) {
      String realmName = "!site.template." + siteTypeForm.getSiteType().getName();

      try {
         AuthzGroup ag = getAuthzGroupService().getAuthzGroup(realmName);
         siteTypeForm.setRealm(realmName);
         return;
      } catch (GroupNotDefinedException e) {
         // didn't find it... must be the other kind
      }
      
      siteTypeForm.setRealm(getSiteTypeToRealmMap().get(siteTypeForm.getSiteType().getName()));
   }

   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
      SiteTypeListForm form = (SiteTypeListForm) requestModel;
      
      if (form.getCatToolMovedTo() != null && form.getCatToolMovedTo().length() > 0) {
         // move the tool to new category
         moveTool(form);
      }
      if ("true".equals(request.get("submittingChangedName"))) {
         renameCategory(form);  
      }
      
      if (request.get("catMoveUpId") != null && !request.get("catMoveUpId").equals("")) {
         moveCat(form.getSiteTypeExpanded(), (String) request.get("catMoveUpId"), true);
      }
      if (request.get("catMoveDownId") != null && !request.get("catMoveDownId").equals("")) {
         moveCat(form.getSiteTypeExpanded(), (String) request.get("catMoveDownId"), false);
      }
      
      if (request.get("toolMoveUpId") != null && !request.get("toolMoveUpId").equals("")) {
         moveToolPos(form.getCatToolMovedFrom(), (String) request.get("toolMoveUpId"), true);
      }
      if (request.get("toolMoveDownId") != null && !request.get("toolMoveDownId").equals("")) {
         moveToolPos(form.getCatToolMovedFrom(), (String) request.get("toolMoveDownId"), false);
      }
      
      return new ModelAndView("success", form.getModel());
   }

   private void moveToolPos(String catId, String toolId, boolean up) {
      ToCategory cat = getUpdatableServerConfigurationService().getCategory(catId);
      
      int newIndex;
      
      for (int index = 0;index<cat.getTools().size();index++) {
         ToToolDef tool = cat.getTools().get(index);
         
         if (tool.getToolId().equals(toolId)) {
            cat.getTools().remove(index);
            newIndex = index + (up?-1:1);
            cat.getTools().add(newIndex, tool);
            break;
         }
      }

      getUpdatableServerConfigurationService().saveCategory(cat);
   }

   protected void moveCat(String siteTypeId, String catId, boolean up) {
      ToSiteType siteType = getUpdatableServerConfigurationService().getSiteType(siteTypeId);
      ToCategory cat = getUpdatableServerConfigurationService().getCategory(catId);
      
      int currentIndex = siteType.getCategories().indexOf(cat);
      
      if (up) {
         currentIndex--;
      }
      else {
         currentIndex++;
      }
      
      siteType.getCategories().remove(cat);
      siteType.getCategories().add(currentIndex, cat);
      
      getUpdatableServerConfigurationService().saveSiteType(siteType);
   }
   
   protected void renameCategory(SiteTypeListForm form) {
      if (form.getRenamingCatId() != null && !form.getRenamingCatId().equals("")) {
         ToCategory cat;
         if (form.getRenamingCatId().equals("new")) {
            cat = new ToCategory();
            cat.setName(form.getNewCategoryName());
            getUpdatableServerConfigurationService().saveNewCategory(form.getSiteTypeExpanded(), cat);
         }
         else {
            cat = getUpdatableServerConfigurationService().getCategory(form.getRenamingCatId());
            cat.setName(form.getNewCategoryName());
            getUpdatableServerConfigurationService().saveCategory(cat);
         }
      }
      
      form.setRenamingCatId(null);
   }

   protected void moveTool(SiteTypeListForm form) {
      getUpdatableServerConfigurationService().moveTool(form.getCatToolMovedFrom(),
         form.getCatToolMovedTo(), form.getMovingToolId());
      
      form.setCatToolMovedFrom(null);
      form.setCatToolMovedTo(null);
      form.setMovingToolId(null);
   }

   public ReloadableToolManager getToolManager() {
      return getCustomizerService().toolManager();
   }

   public CustomizerService getCustomizerService() {
      return customizerService;
   }

   public void setCustomizerService(CustomizerService customizerService) {
      this.customizerService = customizerService;
   }

   public AuthzGroupService getAuthzGroupService() {
      return authzGroupService;
   }

   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   public UpdatableServerConfigurationService getUpdatableServerConfigurationService() {
      return updatableServerConfigurationService;
   }

   public void setUpdatableServerConfigurationService(UpdatableServerConfigurationService updatableServerConfigurationService) {
      this.updatableServerConfigurationService = updatableServerConfigurationService;
   }

   public IdManager getIdManager() {
      return idManager;
   }

   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   public Map<String, String> getSiteTypeToRealmMap() {
      return siteTypeToRealmMap;
   }

   public void setSiteTypeToRealmMap(Map<String, String> siteTypeToRealmMap) {
      this.siteTypeToRealmMap = siteTypeToRealmMap;
   }
}
