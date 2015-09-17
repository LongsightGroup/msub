package com.rsmart.admin.customizer.tool;

import org.sakaiproject.metaobj.utils.mvc.impl.servlet.AbstractFormController;
import org.sakaiproject.metaobj.utils.mvc.intf.LoadObjectController;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.FunctionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.Errors;

import java.util.*;

import com.rsmart.admin.customizer.api.CustomizerService;
import com.rsmart.admin.customizer.api.RoleWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2009
 * Time: 10:17:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteTypePermController extends AbstractFormController implements LoadObjectController {

   protected CustomizerService customizerService;
   private AuthzGroupService authzGroupService;
   
   public Object fillBackingObject(Object incomingModel, Map request, Map session, Map application) throws Exception {
      PermForm form = (PermForm) incomingModel;
      
      // fill in roles and functions
      List<String> allFunctions = FunctionManager.getRegisteredFunctions();
		Collections.sort(allFunctions);
      
      form.setFunctions(allFunctions);
      
      AuthzGroup group = getAuthzGroupService().getAuthzGroup(form.getRealm());
      Set<Role> roles = group.getRoles();
      
      List<RoleWrapper> wrappedRoles = new ArrayList<RoleWrapper>();
      
      for (Role role : roles) {
         wrappedRoles.add(new RoleWrapper(role, allFunctions));      
      }
      
      form.setRoles(wrappedRoles);
      
      return form;
   }
   
   public ModelAndView handleRequest(Object requestModel, Map request, Map session, Map application, Errors errors) {
      Map<String, List<String>> perms = new Hashtable();
      PermForm form = (PermForm) requestModel;
      
      for (String rolePerm : form.getPerms()) {
         String[] rolePermAr = rolePerm.split("~");
         String func = rolePermAr[0];
         String role = rolePermAr[1];
         
         List<String> roleList = perms.get(role);
         
         if (roleList == null) {
            roleList = new ArrayList();
            perms.put(role, roleList);
         }
         
         roleList.add(func);
      }
      
      getCustomizerService().updatePermissions(form.getRealm(), form.getSiteType(), perms);
      
      return new ModelAndView("success");
   }

   public AuthzGroupService getAuthzGroupService() {
      return authzGroupService;
   }

   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   public CustomizerService getCustomizerService() {
      return customizerService;
   }

   public void setCustomizerService(CustomizerService customizerService) {
      this.customizerService = customizerService;
   }
}
