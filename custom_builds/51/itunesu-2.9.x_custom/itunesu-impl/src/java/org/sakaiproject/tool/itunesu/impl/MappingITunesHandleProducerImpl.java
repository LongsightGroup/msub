/**
 * 
 */
package org.sakaiproject.tool.itunesu.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.itunesu.api.ITunesHandleProducer;

/**
 * @author lance
 * 
 */
public class MappingITunesHandleProducerImpl implements ITunesHandleProducer
{
	private static final Log LOG = LogFactory
			.getLog(MappingITunesHandleProducerImpl.class);

	/** keys to pull handles from sakai.properties */
	private final String ITUNESU_COURSE_HANDLE_COUNT = "iTunesUCourseSectionCount";
	private final String COURSE_HANDLE_KEY_PREFIX = "course.key.";
	private final String COURSE_HANDLE_VALUE_PREFIX = "course.handle.";
	
	/**
	 * All Sites of type "project" will map to a single iTunes handle.
	 */
	private String projectTypeHandle = null;

	/**
	 * All Sites of type "portfolio" will map to a single iTunes handle.
	 */
	private String portfolioTypeHandle = null;

	/**
	 * The regex pattern to match when matching on site ids.
	 */
	private String siteIdMatchPattern = "^(\\w{2}\\d{2}).*";

	/**
	 * Config option that declares which Match groups to return in key
	 */
	private int[] siteIdMatchGroups = { 1 };

	/**
	 * Pattern will be compiled during init() and reused
	 */
	private Pattern siteIdPattern = null;

	/**
	 * Map of course sites to appropriate iTunes U handles
	 */
	private Map<String, String> handleToSiteMap = null;

	public void init()
	{
		// Pull course map from sakai.properties to populate Map
		if (handleToSiteMap == null)
		{
			handleToSiteMap = new HashMap<String, String>();

			final int handleMapCount = ServerConfigurationService.getInt(ITUNESU_COURSE_HANDLE_COUNT, 0);
			
			for (int i = 1; i <= handleMapCount; i++)
			{
				String courseKey = ServerConfigurationService.getString(COURSE_HANDLE_KEY_PREFIX + i);
				String courseValue = ServerConfigurationService.getString(COURSE_HANDLE_VALUE_PREFIX + i);
				
				handleToSiteMap.put(courseKey, courseValue);
			}
		}

		// init Pattern match
		siteIdPattern = Pattern.compile(siteIdMatchPattern);
	}

	public void destroy()
	{
		LOG.debug(this + " destroy()");		
	}
	
	/**
	 * @see org.sakaiproject.tool.itunesu.api.ITunesHandleProducer#getITunesHandle()
	 */
	public String getITunesHandle()
	{
		String handle = null;
		Site site = getSite();
		if (SiteType.COURSE.toString().equals(site.getType()))
		{
			final String handleKey = getKeyToHandleMap(siteIdMatchGroups);
			handle = handleToSiteMap.get(handleKey);
		}
		if (SiteType.PROJECT.toString().equals(site.getType()))
			handle = projectTypeHandle;
		if (SiteType.PORTFOLIO.toString().equals(site.getType()))
			handle = portfolioTypeHandle;
		return handle;
	}

	private String getKeyToHandleMap(int[] matcherGroups)
	{
		String rv = "";
		final Site site = getSite();
		if (site == null)
			throw new IllegalStateException("Site == null!");
		
		final String id = site.getId();
		Matcher matcher = siteIdPattern.matcher(id);
		
		if (matcher.matches())
		{
			for (int i = 0; i < matcherGroups.length; i++)
			{
				rv += matcher.group(matcherGroups[i]);
			}
		}
		if ("".equals(rv))
			rv = null;
		return rv;
	}

	/**
	 * @return
	 * 		Site object for current site
	 */
	private Site getSite()
	{
		Site site = null;
		try
		{
			Placement placement = ToolManager.getCurrentPlacement();
			final String siteId = placement.getContext();
			site = SiteService.getSite(siteId);
		} catch (IdUnusedException e)
		{
			LOG.error(e.getMessage(), e);
		}
		return site;
	}

	/**
	 * @param handleToSiteMap
	 *            the handleToSiteMap to set
	 */
	public void setHandleToSiteMap(Map<String, String> handleToSiteMap)
	{
		LOG.debug("setHandleToSiteMap(Map<String, String> " + handleToSiteMap
				+ ")");
		this.handleToSiteMap = handleToSiteMap;
	}

	/**
	 * @param projectTypeHandle
	 *            the projectTypeHandle to set
	 */
	public void setProjectTypeHandle(String projectTypeHandle)
	{
		LOG.debug("setProjectTypeHandle(String " + projectTypeHandle + ")");
		if (projectTypeHandle == null || "".equals(projectTypeHandle))
			throw new IllegalArgumentException(
					"projectTypeHandle cannot be null or empty!");
		this.projectTypeHandle = projectTypeHandle;
	}

	/**
	 * @param portfolioTypeHandle
	 *            the portfolioTypeHandle to set
	 */
	public void setPortfolioTypeHandle(String portfolioTypeHandle)
	{
		LOG.debug("setPortfolioTypeHandle(String " + portfolioTypeHandle + ")");
		if (portfolioTypeHandle == null || "".equals(portfolioTypeHandle))
			throw new IllegalArgumentException(
					"portfolioTypeHandle cannot be null or empty!");
		this.portfolioTypeHandle = portfolioTypeHandle;
	}

	/**
	 * @param siteIdMatchPattern
	 *            the siteIdMatchPattern to set
	 */
	public void setSiteIdMatchPattern(String siteIdMatchPattern)
	{
		this.siteIdMatchPattern = siteIdMatchPattern;
	}

	/**
	 * @param siteIdMatchGroups
	 *            the siteIdMatchGroups to set
	 */
	public void setSiteIdMatchGroups(int[] siteIdMatchGroups)
	{
		this.siteIdMatchGroups = siteIdMatchGroups;
	}

}
