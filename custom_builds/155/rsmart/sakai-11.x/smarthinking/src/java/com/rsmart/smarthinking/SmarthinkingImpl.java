/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package com.rsmart.smarthinking;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * Sakai Link Tool.
 * </p>
 * 
 * @author Charles Hedrick, Rutgers University.
 * @version $Revision$
 */
@SuppressWarnings({ "serial", "deprecation" })
public class SmarthinkingImpl extends HttpServlet {
	private static final String UTF8 = "UTF-8";

	private static final String headHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n<head><title>Smarthinking</title>";

	private static final String headHtml1 = "<script type=\"text/javascript\" language=\"JavaScript\">function setFrameHeight(id) { var frame = parent.document.getElementById(id); if (frame) {                var objToResize = (frame.style) ? frame.style : frame; objToResize.height = \"";

	private static final String headHtml2 = "\";  }} </script></head>\n<body onload=\"";

	private static final String headHtml3 = "\" style='margin:0;padding:0;'>";

	private static final String tailHtml = "</body></html>";

	private static final String successfulRequest = "valid-request";
	private static final String failedRequest = "error";

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SmarthinkingImpl.class);

	private Pattern legalKeys;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo() {
		return "rSmart Smarthinking Integration";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *            The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		legalKeys = Pattern.compile("^[a-zA-Z0-9]+$");
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy() {
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to Get requests: display main content by redirecting to it and
	 * adding user= euid= site= role= serverurl= time= sign= for privileged
	 * users, add a bar at the top with a link to the setup screen ?Setup
	 * generates the setup screen
	 * 
	 * @param req
	 *            The servlet request.
	 * @param res
	 *            The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */

	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		// get the Tool
		Placement placement = ToolManager.getCurrentPlacement();
		Properties config = null;
		String placementId = "none";

		if (placement != null) {
			config = placement.getConfig();
			placementId = placement.getId();
		}

		res.setContentType("text/html; charset=utf-8");
		PrintWriter out = res.getWriter();

		String userid = null;
		String euid = null;
		String useremail = null;
		String lastname = null;
		String firstname = null;
		String siteid = null;
		String sessionid = null;
		String url = null;
		String element = null;
		// String oururl = req.getRequestURI();
		// String query = req.getQueryString();

		boolean isAnon = false;

		// set frame height

		StringBuffer bodyonload = new StringBuffer();
		if (placement != null) {
			element = Web.escapeJavascript("Main" + placement.getId());
			bodyonload.append("setFrameHeight('" + element + "');");
		}

		// prepare the data for the redirect

		// we can always get the userid from the session
		Session s = SessionManager.getCurrentSession();
		if (s != null && s.getUserId() != null) {
			User user = UserDirectoryService.getCurrentUser();
			M_log.debug("got session " + s.getId());
			userid = user.getId();
			euid = s.getUserEid();
			useremail = user.getEmail();
			lastname = user.getLastName();
			firstname = user.getFirstName();
			sessionid = s.getId();
		} else {
			// No valid user session
			User anon = UserDirectoryService.getAnonymousUser();
			userid = anon.getId();
			euid = anon.getEid();
			useremail = anon.getEmail();
			lastname = anon.getLastName();
			firstname = anon.getFirstName();
			isAnon = true;
		}

		if (userid != null && (euid == null || "".equals(euid)))
			euid = userid;

		// site is there only for tools, otherwise have to use user's arg
		// this is safe because we verify that the user has a role in site
		if (placement != null)
			siteid = placement.getContext();
		if (siteid == null)
			siteid = req.getParameter("site");

		// if user has asked for a url, use it
		url = req.getParameter("url");
		// else take it from the tool config
		if (url == null && config != null)
			url = config.getProperty("url", null);

		if (url == null && config != null) {
			String urlProp = config.getProperty("urlProp", null);

			if (urlProp != null) {
				url = ServerConfigurationService.getString(urlProp);
			}
		}

		// now get user's role in site; must be defined
		String realmId = null;
		String rolename = null;

		if (siteid != null) {
			realmId = SiteService.siteReference(siteid);
		}

		if (realmId != null && userid != null && !isAnon) {
			rolename = AuthzGroupService.getUserRole(userid, realmId);
		}

		// Check for .auth or .anon role
		if (rolename == null)
			rolename = isAnon ? AuthzGroupService.ANON_ROLE
					: AuthzGroupService.AUTH_ROLE;

		// sessionid = (sessionid != null) ? encrypt(sessionid) : "";

		// generate redirect, as url?user=xxx&site=xxx

		if (url != null && userid != null && siteid != null && rolename != null
				&& sessionid != null) {

			// command is the thing that will be signed

			StringBuilder command = new StringBuilder();

			command.append("userid=" + URLEncoder.encode(userid, UTF8)
					+ "&useremail=" + URLEncoder.encode(useremail, UTF8)
					+ "&lastname=" + URLEncoder.encode(lastname, UTF8)
					+ "&firstname=" + URLEncoder.encode(firstname, UTF8));

			// pass on any other arguments from the user.
			// but sanitize them to prevent people from trying to
			// fake out the parameters we pass, or using odd syntax
			// whose effect I can't predict

			Map params = req.getParameterMap();
			Set entries = params.entrySet();
			Iterator pIter = entries.iterator();
			while (pIter.hasNext()) {
				Map.Entry entry = (Map.Entry) pIter.next();
				String key = "";
				String value = "";
				try {
					key = (String) entry.getKey();
					value = ((String[]) entry.getValue())[0];
				} catch (Exception e) {
					M_log.debug("Exception getting key/value", e);
				}
				if (legalKeys.matcher(key).matches())
					command.append("&" + key + "="
							+ URLEncoder.encode(value, UTF8));
			}

			// Pass on additional parameters from the tool mode configured url
			// (e.g. http://.../somescript?param=value)

			int param = url.indexOf('?');
			if (param > 0) {
				String extraparams = url.substring(param + 1);
				url = url.substring(0, param);

				String[] plist = extraparams.split("&");
				for (int i = 0; i < plist.length; i++) {
					String[] pval = plist[i].split("=");
					if (pval.length == 2) {
						String key = pval[0];
						String value = pval[1];
						if (legalKeys.matcher(key).matches())
							command.append("&" + key + "="
									+ URLEncoder.encode(value, UTF8));
					}
				}

			}

			try {
				// System.out.println("sign >" + command + "<");

				// signature = sign(command.toString());
				url = url + "?" + command;
				URL surl = new URL(url);
				HttpsURLConnection conn = (HttpsURLConnection) surl
						.openConnection();

				StringWriter writer = new StringWriter();
				IOUtils.copy(conn.getInputStream(), writer, UTF8);

				String output = writer.toString();

				if (output.indexOf(Validator.escapeHtml(successfulRequest)) > 0) {
					url = FormattedText
							.convertFormattedTextToPlaintext(FormattedText
									.unEscapeHtml(output));
					bodyonload.append("window.location = '"
							+ Validator.escapeJsQuoted(Validator
									.escapeHtml(url)) + "';");
				} else if (output.indexOf(Validator.escapeHtml(failedRequest)) > 0) {
					writeErrorPage(
							req,
							out,
							bodyonload.toString(),
							FormattedText
									.convertFormattedTextToPlaintext(FormattedText
											.unEscapeHtml(output)));
					return;
				}

			} catch (Exception e) {
				M_log.debug("Exception communicating to smarthinking", e);
			}

		} else {
			// Cannot generate a correctly signed URL for some reason, so just
			// use the URL as is
			M_log.debug("Cannot generate signed URL for remote application: url="
					+ url
					+ " userid="
					+ userid
					+ " siteid="
					+ siteid
					+ "rolename=" + rolename + " sessionid=" + sessionid);

		}

		// now put out a vestigial web page, whose main functional
		// part is actually the <body onload=

		int height = 600;
		String heights;
		if (config != null) {
			heights = safetrim(config.getProperty("height", "600"));
			if (heights.endsWith("px"))
				heights = safetrim(heights.substring(0, heights.length() - 2));
			// what may be saved might not be a number
			try {
				height = Integer.parseInt(heights);
			} catch (NumberFormatException e) {
				// nothing realy to do
			}
		}

		// now generate the page

		if (placement != null && config != null
				&& SiteService.allowUpdateSite(siteid)) {
			if (writeSmarthinkingPage(req, out, height, url, element))
				return;
		}

		// default output - show the requested application
		out.println(headHtml + headHtml1 + height + "px" + headHtml2
				+ bodyonload + headHtml3);
		out.println(tailHtml);

	}

	/**
	 * Called by doGet to display the main contents. Differs from the default
	 * output in that it adds a bar at the top containing a link to the Setup
	 * option.
	 * 
	 * @param out
	 *            printwriter generating web display
	 * @param height
	 *            height of the window to display
	 * @param url
	 *            url to redirect to
	 * @param element
	 *            Javascript window id
	 */

	private boolean writeSmarthinkingPage(HttpServletRequest req,
			PrintWriter out, int height, String url, String element) {

		String bodyonload = "";

		String sakaiHead = (String) req.getAttribute("sakai.html.head");

		if (url == null)
			return false;

		if (element != null)
			bodyonload = "setFrameHeight('" + element + "');";

		out.println(headHtml + sakaiHead + headHtml1 + (height + 50) + "px"
				+ headHtml2 + bodyonload + headHtml3);
		out.println("<div class=\"portletBody\">");
		out.println("<iframe src=\""
				+ Validator.escapeHtml(url)
				+ "\" height=\""
				+ height
				+ "\" "
				+ "width=\"100%\" frameborder=\"0\" marginwidth=\"0\" marginheight=\"0\" scrolling=\"auto\" style=\"padding: 0.15em 0em 0em 0em;\" />");
		out.println("</div>");
		out.println(tailHtml);

		return true;
	}

	/**
	 * Output a page with an error message on it
	 * 
	 * @param out
	 *            printwriter generating web display
	 * @param element
	 *            Javascript window id
	 * @param error
	 *            the actual error message
	 * @param oururl
	 *            URL for this application
	 */

	private boolean writeErrorPage(HttpServletRequest req, PrintWriter out,
			String element, String error) {

		String bodyonload = "";
		String sakaiHead = (String) req.getAttribute("sakai.html.head");

		if (element != null)
			bodyonload = "setMainFrameHeight('" + element
					+ "');setFocus(focus_path);";
		// "sakai.html.body.onload"

		out.println(headHtml);
		out.println(sakaiHead);
		out.println(headHtml1 + "300px" + headHtml2 + bodyonload + headHtml3);

		out.println("<div class=\"portletBody\"><h3>Error</h3>");

		out.println("<div class=\"alertMessage\">" + error + "</div>");

		out.println("</div>");

		out.println(tailHtml);
		return true;
	}

	/**
	 * Version of trim that won't blow up if fed null
	 * 
	 * @param a
	 *            string
	 */

	private String safetrim(String s) {
		if (s == null)
			return null;
		return s.trim();
	}

}
