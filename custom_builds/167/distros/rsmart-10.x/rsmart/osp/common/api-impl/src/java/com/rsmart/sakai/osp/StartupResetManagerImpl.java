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

package com.rsmart.sakai.osp;

import org.theospi.portfolio.admin.intf.StartupResetManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.db.api.SqlService;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 24, 2007
 * Time: 8:45:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class StartupResetManagerImpl implements StartupResetManager {

   private boolean recreate = false;
   private List dependancies;
   private EventTrackingService eventTrackingService;
   private SqlService sqlService;
   
   private static final String STARTUP_EVENT = "rsn.startup";

   public void init() {
      /*
      List events = getSqlService().dbRead("select EVENT from SAKAI_EVENT where EVENT='" + STARTUP_EVENT + "'");
      
      if (events.size() == 0) {
         setRecreate(true);
         Event event = getEventTrackingService().newEvent(STARTUP_EVENT, null, true);
         getEventTrackingService().post(event);
      }
      else {
         setRecreate(false);
      }
      */
   }
   
   public boolean isRecreate() {
      return recreate;
   }

   public void setRecreate(boolean recreate) {
      this.recreate = recreate;
   }

   public List getDependancies() {
      return dependancies;
   }

   public void setDependancies(List dependancies) {
      this.dependancies = dependancies;
   }

   public EventTrackingService getEventTrackingService() {
      return eventTrackingService;
   }

   public void setEventTrackingService(EventTrackingService eventTrackingService) {
      this.eventTrackingService = eventTrackingService;
   }

   public SqlService getSqlService() {
      return sqlService;
   }

   public void setSqlService(SqlService sqlService) {
      this.sqlService = sqlService;
   }
}
