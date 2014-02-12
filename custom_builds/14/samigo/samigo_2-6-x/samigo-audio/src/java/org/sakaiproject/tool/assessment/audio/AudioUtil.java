/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.audio;

import java.util.Locale;
import java.util.ResourceBundle;

public class AudioUtil {

	private static final String RESOURCE_PACKAGE = "org.sakaiproject.tool.assessment.bundle";

	private static final String RESOURCE_NAME = "AudioResources";

	private static AudioUtil INSTANCE;

	private String localeLanguage;

	private String localeCountry;

	public static AudioUtil getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AudioUtil();
		}
		return INSTANCE;
	}

	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	public String getLocaleLanguage() {
		return localeLanguage;
	}

	public void setLocaleCountry(String localeCountry) {
		this.localeCountry = localeCountry;
	}

	public String getLocaleCountry() {
		return localeCountry;
	}

	public ResourceBundle getResourceBundle() {
		Locale locale = Locale.getDefault();
		if (localeLanguage != null && !"".equals(localeLanguage)) {
			if (this.localeCountry != null && !"".equals(this.localeCountry)) {
				locale = new Locale(localeLanguage, localeCountry);
			} else {
				locale = new Locale(localeLanguage);
			}
		}
		ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PACKAGE + "."
				+ RESOURCE_NAME, locale);
		return res;
	}

}
