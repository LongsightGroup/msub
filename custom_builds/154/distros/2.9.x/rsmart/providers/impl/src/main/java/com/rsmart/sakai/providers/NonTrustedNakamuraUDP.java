package com.rsmart.sakai.providers;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.springframework.beans.BeanUtils;

import java.util.Collection;

/**
 *
 * This is the Sakai UDP to use in hyrid mode with OAE.  In the typical setup Basic LTI is going to
 * provision CLE users.  When it does this it appends the oauth_consumer_key to the userEid.  So all of the
 * CLE userEids look like "edu:jbush" where "edu" is the oauth_consumer_key.  This is a problem for SIS processing
 * which does not know this prefix, as well as calls back to OAE to lookup users, because OAE doesn't know about the
 * prefix either.
 *
 * So this UDP strips out the prefix for calls to lookup up users and then puts the prefix back afterwards.  In the cases
 * where the prefix is not detected, like from SIS batch loads, the prefix is prepended in the UserEdit object that is
 * returned.
 *
 * This provider always returns false to authentication calls, because its meant to be used in hybrid mode only.  There
 * is no direct cle access allowed by this provider.
 *
 * principal@com.rsmart.sakai.providers.NonTrustedNakamuraUDP=
 * validateUrl@com.rsmart.sakai.providers.NonTrustedNakamuraUDP=
 * hostname@com.rsmart.sakai.providers.NonTrustedNakamuraUDP=
 *
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 3/1/12
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NonTrustedNakamuraUDP implements UserDirectoryProvider {
    private static final Log logger = LogFactory.getLog(NonTrustedNakamuraUDP.class);

    private String prefix;
    private String prefixPlusDelim;

    private boolean authenticateWithProviderFirst = true;

    private Cache foundUserCache;

    private static final Log LOG = LogFactory.getLog(NonTrustedNakamuraUDP.class);


	/**
	 * The Nakamura RESTful service to validate authenticated users. A good
	 * default for common hybrid implementations is supplied.
	 *
	 */
	protected String userLookupUrl = "http://localhost:8080/system/userManager/user/";

    protected String userLookupByEmailUrl = "http://localhost:8080/var/search/";

    protected String defaultType = "registered";

	/**
	 * The nakamura user that has permissions to GET
	 * /var/cluster/user.cookie.json. A good default for common hybrid
	 * implementations is supplied.
	 *
	 * @see org.sakaiproject.hybrid.util.XSakaiToken#createToken(String, String)
	 */
	protected String principal = "admin";

	/**
	 * The hostname we will use to lookup the sharedSecret for access to
	 * validateUrl. A good default for common hybrid implementations is
	 * supplied.
	 *
	 * @see org.sakaiproject.hybrid.util.XSakaiToken#createToken(String, String)
	 */
	protected String hostname = "localhost";

	// dependencies
	ComponentManager componentManager; // injected
	ThreadLocalManager threadLocalManager; // injected
	ServerConfigurationService serverConfigurationService; // injected
	NakamuraAuthenticationHelper nakamuraAuthenticationHelper;

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#authenticateUser(java.lang.String,
	 *      org.sakaiproject.user.api.UserEdit, java.lang.String)
	 */
	public boolean getUserFromNakamura(String eid, UserEdit edit) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUserFromNakamura(String " + eid + ", UserEdit " + edit);
		}
		if (eid == null || "null".equalsIgnoreCase(eid) || "".equals(eid)) {
			// maybe should throw exception instead?
			// since I assume I am in a chain, I will be quiet about it
			LOG.debug("eid == null");
			return false;
		}
		final NakamuraAuthenticationHelper.AuthInfo authInfo = nakamuraAuthenticationHelper
				.getPrincipalLoggedIntoNakamura(eid);
		if (authInfo != null) {
			if (eid.equalsIgnoreCase(authInfo.getPrincipal())) {
				edit.setEid(authInfo.getPrincipal());
				edit.setFirstName(authInfo.getFirstName());
				edit.setLastName(authInfo.getLastName());
				edit.setEmail(authInfo.getEmailAddress());
                edit.setType(defaultType);
                LOG.debug("found user: " + eid + " in nakamura");
				return true;
			}
		}
        LOG.debug("can't find user: " + eid + " in nakamura");

		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#authenticateWithProviderFirst(java.lang.String)
	 */
	public boolean authenticateWithProviderFirst(String eid) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("authenticateWithProviderFirst(String " + eid + ")");
		}
		return authenticateWithProviderFirst;
	}



	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#getUser(org.sakaiproject.user.api.UserEdit)
	 */
	public boolean getUser(UserEdit edit) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getUser(UserEdit " + edit + ")");
		}
		/*
		 * For some crazy reason, the not-null String "null" can be passed in
		 * edit.getEid(). Very odd behavior indeed.
		 */
		if (edit != null) {
			final String eid = edit.getEid();
			if (eid != null && !"null".equalsIgnoreCase(eid)) {
                String prefixedEid = eid;

                if (!eid.startsWith(prefixPlusDelim)) {
                    prefixedEid = buildCleEid(eid);
                }

                if (foundUserCache.isKeyInCache(prefixedEid)) {
                    Element element = foundUserCache.get(prefixedEid);
                    if (element != null && element.getObjectValue() != null) {
                        UserEdit loadedEdit =  (UserEdit) element.getObjectValue();
                        if (edit.getId() == null) {
                            BeanUtils.copyProperties(loadedEdit, edit);
                        } else {
                            if (!edit.getId().equals(loadedEdit.getId())) {
                                LOG.debug("User IDs do not match! edit=" + edit.getId() + "; loadedEdit="
                                        + loadedEdit.getId());
                            }
                            BeanUtils.copyProperties(loadedEdit, edit, new String[]{"id"});
                        }
                        LOG.debug("found user: " + prefixedEid + " in cache");
                        return true;
                    }
                }

				boolean found;

                if (eid.startsWith(prefixPlusDelim)) {
                    String adjustedEid = extractOaeEid(eid);
                    LOG.debug("adjusting eid:" + eid + " to " + adjustedEid);
                    found = getUserFromNakamura(adjustedEid, edit);
                    edit.setEid(eid);
                } else {
                    found = getUserFromNakamura(eid, edit);
                    LOG.debug("prefixing eid:" + eid + " to " + prefixedEid);
                    edit.setEid(prefixedEid);
                }

                if (found) {
                    foundUserCache.put(new Element(edit.getEid(), edit));
                }
                return found;
			}
		}

		return false;
	}

	/**
	 * @see org.sakaiproject.user.api.UserDirectoryProvider#getUsers(java.util.Collection)
	 */
	public void getUsers(Collection<UserEdit> users) {
        for (UserEdit edit: users) {
            getUser(edit);
        }
	}

	/**
	 * Initialize class.
	 */
	public void init() {
		LOG.debug("init()");
        if (prefixPlusDelim == null) {
            prefixPlusDelim = prefix + ":";
        }

		if (componentManager == null) { // may be in a test
			componentManager = org.sakaiproject.component.cover.ComponentManager
					.getInstance();
		}
        if (userLookupUrl != null && !"".equals(userLookupUrl)) {
            userLookupUrl = userLookupUrl.trim();
            if (!userLookupUrl.endsWith("/")){
                userLookupUrl += "/";
            }
        }
        if (userLookupByEmailUrl != null && !"".equals(userLookupByEmailUrl)) {
            userLookupByEmailUrl = userLookupByEmailUrl.trim();
            if (!userLookupByEmailUrl.endsWith("/")){
                userLookupByEmailUrl += "/";
            }
            userLookupByEmailUrl += "users.json?q=";
        }

		if (nakamuraAuthenticationHelper == null) {
			nakamuraAuthenticationHelper = new NakamuraAuthenticationHelper(
					componentManager, userLookupUrl, userLookupByEmailUrl, principal, hostname);
		}


	}

	/**
	 * @param threadLocalManager
	 *            the threadLocalManager to inject
	 */
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		LOG.debug("setThreadLocalManager(ThreadLocalManager threadLocalManager)");
		this.threadLocalManager = threadLocalManager;
	}

	/**
	 * @param componentManager
	 *            the componentManager to set
	 */
	public void setComponentManager(ComponentManager componentManager) {
		LOG.debug("setComponentManager(ComponentManager componentManager)");
		this.componentManager = componentManager;
	}

	/**
	 * @param serverConfigurationService
	 *            the serverConfigurationService to set
	 */
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		LOG.debug("setServerConfigurationService(ServerConfigurationService serverConfigurationService)");
		this.serverConfigurationService = serverConfigurationService;
	}

    /**
     *
     * @param eid
     *        The user eid.
     * @param edit
     *        The UserEdit matching the eid to be authenticated (may be updated by the provider).
     * @param password
     *        The password.
     * @return we always return false, this provider does not provide authentication capability, only lookups, since
     *         its for use in hybrid mode, there will be no direct cle access allowed.
     */
    public boolean authenticateUser(String eid, UserEdit edit, String password) {
        return false;
    }

    protected String extractOaeEid(String eid) {
        String adjustedEid = eid.replaceFirst(prefixPlusDelim, "");
        logger.info("adjusting eid from " + eid + " to " + adjustedEid);
        return adjustedEid;
    }

    public boolean findUserByEmail(UserEdit edit, String email) {
        if (LOG.isDebugEnabled()) {
			LOG.debug("findUserByEmail(UserEdit " + edit + ", String " + email
					+ ")");
		}
		if (email == null) {
			LOG.debug("String email == null");
			return false;
		}

		final NakamuraAuthenticationHelper.AuthInfo authInfo = nakamuraAuthenticationHelper
				.getPrincipalByEmail(email);

		if (authInfo != null) {
			if (email.equalsIgnoreCase(authInfo.getEmailAddress())) {
                edit.setEid(buildCleEid(authInfo.getPrincipal()));
				edit.setFirstName(authInfo.getFirstName());
				edit.setLastName(authInfo.getLastName());
				edit.setEmail(authInfo.getEmailAddress());
				return true;
			}
		}

        return false;
    }

    private String buildCleEid(String eid) {
        return prefixPlusDelim + eid;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setPrefixPlusDelim(String prefixPlusDelim) {
        this.prefixPlusDelim = prefixPlusDelim;
    }

    public void setUserLookupUrl(String userLookupUrl) {
        this.userLookupUrl = userLookupUrl;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUserLookupByEmailUrl(String userLookupByEmailUrl) {
        this.userLookupByEmailUrl = userLookupByEmailUrl;
    }

    public void setAuthenticateWithProviderFirst(boolean authenticateWithProviderFirst) {
        this.authenticateWithProviderFirst = authenticateWithProviderFirst;
    }

    public void setFoundUserCache(Cache foundUserCache) {
        this.foundUserCache = foundUserCache;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }
}
