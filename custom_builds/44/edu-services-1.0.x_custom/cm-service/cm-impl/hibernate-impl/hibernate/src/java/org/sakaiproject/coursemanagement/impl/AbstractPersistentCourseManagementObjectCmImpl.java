/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/edu-services/branches/edu-services-1.0.x/cm-service/cm-impl/hibernate-impl/hibernate/src/java/org/sakaiproject/coursemanagement/impl/AbstractPersistentCourseManagementObjectCmImpl.java $
 * $Id: AbstractPersistentCourseManagementObjectCmImpl.java 59674 2009-04-03 23:05:58Z arwhyte@umich.edu $
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
import java.util.Date;

public abstract class AbstractPersistentCourseManagementObjectCmImpl implements Serializable {

	public static final String AUTHORITY = "Sakai";
	
	/**
	 * The DB's primary key for this object / record.
	 */
	protected Long key;

	/**
	 * The object instance version for optimistic locking.
	 */
	protected int version;

	/**
	 * The agent (either a user id or a process id) that last modified this object.  
	 */
	protected String lastModifiedBy;
	
	/**
	 * The timestamp when this object was last modified.
	 */
	protected Date lastModifiedDate;
	
	/**
	 * The agent (either a user id or a process id) that created this object.  
	 */
	protected String createdBy;

	/**
	 * The timestamp when this object was created.
	 */
	protected Date createdDate;
	
	public Long getKey() {
		return key;
	}
	public void setKey(Long key) {
		this.key = key;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public String getAuthority() {
		return AUTHORITY;
	}
	
	public void setAuthority(String authority) {
		throw new RuntimeException("You can not change the authority of this CM object.  Authority = " +
				AbstractPersistentCourseManagementObjectCmImpl.AUTHORITY);
	}
}
