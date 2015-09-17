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
package com.rsmart.sakai.common.i18n;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;



/**
 *
 */
public class LocalizableRuntimeException extends RuntimeException {

   public LocalizableRuntimeException() {
      super();
   }

   public String getLocalizedMessage() {
      Locale locale = LocaleContextHolder.getLocale();
      // TODO impl this
      return super.getLocalizedMessage();
   }

   public LocalizableRuntimeException(String message) {
      super(message);
   }

   public LocalizableRuntimeException(Throwable cause) {
      super(cause);
   }

   public LocalizableRuntimeException(String message, Throwable cause) {
      super(message, cause);
   }
}
