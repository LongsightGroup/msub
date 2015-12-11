package com.rsmart.evalsys;


/**
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * 
 *
 */
public class CMEvalGroupProviderImpl implements EvalGroupsProvider
{
	private static final Log log = LogFactory.getLog(CMEvalGroupProviderImpl.class);

    protected CourseManagementService courseManagementService;
    protected UserDirectoryService uds;
    protected SqlService sqlService;
    private boolean includeIORInEvalTitle = true;
    private boolean includeTARoleInTitle = true;

	public void setCourseManagementService(CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}

	protected EvalExternalLogic externalLogic;

    private String permBeEvaluated = "provider.be.evaluated";
    private String permTakeEvaluation = "provider.take.evaluation";
    private String permAssignEvaluation = "provider.assign.eval";
    private String permRoleTA = "provider.role.ta";
    private boolean useInstructorUserPropertyInTitle = false;
    private String instructorUserPropertyName;
    private String cmTeachingAssistantRole = "TA";

    private String groupDelimiter = "@@@";

	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}



    /**
     * Initialize this provider
     */
    public void init()
    {
    	log.info("init");

    }

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countEvalGroupsForUser(java.lang.String, java.lang.String)
	 */
	public int countEvalGroupsForUser(String userId, String permission)
	{
        return getEvalGroupsForUser(userId, permission).size();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#countUserIdsForEvalGroups(java.lang.String[], java.lang.String)
	 */
	public int countUserIdsForEvalGroups(String[] groupIds, String permission) {

		return getUserIdsForEvalGroups(groupIds, permission).size();
	}

	public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {

        User user = null;
        try {
            user = uds.getUser(userId);
        } catch (UserNotDefinedException e) {
            e.printStackTrace(); 
        }

        if (permAssignEvaluation.equals(permission)) {
            Set<Section> sections = getCurrentSectionsManagedBy(user.getEid());
            if (sections != null) {
                return createEvalGroupsFromSections(sections);
            }
        }
        if (permTakeEvaluation.equals(permission)) {
            Set<Section> sections = courseManagementService.findEnrolledSections(user.getEid());
            if (sections != null) {
                return createEvalGroupsFromSections(sections);
            }
        }
        if (permBeEvaluated.equals(permission)) {
            Set<Section> sections = courseManagementService.findInstructingSections(user.getEid());
            if (sections == null) {
                sections = new HashSet<Section>();
            }
            Map<String, String> sectionRoles = courseManagementService.findSectionRoles(user.getEid());

            List<EvalGroup> evalGroups = new ArrayList<EvalGroup>();

            for (Section section: sections) {
                addEvalGroup(user, evalGroups, section, false);
            }


            if (sectionRoles != null) {
                // add in any section for which user is a TA
                for (String sectionEid : sectionRoles.keySet()) {
                    if (cmTeachingAssistantRole.equals(sectionRoles.get(sectionEid))) {
                        Section section = courseManagementService.getSection(sectionEid);
                        if (section != null) {
                            addEvalGroup(user, evalGroups, section, true);
                        }
                    }
                }
            }



            return evalGroups;

        }
        return new ArrayList<EvalGroup>();
	}

    private void addEvalGroup(User user, List<EvalGroup> evalGroups, Section section, boolean isTA) {
        try {
            EvalGroup evalGroup = new EvalGroup(packGroupId(section.getEid(), user.getEid()), getEvaluationTitle(section, user.getEid(), isTA), EvalConstants.GROUP_TYPE_PROVIDED);
            evalGroups.add(evalGroup);
        } catch (UserNotDefinedException e) {
            log.error("can't find ior with eid:" + user.getEid());
        }
    }

    private List<EvalGroup> createEvalGroupsFromSections(Set<Section> sections) {
        List<EvalGroup> evalGroups = new ArrayList<EvalGroup>();
        for (Section section: sections) {
            for (String instructor : section.getEnrollmentSet().getOfficialInstructors()) {
                try {
                    EvalGroup evalGroup = new EvalGroup(packGroupId(section.getEid(), instructor), getEvaluationTitle(section, instructor, false), EvalConstants.GROUP_TYPE_PROVIDED);
                    evalGroups.add(evalGroup);
                } catch (UserNotDefinedException e) {
                    log.error("can't find ior with eid:" + instructor);
                }
            }
            // add in TA's
            Set<Membership> sectionMemberships = courseManagementService.getSectionMemberships(section.getEid());
            for (Membership membership : sectionMemberships) {
                if (membership.getRole().equals(cmTeachingAssistantRole)) {
                    try {
                        EvalGroup evalGroup = new EvalGroup(packGroupId(section.getEid(), membership.getUserId()),
                                getEvaluationTitle(section, membership.getUserId(), true),
                                EvalConstants.GROUP_TYPE_PROVIDED);
                        evalGroups.add(evalGroup);
                    } catch (UserNotDefinedException e) {
                        log.error("can't find ta with eid:" + membership.getUserId());
                    }
                }
            }
        }
        return evalGroups;
    }

    private String packGroupId(String eid, String instructor) {
        return eid + groupDelimiter + instructor;
    }

    private String getEvaluationTitle(Section section, String iorEid, boolean isTA) throws UserNotDefinedException {
        if (includeIORInEvalTitle) {

            if (org.apache.commons.lang.StringUtils.isNotEmpty(iorEid)) {

                User instructorOfRecord = uds.getUserByEid(iorEid);

                String iorId = instructorOfRecord.getEid();

                if (useInstructorUserPropertyInTitle &&
                        org.apache.commons.lang.StringUtils.isNotEmpty((String) instructorOfRecord.getProperties().get(instructorUserPropertyName))) {
                    iorId = (String) instructorOfRecord.getProperties().get(instructorUserPropertyName);
                }

                String title = section.getTitle() + " " + instructorOfRecord.getLastName() + ", " +
                                        instructorOfRecord.getFirstName() + " (" + iorId + ")";

                if (includeTARoleInTitle && isTA) {
                    title += " (" + cmTeachingAssistantRole + ")";
                }

                return title;

            }
        }
        return section.getTitle();

    }

    protected Set<Section> getCurrentSectionsManagedBy(String userEid) {
        Connection con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        Set<Section> sections = new HashSet<Section>();
        try {
            con = sqlService.borrowConnection();
            st = con.prepareStatement("SELECT \n" +
                    "    section.ENTERPRISE_ID\n" +
                    "FROM \n" +
                    "    cm_member_container_t as section, \n" +
                    "    cm_member_container_t as courseset, \n" +
                    "    cm_member_container_t as courseoffering, \n" +
                    "    cm_membership_t       as membership,\n" +
                    "    cm_academic_session_t as term,\n" +
                    "    cm_course_set_offering_assoc_t as cs_co\n" +
                    "WHERE \n" +
                    "    section.CLASS_DISCR            = 'org.sakaiproject.coursemanagement.impl.SectionCmImpl' \n" +
                    "    and courseset.CLASS_DISCR      = 'org.sakaiproject.coursemanagement.impl.CourseSetCmImpl' \n" +
                    "    and courseoffering.CLASS_DISCR = 'org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl' \n" +
                    "    and courseoffering.ACADEMIC_SESSION = term.ACADEMIC_SESSION_ID\n" +
                    "    and membership.user_id = ?\n" +
                    "    and term.START_DATE <= ?\n" +
                    "    and term.END_DATE >= ?\n" +
                    "    and courseset.member_container_id = membership.MEMBER_CONTAINER_ID \n" +
                    "    and section.COURSE_OFFERING = courseoffering.member_container_id \n" +
                    "    and courseoffering.MEMBER_CONTAINER_ID = cs_co.COURSE_OFFERING\n" +
                    "    and courseset.MEMBER_CONTAINER_ID = cs_co.COURSE_SET\n" +
                    ";");

            st.setString(1, userEid);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date now = new java.sql.Date(utilDate.getTime());
            st.setDate(2, now);
            st.setDate(3, now);

            rs = st.executeQuery();
            while (rs.next()) {
                String sectionEid = rs.getString(1);
                Section section = courseManagementService.getSection(sectionEid);
                sections.add(section);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }

        }
        return sections;
    }

    /* (non-Javadoc)
      * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getEvalGroupsForUser(java.lang.String)
      */
	public Map<String, List<EvalGroup>> getEvalGroupsForUser(String userId) {
		Map<String, List<EvalGroup>> groups = new HashMap<String, List<EvalGroup>>();
		for(String permission : new String[]{permAssignEvaluation, permBeEvaluated, permTakeEvaluation, permRoleTA}) {
			List<EvalGroup> list = this.getEvalGroupsForUser(userId, permission);
			if(list != null && ! list.isEmpty()) {
				groups.put(permission, list);
			}
		}
		return groups;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getGroupByGroupId(java.lang.String)
	 */
	public EvalGroup getGroupByGroupId(String groupId) {
        String sectionEid = unpackSectionFrom(groupId);
        if (sectionEid == null) return null;
        try {
            if (!courseManagementService.isSectionDefined(sectionEid)){
                return null;
            }
        } catch (UnsupportedOperationException e){
            log.warn("isSectionDefined() is not supported for " + courseManagementService.getClass().getName());
            return null;
        }

        Section section = courseManagementService.getSection(sectionEid);
        String instructorEid = unpackInstructorEidFrom(groupId);
        if (instructorEid == null) {
            return null;
        }


        boolean isTA = true;
        for (String instructor : section.getEnrollmentSet().getOfficialInstructors()) {
            if (instructor.equals(instructorEid)) {
                isTA = false;
                break;
            }
        }

        try {
            return new EvalGroup(groupId, getEvaluationTitle(section, instructorEid, isTA), EvalConstants.GROUP_TYPE_PROVIDED);
        } catch (UserNotDefinedException e) {
            log.error("can't find ior with eid:" + instructorEid);
        }
        return null;
	}

    private String unpackSectionFrom(String groupId) {
        String[] bits = groupId.split(groupDelimiter);
        if (bits.length != 2) {
            log.info("the groupId of: " + groupId + " is not a valid groupId for CMEvalGroupProviderImpl, it should contain the string:" + groupDelimiter);
            return null;
        }
        return bits[0];
    }

      private String unpackInstructorEidFrom(String groupId) {
        String[] bits = groupId.split(groupDelimiter);
        if (bits.length != 2) {
            log.info("the groupId of: " + groupId + " is not a valid groupId for CMEvalGroupProviderImpl, so can't parse the instructorEid, it should contain the string:" + groupDelimiter);
            return null;
        }
        return bits[1];
    }

    /* (non-Javadoc)
      * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#getUserIdsForEvalGroups(java.lang.String[], java.lang.String)
      */
	public Set<String> getUserIdsForEvalGroups(String[] groupIds, String permission) {
        Set userIds = new HashSet();

        for (int i = 0; i < groupIds.length; i++) {
            String sectionEid = unpackSectionFrom(groupIds[i]);
            if (sectionEid != null) {
                try {

                    if (!courseManagementService.isSectionDefined(sectionEid)) {
                        continue;
                    }
                } catch (UnsupportedOperationException e) {
                    log.warn("isSectionDefined() is not supported for " + courseManagementService.getClass().getName());
                    return userIds;
                }

                Section section = courseManagementService.getSection(sectionEid);
                EnrollmentSet enrollmentSet = section.getEnrollmentSet();

                if (permAssignEvaluation.equals(permission)) {
                    if (courseManagementService.isCourseOfferingDefined(section.getCourseOfferingEid())) {
                        CourseOffering offering = courseManagementService.getCourseOffering(section.getCourseOfferingEid());
                        Set<String> courseSetEids = offering.getCourseSetEids();
                        if (courseSetEids != null) {
                            for (String courseSetEid : courseSetEids) {
                                Set existingMembers = courseManagementService.getCourseSetMemberships(courseSetEid);
                                if (existingMembers != null) {
                                    for (Iterator iter = existingMembers.iterator(); iter.hasNext(); ) {
                                        Membership member = (Membership) iter.next();
                                        try {
                                            String userId = uds.getUserId(member.getUserId());
                                            userIds.add(userId);
                                        } catch (UserNotDefinedException e) {
                                            log.error(" can't find user with userId=" + member.getUserId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (permTakeEvaluation.equals(permission)) {
                    for (Enrollment enrollment : courseManagementService.getEnrollments(enrollmentSet.getEid())) {
                        if (enrollment.isDropped()) {
                            log.debug("skipping dropped enrollment with enrollment set id=" + enrollmentSet.getEid() + " and userId=" + enrollment.getUserId());
                            continue;
                        }

                        try {
                            String userId = uds.getUserId(enrollment.getUserId());
                            userIds.add(userId);
                        } catch (UserNotDefinedException e) {
                            log.error(" can't find user with userId=" + enrollment.getUserId());
                        }

                    }
                }
                if (permBeEvaluated.equals(permission)) {
                    for (String instructor : enrollmentSet.getOfficialInstructors()) {
                        try {
                            String userId = uds.getUserId(instructor);
                            userIds.add(userId);
                        } catch (UserNotDefinedException e) {
                            log.error(" can't find user with eid=" + instructor);
                        }

                    }
                    // add in TA's
                    Set<Membership> coMemberships = courseManagementService.getSectionMemberships(section.getEid());
                    if (coMemberships != null) {
                        for (Membership membership : coMemberships) {
                            if (membership.getRole().equals(cmTeachingAssistantRole)) {
                                try {
                                    String userId = uds.getUserId(membership.getUserId());
                                    userIds.add(userId);
                                } catch (UserNotDefinedException e) {
                                    log.error(" can't find user with eid=" + membership.getUserId());
                                }
                            }
                        }
                    }
                }
            }
        }
        return userIds;

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.providers.EvalGroupsProvider#isUserAllowedInGroup(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean isUserAllowedInGroup(String userId, String permission,
			String groupId) {
		return false;
	}

    public void setPermBeEvaluated(String permBeEvaluated) {
        this.permBeEvaluated = permBeEvaluated;
    }

    public void setPermTakeEvaluation(String permTakeEvaluation) {
        this.permTakeEvaluation = permTakeEvaluation;
    }

    public void setPermAssignEvaluation(String permAssignEvaluation) {
        this.permAssignEvaluation = permAssignEvaluation;
    }

    public void setUds(UserDirectoryService uds) {
        this.uds = uds;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setIncludeIORInEvalTitle(boolean includeIORInEvalTitle) {
        this.includeIORInEvalTitle = includeIORInEvalTitle;
    }

    public void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }

    public void setUseInstructorUserPropertyInTitle(boolean useInstructorUserPropertyInTitle) {
        this.useInstructorUserPropertyInTitle = useInstructorUserPropertyInTitle;
    }

    public void setInstructorUserPropertyName(String instructorUserPropertyName) {
        this.instructorUserPropertyName = instructorUserPropertyName;
    }

    public void setPermRoleTA(String permRoleTA) {
        this.permRoleTA = permRoleTA;
    }

    public void setCmTeachingAssistantRole(String cmTeachingAssistantRole) {
        this.cmTeachingAssistantRole = cmTeachingAssistantRole;
    }

    public boolean isIncludeTARoleInTitle() {
        return includeTARoleInTitle;
    }

    public void setIncludeTARoleInTitle(boolean includeTARoleInTitle) {
        this.includeTARoleInTitle = includeTARoleInTitle;
    }
}
