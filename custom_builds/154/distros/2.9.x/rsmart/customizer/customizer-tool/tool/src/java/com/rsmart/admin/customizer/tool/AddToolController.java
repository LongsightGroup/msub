package com.rsmart.admin.customizer.tool;

import org.sakaiproject.metaobj.utils.mvc.impl.servlet.AbstractFormController;
import org.sakaiproject.metaobj.utils.mvc.intf.LoadObjectController;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import com.rsmart.sakaiproject.component.model.ToSiteType;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 9, 2009
 * Time: 10:13:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddToolController extends SiteTypesController {
   
   public Map referenceData(Map request, Object command, Errors errors) {
      Map model = new Hashtable();
      AddToolForm form = (AddToolForm) command;
      
      Set categories = new HashSet();
      categories.add(form.getSiteType());
      Set<Tool> toolRegistrations = ToolManager.findTools(categories, null);
      
      // load in tool list here
      Set<Tool> toolSet = getToolManager().findTools(null, null);
      TreeSet<Tool> tools = new TreeSet<Tool>(
         new Comparator<Tool>(){
            public int compare(Tool tool, Tool tool1) {
               return tool.getId().compareTo(tool1.getId());
            }
         }
      );
      
      tools.addAll(toolSet);

      List<ToolForm> toolForms = new ArrayList();
      
      for (Iterator<Tool> i=tools.iterator();i.hasNext();) {
         Tool tool = i.next();
         
         if (getToolManager().isStealthed(tool)) {
            i.remove();
         }
         else {
            toolForms.add(new ToolForm(tool, 
               toolRegistrations.contains(tool)));
         }
      }
      
      Map<String, Boolean> selectedTools = new Hashtable();
      
      for (Tool tool : toolRegistrations) {
         selectedTools.put(tool.getId(), true);
      }
      
      form.setSelectedTools(selectedTools);
      
      model.put("toolList", toolForms);
      
      return model;
   }

   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
      AddToolForm form = (AddToolForm) requestModel;
      
      if (form.getAddToolsToType() != null) {
         getCustomizerService().addToolsToCategory(form.getSiteType(), form.getAddToolsToType());
      }
      
      if (form.getAddToolsToSites() != null) {
         getCustomizerService().addToolsToCategorySites(form.getSiteType(),  form.getAddToolsToSites());
      }
      
      Map model = new Hashtable();
      
      model.put("siteTypeExpanded", form.getSiteTypeId());
      return new ModelAndView("success", model);
   }
}
