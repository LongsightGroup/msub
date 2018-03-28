package com.rsmart.login.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Oct 8, 2009
 * Time: 4:46:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserAttributeResolver
{
    public UserAttributes resolveAttributes (String eid, HttpServletRequest req)
        throws ServletException;
}
