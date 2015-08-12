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
import org.sakaiproject.coursemanagement.api.Membership;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SectionMemberProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(SectionMemberProcessor.class);

    private final static Map getSectionMembers (ProcessorState state)
    {
        return (Map)state.getConfiguration().get("sectionMembers");
    }

    public ProcessorState init (Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("sectionMembers", new HashMap());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map sectionMembers = getSectionMembers(state);
        String sectionEid = data[0];
        List members = (List) sectionMembers.get(sectionEid);
        if (members == null){
            members = new ArrayList();
            sectionMembers.put(sectionEid, members);
        }
        members.add(data);
    }

    public String getProcessorTitle() {
        return "Section Memeber Processor";
    }


    public void preProcess(ProcessorState state) throws Exception {
        Map sectionMembers = getSectionMembers(state);
        sectionMembers.clear();
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map sectionMembers = getSectionMembers(state);
        for (Iterator<String> i=sectionMembers.keySet().iterator(); i.hasNext();) {
            String sectionEid = i.next();

            if(!cmService.isSectionDefined(sectionEid)) {
                log.error("can't sync section memberships, no section with eid of " + sectionEid + " found");
                continue;
            }

            Set existingMembers = cmService.getSectionMemberships(sectionEid);

            // Build a map of existing member userEids to Memberships
            Map existingMemberMap = new HashMap(existingMembers.size());
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                existingMemberMap.put(member.getUserId(), member);
            }

            List memberElements = (List) sectionMembers.get(sectionEid);

            // Keep track of the new members userEids, and add/update them
            Set newMembers = new HashSet();
            for(Iterator iter = memberElements.iterator(); iter.hasNext();) {
                String[] memberElement = (String[])iter.next();
                String userEid = memberElement[1];
                String role = memberElement[2];
                String status = memberElement[3];
                newMembers.add(cmAdmin.addOrUpdateSectionMembership(userEid, role, sectionEid, status));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                cmAdmin.removeSectionMembership(member.getUserId(), sectionEid);
            }
        }

    }
                }