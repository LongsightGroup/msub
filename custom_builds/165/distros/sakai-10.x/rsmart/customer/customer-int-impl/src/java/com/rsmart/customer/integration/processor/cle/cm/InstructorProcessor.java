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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 30, 2008
 * Time: 3:20:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstructorProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(InstructorProcessor.class);

    private final static Map getInstructorMap (final ProcessorState state)
    {
        return (Map)state.getConfiguration().get("instructorMap");
    }

    public ProcessorState init(Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("instructorMap", new HashMap());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception
    {
        Map
            instructorMap = getInstructorMap(state);

        Set enrollmentSet = (Set)instructorMap.get(data[0]);
        if (enrollmentSet == null) {
            enrollmentSet = new HashSet();
            instructorMap.put(data[0], enrollmentSet);
        }
        enrollmentSet.add(data[1]);
    }

    public String getProcessorTitle() {
        return "Instructor Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map
            instructorMap = getInstructorMap(state);

        for (Iterator<String> i=instructorMap.keySet().iterator();i.hasNext();) {
            String enrollmentSetEid = i.next();
            Set newInstructorElements = (Set) instructorMap.get(enrollmentSetEid);
            Set newUserEids = new HashSet();

            for(Iterator iter = newInstructorElements.iterator(); iter.hasNext();) {
                String userEid = (String) iter.next();
                newUserEids.add(userEid);
            }

            if (!cmService.isEnrollmentSetDefined(enrollmentSetEid)) {
                log.error("can't sync instructors no enrollment set exists with eid: " + enrollmentSetEid);
                continue;
            }

            EnrollmentSet enrollmentSet = cmService.getEnrollmentSet(enrollmentSetEid);

            Set officialInstructors = enrollmentSet.getOfficialInstructors();
            if(officialInstructors == null) {
                officialInstructors = new HashSet();
                enrollmentSet.setOfficialInstructors(officialInstructors);
            }
            officialInstructors.clear();
            officialInstructors.addAll(newUserEids);
            try {
                cmAdmin.updateEnrollmentSet(enrollmentSet);
            } catch (Exception e) {
                log.error("can't save instructor enrollment set",e);
            }
        }
    }

}
