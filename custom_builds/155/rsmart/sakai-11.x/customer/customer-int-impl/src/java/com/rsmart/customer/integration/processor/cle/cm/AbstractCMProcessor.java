/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.processor.cle.cm;

import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 5:37:18 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class AbstractCMProcessor extends BaseCsvFileProcessor {
    private static final Log log = LogFactory.getLog(EnrollmentProcessor.class);

    protected CourseManagementService cmService;
    protected CourseManagementAdministration cmAdmin;
    protected String dateFormat = "M/d/yyyy";

    public CourseManagementService getCmService() {
        return cmService;
    }

    public void setCmService(CourseManagementService cmService) {
        this.cmService = cmService;
    }

    public CourseManagementAdministration getCmAdmin() {
        return cmAdmin;
    }

    public void setCmAdmin(CourseManagementAdministration cmAdmin) {
        this.cmAdmin = cmAdmin;
    }

	public Date getDate(String str) {
		if(str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		try {
			return df.parse(str);
		} catch (ParseException pe) {
            throw new RuntimeException("invalid data: " + str);
		}
	}

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
