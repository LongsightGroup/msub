/**
 * $Id: ExternalSecurity.java 48892 2008-05-12 13:04:07Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/api/src/java/org/sakaiproject/evaluation/logic/externals/ExternalSecurity.java $
 * ExternalSecurity.java - evaluation - May 12, 2008 11:14:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;


/**
 * Handles external security checks
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalSecurity {

   // PERMISSIONS

   /**
    * Check if this user has super admin access in the evaluation system
    * @param userId the internal user id (not username)
    * @return true if the user has admin access, false otherwise
    */
   public boolean isUserAdmin(String userId);

   /**
    * Check if a user has a specified permission within a evalGroupId, primarily
    * a convenience method and passthrough
    * 
    * @param userId the internal user id (not username)
    * @param permission a permission string constant
    * @param evalGroupId the internal unique eval group ID (represents a site or group)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId);

}
