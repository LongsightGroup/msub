/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/tags/1.0/api/src/java/org/sakaiproject/assignment2/exception/AssignmentException.java $
 * $Id: AssignmentException.java 48274 2008-04-23 20:07:00Z wagnermr@iupui.edu $
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
 * 
 * Generic AssignmentException
 *
 * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
 *
 */
public class AssignmentException extends RuntimeException {
    protected AssignmentException(String message) {
        super(message);
    }
    protected AssignmentException(Throwable t) {
        super(t);
    }
    protected AssignmentException(String msg, Throwable t) {
        super(msg, t);
    }
}
