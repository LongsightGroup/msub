package org.sakaiproject.login.springframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;


import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.*;

/**
 * Extends the ootb Spring proxy by becoming a no op in the situation where
 * the targetBean can not be found in the context.  The ootb behavior is to
 * blow up the webapp.  This allows us to drop in context files into the sakai.home
 * directory.  If they are found we can wire up some filters, if they aren't found
 * we just go on safely about our business.
 * <p/>
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 1/29/13
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SafeDelegatingFilterProxy extends DelegatingFilterProxy {
    private static Log log = LogFactory.getLog(SafeDelegatingFilterProxy.class);

    private final Object delegateMonitor = new Object();
    private boolean enabled = false;

    protected void initFilterBean() throws ServletException {
        // If no target bean name specified, use filter name.
        if (getTargetBeanName() == null) {
            setTargetBeanName(getFilterName());
        }

        // make sure context is valid and bean exists before enabling this filter
        synchronized (this.delegateMonitor) {
            WebApplicationContext wac = findWebApplicationContext();
            if (wac != null) {
                if (wac.containsBean(getTargetBeanName())) {
                    super.initFilterBean();
                    enabled = true;
                } else {
                    log.info("Can't find a bean with name: " + getTargetBeanName() + ", safely disable proxying");
                }
            } else {
                log.warn("Can't find web application context");
            }
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (enabled) {
            super.doFilter(request, response, filterChain);
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        if (enabled) {
            super.destroy();
        }
    }
}
