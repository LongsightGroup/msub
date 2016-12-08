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
package com.rsmart.sakai.common.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.Method;



/**
 * Use for simple jobs that simply call a method in a service.  Wire up the service object and
 * the method name and you are done.
 */
public class AdminServiceJob extends AbstractAdminJob {
   private Object service;
   private String method;

   protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      if (service == null) {
         throw new RuntimeException("no service defined, did you forget to set the service?");
      }
      if (method == null) {
         throw new RuntimeException("no method defined, did you forget to set the method?");
      }

      try {
         Method serviceMethod = service.getClass().getMethod(method, (Class[])null);
         serviceMethod.invoke(service, (Object[]) null);
         logger.debug("invoked " + service.getClass().getName() + "." + method + "()");
      } catch (Exception e) {
         logger.error("problem invoking method [" + method + "] on [" + service.getClass().getName() + "]" , e);
      }
   }

   public String getMethod() {
      return method;
   }

   public void setMethod(String method) {
      this.method = method;
   }

   public Object getService() {
      return service;
   }

   public void setService(Object service) {
      this.service = service;
   }
}
