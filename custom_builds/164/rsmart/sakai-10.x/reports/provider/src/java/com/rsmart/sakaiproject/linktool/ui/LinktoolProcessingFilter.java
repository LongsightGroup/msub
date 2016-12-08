package com.rsmart.sakaiproject.linktool.ui;


import org.acegisecurity.ui.AbstractProcessingFilter;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Oct 27, 2008
 * Time: 9:28:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinktoolProcessingFilter extends AbstractProcessingFilter {

    /** Used to identify a Linktool request for a stateful user agent, such as a web browser. */
    public static final String LINKTOOL_USER_IDENTIFIER = "_linktool_user_";
   
   
   /**
    * Performs actual authentication.
    *
    * @param request from which to extract parameters and perform the
    *                authentication
    * @return the authenticated user
    * @throws org.springframework.security.AuthenticationException
    *          if authentication fails
    */
   public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        final String username = LINKTOOL_USER_IDENTIFIER;
        String password = request.getQueryString();

        if (password == null) {
            password = "";
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

      Authentication returned = this.getAuthenticationManager().authenticate(authRequest);
      
      Map<String, String> params = (Map<String, String>) returned.getDetails();
      for (Map.Entry<String, String> entry : params.entrySet()) {
         String key = "sakai." + entry.getKey();
         request.getSession().setAttribute(key, entry.getValue());
      }
      
      return returned;
   }

   /**
    * Specifies the default <code>filterProcessesUrl</code> for the
    * implementation.
    *
    * @return the default <code>filterProcessesUrl</code>
    */
   public String getDefaultFilterProcessesUrl() {
        return "/j_spring_linktool_security_check";
   }

   public static Map<String, String> convertStrings(Map<String, String[]> strings) {
      Map<String, String> returned = new Hashtable<String, String>();
      
      for (Map.Entry<String, String[]> entry : strings.entrySet()) {
         returned.put(entry.getKey(), entry.getValue()[0]);
      }
      
      return returned;
   }
}
