/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/edu-services/branches/edu-services-1.0.x/cm-service/cm-impl/hibernate-impl/hibernate/src/java/org/sakaiproject/coursemanagement/impl/EnrollmentCmImpl.java $
 * $Id: EnrollmentCmImpl.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.coursemanagement.impl;

import java.io.Serializable;

import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;

public class EnrollmentCmImpl extends AbstractPersistentCourseManagementObjectCmImpl
	implements Enrollment, Serializable {

	private static final long serialVersionUID = 1L;

	private String userId;
	private EnrollmentSet enrollmentSet;
	private String enrollmentStatus;
	private String credits;
	private String gradingScheme;
	private boolean dropped;
	
	public EnrollmentCmImpl() {}
	
	public EnrollmentCmImpl(String userId, EnrollmentSet enrollmentSet, String enrollmentStatus, String credits, String gradingScheme) {
		this.userId = userId;
		this.enrollmentSet = enrollmentSet;
		this.enrollmentStatus = enrollmentStatus;
		this.credits = credits;
		this.gradingScheme = gradingScheme;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public EnrollmentSet getEnrollmentSet() {
		return enrollmentSet;
	}
	public void setEnrollmentSet(EnrollmentSet enrollmentSet) {
		this.enrollmentSet = enrollmentSet;
	}

	public String getCredits() {
		return credits;
	}
	public void setCredits(String credits) {
		this.credits = credits;
	}

	public String getEnrollmentStatus() {
		return enrollmentStatus;
	}
	public void setEnrollmentStatus(String enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}

	public String getGradingScheme() {
		return gradingScheme;
	}
	public void setGradingScheme(String gradingScheme) {
		this.gradingScheme = gradingScheme;
	}

	public boolean isDropped() {
		return dropped;
	}
	public void setDropped(boolean dropped) {
		this.dropped = dropped;
	}	
}
