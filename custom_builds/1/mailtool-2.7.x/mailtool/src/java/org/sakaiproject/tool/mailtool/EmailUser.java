/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.mailtool;

import java.io.UnsupportedEncodingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.sakaiproject.util.StringUtil;

/**
 * EmailUser contains user id, full name, first/last names, etc. (commented by kimsooil@bu.edu)
 * 
 * @author sgithens
 *
 */
public class EmailUser implements Comparable {
	String m_userid = "";
	String m_displayname = "";
	String m_email = "";
	String m_firstname ="";
	String m_lastname="";
	
	public EmailUser(String userid, String displayname, String email)
	{
		m_userid = userid;
		m_displayname = displayname;
		m_email = email;
	}

	public EmailUser(String userid, String firstname, String lastname, String email)
	{
		m_userid = userid;
		//m_displayname = firstname+" "+lastname;
		String comma=firstname.trim().equals("") ? "" : ", ";
		m_displayname = lastname.trim().equals("") ? firstname: lastname+comma+firstname;
		m_firstname=firstname;
		m_lastname=lastname;
		m_email = email;
	}
	public boolean equals(EmailUser user)
	{
		if (user.getUserid().equals(m_userid) &&
			user.getEmail().equals(m_email))
		{
			return true;
		}
		else
			return false;
		
	}
	
	public String getFirstname() {
		return m_firstname;
	}
	public void setFirstname(String m_firstname) {
		this.m_firstname = m_firstname;
	}
	public String getLastname() {
		return m_lastname;
	}
	public void setLastname(String m_lastname) {
		this.m_lastname = m_lastname;
	}	
	public String getDisplayname() {
		return m_displayname;
	}
	public void setDisplayname(String m_displayname) {
		this.m_displayname = m_displayname;
	}
	public String getEmail() {
		return m_email;
	}
	public void setEmail(String m_email) {
		this.m_email = m_email;
	}
	public String getUserid() {
		return m_userid;
	}
	public void setUserid(String m_userid) {
		this.m_userid = m_userid;
	}
	
	public String getNiceEmail()
	{
		String nicestring = "";
		
		if (m_displayname == "" || m_displayname == null)
			return m_email;
		else
			nicestring = m_displayname + " <" + m_email + ">";
		
		return nicestring;
	}

	public int compareTo(Object anotherEmailUser) throws ClassCastException
	{
		if (!(anotherEmailUser instanceof EmailUser))
			throw new ClassCastException("An EmailUser object expected.");
			    
		String anotherEmailUserName = ((EmailUser)
					anotherEmailUser).getDisplayname();  
		return (getDisplayname().compareTo (anotherEmailUserName));	       
	}
		/**
		 * Constructs an {@link InternetAddress} based on this user's current
		 * state. If the user has not been assigned an email address String, will
		 * return an "empty" {@link InternetAddress}. If no display name
		 * has been assigned, will return an instance initialized by 
		 * {@link InternetAddress#InternetAddress(String)}. If both a display
		 * name and an email address String have been assigned, will return
		 * an instance initialized by {@link InternetAddress#InternetAddress(String, String)}
		 * 
		 * @return a new {@link InternetAddress} derived from this user's state
		 * @throws AddressException if the proposed address fails {@link InternetAddress}
		 *   parsing rules
		 * @throws UnsupportedEncodingException
		 */
		public InternetAddress getInternetAddress() throws AddressException, 
		UnsupportedEncodingException {
			
			if ( StringUtil.trimToNull(m_email) == null ) {
				return new InternetAddress();
			}
			
			if ( StringUtil.trimToNull(m_displayname) == null ) {
				return new InternetAddress(m_email);
			}
			
			return new InternetAddress(m_email, m_displayname);
			
		}
		
}
