package org.sakaiproject.login.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

import com.lastpass.saml.AttributeSet;
import com.lastpass.saml.IdPConfig;
import com.lastpass.saml.SAMLClient;
import com.lastpass.saml.SAMLException;
import com.lastpass.saml.SAMLInit;
import com.lastpass.saml.SAMLUtils;
import com.lastpass.saml.SPConfig;

public class SAMLLogin extends HttpServlet
{
	private static final long serialVersionUID = -3589514330633190919L;

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SAMLLogin.class);
	
	// Services are transient because the class is serializable but the services aren't
	private transient ServerConfigurationService serverConfigurationService;
	private transient UserDirectoryService userDirectoryService;
	private transient UsageSessionService usageSessionService;
	private transient SessionManager sessionManager;
	
	private static final String SAML_IDP_CONFIG_FILE = "idp-metadata.xml";
	private static final String SAML_SP_CONFIG_FILE = "sp-metadata.xml";
	private static final String SAML_SP_PRIVATE_KEY_FILE = "samlKeystore.jks";
	
	private String defaultReturnUrl;
	private SAMLClient samlClient;

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai SAML Login";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
		sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
		usageSessionService = (UsageSessionService) ComponentManager.get(UsageSessionService.class);

		// Where should the user go if the SAML login fails
		defaultReturnUrl = serverConfigurationService.getPortalUrl() + "/xlogin";

		// Initialize SAML library and create the SAML client
		String samlConfigLocation = System.getProperty("sakai.home");
		try {
			SAMLInit.initialize();
			IdPConfig idpConfig = new IdPConfig(new File(samlConfigLocation + File.separator + SAML_IDP_CONFIG_FILE));
			SPConfig spConfig = new SPConfig(new File(samlConfigLocation + File.separator + SAML_SP_CONFIG_FILE));
			try {
				FileInputStream is = new FileInputStream(samlConfigLocation + File.separator + SAML_SP_PRIVATE_KEY_FILE);
				KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
				keystore.load(is, "changeit".toCharArray());
				PrivateKey pkey = (PrivateKey) keystore.getKey("ucsc", "changeit".toCharArray());
				spConfig.setPrivateKey(pkey);
			} catch (Exception e) { M_log.warn("Could not set private key", e); }
			samlClient = new SAMLClient(spConfig, idpConfig);
		} catch (SAMLException e) {
			M_log.warn("Could not initialize SAML", e);
		}
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to SAML POST. This is the user coming back from an IdP with a big XML SAML response.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = sessionManager.getCurrentSession();

		String authresponse = req.getParameter("SAMLResponse");
		String username = "";
		AttributeSet aset;
		User u = null;

		try {
		    aset = samlClient.validateResponse(authresponse);
		    Map<String, List<String>> map = aset.getAttributes();
		    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
		    	String key = entry.getKey();
		    	if ("urn:oid:1.3.6.1.4.1.5923.1.1.1.6".equals(key)) {
		    		List<String> list = entry.getValue();
		                  M_log.debug("key: " + key + ";val: " + java.util.Arrays.toString(list.toArray()));
                                for (String val : list) {
		                    M_log.debug("Found username: " + key + ";val: " + val);
		                    int index = "@".indexOf(val);
                                    if (index > -1) {
                                        username = val.substring(0, index).toLowerCase();
                                    }
                                    else {
                                        username = val.toLowerCase();
                                    }
                                }
                        }
                    }
		    if (M_log.isDebugEnabled()) M_log.debug("Attempting SAML login of username=" + username);
		    u = userDirectoryService.getUserByEid(username);
		} catch (SAMLException e) {
			M_log.warn("Error attempting to SAMLLogin: " + authresponse, e);
		} catch (UserNotDefinedException e) {
			M_log.warn("SAMLLogin user not defined: " + username);
		}
            
		// login the user
		if (u != null && usageSessionService.login(u.getId(), u.getEid(), req.getRemoteAddr(), req.getHeader("user-agent"), UsageSessionService.EVENT_LOGIN_CONTAINER))
		{
			// get the return URL
			String url = getUrl(session, Tool.HELPER_DONE_URL);

			// cleanup session
			session.removeAttribute(Tool.HELPER_MESSAGE);
			session.removeAttribute(Tool.HELPER_DONE_URL);
			
			// redirect to the done URL
			res.sendRedirect(res.encodeRedirectURL(url));
			return;
		}
		
		res.sendRedirect(res.encodeRedirectURL(getUrl(session, SkinnableLogin.ATTR_RETURN_URL)));
	}
	
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
	    if (samlClient != null) {
			try {
				String requestId = SAMLUtils.generateRequestId();
				String authrequest = samlClient.generateAuthnRequest(requestId);
				String samlUrl = samlClient.getIdPConfig().getLoginUrl() + "?SAMLRequest=" + URLEncoder.encode(authrequest, "UTF-8");
				res.sendRedirect(samlUrl);
			} catch (SAMLException e) {
				M_log.warn("Could not generate a SAML login URL", e);
			} catch (UnsupportedEncodingException e) {
				M_log.warn("Could not UTF-8 encode the SAML authrequest", e);
			}
	    }
    }

	/**
	 * Gets a URL from the session, if not found returns the portal URL.
	 * @param session The users HTTP session.
	 * @param sessionAttribute The attribute the URL is stored under.
	 * @return The URL.
	 */
	private String getUrl(Session session, String sessionAttribute) {
		String url = (String) session.getAttribute(sessionAttribute);
		if (url == null || url.length() == 0)
		{
			M_log.debug("No "+ sessionAttribute + " URL, redirecting to portal URL.");
			url = defaultReturnUrl;
		}
		return url;
	}
}
