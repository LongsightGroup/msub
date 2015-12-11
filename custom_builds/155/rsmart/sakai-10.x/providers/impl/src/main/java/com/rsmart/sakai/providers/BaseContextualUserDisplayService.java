package com.rsmart.sakai.providers;

import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 29, 2011
 * Time: 9:49:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseContextualUserDisplayService implements ContextualUserDisplayService {
    public String getUserDisplayId(User user) {
        return user.getEid();
    }

    public String getUserDisplayName(User user) {
        String rv = null;
        StringBuilder buf = new StringBuilder(128);
        if (user.getFirstName() != null) buf.append(user.getFirstName());
        if (user.getLastName() != null) {
            buf.append(" ");
            buf.append(user.getLastName());
        }

        if (buf.length() == 0) {
            rv = user.getEid();
        } else {
            rv = buf.toString();
        }
        return rv;
    }


    public String getUserDisplayId(User user, String s) {
        return user.getEid();
    }

    public String getUserDisplayName(User user, String s) {
        return getUserDisplayName(user);
    }
}
