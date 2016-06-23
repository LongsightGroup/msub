package com.rsmart.sakai.providers;

import com.rsmart.sakai.providers.util.*;
import java.util.*;
import org.apache.commons.logging.*;
import org.sakaiproject.component.api.*;
import org.sakaiproject.entity.api.*;
import org.sakaiproject.exception.*;
import org.sakaiproject.site.api.*;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.user.api.*;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: 5/9/12
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailDisplayAdvisor extends BaseContextualUserDisplayService implements ContextualUserDisplayService {
    private static final Log log = LogFactory.getLog(UserPropertyDisplayAdvisor.class);

    public void init() {

    }

    public String getUserDisplayId(User user) {
        String userEmail = "";

        if(user != null){
            userEmail = user.getEmail();
        }

        if(userEmail != null && !"".equals(userEmail)){
            return userEmail;
        } else{
            return "";
        }
    }

    public String getUserDisplayId(User user, String s) {
        return getUserDisplayId(user);
    }
}