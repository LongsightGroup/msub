package com.rsmart.virtual_classroom.tool.spring.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.rsmart.virtual_classroom.intf.VirtualClassroomService;

import java.io.IOException;

/* Copyright 2008 The rSmart Group
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

public class JoinMeeting extends HandlerInterceptorAdapter {
   private VirtualClassroomService virtualClassroomService;

   public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {
      if ("join".equals(request.getParameter("command"))) {
         String virtualClassroomSessionId = request.getParameter("virtual_classroom_session_id");
         String jnlpDoc = virtualClassroomService.getJoinUrl(virtualClassroomSessionId);
         writeJNLPDoc(response, jnlpDoc);
         return false;
      } else if ("view".equals(request.getParameter("command"))) {
         long elluminateId = Long.valueOf(request.getParameter("elluminate_id")).longValue();
         String elluminateRecordingId = request.getParameter("elluminate_recording_id");
         String jnlpDoc = virtualClassroomService.getViewUrl(elluminateId, elluminateRecordingId);
         writeJNLPDoc(response, jnlpDoc);
         return false;
      }

      return true;
   }

   private void writeJNLPDoc(HttpServletResponse response, String jnlpDoc) throws IOException {
      response.setHeader("Content-Disposition", "attachment; filename=\"meeting.jnlp\"");
      response.setContentType("application/x-java-jnlp-file");
      response.getOutputStream().write(jnlpDoc.getBytes());
   }

   public void setVirtualClassroomService(VirtualClassroomService virtualClassroomService) {
      this.virtualClassroomService = virtualClassroomService;
   }
}
