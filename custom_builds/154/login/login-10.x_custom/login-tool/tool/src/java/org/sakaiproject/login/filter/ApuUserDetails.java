package org.sakaiproject.login.filter;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

public class ApuUserDetails implements SAMLUserDetailsService {
        @Override
        public Object loadUserBySAML(SAMLCredential cred) throws UsernameNotFoundException {
                return cred.getAttributeAsString("https://apu.edu/attributes/netid");
        }
}
