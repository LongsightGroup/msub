package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 5/19/11
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class SectionCmImpl implements Section, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String category;
	private Set<Meeting> meetings;
	private CourseOffering courseOffering;
	private String courseOfferingEid; // note from Josh - save for review later:
									  // We keep this here to avoid lazy
										// loading of the courseOffering
	private Section parent;
	private EnrollmentSet enrollmentSet;
	private Integer maxSize;
	private String title;
	private String eid;
	private String description;
	private String createdBy;
	private Date createdDate;
	private String lastModifiedBy;
	private Date lastModifiedDate;
	private String Authority;


	public SectionCmImpl() {
	}

	public SectionCmImpl(String eid, String title, String description,
			String category, Section parent, CourseOffering courseOffering,
			EnrollmentSet enrollmentSet, Integer maxSize) {
		this.eid = eid;
		this.title = title;
		this.description = description;
		this.category = category;
		this.parent = parent;
		this.courseOffering = courseOffering;
		if (courseOffering != null) {
			this.courseOfferingEid = courseOffering.getEid();
		}
		this.enrollmentSet = enrollmentSet;
		this.maxSize = maxSize;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public CourseOffering getCourseOffering() {
		return courseOffering;
	}

	public String getCourseOfferingEid() {
		return courseOfferingEid;
	}

	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
		if (courseOffering == null) {
			this.courseOfferingEid = null;
		} else {
			this.courseOfferingEid = courseOffering.getEid(); // Make sure we
																// update the
																// cached eid
		}
	}

	public Section getParent() {
		return parent;
	}

	public void setParent(Section parent) {
		this.parent = parent;
	}

	public EnrollmentSet getEnrollmentSet() {
		return enrollmentSet;
	}

	public void setEnrollmentSet(EnrollmentSet enrollmentSet) {
		this.enrollmentSet = enrollmentSet;
	}

	public Set<Meeting> getMeetings() {
		return meetings;
	}

	public void setMeetings(Set<Meeting> meetings) {
		this.meetings = meetings;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setCourseOfferingEid(String courseOfferingEid) {
		this.courseOfferingEid = courseOfferingEid;
	}

	public String getAuthority() {
		return Authority;
	}

	public void setAuthority(String authority) {
		Authority = authority;
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



}