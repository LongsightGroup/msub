package org.sakaiproject.springframework.beans;

import org.springframework.beans.factory.FactoryBean;

import java.net.URI;

/**
 * User: duffy
 * Date: May 26, 2011
 * Time: 9:14:44 AM
 */
public class URIFactoryBean
    implements FactoryBean
{
    private String
        scheme      = null,
        userInfo    = null,
        host        = null,
        path        = null,
        query       = null,
        fragment    = null;
    private int
        port        = -1;

    public Object getObject()
        throws Exception
    {
        return new URI(scheme, userInfo, host, port, path, query, fragment);
    }

    public Class getObjectType()
    {
        return URI.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void setFragment(String fragment)
    {
        this.fragment = fragment;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }

    public void setUserInfo(String userInfo)
    {
        this.userInfo = userInfo;
    }
}
