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

package com.rsmart.admin.customizer.util;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.sakaiproject.util.Xml;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 9, 2006
 * Time: 2:20:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolIconProcessor extends XsltPreProcessor {

   public ToolIconProcessor() {
   }

   public ToolIconProcessor(boolean runtime) {
      super(runtime);
   }

   public void process(String sourceDir, String workDir, String toolIconDir) {
      Document toolDoc = Xml.readDocument(
         new File(workDir, "tools-scrubbed.xml").getAbsolutePath());

      NodeList tools = toolDoc.getDocumentElement().getElementsByTagName("tool");

      for (int i=0;i<tools.getLength();i++) {
         Element toolElement = (Element) tools.item(i);

         NodeList icons = toolElement.getElementsByTagName("icon");
         if (icons.getLength() > 0) {
            String toolIcon = getElementText((Element) icons.item(0));

            if (toolIcon != null) {
               File iconFile = new File(toolIconDir, toolIcon);

               if (iconFile.exists()) {
                  File oldIcon = new File(sourceDir,
                     "reference/library/src/webapp/image/toolIcons/" + toolIcon);

                  if (oldIcon.exists()) {
                     oldIcon.delete();
                  }

                  try {
                     copyFile(iconFile, oldIcon);
                  } catch (IOException e) {
                     logger.error("", e);
                  }
               }
            }
         }
      }
   }

   public void copyFile(File src, File dest) throws IOException {
      OutputStream os = new FileOutputStream(dest);
      InputStream is = new FileInputStream(src);

      byte [] buffer = new byte[1024 * 100];

      int read = is.read(buffer);

      while (read != -1) {
         os.write(buffer, 0, read);
         read = is.read(buffer);
      }
   }

}
