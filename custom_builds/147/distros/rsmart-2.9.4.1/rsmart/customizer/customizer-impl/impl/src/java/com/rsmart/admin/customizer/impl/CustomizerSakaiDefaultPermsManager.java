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

package com.rsmart.admin.customizer.impl;

import org.theospi.portfolio.security.model.SakaiDefaultPermsManager;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 16, 2007
 * Time: 11:50:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerSakaiDefaultPermsManager extends SakaiDefaultPermsManager {

   public void init() {
      // don't want to init yet
   }
   
   public void execute() {
      setAutoDdl(true);
      super.init();
   }

}
