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

package com.rsmart.admin.customizer.app;

import com.rsmart.admin.customizer.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 25, 2006
 * Time: 10:09:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

   public static void main(String args[]) {

      if (args.length < 3) {
         System.out.println("Usage:  ");
         System.out.println("customizer <spreadsheet.xml> <source path> <working dir> [<icon dir>]");
         System.out.println("Usage:  ");
         return;
      }

      if (System.getProperty("dbuggin") != null) {
         try {
            Thread.sleep(Long.valueOf(System.getProperty("dbuggin")));
         } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
      }
      
      try {
         Thread.sleep(10);
      } catch (InterruptedException e) {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

      ExcelPreProcessor preProcessor = new ExcelPreProcessor();

      preProcessor.processDocument(args[0], args[1], args[2]);

      XsltPreProcessor processor = new XsltPreProcessor();
      processor.process(args[1], args[2]);

      ToolRegProcessor toolRegProcessor = new ToolRegProcessor();
      toolRegProcessor.process(args[1], args[2]);

      SakaiRealmProcessor sakaiRealmProcessor = new SakaiRealmProcessor();
      sakaiRealmProcessor.process(args[1], args[2]);

      ToolCategoryProcessor toolCategoryProcessor = new ToolCategoryProcessor();
      toolCategoryProcessor.process(args[1], args[2]);

      WorkspaceProcessor workspaceProcessor = new WorkspaceProcessor();
      workspaceProcessor.process(args[1], args[2]);

      if (args.length > 3) {
         ToolIconProcessor toolIconProcessor = new ToolIconProcessor();
         toolIconProcessor.process(args[1], args[2], args[3]);
      }
   }

}
