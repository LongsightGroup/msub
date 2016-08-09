package com.rsmart.sakai.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.springframework.beans.BeanUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/10/12
 * Time: 7:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class JenzabarUDP implements UserDirectoryProvider {
    private static final Log LOG = LogFactory.getLog(JenzabarUDP.class);

    private static final String UTF_8 = "UTF-8";

    // Jenzabar parameter names
    public static final String USERNAME_PARAM = "para0";
    public static final String PASSWORD_PARAM = "para1";

    private String logonUrl;

    private UserDirectoryService uds;

    @Override
    public boolean authenticateUser(String eid, UserEdit edit, String password) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            final URI uri = new URI(logonUrl + "?" +
                    USERNAME_PARAM + "=" + URLEncoder.encode(eid, UTF_8) + "&" +
                    PASSWORD_PARAM + "=" + URLEncoder.encode(password, UTF_8));

            final HttpGet httpget = new HttpGet(uri);
            final ResponseHandler<String> responseHandler = new BasicResponseHandler();

            LOG.debug("calling Jenzabar authentication at endpoint:" + logonUrl + " for eid:" + eid);

            String responseBody = httpClient.execute(httpget, responseHandler);

            LOG.debug("Jenzabar authentication call returned: " + responseBody);

            if (Boolean.parseBoolean(responseBody)) {
                LOG.info("Login succeeded for " + eid);

                User user = uds.getUserByEid(eid);

                LOG.debug("Found existing user: " + eid);

                if (user != null) {
                    BeanUtils.copyProperties(user, edit, new String[]{"id"});
                }

                return true;
            } else {
                LOG.info("Login failed for user: " + eid + ". Response from Jenzabar: " + responseBody);
            }
        } catch (HttpResponseException e) {
            LOG.info("Login failed for user: " + eid + ". Response from Jenzabar: " + e.getStatusCode() + " " + e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return false;
    }

    @Override
    public boolean authenticateWithProviderFirst(String eid) {
        return true;
    }

    @Override
    public boolean findUserByEmail(UserEdit edit, String email) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean getUser(UserEdit edit) {
        return false;
    }

    @Override
    public void getUsers(Collection<UserEdit> users) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setLogonUrl(String logonUrl) {
        this.logonUrl = logonUrl;
    }

    public void setUds(UserDirectoryService uds) {
        this.uds = uds;
    }
}
