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
 * CLE User
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public class CleUser {

	private String userName;

	private String lastName;

	private String firstName;

	private String emailAddress;

	private String password;

	private String userType;

	private String userId;

    private String property1;
    private String property2;
    private String property3;
    private String property4;
    private String property5;


	/**
	 * 
	 * @return
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * 
	 * @param emailAddress
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * 
	 * @return
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * 
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * 
	 * @return
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * 
	 * @param lastName
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get ID
	 * 
	 * @return
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Set ID
	 * 
	 * @param cleId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
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
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Get User Type
	 * 
	 * @return String
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * Set User Type
	 * 
	 * @param userType
	 */
	public void setUserType(String userType) {
		this.userType = userType;
	}

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(String property2) {
        this.property2 = property2;
    }

    public String getProperty3() {
        return property3;
    }

    public void setProperty3(String property3) {
        this.property3 = property3;
    }

    public String getProperty4() {
        return property4;
    }

    public void setProperty4(String property4) {
        this.property4 = property4;
    }

    public String getProperty5() {
        return property5;
    }

    public void setProperty5(String property5) {
        this.property5 = property5;
    }
}
