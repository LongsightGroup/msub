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

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;



/**
 * <p>Checks that the current agent has permission in the current site.  Throws a PermissionException if the check fails,
 * otherwise lets the method call continue.</p>
 *
 * <p>To use inject the permission (function) name and wire to an advisor and target.</p>
 */
public class SitePermissionAdvice extends AbstractPermissionEnforcer implements MethodBeforeAdvice {

   public void before(Method method, Object[] objects, Object object) throws Throwable {
      checkPermission();
   }
}
