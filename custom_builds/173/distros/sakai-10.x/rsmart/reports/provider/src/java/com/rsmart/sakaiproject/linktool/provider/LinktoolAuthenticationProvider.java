package com.rsmart.sakaiproject.linktool.provider;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.*;
import org.xml.sax.InputSource;
import com.rsmart.sakaiproject.linktool.ui.LinktoolProcessingFilter;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import javax.servlet.http.HttpUtils;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Hashtable;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Oct 27, 2008
 * Time: 9:33:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinktoolAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

   private static final String SIGNING_SUFFIX = "SakaiSigning.jws?wsdl";
   private static final String SITE_SUFFIX = "SakaiSite.jws?wsdl";
   
   private String key;
   private String sakaiEntryPointPrefix;
   
   protected MessageSourceAccessor messages;// = SpringSecurityMessageSource.getAccessor();


   public void afterPropertiesSet() throws Exception {
      Assert.hasText(this.key, "A Key is required so LinktoolAuthenticationProvider can identify tokens it previously authenticated");
      Assert.hasText(this.sakaiEntryPointPrefix, "The sakai signing entry point is required.");
   }

   /**
    * Performs authentication with the same contract as {@link
    * org.springframework.security.AuthenticationManager#authenticate(org.springframework.security.Authentication)}.
    *
    * @param authentication the authentication request object.
    * @return a fully authenticated object including credentials. May return <code>null</code> if the
    *         <code>AuthenticationProvider</code> is unable to support authentication of the passed
    *         <code>Authentication</code> object. In such a case, the next <code>AuthenticationProvider</code> that
    *         supports the presented <code>Authentication</code> class will be tried.
    * @throws org.springframework.security.AuthenticationException
    *          if authentication fails.
    */
   public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken
            && (!LinktoolProcessingFilter.LINKTOOL_USER_IDENTIFIER.equals(authentication.getPrincipal().toString()))) {
            // UsernamePasswordAuthenticationToken not CAS related
            return null;
        }

        // If an existing CasAuthenticationToken, just check we created it
        if (authentication instanceof LinktoolAuthenticationToken) {
            if (this.key.hashCode() == ((LinktoolAuthenticationToken) authentication).getKeyHash()) {
                return authentication;
            } else {
                throw new BadCredentialsException(messages.getMessage("LinktoolAuthenticationProvider.incorrectKey",
                        "The presented LinktoolAuthenticationToken does not contain the expected key"));
            }
        }

        // Ensure credentials are presented
        if ((authentication.getCredentials() == null) || "".equals(authentication.getCredentials())) {
            throw new BadCredentialsException(messages.getMessage("LinktoolAuthenticationProvider.noServiceTicket",
                    "Failed to provide a Linktool query string to validate"));
        }

        LinktoolAuthenticationToken result = null;

        if (result == null) {
            result = this.authenticateNow(authentication);
            result.setDetails(authentication.getDetails());
        }

        return result;
   }

   protected LinktoolAuthenticationToken authenticateNow(Authentication authentication) throws AuthenticationException {
      try {
         Service signingService = new Service();//getSakaiSigningEntryPoint(), new QName("SakaiSigningService"));
         Call call = (Call) signingService.createCall(); //new QName("SakaiSigning"), "testSign");
         call.setTargetEndpointAddress(new java.net.URL(getSakaiSigningEntryPoint()));
         call.setOperationName(new QName("getsession"));
         Map<String, String> queryParams = 
            LinktoolProcessingFilter.convertStrings(HttpUtils.parseQueryString((String)authentication.getCredentials()));
         String ret = (String) call.invoke( new Object[] {authentication.getCredentials(),
            URLDecoder.decode(queryParams.get("signedobject"))} );
  
         queryParams.put("sessionId", ret);

         if (ret.equalsIgnoreCase("")) {
            throw new BadCredentialsException("Failed to verify signature.");
         }
            
         String user = queryParams.get("user");
         
         // get the site type
         call.setTargetEndpointAddress(new java.net.URL(getSakaiSiteEntryPoint()));
         call.setOperationName(new QName("getSiteType"));
         String siteId = queryParams.get("site");
         String siteXml = (String) call.invoke(new Object[]{ret,siteId});
         String siteType = getSiteType(siteXml);
         String role = queryParams.get("role");
         queryParams.put("siteType", siteType);
         return new LinktoolAuthenticationToken(createGrantedAuthorities(role, siteType), 
            authentication.getCredentials(), user, getKey().hashCode(), queryParams);
      } catch (ServiceException e) {
         throw new AuthenticationServiceException("", e);
      } catch (RemoteException e) {
         throw new AuthenticationServiceException("", e);
      } catch (MalformedURLException e) {
         throw new AuthenticationServiceException("", e);
      } catch (XPathExpressionException e) {
         throw new AuthenticationServiceException("failed to get site type", e);
      }
   }

   protected String getSiteType(String siteXml) throws XPathExpressionException {
      XPath xpath = XPathFactory.newInstance().newXPath();
      InputSource is = new InputSource(new StringReader(siteXml));
      return xpath.evaluate("/site/type", is);
   }

   private GrantedAuthority[] createGrantedAuthorities(String role, String siteType) {
      return new GrantedAuthority[]{
         new GrantedAuthorityImpl("dev"),
         new GrantedAuthorityImpl("User"),
         new GrantedAuthorityImpl("Authenticated"),
         new GrantedAuthorityImpl(role + "-" + siteType),
         new GrantedAuthorityImpl(role),
         new GrantedAuthorityImpl(siteType)
      };
   }

   /**
    * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the indicated
    * <Code>Authentication</code> object.
    * <p>
    * Returning <code>true</code> does not guarantee an <code>AuthenticationProvider</code> will be able to
    * authenticate the presented instance of the <code>Authentication</code> class. It simply indicates it can support
    * closer evaluation of it. An <code>AuthenticationProvider</code> can still return <code>null</code> from the
    * {@link #authenticate(org.springframework.security.Authentication)} method to indicate another <code>AuthenticationProvider</code> should be
    * tried.
    * </p>
    * <p>Selection of an <code>AuthenticationProvider</code> capable of performing authentication is
    * conducted at runtime the <code>ProviderManager</code>.</p>
    *
    * @param authentication DOCUMENT ME!
    * @return <code>true</code> if the implementation can more closely evaluate the <code>Authentication</code> class
    *         presented
    */
   public boolean supports(Class authentication) {
        if (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)) {
            return true;
        } else return LinktoolAuthenticationToken.class.isAssignableFrom(authentication);
   }

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getSakaiSigningEntryPoint() {
      return sakaiEntryPointPrefix + SIGNING_SUFFIX;
   }

   public String getSakaiSiteEntryPoint() {
      return sakaiEntryPointPrefix + SITE_SUFFIX;
   }

   public String getSakaiEntryPointPrefix() {
      return sakaiEntryPointPrefix;
   }

   public void setSakaiEntryPointPrefix(String sakaiEntryPointPrefix) {
      this.sakaiEntryPointPrefix = sakaiEntryPointPrefix;
   }

   public void setMessageSource(MessageSource messageSource) {
      messages = new MessageSourceAccessor(messageSource);
   }
   
}
