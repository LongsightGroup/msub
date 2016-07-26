package com.rsmart.preauth.client.authentication;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.rsmart.preauth.client.util.AbstractFilter;
import com.rsmart.preauth.client.util.HexMac;


/**
 * Filter implementation to intercept all requests and attempt to authenticate
 * the user using preauth from Zimbra. 
 * <p>
 * This filter allows you to specify the following parameters (at either the context-level or the filter-level):
 * <ul>
 * <li><code>portalServerUrl</code> - the portal a user should be redirected to, i.e. https://my.portal.edu</li>
 * <li><code>portalPrivateKey</code> - the DOMAIN_KEY used to create preauth key, this should always remain secret.</li>
 * </ul>
 *
 * @author Earle Nietzel
 */
public class AuthenticationFilter extends AbstractFilter {

    /**
     * The URL to the Portal Server login.
     */
    private String portalServerUrl;

    /**
     * The portal private key 
     */
    private String portalPrivateKey;

    protected void initInternal(final FilterConfig filterConfig) throws ServletException {
        if (!isIgnoreInitConfiguration()) {
            super.initInternal(filterConfig);
            setPortalServerUrl(getPropertyFromInitParams(filterConfig, "portalServerUrl", null));
            setPortalPrivateKey(getPropertyFromInitParams(filterConfig, "portalPrivateKey", null));
        }
    }

    public void init() {
        super.init();
        if(this.portalServerUrl == null)
        	throw new IllegalArgumentException("portalServerUrl cannot be null.");
        if(this.portalPrivateKey == null)
        	throw new IllegalArgumentException("portalPrivateKey cannot be null.");
    }

    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;
        final HttpSession session = request.getSession(false);

        if (session != null) {
            filterChain.doFilter(request, response);
            return;
        } else {
        	if (validate(request)) {
        		// set the Remote User using a request wrapper with the account parameter
        		filterChain.doFilter(new PreauthAccountRequestWrapper(request, request.getParameter("account")), response);
        		return;
        	} 
        	// else it will redirect to portalServerUrl
        }

        if (log.isDebugEnabled())
        	log.debug("redirecting to \"" + this.portalServerUrl + "\"");

        response.sendRedirect(this.portalServerUrl);
    }

    public final void setPortalPrivateKey(final String portalPrivateKey) {
        this.portalPrivateKey = portalPrivateKey;
    }
    
    public final void setPortalServerUrl(final String casServerLoginUrl) {
        this.portalServerUrl = casServerLoginUrl;
    }
    
	private Boolean validate(HttpServletRequest request) {
           
		@SuppressWarnings("unchecked")
		final Map<String, String[]> requestParams = request.getParameterMap();
		
		if (requestParams.containsKey("account") &&
				requestParams.containsKey("by") &&
				requestParams.containsKey("timestamp") &&
				requestParams.containsKey("expires") &&
				requestParams.containsKey("preauth")) {
			
			final String requestData =  
				(requestParams.get("account"))[0] + "|" +
				(requestParams.get("by"))[0] + "|" +
				(requestParams.get("expires"))[0] + "|" +
				(requestParams.get("timestamp"))[0];
			
			// check if preauth request has expired
			final long expires = Long.parseLong((requestParams.get("expires"))[0]);
			
			// expires = 0 means no expiration
			if (expires > 0) {
				final long timestamp = Long.parseLong((requestParams.get("timestamp"))[0]);
				final long now = System.currentTimeMillis();
				
				// preauth has expired
				if ((expires * 1000) + timestamp < now ) {
					if (log.isDebugEnabled())
						log.debug("Expired credentials found in: " + requestData);
					return false;
				}
			}
			
			// preauth from request
			final String requestHmac =  (requestParams.get("preauth"))[0];
			
			final HexMac hexMac = new HexMac(portalPrivateKey.getBytes()); 
			// compute preauth from params
			final String computedHmac = hexMac.getHmac(requestData);
            
			// check to see if preauth values are the same
			if (computedHmac.equals(requestHmac)) {
				return true;
			} else {
				if (log.isDebugEnabled())
					log.debug("Computed preauth keys do not match for: " + requestData);
			}
		} else {
			if (log.isDebugEnabled())
				log.debug("Not all required parameters were found in the request");
		}
		
		return false;
    }



    
}
