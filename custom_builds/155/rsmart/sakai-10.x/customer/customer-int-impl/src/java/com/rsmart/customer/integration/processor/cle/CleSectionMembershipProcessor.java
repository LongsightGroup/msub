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

package com.rsmart.customer.integration.processor.cle;

import com.rsmart.customer.integration.model.CleSectionMembership;
import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import com.rsmart.customer.integration.processor.ProcessorState;
import com.rsmart.customer.integration.util.SiteHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.User;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 28, 2008
 * Time: 9:22:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleSectionMembershipProcessor extends BaseCsvFileProcessor {
    private static final Log logger = LogFactory.getLog(CleSectionMembershipProcessor.class);

    private SiteHelper siteHelper;
    private SectionManager sectionManager;
    private String taRole;
    private String studentRole;
    private UserDirectoryService userDirectoryService;
    private boolean deleteStudents = true;
    private boolean deleteTeachingAssistants = true;

    public SectionManager getSectionManager() {
        return sectionManager;
    }

    public void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }

    public String getProcessorTitle() {
        return "SIS Section Membership Processsor";
    }

    public ProcessorState init(Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("memberships", new HashMap());

        return state;
    }

    public void preProcess(ProcessorState state)
        throws Exception
    {
        Map
            memberships = (Map)state.getConfiguration().get("memberships");

        memberships.clear();
    }

    public void processRow(String[] data, ProcessorState state)
        throws Exception
    {

        String sectionEid = data[0];
        String userEid = data[1];
        String role = data[2];
        String courseEid = data[3];


        Site site = siteHelper.findSite(courseEid);

        if (site == null) {
            throw new Exception("can't find a site with an " + SiteHelper.EXTERNAL_SITE_ID +  " of [" +
                    courseEid + "], skipping section membership record with sectionEid = [" +
                    sectionEid + "] for user [" + userEid + "]");
        }

        for (Iterator<Group> i=site.getGroups().iterator(); i.hasNext(); ) {
            Group group = i.next();
            if (group.getProperties().getProperty("externalSectionId") != null &&
                    group.getProperties().getProperty("externalSectionId").equals(sectionEid)) {
                try {
                    User user = userDirectoryService.getUserByEid(userEid);
                    CleSectionMembership sectionMembership = new CleSectionMembership();
                    sectionMembership.setUserId(user.getId());
                    sectionMembership.setRole(role);
                    sectionMembership.setGroupReference(group.getReference());
                    logger.debug("queuing section membership: " + sectionMembership.toString());
                    addMembership(sectionEid,sectionMembership, state);
                    return;
                } catch (UserNotDefinedException e) {
                    logger.error("can't find user with eid=[" + userEid + "] for adding to group: " +
                            group.getId());    
                }
            }
        }

        throw new Exception("can't find a section with an eid of [" + sectionEid +
                "] in site [" + site.getId() + "] skipping section membership record for user = [" + userEid + "]");

    }

    protected void addMembership(String sectionEid, CleSectionMembership sectionMembership, ProcessorState state)
    {
        Map
            memberships = (Map) state.getConfiguration().get("memberships");

        List membershipList = (List) memberships.get(sectionEid);
        if (membershipList == null) {
            membershipList = new ArrayList();
            memberships.put(sectionEid, membershipList);
        }
        membershipList.add(sectionMembership);
    }

    public void postProcess(ProcessorState state)
        throws Exception
    {
        Map
            memberships = (Map)state.getConfiguration().get("memberships");

        logger.debug("postProcess() " + memberships.size() + " sections to inspect");

        for (Iterator<String> i= memberships.keySet().iterator(); i.hasNext();) {
            String sectionEid = i.next();
            String groupReference = null;
            List currentMemberships = (List) memberships.get(sectionEid);
            List studentsAdded = new ArrayList();
            List tasAdded = new ArrayList();

            logger.debug(currentMemberships.size() + " memberships for sectionEid: " + sectionEid);
            for (Iterator<CleSectionMembership> j= currentMemberships.iterator(); j.hasNext();) {
                CleSectionMembership sectionMembership = j.next();
                groupReference = sectionMembership.getGroupReference();

                //add membership
                Role role = getRole(sectionMembership.getRole());
                if (role.equals(Role.NONE)) {
                  logger.error("unknown role of [" + sectionMembership.getRole() + "] for group [" +
                          sectionMembership.getGroupReference() + "] assigning no role for user [" +
                          sectionMembership.getUserId() + "]");
                }
                if (role.equals(Role.STUDENT)) {
                    studentsAdded.add(sectionMembership.getUserId());
                }
                if (role.equals(Role.TA)) {
                    tasAdded.add(sectionMembership.getUserId());
                }
                try {
                    sectionManager.addSectionMembership(sectionMembership.getUserId(), role, sectionMembership.getGroupReference());
                    logger.debug("added section membership: " + sectionMembership.toString());
                } catch (Exception e) {
                    logger.error("error adding section membership: " + sectionMembership.toString(), e);
                }
            }

            deleteStudents(groupReference, studentsAdded);
            deleteTeachingAssistants(groupReference, tasAdded);

        }
        memberships.clear();
        memberships = null;
    }

    private void deleteTeachingAssistants(String groupReference, List tasAdded) {
        if (isDeleteTeachingAssistants()) {
            for (Iterator<ParticipationRecord> k=sectionManager.getSectionTeachingAssistants(groupReference).iterator();k.hasNext();){
                ParticipationRecord pRecord = k.next();
                if (!tasAdded.contains(pRecord.getUser().getUserUid())) {
                    try {
                        logger.debug("dropping section membership from section: " + groupReference + " for user " + pRecord.getUser().getUserUid());
                        sectionManager.dropSectionMembership(pRecord.getUser().getUserUid(), groupReference);
                    } catch (Exception e) {
                        logger.error("problem dropping section membership", e);
                    }
                }
            }
        }
    }

    private void deleteStudents(String groupReference, List studentsAdded) {
        if (isDeleteStudents()) {
            for (Iterator<EnrollmentRecord> k=sectionManager.getSectionEnrollments(groupReference).iterator();k.hasNext();){
                EnrollmentRecord eRecord = k.next();
                if (!studentsAdded.contains(eRecord.getUser().getUserUid())) {
                    try {
                        logger.debug("dropping section membership from section: " + groupReference + " for user " + eRecord.getUser().getUserUid());
                        sectionManager.dropSectionMembership(eRecord.getUser().getUserUid(), groupReference);
                    } catch (Exception e) {
                        logger.error("problem dropping section membership", e);
                    }
                }
            }
        }
    }

    /**
     * converts role in SIS file to internal Role
     * @param role
     * @return
     */
    protected Role getRole(String role) {
        if (role.equalsIgnoreCase(studentRole)) {
            return Role.STUDENT;
        }
        if (role.equalsIgnoreCase(taRole)) {
            return Role.TA;
        }

       return Role.NONE;

    }

    public SiteHelper getSiteHelper() {
        return siteHelper;
    }

    public void setSiteHelper(SiteHelper siteHelper) {
        this.siteHelper = siteHelper;
    }

    public String getTaRole() {
        return taRole;
    }

    public void setTaRole(String taRole) {
        this.taRole = taRole;
    }

    public String getStudentRole() {
        return studentRole;
    }

    public void setStudentRole(String studentRole) {
        this.studentRole = studentRole;
    }

    public UserDirectoryService getUserDirectoryService() {

        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public boolean isDeleteStudents() {
        return deleteStudents;
    }

    public void setDeleteStudents(boolean deleteStudents) {
        this.deleteStudents = deleteStudents;
    }

    public boolean isDeleteTeachingAssistants() {
        return deleteTeachingAssistants;
    }

    public void setDeleteTeachingAssistants(boolean deleteTeachingAssistants) {
        this.deleteTeachingAssistants = deleteTeachingAssistants;
    }
}
