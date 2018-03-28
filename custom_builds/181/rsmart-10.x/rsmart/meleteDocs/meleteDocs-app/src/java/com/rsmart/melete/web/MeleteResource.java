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

package com.rsmart.melete.web;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * <p>This servlet simply checks that the user is logged in and then servers up the resource specified by the requestURI.</p>
 *
 * <p>For example, if the request was http://localhost:8080/meleteDocs/course_238a3780-4539-432b-005b-38a2050febef/enrollment%20snapshot.xls</p>
 * <p>The servlet will stream the file located in ${melete.meleteDocsDir}/meleteDocs/course_238a3780-4539-432b-005b-38a2050febef/enrollment snapshot.xls
 * on the file system</p>
 *
 * <p>melete.meleteDocsDir is a property configured in the sakai.properties</p>
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 21, 2006
 * Time: 2:54:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class MeleteResource extends HttpServlet {

   public void doGet(HttpServletRequest req, HttpServletResponse res)
         throws ServletException, IOException {

      // Use a ServletOutputStream because we may pass binary information
      ServletOutputStream out = res.getOutputStream();

      String currentUser = SessionManager.getInstance().getCurrentSessionUserId();

      if (currentUser == null) {
         out.println("Not authorized");
         return;
      }

      // Get the file to view
      String fileName = getMeleteDocsPath() + URLDecoder.decode(req.getRequestURI().toString(), "UTF8").replaceFirst(req.getContextPath(),"");

      // No file, nothing to view
      if (fileName == null) {
         out.println("File not found");
      }

      File file = new File(fileName);
      if (!file.exists()){
         out.println("File not found");
         return;
      }

      // Get and set the type of the file
      String contentType = getServletContext().getMimeType(fileName);
      res.setContentType(contentType);

      // Return the file
      try {
         returnFile(file, out);
      }
      catch (FileNotFoundException e) {
         out.println("File not found");
      }
      catch (IOException e) {
         out.println("Problem sending file: " + e.getMessage());
      }
   }

   protected String getMeleteDocsPath() {
      return ServerConfigurationService.getString("melete.meleteDocsDir", ServerConfigurationService.getSakaiHomePath() + "/melete/meleteDocs");
   }

   protected void returnFile(File file, OutputStream out)
         throws FileNotFoundException, IOException {
      // A FileInputStream is for bytes
      FileInputStream fis = null;
      try {
         fis = new FileInputStream(file);
         byte[] buf = new byte[4 * 1024];  // 4K buffer
         int bytesRead;
         while ((bytesRead = fis.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
         }
      }
      finally {
         if (fis != null) fis.close();
      }
   }
}
