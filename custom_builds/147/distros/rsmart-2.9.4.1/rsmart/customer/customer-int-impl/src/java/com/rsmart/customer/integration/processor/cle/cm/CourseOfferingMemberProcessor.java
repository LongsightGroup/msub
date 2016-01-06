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
import org.sakaiproject.coursemanagement.api.Membership;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseOfferingMemberProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(CourseOfferingMemberProcessor.class);

    public ProcessorState init(Map config) {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("membersMap", new HashMap());

        return state;
    }

    private static final Map getMembersMap(final ProcessorState state)
    {
        return (Map)state.getConfiguration().get("membersMap");
    }
    
    public void processRow(String[] data, ProcessorState state) throws Exception {

        Map
            membersMap = getMembersMap(state);

        List memberList = (List)membersMap.get(data[0]);
        if (memberList == null) {
            memberList = new ArrayList();
            membersMap.put(data[0], memberList);
        }
        memberList.add(data);
    }

    public void postProcess(ProcessorState state) throws Exception {

        Map
            membersMap = getMembersMap(state);

        for (Iterator<String> i=membersMap.keySet().iterator();i.hasNext();){
            String courseOfferingEid = i.next();

            if (!cmService.isCourseOfferingDefined(courseOfferingEid)) {
                log.error("can't find course offering with eid: " + courseOfferingEid);
                continue;
            }
            Set existingMembers = cmService.getCourseOfferingMemberships(courseOfferingEid);

            // Build a map of existing member userEids to Memberships
            Map existingMemberMap = new HashMap(existingMembers.size());
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                existingMemberMap.put(member.getUserId(), member);
            }

            // Keep track of the new members userEids, and add/update them
            Set newMembers = new HashSet();
            List memberElements = (List) membersMap.get(courseOfferingEid);
            for(Iterator<String[]> iter = memberElements.iterator(); iter.hasNext();) {
                String[] member = iter.next();
                String userEid = member[1];
                String role = member[2];
                String status = member[3];
                newMembers.add(cmAdmin.addOrUpdateCourseOfferingMembership(userEid, role, courseOfferingEid, status));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                cmAdmin.removeCourseOfferingMembership(member.getUserId(), courseOfferingEid);
            }
        }

    }

    public String getProcessorTitle() {
        return "Course Offering Member Processor";
    }
}