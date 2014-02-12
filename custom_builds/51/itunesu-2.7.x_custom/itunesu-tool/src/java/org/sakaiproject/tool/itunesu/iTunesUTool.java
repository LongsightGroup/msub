/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.itunesu;

import java.net.URLEncoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

import org.sakaiproject.tool.itunesu.api.ITunesUConstants;
import org.sakaiproject.tool.itunesu.api.ITunesUService;

import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class iTunesUTool extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// added as part of ONC-371 (local IU JIRA bug)
	// refactored how iTunes U handle saved in tool placement from
	// separate ShowTree request to extracting from AddCourse response

	private String siteId = ToolManager.getCurrentPlacement().getContext();

	/** Our log (commons). */
	private static final Log LOG = LogFactory.getLog(iTunesUTool.class);

	private ITunesUService itunesuService = (ITunesUService) ComponentManager.get("org.sakaiproject.tool.itunesu.api.ITunesUService");
	
	private final String ITUNESU_TOOL_MESSAGE_BUNDLE = "org.sakaiproject.tool.itunesu.bundle.Messages";
    private final ResourceLoader rl = new ResourceLoader(ITUNESU_TOOL_MESSAGE_BUNDLE);
    
	/**
	 * respond to an HTTP GET request
	 * 
	 * @param req
	 *            HttpServletRequest object with the client request
	 * @param res
	 *            HttpServletResponse object back to the client
	 * @exception ServletException
	 *                in case of difficulties
	 * @exception IOException
	 *                in case of difficulties
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		dispatch(req, res);
	}

	/**
	 * respond to an HTTP POST request
	 * 
	 * @param req
	 *            HttpServletRequest object with the client request
	 * @param res
	 *            HttpServletResponse object back to the client
	 * @exception ServletException
	 *                in case of difficulties
	 * @exception IOException
	 *                in case of difficulties
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		dispatch(req, res);
	}

	/**
	 * handle get and post communication from the user
	 * 
	 * @param req
	 *            HttpServletRequest object with the client request
	 * @param res
	 *            HttpServletResponse object back to the client
	 */
	public void dispatch(HttpServletRequest req, HttpServletResponse res)
			throws ServletException {
		String rv = StringUtil.trimToNull(handleITunesU());
		
		if (rv != null)
		{
			try {
				res.setContentType("text/html");
				res.setCharacterEncoding("UTF-8");
	
				PrintWriter out = null;
				try {
					out = res.getWriter();
					// output content
					out.write(rv);
	
					out.flush();
				} catch (Throwable ignore) {
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (Throwable ignore) {
						}
					}
				}
	
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Utility method to return byte array as hexadecimal String
	 */
	public static String GetAsHexadecimal(byte[] bytes) {
		StringBuffer s = new StringBuffer(bytes.length * 2);
		int length = bytes.length;
		for (int n = 0; n < length; n++) {
			int number = bytes[n];
			number = (number < 0) ? (number + 256) : number; // shift to
															 // positive
															 // range
			if (number < 16) {
				s.append("0");
				s.append(Integer.toString(number, 16));
			} else {
				s.append(Integer.toString(number, 16));
			}
		}
		return s.toString();
	}

	/**
     * iTunes U identity and credential transmission sample. When your
     * itunes U site is initially created, Apple will send your institution's
     * technical contact a welcome email with a link to an iTunes U page
     * containing the following information, which you will need to customize
     * this method's code for your site: <P><DD><DL><DT><B>
     *
     * Information:</B><DD><CODE>
     *   Site URL</CODE> - The URL to your site in iTunes U. The last
     *                     component of that URL, after the last slash,
     *                     is a domain name that uniquely identifies your
     *                     site within iTunes U.<DD><CODE>
     *   shared secret</CODE> - A secret key known only to you and Apple that
     *                          allows you to control who has access to your
     *                          site and what access they have to it.<DD><CODE>
     *   debug suffix</CODE> - A suffix you can append to your site URL
     *                         to obtain debugging information about the
     *                         transmission of identity and credential
     *                         information from your institution's
     *                         authentication and authorization services
     *                         to iTunes U.<DD><CODE>
     *   administrator credential</CODE> - The credential string to assign
     *                                     to users who should have the
     *                                     permission to administer your
     *                                     iTunes U site.</DL></DD><P><DD>
     *
     * Once you have substitute the information above in this method's code
     * as indicated in the code's comments, this method will connect
     * to iTunes U and obtain from it the HTML that needs to be returned to a
     * user's web browser to have a particular page or item in your iTunes U
     * site displayed to that user in iTunes. You can modify this method to
     * instead output the URL that would need to be opened to have that page
     * or item displayed in iTunes.</DD>
     */
    public String getITunesUPage(String prefix, String destination, String token)
    {
        String url = prefix + "/Browse/" + destination;
        String responsePage = itunesuService.invokeAction(url, token);
        
        // Need to add resizing iframe here since response is coming from
        // iTunes U so we can't put it in the proper place
        // had to create a custom resizing for FF hence the extra parameter
        responsePage += "<script type='text/javascript' language='JavaScript' src='js/frameAdjust.js'></script>";
        responsePage += "\n<script type='text/javascript' language='JavaScript'>\n";
        responsePage += "\t// checking if ff browser since iframe\n\t// does not expand enough\n";
		responsePage += "\tvar agt=navigator.userAgent.toLowerCase();\n";
		responsePage += "\tsetMainFrameHeight(self.name,(agt.indexOf('firefox') != -1));\n</script>";

        return responsePage;
    }

    // added as part of ONC-371 (local IU JIRA bug)
    /**
     * Returns the course handle from the AddCourse response in order
     * to save it in the tool placement
     * @param response  The XML document response from an AddCourse request
     * 						as an InputStream
     * 
     * @return The iTunes U handle as a String
     */
    private String getHandleFromAddCourse(String response) {
    	String rv = null;
 
    	Document doc = Xml.readDocumentFromString(response);
    	if (doc != null)
    	{
	    	NodeList nodes = doc.getElementsByTagName("AddedObjectHandle");
	    	
	    	if (nodes.getLength() > 0) {
	    		Node handleNode = nodes.item(0);
	    		
	    		rv = handleNode.getNodeValue();
	    	}
    	}
    	
    	return rv;
    }
    
    /**
	 * get the xml file ready to request the site structure
	 * @param filename
	 */
   /* private void generateShowTreeXml(String filename) 
	{
		// create xml doc for requery the course information
		Document doc = Xml.createDocument();
		Element root = doc.createElement("ITunesUDocument");
		doc.appendChild(root);
		Element e1 = doc.createElement("ShowTree");
		Element e2 = doc.createElement("KeyGroup");
		Text t = doc.createTextNode("minimal");
		e2.appendChild(t);
		e1.appendChild(e2);
		root.appendChild(e1);
		
		ExternalContext external = FacesContext.getCurrentInstance().getExternalContext();
		ServletContext servletContext = (ServletContext) external.getContext();
		String path = servletContext.getRealPath("/") + filename;
		Xml.writeDocument(doc, path);
	}*/
	
    /**
     * Create a course page on iTunesU server
     * @param prefix
     * @param destination
     * @param token
     */
    void addITunesUCourse(String uploadURL, String prefix, String destination, String token)
    {
    		// get the xml file ready for adding course
    		String addCourseXmlDocument = itunesuService.getAddCourseXml(siteId);
    		
    		// send the upload http request
    		String rvInputString = itunesuService.wsCall(itunesuService.WS_ADD_COURSE, uploadURL, addCourseXmlDocument, prefix, destination, token);
    		
    		if (itunesuService.isSaveHandleInTool())
    		{
    			// Added as part of ONC-371 (local IU JIRA bug)
    			// The response XML document from the AddCourse request contains the course
    			// handle so extract it from there and save it in the tool placement
    			String handle = getHandleFromAddCourse(rvInputString);
    			saveToolITunesUHandle(handle);
    		}
        	// current user
    		User currentUser = UserDirectoryService.getCurrentUser();
    		
    		EventTrackingService.post(EventTrackingService.newEvent(ITunesUConstants.EVENT_ITUNESU_ADD_PAGE, "/site/" + siteId, true));
    		LOG.info(this + ":Add itunesu page for site " + siteId + " by " + currentUser.getDisplayName());
    }

    /**
     * Starting point for processing. Will get handle for iTunes U course
     * and will create iTunes U course if it does not exist
     * @return
     */
	private String handleITunesU()
   	{
		if (!itunesuService.missingInitParams())
		{	
    		// get the admin credential first to check the course existence
			Hashtable<String, String> t = itunesuService.getITunesUCreds(true,false);
    		
    		// parse the result
    		String prefix = t.get("prefix")!=null?(String) t.get("prefix"):"";
    		String destination= t.get("destination")!=null?(String) t.get("destination"):"";
    		String token = t.get("token")!=null?(String) t.get("token"):"";
    		
    		String courseHandle = "";
    		
    		LOG.info(this + ":handleITunesU: get course handle first");
    		// if course handle does not exist (either within tool or in ShowTree XML file),
    		// create the course
    		
    		String siteName = "";
    		try {
    		    Site currentSite = SiteService.getSite(siteId);
    		    siteName = currentSite.getTitle();
    		} catch (IdUnusedException e) {
    		    throw new RuntimeException("Unable to look up the current site by ID [" + siteId +
    		            "]. This can result in the end user being presented with a link to the incorrect iTunesU course page, so handling of this request has been aborted.", e);
    		}
    		
    		if ((courseHandle = itunesuService.getITunesUCourseHandle(siteId, siteName, prefix, destination, token)) == null)
    		{
    			LOG.info(this + ":handleITunesU: cannot find course page, prepare to create one");
    			// get upload url
    			String sectionHandle = itunesuService.getITunesParentHandle();
    			String uploadURL = itunesuService.getUploadURL(sectionHandle, prefix, destination, token);

    			// actual creation of course
    			if (uploadURL != null)
    			{
    				addITunesUCourse(uploadURL, prefix, destination, token);
    			}

    			// now that's its created, let's try to get handle again
        		courseHandle = itunesuService.getITunesUCourseHandle(siteId, siteName, prefix, destination, token);

        		// add the custom permissions to the iTunes site if enabled
        		if(itunesuService.isCustomItunesPermissionsPerSite())
        		{
            		if (courseHandle != null)
            		{
                		// get the xml file ready for adding course
                		String addPermissionXmlDocument = itunesuService.getAddPermissionXml(courseHandle, siteId);
                		
                		// get upload url
                		courseHandle = sectionHandle + "." + courseHandle;
            			uploadURL = itunesuService.getUploadURL(courseHandle, prefix, destination, token);
                		
                		// send the upload http request
                		itunesuService.wsCall(itunesuService.WS_ADD_PERMISSION, uploadURL, addPermissionXmlDocument, prefix, destination, token);
                		LOG.info(this + ":handleITunesU: permission added");
            		}
        		}
    		}
    		

			// Are we supposed to save in tool placement?
			if (itunesuService.isSaveHandleInTool())
			{
				LOG.info("handleITunesU=" + courseHandle);
				saveToolITunesUHandle(StringUtil.trimToNull(courseHandle));
			}
    		
     		// get credentials, this time for all courses if parameter set
   			t = itunesuService.getITunesUCreds(false, itunesuService.getCredentialsForAllSites());
    		
    		// parse the result
    		prefix = t.get("prefix")!=null?(String) t.get("prefix"):"";
    		destination= t.get("destination")!=null?(String) t.get("destination") + "." + courseHandle:"";
    		token = t.get("token")!=null?(String) t.get("token"):"";
    		
    		User currentUser = UserDirectoryService.getCurrentUser();
    		String siteId = ToolManager.getCurrentPlacement().getContext();
    		EventTrackingService.post(EventTrackingService.newEvent(ITunesUConstants.EVENT_ITUNESU_READ, "/site/" + siteId, true));
    		LOG.info(this + ":Access itunesu page for site " + siteId + " by " + currentUser.getDisplayName());
    		
    		return getITunesUPage(prefix, destination, token);
		}
		else
		{
			return rl.getString("missingInitParams");
		}
    		
    }

	/**
	 * Stores the iTunes U course handle for this site in the tool placement for easy retrieval
	 */
	private void saveToolITunesUHandle(String handle)
	{
		if (handle != null)
		{
			Placement placement = ToolManager.getCurrentPlacement();
			placement.getPlacementConfig().setProperty("iTunesUHandle", handle);
			placement.save();
		}
	}
}
