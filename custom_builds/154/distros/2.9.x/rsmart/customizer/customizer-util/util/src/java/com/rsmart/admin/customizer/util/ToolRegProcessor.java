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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.sakaiproject.util.Xml;

import javax.xml.transform.TransformerException;
import java.util.Map;
import java.util.Hashtable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.rsmart.admin.customizer.util.FileSystemResourceResolver;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 1, 2006
 * Time: 9:52:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ToolRegProcessor extends XsltPreProcessor {

   public ToolRegProcessor() {
   }

   public ToolRegProcessor(boolean runtime) {
      super(runtime);
   }

   public void process(String sourceDir, String outputDir) {
      Map baseMap = new Hashtable();
      baseMap.put("source", sourceDir);
      baseMap.put("output", outputDir);

      setResolver(new FileSystemResourceResolver(baseMap));
      setWorking(new File(sourceDir));

      File inputFilePath = new File(outputDir, "tools-scrubbed.xml");
      Document tools = Xml.readDocument(inputFilePath.getAbsolutePath());

      NodeList toolsList = tools.getElementsByTagName("tool");
      try {
         for (int i=0;i<toolsList.getLength();i++) {
            Element tool = (Element) toolsList.item(i);

            String regFilePath = tool.getAttribute("regFile");
            tranformFile("transform/regFile.xsl", regFilePath, sourceDir);
         }

      } catch (FileNotFoundException e) {
         logger.error("", e);
      } catch (IOException e) {
         logger.error("", e);
      } catch (TransformerException e) {
         logger.error("", e);
      }
   }



}
