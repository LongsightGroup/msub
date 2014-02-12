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

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.jmock.core.Constraint;
import org.jmock.core.Formatting;

/**
 * Verifies that a given {@link MimeMessage} is "from" the
 * expected {@link InternetAddress} (and not any other).
 */
public class MimeMessageFromConstraint implements Constraint {
	private final InternetAddress from;

    public MimeMessageFromConstraint(InternetAddress from) {
        this.from = from;
    }

    public boolean eval(Object objToTest) {
    	
    	if ( objToTest == null ) {
    		return false;
    	}
    	
    	if ( !(objToTest instanceof MimeMessage) ) {
    		return false;
    	}
    	
    	try {
    		Address[] actualFroms = ((MimeMessage)objToTest).getFrom();
    		if ( actualFroms == null || actualFroms.length == 0 ) {
    			return from == null;
    		}
    		if ( actualFroms.length > 1 ) {
    			return false;
    		}
    		return actualFroms[0].equals(from);
    	} catch ( MessagingException e ) {
    		throw new RuntimeException("Failed to compare from addresses", e);
    	}
    	
    }

    public StringBuffer describeTo(StringBuffer buffer) {
        return buffer.append("a message from ").
            append(Formatting.toReadableString(from));
    }
}