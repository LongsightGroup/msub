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

import com.rsmart.admin.customizer.util.FileSystemResourceResolver;

import javax.xml.transform.TransformerException;
import java.util.Map;
import java.util.Hashtable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 7, 2006
 * Time: 7:14:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkspaceProcessor extends XsltPreProcessor {

   public WorkspaceProcessor() {
   }

   public WorkspaceProcessor(boolean runtime) {
      super(runtime);
   }

   public void process(String sourceDir, String outputDir) {
      Map baseMap = new Hashtable();
      baseMap.put("source", sourceDir);
      baseMap.put("output", outputDir);

      setResolver(new FileSystemResourceResolver(baseMap));
      setWorking(new File(sourceDir));

      String rsmartIntegrationComponents = "rsmart/osp/integration/component/src/webapp/WEB-INF/components.xml";
      String ospIntegrationComponents = "osp/integration/component/src/webapp/WEB-INF/components.xml";

      try {
         tranformFile("transform/rsmartComponents.xsl", rsmartIntegrationComponents, sourceDir);
         tranformFile("transform/integrationOptions.xsl", ospIntegrationComponents, sourceDir);
      } catch (FileNotFoundException e) {
         logger.error("", e);
      } catch (MalformedURLException e) {
         logger.error("", e);
      } catch (TransformerException e) {
         logger.error("", e);
      } catch (IOException e) {
         logger.error("", e);
      }
   }
}
