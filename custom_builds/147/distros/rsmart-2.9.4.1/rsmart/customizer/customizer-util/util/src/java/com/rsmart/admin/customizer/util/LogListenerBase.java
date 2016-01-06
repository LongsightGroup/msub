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

package com.rsmart.admin.customizer.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 8:31:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogListenerBase implements LogListener {

   private List warns = new ArrayList();
   private List errors = new ArrayList();
   private List fatals = new ArrayList();

   public void warn(Object object) {
      warns.add(object.toString());
   }

   public void warn(Object object, Throwable throwable) {
      warns.add(object.toString() + " -- " + throwable.getLocalizedMessage());
   }

   public void error(Object object) {
      errors.add(object.toString());
   }

   public void error(Object object, Throwable throwable) {
      errors.add(object.toString() + " -- " + throwable.getLocalizedMessage());
   }

   public void fatal(Object object) {
      fatals.add(object.toString());
   }

   public void fatal(Object object, Throwable throwable) {
      fatals.add(object.toString() + " -- " + throwable.getLocalizedMessage());
   }

   public List getWarns() {
      return warns;
   }

   public List getErrors() {
      return errors;
   }

   public List getFatals() {
      return fatals;
   }
}
