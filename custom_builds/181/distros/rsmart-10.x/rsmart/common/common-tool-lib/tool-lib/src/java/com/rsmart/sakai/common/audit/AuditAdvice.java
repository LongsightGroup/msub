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
package com.rsmart.sakai.common.audit;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;



/**
 * Stores a method invocation into sakai's event table
 */
public class AuditAdvice implements AfterReturningAdvice {

   public void afterReturning(Object object, Method method, Object[] objects, Object object1) throws Throwable {
      String currentSiteId = getWorksiteManager().getCurrentWorksiteId().getValue();

      // note, sakai's event.event column is only 32 characters long, so for long method names this will fail
      Event event = EventTrackingService.newEvent(method.getName(),currentSiteId, false);
      EventTrackingService.post(event);
   }

   private WorksiteManager getWorksiteManager() {
      return (WorksiteManager)ComponentManager.get(WorksiteManager.class);
   }
}
