/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/tags/1.0/api/src/java/org/sakaiproject/assignment2/exception/InvalidGradeForAssignmentException.java $
 * $Id: InvalidGradeForAssignmentException.java 61480 2009-06-29 18:39:09Z swgithen@mtu.edu $
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
 * Indicates that the grade to be saved in the gradebook was invalid
 *
 * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
 */
public class InvalidGradeForAssignmentException extends AssignmentException {

    private static final long serialVersionUID = 6455123022460020730L;

    public InvalidGradeForAssignmentException(String message) {
        super(message);
    }

    public InvalidGradeForAssignmentException(String msg, Throwable t) {
        super(msg, t);
    }
}
