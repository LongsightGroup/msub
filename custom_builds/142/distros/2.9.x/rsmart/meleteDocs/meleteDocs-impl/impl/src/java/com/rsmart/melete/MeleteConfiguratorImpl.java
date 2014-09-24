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

package com.rsmart.melete;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FileUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Sep 21, 2006
 * Time: 4:57:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MeleteConfiguratorImpl  {
   private static final Log logger = LogFactory.getLog(MeleteConfiguratorImpl.class);

   private String homeDir;
   private String packageFilesDir;
   private String meleteDocsDir;
   private String uploadDir;

   public void init(){
      // look in sakai.properties first, else fall back on default in spring config
      homeDir = ServerConfigurationService.getString("melete.homeDir", getHomeDir());
      packageFilesDir = ServerConfigurationService.getString("melete.packagingDir", getPackageFilesDir());
      //meleteDocsDir = ServerConfigurationService.getString("melete.meleteDocsDir", getMeleteDocsDir());
      uploadDir = ServerConfigurationService.getString("melete.uploadDir", getUploadDir());

      if(homeDir != null && !homeDir.isEmpty()){
          createDirectory(homeDir);
      }
      if(packageFilesDir != null && !packageFilesDir.isEmpty()){
          createDirectory(packageFilesDir);
          createPackageFiles(new String[]{"imscp_v1p1.xsd", "imsmanifest.xml", "imsmd_v1p2.xsd", "SCORM2004base.zip", "xml.xsd"});
      }
      if(uploadDir != null && !uploadDir.isEmpty()){
          createDirectory(uploadDir);
      }

      //createDirectory(meleteDocsDir);

      //ServerConfigurationService.getString("melete.homeDir", ServerConfigurationService.getSakaiHomePath() + "/melete");
      //ServerConfigurationService.getString("melete.packagingDir", ServerConfigurationService.getSakaiHomePath() + "/melete/packagefiles");
      //ServerConfigurationService.getString("melete.meleteDocsDir", ServerConfigurationService.getSakaiHomePath() + "/melete/meleteDocs");
      //ServerConfigurationService.getString("melete.uploadDir", ServerConfigurationService.getSakaiHomePath() + "/melete/uploads");

   }

    private void createPackageFiles(String[] fileNames) {

        for(String fileName : fileNames){
            String destinationString = packageFilesDir + "/" + fileName;
            File destinationFile = new File(destinationString);

            if(!destinationFile.exists()){
                String sourceString = System.getProperty("catalina.base") +  "/webapps/meleteDocs/packaging/" + fileName;
                File sourceFile = new File(sourceString);

                try {
                    FileUtils.copyFile(sourceFile, destinationFile);
                } catch (IOException e) {
                    logger.error("error trying to copy package file " + e, e);
                }
            }
        }
    }

    protected void createDirectory(String dirName) {
      File file = new File(dirName);
      if (!file.exists()) {
         boolean result = file.mkdir();
         if (!result) {
            logger.error("error trying to create dir  [" + dirName + "] ");
         }
      }
      if (!file.isDirectory()){
         logger.error("path  [" + dirName + "] is not a directory");         
      }
   }


   public String getHomeDir() {
      return homeDir;
   }

   public void setHomeDir(String homeDir) {
      this.homeDir = homeDir;
   }

   public String getPackageFilesDir() {
      return packageFilesDir;
   }

   public void setPackageFilesDir(String packageFilesDir) {
      this.packageFilesDir = packageFilesDir;
   }

   public String getMeleteDocsDir() {
      return meleteDocsDir;
   }

   public void setMeleteDocsDir(String meleteDocsDir) {
      this.meleteDocsDir = meleteDocsDir;
   }

   public String getUploadDir() {
      return uploadDir;
   }

   public void setUploadDir(String uploadDir) {
      this.uploadDir = uploadDir;
   }
}
