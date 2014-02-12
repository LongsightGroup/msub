/**
 * 
 */
package org.sakaiproject.tool.itunesu.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.itunesu.api.ITunesHandleProducer;
import org.sakaiproject.util.StringUtil;

/**
 * This simple implementation will always return the same, configured, iTunes
 * handle.
 * 
 * @author lance
 * 
 */
public class SimpleITunesHandleProducerImpl implements ITunesHandleProducer
{
	private static final Log LOG = LogFactory
			.getLog(SimpleITunesHandleProducerImpl.class);

	/**
	 * The single iTunes handle that will be returned for all Sites (i.e. tool
	 * placements)
	 */
	private String section_handle = null;

	/**
	 * @see org.sakaiproject.tool.itunesu.api.ITunesHandleProducer#getItunesHandle()
	 */
	public String getITunesHandle()
	{
		LOG.debug("getITunesHandle()");
		if (section_handle == null)
		{
			// TODO migrate setting to property override only (i.e. deprecate
			// "section_handle" key.
			section_handle = StringUtil.trimToNull(ServerConfigurationService
					.getString("section_handle"));
		}
		return section_handle;
	}

	/**
	 * Server configuration option:
	 * Sets the iTunes U Section handle if all iTunes U sites
	 * are housed under one Section.
	 * To configure this option see sakai.properties (e.g.):
	 * section_handle@org.sakaiproject.tool.itunesu.api.ITunesHandleProducer=9999999999
	 * @param section_handle
	 *            the iTunesHandle to set
	 */
	public void setSection_handle(String section_handle)
	{
		if (LOG.isDebugEnabled())
			LOG.debug("setsection_handle(String " + section_handle + ")");
		this.section_handle = section_handle;
	}

}
