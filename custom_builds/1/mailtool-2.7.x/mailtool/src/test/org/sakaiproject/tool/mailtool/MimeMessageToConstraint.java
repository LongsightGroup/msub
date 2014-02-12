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

import java.util.Arrays;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jmock.core.Constraint;
import org.jmock.core.Formatting;

/**
 * Asserts that a {@link MimeMessage} has been addressed
 * to some list of addresses.
 * 
 * 
 * @author dmccallum@unicon.net
 *
 */
public class MimeMessageToConstraint implements Constraint {

	private final InternetAddress[] toAddresses;
	private final Message.RecipientType javaxMailRecipientType;

    public MimeMessageToConstraint(InternetAddress[] toAddresses, 
    		Message.RecipientType javaxMailRecipientType) {
        this.toAddresses = toAddresses;
        this.javaxMailRecipientType = javaxMailRecipientType;
    }

    public boolean eval(Object objToTest) {
    	
    	if ( objToTest == null ) {
    		return false;
    	}
    	
    	if ( !(objToTest instanceof MimeMessage) ) {
    		return false;
    	}
    	
    	try {
    		Address[] actualTos = 
    			((MimeMessage)objToTest).getRecipients(javaxMailRecipientType);
    		
    		if ( actualTos == null || actualTos.length == 0 ) {
    			return toAddresses == null;
    		}
    		
    		return Arrays.equals(toAddresses, actualTos);
    		
    	} catch ( MessagingException e ) {
    		throw new RuntimeException("Failed to compare from addresses", e);
    	}
    	
    }

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("a message to ").
            append(Formatting.toReadableString(toAddresses)).
            append(" in field: " +  Formatting.toReadableString(javaxMailRecipientType));
    }
	
}