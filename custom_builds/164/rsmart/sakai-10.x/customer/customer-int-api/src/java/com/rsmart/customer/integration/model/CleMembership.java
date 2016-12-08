/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.model;

/**
 * CLE Membership
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public class CleMembership {

	private String id;
	
	private String courseNumber;

	private String userName;

	private String role;

   private boolean active = true;

   /**
	 * Get ID
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set ID
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Get Cours Number
	 * 
	 * @return String
	 */
	public String getCourseNumber() {
		return courseNumber;
	}

	/**
	 * Set Course Number
	 * 
	 * @param courseNumber
	 */
	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	/**
	 * Get User Name
	 * 
	 * @return String
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set User Name
	 * 
	 * @param userId
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Get Role
	 * 
	 * @return String
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Set Role
	 * 
	 * @param role
	 */
	public void setRole(String role) {
		this.role = role;
	}

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }
}
