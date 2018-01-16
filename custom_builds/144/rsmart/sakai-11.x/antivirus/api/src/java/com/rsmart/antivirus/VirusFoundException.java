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

package com.rsmart.antivirus;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 6, 2006
 * Time: 10:17:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class VirusFoundException extends RuntimeException {
   private String virusName = "unknown";

   public VirusFoundException(String message) {
      super(message);
   }

   public String getVirusName() {
      return virusName;
   }

   public void setVirusName(String virusName) {
      this.virusName = virusName;
   }
}
