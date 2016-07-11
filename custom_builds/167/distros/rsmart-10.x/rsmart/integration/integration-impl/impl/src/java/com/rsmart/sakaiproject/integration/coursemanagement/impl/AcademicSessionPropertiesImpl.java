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

package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.sakaiproject.coursemanagement.impl.AcademicSessionCmImpl;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: May 21, 2007
 * Time: 2:57:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class AcademicSessionPropertiesImpl extends AcademicSessionCmImpl {

   private boolean currentTerm = false;

   public AcademicSessionPropertiesImpl(String eid, String title, String description, Date startDate, Date endDate, boolean currentTerm) {
      super(eid, title, description, startDate, endDate);
      this.currentTerm = currentTerm;
   }

   public boolean isCurrentTerm() {
      return currentTerm;
   }

   public void setCurrentTerm(boolean currentTerm) {
      this.currentTerm = currentTerm;
   }
}
