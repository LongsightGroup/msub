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

package com.rsmart.admin.customizer.tool;

import com.rsmart.admin.customizer.api.CustomizerProcessingException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 9:10:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerForm {

   private CustomizerProcessingException exception;
   private long lastModifiedDir = 0;
   private String fileId;
   
   public CustomizerProcessingException getException() {
      return exception;
   }

   public void setException(CustomizerProcessingException exception) {
      this.exception = exception;
   }

   public long getLastModifiedDir() {
      return lastModifiedDir;
   }

   public void setLastModifiedDir(long lastModifiedDir) {
      this.lastModifiedDir = lastModifiedDir;
   }

   public String getFileId() {
      return fileId;
   }

   public void setFileId(String fileId) {
      this.fileId = fileId;
   }
}
