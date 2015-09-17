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

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 8:26:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListenableLogger implements Log {
   
   private Log logger;
   private LogListener listener = null;

   public ListenableLogger(Log logger) {
      this.logger = logger;
   }

   public boolean isDebugEnabled() {
      return logger.isDebugEnabled();
   }

   public boolean isErrorEnabled() {
      return logger.isErrorEnabled();
   }

   public boolean isFatalEnabled() {
      return logger.isFatalEnabled();
   }

   public boolean isInfoEnabled() {
      return logger.isInfoEnabled();
   }

   public boolean isTraceEnabled() {
      return logger.isTraceEnabled();
   }

   public boolean isWarnEnabled() {
      return logger.isWarnEnabled();
   }

   public void trace(Object o) {
      logger.trace(o);
   }

   public void trace(Object o, Throwable throwable) {
      logger.trace(o, throwable);
   }

   public void debug(Object o) {
      logger.debug(o);
   }

   public void debug(Object o, Throwable throwable) {
      logger.debug(o, throwable);
   }

   public void info(Object o) {
      logger.info(o);
   }

   public void info(Object o, Throwable throwable) {
      logger.info(o, throwable);
   }

   public void warn(Object o) {
      if (listener != null) {
         listener.warn(o);
      }
      logger.warn(o);
   }

   public void warn(Object o, Throwable throwable) {
      if (listener != null) {
         listener.warn(o, throwable);
      }
      logger.warn(o, throwable);
   }

   public void error(Object o) {
      if (listener != null) {
         listener.error(o);
      }
      logger.error(o);
   }

   public void error(Object o, Throwable throwable) {
      if (listener != null) {
         listener.error(o, throwable);
      }
      logger.error(o, throwable);
   }

   public void fatal(Object o) {
      if (listener != null) {
         listener.fatal(o);
      }
      logger.fatal(o);
   }

   public void fatal(Object o, Throwable throwable) {
      if (listener != null) {
         listener.fatal(o, throwable);
      }
      logger.fatal(o, throwable);
   }

   public void setListener(LogListener listener) {
      this.listener = listener;
   }

}
