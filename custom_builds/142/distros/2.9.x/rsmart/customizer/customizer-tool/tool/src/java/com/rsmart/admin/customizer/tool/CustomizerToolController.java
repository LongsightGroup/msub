package com.rsmart.admin.customizer.tool;

import org.sakaiproject.metaobj.utils.mvc.impl.servlet.AbstractFormController;
import org.sakaiproject.metaobj.utils.mvc.intf.LoadObjectController;
import org.sakaiproject.tool.api.Tool;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;

import java.util.*;

import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;
import com.rsmart.admin.customizer.api.CustomizerService;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 2, 2008
 * Time: 10:55:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerToolController extends AbstractFormController implements LoadObjectController {

   protected CustomizerService customizerService;

   public Object fillBackingObject(Object incomingModel, Map request, Map session, Map application) throws Exception {
         
      return incomingModel;
   }
   
   public Map referenceData(Map request, Object command, Errors errors) {
      Map model = super.referenceData(request, command, errors);      
   
      // get tool list here
      Set<Tool> toolSet = getToolManager().findTools(null, null);
      TreeSet<Tool> tools = new TreeSet<Tool>(
         new Comparator<Tool>(){
            public int compare(Tool tool, Tool tool1) {
               return tool.getId().compareTo(tool1.getId());
            }
         }
      );
      
      tools.addAll(toolSet);
      model.put("tools", tools);
      
      return model;
   }

   /**
    * Return if cancel action is specified in the request.
    * <p>Default implementation looks for "_cancel" parameter in the request.
    *
    * @param request current HTTP request
    * @see #PARAM_CANCEL
    */
   public boolean isCancel(Map request) {
      return "true".equals(request.get("cancelEdit"));
   }

   public ModelAndView processCancel(Map request, Map session, Map application,
                                     Object command, Errors errors) throws Exception {
      return new ModelAndView("success");
   }
   
   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
      ToolForm tool = (ToolForm) requestModel;
      
      if (tool.getToolId() != null) {
         // save changes here
         getCustomizerService().changeToolTitle(tool.getToolId(), tool.getToolTitle());
         tool.setToolId(null);         
      }
      
      if (tool.getEditToolId() != null && tool.getEditToolId().length() > 0) {
         // redirect here
         tool.setToolId(tool.getEditToolId());
         tool.setToolTitle(getToolManager().getTool(tool.getToolId()).getTitle());
      }
      
      Hashtable model = new Hashtable();
      
      if (tool.getToolId() != null) {
         model.put("toolId", tool.getToolId());
         model.put("toolTitle", tool.getToolTitle());
      }
      
      return new ModelAndView("success", model);
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
   
}
