/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.model;



/**
 * general purpose exception class for virtual classroom errors.
 */
public class VirtualClassroomException extends java.lang.Exception {

   /**
    * default constructor.
    */
   public VirtualClassroomException() {
      // no code necessary
   }

   /**
    * Constructs a new exception with the specified detail message.
    * The cause is not initialized, and may subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
    */
   public VirtualClassroomException(String message) {
      super(message);
   }

   /**
    * constructor.
    */
   public VirtualClassroomException(Throwable cause) {
      super(cause);
   }

   /**
    * Constructs a new exception with the specified detail message and cause.
    * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
    * <br/><br/>
    * @param message  the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
    * @param cause    the cause (which is saved for later retrieval by the Throwable.getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
    */
   public VirtualClassroomException(String message, Throwable cause) {
      super(message, cause);
   }
}
