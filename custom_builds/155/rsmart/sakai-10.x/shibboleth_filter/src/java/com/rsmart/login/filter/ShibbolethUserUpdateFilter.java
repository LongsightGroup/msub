package com.rsmart.login.filter;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.event.cover.UsageSessionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Oct 8, 2009
 * Time: 4:31:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShibbolethUserUpdateFilter
    implements Filter
{

    private static final Log
        LOG = LogFactory.getLog(ShibbolethUserUpdateFilter.class);

    private static String
        noUserRedirectUrl;
    private static UserAttributeResolver
        attributeResolver;
    private static String
        runAsUser;
    private static boolean
        updateUsers,
	    createUsers,
	    stripScoping,
        initialized = false;

    public void init(FilterConfig filterConfig) throws ServletException 
    {
	    LOG.debug ("ShibbolethUserUpdateFilter.init(...) called");
	    init();
    }

    public void init() throws ServletException
    {
        if (attributeResolver == null)
            attributeResolver = (UserAttributeResolver) ComponentManager.get("shib.userAttributeResolver");

        runAsUser = ServerConfigurationService.getString("shib.runas.user", "admin");
	    LOG.debug ("running user acct. maint. as: " + runAsUser);

        noUserRedirectUrl = ServerConfigurationService.getString("shib.nouser.redirect.url");
	    LOG.debug ("no user redirect URL: " + noUserRedirectUrl);

        updateUsers = ServerConfigurationService.getBoolean("shib.updateUsers", false);
	    LOG.debug (updateUsers ? "updating user accounts" : "not updating user accounts");

        createUsers = ServerConfigurationService.getBoolean("shib.createUsers", false);
	    LOG.debug (createUsers ? "creating user accounts" : "not creating user accounts");

        stripScoping = ServerConfigurationService.getBoolean("shib.stripEppnScoping",false);
	    LOG.debug (stripScoping ? "stripping scope from eppn" : "not stripping scope from eppn");

        initialized = true;

    }

    public void setUserAttributeResolver (UserAttributeResolver resolver)
    {
        attributeResolver = resolver;
    }

    public void destroy()
    {
        //no-op
    }

    protected void runAs(User runAsUser, HttpServletRequest req, Runnable block)
    {
        Session
            currentSession = SessionManager.getCurrentSession();
        Session
            adminSession = null;
        try
        {
            adminSession = SessionManager.startSession();
            SessionManager.setCurrentSession(adminSession);
            UsageSessionService.login(new org.sakaiproject.util.Authentication(runAsUser.getId(), runAsUser.getEid()), req);

            block.run();
        }
        catch (Throwable t)
        {
            LOG.error ("*** An error occured while attempting to add a user based on Shibboleth attributes ***", t);
        } finally {
            UsageSessionService.logout();

            if (adminSession != null)
                adminSession.invalidate();

            SessionManager.setCurrentSession(currentSession);
        }
    }

    protected void createOrUpdateUser (final String eid, final HttpServletRequest hReq)
	    throws ServletException
    {

        if (!initialized)
        {
            throw new ServletException ("Could not create or update user - ShibbolethUserUpdateFilter was not initilized.  Make sure init-method=\"init\" is specified in the bean definition for this filter, or that this filter is used directly in web.xml");
        }
        
        final UserDirectoryService
            uds = org.sakaiproject.user.cover.UserDirectoryService.getInstance();

        User adminUser = null;

        try
        {
            adminUser = uds.getUserByEid(runAsUser);
        }
        catch (UserNotDefinedException ex)
        {
            throw new ServletException("runAs user not found - unable to create a user account for '" + eid + "' based on Shibboleth attributes");
        }

        runAs(adminUser, hReq, new Runnable()
        {
            public void run()
            {
                try
                {
                    UserAttributes
                        info = attributeResolver.resolveAttributes(eid, hReq);
                    User
                        user = null;

                    try
                    {
                        user = uds.getUserByEid(eid);
                    }
                    catch (UserNotDefinedException ex)
                    {
                        user = null;
                    }

                    if (user == null && createUsers)
                    {
                        uds.addUser(null,
                                    eid,
                                    info.getFirstName(),
                                    info.getLastName(),
                                    info.getEmail(),
                                    null,
                                    info.getType(),
                                    null);
                    }
                    else if (user != null && updateUsers)
                    {
                        if (updateUsers)
                        {
                            LOG.debug ("Updating user '" + eid + "' with Shibboleth attributes");

                            UserEdit edit = uds.editUser(user.getId());
                            edit.setEmail(info.getEmail());
                            edit.setFirstName(info.getFirstName());
                            edit.setLastName(info.getLastName());
                            edit.setType(info.getType());
                            uds.commitEdit(edit);
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }

    protected String stripScope (String eid)
    {
    	int atIdx = eid.indexOf('@');

    	if (atIdx == -1)
    	    return eid;

    	return eid.substring(0, atIdx);
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException
    {
        final HttpServletRequest
            hReq = (HttpServletRequest)req;
        final HttpServletResponse
            hResp = (HttpServletResponse)resp;
        final Principal
            principal = hReq.getUserPrincipal();
        String
            eid = (principal == null ? null : principal.getName());

        LOG.debug ("eid obtained from HttpServletRequest.getUserPrincipal(): '" + eid + "'");

    	if (eid == null)
    	{
    	    eid = hReq.getHeader("REMOTE_USER");
    	    LOG.debug ("eid obtained from HttpServletRequest.getHeader(\"REMOTE_USER\"): '" + eid + "'");
        }

        if (eid == null || eid.equals(""))
        {
            LOG.debug ("eid is null, redirecting to '" + noUserRedirectUrl + "'");
            ((HttpServletResponse)resp).sendRedirect(noUserRedirectUrl);
            return;
        }

        if (stripScoping)
        {
                LOG.debug ("Stripping scope from eppn");
                eid = stripScope(eid);

                LOG.debug ("eid after stripped scope: '" + eid + "'");
        }

        if (!createUsers || !updateUsers) {
            UserDirectoryService uds = org.sakaiproject.user.cover.UserDirectoryService.getInstance();
            try {
                uds.getUserByEid(eid);
            } catch (UserNotDefinedException e) {
               LOG.warn("Can't find user with eid:" + eid +
                       " denying Shibboleth logon, try back again when you have a valid user account in sakai, or change your setup to have shib create users");
               ShibbolethDeniedRequestWrapper wrappedRequest = new ShibbolethDeniedRequestWrapper(hReq);
               chain.doFilter(wrappedRequest, resp);
               return;
            }
        }

        ShibbolethRequestWrapper
            wrappedRequest = new ShibbolethRequestWrapper (eid, hReq);

            if (createUsers || updateUsers)
            {
                LOG.debug ("committing Shibboleth attributes to user table");
                createOrUpdateUser(eid, (HttpServletRequest)wrappedRequest);
            }

        chain.doFilter(wrappedRequest, resp);
    }

    public class ShibbolethDeniedRequestWrapper extends HttpServletRequestWrapper
    {

        public ShibbolethDeniedRequestWrapper (HttpServletRequest req)
        {
            super(req);

        }

        public String getHeader (String key)
        {
            if (key == null)
            {
                LOG.error("Illegal request for HTTP header with NULL key in ShibbolethUserUpdateFilter.getHeader(...)");
                return null;
            }

	        LOG.debug ("getting header from ShibbolethRequestWrapper: " + key);
            if (key.equalsIgnoreCase("REMOTE_USER"))
            {
                return null;
            }
            else return super.getHeader(key);
        }

        public Enumeration getHeaders (String key)
        {
            if (key == null)
            {
                LOG.error("Illegal request for HTTP header with NULL key in ShibbolethUserUpdateFilter.getHeader(...)");
                return null;
            }

    	    LOG.debug ("getting headers from ShibbolethRequestWrapper: " + key);
            if (key.equalsIgnoreCase("REMOTE_USER"))
            {
                return new Vector(0).elements();
            }
            else return super.getHeaders(key);
        }

        public String getRemoteUser()
        {
            return null;
        }
    }

    public class ShibbolethRequestWrapper extends HttpServletRequestWrapper
    {
        private String eid;

        public ShibbolethRequestWrapper (String eid, HttpServletRequest req)
        {
            super(req);
            this.eid = eid;
        }

        public String getHeader (String key) 
        {
            if (key == null)
            {
                LOG.error("Illegal request for HTTP header with NULL key in ShibbolethUserUpdateFilter.getHeader(...)");
                return null;
            }

	        LOG.debug ("getting header from ShibbolethRequestWrapper: " + key);
            if (key.equalsIgnoreCase("REMOTE_USER"))
            {
                return eid;
            }
            else return super.getHeader(key);
        }

        public Enumeration getHeaders (String key)
        {
            if (key == null)
            {
                LOG.error("Illegal request for HTTP header with NULL key in ShibbolethUserUpdateFilter.getHeader(...)");
                return null;
            }

    	    LOG.debug ("getting headers from ShibbolethRequestWrapper: " + key);
            if (key.equalsIgnoreCase("REMOTE_USER"))
            {
                Vector
                    headers = new Vector(1);

	        	headers.addElement(eid);

		        return headers.elements();
            }
            else return super.getHeaders(key);
        }

        public String getRemoteUser()
        {
            return eid;
        }
    }
}