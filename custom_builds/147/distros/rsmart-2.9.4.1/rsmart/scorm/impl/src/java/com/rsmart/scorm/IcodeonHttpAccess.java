package com.rsmart.scorm;

import org.sakaiproject.entity.api.*;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.io.IOException;
import java.net.URLEncoder;

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
public class IcodeonHttpAccess implements HttpAccess {
   public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException {
      try {
         String uuid = ContentHostingService.getUuid(ref.getId());
         String sessionId = SessionManager.getCurrentSession().getId();
         String learnerId = SessionManager.getCurrentSession().getUserId();
         redirectToLaunchPage(req, res, uuid, sessionId, learnerId);
        // redirectAndLaunch(res, uuid, sessionId, learnerId);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private void redirectToLaunchPage(HttpServletRequest req, HttpServletResponse res, String uuid, String sessionId, String learnerId) throws IOException {
      res.sendRedirect("/rsmart-scorm-helper/launch.form?url=" + URLEncoder.encode(getLaunchUrl(uuid, sessionId, learnerId)));     
   }


   private void redirectAndLaunch(HttpServletResponse res, String uuid, String sessionId, String learnerId) throws IOException {
      res.sendRedirect(getLaunchUrl(uuid, sessionId, learnerId));
   }

   private String getLaunchUrl(String uuid, String sessionId, String learnerId) {
      return ServerConfigurationService.getServerUrl() +
            "/player2/skins/main.do?learnerID=" + learnerId + "&courseID=" + uuid +
            "&sessionID=" + sessionId + "&domainID=sakai";
   }


}
