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

import junit.framework.TestCase;

public class MailtoolEmailListValidationTest extends TestCase {

	private Mailtool mailtool;
	
	protected void setUp() throws Exception {
		// some hacks b/c of env assumptions in the Mailtool constructor
		mailtool = new Mailtool() {
			
			protected String getSiteID() {
				return "THIS_IS_A_STUB_SITE_ID";
			}
			
			protected String getSiteTitle() {
				return "THIS_IS_A_STUB_SITE_TITLE";
			}

			protected String getSiteRealmID() {
				return "/site/" + getSiteID();
			}
			
			public String getGroupAwareRole() {
				return groupAwareRoleDefault;
			}
			
			protected String getConfigParam(String parameter) {
				return "";
			}
			
			public int readMaxNumAttachment() {
				return 10000;
			}
			
			protected String getSiteType() {
				return "";
			}
			
			public void getRecipientSelectors() {
				// do nothing
			}
			
			public void checkifGroupAwareRoleExist() {
				// do nothing
			}
			
			public boolean isEmailArchiveAddedToSite() {
				return false;
			}
			
		};
		
		super.setUp();
	}
	

	public void testIsValidEmailList_AcceptsSimpleAddress() {	
		String toMatch = "someone@domain.com";
		assertTrue("Should have validated a simple address [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_AcceptsAddressWithFourthLevelDomain() {
		String toMatch = "someone@subber.sub.domain.com";
		assertTrue("Should have validated an address with a third-level domain [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_RejectsAddressWithEmptyDomainSegment() {
		String toMatch = "someone@sub..com";
		assertFalse("Should have rejected an address with empty domain segment [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_RejectsAddressWithUnqualifiedDomain() {
		String toMatch = "someone@domain";
		assertFalse("Should have rejected an address with invalid domain [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_RejectsAddressWithOnlyOneToken() {
		String toMatch = "garbage";
		assertFalse("Should have rejected an address with invalid domain [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
		
	public void testIsValidEmaiList_AcceptsAddressWithMixedCaseDomain() {
		String toMatch = "someone@sUb.doMain.CoM";
		assertTrue("Should have validated an address with a mixed-case domain [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_AcceptsCommaDelimitedListOfValidAddresses() {
		String toMatch = "someone@domain.com,someone@domain.com";
		assertTrue("Should have validated multiple simple addresses [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_AcceptsCommaDelimitedListOfValidAddressesWithWhitespace() {
		String toMatch = "someone@domain.com, someoneelse@domain.com";
		assertTrue("Should have validated multiple simple addresses with comma and space delims [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
		
	public void testIsValidEmailList_AcceptsSemiColonDelimitedListOfValidAddresses() {
		String toMatch = "someone@domain.com;someoneelse@domain.com";
		assertTrue("Should have validated multiple simple addresses with semicolon delim [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_AcceptsSemiColonDelimitedListOfValidAddressesWithWhitespace() {
		String toMatch = "someone@domain.com; someoneelse@domain.com";
		assertTrue("Should have validated multiple simple addresses with semicolon and space delims [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
		
	public void testIsValidEmailList_AcceptsLongListOfValidAddresseWithMixedDelimiters() {
		String toMatch = "someone@domain.com, someoneelse@domain.com; someoneelseentirely@domain.com,someonenew@domain.com";
		assertTrue("Should have validated a large number of simple addresses with mixed delims [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
		
	public void testIsValidEmailList_RejectsListOfAddressesWithGarbageInFirstElement() {
		String toMatch = "garbage, someone@domain.com, someoneelseentirely@domain.com";
		assertFalse("Should have rejected a list of addresses with garbage in the first element [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_RejectsListOfAddressesWithGarbageInInnerElement() {
		String toMatch = "someone@domain.com, garbage, someoneelseentirely@domain.com";
		assertFalse("Should have rejected a list of addresses with garbage in an inner element [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_RejectsListOfAddressesWithGarbageInLastElement() {
		String toMatch = "someone@domain.com, someoneelseentirely@domain.com, garbage";
		assertFalse("Should have rejected a list of addresses with garbage in the last element [" + toMatch + "]", 
				mailtool.isValidEmailList(toMatch));
	}
	
	public void testIsValidEmailList_AcceptsAddressAtLocalhost() {
		String toMatch = "someone@localhost";
		assertTrue("Should have validated an address at localhost [" + toMatch + "]",
				mailtool.isValidEmailList(toMatch));
	}
		
}