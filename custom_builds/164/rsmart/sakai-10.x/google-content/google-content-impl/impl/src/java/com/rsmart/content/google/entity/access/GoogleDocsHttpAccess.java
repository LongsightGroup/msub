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

package com.rsmart.content.google.entity.access;

import com.google.api.client.util.ByteStreams;
import com.google.api.services.drive.model.File;
import com.rsmart.content.google.api.GoogleDocsException;
import com.rsmart.content.google.api.GoogleDocsServiceNotConfiguredException;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.content.impl.BaseContentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rsmart.content.google.api.GoogleDocumentMissingException;
import com.rsmart.content.google.api.GoogleServiceException;
import com.rsmart.content.google.api.GoogleOAuthConfigurationException;
import com.rsmart.content.google.api.GoogleOAuthTokenNotFoundException;
import com.rsmart.content.google.entity.GoogleContentEntity;
import com.rsmart.content.google.entity.GoogleContentResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

/**
 * This HttpAccess implementation works with the Access servlet to permit download of Google Documents
 * linked into Sakai resources.
 *
 * User: duffy
 * Date: Mar 4, 2010
 * Time: 9:02:48 AM
 */
public class GoogleDocsHttpAccess
    implements HttpAccess
{
    private final static Log
        LOG = LogFactory.getLog (GoogleDocsHttpAccess.class);

    private final static String
        ERROR_NODOCUMENT                    = "error.document",
        ERROR_NOGOOGLE                      = "error.google",
        ERROR_NOCONFIG                      = "error.config",
        ERROR_NOTOKEN                       = "error.token",
        ERROR_UNSPECIFIED                   = "error.unspecified";

    private final static ResourceLoader
        rl = new ResourceLoader("googlehttpsaccess-messages");

    /**
     * Streams an exception page into the HttpServletResponse when a RuntimeException occurs.
     *
     * @param req
     * @param res
     * @param re
     * @return
     */
    private final RuntimeException handleException (HttpServletRequest req, HttpServletResponse res,
                                                    RuntimeException re)
    {
        Throwable
            t = re.getCause();
        String
            errorString = null;

        if (t instanceof GoogleDocumentMissingException)
        {
            errorString = ERROR_NODOCUMENT;
        }
        else if (t instanceof GoogleServiceException)
        {
            errorString = ERROR_NOGOOGLE;
        }
        else if (t instanceof GoogleOAuthConfigurationException)
        {
            errorString = ERROR_NOCONFIG;
        }
        else if (t instanceof GoogleOAuthTokenNotFoundException)
        {
            errorString = ERROR_NOTOKEN;
        }
        else if (t instanceof GoogleDocsServiceNotConfiguredException)
        {
            errorString = ERROR_NOCONFIG;
        }
        else if (t instanceof GoogleDocsException)
        {
            errorString = ERROR_UNSPECIFIED;
        }

        if (errorString != null)
        {
            ServletOutputStream
                out = null;
            StringBuffer
                sb = new StringBuffer();

            sb.append("<html><body><h1>");
            sb.append(rl.getString("error.title"));
            sb.append("</h1><p>");
            sb.append(rl.getString(errorString));
            sb.append("</p></body></html>");

            try
            {
                out = res.getOutputStream();
                out.print(sb.toString());
            }
            catch (IOException e)
            {
                LOG.error("Error writing error page", e);
            }

            return null;
        }

        return re;
    }

    /**
     * Determines if the current user has "content.read" permissions for the supplied reference value.
     *
     * @param reference
     * @return
     */
    private boolean checkSecurity (String reference)
    {
        return SecurityService.unlock("content.read", reference);
	}


    public void handleAccess(HttpServletRequest req, HttpServletResponse res,
                             Reference ref, Collection copyrightAcceptedRefs)
        throws EntityPermissionException, EntityNotDefinedException,
               EntityAccessOverloadException, EntityCopyrightException
    {
        if (ref == null)
            throw new EntityNotDefinedException ("Document does not exist");

        if (!checkSecurity(ref.getReference()))
        {
            throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), "content.read", ref.getReference());
        }

        final GoogleContentEntity
            googleEnt = (GoogleContentEntity)ref.getEntity();

        ResourceProperties
            properties = googleEnt.getProperties();

        if (properties.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null && !copyrightAcceptedRefs.contains(ref.getReference()))
        {
            throw new EntityCopyrightException(ref.getReference());
        }

        if (googleEnt != null && googleEnt.isResource())
        {
            final GoogleContentResource googleDoc = (GoogleContentResource)googleEnt;
            OutputStream out = null;
            InputStream in = null;

            try {
                if (ServerConfigurationService.getBoolean("google-content.useOAuth2", false)) {
                    out = res.getOutputStream();
                    in = googleDoc.streamDriveContent();

                    res.setContentType(googleDoc.getExportMimeType());
                    ByteStreams.copy(in, out);

                    out.flush();
                } else {
                    String contentType = null;
                    int buffLen = res.getBufferSize();
                    long contentLen = -1;

                    try {
                        contentType = googleDoc.getContentType();
                        contentLen = googleDoc.getContentLength();
                    } catch (RuntimeException re) {
                        RuntimeException toThrow = handleException(req, res, re);
                        if (toThrow != null) {
                            throw toThrow;
                        }
                        return;
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Streaming Google Document: " + req.getPathInfo());
                        LOG.debug("content type: " + contentType);
                        LOG.debug("content len: " + contentLen);
                        LOG.debug("output buffer len: " + buffLen);
                    }

                    out = res.getOutputStream();
                    in = googleDoc.streamContent();

                    //these are set after establishing the stream so error pages
                    // can be displayed if fetching data fails for a protocol issue
                    // or a missing document
                    res.setContentType(contentType);
                    res.setContentLength((int) contentLen);

                    int readLen = 0;
                    byte buff[] = new byte[buffLen];

                    while ((readLen = in.read(buff)) != -1) {
                        if (LOG.isDebugEnabled()) {
                            final String chunk = new String(buff);
                            LOG.debug("chunk from Google: [" + chunk + "], length:" + readLen);
                        }
                        out.write(buff, 0, readLen);
                    }
                }
            }
            catch (RuntimeException re)
            {
                RuntimeException
                    toThrow = handleException (req, res, re);

                if (toThrow != null)
                    throw toThrow;

                return;
            }
            catch (Exception e)
            {
                LOG.error ("Error streaming Google Document: " + req.getPathInfo(), e);
            }
            finally
            {
                try
                {
                    if (in != null)
                        in.close();
                    res.flushBuffer();
                }
                catch (IOException e)
                {
                    LOG.error ("Error closing streams for downloading google doc: " + req.getPathInfo(), e);
                }

            }
        }
    }
}