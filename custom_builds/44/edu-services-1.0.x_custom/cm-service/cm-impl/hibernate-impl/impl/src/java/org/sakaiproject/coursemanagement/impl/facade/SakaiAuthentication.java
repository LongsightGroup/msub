/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/edu-services/branches/edu-services-1.0.x/cm-service/cm-impl/hibernate-impl/impl/src/java/org/sakaiproject/coursemanagement/impl/facade/SakaiAuthentication.java $
 * $Id: SakaiAuthentication.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.coursemanagement.impl.facade;

import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * An implementation of Authentication that delegates to Sakai's UserDirectoryService.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">jholtzman@berkeley.edu</a>
 *
 */
public class SakaiAuthentication implements Authentication {

	/**
	 * {@inheritDoc}
	 */
	public String getUserEid() {
		return UserDirectoryService.getCurrentUser().getEid();
	}

}
