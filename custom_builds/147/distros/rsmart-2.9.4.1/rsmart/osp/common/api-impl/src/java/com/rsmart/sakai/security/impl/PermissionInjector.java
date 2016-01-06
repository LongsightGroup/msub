/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.sakai.security.impl;

import org.theospi.portfolio.security.mgt.ToolPermissionManager;
import org.springframework.beans.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

import com.rsmart.sakai.security.intf.PermissionInjectorService;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 3, 2006
 * Time: 3:44:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionInjector {

   private static final Log log = LogFactory.getLog(PermissionInjector.class);

   private String key;
   private ToolPermissionManager permissionManager;
   private Map siteTypePermMap;
   private PermissionInjectorService permissionInjectorService;

   public void init() {
      PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(
         permissionManager.getClass(), "siteTypePermissions");

      setSiteTypePermMap(getPermissionInjectorService().getPermissionsMap(getKey(), getSiteTypePermMap()));
      
      try {
         descriptor.getWriteMethod().invoke(permissionManager, new Object[]{siteTypePermMap});
      } catch (IllegalAccessException e) {
         log.error("unable to set site type permissions", e);
      } catch (InvocationTargetException e) {
         log.error("unable to set site type permissions", e);
      }
   }

   public Map getSiteTypePermMap() {
      return siteTypePermMap;
   }

   public void setSiteTypePermMap(Map siteTypePermMap) {
      this.siteTypePermMap = siteTypePermMap;
   }

   public ToolPermissionManager getPermissionManager() {
      return permissionManager;
   }

   public void setPermissionManager(ToolPermissionManager permissionManager) {
      this.permissionManager = permissionManager;
   }

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public PermissionInjectorService getPermissionInjectorService() {
      return permissionInjectorService;
   }

   public void setPermissionInjectorService(PermissionInjectorService permissionInjectorService) {
      this.permissionInjectorService = permissionInjectorService;
   }

}
