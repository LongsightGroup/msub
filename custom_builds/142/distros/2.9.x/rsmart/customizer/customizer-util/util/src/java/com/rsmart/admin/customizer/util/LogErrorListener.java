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

import org.apache.commons.logging.Log;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 6, 2007
 * Time: 12:19:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogErrorListener implements ErrorListener {

   private Log logger;

   public LogErrorListener(Log logger) {
      this.logger = logger;
   }

   public void warning(TransformerException transformerException) throws TransformerException {
      logger.warn("" + transformerException);   
   }

   public void error(TransformerException transformerException) throws TransformerException {
      logger.error("", transformerException);   
   }

   public void fatalError(TransformerException transformerException) throws TransformerException {
      logger.fatal("", transformerException);   
   }

}
