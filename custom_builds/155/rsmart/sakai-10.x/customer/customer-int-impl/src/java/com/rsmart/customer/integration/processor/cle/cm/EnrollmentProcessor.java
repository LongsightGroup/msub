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
import org.sakaiproject.coursemanagement.api.Enrollment;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollmentProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(EnrollmentProcessor.class);

    private static final Map getEnrollmentMap(final ProcessorState state)
    {
        return (Map) state.getConfiguration().get("enrollmentMap");
    }

    public ProcessorState init(Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put ("enrollmentMap", new HashMap());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map
            enrollmentMap = getEnrollmentMap(state);

        String enrollmentSetEid = data[0];
        List members = (List) enrollmentMap.get(enrollmentSetEid);
        if (members == null){
            members = new ArrayList();
            enrollmentMap.put(enrollmentSetEid, members);
        }
        members.add(data);
    }


    public String getProcessorTitle() {
        return "Enrollment Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map
            enrollmentMap = getEnrollmentMap(state);

        for (Iterator<String> i = enrollmentMap.keySet().iterator(); i.hasNext(); ) {
            String enrollmentSetEid = i.next();
            List newEnrollmentElements = (List)enrollmentMap.get(enrollmentSetEid);
            Set newUserEids = new HashSet();

            if (!cmService.isEnrollmentSetDefined(enrollmentSetEid)) {
                log.error("can't sync enrollment for non-existent enrollment set with eid " + enrollmentSetEid);
                continue;
            }

            Set existingEnrollments = (Set)cmService.getEnrollments(enrollmentSetEid);

            for(Iterator iter = newEnrollmentElements.iterator(); iter.hasNext();) {
                String[] enrollmentElement = (String[])iter.next();
                String userEid = enrollmentElement[1];
                newUserEids.add(userEid);
                String status = enrollmentElement[2];
                String credits = enrollmentElement[3];
                String gradingScheme = enrollmentElement[4];
                cmAdmin.addOrUpdateEnrollment(userEid,enrollmentSetEid, status, credits, gradingScheme);
            }

            for(Iterator iter = existingEnrollments.iterator(); iter.hasNext();) {
                Enrollment existingEnr = (Enrollment) iter.next();
                if( ! newUserEids.contains(existingEnr.getUserId())) {
                    // Drop this enrollment
                    cmAdmin.removeEnrollment(existingEnr.getUserId(), enrollmentSetEid);
                }
            }
            
        }
    }

    public void preProcess(ProcessorState state) throws Exception {
        Map
            enrollmentMap = getEnrollmentMap(state);

        enrollmentMap.clear();
    }
}