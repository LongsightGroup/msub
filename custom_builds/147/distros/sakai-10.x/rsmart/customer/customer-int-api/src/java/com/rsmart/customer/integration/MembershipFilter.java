package com.rsmart.customer.integration;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Oct 27, 2009
 * Time: 11:12:43 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MembershipFilter {
    /**
     * hook into the adding of users that allows a call to specify whether or not
     * the user membership should be processed.  This allows for internal sakai business logic
     * to override the wishes of the external SIS system.
     * @param user
     * @param site
     * @return
     */
    public boolean processMembership(User user, Site site, String role);
}
