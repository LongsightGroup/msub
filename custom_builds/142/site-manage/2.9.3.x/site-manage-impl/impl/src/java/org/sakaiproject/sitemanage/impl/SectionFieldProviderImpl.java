/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/sitemanage/impl/SectionFieldProviderImpl.java $
 * $Id: SectionFieldProviderImpl.java 23942 2010-12-09 16:48:03Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.sitemanage.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitemanage.api.SectionField;
import org.sakaiproject.sitemanage.api.SectionFieldProvider;
import org.sakaiproject.util.ResourceLoader;

public class SectionFieldProviderImpl implements SectionFieldProvider {
	private static final Log log = LogFactory.getLog(SectionFieldProviderImpl.class);

	public List<SectionField> getRequiredFields() {
		ResourceLoader resourceLoader = new ResourceLoader("SectionFields");
		List<SectionField> fieldList = new ArrayList<SectionField>();

		fieldList.add(new SectionFieldImpl(resourceLoader.getString("required_fields_subject"), null, 8));
		fieldList.add(new SectionFieldImpl(resourceLoader.getString("required_fields_course"), null, 8));
		fieldList.add(new SectionFieldImpl(resourceLoader.getString("required_fields_section"), null, 8));		
		
		return fieldList;
	}

	public String getSectionEid(String academicSessionEid, List<SectionField> fields) {
		if(fields == null || fields.isEmpty()) {
			if(log.isDebugEnabled()) log.debug("Returning an empty sectionEID for an empty (or null) list of fields");
			return "";
		}
		
		StringBuffer sectionEid = new StringBuffer();
		for(int i = 0; i < fields.size(); i++) {
			SectionField sf = fields.get(i);
            sectionEid.append(sf.getValue());
            sectionEid.append("_");
		}
        sectionEid.append(academicSessionEid);
		if(log.isDebugEnabled()) log.debug(this + ":getSectionEid: Generated section eid = " + sectionEid.toString());
		return sectionEid.toString();
	}
	
	public String getSectionTitle(String academicSessionEid, List<SectionField> fields) {
		if(fields == null || fields.isEmpty()) {
			if(log.isDebugEnabled()) log.debug(this + ":getSectionTitle: Returning an empty section title for an empty (or null) list of fields");
			return "";
		}
		
		StringBuffer rv = new StringBuffer();
		
		String[] values = new String[fields.size()+1];
		for(int i = 0; i < fields.size(); i++) {
			SectionField sf = fields.get(i);
			rv.append(sf.getValue()).append(" ");
		}
		rv.append(academicSessionEid);
		
		return rv.toString();
	}

	public void init() {
	}

	public void destroy() {
	}

}
