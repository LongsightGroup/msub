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
import java.util.List;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.jmock.core.Constraint;

/**
 * Verifies handling of {@link Mailtool}-generated message recipients.
 * 
 * @author dmccallum@unicon.net
 */
public class MailtoolRecipientsTest extends BaseMailtoolTest {

	private static final String MOCK_RECIPIENT_USER_ID = "mock-recipient-user-id";
	private static final String MOCK_RECIPIENT_USER_DISPLAY_NAME = "Mock Recipient User";
	private static final String MOCK_RECIPIENT_USER_EMAIL = "mockrecipient@localhost";
	private static final String MOCK_SENDER_USER_ID = "mock-sender-user-id";
	private static final String MOCK_SENDER_USER_DISPLAY_NAME = "Mock Sender User";
	private static final String MOCK_SENDER_USER_EMAIL = "mockuser@localhost";
	
	private List<EmailGroup> recipientEmailGroups;
	private List<EmailUser> recipientEmailUsers;
	private EmailUser senderEmailUser;
	private EmailUser recipientEmailUser;
	
	
	protected void setUp() throws Exception {
		
		// note that users added to recipientEmailUsers will automatically show
		// up in recipientEmailGroups
		recipientEmailGroups = new ArrayList<EmailGroup>();
		recipientEmailUsers = new ArrayList<EmailUser>();
		EmailRole emailRole = new EmailRole(null, null, null, null);
		EmailGroup emailGroup = new EmailGroup(emailRole, recipientEmailUsers);
		recipientEmailGroups.add(emailGroup);
		recipientEmailUser = new EmailUser(MOCK_RECIPIENT_USER_ID, 
				MOCK_RECIPIENT_USER_DISPLAY_NAME, MOCK_RECIPIENT_USER_EMAIL);
		recipientEmailUsers.add(recipientEmailUser);
		senderEmailUser = new EmailUser(MOCK_SENDER_USER_ID, 
				MOCK_SENDER_USER_DISPLAY_NAME, MOCK_SENDER_USER_EMAIL);
		super.setUp();
	}
	
	/**
	 * Verifies that a message is sent to no-one if the only targeted
	 * recipient has no email address.
	 * 
	 * TODO This probably isn't the greatest behavior. Noisier failure
	 *   would probably be more appropriate is this most likely
	 *   represents either programmer or user error. Also, this
	 *   is only testing the "allUsersSelected" mode. Behaviors may
	 *   be different in other modes.
	 * 
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testFailsQuietlyIfOnlyRecipientHasNoEmailAddress() 
	throws AddressException, UnsupportedEncodingException {
		
		recipientEmailUser.setEmail(null);
		
		mailtool.setAllUsersSelected(true);
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(recipientEmailUsers));
		mockUserDirectoryService.expects(atLeastOnce()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(atLeastOnce()).method("getId").will(returnValue(senderEmailUser.getUserid()));
		mockCurrentUser.expects(atLeastOnce()).method("getDisplayName").will(returnValue(senderEmailUser.getDisplayname()));
		mockCurrentUser.expects(atLeastOnce()).method("getEmail").will(returnValue(senderEmailUser.getEmail()));
		InternetAddress expectedFromAddr = senderEmailUser.getInternetAddress();
		InternetAddress[] expectedToAddrs = null;
		
		mockMimeMessageSender.expects(once()).method("send").with(and(messageFrom(expectedFromAddr),
				messageTo(expectedToAddrs, Message.RecipientType.BCC)));
		
		// the code exercise
		mailtool.processSendEmail();
		
		// TODO verify "result string"?
		
	}
	
	/**
	 * Verifies that targeting a recipient without an email address can
	 * be ignored gracefully.
	 * 
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testSkipsRecipientsWithNoEmailAddress() 
	throws AddressException, UnsupportedEncodingException {
		
		EmailUser recipientUserWithoutEmail = new EmailUser(MOCK_RECIPIENT_USER_ID + "2", 
				MOCK_RECIPIENT_USER_DISPLAY_NAME + "2", null);
		recipientEmailUsers.add(recipientUserWithoutEmail);
		
		mailtool.setAllUsersSelected(true);
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(recipientEmailUsers));
		mockUserDirectoryService.expects(atLeastOnce()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(atLeastOnce()).method("getId").will(returnValue(senderEmailUser.getUserid()));
		mockCurrentUser.expects(atLeastOnce()).method("getDisplayName").will(returnValue(senderEmailUser.getDisplayname()));
		mockCurrentUser.expects(atLeastOnce()).method("getEmail").will(returnValue(senderEmailUser.getEmail()));
		InternetAddress expectedFromAddr = senderEmailUser.getInternetAddress();
		InternetAddress[] expectedToAddrs =
			new InternetAddress[] { recipientEmailUser.getInternetAddress() }; // exclude recipientUserWithoutEmail
		
		mockMimeMessageSender.expects(once()).method("send").with(and(messageFrom(expectedFromAddr),
				messageTo(expectedToAddrs, Message.RecipientType.BCC)));
		
		// the code exercise
		mailtool.processSendEmail();
		
		// TODO verify "result string"?
		
	}
	
	/**
	 * Verifies that {@link Mailtool#processSendEmail()} excludes the
	 * sender from the delivery list if the end user selected the "all
	 * users" option.
	 * 
	 * TODO need a test to verify the alternate case: where another mode
	 *   has been selected, but the sender appears in the selected recipients
	 *   anyway.
	 *
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testExcludesCurrentUserFromRecipientListIfAllParticipantsSelected() 
	throws AddressException, UnsupportedEncodingException {
		
		recipientEmailUsers.add(senderEmailUser);
		
		mailtool.setAllUsersSelected(true);
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(recipientEmailUsers));
		mockUserDirectoryService.expects(atLeastOnce()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(atLeastOnce()).method("getId").will(returnValue(senderEmailUser.getUserid()));
		mockCurrentUser.expects(atLeastOnce()).method("getDisplayName").will(returnValue(senderEmailUser.getDisplayname()));
		mockCurrentUser.expects(atLeastOnce()).method("getEmail").will(returnValue(senderEmailUser.getEmail()));
		InternetAddress expectedFromAddr = senderEmailUser.getInternetAddress();
		InternetAddress[] expectedToAddrs =
			new InternetAddress[] { recipientEmailUser.getInternetAddress() };
		
		mockMimeMessageSender.expects(once()).method("send").with(and(messageFrom(expectedFromAddr),
				messageTo(expectedToAddrs, Message.RecipientType.BCC)));
		
		// the code exercise
		mailtool.processSendEmail();
		
		// TODO verify "result string"?
		
	}
	
	/**
	 * Verifies that message transmission is not aborted while filtering
	 * the "all participants" recipient list if the sending user has
	 * no email address. This was introduced specifically because previous
	 * implementations generated an NPE in this scenario.
	 * 
	 * @throws AddressException test failure
	 * @throws UnsupportedEncodingException test failure
	 */
	public void testCompletesGracefullyIfAllParticipantsSelectedButSenderHasNoEmailAddress() 
	throws AddressException, UnsupportedEncodingException {
		
		EmailUser recipientUser = new EmailUser(MOCK_RECIPIENT_USER_ID, 
				MOCK_RECIPIENT_USER_DISPLAY_NAME, MOCK_RECIPIENT_USER_EMAIL);
		recipientEmailUsers.add(recipientUser);
		
		mailtool.setAllUsersSelected(true);
		mockRecipientSelector.expects(once()).method("getSelectedUsers").will(returnValue(recipientEmailUsers));
		mockUserDirectoryService.expects(atLeastOnce()).method("getCurrentUser").will(returnValue(currentUser));
		mockCurrentUser.expects(atLeastOnce()).method("getId").will(returnValue(MOCK_SENDER_USER_ID));
		mockCurrentUser.expects(atLeastOnce()).method("getDisplayName").will(returnValue(MOCK_SENDER_USER_DISPLAY_NAME));
		mockCurrentUser.expects(atLeastOnce()).method("getEmail").will(returnValue(null)); // the special case
		InternetAddress expectedFromAddr = 
			mailtool.createNoReplyInternetAddress(MOCK_SENDER_USER_DISPLAY_NAME);
		InternetAddress[] expectedToAddrs =
			new InternetAddress[] { recipientUser.getInternetAddress() };
		
		mockMimeMessageSender.expects(once()).method("send").with(and(messageFrom(expectedFromAddr),
				messageTo(expectedToAddrs, Message.RecipientType.BCC)));
		
		// the code exercise
		mailtool.processSendEmail();
		
	}

	/**
	 * Overriden to short-circuit {@link Mailtool#getEmailGroups()} and
	 * return an empty list. Assumes the fixture's
	 * message sink has already been configured.
	 */
	@Override
	protected Mailtool createMailtool() {
		return new StubMailtoolWithMessageSink(this.mimeMessageSender) {
			
			public List getEmailGroups() {
				return recipientEmailGroups;
			}
			
		};
	}
	
	/**
	 * Factory for {@link MimeMessageToConstraint} instances.
	 * 
	 * @param listOfEmailAddrs
	 * @param javaxMailRecipientType
	 * @return
	 */
	protected Constraint messageTo(InternetAddress[] listOfEmailAddrs, 
			Message.RecipientType javaxMailRecipientType) {
		return new MimeMessageToConstraint(listOfEmailAddrs, javaxMailRecipientType);
	}
	
}