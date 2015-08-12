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

package com.rsmart.content.google.api;

import com.google.api.services.drive.Drive;
import com.rsmart.oauth.api.OAuthTokenService;
import com.rsmart.oauth.api.OAuthToken;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 * Defines the inteface for interacting with Google Docs.
 *
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Feb 3, 2010
 * Time: 12:48:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GoogleDocsService
{
    public static final String
        ROOT_FOLDER_ID              = "___ROOT___",
        ROOT_FOLDER_TITLE           = "Root";

    /**
     * Reports whether an OAuth Provider for Google has been registered with the OAuth service.
     * @return
     */
    public boolean isOAuthProviderRegistered();

    public boolean isOAuthProviderEnabled();
    
    /**
     * Reports whether this service has been properly initialized
     * @return
     */
    public boolean isInitialized();

    /**
     * Reports whether this service has been enabled.
     * @return
     */
    public boolean isEnabled();

    /**
     * Returns the default MIME type to use for the given Google Doc type (eg. spreadsheet, document, etc.)
     *
     * @param type
     * @return
     */
    public String getDefaultMimeTypeForType(String type);

    /**
     * Returns the OAuthTokenService for obtaining access to the Google OAuth Provider and the OAuth Tokens
     * associated with specific users or documents.
     *
     * @return
     */
    public OAuthTokenService getOAuthTokenService();

    /**
     * Returns the name of the OAuth provider which will be sought from the OAuthTokenManagerService
     * @return
     */
    public String getOAuthProviderName();

    /**
     * Generates the URL to which the user should be directed in order to forward a request for a token to the OAuth
     * provider.
     *
     * @param callbackURL
     * @return
     * @throws GoogleDocsException
     */
    public URL createOAuthRequestURL (String callbackURL)
        throws GoogleDocsException;

    /**
     * Generates the URL to which the user should be directed in order to forward a request for a token to the OAuth
     * provider.
     *
     * @param callbackURL
     * @return
     * @throws GoogleDocsException
     */
    public URL createOAuth2RequestURL (String callbackURL)
            throws GoogleDocsException;

    /**
     * Extracts the authorized OAuth token from the CGI query string appended to the URL which brought user back
     * from the OAuth provider.
     *
     * @param queryString
     * @return
     * @throws GoogleDocsException
     */
    public OAuthToken processAuthorizedAccessToken(String queryString)
        throws GoogleDocsException;

    /**
     * Reports whether an OAuth token for Google already exists for the current user.
     *
     * @return
     * @throws GoogleDocsException
     */
    public boolean userOAuthTokenExists()
        throws GoogleDocsException;

    /**
     * Returns the OAuth token (if it exists) for Google for the current user.
     *
     * @return
     * @throws GoogleDocsException
     */
    public OAuthToken getUserOAuthToken()
        throws GoogleDocsException;

    /**
     * Returns a Map containing the contents of the current user's Google Docs account. Each entry is keyed by the
     * document of folder's ID. The ID for the root folder will be GoogleDocsService.ROOT_FOLDER_ID. Each document or
     * folder is represented by a GoogleDocsDescriptor object.
     * @return
     * @throws GoogleDocsException
     */
    public Map <String, List<GoogleDocDescriptor>> getDirectoryTreeForUser()
        throws GoogleDocsException;

    public Map <String, List<GoogleDocDescriptor>> getDriveDirectoryTreeForUser()
            throws GoogleDocsException;

    /**
     * Returns the available folders within Google Docs for the current user.
     *
     * @return
     * @throws GoogleDocsException
     */
    public List getFoldersForUser()
        throws GoogleDocsException;

    /**
     * Returns the document length for the document represented by the submitted ID.
     *
     * @param entity
     * @return
     * @throws GoogleDocsException
     */
    public int getDocumentLength(GoogleDocDescriptor entity)
        throws GoogleDocsException;

    /**
     * Get's the MIME type for the supplied document in order to set the type in an HTTP response.
     * @param entity
     * @return
     * @throws GoogleDocsException
     */
    public String getDocumentMimeType(GoogleDocDescriptor entity)
        throws GoogleDocsException;

    /**
     * Returns the possible MIME types which might be used for the given Google Docs type.
     *
     * @param type
     * @return
     * @throws GoogleDocsException
     */
    public Map<String, String> getMimeTypesForType (String type)
        throws GoogleDocsException;

    /**
     * Obtains a stream for retrieving the content of the Google Document reprsented by the supplied GoogleDocDescriptor.
     *
     * @param entity
     * @return
     * @throws GoogleDocsException
     */
    public InputStream getDocumentInputStream(GoogleDocDescriptor entity)
        throws GoogleDocsException;

    /**
     * Converts a GoogleDocDescriptor to an XML representation.
     *
     * @param desc
     * @return
     * @throws GoogleDocsException
     */
    public String descriptorToXML (GoogleDocDescriptor desc)
        throws GoogleDocsException;

    /**
     * Constructs a GoogleDocDescriptor from the supplied XML document.
     *
     * @param xml
     * @return
     * @throws GoogleDocsException
     */
    public GoogleDocDescriptor xmlToDescriptor (String xml)
        throws GoogleDocsException;

    public OAuthToken processAccessCode(String accessCode, String callbackUrl);

    /**
     * Constructs a Google Drive Service Object
     *
     * @return
     */
    public Drive getDriveService(String userId);


    /**
     * Returns a String with a mime type Google can understand for creating the export url.
     *
     * @param entity
     * @return
     */
    public String getExportMimeTypeForEntity(GoogleDocDescriptor entity);
}