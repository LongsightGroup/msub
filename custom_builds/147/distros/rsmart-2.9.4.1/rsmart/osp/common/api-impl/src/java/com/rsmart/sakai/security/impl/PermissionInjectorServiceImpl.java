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

import com.rsmart.sakai.security.intf.PermissionInjectorService;
import com.rsmart.sakai.security.model.PermissionMap;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 16, 2007
 * Time: 9:50:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionInjectorServiceImpl extends HibernateDaoSupport implements PermissionInjectorService {

   private boolean recreate = false;
   
   public void init() {
      if (recreate) {
         clearPermissions();
      }
   }

   public Map getPermissionsMap(String key, Map defaultMap) {
      List permMaps = getHibernateTemplate().find("from PermissionMap where key = ?", key);
      PermissionMap returned = null;
      
      if (permMaps.size() > 0) {
         returned = (PermissionMap) permMaps.get(0);
      }
      else {
         returned = new PermissionMap(key, defaultMap);
         getHibernateTemplate().save(returned);
      }
      
      return returned.getDefaultPermsNative();
   }

   public void clearPermissions() {
      List perms = getHibernateTemplate().loadAll(PermissionMap.class);
      getHibernateTemplate().deleteAll(perms);
   }

   public boolean isRecreate() {
      return recreate;
   }

   public void setRecreate(boolean recreate) {
      this.recreate = recreate;
   }
}
