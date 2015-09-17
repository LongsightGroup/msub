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

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 3, 2006
 * Time: 12:20:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class SakaiRealmProcessor extends XsltPreProcessor {

   public SakaiRealmProcessor() {
   }

   public SakaiRealmProcessor(boolean runtime) {
      super(runtime);
   }

   public void process(String sourceDir, String outputDir) {
      Map baseMap = new Hashtable();
      baseMap.put("source", sourceDir);
      baseMap.put("output", outputDir);

      setResolver(new FileSystemResourceResolver(baseMap));
      setWorking(new File(sourceDir));

      String commonComponents = "osp/common/component/src/webapp/WEB-INF/components.xml";
      String rsmartOspComponents = "rsmart/osp/common/component/src/webapp/WEB-INF/components.xml";
      String reportsComponents = "osp/reports/components/src/webapp/WEB-INF/components.xml";

      try {
         tranformFile("transform/realms.xsl", commonComponents, sourceDir);
         tranformFile("transform/osp-perms.xsl", rsmartOspComponents, sourceDir);
         tranformFile("transform/reports-cleanup.xsl", reportsComponents, sourceDir);
      } catch (FileNotFoundException e) {
         logger.error("", e);
      } catch (IOException e) {
         logger.error("", e);
      } catch (TransformerException e) {
         logger.error("", e);
      }
   }

}
