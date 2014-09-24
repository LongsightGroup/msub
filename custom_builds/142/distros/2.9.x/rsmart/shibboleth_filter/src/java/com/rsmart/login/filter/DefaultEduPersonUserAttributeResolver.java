package com.rsmart.login.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Oct 8, 2009
 * Time: 4:47:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultEduPersonUserAttributeResolver
    implements UserAttributeResolver
{

	private static final Log
        LOG = LogFactory.getLog(DefaultEduPersonUserAttributeResolver.class);

    private String
            firstNameAttribute,
            lastNameAttribute,
            emailAttribute,
            affiliationAttribute,
            defaultAffiliation;

    private Map<String, String>
            scopeMap;

    public Map<String, String> getInScopes()
    {
        return scopeMap;
    }

    /**
     * Maps eppn scopes to user types.
     *
     * @param scopeMap
     */
    public void setScopeMap(Map <String, String> scopeMap)
    {
       this.scopeMap = scopeMap;
    }

    public void setDefaultAffiliation (String aff)
    {
        defaultAffiliation = aff;
    }

    public String getDefaultAffiliation ()
    {
        return defaultAffiliation;
    }

    public void setFirstNameAttribute (String fnAttr)
    {
        this.firstNameAttribute = fnAttr;
    }

    public String getFirstNameAttribute ()
    {
        return firstNameAttribute;
    }

    public void setLastNameAttribute (String lnAttr)
    {
        this.lastNameAttribute = lnAttr;
    }

    public String getLastNameAttribute ()
    {
        return lastNameAttribute;
    }

    public void setEmailAttribute (String emailAttr)
    {
        this.emailAttribute = emailAttr;
    }

    public String getEmailAttribute ()
    {
        return emailAttribute;
    }

    public void setAffiliationAttribute (String affAttr)
    {
        this.affiliationAttribute = affAttr;
    }

    public String getAffiliationAttribute ()
    {
        return affiliationAttribute;
    }

    /**
     * Pulls the Shibboleth attributes from the request headers and populates a UserAttributes object.
     *
     * @param eid
     * @param req
     * @return
     * @throws ServletException
     */
    public UserAttributes resolveAttributes(String eid, HttpServletRequest req)
        throws ServletException 
    {
        // dump the request headers
        if (LOG.isDebugEnabled())
        {
            final StringBuilder
                bldr = new StringBuilder();
            final Enumeration
                hdrs = req.getHeaderNames();

            bldr.append ("Headers:\n");
            while (hdrs.hasMoreElements())
            {
                final String
                    hdrName = (String)hdrs.nextElement();

                bldr.append(hdrName).append(": '").append(req.getHeader(hdrName)).append('\n');
            }

            LOG.debug(bldr.toString());
        }

        LOG.debug("creating user from Shib attributes");
        UserAttributes
            user = new UserAttributes();

        user.setEid(eid);
        user.setEmail(resolveEmail(req));

        final String
            fnAttr = getFirstNameAttribute();

        if (fnAttr == null)
        {
            LOG.error ("Configuration Error: Shibboleth header key for first name attribute not set");
        }
        else
        {
            user.setFirstName(req.getHeader(fnAttr));
        }

        final String
            lnAttr = getLastNameAttribute();

        if (lnAttr == null)
        {
            LOG.error ("Configuration Error: Shibboleth header key for last name attribute not set");
        }
        else
        {
            user.setLastName(req.getHeader(lnAttr));
        }
        
        user.setType(resolveType(eid, req));

        return user;
    }

    private String resolveType(String eid, HttpServletRequest req)
    {
        String type = getDefaultAffiliation();
        String scope = scopeOf(eid);
        if (scope != null && scopeMap != null)
        {
            String temp = scopeMap.get(scope);

            if (temp != null)
                type = temp;
        }

        return type;
    }

    private String resolveEmail(HttpServletRequest req)
    {
        final String
            emailAttr = getEmailAttribute();

        if (emailAttr == null)
        {
            LOG.error ("Configuration Error: Shibboleth header name for email attribute not set");
            return null;
        }

        final String
            mailHeader = req.getHeader (getEmailAttribute());

        String[]
            email = mailHeader == null ? null : mailHeader.split(";");

        if (email == null || email.length < 1)
            return null;
        
        return email[0];
    }

    private String scopeOf(String principal)
    {
        int atIdx = principal.indexOf('@');
        if (atIdx == -1)
            return null;

        return principal.substring(atIdx+1);
    }

    private String scopedLocalPart(String str, String scope)
    {
        int atIdx = str.indexOf('@');
        if (atIdx == -1)
            return null;

        String theScope = str.substring(atIdx+1);

        return theScope.equalsIgnoreCase(scope) ? str.substring(0,atIdx) : null;
    }

    private String[] affiliationsOf(HttpServletRequest req, String scope)
    {
        List<String> affiliations = new ArrayList<String>();
        String scopedAffiliationHeader = req.getHeader(getAffiliationAttribute());
        if (scopedAffiliationHeader != null && scopedAffiliationHeader.length() > 0)
        {
            String[] scopedAffiliations = scopedAffiliationHeader.split(";");
            for (int i = 0; i < scopedAffiliations.length; i++)
            {
                String affiliation = scopedLocalPart(scopedAffiliations[i], scope);
                if (affiliation != null)
                    affiliations.add(affiliation);
            }
        }

        return affiliations.toArray(new String[affiliations.size()]);
    }

    private boolean isInArray(String v, String[] values)
    {
        if (values == null || v == null)
            return false;

        for (int i = 0; i < values.length; i++)
        {
            if (values[i].equalsIgnoreCase(v))
                return true;
        }

        return false;
    }

}
