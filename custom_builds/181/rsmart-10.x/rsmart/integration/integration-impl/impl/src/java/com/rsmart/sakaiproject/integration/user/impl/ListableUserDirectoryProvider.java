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

package com.rsmart.sakaiproject.integration.user.impl;

import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.component.cover.ComponentManager;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 22, 2007
 * Time: 4:05:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListableUserDirectoryProvider implements UserDirectoryProvider {

   private UserDirectoryProvider realDirectoryProvider = null;
   private String realDirectoryProviderName = null;

   public boolean authenticateUser(String eid, UserEdit edit, String password) {
      if (getRealDirectoryProvider() == null) {
         return false;
      }
      return getRealDirectoryProvider().authenticateUser(eid, edit, password);
   }

   public boolean authenticateWithProviderFirst(String eid) {
      if (getRealDirectoryProvider() == null) {
         return false;
      }
      return getRealDirectoryProvider().authenticateWithProviderFirst(eid);
   }

   public boolean findUserByEmail(UserEdit edit, String email) {
      if (getRealDirectoryProvider() == null) {
         return false;
      }
      return getRealDirectoryProvider().findUserByEmail(edit, email);
   }

   public boolean getUser(UserEdit edit) {
      if (getRealDirectoryProvider() == null) {
         return false;
      }
      return getRealDirectoryProvider().getUser(edit);
   }

   public void getUsers(Collection users) {
      if (getRealDirectoryProvider() == null) {
         return;
      }
      getRealDirectoryProvider().getUsers(users);
   }

   public UserDirectoryProvider getRealDirectoryProvider() {
      if (realDirectoryProvider == null && realDirectoryProviderName != null) {
         realDirectoryProvider = (UserDirectoryProvider) ComponentManager.get(realDirectoryProviderName);
      }

      return realDirectoryProvider;
   }

   public void setRealDirectoryProvider(UserDirectoryProvider realDirectoryProvider) {
      this.realDirectoryProvider = realDirectoryProvider;
   }

   public String getRealDirectoryProviderName() {
      return realDirectoryProviderName;
   }

   public void setRealDirectoryProviderName(String realDirectoryProviderName) {
      this.realDirectoryProviderName = realDirectoryProviderName;
   }
}
