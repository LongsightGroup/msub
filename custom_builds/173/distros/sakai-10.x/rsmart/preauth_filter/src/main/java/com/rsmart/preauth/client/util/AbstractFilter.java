package com.rsmart.preauth.client.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstracts the ability to configure the filters from the initial properties provided.
 * 
 * @author Earle Nietzel
 *
 */
public abstract class AbstractFilter implements Filter {
	
	protected final Log log = LogFactory.getLog(getClass());

    private boolean ignoreInitConfiguration = false;

   /**
     * Retrieves the property from the FilterConfig.  First it checks the FilterConfig's initParameters to see if it
     * has a value.
     * If it does, it returns that, otherwise it retrieves the ServletContext's initParameters and returns that value if any.
     * <p>
     * Finally, it will check JNDI if all other methods fail.  All the JNDI properties should be stored under java:comp/env/{propertyName}
     *
     * @param filterConfig the Filter Configuration.
     * @param propertyName the property to retrieve.
     * @param defaultValue the default value if the property is not found.
     * @return the property value, following the above conventions.  It will always return the more specific value (i.e.
     *  filter vs. context).
     */
    protected final String getPropertyFromInitParams(final FilterConfig filterConfig, final String propertyName, final String defaultValue)  {
        final String value = filterConfig.getInitParameter(propertyName);

        if (!value.isEmpty()) {
            if (log.isDebugEnabled())
            	log.debug("Property [" + propertyName + "] loaded from FilterConfig.getInitParameter with value [" + value + "]");
            return value;
        }

        final String value2 = filterConfig.getServletContext().getInitParameter(propertyName);

        if (!value2.isEmpty()) {
            if (log.isDebugEnabled())
            	log.debug("Property [" + propertyName + "] loaded from ServletContext.getInitParameter with value [" + value2 + "]");
            return value2;
        }
        InitialContext context = null;
        try {
         context = new InitialContext();
        } catch (final NamingException e) {
        	log.warn(e,e);
        	return defaultValue;
        }
        
        
        final String shortName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".")+1);
        final String value3 = loadFromContext(context, "java:comp/env/" + shortName + "/" + propertyName);
        
        if (!value3.isEmpty()) {
            if (log.isDebugEnabled())
            	log.debug("Property [" + propertyName + "] loaded from JNDI Filter Specific Property with value [" + value3 + "]");
        	return value3;
        }
        
        final String value4 = loadFromContext(context, "java:comp/env/" + propertyName); 
        
        if (!value4.isEmpty()) {
        	if (log.isDebugEnabled())
        		log.debug("Property [" + propertyName + "] loaded from JNDI with value [" + value3 + "]");
        	return value4;
        }

        if (log.isWarnEnabled())
        	log.warn("Property [" + propertyName + "] not found.  Using default value [" + defaultValue + "]");
        
        return defaultValue;
    }
    
    public final void init(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            initInternal(filterConfig);
        }
        init();
    }

    /** Controls the ordering of filter initialization and checking by defining a method that runs before the init.
     * @param filterConfig the original filter configuration.
     * @throws ServletException if there is a problem.
     *
     */
    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        // template method
    }
    
    public void init() {
    }
    
    // empty implementation as most filters won't need this.
    public void destroy() {
        // nothing to do
    }


    protected final boolean parseBoolean(final String value) {
    	return ((value != null) && value.equalsIgnoreCase("true"));
    }
    
    protected final String loadFromContext(final InitialContext context, final String path) {
    	try {
    		return (String) context.lookup(path);
    	} catch (final NamingException e) {
    		return null;
    	}
    }

    public final void setIgnoreInitConfiguration(boolean ignoreInitConfiguration) {
        this.ignoreInitConfiguration = ignoreInitConfiguration;
    }

    protected final boolean isIgnoreInitConfiguration() {
        return this.ignoreInitConfiguration;
    }
 }
