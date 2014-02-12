/**
 * 
 */
package org.sakaiproject.tool.itunesu.impl;

/**
 * @author lance
 * 
 */
public enum SiteType
{
	COURSE("course"), PROJECT("project"), PORTFOLIO("portfolio");

	private final String key;

	SiteType(String value)
	{
		this.key = value;
	}

	@Override
	public String toString()
	{
		return key;
	}

}
