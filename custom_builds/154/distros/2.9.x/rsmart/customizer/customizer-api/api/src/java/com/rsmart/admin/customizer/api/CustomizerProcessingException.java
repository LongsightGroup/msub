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

package com.rsmart.admin.customizer.api;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 7:39:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerProcessingException extends RuntimeException {

   private List<String> warnings = new ArrayList<String>();
   private List<String> errors = new ArrayList<String>();

   public CustomizerProcessingException() {
   }

   public CustomizerProcessingException(String string) {
      super(string);
   }

   public CustomizerProcessingException(String string, Throwable throwable) {
      super(string, throwable);
   }

   public CustomizerProcessingException(Throwable throwable) {
      super(throwable);
   }

   public void addWarning(String warn) {
      getWarnings().add(warn);
   }
   
   public void addError(String error) {
      getErrors().add(error);   
   }
   
   public List<String> getWarnings() {
      return warnings;
   }

   public List<String> getErrors() {
      return errors;
   }

   public boolean hasWarningsOrErrors() {
      return errors.size() > 0 || warnings.size() > 0;
   }

}
