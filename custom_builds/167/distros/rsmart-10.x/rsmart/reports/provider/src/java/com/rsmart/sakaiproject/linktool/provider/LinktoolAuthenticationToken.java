package com.rsmart.sakaiproject.linktool.provider;


import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Oct 27, 2008
 * Time: 9:34:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinktoolAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Object credentials;
    private final Object principal;
    private final int keyHash;
   private Map<String, String> detailsMap;

   /**
    * Creates a token with the supplied array of authorities.
    *
    * @param authorities the list of <tt>GrantedAuthority</tt>s for the
    *                    principal represented by this authentication object. A
    *                    <code>null</code> value indicates that no authorities have been
    *                    granted (pursuant to the interface contract specified by {@link
    *                    org.springframework.security.Authentication#getAuthorities()}<code>null</code> should only be
    *                    presented if the principal has not been authenticated).
    */
   public LinktoolAuthenticationToken(GrantedAuthority[] authorities, Object credentials, Object principal, int keyHash, 
                                      Map<String, String> detailsMap) {
      super(authorities);
      this.credentials = credentials;
      this.principal = principal;
      this.keyHash = keyHash;
      this.detailsMap = detailsMap;
   }

   /**
    * The credentials that prove the principal is correct. This is usually a password, but could be anything
    * relevant to the <code>AuthenticationManager</code>. Callers are expected to populate the credentials.
    *
    * @return the credentials that prove the identity of the <code>Principal</code>
    */
   public Object getCredentials() {
      return credentials;
   }

   /**
    * The identity of the principal being authenticated. This is usually a username. Callers are expected to
    * populate the principal.
    *
    * @return the <code>Principal</code> being authenticated
    */
   public Object getPrincipal() {
      return principal;
   }

   public int getKeyHash() {
      return keyHash;
   }

   public Map<String, String> getDetailsMap() {
      return detailsMap;
   }

   public void setDetailsMap(Map<String, String> detailsMap) {
      this.detailsMap = detailsMap;
   }

   public Object getDetails() {
      return getDetailsMap();
   }
}
