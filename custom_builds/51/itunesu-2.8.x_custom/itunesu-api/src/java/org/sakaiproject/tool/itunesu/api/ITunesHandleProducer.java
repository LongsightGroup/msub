/**
 * 
 */
package org.sakaiproject.tool.itunesu.api;

/**
 * A simple pluggable interface that returns the appropriate iTunes handle for
 * the Sakai Site (i.e. tool placement)
 * 
 * @author lance
 * 
 */
public interface ITunesHandleProducer
{
	/**
	 * @return the appropriate iTunes handle for the Sakai Site (i.e. tool
	 *         placement)
	 */
	public String getITunesHandle();
}
