package com.rsmart.customer.integration.processor.cle.cm;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Columns are:  mode, membership_eid, user_eid, role, status
 *
 * Valid values for mode are:  "instructor", "courseoffering", or "courseset"
 * The mode designates which type of membership record this is.  The role and status are not used for instructor mode
 * records but are necessary for courseoffering and courseset memberships.  They should be left blank for the instructor mode
 *
 *
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Oct 15, 2010
 * Time: 9:00:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class MembershipProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(MembershipProcessor.class);

    private final static Map getInstructorMap (final ProcessorState state)
    {
        return (Map)state.getConfiguration().get("instructorMap");
    }

    private final static Map getCourseSetMembershipMap (final ProcessorState state)
    {
        return (Map)state.getConfiguration().get("courseSetMembersMap");
    }

    private final static Map getMembersMap (final ProcessorState state)
    {
        return (Map)state.getConfiguration().get("membersMap");
    }



    public ProcessorState init(Map config) {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("instructorMap", new HashMap());
        state.getConfiguration().put("membersMap", new HashMap());
        state.getConfiguration().put("courseSetMembersMap", new HashMap());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
        Map instructorMap = getInstructorMap(state);
        Map membersMap = getMembersMap(state);
        Map courseSetMembersMap = getCourseSetMembershipMap(state);

        String mode = data[0];

        if (data[0].equalsIgnoreCase("instructor")){
            Set enrollmentSet = (Set)instructorMap.get(data[1]);
            if (enrollmentSet == null) {
                enrollmentSet = new HashSet();
                instructorMap.put(data[1], enrollmentSet);
            }
            enrollmentSet.add(data[2]);
        }
        if (data[0].equalsIgnoreCase("courseoffering")){

            List memberList = (List)membersMap.get(data[1]);
            if (memberList == null) {
                memberList = new ArrayList();
                membersMap.put(data[1], memberList);
            }
            memberList.add(data);
        }

        if (data[0].equalsIgnoreCase("courseset")){

            List courseSetMemberList = (List)courseSetMembersMap.get(data[1]);
            if (courseSetMemberList == null) {
                courseSetMemberList = new ArrayList();
                courseSetMembersMap.put(data[1], courseSetMemberList);
            }
            courseSetMemberList.add(data);
        }

    }

    public String getProcessorTitle() {
        return "Membership Processor";
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map instructorMap = getInstructorMap(state);
        Map membersMap = getMembersMap(state);
        Map courseSetMembersMap = getCourseSetMembershipMap(state);

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
                String userEid = member[2];
                String role = member[3];
                String status = member[4];
                newMembers.add(cmAdmin.addOrUpdateCourseOfferingMembership(userEid, role, courseOfferingEid, status));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                cmAdmin.removeCourseOfferingMembership(member.getUserId(), courseOfferingEid);
            }
        }

        for (Iterator<String> i=courseSetMembersMap.keySet().iterator();i.hasNext();){
            String courseSetEid = i.next();

            if (!cmService.isCourseSetDefined(courseSetEid)) {
                log.error("can't find course set with eid: " + courseSetEid);
                continue;
            }
            Set existingMembers = cmService.getCourseSetMemberships(courseSetEid);

            // Build a map of existing member userEids to Memberships
            Map existingMemberMap = new HashMap(existingMembers.size());
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                existingMemberMap.put(member.getUserId(), member);
            }

            // Keep track of the new members userEids, and add/update them
            Set newMembers = new HashSet();
            List memberElements = (List) courseSetMembersMap.get(courseSetEid);
            for(Iterator<String[]> iter = memberElements.iterator(); iter.hasNext();) {
                String[] member = iter.next();
                String userEid = member[2];
                String role = member[3];
                String status = member[4];
                newMembers.add(cmAdmin.addOrUpdateCourseSetMembership(userEid, role, courseSetEid, status));
            }

            // For everybody not in the newMembers set, remove their memberships
            existingMembers.removeAll(newMembers);
            for(Iterator iter = existingMembers.iterator(); iter.hasNext();) {
                Membership member = (Membership)iter.next();
                cmAdmin.removeCourseSetMembership(member.getUserId(), courseSetEid);
            }

        }


    }

    public void preProcess(ProcessorState state) throws Exception {
        Map instructorMap = getInstructorMap(state);
        Map membersMap = getMembersMap(state);
        Map courseSetMembersMap = getCourseSetMembershipMap(state);

        instructorMap.clear();
        membersMap.clear();
        courseSetMembersMap.clear();

    }


}
