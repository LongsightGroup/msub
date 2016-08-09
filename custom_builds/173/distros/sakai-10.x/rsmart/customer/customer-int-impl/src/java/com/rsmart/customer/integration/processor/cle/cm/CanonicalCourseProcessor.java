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
import org.sakaiproject.coursemanagement.api.CanonicalCourse;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CanonicalCourseProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(CanonicalCourseProcessor.class);


    
    public void processRow(String[] data, ProcessorState state) throws Exception {

        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Reconciling canonical course " + eid);

        if(cmService.isCanonicalCourseDefined(eid)) {
            updateCanonicalCourse(cmService.getCanonicalCourse(eid), data);
        } else {
            addCanonicalCourse(data);
        }

        if (data.length > 3) {
            String courseSet = data[3];
            if (courseSet != null && cmService.isCourseSetDefined(courseSet)) {
                cmAdmin.addCanonicalCourseToCourseSet(courseSet, eid);
            }
        }

    }

    public String getProcessorTitle() {
        return "Canonical Course Processor";
    }



	public void addCanonicalCourse(String[] data) {
		String eid = data[0];
		if(log.isDebugEnabled()) log.debug("Adding CanonicalCourse + " + eid);
		String title = data[1];
		String description = data[2];
		cmAdmin.createCanonicalCourse(eid, title, description);
	}

	public void updateCanonicalCourse(CanonicalCourse canonicalCourse, String[] data) {
		if(log.isDebugEnabled()) log.debug("Updating CanonicalCourse + " + canonicalCourse.getEid());
		canonicalCourse.setTitle(data[1]);
		canonicalCourse.setDescription(data[2]);
		cmAdmin.updateCanonicalCourse(canonicalCourse);
	}
}
