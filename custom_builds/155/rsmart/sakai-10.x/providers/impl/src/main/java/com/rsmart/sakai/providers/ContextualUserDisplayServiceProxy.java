package com.rsmart.sakai.providers;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.ContextualUserDisplayService;
import org.sakaiproject.user.api.User;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 29, 2011
 * Time: 9:45:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContextualUserDisplayServiceProxy implements ContextualUserDisplayService {
    private ContextualUserDisplayService contextualUserDisplayService;
    private String contextualUserDisplayServiceName;

    public void init(){
        if (contextualUserDisplayServiceName != null) {
            contextualUserDisplayService = (ContextualUserDisplayService) ComponentManager.get(contextualUserDisplayServiceName);
        }
    }

    public String getUserDisplayId(User user) {
        if (contextualUserDisplayService == null) return null;
        return contextualUserDisplayService.getUserDisplayId(user);
    }

    public String getUserDisplayName(User user) {
        if (contextualUserDisplayService == null) return null;

        return contextualUserDisplayService.getUserDisplayName(user);
    }

    public String getUserDisplayId(User user, String s) {
        if (contextualUserDisplayService == null) return null;

        return contextualUserDisplayService.getUserDisplayId(user, s);
    }

    public String getUserDisplayName(User user, String s) {
        if (contextualUserDisplayService == null) return null;

        return contextualUserDisplayService.getUserDisplayName(user, s);
    }

    public void setContextualUserDisplayService(ContextualUserDisplayService contextualUserDisplayService) {
        this.contextualUserDisplayService = contextualUserDisplayService;
    }

    public void setContextualUserDisplayServiceName(String contextualUserDisplayServiceName) {
        this.contextualUserDisplayServiceName = contextualUserDisplayServiceName;
    }
}
