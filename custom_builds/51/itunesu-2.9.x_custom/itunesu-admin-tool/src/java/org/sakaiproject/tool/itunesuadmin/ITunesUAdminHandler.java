package org.sakaiproject.tool.itunesuadmin;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Vector;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.itunesu.api.*;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

import org.sakaiproject.tool.itunesu.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author 
 *
 */
public class ITunesUAdminHandler {
	
    /** Our log (commons). */
    private static Log M_log = LogFactory.getLog(ITunesUAdminHandler.class);
	
    public Site site = null;
    public SiteService siteService = null;
    public AuthzGroupService authzGroupService = null;
    public ToolManager toolManager = null;
    public SessionManager sessionManager = null;
    public ServerConfigurationService serverConfigurationService;
    public String memberList = "";
    public boolean update = false;
    public boolean done = false;
    
    private String NULL_STRING = "";
    
    private final String TOOL_CFG_FUNCTIONS = "functions.require";
    private final String MERGE_SITE = "merge.site";

    // Tool session attribute name used to schedule a whole page refresh.
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh"; 
	
	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}
	
	private ITunesUService itunesuService = null;
	
	public ITunesUService getItunesuService() {
		return itunesuService;
	}

	public void setItunesuService(ITunesUService itunesuService) {
		this.itunesuService = itunesuService;
	}

	// the lookup site id
	private String lookupSiteId;
	
	
	public String getLookupSiteId() {
		return lookupSiteId;
	}

	public void setLookupSiteId(String lookupSiteId) {
		this.lookupSiteId = lookupSiteId;
	}
	
	// the result of lookup base on site id
	private String lookupHandle;
	

	public String getLookupHandle() {
		return lookupHandle;
	}

	public void setLookupHandle(String lookupHandle) {
		this.lookupHandle = lookupHandle;
	}

	// the target site id
	private String targetSiteId;
	
	public String getTargetSiteId() {
		return targetSiteId;
	}

	public void setTargetSiteId(String targetSiteId) {
		this.targetSiteId = targetSiteId;
	}
	
	// the source site id
	private String sourceSiteId;
	
	public String getSourceSiteId() {
		return sourceSiteId;
	}
	
	public void setSourceSiteId(String sourceSiteId) {
		this.sourceSiteId = sourceSiteId;
	}
	
	public String deleteSiteIds = null;
	
	public String getDeleteSiteIds() {
		return deleteSiteIds;
	}

	public void setDeleteSiteIds(String deleteSiteIds) {
		this.deleteSiteIds = deleteSiteIds;
	}
	
	// the result of find orphanedPage
	private HashMap<String, String> orphanedPages;
	public HashMap<String, String> getOrphanedPages() {
		return orphanedPages;
	}
	public void setOrphanedPages(HashMap<String, String> orphanedPages) {
		this.orphanedPages = orphanedPages;
	}
	
	/**
	 * The course handle for adding feed group
	 */
	public String addFeedGroup_siteId;
	
	public String getAddFeedGroupSiteId() {
		return addFeedGroup_siteId;
	}

	public void setAddFeedGroupSiteId(String addFeedGroup_siteId) {
		addFeedGroup_siteId = addFeedGroup_siteId;
	}
	
	/**
	 * The name for feed group
	 */
	public String addFeedGroup_name;
	
	public String getAddFeedGroupName() {
		return addFeedGroup_name;
	}

	public void setAddFeedGroupName(String addFeedGroupName) {
		addFeedGroup_name = addFeedGroupName;
	}
	
	/**
	 * the feed url for feed group
	 */
	public String addFeedGroup_url;

	public String getAddFeedGroupUrl() {
		return addFeedGroup_url;
	}

	public void setAddFeedGroupUrl(String addFeedGroupUrl) {
		addFeedGroup_url = addFeedGroupUrl;
	}
	
	/**
	 * The owner email address for feed group
	 */
	public String addFeedGroup_ownerEmail;
	
	public String getAddFeedGroupOwnerEmail() {
		return addFeedGroup_ownerEmail;
	}

	public void setAddFeedGroupOwnerEmail(String addFeedGroupOwnerEmail) {
		addFeedGroup_ownerEmail = addFeedGroupOwnerEmail;
	}

	/**
	 * reset the variables
	 */
	public void resetParams()
	{
		targetSiteId = "";
		sourceSiteId = "";
		deleteSiteIds = "";
		lookupSiteId = "";
		lookupHandle = "";
		addFeedGroup_siteId = "";
		addFeedGroup_name = "";
		addFeedGroup_url = "";
		addFeedGroup_ownerEmail = "";
	}
    
    /**
     * Initialization method, just gets the current site in preperation for other calls
     *
     */
    public void init() {
        
    }
    
    /**
     * lookup course
     *
     */
    public String processLookupCourse() {
    	
    	if (lookupSiteId == null || lookupSiteId.length() == 0)
    	{
    		// alert for missing source site id
    		messages.addMessage(new TargettedMessage("lookup_site_id_missing", null, TargettedMessage.SEVERITY_ERROR));
    	}
    	else
    	{
    		lookupHandle = itunesuService.getITunesUCourseHandle(lookupSiteId, "");
    	}
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    /**
     * lookup course
     *
     */
    public String processLookupOrphanedPage() 
	{
		orphanedPages = itunesuService.getOrphanedPages();
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    /**
     * add feed group
     *
     */
    public String processAddFeedGroup() 
	{	
		// find site id
		String siteName = "";
		try
		{
			Site s = siteService.getSite(addFeedGroup_siteId);
			siteName = s.getTitle();
			
			// delete from iTunesU server
			String courseHandle = itunesuService.getITunesUCourseHandle(addFeedGroup_siteId, siteName);
			
			// delete from iTunesU server
			String uploadURL = itunesuService.getUploadURL(courseHandle);
		
			// remove course page
			String xmlDocument = itunesuService.getAddFeedGroupXml(courseHandle, addFeedGroup_name, addFeedGroup_url, addFeedGroup_ownerEmail);
			String operation = itunesuService.WS_ADD_GROUP;
			itunesuService.wsCall(operation, uploadURL, xmlDocument);
			M_log.info(this + ": processAddFeedGroup add feed group for course handle " + courseHandle);
			
		}
		catch (Exception e)
		{
			M_log.warn(this + ": processAddFeedGroup problem with finding itunesu page from site id=" + addFeedGroup_siteId);
		}
		
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    
    /**
     * delete course
     *
     */
    public String processDeleteCourse() {
    	// get the site ids for deleting iTunesU page
		int i;
		Vector<String> dList = new Vector<String>();
		HashSet<String> existingUsers = new HashSet<String>();

		// string for delete ids
		String deleteSiteIdsString = StringUtil.trimToNull(deleteSiteIds);

		// if there is no eid or nonOfficialAccount entered
		if (deleteSiteIdsString == null) {
			messages.addMessage(new TargettedMessage("delete.siteids.missing", null, TargettedMessage.SEVERITY_ERROR));
		}
		else
		{
			String[] deleteSiteIdsArray = deleteSiteIdsString.split("\r\n");
			HashMap<String, String> siteHandles = itunesuService.getITunesUCourseHandle();

			for (i = 0; i < deleteSiteIdsArray.length; i++) {
				String siteId = StringUtil.trimToNull(deleteSiteIdsArray[i].replaceAll("[\t\r\n]", ""));
				String siteName = "";
				
				try
				{
					Site s = siteService.getSite(siteId);
					siteName = s.getTitle();
					
					// remove itunesu tool from the site
					List<SitePage> pages = s.getPages();
					SitePage removePage = null;
					if (pages != null && !pages.isEmpty())
					{
						for (int index = 0; index < pages.size() && removePage == null; index++)
						{
							SitePage page = pages.get(index);
							
							// found home page, add all tool ids to return value
							for(ToolConfiguration tConfiguration : (List<ToolConfiguration>) page.getTools())
							{
								if ("sakai.iTunesU".equals(tConfiguration.getToolId()))
								{
									removePage = page;
									break;
								}
							}
						}
					}
					if (removePage != null)
					{
						s.removePage(removePage);
						siteService.save(s);
					}
					
				}
				catch (Exception e)
				{
					M_log.warn(this + ": processDeleteCourse problem with delete itunesu tool page from site id=" + siteId);
				}
	    		
				// delete from iTunesU server
	    		String courseHandle = siteHandles.containsKey(siteId)?siteHandles.get(siteId):null;
	    		if (StringUtil.trimToNull(courseHandle) != null)
	    		{
		    		String uploadURL = itunesuService.getUploadURL(courseHandle);
		    	
		    		// remove course page
		    		String xmlDocument = itunesuService.getDeleteCourseXml(courseHandle);
		    		String operation = itunesuService.WS_DELETE_COURSE;
					itunesuService.wsCall(operation, uploadURL, xmlDocument);
					M_log.info(this + ": processDeleteCourse removed iTunesU page for site id=" + siteId);
	    		}
	    		else
	    		{
	    			M_log.info(this + ": processDeleteCourse Cannot find itunesu handle for site id=" + siteId);
	    		}
				
			}
		}
		
		deleteSiteIds = "";
    	
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
    
    /**
     * merge course
     *
     */
    public String processMergeCourse() {
    	
    	if (sourceSiteId == null || sourceSiteId.length() == 0)
    	{
    		// alert for missing source site id
    		messages.addMessage(new TargettedMessage("source_site_id_missing", null, TargettedMessage.SEVERITY_ERROR));
    	}
    	else if (targetSiteId == null || targetSiteId.length() == 0)
    	{
    		// alert for missing target site id
    		messages.addMessage(new TargettedMessage("target_site_id_missing", null, TargettedMessage.SEVERITY_ERROR));
    	} 
    	else
    	{
    		
    	}
        ToolSession session = sessionManager.getCurrentToolSession();
        session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);

        return "done";
    }
}

