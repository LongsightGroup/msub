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
package com.rsmart.sakai.common.web.springframework;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TimeZone;



/**
 *
 */
public class TimezoneHandlerInterceptor implements HandlerInterceptor {

   public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
      String             timeZoneId = TimeZone.getDefault().getID();
      String             userId       = SessionManager.getCurrentSession().getUserId();
      Preferences        prefs      = PreferencesService.getInstance().getPreferences(userId);
      ResourceProperties props      = prefs.getProperties(TimeService.class.getName());
      String             timeZone   = props.getProperty  (TimeService.TIMEZONE_KEY   );

     if (timeZone != null && timeZone.length() >0) {
       timeZoneId = TimeZone.getTimeZone( timeZone ).getID();
     }
     httpServletRequest.setAttribute("timezone", timeZoneId);
      return true;
   }

   public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object, ModelAndView modelAndView) throws Exception {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object, Exception exception) throws Exception {
      //To change body of implemented methods use File | Settings | File Templates.
   }

}
