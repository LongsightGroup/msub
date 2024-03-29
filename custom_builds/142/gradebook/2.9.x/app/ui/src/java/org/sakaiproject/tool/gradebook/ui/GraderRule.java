/**********************************************************************************
*
* $Id: GraderRule.java 13692 2009-04-24 16:14:32Z jbush $
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.sakaiproject.tool.gradebook.Permission;

/**
 * UI for the grader rule select menus
 */
public class GraderRule implements Serializable {
	private String gradeOrViewValue;
	private String selectedCategoryId;
	private String selectedSectionUuid;
	private Permission permission;
	
	public GraderRule() {
	}
	
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	public Permission getPermission() {
		return permission;
	}
	
	public void setGradeOrViewValue(String gradeOrViewValue) {
		this.gradeOrViewValue = gradeOrViewValue;
	}
	public String getGradeOrViewValue() {
		return gradeOrViewValue;
	}
	
	public void setSelectedCategoryId(String selectedCategoryId) {
		this.selectedCategoryId = selectedCategoryId;
	}
	public String getSelectedCategoryId() {
		return selectedCategoryId;
	}
	
	public void setSelectedSectionUuid(String selectedSectionUuid) {
		this.selectedSectionUuid = selectedSectionUuid;
	}
	public String getSelectedSectionUuid() {
		return selectedSectionUuid;
	}

}