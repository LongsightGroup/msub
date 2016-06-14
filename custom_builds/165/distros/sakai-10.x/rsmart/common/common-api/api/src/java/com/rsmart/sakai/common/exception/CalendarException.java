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
package com.rsmart.sakai.common.exception;



/**
 * general purpose exception class for calendar errors.
 */
public class CalendarException extends RuntimeException {

   /**
    * default constructor.
    */
   public CalendarException() {
      // no code necessary
   }

   /**
    * Constructs a new exception with the specified detail message.
    * The cause is not initialized, and may subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
    */
   public CalendarException(String message) {
      super(message);
   }

   /**
    * constructor.
    */
   public CalendarException(Throwable cause) {
      super(cause);
   }

   /**
    * Constructs a new exception with the specified detail message and cause.
    * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
    * <br/><br/>
    * @param message  the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
    * @param cause    the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public CalendarException(String message, Throwable cause) {
      super(message, cause);
   }
}
