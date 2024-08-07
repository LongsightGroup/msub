/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/kernel-impl/src/main/java/org/sakaiproject/user/impl/UserNotificationPreferencesRegistrationServiceImpl.java $
 * $Id: UserNotificationPreferencesRegistrationServiceImpl.java 26234 2011-04-11 19:33:29Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;

/**
 * Service implementation to register email notification preferences
 * @author chrismaurer
 *
 */
public class UserNotificationPreferencesRegistrationServiceImpl implements
		UserNotificationPreferencesRegistrationService {

	private List<UserNotificationPreferencesRegistration> registeredItems = new ArrayList<UserNotificationPreferencesRegistration>();
	private List<String> keys = new ArrayList<String>();
	
	/**
	 * {@inheritDoc}
	 */
	public void register(UserNotificationPreferencesRegistration reg) {
		getRegisteredItems().add(reg);
		getKeys().add(reg.getType());
	}

	/**
	 * Setter
	 * @param registeredItems
	 */
	public void setRegisteredItems(List<UserNotificationPreferencesRegistration> registeredItems) {
		this.registeredItems = registeredItems;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<UserNotificationPreferencesRegistration> getRegisteredItems() {
		return registeredItems;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getKeys() {
		return keys;
	}
	
	/**
	 * Setter
	 * @param keys
	 */
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

}
