/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/tags/1.0/api/src/java/org/sakaiproject/assignment2/exception/AnnouncementPermissionException.java $
 * $Id: AnnouncementPermissionException.java 61480 2009-06-29 18:39:09Z swgithen@mtu.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment2.exception;

/**
 * Indicates that the user's action (such as saving an assignment) required
 * some change to an associated announcement in the Announcements tool, but
 * the user is not authorized to take this action
 *
 * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
 */
public class AnnouncementPermissionException extends AssignmentException {

    private static final long serialVersionUID = 1L;

    public AnnouncementPermissionException(String message) {
        super(message);
    }

    public AnnouncementPermissionException(String msg, Throwable t) {
        super(msg, t);
    }
}
