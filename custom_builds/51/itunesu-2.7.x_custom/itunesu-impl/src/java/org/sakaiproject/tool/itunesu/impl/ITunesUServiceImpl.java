package org.sakaiproject.tool.itunesu.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.itunesu.api.Digester;
import org.sakaiproject.tool.itunesu.api.ITunesHandleProducer;
import org.sakaiproject.tool.itunesu.api.ITunesUService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.client.methods.HttpPost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ITunesUServiceImpl implements ITunesUService {
	private static final Log LOG = LogFactory.getLog(ITunesUServiceImpl.class);
    private SqlService sqlService;
    
    private final String ITUNESU_MESSAGE_BUNDLE = "org.sakaiproject.tool.itunesu.impl.bundle.Messages";
    private final ResourceLoader rl = new ResourceLoader(ITUNESU_MESSAGE_BUNDLE);
	
	// the connection timeout limit - defaults to one minute
	private int connectionTimeoutMilliseconds = 60000;
	
	// the socket timeout limit - defaults to one minute
	private int socketTimeoutMilliseconds = 60000; 

//	private HttpConnectionManagerParams httpParams = new HttpConnectionManagerParams();

//	private HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
	
	/**
	 * The injected iTunesHandleProducer that will be used to provide the iTunes parent handle for sites.
	 */
	private ITunesHandleProducer itunesHandleProducer;
	
	/**
	 * Configuration options (from umich's original code):
	 * 		iTunesU_site_id - the institution's name used to access its site on iTunes U (eg, mySchool.edu)
	 */
	private String iTunesU_site_id = null;

	/** Configuration options (from umich's original code):
	  * 		iTunesU_site_sharedSecret - the shared secret between your institution and iTunes U
	  */
	private String iTunesU_site_sharedSecret = null;

	/** Configuration options (from umich's original code):
	 * 		site_maintain_credential - iTunes U credential for a site maintainer (instructor)
	 */
	private String site_maintain_credential = null;

	/** Configuration options (from umich's original code):
	  * 		site_member_credential - iTunes U credential for a site member (student)
	  */
	private String site_member_credential = null;

	/** Configuration options (from umich's original code):
	  * 		admin_credential - iTunes U credential for an administrator
	  */
	private String admin_credential = null;

	/** Configuration options (from umich's original code):
	 * 		section_handle - iTunes U Section handle if all courses stored under a single Section
	 */
	private String section_handle = null;
	
	/**
     * Configuration options (added by IU):
     *      customItunesPermissionsPerSite - create custom permissions per iTunes site? FALSE by default.
     */
	private boolean customItunesPermissionsPerSite = false;
	
	/**
     * Configuration options (added by IU):
     *      saveHandleInTool - save iTunes U handle within tool placement? FALSE by default.
     */
    private boolean saveHandleInTool = false;

    /**
     * Configuration options (added by IU):
     *      matchOnIdentifier - with ShowTree XML doc, match using Identifier tag? FALSE by default.
     */
    private boolean matchIdentifier = false;
    
    /**
     * Configuration options (added by IU):
     *      credentialsForAllSites - when directing to course, construct credentials from all sakai sites? FALSE by default.
     */
    private boolean credentialsForAllSites = false;
    

	public void init() {
		if (LOG.isInfoEnabled())
			LOG.info("init() itunes");
		if (iTunesU_site_id == null)
		{
			iTunesU_site_id = ServerConfigurationService.getString("iTunesU_site_id", null);
		}
		
		if (iTunesU_site_sharedSecret == null)
		{
			iTunesU_site_sharedSecret = ServerConfigurationService.getString("iTunesU_site_sharedSecret", null);
		}

		if (site_maintain_credential == null )
		{
			site_maintain_credential = ServerConfigurationService.getString("site_maintain_credential", null);
		}

		if (site_member_credential == null)
		{
			site_member_credential = ServerConfigurationService.getString("site_member_credential", null);
		}
		
		if (admin_credential == null)
		{
			admin_credential = ServerConfigurationService.getString("admin_credential", null);
		}
		
		if (section_handle == null)
		{
			section_handle = ServerConfigurationService.getString("section_handle", null);
		}

		if (missingInitParams()) {
			LOG.error ("iTunes U site id and/or shared secret and/or credentials not loaded.");
		}
		
		// override connection timeout setting
		String connectionTimeoutConfig  = StringUtils.trimToNull(ServerConfigurationService.getString("iTunesu.connectionTimeoutMilliseconds"));
		if (connectionTimeoutConfig != null)
		{
			connectionTimeoutMilliseconds = Integer.valueOf(connectionTimeoutConfig).intValue();
		}
		
		// override socket timeout setting
		String socketTimeoutConfig = StringUtils.trimToNull(ServerConfigurationService.getString("iTunesu.socketTimeoutMilliseconds"));
		if (socketTimeoutConfig != null)
		{
			socketTimeoutMilliseconds = Integer.valueOf(socketTimeoutConfig).intValue();
		}
		
	}

	private HttpClient getHttpClientInstance() {
		// use default connection pool setting
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMilliseconds);
		HttpConnectionParams.setSoTimeout(httpParams, socketTimeoutMilliseconds);
		return httpClient;
	}

	/**
	 * Cleans up any resources in use before destroying this service.
	 */
	public void destroy() {
		if (LOG.isInfoEnabled())
			LOG.info("destroy()");
	}

	public void createITunesSite(Site site) {
		try {
			String uploadUrl = getITunesUUploadPage(site.getId());
			URL url = new URL(uploadUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStream out = conn.getOutputStream();
			prepareXmlOutputStream(site, out);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public String getITunesUUploadPage(String siteId) {
		// current user

		User currentUser = UserDirectoryService.getCurrentUser();

		// Define your site's information. Get value from configuraton files
		String iTunesUSiteId = getITunesU_site_id();
		String sharedSecret = getITunesU_site_sharedSecret();
		String adminCredential = ServerConfigurationService.getString("admin_credential", "");
		String iTunesU_upload_url = "https://deimos.apple.com/WebObjects/Core.woa/API/GetUploadURL/";
		String siteURL = iTunesU_upload_url + iTunesUSiteId;

		// admin credential Strings
		String[] credentialsArray = { adminCredential };

		String displayName = currentUser.getDisplayName();
		String emailAddress = currentUser.getEmail();
		String username = currentUser.getId();
		String userIdentifier = currentUser.getId();
		String identity = getIdentityString(displayName, emailAddress,
				username, userIdentifier);
		String credentials = getCredentialsString(credentialsArray);
		Date now = new Date();
		byte[] key = getBytes(sharedSecret, "US-ASCII");
		String token = getAuthorizationToken(credentials, identity,
				now, key);
		//String uploadUrl = iInstance.invokeAction(siteURL, token);
		String uploadUrl = siteURL+"?"+token;
		LOG.debug("**** uploadUrl" + uploadUrl);
		return uploadUrl;
	}

	private void prepareXmlOutputStream(Site site, OutputStream out0) {
		// PrintWriter from a Servlet
		FileOutputStream out = null;
		try {
			// get page
			// PrintWriter from a Servlet
			out = new FileOutputStream("/tmp/xmlDoc");

			// Create XML DOM document (Memory consuming).
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();

			// Document
			Document xmldoc = impl
					.createDocument(null, "ITunesUDocument", null);

			// Root element.
			Element root = xmldoc.getDocumentElement();
			Element action = xmldoc.createElementNS(null, "AddCourse");
			Element course = xmldoc.createElementNS(null, "Course");
			Element template = xmldoc.createElementNS(null, "TemplateHandle");
			Node templateNode = xmldoc.createTextNode("Academic");
			template.appendChild(templateNode);
			Element name = xmldoc.createElementNS(null, "Name");
			Node siteIdNode = xmldoc.createTextNode(site.getId());
			name.appendChild(siteIdNode);

			Element group = xmldoc.createElementNS(null, "Group");
			Element groupName = xmldoc.createElementNS(null, "Name");
			Node groupNameNode = xmldoc.createTextNode(site.getTitle());
			groupName.appendChild(groupNameNode);
			group.appendChild(groupName);
			course.appendChild(name);
			course.appendChild(group);
			action.appendChild(template);
			action.appendChild(course);
			root.appendChild(action);

			String[] permissionCredential = {
					"admin@urn:mace:" + iTunesU_site_id + ":courses:" + site.getId(),
					"student@urn:mace:"+ iTunesU_site_id + ":courses:" + site.getId() };
			String[] permissionAccess = { "Edit", "Download" };
			for (int i = 0; i < permissionCredential.length; i++) {
				Element permission = xmldoc.createElementNS(null, "Permission");
				Element e = xmldoc.createElementNS(null, "Credential");
				Node n = xmldoc.createTextNode(permissionCredential[i]);
				Element e1 = xmldoc.createElementNS(null, "Access");
				Node n1 = xmldoc.createTextNode(permissionAccess[i]);
				e.appendChild(n);
				e1.appendChild(n1);
				permission.appendChild(e);
				permission.appendChild(e1);
				group.appendChild(permission);
			}

			// Serialisation through Tranform.
			DOMSource domSource = new DOMSource(xmldoc);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.transform(domSource, streamResult);
			out0 = out;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally{
			try{
				if (out!=null) out.close();
			}
			catch (Exception e){
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	
    /**
     * Get the US-ASCII or UTF-8 representation of a string.
     *
     * @param string The string to encode.
     * @param encoding "US-ASCII" or "UTF-8".
     *
     * @return A byte array with the appropriate representation of the
     *         string, or <CODE>null</CODE> if the requested encoding is
     *         not "US-ASCII" or "UTF-8". Instead of raising an exception
     *         if the requested encoding is not supported, as String's
     *         getBytes(String encoding) does, this method raises an error,
     *         which simplifies the code of this class and is appropriate
     *         because this class cannot function without US-ASCII or UTF-8.
     */
    public byte[] getBytes(String string, String encoding) {
        byte[] bytes = null;
        if ("US-ASCII".equals(encoding) || "UTF-8".equals(encoding)) {
            try {
                bytes = string.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                throw new Error(
                        "ITunesU.getBytes(): "
                        + encoding
                        + " encoding not supported!");
            }
        }
        return bytes;
    }

    /**
     * Generate the HMAC-SHA256 signature of a message string, as defined in
     * <A HREF="http://www.ietf.org/rfc/rfc2104.txt">RFC 2104</A>.
     *
     * @param message The string to sign.
     * @param key The bytes of the key to sign it with.
     *
     * @return A hexadecimal representation of the signature.
     */
    public String hmacSHA256(String message, byte[] key) {

        // Start by getting an object to generate SHA-256 hashes with.
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(
                    this.getClass().getName()
                    + ".hmacSHA256(): SHA-256 algorithm not found!");
        }

        // Hash the key if necessary to make it fit in a block (see RFC 2104).
        if (key.length > 64) {
            sha256.update(key);
            key = sha256.digest();
            sha256.reset();
        }

        // Pad the key bytes to a block (see RFC 2104).
        byte block[] = new byte[64];
        for (int i = 0; i < key.length; ++i) block[i] = key[i];
        for (int i = key.length; i < block.length; ++i) block[i] = 0;

        // Calculate the inner hash, defined in RFC 2104 as
        // SHA-256(KEY ^ IPAD + MESSAGE)), where IPAD is 64 bytes of 0x36.
        for (int i = 0; i < 64; ++i) block[i] ^= 0x36;
        sha256.update(block);
        sha256.update(this.getBytes(message, "UTF-8"));
        byte[] hash = sha256.digest();
        sha256.reset();

        // Calculate the outer hash, defined in RFC 2104 as
        // SHA-256(KEY ^ OPAD + INNER_HASH), where OPAD is 64 bytes of 0x5c.
        for (int i = 0; i < 64; ++i) block[i] ^= (0x36 ^ 0x5c);
        sha256.update(block);
        sha256.update(hash);
        hash = sha256.digest();

        // The outer hash is the message signature...
        // convert its bytes to hexadecimals.
        char[] hexadecimals = new char[hash.length * 2];
        for (int i = 0; i < hash.length; ++i) {
            for (int j = 0; j < 2; ++j) {
                int value = (hash[i] >> (4 - 4 * j)) & 0xf;
                char base = (value < 10) ? ('0') : ('a' - 10);
                hexadecimals[i * 2 + j] = (char)(base + value);
            }
        }

        // Return a hexadecimal string representation of the message signature.
        return new String(hexadecimals);

    }

    /**
     * Combine user identity information into an appropriately formatted string.
     *
     * @param displayName The user's name (optional).
     * @param emailAddress The user's email address (optional).
     * @param username The user's username (optional).
     * @param userIdentifier A unique identifier for the user (optional).
     *
     * @return A non-<CODE>null</CODE> user identity string.
     */
    public String getIdentityString(String displayName, String emailAddress,
                                    String username, String userIdentifier) {

        // Create a buffer with which to generate the identity string.
        StringBuffer buffer = new StringBuffer();

        // Define the values and delimiters of each of the string's elements.
        String[] values = { displayName, emailAddress,
                            username, userIdentifier };
        char[][] delimiters = { { '"', '"' }, { '<', '>' },
                                { '(', ')' }, { '[', ']' } };

        // Add each element to the buffer, escaping
        // and delimiting them appropriately.
        for (int i = 0; i < values.length; ++i) {
            if (values[i] != null) {
                if (buffer.length() > 0) buffer.append(' ');
                buffer.append(delimiters[i][0]);
                for (int j = 0, n = values[i].length(); j < n; ++j) {
                    char c = values[i].charAt(j);
                    if (c == delimiters[i][1] || c == '\\') buffer.append('\\');
                    buffer.append(c);
                }
                buffer.append(delimiters[i][1]);
            }
        }

        // Return the generated string.
        return buffer.toString();

    }

    /**
     * Combine user credentials into an appropriately formatted string.
     *
     * @param credentials An array of credential strings. Credential
     *                    strings may contain any character but ';'
     *                    (semicolon), '\\' (backslash), and control
     *                    characters (with ASCII codes 0-31 and 127).
     *
     * @return <CODE>null</CODE> if and only if any of the credential strings 
     *         are invalid.
     */
    public String getCredentialsString(String[] credentials) {

        // Create a buffer with which to generate the credentials string.
        StringBuffer buffer = new StringBuffer();

        // Verify and add each credential to the buffer.
        if (credentials != null) {
            for (int i = 0; i < credentials.length; ++i) {
                if (i > 0) buffer.append(';');
                for (int j = 0, n = credentials[i].length(); j < n; ++j) {
                    char c = credentials[i].charAt(j);
                    if (c != ';' && c != '\\' && c >= ' ' && c != 127) {
                        buffer.append(c);
                    } else {
                        return null;
                    }
                }
            }
        }

        // Return the credentials string.
        return buffer.toString();

    }

    /**
     * Generate an iTunes U digital signature for a user's identity
     * and credentials. Signatures are usually sent to iTunes U
     * along with the identity, credentials and a time stamp to
     * warrant to iTunes U that the identity and credential values are
     * officially sanctioned. For such uses, it will usually makes
     * more sense to use an authorization token obtained from the
     * {@link #getAuthorizationToken(java.lang.String, java.lang.String, java.util.Date, byte[])}
     * method than to use a signature directly: Authorization
     * tokens include the signature but also the identity, credentials,
     * and time stamp, and have those conveniently packaged in
     * a format that is easy to send to iTunes U over HTTPS.
     *
     * @param identity The user's identity string, as
     *                 obtained from getIdentityString().
     * @param credentials The user's credentials string, as
     *                    obtained from getCredentialsString().
     * @param time Signature time stamp.
     * @param key The bytes of your institution's iTunes U shared secret key.
     *
     * @return A hexadecimal representation of the signature.
     */
    public String getSignature(String identity, String credentials,
                               Date time, byte[] key) {

        // Create a buffer in which to format the data to sign.
        StringBuffer buffer = new StringBuffer();

        // Generate the data to sign.
        try {

            // Start with the appropriately encoded credentials.
            buffer.append("credentials=");
            buffer.append(URLEncoder.encode(credentials, "UTF-8"));

            // Add the appropriately encoded identity information.
            buffer.append("&identity=");
            buffer.append(URLEncoder.encode(identity, "UTF-8"));

            // Add the appropriately formatted time stamp. Note that
            // the time stamp is expressed in seconds, not milliseconds.
            buffer.append("&time=");
            buffer.append(time.getTime() / 1000);

        } catch (UnsupportedEncodingException e) {

            // UTF-8 encoding support is required.
            throw new Error(
                "ITunesU.getSignature():  UTF-8 encoding not supported!");

        }

        // Generate and return the signature.
        String signature = this.hmacSHA256(buffer.toString(), key);
        return signature;

    }

    /**
     * Generate and sign an authorization token that you can use to securely
     * communicate to iTunes U a user's identity and credentials. The token
     * includes all the data you need to communicate to iTunes U as well as
     * a creation time stamp and a digital signature for the data and time.
     *
     * @param identity The user's identity string, as
     *                 obtained from getIdentityString().
     * @param credentials The user's credentials string, as
     *                    obtained from getCredentialsString().
     * @param time Token time stamp. The token will only be valid from
     *             its time stamp time and for a short time thereafter
     *             (usually 90 seconds thereafter, this "transfer
     *             timeout" being configurable in the iTunes U server).
     * @param key The bytes of your institution's iTunes U shared secret key.
     *
     * @return The authorization token. The returned token will
     *         be URL-encoded and can be sent to iTunes U with
     *         a <A HREF="http://www.ietf.org/rfc/rfc1866.txt">form
     *         submission</A>. iTunes U will typically respond with
     *         HTML that should be sent to the user's browser.
     */
    public String getAuthorizationToken(String credentials, String identity,
                                        Date time, byte[] key) {

        // Create a buffer with which to generate the authorization token.
        StringBuffer buffer = new StringBuffer();

        // Generate the authorization token.
        try {

            // Start with the appropriately encoded credentials.
            buffer.append("credentials=");
            buffer.append(URLEncoder.encode(credentials, "UTF-8"));

            // Add the appropriately encoded identity information.
            buffer.append("&identity=");
            buffer.append(URLEncoder.encode(identity, "UTF-8"));

            // Add the appropriately formatted time stamp. Note that
            // the time stamp is expressed in seconds, not milliseconds.
            buffer.append("&time=");
            buffer.append(time.getTime() / 1000);

            // Generate and add the token signature.
            String data = buffer.toString();
            buffer.append("&signature=");
            buffer.append(this.hmacSHA256(data, key));

        } catch (UnsupportedEncodingException e) {

            // UTF-8 encoding support is required.
            throw new Error(
                "ITunesU.getAuthorizationToken(): "
                + "UTF-8 encoding not supported!");

        }

        // Return the signed authorization token.
        return buffer.toString();

    }

    /**
     * Send a request for an action to iTunes U with an authorization token. 
     *
     * @param url URL defining how to communicate with iTunes U and
     *            identifying which iTunes U action to invoke and which iTunes
     *            U page or item to apply the action to. Such URLs have a
     *            format like <CODE>[PREFIX]/[ACTION]/[DESTINATION]</CODE>,
     *            where <CODE>[PREFIX]</CODE> is a value like
     *            "https://deimos.apple.com/WebObjects/Core.woa" which defines
     *            how to communicate with iTunes U, <CODE>[ACTION]</CODE>
     *            is a value like "Browse" which identifies which iTunes U
     *            action to invoke, and <CODE>[DESTINATION]</CODE> is a value
     *            like "example.edu" which identifies which iTunes U page
     *            or item to apply the action to. The destination string
     *            "example.edu" refers to the root page of the iTunes U site
     *            identified by the domain "example.edu". Destination strings
     *            for other items within that site contain the site domain
     *            followed by numbers separated by periods. For example:
     *            "example.edu.123.456.0789". You can find these
     *            strings in the items' URLs, which you can obtain from
     *            iTunes. See the iTunes U documentation for details.
     * @param token Authorization token generated by getAuthorizationToken().
     *
     * @return The iTunes U response, which may be HTML or
     *         text depending on the type of action invoked.
     */
    public String invokeAction(String url, String token) {

        // Send a request to iTunes U and record the response.
        StringBuffer response = new StringBuffer();
        try {

            // Verify that the communication will be over SSL.
            if (!url.startsWith("https")) {
                throw new MalformedURLException(
                    "ITunesU.invokeAction(): URL \""
                    + url + "\" does not use HTTPS.");
            }

            // Create a connection to the requested iTunes U URL.
            HttpURLConnection connection =
                    (HttpURLConnection)new URL(url).openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");

            // Send the authorization token to iTunes U.
            connection.connect();
            OutputStream output = connection.getOutputStream();
            output.write(this.getBytes(token, "UTF-8"));
            output.flush();
            output.close();

            // Read iTunes U's response.
            InputStream input = connection.getInputStream();
            Reader reader = new InputStreamReader(input, "UTF-8");
            reader = new BufferedReader(reader);
            char[] buffer = new char[16 * 1024];
            for (int n = 0; n >= 0;) {
                n = reader.read(buffer, 0, buffer.length);
                if (n > 0) response.append(buffer, 0, n);
            }

            // Clean up.
            input.close();
            connection.disconnect();

        } catch (IOException e) {
        		// there is no return from server
            response.append("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 3.2//EN'><html><head><title>");
            response.append(rl.getString("notFoundPageTitle") + "</title></head><body><h4>");
            response.append(rl.getString("notFoundPageBody") + "</h4></body></html>");
        }

        // Return the response received from iTunes U.
        return response.toString();

    }

    /**
      * Returns a Map of roles for user keyed on site id
      * 
      * @return
      *                 Map of Strings in role:siteId format
      */
     public Map<String, String> getUserRoleForAllSites()
     {
        // Move this to a helper to get the correct version?
        final String statement = "select userSiteInfo.siteId, roleTable.roleName, rrg.role_key from SAKAI_REALM_RL_GR rrg " +
         	"join (select site.SITE_ID siteId, myrealm.realm_key realm_key, userinfo.userId userId" + 
         	"        from sakai_site site" +
         	"        join (select realm_id realm_id, realm_key realm_key from sakai_realm) myrealm" +
            "        on myrealm.realm_id = '/site/'|| site.site_id" +
            "        join (select idmap.USER_ID userId, siteUser.SITE_ID siteId from sakai_user_id_map idmap, SAKAI_SITE_USER siteUser" +
            "         where idmap.eid = ? and idmap.USER_ID = siteUser.USER_ID) userinfo" +
            "        on userinfo.siteId = site.site_id) userSiteInfo " +
            "on rrg.REALM_KEY = userSiteInfo.realm_key " +
            "and rrg.user_id = userSiteInfo.userId " +
            "join (select role_name  roleName, role_key roleKey from  SAKAI_REALM_ROLE) roleTable " +
            "on rrg.role_key = roleTable.roleKey";
    
         Object fields[] = new Object[1];
         fields[0] = UserDirectoryService.getCurrentUser().getEid();
    
         final Map<String, String> siteRoleNameMap = new HashMap<String, String>();
         sqlService.dbRead(statement, fields, new SqlReader()
         {
        	 public Object readSqlResultRecord(ResultSet result)
        	 {
        		 try
        		 {
        			 siteRoleNameMap.put(result.getString(1), result.getString(2));
    
        			 return null;
        		 }
        		 catch (SQLException e)
        		 {
        			 LOG.error("getUserRoleForAllSites: " + e, e);
        			 return null;
        		 }
        	 }
         });
    
         if (siteRoleNameMap.size() < 1) return null;
    
         return siteRoleNameMap;
     }
     
 	/**
      * @see org.sakaiproject.tool.itunesu.api.ITunesUService#getITunesU_site_id()
   	 */
	public String getITunesU_site_id() {
		return iTunesU_site_id;
	}

	/**
	 * Sets the site id used in the construction of urls that point
	 * to your institution's iTunes U site (eg, mySchool.edu)
	 * @param tunesU_site_id
	 */
	public void setItunesU_site_id(String tunesU_site_id) {
		iTunesU_site_id = tunesU_site_id;
	}

	/**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#getITunesU_sharedSecret()
   	 */
	public String getITunesU_site_sharedSecret() {
		return iTunesU_site_sharedSecret;
	}

	/**
	 * Sets the shared secret between your institution and iTunes U
	 * @param tunesU_site_sharedSecret
	 */
	public void setItunesU_site_sharedSecret(String tunesU_site_sharedSecret) {
		iTunesU_site_sharedSecret = tunesU_site_sharedSecret;
	}

	/**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#isCustomItunesPermissionsPerSite()
   	 */
	public boolean isCustomItunesPermissionsPerSite()
	{
		LOG.debug("isCustomItunesPermissionsPerSite()");
		return customItunesPermissionsPerSite;
	}

	/**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#setCustomItunesPermissionsPerSite()
 	 */
	public void setCustomItunesPermissionsPerSite(
			boolean customItunesPermissionsPerSite)
	{
		LOG.debug("setCustomItunesPermissionsPerSite(boolean "
				+ customItunesPermissionsPerSite + ")");
		this.customItunesPermissionsPerSite = customItunesPermissionsPerSite;
	}

	/**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#isSaveHandleInTool()
     */
    public boolean isSaveHandleInTool()
    {
    	return saveHandleInTool;
    }

    /**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#isMatchIdentifier()
     */
    public boolean isMatchIdentifier()
    {
    	return matchIdentifier;
    }

    /**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#getCredentialsForAllSites()
     */
    public boolean getCredentialsForAllSites() {
    	return credentialsForAllSites;
    }

    /**
     * TRUE  - save iTunes U handle for this site in tool placement
     * FALSE - get iTunes U handle from ShowTree XML document
     * @param saveHandleInTool
     */
    public void setSaveHandleInTool(boolean saveHandleInTool) {
		this.saveHandleInTool = saveHandleInTool;
	}

    /**
     * When determining iTunes U site handle,
     * 		TRUE  - look at Identifier tag from ShowTree command (using 'most' specifier)
     * 		FALSE - look at Name tag from ShowTree command (using 'minimal' identifier)
     * @param matchIdentifier 
     */
	public void setMatchIdentifier(boolean matchIdentifier) {
		this.matchIdentifier = matchIdentifier;
	}

	/**
	 * TRUE  - send credentials for all sites user member of
	 * FALSE - send credential just for current site
	 * @param credentialsForAllSites 
	 */
	public void setCredentialsForAllSites(boolean credentialsForAllSites) {
		this.credentialsForAllSites = credentialsForAllSites;
	}

	/**
     * @see org.sakaiproject.tool.itunesu.api.ITunesUService#getITunesParentHandle()
     */
	public String getITunesParentHandle()
	{
		LOG.debug("getItunesHandle()");
		if (itunesHandleProducer == null)
			throw new IllegalStateException("iTunesHandleProducer == null");
		return itunesHandleProducer.getITunesHandle();
	}

	/**
	 * @return the itunesHandleProducer
	 */
	public ITunesHandleProducer getItunesHandleProducer()
	{
		return itunesHandleProducer;
	}

	/**
	 * @param itunesHandleProducer the itunesHandleProducer to set
	 */
	public void setItunesHandleProducer(ITunesHandleProducer itunesHandleProducer)
	{
        if (LOG.isDebugEnabled())
        	LOG.debug("setITunesHandleProducer(ITunesHandleProducer "
        					+ itunesHandleProducer + ")");
        	 	this.itunesHandleProducer = itunesHandleProducer;
	}

    public void setSqlService(SqlService sqlService) {
    	this.sqlService = sqlService;
    }
    
    /**
     * {@inheritDoc}}
     */
	public Hashtable<String, String> getITunesUCreds(boolean overrideWithAdmin, boolean allSites)
    {
    	// current user
		User currentUser = UserDirectoryService.getCurrentUser();
		
		Placement placement = ToolManager.getCurrentPlacement();
		boolean isSiteMaintainer = false;
		
		// get the current site id
		String siteId = placement.getContext();
			
		try
		{
			Site s = SiteService.getSite(siteId);
			// site title
			String siteName = s.getTitle();
			
			if (SiteService.allowUpdateSite(s.getId()))
			{
				// a maintainer type user
				isSiteMaintainer = true;
			}
		}
		catch (Exception e)
		{
			LOG.error("Failed to get site name for site id=" + siteId, e);
		}
		
		Properties props = placement.getPlacementConfig();
		if (placement != null)
		{
			if(props.isEmpty())
				props = placement.getConfig();
			
			// load the saved target site option data, which can only be set by admin users
			// if not null, the id should override the current site id and iTunes U tool should be redirected to the new target
			// however, the role settings should be the same as the ones in current site context.
			String targetSiteId = StringUtil.trimToNull(props.getProperty("target_site_id"));
			if (targetSiteId != null)
			{
				siteId = targetSiteId;
			}
		}

		// Define your site's information. Get value from configuraton files (sakai.properties)
		String iTunesUSiteId = ServerConfigurationService.getString("iTunesU_site_id", "");;
		String sharedSecret = ServerConfigurationService.getString("iTunesU_site_sharedSecret", "");;
		String siteURL = "https://deimos.apple.com/WebObjects/Core.woa/Browse/" + iTunesUSiteId;

        String [] credentialsArray = new String [1];

        // do you want credentials corresponding to all sites or just this one?
        if (allSites)
		{
			credentialsArray = getCredentialsArrayForAllSites();
		}
        else
        {
        	// original version pulls credentials from config file (sakai.properties)
        	String instructorCredential = ServerConfigurationService.getString("site_maintain_credential", "") + ":" + siteId;
        	String memberCredential = ServerConfigurationService.getString("site_member_credential", "") + ":" + siteId;
        	String adminCredential = ServerConfigurationService.getString("admin_credential", "");
        
        	// current user is either the user role equals to the admin role if set, or of instructor type, or of learner/student type
        	credentialsArray[0] = (overrideWithAdmin || SecurityService.isSuperUser())?adminCredential:(isSiteMaintainer?instructorCredential:memberCredential);
        }

        // Define the user information. Replace the optional identity
       	// information with the identity of the current user, and the
       	// credentials with the credentials you want to grant to that user.
       	// For initial testing and site setup, use the singe administrator 
       	// credential defined when your iTunes U site was created. Once
       	// you have access to your iTunes U site, you will be able to define
       	// additional credentials and the iTunes U access they provide.
        String displayName = currentUser.getDisplayName();
       	String emailAddress = currentUser.getEmail();
       	String username = currentUser.getId();
       	String userIdentifier = currentUser.getId();
        
        // Define the iTunes U page to browse. Use the domain name that
        // uniquely identifies your site in iTunes U to browse to that site's
        // root page; use a destination string extracted from an iTunes U URL
        // to browse to another iTunes U page; or use a destination string
        // supplied as the "destination" parameter if this program is being
        // invoked as a part of the login web service for your iTunes U site.
        String siteDomain = siteURL.substring(siteURL.lastIndexOf('/') + 1);
        String destination = siteDomain;
        
        // Append your site's debug suffix to the destination if you
        // want to receive an HTML page providing information about
        // the transmission of identity and credentials between this
        // program and iTunes U. Remove this code after initial
        // testing to instead receive the destination page requested.
        // destination = destination + "/oqr456";

        // Use an ITunesU instance to format the identity and credentials
        // strings and to generate an authorization token for them.
        String identity = getIdentityString(displayName, emailAddress,username, userIdentifier);
        String credentials = getCredentialsString(credentialsArray);
        Date now = new Date();
        byte[] key = getBytes(sharedSecret, "US-ASCII");
        String token = getAuthorizationToken(credentials, identity, now, key);

        // Use the authorization token to connect to iTunes U and obtain
        // from it the HTML that needs to be returned to a user's web
        // browser to have a particular page or item in your iTunes U
        // site displayed to that user in iTunes. Replace "/Browse/" in
        // the code below with "/API/GetBrowseURL/" if you instead want
        // to return the URL that would need to be opened to have that
        // page or item displayed in iTunes.
        String prefix = siteURL.substring(0, siteURL.indexOf(".woa/") + 4);
        
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put("prefix", prefix);
        table.put("destination", destination);
        table.put("token", token);
        
        return table;
    }
	
	/**
	 * Constructs the credential array from all OnCourse sites so can navigate
	 * to other iTunes U courses once user directed to iTunes U
	 * 
	 * @return
	 * 	String array of credentials - 1 entry per site
	 */
	@SuppressWarnings("unchecked")
	private String [] getCredentialsArrayForAllSites()
	{
		final String iTunesUSiteId = ServerConfigurationService.getString("iTunesU_site_id", "");;
		Map<String, String> siteRoleMap = getUserRoleForAllSites();
	
		String [] results = new String [siteRoleMap.size()];
		int i = 0;
		Set siteRoleEntrySet = siteRoleMap.entrySet();
		
		for (Iterator iter = siteRoleEntrySet.iterator(); iter.hasNext();)
		{
			Entry info = (Entry) iter.next();
			
	    	final String credential = encodeRole((String) info.getValue()) + "@urn:mace:itunesu.com:sites:" + iTunesUSiteId + ":" + info.getKey();
			results[i++] = credential;
		}
		
		return results;
	}
	

	/**
	 * Remove invalid characters from role while
	 * constructing credentials to send to iTunes U
	 * @param role
	 */
	private String encodeRole(String role)
	{
		StringBuffer modifiedRoleString = new StringBuffer();
       	char previous = ' '; // Used to determine first letter of each 'word'
        		
        for (int j = 0, n = role.length(); j < n; ++j) {
            char c = role.charAt(j);
            // Need Character.getNumericValue to return Unicode value
            if (Character.isLetterOrDigit(c)) 
            {
           		modifiedRoleString.append(c);
            }
        }

        return modifiedRoleString.toString();
	}
	
	/**
     * @{inherit}
     */
	public String getUploadURL(String handle, String prefix, String destination, String token) 
	{
		String rv = null;
		
		String url = prefix + "/API/getUploadURL/" + destination;
		if (handle != null)
		{
			url = url + "." + handle;
		}
		url = url + "?type=XMLControlFile&" + token;
		
		LOG.info(this + "uploadUrl = " + url);
		try
		{
			// get HttpClient instance
			HttpClient httpClient = getHttpClientInstance();
			
			// add the course site
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httppost);
			LOG.debug(response.getStatusLine());
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				rv = getHttpEntityString(entity);
			    // When HttpClient instance is no longer needed, 
		        // shut down the connection manager to ensure
		        // immediate deallocation of all system resources
		        httpClient.getConnectionManager().shutdown();
			}
		}
		catch (IOException e)
		{
			LOG.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
		
		return rv;
	}

	/**
	 * read the HttpEntity content stream into String object
	 * @param entity
	 * @return
	 * @throws IOException
	 */
	private String getHttpEntityString(HttpEntity entity) throws IOException {
		String rv;
		StringBuffer buffer = new StringBuffer();
		InputStream instream = entity.getContent(); 
		int size = 2048;
		byte[] data = new byte[size];
		while ((size = instream.read(data, 0, data.length)) > 0)
		        buffer.append(new String(data, 0, size));
		instream.close();
		rv = buffer.toString();
		return rv;
	}
	
	 /**
     * {@inherit}
     */
    public String getITunesUCourseHandle(String siteId, String siteName, String prefix, String destination, String token)
    {
    	String rv = null;
    	
    	// CAUTION (April 2008): if the body of the if statement is executed, the code will work
    	// ONLY if the entire site's ShowTree is grabbed. Previous to this, restricting to an iTunes U 
    	// Section would contain the Course information for courses nested inside of it.
    	// if we don't save handle in tool OR not yet stored, get it from iTunes U (ShowTree XML doc)
    	if (isSaveHandleInTool())
    	{
    		Placement placement = ToolManager.getCurrentPlacement();
    		if (placement.getContext().equals(siteId))
    		{
    			// invoked from within the site
    			rv = placement .getPlacementConfig().getProperty("iTunesUHandle");
    		}
    		else
    		{
    			// invoked from iTunesU Admin tool, looking up for the site iTunesU Handle
    			try
    			{
    				Site site = SiteService.getSite(siteId);
    				// search the pages
    				for (Iterator iPages = site.getPages().iterator(); iPages.hasNext();)
    				{
    					SitePage page = (SitePage) iPages.next();
    					List<ToolConfiguration> tools = page.getTools();
    					if (tools != null && !tools.isEmpty())
    					{
    						for (ToolConfiguration tool: tools)
    						{
    							if ("sakai.iTunesU".equals(tool.getToolId()))
    							{
    								rv = tool.getPlacementConfig().getProperty("iTunesUHandle");
    								break;
    							}
    						}
    					}
    				}
    			}
    			catch (Exception e)
    			{
    				LOG.warn(this + ": getITunesUCourseHandle: lookup site itunesu handle, but cannot find site for " + siteId);
    			}
    		}
    	}
    	
    	if (rv == null)
    	{
	    	LOG.info("getITunesUCourseHandle: course handle not saved in tool handle, need to retrieve it from iTunesU site");
			
			String url = ""; 
			url = prefix + "/API/ShowTree/" + destination;
	
			// add the section handle
			String sectionHandle = getITunesParentHandle();
			if (sectionHandle != null)
			{
				url = url + "." + sectionHandle;
			}
		
			// if pulling handle based on site id, get 'most' XML file
			// otherwise get minimal
			// NOTE: minimal returns Name, Handle, AggregateSize only
			if (isMatchIdentifier())
			{
				url = url + "?keyGroup=minimal&" + token;
			}
			else
			{
				url = url + "?keyGroup=minimal&" + token;
			}
			
			LOG.info("getITunesUCourseHandle: url=" + url);
	
			HttpPost httppost = new HttpPost(url);
			try
			{
				// get HttpClient instance
				HttpClient httpClient = getHttpClientInstance();
				
				// add the course site	
				HttpResponse response = httpClient.execute(httppost);
				LOG.info("getITunesUCourseHandle get ShowTree response");
				// if using Identifier to determine course, call local method
				// otherwise, feed it into the Digester to match on name
				HttpEntity httpEntity = response.getEntity();
	    		if (httpEntity != null)
	    		{
	    			InputStream responseStream = httpEntity.getContent();
					if (isMatchIdentifier())
		    		{
		    			LOG.info("getITunesUCourseHandle isMatchIdentifier = true");
		    			rv = parseShowTreeXMLDoc(responseStream, siteId);
		    		}
		    		else
		    		{
		    			LOG.info("getITunesUCourseHandle isMatchIdentifier = false");
		    			XMLReader xr = XMLReaderFactory.createXMLReader();
		    			String[] params = {siteId, siteName};
		    			Digester d = new Digester(params);
		    			xr.setContentHandler(d);
		    			InputSource is = new InputSource(responseStream);
		    			xr.parse(is);
		    			rv = d.getCourseHandle();
		    			LOG.info("getITunesUCourseHandle courseHandle=" + rv);
		    		}
					responseStream.close();
					// When HttpClient instance is no longer needed, 
			        // shut down the connection manager to ensure
			        // immediate deallocation of all system resources
			        httpClient.getConnectionManager().shutdown();
	    		}
			}
			catch (FileNotFoundException e)
			{
				LOG.warn(e.getMessage(), e);
			}
			catch (IOException e)
			{
				LOG.warn(e.getMessage(), e);
			}
			catch (Exception e)
			{
				LOG.warn(e.getMessage(), e);
			}
    	}
    	
    	rv = StringUtil.trimToNull(rv);
		
    	// ZQIAN: TODO Sometimes the course handle is returned with SECTION_HANDLE.COURSE_HANDLE, need to only retrieve the course handle part.
    	if (rv != null && rv.indexOf(".") != -1 && !rv.endsWith("."))
    	{
    		rv = rv.substring(rv.lastIndexOf(".") + 1);
    	}
    	
		return rv;
		
    }	// getITunesUCourse
    
	 /**
     * {@inherit}
     */
    public HashMap<String, String> getOrphanedPages(String prefix, String destination, String token)
    {
    	HashMap<String, String> rv = new HashMap<String, String>();
	
		String url = ""; 
		url = prefix + "/API/ShowTree/" + destination;

		// add the section handle
		String sectionHandle = getITunesParentHandle();
		if (sectionHandle != null)
		{
			url = url + "." + sectionHandle;
		}
	
		// if pulling handle based on site id, get 'most' XML file
		// otherwise get minimal
		// NOTE: minimal returns Name, Handle, AggregateSize only
		if (isMatchIdentifier())
		{
			url = url + "?keyGroup=minimal&" + token;
		}
		else
		{
			url = url + "?keyGroup=minimal&" + token;
		}
		
		LOG.info("getOrphanedPages: url=" + url);

		HttpPost httppost = new HttpPost(url);
		try
		{
			// get HttpClient instance
			HttpClient httpClient = getHttpClientInstance();
			
			// add the course site	
			HttpResponse response = httpClient.execute(httppost);
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null)
			{
				InputStream responseStream = httpEntity.getContent();
				LOG.info("getOrphanedPages get ShowTree response");
				// if using Identifier to determine course, call local method
				// otherwise, feed it into the Digester to match on name
	    		if (isMatchIdentifier())
	    		{
	    			LOG.info("getOrphanedPages isMatchIdentifier = true");
	    			rv = parseShowTreeXMLDocForOrphanedPages(responseStream);
	    		}
	    		responseStream.close();
	    		// When HttpClient instance is no longer needed, 
		        // shut down the connection manager to ensure
		        // immediate deallocation of all system resources
		        httpClient.getConnectionManager().shutdown();
			}
		}
		catch (FileNotFoundException e)
		{
			LOG.warn(e.getMessage(), e);
		}
		catch (IOException e)
		{
			LOG.warn(e.getMessage(), e);
		}
		catch (Exception e)
		{
			LOG.warn(e.getMessage(), e);
		}
    	
		return rv;
		
    }	// getOrphanedPages
    
    /**
     * Process the ShowTree document from iTunes U to retrieve the
     * course handle.
     * @param response The input stream of the ShowTree from iTunes U
     * 
     * @return The iTunes U handle as a string if found or null if not
     */
    private String parseShowTreeXMLDoc(InputStream response, String siteId)
    {
    	String rv = null;
    	
    	if (response != null)
    	{
			String tmpHandle = null;
			boolean idFound = false;
			   
			Document doc = Xml.readDocumentFromStream(response);
			if (doc != null)
			{
				NodeList nodes = doc.getElementsByTagName("Course");
				
				// Search all course sites
				for (int i = 0; i < nodes.getLength(); i++)
				{
					Node courseNode = nodes.item(i);
					NodeList children = courseNode.getChildNodes();
					
					// search for identifier to see if this is course
					// wanted and its associated identifier
					for (int j = 0; j < children.getLength(); j++)
					{
						Node node = children.item(j);
						String nodeName = node.getNodeName();
		
						if (nodeName.equalsIgnoreCase("Handle")) 
						{
							tmpHandle = node.getTextContent();
						}
						else if (nodeName.equalsIgnoreCase("Identifier")) 
						{
							String identifier = node.getTextContent(); 
							if (identifier != null && identifier.equalsIgnoreCase(siteId)) 
							{
								idFound = true;
							}
						}
		
						// have i found the correct course and handle for it
						// needed since can't assume order nodes processed
						if (idFound && tmpHandle != null) 
						{
							rv = tmpHandle;
							break;
						}
					}
		
					// have i found the correct course and handle for it
					// if so, we are done so break out of outer loop
					if (idFound && tmpHandle != null) 
					{
						break;
					}
				}
			}   	
    	}
		   
		return rv;
    }
    
    /**
     * Process the ShowTree document from iTunes U to find out whether any orphaned page exists (the site has been deleted)
     * @param response The input stream of the ShowTree from iTunes U
     * @return The list of orphaned sites, if any
     */
    private HashMap<String, String> parseShowTreeXMLDocForOrphanedPages(InputStream response)
    {
    	StringBuffer buffer = new StringBuffer();
		String tmpHandle = null;
		String identifier = null;
		boolean orphaned = false;
		HashMap<String, String> oMap = new HashMap<String, String>();
		
		Document doc = Xml.readDocumentFromStream(response);
		NodeList nodes = doc.getElementsByTagName("Course");
		
		// Search all course sites
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node courseNode = nodes.item(i);
			NodeList children = courseNode.getChildNodes();
			
			// search for orphaned sites
			for (int j = 0; j < children.getLength(); j++)
			{
				Node node = children.item(j);
				String nodeName = node.getNodeName();
				if (nodeName.equalsIgnoreCase("Handle")) 
				{
					tmpHandle = node.getTextContent();
				}
				else if (nodeName.equalsIgnoreCase("Identifier")) 
				{
					identifier = node.getTextContent(); 
					if (identifier != null) 
					{
						try
						{
							Site s = SiteService.getSite(identifier);
						}
						catch (Exception e)
						{
							// exception means this is an orphaned node now
							orphaned = true;
						}
					}
				}

				// this is an orphaned site
				if (orphaned && tmpHandle != null) 
				{
					if (!oMap.containsKey(tmpHandle))
					{
						oMap.put(tmpHandle, identifier);
						LOG.info("found " + tmpHandle + "(" + identifier + ")" );
					}
				}
			}
		}   			
		   
		return oMap;
    }
    
    
    /**
     *{@inherit}
     */
	public String wsCall(String operation, String uploadURL, String xmlDocument, String prefix, String destination, String token) 
	{
		String rv = null;
		
		if (LOG.isDebugEnabled())
			LOG.debug("sendUploadRequest(String " + operation + ", String "
					+ uploadURL + ", String " + xmlDocument + ", String "
					+ prefix + ", String " + destination + ", String " + token
					+ ")");
        
		try
		{
			if (xmlDocument != null)
			{
				File tempFile = File.createTempFile("wsTmp", ".xml");
				tempFile.deleteOnExit();
				FileOutputStream fout = new FileOutputStream(tempFile);
				try {
	    			fout.write(xmlDocument.getBytes());
				}
				catch (Exception fileException)
				{
					LOG.warn(this + " wscall: problem with writing FileOutputStream " + fileException.getMessage());
				}	
				finally {
					try
					{
		    			fout.flush();
						fout.close(); // The file channel needs to be closed before the deletion.
					}
					catch (IOException ioException)
					{
						LOG.warn(this + " wscall: problem closing FileOutputStream " + ioException.getMessage());
					}
				}
				
				// get HttpClient instance
				HttpClient httpClient = getHttpClientInstance();
				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
				FileBody bin = new FileBody(tempFile);  
				reqEntity.addPart("file", bin );
		        HttpPost httppost = new HttpPost(uploadURL);	
				httppost.setEntity(reqEntity);
			    HttpResponse response = httpClient.execute(httppost);
			    if (response.getStatusLine() != null)
			    {
				    int status =response.getStatusLine().getStatusCode();
				    if (status == 200)
				    {
				    		HttpEntity httpEntity = response.getEntity();
				    		if (httpEntity != null)
				    		{
				    			rv = getHttpEntityString(httpEntity);
				    			
				    			// When HttpClient instance is no longer needed, 
						        // shut down the connection manager to ensure
						        // immediate deallocation of all system resources
						        httpClient.getConnectionManager().shutdown();
				    		}
				    }
				    else
				    {
				    		LOG.error("Upload failed response = " + response.getStatusLine().toString());
				    }
				}
			}
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
		
		return rv;
	}
	
	/**{@inheritDoc}
	 * @param siteId
	 * @return
	 */
	public String getAddCourseXml(String siteId) 
	{
		String instructorName = "";
		String siteDescription = "";
		String siteName = "";
		
		try
		{
			Site s = SiteService.getSite(siteId);
			siteName = s.getTitle();
			// instructor name
			Set<String> userSet = s.getUsersHasRole(s.getMaintainRole());
			if (userSet != null)	
			{
				// append user names
				for (Iterator<String> i = userSet.iterator(); i.hasNext();)
				{
					try
					{
						User u = UserDirectoryService.getUser(i.next());
						instructorName=instructorName.concat(u.getDisplayName()) + ",";
					}
					catch (Exception ee)
					{
						LOG.error(ee.getMessage(), ee);
					}
					
				}
			}
			// site description
			siteDescription = "";
		}
		catch (Exception e)
		{
			LOG.error(e.getMessage(), e);
		}
		
		// create xml doc for requry the course information
		Document doc = Xml.createDocument();
		Element root = doc.createElement("ITunesUDocument");
		doc.appendChild(root);
		
		// AddCourse node
		Element addCourseNode = doc.createElement("AddCourse");
		// parenthandle node
		String sectionHandle = getITunesParentHandle();
		writeStringNodeToDom(doc, addCourseNode, "ParentHandle", sectionHandle);
		// TemplateHandle node
		String templateHandle = StringUtil.trimToNull(ServerConfigurationService.getString("template_handle"));
		writeStringNodeToDom(doc, addCourseNode, "TemplateHandle", templateHandle);
		
		//Course node
		Element courseNode = doc.createElement("Course");
		writeStringNodeToDom(doc, courseNode, "Name", siteName);
		writeStringNodeToDom(doc, courseNode, "ShortName", siteName);
		writeStringNodeToDom(doc, courseNode, "Identifier", siteId);
		writeStringNodeToDom(doc, courseNode, "Instructor", instructorName);
		writeStringNodeToDom(doc, courseNode, "Description", siteDescription);
		addCourseNode.appendChild(courseNode);			
		
		root.appendChild(addCourseNode);
		
		return Xml.writeDocumentToString(doc);
	}
	
	/**
	 * {@inheritDoc}
	 *
	 */
	public String getAddPermissionXml(String siteHandle, String siteId) 
	{
		// create xml doc for requry the course information
		Document doc = Xml.createDocument();
		Element root = doc.createElement("ITunesUDocument");
		doc.appendChild(root);
		
		// AddPermission node one: for the maintianer's permission
		Element addPermissionNode = doc.createElement("AddPermission");
		// parenthandle node
		writeStringNodeToDom(doc, addPermissionNode, "ParentHandle", siteHandle);
		//permission node
		Element permissionNode = doc.createElement("Permission");
		String maintainCredential = ServerConfigurationService.getString("site_maintain_credential", "") + ":" + siteId;
		writeStringNodeToDom(doc, permissionNode, "Credential", maintainCredential);
		writeStringNodeToDom(doc, permissionNode, "Access", "Edit");
		addPermissionNode.appendChild(permissionNode);
		
		root.appendChild(addPermissionNode);
		
		// AddPermission node two: for the member's permission
		Element addPermissionNode2 = doc.createElement("AddPermission");
		// parenthandle node
		writeStringNodeToDom(doc, addPermissionNode2, "ParentHandle", siteHandle);
		
		Element permissionNode2 = doc.createElement("Permission");
		String memberCredential = ServerConfigurationService.getString("site_member_credential", "") + ":" + siteId;
		writeStringNodeToDom(doc, permissionNode2, "Credential", memberCredential);
		writeStringNodeToDom(doc, permissionNode2, "Access", "Download");
		addPermissionNode2.appendChild(permissionNode2);
		
		root.appendChild(addPermissionNode2);
		
		return Xml.writeDocumentToString(doc);
	}
	
	
    /**
	 * {@inheritDoc}
	 * 
	 */
	public String getDeleteCourseXml(String courseHandle)
	{
		// create xml doc for requry the course information
		Document doc = Xml.createDocument();
		Element root = doc.createElement("ITunesUDocument");
		doc.appendChild(root);
		
		// AddPermission node one: for the maintianer's permission
		Element deleteCourseNode = doc.createElement("DeleteCourse");
		writeStringNodeToDom(doc, deleteCourseNode, "CourseHandle", courseHandle);
		writeStringNodeToDom(doc, deleteCourseNode, "CoursePath", "");
		root.appendChild(deleteCourseNode);
		
		return Xml.writeDocumentToString(doc);
	}
	
    /**
	 * {@inheritDoc}
	 */
	public String getAddFeedGroupXml(String courseHandle, String feedGroupName, String feedGroupUrl, String feedGroupOwnerEmail)
	{
		// create xml doc for add feed group
		Document doc = Xml.createDocument();
		Element root = doc.createElement("ITunesUDocument");
		doc.appendChild(root);
		
		// Add group node
		Element addFeedGroup = doc.createElement("AddGroup");
		writeStringNodeToDom(doc, addFeedGroup, "ParentHandle", courseHandle);
		
		Element group = doc.createElement("Group");
		writeStringNodeToDom(doc, group, "Name", feedGroupName);
		writeStringNodeToDom(doc, group, "GroupType", "Feed");
		
		Element externalFeed = doc.createElement("ExternalFeed");
		writeStringNodeToDom(doc, externalFeed, "URL", feedGroupUrl);
		writeStringNodeToDom(doc, externalFeed, "OwnerEmail", feedGroupOwnerEmail);
		writeStringNodeToDom(doc, externalFeed, "PollingInterval", "Daily");
		writeStringNodeToDom(doc, externalFeed, "SecurityType", "None");
		writeStringNodeToDom(doc, externalFeed, "SignatureType", "None");
		
		group.appendChild(externalFeed);
		
		addFeedGroup.appendChild(group);
		
		root.appendChild(addFeedGroup);
		
		return Xml.writeDocumentToString(doc);
	}
    
	/**
	 * Utility routine to write a string node to the DOM.
	 */
	protected void writeStringNodeToDom(Document doc, Element parent, String nodeName, String nodeValue)
	{
		if (nodeValue != null && nodeValue.length() != 0)
		{
			Element name = doc.createElement(nodeName);
			Text t = doc.createTextNode(nodeValue);
			name.appendChild(t);
			parent.appendChild(name);
		}

		return;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean missingInitParams()
	{
		return iTunesU_site_id == null || iTunesU_site_sharedSecret == null || site_maintain_credential == null || site_member_credential == null || admin_credential == null || section_handle == null;
	}
    
}
