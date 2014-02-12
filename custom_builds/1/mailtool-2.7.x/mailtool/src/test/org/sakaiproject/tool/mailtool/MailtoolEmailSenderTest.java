/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.Formatting;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Verifies behaviors related to handling of the "from" address associated
 * with Mailtool-generated messages. 
 * 
 * @author dmccallum@unicon.net
 */
public class MailtoolEmailSenderTest extends BaseMailtoolTest {


	/**
	 * Verifies that a no-reply from address is generated in the event
	 * that the current sender has no configured email address. See
	 * SAK-11046 comments on Aug 9, 2007 for background.
	 * 
	 * <p>Note that the expected behavior is slightly different here
	 * as compared to situations where the sender has explicitly
	 * indicated that a no-reply address should be generated. Here
	 * we still use the sender's personal name, whereas the worksite's 
	 * title is used as the from address's personal name in the other case.</p>
	 * 
	 * @see #testConstructsDefaultNoReplyAddressFromCorrectValues()
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testConvertsNullFromAddressToNoReplyAddressWithCurrentUsersPersonalName() 
	throws AddressException, UnsupportedEncodingException {
		final String MOCK_USER_ID = "mock-user-id";
		final String MOCK_USER_DISPLAY_NAME = "Mock User";
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(new ArrayList()));
		mockUserDirectoryService.expects(once()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(once()).method("getId").will(returnValue(MOCK_USER_ID));
		mockCurrentUser.expects(once()).method("getDisplayName").will(returnValue(MOCK_USER_DISPLAY_NAME));
		mockCurrentUser.expects(once()).method("getEmail").will(returnValue(null)); // the special case
		InternetAddress expectedFromAddr = mailtool.createNoReplyInternetAddress(MOCK_USER_DISPLAY_NAME);
		mockMimeMessageSender.expects(once()).method("send").with(messageFrom(expectedFromAddr));
		
		// the code exercise
		mailtool.processSendEmail();
	}
	
	/**
	 * Verifies the attributes of the no-reply address generated when
	 * the sending user explicitly indicates that she prefers to generate
	 * a no-reply address. 
	 * 
	 * See @{@link #testConvertsNullFromAddressToNoReplyAddressWithCurrentUsersPersonalName()}
	 * for a discussion of the different "no-reply" address construction
	 * behaviors.
	 * 
	 * @see #testConvertsNullFromAddressToNoReplyAddressWithCurrentUsersPersonalName()
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testNoReplyOptionSendsMailFromDefaultNoReplyAddressWithSiteTitleAsPersonalName() 
	throws AddressException, UnsupportedEncodingException {
		mailtool.m_replyto = "no"; // the special case
		final String MOCK_USER_ID = "mock-user-id";
		final String MOCK_USER_DISPLAY_NAME = "Mock User";
		final String MOCK_USER_EMAIL = "mockuser@localhost";
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(new ArrayList()));
		mockUserDirectoryService.expects(once()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(once()).method("getId").will(returnValue(MOCK_USER_ID));
		mockCurrentUser.expects(once()).method("getDisplayName").will(returnValue(MOCK_USER_DISPLAY_NAME));
		mockCurrentUser.expects(once()).method("getEmail").will(returnValue(MOCK_USER_EMAIL));
		InternetAddress expectedFromAddr = mailtool.createNoReplyInternetAddress();
		mockMimeMessageSender.expects(once()).method("send").with(messageFrom(expectedFromAddr));
		
		// the code exercise
		mailtool.processSendEmail();
	}
	
	/**
	 * Verifies construction of no-reply addresses in the absence
	 * of a user-specified personal name.
	 * 
	 * @see #testAcceptsOverridesOfDefaultNoReplyAddressPersonalName()
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testConstructsDefaultNoReplyAddressFromCorrectValues() 
	throws AddressException, UnsupportedEncodingException {
		
		final String expectedFromAddrPersonalName = mailtool.getDefaultNoReplyAddressPersonalName();
		final String expectedFromAddrString = mailtool.getNoReplyEmailAddress();
		final InternetAddress expectedFromAddr = 
			new InternetAddress(expectedFromAddrString, expectedFromAddrPersonalName);
		
		assertNotNull(expectedFromAddrPersonalName); // sanity check
		assertNotNull(expectedFromAddrString); // sanity check
		
		// the code exercise
		final InternetAddress actualFromAddr = mailtool.createNoReplyInternetAddress();
		
		// verification
		assertEquals(expectedFromAddr, actualFromAddr);
		
	}
	
	/**
	 * Verifies that {@link Mailtool#createNoReplyInternetAddress(String)}
	 * correctly handles the given <code>String</code> parameter.
	 * 
	 * @see #testConstructsDefaultNoReplyAddressFromCorrectValues()
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testAcceptsOverridesOfDefaultNoReplyAddressPersonalName() 
	throws AddressException, UnsupportedEncodingException {
		
		final String expectedFromAddrString = mailtool.getNoReplyEmailAddress();
		final String PERSONAL_NAME_OVERRIDE = "No Replies Please";
		final InternetAddress expectedFromAddr = 
			new InternetAddress(expectedFromAddrString, PERSONAL_NAME_OVERRIDE);
		
		assertNotNull(expectedFromAddrString); // sanity check
		
		// the code exercise
		final InternetAddress actualFromAddr = 
			mailtool.createNoReplyInternetAddress(PERSONAL_NAME_OVERRIDE);
		
		// verification
		assertEquals(expectedFromAddr, actualFromAddr);
	}
	
	
	
	
	
}