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

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 28, 2006
 * Time: 8:29:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class FileSystemResourceResolver implements URIResolver  {

   private Map baseMap;

   public FileSystemResourceResolver(Map baseMap) {
      this.baseMap = baseMap;
   }

   public Source resolve(String href, String base) throws TransformerException {
      File file = new File(href);
      String baseName = "";

      File parent = file.getParentFile();
      while (parent != null) {
         baseName = parent.getName();
         parent = parent.getParentFile();
      }

      href = href.substring(baseName.length() + 1);

      String baseFile = (String) baseMap.get(baseName);
      File resolvedFile = new File(baseFile, href);
      InputStream fis;

      try {
         if (resolvedFile.exists()) {
            fis = new FileInputStream(resolvedFile);
         }
         else {
            fis = getClass().getResourceAsStream("/" + href);
            if (fis == null) {
               throw new FileNotFoundException(href);
            }
         }
         return new StreamSource(fis);
      } catch (FileNotFoundException e) {
         try {
            fis = getClass().getResourceAsStream("/empty.xml");
            if (fis == null) {
               throw new FileNotFoundException();
            }
            return new StreamSource(fis);
         } catch (FileNotFoundException e1) {
            throw new TransformerException(e);
         }
      }

   }
}
