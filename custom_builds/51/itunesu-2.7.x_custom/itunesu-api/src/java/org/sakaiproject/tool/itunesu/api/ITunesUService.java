package org.sakaiproject.tool.itunesu.api;

import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;

public interface ITunesUService {

	public static final String WS_ADD_COURSE = "AddCourse";

	public static final String WS_ADD_PERMISSION = "AddPermission";

	public final String WS_ADD_GROUP = "AddGroup";

	public final String WS_DELETE_COURSE = "DeleteCourse";
	
	public String getIdentityString(String displayName, String emailAddress, String username, String userIdentifier);

	public String getCredentialsString(String[] credentialsArray);

	public String getAuthorizationToken(String credentials, String identity, Date now, byte[] key);

	public byte[] getBytes(String sharedSecret, String string);

	public Map<String, String> getUserRoleForAllSites();

	public String invokeAction(String url, String token);
	
	/**
	 * Service configuration option:
	 * true = create custom permissions for each individual iTunes site 
	 * 			(sent as part of iTunes U course creation)
	 * false = do not create custom permissions per iTunes site (i.e. inherit from iTunes section)
	 * To configure this option see sakai.properties (e.g.):
	 * customItunesPermissionsPerSite@org.sakaiproject.tool.itunesu.api.ITunesUService=false
	 */
	public boolean isCustomItunesPermissionsPerSite();

	/**
	 * @return the appropriate iTunes parent handle for the Sakai Site (i.e. tool
	 *         placement)
	 */
	public String getITunesParentHandle();
	
	/**
	 * Service configuration option:
	 * true = save iTunes U handle in tool placement
	 * false = get iTunes U handle from ShowTree XML file from iTunes U
	 * To configure this option see sakai.properties (e.g.):
	 * saveHandleInTool@org.sakaiproject.tool.itunesu.api.ITunesUService=false
	 */
	 public boolean isSaveHandleInTool();
	
	 /**
	  * Service configuration option:
	  * true = use Identifier tag with 'most' ShowTree XML file to determine iTunes U handle
	  * false = use Digester with 'minimal' ShowTree XML file to determine iTunes U handle 
	  * To configure this option see sakai.properties (e.g.):
	  * matchOnIdentifier@org.sakaiproject.tool.itunesu.api.ITunesUService=false
	  */
	  public boolean isMatchIdentifier();
	
	  /**
	   * Service configuration option:
	   * true = will construct credentials from all sakai sites user member of to pass to iTunes U
	   * false = only construct credential for current sakai site
	   * To configure this option see sakai.properties (e.g.):
	   * credentialsForAllSites@org.sakaiproject.tool.itunesu.api.ITunesUService=false
	   */
	  public boolean getCredentialsForAllSites();
	  
	  /**
	   * Get the credential string for current user. If the overrideWithAdmin is set to be true, Admin credential will be used instead.
	   * In addition, allSites used to construct the credential array for all sites user is a part of if true
	   * @param overrideWithAdmin
	   * @param allSites
	   * @return
	   */
	  public Hashtable<String, String> getITunesUCreds(boolean overrideWithAdmin, boolean allSites);
	  
	  /**
	     * client-software based interaction with the indigo repository 
	     * @param request
	     * @param fileName
	     * @param prefix
	     * @param destination
	     * @param token
	     */
		public String getUploadURL(String handle, String prefix, String destination, String token);
		
		 /**
	     * Is the iTunesU Course exist? If yes, return the course handle
	     * @param prefix
	     * @param destination
	     * @param token
	     * @return
	     */
	    public String getITunesUCourseHandle(String siteId, String siteName, String prefix, String destination, String token);
	    
	    /**
	     * send WS requestion to server
	     * @param uploadURL
	     * @param xmlDocument
	     * @param prefix
	     * @param destination
	     * @param token
	     * @return
	     */
	    public String wsCall(String function, String uploadURL, String xmlDocument, String prefix, String destination, String token);
	    
	    /**
		 * generate the DeleteCourse xml file
		 * 	<?xml version="1.0" encoding="UTF-8"?>
		 *  <ITunesUDocument>
		 *     <Version>1.1.3</Version>
		 *     <DeleteCourse>
		 *        <CourseHandle>coursehandle value</CourseHandle>
		 *        <CoursePath>coursepath value</CoursePath>
		 *        </DeleteCourse>
		 *     </ITunesUDocument>
		 *     CourseHandle: Specifies the handle in your iTunes U site for the course you want to delete. 
		 *     The course handle must correspond to a Course page object in your iTunes U site. 
		 *     If the handle does not match an existing object, iTunes U returns an error message. 
		 *     If you do not specify the CourseHandle entity, iTunes U uses the destination string in your URL.
		 *     CoursePath. Specifies the path in your iTunes U site for the course you want to delete. 
		 *     The course path must correspond to a Course page object in your iTunes U site. 
		 *     If the path does not match an existing object, or matches multiple objects, iTunes U returns an error message. 
		 *     If you do not specify the CoursePath entity, iTunes U uses the destination string in your URL. 
		 *     The CoursePath entity must be in the form “a/b/c” where a, b, and c are folder names. 
		 *     For example, if you have a site named “CupertinoU” containing a section named “Humanities” and a course in that section named “Literature,” 
		 *     you can refer to the CoursePath as <CoursePath>CupertinoU/Humanities/Literature</CoursePath> in your DeleteCourse operation. 
		 *     If specifying the root path, you must start the CoursePath entity with a leading slash (/). 
		 *     To specify a slash (/) as a character within a node of the slash-delimited path, escape the character with a second slash (//).
		 *     
		 * @param courseHandle the course handle
		 *
		 */
	    public String getDeleteCourseXml(String courseHandle);
	    
		/**
		 * get AddPermission xml file
		 * Specifies a permission you want to add to a section, course, or group in your iTunes U site.
		 * When you specify this operation, iTunes U adds the permission with the specified credential string and access level (No Access, Download, Drop Box, Shared, Edit) to your iTunes U site.
		 * You specify the following entities with the AddPermission operation:
		 *
		 *ParentHandle. Specifies the handle in your iTunes U site where you want to add a permission. 
		 *              The parent handle must correspond to a section, Course page, or Course page group object in your iTunes U site.
		 *ParentPath. Specifies the path in your iTunes U site where you want to add a permission. 
		 *				The parent path must correspond to a section, Course page, or Course page group object in your iTunes U site. 
		 *				The ParentPath entity must be in the form “a/b/c” where a, b, and c are folder names. 
		 *				For example, if you have a site named “CupertinoU” containing a section named “Humanities,” a course named “Literature,” and a group in that course named “Audio,” 
		 *				you can refer to the ParentPath as <ParentPath>CupertinoU/Humanities/Literature/Audio</ParentPath> in your AddPermission operation. 
		 *				If specifying the root path, you must start the ParentPath entity with a leading slash (/). 
		 *				To specify a slash (/) as a character within a node of the slash-delimited path, escape the character with a second slash (//).
		 *Permission. Specifies the entities for the permission attributes you want to add, including the Credential string and Access level.
		 * The following is an example of an iTunes U Web Services document using AddPermission:
		 * 		<?xml version="1.0" encoding="UTF-8"?>
		 * 		<ITunesUDocument>
		 * 			<Version>1.1.3</Version>
		 * 			<AddPermission>
		 * 				<ParentHandle>parenthandle value</ParentHandle>
		 * 				<ParentPath>parentpath value</ParentPath>
		 * 				<Permission>
		 * 					<Credential>credential value</Credential>
		 * 					<Access>access value</Access>
		 * 			</AddPermission>
		 * 		</ITunesUDocument>
		 * @param siteHandle
		 * @param siteId
		 * @return
		 */
		public String getAddPermissionXml(String siteHandle, String siteId);
		
		/**
		 * Specifies a course you want to add to a section in your iTunes U site.
		 * You specify the following entities with the AddCourse operation:
		 * ParentHandle. Specifies the handle for a section in your iTunes U site where you want to add a course. 
		 * 				The parent handle must correspond to an object to which you can add a Course page in your iTunes U site. 
		 * 				For example, currently you can only add a Course page to a section. 
		 * 				If you do not specify the ParentHandle entity, iTunes U uses the destination string in your URL.
		 * ParentPath. Specifies the path for a section in your iTunes U site where you want to add a course. 
		 * 				The parent path must correspond to an object to which you can add a Course page in your iTunes U site. 
		 * 				For example, currently you can only add a Course page to a section. 
		 * 				If you do not specify the ParentPath entity, iTunes U uses the destination string in your URL. 
		 * 				The ParentPath entity must be in the form “a/b/c” where a, b, and c are folder names. 
		 * 				For example, if you have a site named “CupertinoU” containing a section named “Humanities,” you can refer to the ParentPath as <ParentPath>CupertinoU/Humanities</ParentPath> in your AddCourse operation. 
		 * 				If specifying the root path, you must start the ParentPath entity with a leading slash (/). 
		 * 				To specify a slash (/) as a character within a node of the slash-delimited path, escape the character with a second slash (//).
		 * TemplateHandle. Specifies the handle of the template you want to use when adding the course. 
		 * 				The template handle can correspond to a template or Course page object in your iTunes U site. 
		 * 				iTunes U uses the template you specify to provide default values for entities not provided within the Course entity. 
		 * 				For example, if you do not specify a course Name entity, iTunes U uses the Name in the specified template.
		 * Course. Specifies the entities for the course attributes you want to add, including course Name, ShortName, Identifier, Instructor, Description, Group, AllowSubscription, and Permission. 
		 * 				iTunes U adds the new course to the end of the existing courses in the destination section. 
		 * 				The web service course Name entity corresponds to the course Title in your iTunes U site.
		 * Notes:
		 * Specify simple text string values for the Name, ShortName, Identifier, Instructor, and Description entities.
		 * Specify all details for any Group (including Name) or Permission (including Credentials and Access) entities.
		 * The following is an example of an iTunes U Web Services document using AddCourse:
		 * 	<?xml version="1.0" encoding="UTF-8"?>
		 * 	<ITunesUDocument>
		 * 	<Version>1.1.3</Version>
		 * 	<AddCourse>
		 * 		<ParentHandle>parenthandle value</ParentHandle>
		 * 		<ParentPath>parentpath value</ParentPath>
		 * 		<TemplateHandle>templatehandle value</TemplateHandle>
		 * 		<Course>
		 * 			<Name>name value</Name>
		 * 			<ShortName>shortname value</ShortName>
		 * 			<Identifier>identifier value</Identifier>
		 * 			<Instructor>instructor value</Instructor>
		 * 			<Description>description value</Description>
		 * 			<Group>
		 * 				<Name>name value</Name>
		 * 				<Handle>handle value</Handle>
		 * 				<Track>
		 * 					<Name>name value</Name>
		 * 					<Handle>handle value</Handle>
		 * 					<Kind>kind value</Kind>
		 * 					<DiscNumber>discnumber value</DiscNumber>
		 * 					<DurationMilliseconds>durationmilliseconds value
		 * 					</DurationMilliseconds>
		 * 					<AlbumName>albumname value</AlbumName>
		 * 					<ArtistName>artistname value</ArtistName>
		 * 					<DownloadURL>downloadurl value</DownloadURL>
		 * 				</Track>
		 * 				<Permission>
		 * 					<Credential>credential value</Credential>
		 * 					<Access>access value</Access>
		 * 				</Permission>
		 * 			</Group>
		 * 			<AllowSubscription>allowsubscription value</AllowSubscription>
		 * 		</Course>
		 * 	</AddCourse>
		 * </ITunesUDocument>
		 * @param siteId
		 * @return
		 */
		public String getAddCourseXml(String siteId);
		
		/**
		 * If you are hosting content on your local servers, use feed groups to automatically populate track content in a Course page based on the podcast feed URL and details you specify.
		 * To automatically populate track content using feed groups:
		 * 	1. Log in to iTunes U as an administrator or another user with editing access.
   		 * 	2.Navigate to the Course page where you want to add content using a feed group.
   		 * 	3.Click Edit Page in the Tools area.
   		 * 	4.Click the Edit icon in the tab controls section to edit a feed group.
   		 * 	5.Verify Feed is selected in the Type pop-up menu.
   		 * 	6.Provide details about the feed group using the fields and options provided. For more information, see the “To create a feed group” topic in “Creating Group Types.”
   		 * 	7.Click Apply to save changes. Click Revert to revert any changes you made since the last save.
   		 *  8. Click the Save icon in the tab controls section to save the feed group.
   		 *  9. Click Logout.
		 * Note: iTunes U copies relevant information and data from your RSS feed and automatically populates your feed group track content based on the podcast feed URL information and the details you specify. 
		 * When users subscribe to the feed, they are subscribing to the copied information and data, not your RSS feed directly. 
		 * Because iTunes U does not use your your RSS feed directly, iTunes U cannot update Course page track content at the same time the RSS feed is updated. 
		 * If you choose Daily from the “Check for changes” pop-up menu, iTunes U can update the feed group content on a daily basis. If you want to force a feed group update you can:
		 * Send a request to http(s)://deimos.apple.com/WebObjects/Core.woa/PingPodcast/site.name with an id, handle, or feedURL parameter. You can pass the parameter on the query string for an HTTP GET, or pass it as an HTTP POST parameter using a content type of application/x-www-form-urlencoded. For example: https://deimos.apple.com/WebObjects/Core.woa/PingPodcast/itunesu.edu?handle=876546541.
		 * Use the iTunes U UpdateGroup Web Service operation. For more information, see “UpdateGroup.”
		 * As the site administrator, you can also use iTunes U Web Services to create a feed group, including the URL to the feed, and automatically populate track content within a course. 
		 * For more information, see “Adding Content Using iTunes U Web Services” and “AddGroup.”
		 * The following is an example of an iTunes U Web Services document using AddGroup, and specifying that the new group is a feed group:
		 * <pre>
		 * 	<?xml version="1.0" encoding="UTF-8"?>
		 *	<ITunesUDocument>
		 *		<Version>1.1.1</Version>
	  	 *		<AddGroup>
	  	 *			<ParentHandle>876546541</ParentHandle>
	  	 *			<Group>
	  	 *				<Name>Group's Name</Name>
	  	 *				<GroupType>Feed</GroupType>
	  	 *				<ExternalFeed>
	  	 *					<URL>http://www.itunesu.edu/feedpath.xml</URL>
	  	 *					<OwnerEmail>john.doe@email.com</OwnerEmail>
	  	 *					<PollingInterval>Daily</PollingInterval>
	  	 *					<SecurityType>None</SecurityType>
	  	 *					<SignatureType>None</SignatureType>
	  	 *				</ExternalFeed>
	  	 *			</Group>
	  	 * 		</AddGroup>
	  	 *	</ITunesUDocument>
		 * </pre>
		 * 
		 * @param courseHandle
		 * @param feeGroupName
		 * @param feedGroupUrl
		 * @param feedGroupOwnerEmail
		 * @return
		 */
		
		public String getAddFeedGroupXml(String courseHandle, String feeGroupName, String feedGroupUrl, String feedGroupOwnerEmail);
	    
		/**
		 * return false if any of the necessary init parameter is missing
		 * this indicate an error to system administrator to troubleshoot the problem.
		 * @return
		 */
		public boolean missingInitParams();
		
		/**
		 * return list of iTunesU pages that no longer linked to any Sakai sites
		 * @param prefix
		 * @param destination
		 * @param token
		 */
		public HashMap<String, String> getOrphanedPages (String prefix, String destination, String token);
}
