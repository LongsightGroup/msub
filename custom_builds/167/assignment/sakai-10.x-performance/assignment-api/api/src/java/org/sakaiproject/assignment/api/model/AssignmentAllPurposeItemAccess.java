/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/assignment/assignment-api/api/src/java/org/sakaiproject/assignment/api/model/AssignmentAllPurposeItemAccess.java $
 * $Id: AssignmentAllPurposeItemAccess.java 18770 2010-01-21 04:53:30Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.api.model;

/**
 * the access string(role id or user id) for AssignmentAllPurposeItem
 * @author zqian
 *
 */
public class AssignmentAllPurposeItemAccess {
	private Long id;
	private String access;
	private AssignmentAllPurposeItem assignmentAllPurposeItem;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAccess() {
		return access;
	}
	public void setAccess(String access) {
		this.access = access;
	}
	public AssignmentAllPurposeItem getAssignmentAllPurposeItem()
	{
		return this.assignmentAllPurposeItem;
	}
	public void setAssignmentAllPurposeItem(AssignmentAllPurposeItem assignmentAllPurposeItem)
	{
		this.assignmentAllPurposeItem = assignmentAllPurposeItem;
	}
	
	public AssignmentAllPurposeItemAccess() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
