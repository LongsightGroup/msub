package com.rsmart.admin.customizer.tool;

import org.sakaiproject.metaobj.utils.mvc.impl.servlet.AbstractFormController;
import org.sakaiproject.metaobj.utils.mvc.intf.LoadObjectController;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.authz.cover.FunctionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;

import java.util.Map;
import java.util.List;
import java.util.Collections;

import com.rsmart.admin.customizer.api.CustomizerService;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 2, 2009
 * Time: 3:17:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class FuncRequireController extends AbstractFormController implements LoadObjectController {

   protected CustomizerService customizerService;
   
   public Object fillBackingObject(Object incomingModel, Map request, Map session, Map application) throws Exception {
      FuncRequireForm form = (FuncRequireForm) incomingModel;
      List<String> allFunctions = FunctionManager.getRegisteredFunctions();
		Collections.sort(allFunctions);
      
      form.setFunctions(allFunctions);

      Tool toolReg = getCustomizerService().toolManager().getTool(form.getToolId());
      String funcRequire = toolReg.getMutableConfig().getProperty(CustomizerService.FUNCTIONS_REQUIRE);

      form.setRequired(funcRequire);
      
      return form;
   }

   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
      FuncRequireForm form = (FuncRequireForm) requestModel;
      
      getCustomizerService().setFunctionsRequire(form.getToolId(), form.getRequiredAsString());
      
      return new ModelAndView("success");         
   }

   public CustomizerService getCustomizerService() {
      return customizerService;
   }

   public void setCustomizerService(CustomizerService customizerService) {
      this.customizerService = customizerService;
   }
}
