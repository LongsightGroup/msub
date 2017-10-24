package com.rsmart.preauth.client.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.rsmart.preauth.client.authentication.AuthenticationFilter;
import com.rsmart.preauth.client.util.HexMac;


/**
 * Tests for the AuthenticationFilter.
 *
 * @author Earle Nietzel
 */
public final class AuthenticationFilterTestCase extends TestCase {
	private final Log log = LogFactory.getLog(AuthenticationFilterTestCase.class);

    private static final String portalServerUrl = "http://127.0.0.1/portal/login";

    private static final String portalPrivateKey = "b4ba90d634d14291abf849137c0f3462351c1e8cabc1430ba071366e702a7c89";

    private AuthenticationFilter filter;

    protected void setUp() throws Exception {
        this.filter = new AuthenticationFilter();
        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("portalPrivateKey", portalPrivateKey);
        config.addInitParameter("portalServerUrl", portalServerUrl);
        this.filter.init(config);
    }

    protected void tearDown() throws Exception {
        this.filter.destroy();
    }
    
    public void testRedirect() throws Exception {
        //final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
            }
        };

        //request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertEquals(portalServerUrl, response.getRedirectedUrl());
    }

    public void testSession() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
            }
        };

        request.setSession(session);
        this.filter.doFilter(request, response, filterChain);

        assertNull(response.getRedirectedUrl());
    }

    public void testValid() {

    	final Map<String, String> params = new HashMap<String, String>();
    	MockHttpServletResponse response = null;
        final HexMac hexMac = new HexMac(portalPrivateKey.getBytes()); 
    	
       	params.put("account", "Student100");
    	params.put("by", "name");
    	params.put("timestamp", Long.toString(System.currentTimeMillis()));
    	params.put("expires", Long.toString(0));

		final String data =  
			params.get("account") + "|" +
			params.get("by") + "|" +
			params.get("expires") + "|" +
			params.get("timestamp");

		params.put("preauth", hexMac.getHmac(data));
		
		log.info(data + " preauth=" + hexMac.getHmac(data));

    	try {
			response = sendQueryString(params);
		} catch (Exception e) {
			fail("sendQueryString() threw an exception: " + e.getMessage());
		}
    	
        assertNull(response.getRedirectedUrl());
    }

    
    public void testExpired() {

    	final Map<String, String> params = new HashMap<String, String>();
    	MockHttpServletResponse response = null;
        final HexMac hexMac = new HexMac(portalPrivateKey.getBytes()); 
    	
       	params.put("account", "Student100");
    	params.put("by", "name");
    	params.put("timestamp", Long.toString(System.currentTimeMillis() - 10 * 1000));
    	params.put("expires", Long.toString(5));

		final String data =  
			params.get("account") + "|" +
			params.get("by") + "|" +
			params.get("expires") + "|" +
			params.get("timestamp");

		params.put("preauth", hexMac.getHmac(data));
    	
		try {
			response = sendQueryString(params);
		} catch (Exception e) {
			fail("sendQueryString() threw an exception: " + e.getMessage());
		}
    	
        assertEquals(portalServerUrl, response.getRedirectedUrl());
    }
    
    private MockHttpServletResponse sendQueryString(Map<String, String> parameters) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
	
        request.setParameters(parameters);
        request.setRequestURI("/test");

        final FilterChain filterChain = new FilterChain() {

            public void doFilter(ServletRequest arg0, ServletResponse arg1)
                    throws IOException, ServletException {
            }
        };

        this.filter = new AuthenticationFilter();

        final MockFilterConfig config = new MockFilterConfig();
        config.addInitParameter("portalServerUrl", portalServerUrl);
        config.addInitParameter("portalPrivateKey", portalPrivateKey);
        this.filter.init(config);

        this.filter.doFilter(request, response, filterChain);

        return response;
    }
}
