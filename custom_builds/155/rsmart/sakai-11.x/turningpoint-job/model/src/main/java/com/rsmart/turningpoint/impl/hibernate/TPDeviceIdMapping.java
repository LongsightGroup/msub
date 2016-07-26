/**
 * 
 */
package com.rsmart.turningpoint.impl.hibernate;

import java.util.Date;

/**
 * @author dirk
 *
 */
public class TPDeviceIdMapping {
	/** The ID/Primary key for this object. */
	private Long id;

	/** The ID of the Sakai User to which this mapping applies */
	private String userId;

	/** The Device ID/Response Card ID as issued by Turning Tech */
	private String externalDeviceId;
	
	/** The date that this mapping entry was created (for tracking purposes) */
	private Date createDate = new Date();
	
	/** Whether or not this mapping is "soft-deleted" */
	private Boolean deleted = Boolean.FALSE;
	
	protected TPDeviceIdMapping() {
		/** Supplied for use by Hibernate */
	}

	public TPDeviceIdMapping(final String externalDeviceId) {
		this.externalDeviceId = externalDeviceId;
		createDate = new Date();
		deleted = Boolean.FALSE;
	}

	/**
	 * Returns the ID/Primary Key for this object.  Should not be exposed to end users.
	 * 
	 * @return the ID/Primary Key for this object.
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the ID--the primary key--for this object.  Set to protected to allow Hibernate, but not
	 * your average user, to access it.
	 * 
	 * @param id The ID/Primary key to set.
	 */
	protected void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Returns the ID of the Sakai User to which this mapping applies
	 * 
	 * @return The ID of the Sakai User to which this mapping applies
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the ID for the Sakai User to which this mapping applies
	 * @param userId The ID for the Sakai User
	 */
	public void setUserId(final String userId) {
		this.userId = userId;
	}
	
	/**
	 * Returns the Turning Tech-supplied Device/Remote Card ID.
	 * 
	 * @return The Turning Tech-supplied Device/Remote Card ID
	 */
	public String getExternalDeviceId() {
		return externalDeviceId;
	}

	/**
	 * Sets the Device/Remote Card ID for this object.
	 * 
	 * @param externalDeviceId the Device/Remote Card ID to set
	 */
	public void setExternalDeviceId(final String externalDeviceId) {
		this.externalDeviceId = externalDeviceId;
	}

	/**
	 * Returns the date that this object was created
	 * 
	 * @return the date that this object was created
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the date this object was created; visibility set to protected so
	 * that Hibernate can access it, but end users can't.
	 * 
	 * @param createDate The creation date for this object
	 */
	protected void setCreateDate(final Date createDate) {
		this.createDate = Date.class.cast(createDate.clone());
	}

	/**
	 * Returns whether or not this object has been deleted/purged
	 * 
	 * @return whether or not this object has been deleted/purged
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * Sets this object's deleted status.
	 * 
	 * @param deleted The object's deletion status
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isAllCourses() {
		return true;
	}
}
