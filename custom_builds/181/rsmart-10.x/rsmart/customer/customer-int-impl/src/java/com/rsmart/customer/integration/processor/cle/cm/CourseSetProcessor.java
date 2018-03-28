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
public class CourseSetProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(CourseSetProcessor.class);

    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Reconciling course set " + eid);

        CourseSet courseSet = null;
        if(cmService.isCourseSetDefined(eid)) {
            courseSet = updateCourseSet(cmService.getCourseSet(eid), data);
        } else {
            courseSet = addCourseSet(data);
        }

    }

	public CourseSet updateCourseSet(CourseSet courseSet, String[] data) {
		if(log.isDebugEnabled()) log.debug("Updating CourseSet + " + courseSet.getEid());
		courseSet.setTitle(data[1]);
		courseSet.setDescription(data[2]);
		courseSet.setCategory(data[3]);
		String parentEid = (data[4] == null || data[4].length() == 0) ? null : data[4];
		if(parentEid != null && cmService.isCourseSetDefined(parentEid)) {
			CourseSet parent = cmService.getCourseSet(parentEid);
			courseSet.setParent(parent);
		}
		cmAdmin.updateCourseSet(courseSet);
		return courseSet;
	}

	public CourseSet addCourseSet(String[] data) {
		String eid = data[0];
		if(log.isDebugEnabled()) log.debug("Adding CourseSet + " + eid);
		String title = data[1];
		String description = data[2];
		String category = data[3];
		String parentEid = (data[4] == null || data[4].length() == 0) ? null : data[4];
		return cmAdmin.createCourseSet(eid, title, description, category, parentEid);
	}

    public String getProcessorTitle() {
        return "Course Set Processor";
    }
}