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

import com.rsmart.customer.integration.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollmentSetProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(CourseOfferingMemberProcessor.class);


    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Reconciling enrollment set " + eid);

        EnrollmentSet enr = null;
        if(cmService.isEnrollmentSetDefined(eid)) {
            enr = updateEnrollmentSet(cmService.getEnrollmentSet(eid), data);
        } else {
            enr = addEnrollmentSet(data);
        }

    }

    public String getProcessorTitle() {
        return "Enrollment Set Processor";
    }

	public EnrollmentSet addEnrollmentSet(String[] data) {
		String eid = data[0];
		if(log.isDebugEnabled()) log.debug("Adding EnrollmentSet + " + eid);
		String title = data[1];
		String description = data[2];
		String category = data[3];
		String courseOfferingEid = data[4];
		String defaultEnrollmentCredits = data[5];
		return cmAdmin.createEnrollmentSet(eid, title, description, category, defaultEnrollmentCredits, courseOfferingEid, null);
	}

	public EnrollmentSet updateEnrollmentSet(EnrollmentSet enrollmentSet, String[] data) {
		if(log.isDebugEnabled()) log.debug("Updating EnrollmentSet + " + enrollmentSet.getEid());
		enrollmentSet.setTitle(data[1]);
		enrollmentSet.setDescription(data[2]);
		enrollmentSet.setCategory(data[3]);
		enrollmentSet.setDefaultEnrollmentCredits(data[5]);
		// Note: It is not possible to change the course offering, but this seems OK.

		cmAdmin.updateEnrollmentSet(enrollmentSet);
		return enrollmentSet;
	}

}