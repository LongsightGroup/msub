package com.rsmart.admin.customizer.tool;

import java.util.List;

import com.rsmart.admin.customizer.api.RoleWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2009
 * Time: 10:19:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class PermForm {
   
   String siteType;
   String realm;
   
   List<RoleWrapper> roles;
   List<String> functions;
   List<String> perms;

   public PermForm() {
   }

   public String getSiteType() {
      return siteType;
   }

   public void setSiteType(String siteType) {
      this.siteType = siteType;
   }

   public List<RoleWrapper> getRoles() {
      return roles;
   }

   public void setRoles(List<RoleWrapper> roles) {
      this.roles = roles;
   }

   public List<String> getFunctions() {
      return functions;
   }

   protected String escape(String func) {
      return "func" + Math.abs(func.hashCode());   
   }
   
   public void setFunctions(List<String> functions) {
      this.functions = functions;
   }

   public String getRealm() {
      return realm;
   }

   public void setRealm(String realm) {
      this.realm = realm;
   }

   public List<String> getPerms() {
      return perms;
   }

   public void setPerms(List<String> perms) {
      this.perms = perms;
   }
}
