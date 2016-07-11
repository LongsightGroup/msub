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

import java.util.Date;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseOfferingProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(CourseOfferingProcessor.class);


    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Reconciling course offering " + eid);

        CourseOffering courseOffering = null;

        if(cmService.isCourseOfferingDefined(eid)) {
            courseOffering = updateCourseOffering(cmService.getCourseOffering(eid), data);
        } else {
            courseOffering = addCourseOffering(data);
        }
        if (data.length > 8) {
            String courseSet = data[8];
            if (courseSet != null && cmService.isCourseSetDefined(courseSet)){
                cmAdmin.addCourseOfferingToCourseSet(courseSet, eid);
            }
        }
    }

    public String getProcessorTitle() {
        return "Course Offering Processor";
    }



	public CourseOffering updateCourseOffering(CourseOffering courseOffering, String[] data) {
		if(log.isDebugEnabled()) log.debug("Updating CourseOffering + " + courseOffering.getEid());
		AcademicSession newAcademicSession = cmService.getAcademicSession(data[1]);
		courseOffering.setTitle(data[2]);
		courseOffering.setDescription(data[3]);
		courseOffering.setStatus(data[4]);
		courseOffering.setAcademicSession(newAcademicSession);
		courseOffering.setStartDate(getDate(data[5]));
		courseOffering.setEndDate(getDate(data[6]));

		// Note: we can't update a course offering's canonical course.  This seems reasonable.

		cmAdmin.updateCourseOffering(courseOffering);
		return courseOffering;
	}

	public CourseOffering addCourseOffering(String[] data) {
		String eid = data[0];
		if(log.isDebugEnabled()) log.debug("Adding CourseOffering + " + eid);
		String title = data[2];
		String description = data[3];
		String status = data[4];
		String academicSessionEid = data[1];
		String canonicalCourseEid = data[7];
		Date startDate = getDate(data[5]);
		Date endDate = getDate(data[6]);
		return cmAdmin.createCourseOffering(eid, title, description, status, academicSessionEid, canonicalCourseEid, startDate, endDate);
	}
}