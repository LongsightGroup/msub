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

import java.util.Date;

/**
 * SIS Course
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public class CleCourse {

	private String courseNumber;

	private String term;

	private Date startDate;

	private String title;

	private String shortDescription;

	private String description;

	private Boolean published;

	private Date endDate;

	private String masterSite;

   private String contactName;

   private String contactEmail;

    private String property1;
    private String property2;
    private String property3;
    private String property4;
    private String property5;


   /**
	 * Get Course Num
	 * 
	 * @return String
	 */
	public String getCourseNumber() {
		return courseNumber;
	}

	/**
	 * Set Course Num
	 * 
	 * @param courseNumber
	 */
	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	/**
	 * Get Desc
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set Desc
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * 
	 * @param endDate
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * 
	 * @return
	 */
	public Boolean getPublished() {
		return published;
	}

	/**
	 * 
	 * @param published
	 */
	public void setPublished(Boolean published) {
		this.published = published;
	}

	/**
	 * 
	 * @return
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * 
	 * @param shortDescription
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	/**
	 * 
	 * @return
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * 
	 * @param startDate
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * 
	 * @return
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * 
	 * @param term
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public String getMasterSite() {
		return masterSite;
	}

	public void setMasterSite(String masterSite) {
		this.masterSite = masterSite;
	}

   public String getContactName() {
      return contactName;
   }

   public void setContactName(String contactName) {
      this.contactName = contactName;
   }

   public String getContactEmail() {
      return contactEmail;
   }

   public void setContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
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
