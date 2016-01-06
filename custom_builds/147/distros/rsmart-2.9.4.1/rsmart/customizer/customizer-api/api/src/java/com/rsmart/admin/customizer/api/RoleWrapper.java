package com.rsmart.admin.customizer.api;

import org.sakaiproject.authz.api.Role;

import java.util.Map;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2009
 * Time: 10:27:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class RoleWrapper {
   
   private Role role;
   private Map<String, Boolean> authzMap;

   public RoleWrapper(Role role, List<String> functions) {
      this.role = role;
      authzMap = new Hashtable();
      fillMap(functions);
   }

   protected void fillMap(List<String> functions) {
      authzMap.clear();
      for (String function : functions) {
         authzMap.put(function, getRole().isAllowed(function));
      }
   }

   public Role getRole() {
      return role;
   }

   public void setRole(Role role) {
      this.role = role;
   }

   public Map<String, Boolean> getAuthzMap() {
      return authzMap;
   }

   public void setAuthzMap(Map authzMap) {
      this.authzMap = authzMap;
   }

}
