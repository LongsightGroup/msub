/**
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.rsmart.sakai.providers;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hybrid.util.XSakaiToken;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import java.net.URI;

/**
 * Useful helper for interacting with Nakamura's authentication REST end-points.
 * Note: thread safe.
 */
@SuppressWarnings({"PMD.LongVariable", "PMD.CyclomaticComplexity"})
public class NakamuraAuthenticationHelper {
    /**
     * All sakai.properties settings will be prefixed with this string.
     */
    public static final String CONFIG_PREFIX = NakamuraAuthenticationHelper.class
            .getName();
    /**
     * sakai.properties The name of the nakamura anonymous principal.
     */
    public static final String CONFIG_ANONYMOUS = CONFIG_PREFIX + ".anonymous";

    private static final Log LOG = LogFactory
            .getLog(NakamuraAuthenticationHelper.class);

    /**
     * The key that will be used to cache AuthInfo hits in ThreadLocal. This
     * will handle cases where AuthInfo is requested more than once per request.
     */
    protected static final String THREAD_LOCAL_CACHE_KEY = NakamuraAuthenticationHelper.class
            .getName() + ".AuthInfo.cache";

    protected static final String THREAD_LOCAL_CACHE_KEY_BY_EMAIL = NakamuraAuthenticationHelper.class
            .getName() + ".AuthInfoByEmail.cache";


    /**
     * The anonymous nakamura principal name. A good default is provided. Must
     * be declared static to allow access from {@link AuthInfo} but must also be
     * mutable to allow configuration from sakai.properties.
     *
     * @see #CONFIG_ANONYMOUS
     * @see AuthInfo
     */
    @SuppressWarnings({"PMD.AssignmentToNonFinalStatic"})
    private static String anonymous = "anonymous";
    /**
     * The Nakamura RESTful service to validate users. A good
     * default is provided.
     */
    protected final transient String userLookupUrl;

    protected final transient String userLookupByEmailUrl;

    /**
     * The nakamura user that has permissions to GET
     * /var/cluster/user.cookie.json. A good default is provided.
     */
    protected final transient String principal;

    /**
     * The hostname we will use to lookup the sharedSecret for access to
     * validateUrl. A good default is provided.
     */
    protected final transient String hostname;

    /**
     * A simple abstraction to allow for proper unit testing
     */
    protected transient HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();

    protected transient ServerConfigurationService serverConfigurationService;
    protected transient XSakaiToken xSakaiToken;

    /**
     * Class is immutable and thread safe.
     *
     * @param userLookupUrl The Nakamura REST end-point we will use to validate the
     *                      cookie.
     * @param principal     The principal that will be used when connecting to Nakamura
     *                      REST end-point. Must have permissions to read
     *                      /var/cluster/user.cookie.json.
     * @param hostname      The hostname we will use to lookup the sharedSecret for access
     *                      to validateUrl
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public NakamuraAuthenticationHelper(
            final ComponentManager componentManager, final String userLookupUrl,
            final String userLookupByEmailUrl,
            final String principal, final String hostname) {
        if (componentManager == null) {
            throw new IllegalArgumentException("componentManager == null;");
        }

        serverConfigurationService = (ServerConfigurationService) componentManager
                .get(ServerConfigurationService.class);
        if (serverConfigurationService == null) {
            throw new IllegalStateException(
                    "serverConfigurationService == null");
        }
        if (userLookupUrl == null || "".equals(userLookupUrl)) {
            throw new IllegalArgumentException("userLookupUrl == null OR empty");
        }
        if (principal == null || "".equals(principal)) {
            throw new IllegalArgumentException("principal == null OR empty");
        }
        if (hostname == null || "".equals(hostname)) {
            throw new IllegalArgumentException("hostname == null OR empty");
        }
        if (userLookupByEmailUrl == null || "".equals(userLookupByEmailUrl)) {
            throw new IllegalArgumentException("userLookupByEmailUrl == null OR empty");
        }

        this.userLookupUrl = userLookupUrl;
        this.principal = principal;
        this.hostname = hostname;
        this.userLookupByEmailUrl = userLookupByEmailUrl;
        anonymous = serverConfigurationService.getString(CONFIG_ANONYMOUS,
                anonymous);

        xSakaiToken = new XSakaiToken(componentManager);
    }

    /**
     * calls nakamura to lookup a user by eid
     *
     * @param eid     - eid of user to lookup in Nakamura
     * @return
     */
    public AuthInfo getPrincipalLoggedIntoNakamura(final String eid) {
        LOG.debug("getPrincipalLoggedIntoNakamura()");


        @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
        AuthInfo authInfo = null;
        final HttpClient httpClient = httpClientProvider.getHttpClient();
        try {
            final URI uri = new URI(userLookupUrl + eid + ".json");
            final HttpGet httpget = new HttpGet(uri);
            // authenticate to Nakamura using x-sakai-token mechanism
            final String token = xSakaiToken.createToken(hostname,
                    principal);
            httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
            //
            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String responseBody = httpClient.execute(httpget,
                    responseHandler);
            authInfo = new AuthInfo(responseBody);
            if (authInfo.getPrincipal() == null) {
                return null;
            }
        } catch (HttpResponseException e) {
            // usually a 404 error - could not find cookie / not valid
            if (LOG.isDebugEnabled()) {
                LOG.debug("HttpResponseException: " + e.getMessage() + ": "
                        + e.getStatusCode() + ": " + userLookupUrl + eid + ".json");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }



        return authInfo;
    }

    /**
     * calls nakamura to lookup a user by email
     *
]     * @param email
     * @return
     */
    public AuthInfo getPrincipalByEmail(String email) {
        LOG.debug("getPrincipalByEmail()");

        @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
        AuthInfo authInfo = null;
        final HttpClient httpClient = httpClientProvider.getHttpClient();
        try {
            final URI uri = new URI(userLookupByEmailUrl + email);
            final HttpGet httpget = new HttpGet(uri);
            // authenticate to Nakamura using x-sakai-token mechanism
            final String token = xSakaiToken.createToken(hostname,
                    principal);
            httpget.addHeader(XSakaiToken.X_SAKAI_TOKEN_HEADER, token);
            //
            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            final String responseBody = httpClient.execute(httpget,
                    responseHandler);
            authInfo = new AuthInfo(responseBody, true);
            if (authInfo.getPrincipal() == null) {
                return null;
            }
        } catch (HttpResponseException e) {
            // usually a 404 error - could not find cookie / not valid
            if (LOG.isDebugEnabled()) {
                LOG.debug("HttpResponseException: " + e.getMessage() + ": "
                        + e.getStatusCode() + ": " + userLookupByEmailUrl + email);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return authInfo;
    }


    /**
     * Static final class for storing cached results from Nakamura lookup.
     * Generally the caller should expect raw results from the JSON parsing
     * (e.g. principal could in theory be null).
     */
    public static class AuthInfo {
        private static final String FIRST_NAME = "firstName";
        private static final String LAST_NAME = "lastName";
        private static final String EMAIL = "email";
        private static final String EMPTY_STRING = "";

        // PMD does not like the class name
        @SuppressWarnings("PMD.ProperLogger")
        private static final Log AILOG = LogFactory.getLog(AuthInfo.class);

        private transient String principal;
        private transient String firstName;
        private transient String lastName;
        private transient String emailAddress;

        protected AuthInfo(final String json) {
            this(json, false);
        }

        /**
         * @param json The JSON returned from nakamura.
         */
        protected AuthInfo(final String json, final boolean byEmail) {
            if (AILOG.isDebugEnabled()) {
                AILOG.debug("new AuthInfo(String " + json + ")");
            }


            if (byEmail) {
                parseUserFromResults(json);
            } else {
                parseUser(json);
            }


        }

        protected void parseUser(String json) {
            String principal = null;
            final JSONObject user;

            user = JSONObject.fromObject(json);

            if (user.has("name")) {
                principal = user.getString("name");
            }
            if (principal != null && !EMPTY_STRING.equals(principal)
                    && !anonymous.equals(principal)) {
                this.principal = principal;
            } else {
                this.principal = null;
            }
            if (user.has(FIRST_NAME)) {
                firstName = user.getString(FIRST_NAME);
            } else {
                firstName = EMPTY_STRING;
            }
            if (user.has(LAST_NAME)) {
                lastName = user.getString(LAST_NAME);
            } else {
                lastName = EMPTY_STRING;
            }
            if (user.has(EMAIL)) {
                emailAddress = user.getString(EMAIL);
            } else {
                emailAddress = EMPTY_STRING;
            }
        }

        protected void parseUserFromResults(String json) {
            //TODO JsonPath is so much nicer this is just icky
            JSONArray results = JSONObject.fromObject(json).getJSONArray("results");
            if (results.size() > 0) {
                JSONObject result = (JSONObject) results.get(0);
                String principal = null;
                if (result.has("userid")) {
                    principal = (String) result.get("userid");
                    if (principal != null && !EMPTY_STRING.equals(principal)
                            && !anonymous.equals(principal)) {
                        this.principal = principal;
                    } else {
                        this.principal = null;
                    }
                    if (result.has("basic")) {
                        JSONObject basic = (JSONObject) result.get("basic");
                        if (basic.has("elements")) {
                            JSONObject elements = (JSONObject) basic.get("elements");
                            if (elements.has(FIRST_NAME)) {
                                firstName = (String) elements.getJSONObject(FIRST_NAME).get("value");
                            } else {
                                firstName = EMPTY_STRING;
                            }
                            if (elements.has(LAST_NAME)) {
                                lastName = (String) elements.getJSONObject(LAST_NAME).get("value");
                            } else {
                                lastName = EMPTY_STRING;
                            }
                            if (elements.has(EMAIL)) {
                                emailAddress = (String) elements.getJSONObject(EMAIL).get("value");
                            } else {
                                emailAddress = EMPTY_STRING;
                            }

                        }
                    }
                }
            }
        }

        /**
         * @return the givenName
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @return the familyName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @return the emailAddress
         */
        public String getEmailAddress() {
            return emailAddress;
        }

        /**
         * @return the principal
         */
        public String getPrincipal() {
            return principal;
        }
    }

    /**
     * A simple abstraction to allow for unit testing of
     * {@link com.rsmart.sakai.providers.NakamuraAuthenticationHelper}.
     */
    public interface HttpClientProvider {
        /**
         * Get a reference to an {@link org.apache.http.client.HttpClient}
         *
         * @return the HttpClient
         */
        public HttpClient getHttpClient();
    }

    /**
     * Implementation is thread safe.
     */
    public static final class DefaultHttpClientProvider implements
            HttpClientProvider {
        private static final Log LOG = LogFactory
                .getLog(DefaultHttpClientProvider.class);

        /**
         * @see HttpClientProvider#getHttpClient()
         */
        public HttpClient getHttpClient() {
            LOG.debug("getHttpClient()");
            return new DefaultHttpClient();
        }

    }
}
