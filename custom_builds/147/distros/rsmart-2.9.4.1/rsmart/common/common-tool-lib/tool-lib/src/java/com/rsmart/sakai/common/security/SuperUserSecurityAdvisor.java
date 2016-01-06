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
package com.rsmart.sakai.common.security;

import org.sakaiproject.authz.api.SecurityAdvisor;



/**
 * If the userId matches the superUser, this advisor will all allow.  Otherwise we PASS.
 */
public class SuperUserSecurityAdvisor implements SecurityAdvisor {
   private String superUser;
   public SecurityAdvice isAllowed(String userId, String function, String reference) {
      if (userId != null && userId.equals(superUser)) {
         return SecurityAdvice.ALLOWED;
      }
      return SecurityAdvice.PASS;
   }

   public String getSuperUser() {
      return superUser;
   }

   public void setSuperUser(String superUser) {
      this.superUser = superUser;
   }
}
