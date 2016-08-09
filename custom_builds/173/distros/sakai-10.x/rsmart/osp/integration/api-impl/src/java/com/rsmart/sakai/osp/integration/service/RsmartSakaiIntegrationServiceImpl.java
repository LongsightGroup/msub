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

package com.rsmart.sakai.osp.integration.service;

import org.theospi.portfolio.admin.service.SakaiIntegrationServiceImpl;
import org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin;
import org.theospi.portfolio.admin.intf.StartupResetManager;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 16, 2007
 * Time: 10:42:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsmartSakaiIntegrationServiceImpl extends SakaiIntegrationServiceImpl {

   private boolean applySiteOptions = false;
   private StartupResetManager startupResetManager;


   protected void executePlugin(SakaiIntegrationPlugin plugin) {
      if (isApplyManager()) {
         super.executePlugin(plugin);
      }
   }

   public boolean isApplySiteOptions() {
      return applySiteOptions;
   }

   public void setApplySiteOptions(boolean applySiteOptions) {
      this.applySiteOptions = applySiteOptions;
   }

   protected boolean isApplyManager() {
      if (isApplySiteOptions()) {
         return true;
      }
      
      if (getStartupResetManager() != null) {
         return getStartupResetManager().isRecreate();         
      }
      
      return false;
   }

   public StartupResetManager getStartupResetManager() {
      return startupResetManager;
   }

   public void setStartupResetManager(StartupResetManager startupResetManager) {
      this.startupResetManager = startupResetManager;
   }

}
