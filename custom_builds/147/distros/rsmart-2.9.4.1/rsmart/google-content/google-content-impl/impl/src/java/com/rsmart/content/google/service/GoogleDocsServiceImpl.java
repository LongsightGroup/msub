/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.content.google.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.Link;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;
import com.rsmart.content.google.api.GoogleDocsServiceNotConfiguredException;
import com.rsmart.oauth.api.OAuthSignatureMethod;
import com.rsmart.oauth.api.OAuthTokenService;
import com.rsmart.oauth.api.OAuthProvider;
import com.rsmart.oauth.api.OAuthToken;
import com.rsmart.persistence.PersistenceException;
import com.rsmart.persistence.NoSuchObjectException;
import com.rsmart.content.google.api.GoogleDocsException;
import com.rsmart.content.google.api.GoogleDocsService;
import com.rsmart.content.google.api.GoogleDocDescriptor;
import com.rsmart.content.google.api.GoogleOAuthConfigurationException;
import com.rsmart.content.google.api.GoogleOAuthTokenNotFoundException;
import com.rsmart.content.google.api.GoogleServiceException;
import com.rsmart.content.google.api.GoogleDocumentMissingException;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GoogleDocsServiceImpl implements GoogleDocsService
{
    private static final Log
        LOG = LogFactory.getLog(GoogleDocsServiceImpl.class);

    private static final String
        URL_FEEDS                   = "https://docs.google.com/feeds",
        URL_SPREADSHEETS            = "https://spreadsheets.google.com/feeds",
        URL_DEFAULT                 = "/default",
        URL_DOCUMENT_FEED           = "/private/full",
        URL_DOCUMENT_CONTENT        = "/download/documents/export/Export?id=",
        URL_SPREADSHEET_CONTENT     = "/download/spreadsheets/Export?key=",
        URL_FORMAT                  = "&exportFormat=",
        URL_DOCTYPE_FOLDER          = "/-/folder",
        PARAM_FOLDERS               = "showfolders=true",
        PARAM_OAUTH_TOKEN_SECRET    = "oauth_token_secret=";

    private final static String
        XML_GOOGLEDOC               = "googledoc",
        XML_TITLE                   = "title",
        XML_LINK                    = "link",
        XML_DOCID                   = "docId",
        XML_OWNERID                 = "ownerId",
        XML_TYPE                    = "type",
        XML_EXPORTTYPE              = "exportType",
        XML_WORKSHEETCOUNT          = "worksheetCount",
        XML_WORKSHEET               = "worksheet",
        XML_VERSIONID               = "versionId";

    private final Map<String, String>
        DOCUMENT_TYPES;
    {
        DOCUMENT_TYPES = new HashMap<String, String>();
        DOCUMENT_TYPES.put ("docx", "Microsoft Word (.docx)");
        DOCUMENT_TYPES.put ("txt", "Plain Text (.txt)");
        DOCUMENT_TYPES.put ("odt","OpenDocument Text (.odt)");
        DOCUMENT_TYPES.put ("pdf", "Portable Document Format (.pdf)");
        DOCUMENT_TYPES.put ("png", "Portable Network Graphics (.png)");
        DOCUMENT_TYPES.put ("rtf", "Rich Text Format (.rtf)");
        DOCUMENT_TYPES.put ("html", "HyperText Markup Language (.html)");
        DOCUMENT_TYPES.put ("zip", "Zip Archive Format (.zip)");
    }

    private final Map<String, String>
        PRESENTATION_TYPES;
    {
        PRESENTATION_TYPES = new HashMap<String, String>();
        PRESENTATION_TYPES.put("pdf", "Portable Document Format (.pdf)");
        PRESENTATION_TYPES.put("png", "Portable Network Graphics (.png)");
        PRESENTATION_TYPES.put("ppt", "Microsoft PowerPoint (.ppt)");
        PRESENTATION_TYPES.put("swf", "Shockwave Flash (.swf)");
        PRESENTATION_TYPES.put("txt", "Plain Text (.txt)");
    }

    private final Map<String, String>
        SPREADSHEET_TYPES;
    {
        SPREADSHEET_TYPES = new HashMap<String, String>();
        SPREADSHEET_TYPES.put("xlsx", "Microsoft Excel (.xlsx)");
        SPREADSHEET_TYPES.put("ods", "OpenDocument Spreadsheet (.ods)");
        SPREADSHEET_TYPES.put("pdf", "Portable Document Format (.pdf)");
        SPREADSHEET_TYPES.put("csv", "Comma Separated Values (.csv)");
        SPREADSHEET_TYPES.put("tsv", "Tab Separated Values (.tsv)");
        SPREADSHEET_TYPES.put("html", "HyperText Markup Language (.html)");
    }

    private final Map<String, String>
        SPREADSHEET_FMT_MAP;
    {
        SPREADSHEET_FMT_MAP = new HashMap<String, String>();
        SPREADSHEET_FMT_MAP.put("xlsx", "4");
        SPREADSHEET_FMT_MAP.put("ods", "13");
        SPREADSHEET_FMT_MAP.put("pdf", "12");
        SPREADSHEET_FMT_MAP.put("csv", "5");
        SPREADSHEET_FMT_MAP.put("tsv", "23");
        SPREADSHEET_FMT_MAP.put("html", "102");
    }

    private static String
        DEFAULT_DOCUMENT_EXPORT_TYPE        = null,
        DEFAULT_SPREADSHEET_EXPORT_TYPE     = null,
        DEFAULT_PRESENTATION_EXPORT_TYPE    = null;

    private static String
            DEFAULT_DOCUMENT_MIME_TYPE        = "application/vnd.oasis.opendocument.text",
            DEFAULT_SPREADSHEET_MIME_TYPE     = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            DEFAULT_PRESENTATION_MIME_TYPE    = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            DEFAULT_PDF_MIME_TYPE             = "application/pdf",
            DEFAULT_FOLDER_MIME_TYPE          = "application/vnd.google-apps.folder";

    private static String
        SESSION_KEY_TEMPORARY_TOKEN         = "temporary.google.oauth.token",
        SESSION_KEY_TEMPORARY_TOKEN_SECRET  = "temporary.google.oauth.token.secret";

    private static String
        DRIVE_MY_FOLDERS_QUERY              = "mimeType = 'application/vnd.google-apps.folder' and 'me' in owners",
        DRIVE_MY_FILES_QUERY                = "mimeType != 'application/vnd.google-apps.folder' and 'me' in owners",
        DRIVE_SHARED_FOLDERS_QUERY          = "mimeType = 'application/vnd.google-apps.folder' and sharedWithMe",
        DRIVE_SHARED_FILES_QUERY            = "mimeType != 'application/vnd.google-apps.folder' and sharedWithMe";

    private OAuthTokenService
        tokenService = null;

    private String
        appName = null,
        oAuthProviderName = null;

    private boolean
        initialized = false;

    private Drive driveService;

    public static final String SPREADSHEETS_SERVICE_NAME = "wise";

    public GoogleDocsServiceImpl()
    {
        //set the application name to transmit to Google
        final StringBuffer
            sbName = new StringBuffer();

        //Google requests that the application name be of the format company-application-version
        sbName.append (ServerConfigurationService.getString("ui.inst", "rSmart"))
              .append ('-')
              .append (ServerConfigurationService.getString("ui.service", "Sakai CLE"))
              .append ('-')
              .append (ServerConfigurationService.getString("version.service", "2.7.1.0"));

        setApplicationName(sbName.toString());
    }

    private final void initializeProvider()
        throws GoogleDocsException
    {
        final OAuthTokenService
            tokenSvc = getOAuthTokenService();

        /*if (isOAuthProviderRegistered())
        {
            if (!isOAuthProviderEnabled())
            {
                LOG.error("Google OAuth Provider is disabled - could not initialize Google Docs Service");
                throw new GoogleDocsServiceNotConfiguredException("The Google OAuth Provider is disabled which may reflect a need to configure the provider through the OAuth Administration tool");
            }
            LOG.debug ("Google OAuth Provider was found during Google Docs Service initialization");
            return;
        }

        if (!ServerConfigurationService.getBoolean("google-content.loadOAuthProvider", false))
        {
            LOG.debug ("Google OAuth provider was not found and google-content.loadOAuthProvider is not set to true; no provider is registered; initialization has failed");
            throw new GoogleDocsServiceNotConfiguredException("No OAuth Provider is registered and google-content.loadOAuthProvider is not set to true");
        }

        //that's ok, we'll look for the configuration in the application context and create the provider
        LOG.debug ("Google OAuth provider was not found in database - provider will be registered from Spring configuration");

        //get the provider details from the application context
        OAuthProvider
            googleProvider = (OAuthProvider) ComponentManager.get("google.oauth.provider");

        if (googleProvider == null)
        {
            final String
                msg = "Google OAuth provider is not registered and is not configured in the component configuration file - GoogleDocsService initialization failed";

            LOG.error (msg);

            throw new GoogleDocsServiceNotConfiguredException (msg);
        }

        //dump some output
        if (LOG.isDebugEnabled())
        {
            StringBuilder
                sb = new StringBuilder("provider details from config:");

            sb.append("\n\tname: ").append(googleProvider.getProviderName())
              .append("\n\tdescription: ").append(googleProvider.getDescription())
              .append("\n\tclientId: ").append(googleProvider.getClientId())
              .append("\n\tclientSecret: ").append(googleProvider.getClientSecret())
              .append("\n\tauthUrl: ").append(googleProvider.getAuthUrl())
              .append("\n\ttokenUrl: ").append(googleProvider.getTokenUrl())
              .append("\n\tscope: ").append(googleProvider.getAdditionalHeaders().get("scope"));

            LOG.debug(sb.toString());
        }

        try
        {
            OAuthProvider
                provider = tokenSvc.createOAuthProvider
                        (googleProvider.getProviderName(),
                         googleProvider.getDescription(),
                         googleProvider.getClientId(),
                         googleProvider.getClientSecret(),
                         googleProvider.getAuthUrl(),
                         googleProvider.getTokenUrl(),
                         googleProvider.getAdditionalHeaders(),
                         googleProvider.isEnabled());
        }
        catch (PersistenceException e)
        {
            LOG.error ("Google OAuth provider registration failed - GoogleDocsService failed to initialize");
            throw new GoogleDocsServiceNotConfiguredException ("Failed to register Google OAuth provider", e);
        }*/

        LOG.debug("GoogleDocsService initialiation complete");
    }

    private final void ensureInitialized()
        throws GoogleDocsException
    {
        if (!isInitialized())
            init();
    }

    public final boolean isEnabled()
    {
        return ServerConfigurationService.getBoolean("google-content.enabled", false);
    }

    public final boolean isInitialized()
    {
        return isEnabled() && initialized;
    }

    public final boolean isOAuthProviderRegistered()
    {
        try
        {
            return getOAuthTokenService().providerExists(getOAuthProviderName());
        }
        catch (PersistenceException e)
        {
            LOG.error ("An error occured trying to find the Google OAuth Provider", e);
            return false;
        }
    }

    public final boolean isOAuthProviderEnabled()
    {
        try
        {
            return getOAuthTokenService().getProviderStatus(getOAuthProviderName());
        }
        catch (PersistenceException e)
        {
            LOG.error ("An error occured trying to determine status of Google OAuth Provider", e);
            return false;
        }
    }

    public void safeInit()
    {
        if (!isEnabled())
            return;

        try
        {
            init();
        }
        catch (GoogleDocsException gde)
        {
            initialized = false;
            LOG.error ("initialization failed");
            return;
        }
    }

    public void init()
        throws GoogleDocsException
    {
        initializeProvider();

        DEFAULT_DOCUMENT_EXPORT_TYPE = ServerConfigurationService.getString("google-content.defaultMime.document", "pdf");
        DEFAULT_SPREADSHEET_EXPORT_TYPE = ServerConfigurationService.getString("google-content.defaultMime.spreadsheet", "xlsx");
        DEFAULT_PRESENTATION_EXPORT_TYPE = ServerConfigurationService.getString("google-content.defaultMime.presentation", "pdf");

        initialized = true;
    }

    public void setApplicationName (String name)
    {
        appName = name;
    }

    public String getApplicationName()
    {
        return appName;
    }
    
    public void setOAuthTokenService (OAuthTokenService svc)
    {
        tokenService = svc;
    }

    public OAuthTokenService getOAuthTokenService()
    {
        return tokenService;
    }

    public void setOAuthProviderName (String name)
    {
        oAuthProviderName = name;
    }

    public String getOAuthProviderName()
    {
        return oAuthProviderName;
    }

    private final OAuthProvider getOAuthProvider()
        throws GoogleOAuthConfigurationException
    {
        try
        {
            return getOAuthTokenService().getOAuthProviderByName(getOAuthProviderName());
        }
        catch (PersistenceException e)
        {
            throw new GoogleOAuthConfigurationException ("Error retrieving Google OAuth Provider registration", e);
        }
    }

    private final GoogleOAuthParameters getOAuthParametersForUser()
        throws GoogleDocsException
    {
        OAuthProvider
            provider = getOAuthProvider();
        OAuthToken
            token = getUserOAuthToken();

        GoogleOAuthParameters
            params = new GoogleOAuthParameters();

        params.setOAuthConsumerKey(provider.getConsumerKey());
        params.setOAuthConsumerSecret(provider.getHmacSha1SharedSecret());
        params.setOAuthToken(token.getTokenValue());
        params.setOAuthTokenSecret(token.getTokenSecret());

        return params;
    }

    private final GoogleOAuthParameters getOAuthParametersForDocument(GoogleDocDescriptor entity)
        throws GoogleOAuthConfigurationException, GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        final OAuthProvider
            provider = getOAuthProvider();

        if (!provider.isEnabled())
        {
            throw new GoogleOAuthConfigurationException ("Google Docs OAuth Provider is disabled");
        }
        
        OAuthToken
            token = getOAuthToken (entity.getOwnerId());
        
        GoogleOAuthParameters
            params = new GoogleOAuthParameters();

        params.setOAuthConsumerKey(provider.getConsumerKey());
        params.setOAuthConsumerSecret(provider.getHmacSha1SharedSecret());
        params.setOAuthToken(token.getTokenValue());
        params.setOAuthTokenSecret(token.getTokenSecret());

        return params;
    }

    private final PrivateKey getRSAKey(final OAuthProvider provider)
        throws GoogleOAuthConfigurationException
    {
        final String
            BEGIN = "-----BEGIN PRIVATE KEY-----",
            END = "-----END PRIVATE KEY-----",
            keyData = provider.getRsaSha1Key();
        final int
            BEGIN_LEN = BEGIN.length();

        String
            str = null;

        /*if (keyData.contains(BEGIN) && keyData.contains(END))
        {
            str = keyData.substring(keyData.indexOf(BEGIN) + BEGIN_LEN, keyData.lastIndexOf(END));
        }
        else
        {
            str = keyData;
        }*/

        try
        {
            final KeyFactory
                factory = KeyFactory.getInstance("RSA");
            final EncodedKeySpec
                privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(str));

            return factory.generatePrivate(privKeySpec);
        }
        catch (NoSuchAlgorithmException e)
        {
            LOG.error("RSA algorithm is not available for encoding of OAuth messages", e);
            throw new GoogleOAuthConfigurationException ("RSA algorithm is not available for encoding of OAuth messages", e);
        }
        catch (Base64DecoderException e)
        {
            LOG.error("Could not decode RSA key for Google OAuth Provider", e);
            throw new GoogleOAuthConfigurationException ("Could not decode RSA key for Google OAuth Provider", e);
        }
        catch (InvalidKeySpecException e)
        {
            LOG.error("Invalid RSA key spec for Google OAuth Provider", e);
            throw new GoogleOAuthConfigurationException ("Invalid RSA key spec for Google OAuth Provider", e);
        }
    }

    private final OAuthSigner getOAuthSigner()
            throws GoogleOAuthConfigurationException, OAuthException
    {
        final OAuthProvider
            provider = getOAuthProvider();

        OAuthSigner
            signer = null;

        switch (provider.getSignatureMethod())
        {
            case RSA_SHA1:
            {
                LOG.debug ("Creating OAuth Signer using RSA Key");
                return new OAuthRsaSha1Signer(getRSAKey(provider));
            }
            default:
            case HMAC_SHA1:
            {
                LOG.debug ("Creating OAuth Signer using HMAC Shared Secret");
                return new OAuthHmacSha1Signer();
            }
        }
    }

    private final DocsService getDocsServiceForUser()
        throws GoogleDocsException
    {
        DocsService
            service = new DocsService(appName);

        try
        {
            service.setOAuthCredentials(getOAuthParametersForUser(), getOAuthSigner());
        }
        catch (OAuthException e)
        {
            throw new GoogleDocsException ("Error attaching OAuth parameters for user to Docs Service", e);
        }

        return service;
    }

    private final SpreadsheetService getSpreasdsheetServiceforDocument (GoogleDocDescriptor entity)
        throws GoogleOAuthConfigurationException, GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        SpreadsheetService
            service = new SpreadsheetService(appName);

        try
        {
            service.setOAuthCredentials(getOAuthParametersForDocument(entity), getOAuthSigner());
        }
        catch (OAuthException e)
        {
            throw new GoogleDocsException ("Error attaching OAuth parameters for document to Spreadsheet Service", e);
        }

        return service;
    }

    private final DocsService getDocsServiceForDocument (GoogleDocDescriptor entity)
        throws GoogleOAuthConfigurationException, GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        DocsService
            service = new DocsService(appName);

        try
        {
            service.setOAuthCredentials(getOAuthParametersForDocument(entity), getOAuthSigner());
        }
        catch (OAuthException e)
        {
            throw new GoogleDocsException ("Error attaching OAuth parameters for document to Docs Service", e);
        }

        return service;
    }

    public URL createOAuthRequestURL (String callbackURL)
        throws GoogleDocsException
    {
        ensureInitialized();

        final OAuthProvider
            provider = getOAuthProvider();

        GoogleOAuthParameters
            oAuthParams = new GoogleOAuthParameters();


        oAuthParams.setOAuthConsumerKey(provider.getConsumerKey());
        if (OAuthSignatureMethod.HMAC_SHA1.equals(provider.getSignatureMethod()))
        {
            oAuthParams.setOAuthConsumerSecret(provider.getHmacSha1SharedSecret());
        }

        final Map<String, String>
            properties = provider.getAdditionalHeaders();
        final String
            scope = properties.get("scope");

        if (scope == null || scope.trim().length() < 1)
        {
            throw new GoogleDocsException ("'scope' additional header is not set for Google OAuth Provider - cannot create OAuth token request");
        }

        oAuthParams.setScope(scope);

        if (LOG.isDebugEnabled())
        {
            StringBuilder
                sb = new StringBuilder("OAuth params: ")
                    .append("\n\tconsumer key: ").append(oAuthParams.getOAuthConsumerKey())
                    .append("\n\tsignature method: ").append(oAuthParams.getOAuthSignatureMethod())
                    .append("\n\tscope: ").append(scope);

            LOG.debug(sb.toString());
        }

        StringBuilder
            sb = new StringBuilder (callbackURL).append("?");

        ToolSession
            tSess = SessionManager.getCurrentToolSession();

        if (tSess != null)
        {
            sb.append(Tool.PLACEMENT_ID).append("=").append(tSess.getPlacementId());
        }

        oAuthParams.setOAuthCallback(sb.toString());

        GoogleOAuthHelper
            oAuthHelper = null;

        try
        {
            oAuthHelper = new GoogleOAuthHelper(getOAuthSigner());
        } catch (OAuthException e)
        {
            throw new GoogleDocsException ("Could not obtain signer for Google OAuth messages", e);
        }

        try
        {
            oAuthHelper.getUnauthorizedRequestToken(oAuthParams);
        }
        catch (OAuthException e)
        {
            final Throwable
                t = e.getCause();

            if (IOException.class.isAssignableFrom (t.getClass()))
            {
                final IOException
                    ioe = (IOException)t;

                if (ioe.getMessage().contains("Server returned HTTP response code: 400"))
                {
                    throw new GoogleOAuthConfigurationException ("OAuth Parameters for Google OAuth Provider are likely incorrect", e);
                }
            }

            throw new GoogleDocsException ("Error obtaining unauthorized request token", e);
        }

/* CLE-6680
    Google's protocol changed and no longer returns an oauth_token_secret to the callback URL after the user
    authorizes their OAuth token. The workaround is to store the token and token secret for use when we get
    results from the callback.
 */
        ToolSession
            toolSession = SessionManager.getCurrentToolSession();

        toolSession.setAttribute(SESSION_KEY_TEMPORARY_TOKEN, oAuthParams.getOAuthToken());
        toolSession.setAttribute(SESSION_KEY_TEMPORARY_TOKEN_SECRET, oAuthParams.getOAuthTokenSecret());

        try
        {
            return new URL(oAuthHelper.createUserAuthorizationUrl(oAuthParams));
        }
        catch (MalformedURLException e)
        {
            toolSession.removeAttribute(SESSION_KEY_TEMPORARY_TOKEN);
            toolSession.removeAttribute(SESSION_KEY_TEMPORARY_TOKEN_SECRET);

            throw new GoogleDocsException ("Error forming authorization URL", e);
        }
    }

    public URL createOAuth2RequestURL (String callbackURL)
        throws GoogleDocsException
    {
        ensureInitialized();

        final OAuthProvider provider = getOAuthProvider();
        final Map<String, String> properties = provider.getAdditionalHeaders();
        final String scope = properties.get("scope");

        if (scope == null || scope.trim().length() < 1)
        {
            throw new GoogleDocsException ("'scope' additional header is not set for Google OAuth Provider - cannot create OAuth token request");
        }

        if (LOG.isDebugEnabled())
        {
            StringBuilder
                sb = new StringBuilder("OAuth params: ")
                    .append("\n\tClient Id: ").append(provider.getClientId())
                    .append("\n\tscope: ").append(scope);

            LOG.debug(sb.toString());
        }

        return new GoogleAuthorizationCodeRequestUrl(provider.getClientId(), callbackURL, Arrays.asList(scope.split(" "))).setAccessType("offline").toURL();
    }

    public OAuthToken processAuthorizedAccessToken(final String queryString)
        throws GoogleDocsException
    {
        ensureInitialized();

        final User
            user = UserDirectoryService.getCurrentUser();

        final OAuthProvider
            provider = getOAuthProvider();

        final OAuthTokenService
            tokenService = getOAuthTokenService();

/* CLE-6680
    Google's protocol changed and no longer returns an oauth_token_secret to the callback URL after the user
    authorizes their OAuth token. The workaround is to store the token and token secret for use when we get
    results from the callback.

    tempToken and tempTokenSecret are retrieved from the ToolSession and are used to obtain the real token
    and tokenSecret from Google
 */
        final ToolSession
            toolSession = SessionManager.getCurrentToolSession();
        final String
            tempToken = (String)toolSession.getAttribute(SESSION_KEY_TEMPORARY_TOKEN),
            tempTokenSecret = (String)toolSession.getAttribute(SESSION_KEY_TEMPORARY_TOKEN_SECRET);

        toolSession.removeAttribute(SESSION_KEY_TEMPORARY_TOKEN);
        toolSession.removeAttribute(SESSION_KEY_TEMPORARY_TOKEN_SECRET);

        if (tempToken == null || tempTokenSecret == null)
        {
            throw new GoogleDocsException ("Temporary OAuth token and token secret were not found - some error has occurred negotiating an authorized token with Google Docs");
        }
        final GoogleOAuthParameters
            oAuthParams = new GoogleOAuthParameters();

        oAuthParams.setOAuthConsumerKey(provider.getConsumerKey());
        oAuthParams.setOAuthConsumerSecret(provider.getHmacSha1SharedSecret());
        oAuthParams.setOAuthToken(tempToken);
        oAuthParams.setOAuthTokenSecret(tempTokenSecret);

        GoogleOAuthHelper
            oAuthHelper = null;

        try
        {
            oAuthHelper = new GoogleOAuthHelper(getOAuthSigner());
        }
        catch (OAuthException e)
        {
            throw new GoogleDocsException ("Could not obtain signer for Google OAuth messages", e);
        }

        oAuthHelper.getOAuthParametersFromCallback(queryString, oAuthParams);

        String
            accessToken = null,
            tokenSecret = null;

        try
        {
            accessToken = oAuthHelper.getAccessToken(oAuthParams);
            tokenSecret = oAuthParams.getOAuthTokenSecret();
        }
        catch (OAuthException e)
        {
            throw new GoogleDocsException ("Error: Authentication could not be approved or was declined", e);
        }

        try
        {
            return tokenService.createOAuthToken(provider.getUUID(), accessToken, tokenSecret, user.getId());
        }
        catch (PersistenceException e)
        {
            throw new GoogleDocsException ("Could not store OAuth token", e);
        }

    }

    public boolean userOAuthTokenExists()
        throws GoogleDocsException
    {
        ensureInitialized();

        final OAuthProvider
            provider = getOAuthProvider();

        User
            user = UserDirectoryService.getCurrentUser();

        try
        {
            final OAuthToken
                token = getOAuthTokenService().getOAuthToken(provider.getUUID(),
                                                            user.getId());

            return (token != null);
        }
        catch (NoSuchObjectException nsoe)
        {
            return false;
        }
        catch (PersistenceException e)
        {
            throw new GoogleDocsException ("Query for existing OAuth token failed", e);
        }
    }

    private final OAuthToken getOAuthToken (final String userId)
        throws GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        final OAuthProvider
            provider = getOAuthProvider();

        try
        {
            return getOAuthTokenService().getOAuthToken(provider.getUUID(), userId);
        }
        catch (PersistenceException e)
        {
            throw new GoogleOAuthTokenNotFoundException ("Error fetching OAuth token for user",e);
        }
    }

    public OAuthToken getUserOAuthToken()
        throws GoogleDocsException
    {
        ensureInitialized();

        final String
            userId = UserDirectoryService.getCurrentUser().getId();

        return getOAuthToken (userId);
    }

    private void getWorksheetInfo (GoogleDocDescriptor entity)
        throws GoogleOAuthConfigurationException, GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        SpreadsheetService
            service = getSpreasdsheetServiceforDocument(entity);

        URL
            spreadsheetURL = null;
        try
        {
            spreadsheetURL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/" + entity.getDocId());
        }
        catch (MalformedURLException e)
        {
            throw new GoogleDocsException ("Could not forge a URL to the supplied spreadsheet", e);
        }

        try
        {
            SpreadsheetEntry
                spreadsheet = service.getEntry(spreadsheetURL, SpreadsheetEntry.class);
            List<WorksheetEntry>
                worksheets = spreadsheet.getWorksheets();

            if (worksheets != null)
            {
                entity.setWorksheetCount(worksheets.size());
                entity.setWorksheet(0);
            }
        }
        catch (Exception e)
        {
            entity.setWorksheetCount(-1);
            entity.setWorksheet(-1);
            
            throw new GoogleDocsException ("Error communicating with Google spreadsheet service", e);
        }
    }

    private void addEntry (Map<String, List<GoogleDocDescriptor>> folders, String parentId, DocumentListEntry entry, String userId)
        throws GoogleOAuthConfigurationException, GoogleOAuthTokenNotFoundException, GoogleDocsException
    {
        GoogleDocDescriptor
            entity = new GoogleDocDescriptor();

        entity.setTitle (entry.getTitle().getPlainText());
        entity.setType (entry.getType());
        entity.setDocId (entry.getDocId());
        entity.setLink (entry.getDocumentLink().getHref());
        entity.setOwnerId(userId);

        if ("spreadsheet".equals(entity.getType()))
        {
            try
            {
                getWorksheetInfo(entity);
            }
            catch(GoogleDocsException gde)
            {
                final StringBuilder
                    sb = new StringBuilder();

                sb.append ("Error getting data for spreadsheet: \n\tdoc id: ").append(entity.getDocId())
                  .append("\n\ttitle: ").append(entity.getTitle()).append("\n\n\texception: ").append(gde.getMessage());
                LOG.error (sb.toString(), gde);
            }
        }

        List<GoogleDocDescriptor>
            folder = folders.get(parentId);

        if (folder == null)
        {
            folder = new LinkedList<GoogleDocDescriptor>();
            folders.put (parentId, folder);
        }

        folder.add(entity);
    }

    private void addDriveEntry (Map<String, List<GoogleDocDescriptor>> folders, String parentId, File entry, String userId)
        throws GoogleDocsException
    {
        GoogleDocDescriptor
            entity = new GoogleDocDescriptor();

        entity.setTitle(entry.getTitle());
        entity.setType(getDefaultMimeType(entry.getMimeType()));
        entity.setDocId(entry.getId());
        entity.setLink(entry.getDefaultOpenWithLink());
        entity.setOwnerId(userId);

        List<GoogleDocDescriptor> folder = folders.get(parentId);

        if (folder == null)
        {
            folder = new LinkedList<GoogleDocDescriptor>();
            folders.put (parentId, folder);
        }

        folder.add(entity);
    }

    public Map<String, List<GoogleDocDescriptor>> getDirectoryTreeForUser()
        throws GoogleDocsException
    {
        ensureInitialized();

        String
            userId = UserDirectoryService.getCurrentUser().getId();

        DocsService
            service = getDocsServiceForUser();

        DocumentQuery
            query = null;

        try
        {
            query = new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full?showfolders=true"));
        }
        catch (MalformedURLException e)
        {
            throw new GoogleDocsException ("Google request URL " + query.getUrl().toString() + " is malformed - could not fetch directory", e);
        }

        DocumentListFeed
            dirFeed = null;

        try
        {
            dirFeed = service.getFeed(query, DocumentListFeed.class);
        }
        catch (Exception e)
        {
            throw new GoogleServiceException ("Error communicating with Google Docs Service", e);
        }

        HashMap<String, List<GoogleDocDescriptor>>
            folders = new HashMap<String, List<GoogleDocDescriptor>>();

        for (DocumentListEntry entry : dirFeed.getEntries())
        {
            List<Link>
                parents = entry.getParentLinks();
            String
                parentResourceId = ROOT_FOLDER_ID;

            if (parents == null || parents.size() < 1)
            {
                addEntry (folders, ROOT_FOLDER_TITLE, entry, userId);
            }
            else
            {
                for (Link link : parents)
                {
                    String
                        parentURL = null;
                    try
                    {
                        parentURL = URLDecoder.decode(link.getHref(), "UTF-8");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw new GoogleDocsException ("Error decoding parent ID", e);
                    }

                    int
                        idSep = parentURL.lastIndexOf(":");

                    if (idSep > 0 && idSep < parentURL.length())
                    {
                        String parentTitle = link.getTitle();

                        if (parentTitle != null && !"".equals(parentTitle)) {
                            parentResourceId = parentTitle;
                        } else {
                            parentResourceId = parentURL.substring(idSep + 1);
                        }
                    }

                    addEntry (folders, parentResourceId, entry, userId);
                }
            }

        }

        for (List<GoogleDocDescriptor> folder : folders.values())
        {
            Collections.sort (folder, new Comparator<GoogleDocDescriptor>()
            {
                public int compare(GoogleDocDescriptor folder1, GoogleDocDescriptor folder2)
                {
                    return folder1.getTitle().compareTo(folder2.getTitle());
                }
            });
        }

        return folders;
    }

    /* To get all the files and folders a user should be able to link to, the following queries are performed.
     * Folders owned by user, then all files where the folder id is the parent.
     * All files owned by the user (My Drive).
     * All folders shared with the user, and files where the folder id is the parent.
     * All files shared with the user (Shared with Me).
     */

    public Map<String, List<GoogleDocDescriptor>> getDriveDirectoryTreeForUser()
        throws GoogleDocsException
    {
        ensureInitialized();

        String userId = UserDirectoryService.getCurrentUser().getId();

        try {
            HashMap<String, List<GoogleDocDescriptor>> fileMap = new HashMap<String, List<GoogleDocDescriptor>>();
            Drive service = getDriveService(userId);

            List<File> myFoldersList = service.files().list().setMaxResults(1000).setQ(DRIVE_MY_FOLDERS_QUERY).execute().getItems();

            if (myFoldersList != null && !myFoldersList.isEmpty()) {
                for (File folder : myFoldersList) {

                    List<File> filesInFolder = service.files().list().setMaxResults(1000).setQ("mimeType != 'application/vnd.google-apps.folder' and '" + folder.getId() + "' in parents").execute().getItems();

                    if (filesInFolder != null && !filesInFolder.isEmpty()) {
                        for (File file : filesInFolder) {
                            addDriveEntry(fileMap, folder.getTitle(), file, userId);
                        }
                    }
                }
            }

            List<File> myFilesList = service.files().list().setMaxResults(1000).setQ(DRIVE_MY_FILES_QUERY).execute().getItems();

            if (myFilesList != null && !myFilesList.isEmpty()) {
                for (File file : myFilesList) {
                    addDriveEntry(fileMap, "My Drive", file, userId);
                }
            }

            List<File> sharedFoldersList = service.files().list().setMaxResults(1000).setQ(DRIVE_SHARED_FOLDERS_QUERY).execute().getItems();

            if (sharedFoldersList != null && !sharedFoldersList.isEmpty()) {
                for (File folder : sharedFoldersList) {

                    List<File> filesInFolder = service.files().list().setMaxResults(1000).setQ("mimeType != 'application/vnd.google-apps.folder' and '" + folder.getId() + "' in parents").execute().getItems();

                    if (filesInFolder != null && !filesInFolder.isEmpty()) {
                        for (File file : filesInFolder) {
                            addDriveEntry(fileMap, folder.getTitle(), file, userId);
                        }
                    }
                }
            }

            List<File> sharedFilesList = service.files().list().setMaxResults(1000).setQ(DRIVE_SHARED_FILES_QUERY).execute().getItems();

            if (sharedFilesList != null && !sharedFilesList.isEmpty()) {
                for (File file : sharedFilesList) {
                    addDriveEntry(fileMap, "Shared with Me", file, userId);
                }
            }

            return fileMap;

        } catch (IOException e) {
            LOG.error("IOException when attempting to retrieve file list from Google: " + e, e);
        }

        return null;
    }

    public Drive getDriveService(String userId) {

        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        try {
            OAuthProvider provider = getOAuthProvider();

            GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setClientSecrets(provider.getClientId(), provider.getClientSecret()).build();

            credential.setRefreshToken(tokenService.getOAuthToken(provider.getUUID(), userId).getTokenValue());

            return new Drive.Builder(httpTransport, jsonFactory, credential).build();

        } catch (GoogleOAuthConfigurationException e) {
            LOG.error("GoogleOAuthConfigurationException: " + e, e);
        } catch (Exception e) {
            LOG.error("Caught Exception: " + e, e);
        }

        return null;
    }

    public void setDriveService(Drive driveService){
        this.driveService = driveService;
    }

    public List<FolderEntry> getFoldersForUser()
        throws GoogleDocsException
    {
        ensureInitialized();

        URL
            requestURL = null;

        StringBuffer
            urlBuff = new StringBuffer(URL_FEEDS);

        urlBuff.append(URL_DEFAULT).append(URL_DOCUMENT_FEED);

        try
        {
            LOG.debug("Folder Request URL: " + urlBuff.toString());

            requestURL = new URL(urlBuff.toString());
        }
        catch (MalformedURLException e)
        {
            throw new GoogleDocsException ("Error forming URL to fetch folders", e);
        }

        DocumentQuery
            query = new DocumentQuery(requestURL);

        DocumentListFeed
            feed = null;

        DocsService
            service = getDocsServiceForUser();

        try
        {
            feed = service.getFeed(query, DocumentListFeed.class);
        }
        catch (Exception e)
        {
            throw new GoogleDocsException ("Error communicating with Google Docs Service", e);
        }

        List<DocumentListEntry>
            entries = feed.getEntries();
        List<FolderEntry>
            folders = new ArrayList<FolderEntry>();

        for (DocumentListEntry entry : entries)
        {
            if (entry.getKind().equals (FolderEntry.KIND))
            {
                folders.add((FolderEntry)entry);
            }
        }

        return folders;
    }

    private MediaSource getDocumentMediaSource (GoogleDocDescriptor entity)
        throws GoogleDocsException
    {
        DocsService
            google = getDocsServiceForDocument(entity);

        MediaContent
            content = new MediaContent();

        StringBuffer
            urlBuff = null;


        if ("spreadsheet".equals(entity.getType()))
        {
            urlBuff = new StringBuffer (URL_SPREADSHEETS);

            String
                type = entity.getExportType(),
                fmt = SPREADSHEET_FMT_MAP.get(type);

            if (fmt == null)
                fmt = new String("12");

            urlBuff.append(URL_SPREADSHEET_CONTENT).append(entity.getDocId()).append("&fmcmd=").append(fmt);

            if (type.equals("csv") || type.equals("tsv"))
            {
                urlBuff.append("&gid=").append(entity.getWorksheet() - 1);
            }
        }
        else
        {
            urlBuff = new StringBuffer(URL_FEEDS);

            urlBuff.append(URL_DOCUMENT_CONTENT).append(entity.getDocId());
            String
                exportType = entity.getExportType();

            if (exportType == null)
                exportType = "pdf";

            urlBuff.append(URL_FORMAT).append(exportType);
        }


        LOG.debug ("Google MediaSource URL: " + urlBuff.toString());
        content.setUri(urlBuff.toString());

        try
        {
            return google.getMedia(content);
        }
        catch (ResourceNotFoundException rnfe)
        {
            throw new GoogleDocumentMissingException("Document has moved or has been deleted", rnfe);
        }
        catch (Exception e)
        {
            throw new GoogleServiceException ("Error communicating with Google Docs Service", e);
        }
    }

    public int getDocumentLength (GoogleDocDescriptor entity)
        throws GoogleDocsException
    {
        ensureInitialized();

        final MediaSource
            source = getDocumentMediaSource(entity);

        final long
            len = source.getContentLength();

        if (len > Integer.MAX_VALUE)
        {
            LOG.warn("Google docuent length exceed Integer.MAX_VALUE");
        }
        
        return (int)len;
    }

    public String getDocumentMimeType (GoogleDocDescriptor entity)
        throws GoogleDocsException
    {
        ensureInitialized();

        final MediaSource
            source = getDocumentMediaSource(entity);

        return source.getContentType();
    }

    public Map<String, String> getMimeTypesForType(String type)
        throws GoogleDocsException 
    {
        ensureInitialized();

        if (type == null || "".equals(type)) {
            return null;
        }

        if (type.contains("document") && !type.contains("spreadsheet"))
            return DOCUMENT_TYPES;
        else if (type.contains("presentation"))
            return PRESENTATION_TYPES;
        else if (type.contains("spreadsheet"))
            return SPREADSHEET_TYPES;

        return null;
    }

    private String getDefaultMimeType(String type)
    {
        if (type.contains("document") && !type.contains("spreadsheet")) {
            return DEFAULT_DOCUMENT_MIME_TYPE;
        } else if (type.contains("spreadsheet")) {
            return DEFAULT_SPREADSHEET_MIME_TYPE;
        } else if (type.contains("presentation")) {
            return DEFAULT_PRESENTATION_MIME_TYPE;
        } else if (type.contains("folder")) {
            return DEFAULT_FOLDER_MIME_TYPE;
        }

        return DEFAULT_PDF_MIME_TYPE;
    }

    public String getDefaultMimeTypeForType(String type)
    {
        if ("document".equals(type))
            return DEFAULT_DOCUMENT_EXPORT_TYPE;
        else if ("spreadsheet".equals(type))
            return DEFAULT_SPREADSHEET_EXPORT_TYPE;
        else if ("presentation".equals(type))
            return DEFAULT_PRESENTATION_EXPORT_TYPE;

        return "pdf";
    }

    public InputStream getDocumentInputStream (GoogleDocDescriptor entity)
        throws GoogleDocsException
    {
        ensureInitialized();

        MediaSource
            source = getDocumentMediaSource(entity);

        InputStream
            inStream = null;

        try
        {
            inStream = source.getInputStream();
        }
        catch (IOException e)
        {
            throw new GoogleDocsException ("error getting input strem for Google Doc", e);
        }

        return inStream;
    }

    private final void addAttribute (final StringBuilder sb, final String name, final String value)
    {
        if(sb.charAt(sb.length() - 1) != ' ')
            sb.append (" ");

        sb.append(name).append("=\"").append(value).append("\"");
    }

    public String descriptorToXML(GoogleDocDescriptor desc)
        throws GoogleDocsException
    {
        ensureInitialized();

        //parse the XML descriptor
        DocumentBuilder
            db = null;
        try
        {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            LOG.debug ("Could not create XML documentbuilder");
            throw new GoogleDocsException ("Error creating XML documentbuilder", e);
        }

        if (desc.getDocId() == null)
            throw new GoogleDocsException ("DocID cannot be null");
        if (desc.getType() == null)
            throw new GoogleDocsException ("Type cannot be null");
        if (desc.getOwnerId() == null)
            throw new GoogleDocsException ("OwnerID cannot be null");

        Document
            d = db.newDocument();

        Element
            googleDoc = d.createElement(XML_GOOGLEDOC);

        d.appendChild(googleDoc);

        googleDoc.setAttribute(XML_DOCID, desc.getDocId());
        googleDoc.setAttribute(XML_TITLE, desc.getTitle());
        googleDoc.setAttribute(XML_TYPE, desc.getType());
        googleDoc.setAttribute(XML_EXPORTTYPE, desc.getExportType());
        googleDoc.setAttribute(XML_LINK, desc.getLink());
        googleDoc.setAttribute(XML_OWNERID, desc.getOwnerId());
        googleDoc.setAttribute(XML_WORKSHEETCOUNT, Integer.toString(desc.getWorksheetCount()));
        googleDoc.setAttribute(XML_WORKSHEET, Integer.toString(desc.getWorksheet()));

        // transform the Document into a String
        DOMSource
            domSource = new DOMSource(d);
        StringWriter
            sw = new StringWriter();

        try
        {
            TransformerFactory
                tf = TransformerFactory.newInstance();
            Transformer
                transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult
                sr = new StreamResult(sw);

            transformer.transform(domSource, sr);
        }
        catch (TransformerException te)
        {
            throw new GoogleDocsException ("Could not transform document descriptor into XML", te);
        }

        return sw.toString();
    }

    /**
     * Helper method for parsing XML descriptor
     * @param attr
     * @return
     */
    private final String getAttributeValue (Node attr)
    {
        if (attr == null)
            return null;

        return attr.getNodeValue();
    }

    public GoogleDocDescriptor xmlToDescriptor(String xml)
        throws GoogleDocsException
    {
        ensureInitialized();

        GoogleDocDescriptor
            desc = null;
        
        // no descriptor, not document
        if (xml == null)
            return null;

        //parse the XML descriptor
        DocumentBuilder
            db = null;
        try
        {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            LOG.debug ("Could not create XML parser");
            throw new GoogleDocsException ("Error creating XML parser", e);
        }

        Document
            d = null;
        try
        {
            d = db.parse(new ByteArrayInputStream(xml.getBytes()));
        }
        catch (SAXException e)
        {
            LOG.error ("XML parsing exception");
            throw new GoogleDocsException ("Error parsing resource XML descriptor", e);
        }
        catch (IOException e)
        {
            LOG.error ("XML parsing exception");
            throw new GoogleDocsException ("Error parsing resource XML descriptor", e);
        }

        //initialize the GoogleDocDescriptor
        desc = new GoogleDocDescriptor();

        Node
            docNode = null,
            tempNode = null;

        final NodeList
            nl = d.getChildNodes();
        for (int j = 0; j < nl.getLength(); ++j)
            if (nl.item(j).getNodeName() != null
                    && nl.item(j).getNodeName().equals(XML_GOOGLEDOC))
            {
                docNode = nl.item(j);
                break;
            }
        if (docNode == null)
            return null;

        final NamedNodeMap
            attrs = docNode.getAttributes();

        desc.setTitle(getAttributeValue(attrs.getNamedItem(XML_TITLE)));
        desc.setLink(getAttributeValue(attrs.getNamedItem(XML_LINK)));
        desc.setDocId(getAttributeValue(attrs.getNamedItem(XML_DOCID)));
        desc.setType(getAttributeValue(attrs.getNamedItem(XML_TYPE)));
        desc.setOwnerId(getAttributeValue(attrs.getNamedItem(XML_OWNERID)));
        desc.setExportType(getAttributeValue(attrs.getNamedItem(XML_EXPORTTYPE)));

        String
            intStr = null;

        intStr = getAttributeValue(attrs.getNamedItem(XML_WORKSHEETCOUNT));

        desc.setWorksheetCount(Integer.parseInt(intStr));

        intStr = getAttributeValue(attrs.getNamedItem(XML_WORKSHEET));

        desc.setWorksheet(Integer.parseInt(intStr));

        if (desc.getType() == null || desc.getOwnerId() == null || desc.getDocId() == null)
        {
            LOG.error ("Failed to parse XML descriptor - null fields");
            throw new GoogleDocsException ("Parsing XML descriptor failed; DocID, type and ownerId are required fields");
        }

        return desc;
    }

    @Override
    public OAuthToken processAccessCode(String accessCode, String callbackUrl) {

        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        try {
            //gets access token and refresh token
            GoogleTokenResponse accessTokenResponse = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory, getOAuthProvider().getClientId(), getOAuthProvider().getClientSecret(), accessCode, callbackUrl).execute();

            String userId = UserDirectoryService.getCurrentUser().getId();


            //Check if a refresh token exists for user, save it if none are found
            try {
                getOAuthToken(userId);
            } catch (GoogleOAuthTokenNotFoundException e) {
                try {
                    tokenService.createOAuth2Token(getOAuthProvider().getUUID(), accessTokenResponse.getRefreshToken(), userId);
                } catch (PersistenceException e1) {
                    e1.printStackTrace();
                }
            } catch (GoogleDocsException e) {
                e.printStackTrace();
            }

            //accessTokenResponse.g

            String str = "";

        } catch (GoogleOAuthConfigurationException e) {
            LOG.error("Caught GoogleOAuthConfigurationException: " + e, e);
        } catch (IOException e) {
            LOG.error("Caught IOException: " + e, e);
        }

        return null;
    }

    public String getExportMimeTypeForEntity(GoogleDocDescriptor entity){

        String entityExportType = entity.getExportType();

        if ((entityExportType == null || "".equals(entityExportType)) || "pdf".equals(entityExportType)) {
            return DEFAULT_PDF_MIME_TYPE;
        } else if (DOCUMENT_TYPES.containsKey(entityExportType)) {
            return DEFAULT_DOCUMENT_MIME_TYPE;
        } else if (SPREADSHEET_TYPES.containsKey(entityExportType)) {
            return DEFAULT_SPREADSHEET_MIME_TYPE;
        } else if (PRESENTATION_TYPES.containsKey(entityExportType)) {
            return DEFAULT_PRESENTATION_MIME_TYPE;
        }

        return DEFAULT_PDF_MIME_TYPE;
    }

}