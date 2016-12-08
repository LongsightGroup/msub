/**
 * 
 */
package com.rsmart.turningpoint.impl.hibernate;

/**
 * @author dirk
 *
 */
public class TPCourseBasedDeviceIdMapping extends TPDeviceIdMapping {
	/** The ID of the site/course to which this mapping applies */
	private String siteId;

	protected TPCourseBasedDeviceIdMapping() {
		/** Supplied for use by Hibernate */
		super();
	}

	public TPCourseBasedDeviceIdMapping(final String externalDeviceId) {
		super(externalDeviceId);
	}

	public TPCourseBasedDeviceIdMapping(final String siteId, final String externalDeviceId) {
		super(externalDeviceId);
		this.siteId = siteId;
	}

	/**
	 * Returns the ID of the site/course to which this mapping applies
	 * 
	 * @return the ID of the site/course to which this mapping applies
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * Sets the ID for the site/course to which this mapping applies
	 * 
	 * @param siteId The site ID
	 */
	public void setSiteId(final String siteId) {
		this.siteId = siteId;
	}
	
	@Override
	public boolean isAllCourses() {
		return false;
	}
}
