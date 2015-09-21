package com.rsmart.customer.integration.job;

import com.rsmart.evalsys.HierarchyUtil;
import com.rsmart.sakai.common.job.AbstractAdminJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.CourseSet;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Membership;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/7/11
 * Time: 11:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class CMSHierarchyLoaderJob extends AbstractAdminJob {
	/** Logger */
	private static Log logger = LogFactory.getLog(CMSHierarchyLoaderJob.class);
    protected CourseManagementService cmService;
    protected HierarchyService hierarchyService;
    private ExternalHierarchyLogic externalHierarchyLogic;
    private HierarchyUtil hierarchyUtil;
    private String departmentAdminPerm = "CourseAdmin";

    private String termPropName = "com.rsmart.customer.integration.processor.cle.cm.hierachyloader.term";


    public void init() {
        
    }

    public void executeInternal(JobExecutionContext jec) throws JobExecutionException {
        JobDataMap
            jdm = jec.getMergedJobDataMap();
        String term = (String) jdm.get(termPropName);

        Map nodes = hierarchyUtil.reloadHierarchyNodes();
        loadCourseSetsIntoHierarchy(term, hierarchyUtil.getRootNode(), nodes, cmService.getCourseSets());
    }

    private void loadCourseSetsIntoHierarchy(String term, HierarchyNode rootNode, Map hierarchyNodes, Set<CourseSet> courseSets) {
        for (CourseSet courseSet : courseSets) {
            // create hierarchy nodes
            HierarchyNode courseSetNode = getNodeByEid(courseSet.getEid(), hierarchyNodes);
            if (courseSetNode == null) {
                courseSetNode = hierarchyService.addNode(hierarchyUtil.getRootNodeName(), rootNode.id );
                courseSetNode = hierarchyService.saveNodeMetaData(courseSetNode.id, courseSet.getEid(), courseSet.getDescription(), null);
                hierarchyNodes.put(courseSet.getTitle(), courseSetNode.id);
            }

            Set evalGroups = externalHierarchyLogic.getEvalGroupsForNode(courseSetNode.id);

            //create hierarchy node with parent the courseSet node
            for (CourseOffering courseOffering: cmService.findCourseOfferings(courseSet.getEid(), term)){
/*
                HierarchyNode courseOfferingNode = getNodeByEid(courseOffering.getEid(), hierarchyNodes);
                if (courseOfferingNode == null) {
                    courseOfferingNode = hierarchyService.addNode(hierarchyUtil.getRootNodeName(), courseSetNode.id);
                    courseOfferingNode = hierarchyService.saveNodeMetaData(courseOfferingNode.id, courseOffering.getEid(), courseOffering.getDescription(), null);
                    hierarchyNodes.put(courseOffering.getEid(), courseOfferingNode);
                } else {
                    //courseOfferingNode = hierarchyService.addChildRelation(courseSetNode.id, courseOfferingNode.id);
                }
*/
                // create membership group for course offering group, mapping departmentAdminPerm role to permAssignEvaluation

                //hierarchyService.assignUserNodePerm("admin", courseSetNode.id, hierarchyUtil.permAssignEvaluation, true);

/*
                Set<Membership> memberships = cmService.getCourseOfferingMemberships(courseOffering.getEid());
                if (memberships != null && memberships.size() > 0) {

                    if (!evalGroups.contains(evalGroups)) {
                            evalGroups.add(courseOffering.getEid());
                            externalHierarchyLogic.setEvalGroupsForNode(courseOfferingNode.id, evalGroups);
                    }



                    for (Membership membership : memberships){
                        if (membership.getRole().equals(departmentAdminPerm)) {
                            Set<String> deparmentNodePerms = hierarchyService.getPermsForUserNodes(membership.getUserId(), new String[]{courseOfferingNode.id});

                            if (!deparmentNodePerms.contains(hierarchyUtil.permAssignEvaluation)){
                                hierarchyService.assignUserNodePerm(membership.getUserId(), courseOfferingNode.id, hierarchyUtil.permAssignEvaluation, true);
                            }
                        }
                    }

                }
*/
                //create evalgroups for each section, by loading section memberships, instructors, and enrollments sets
                for (Section section: cmService.getSections(courseOffering.getEid())) {

                    if (!evalGroups.contains(evalGroups)) {
                        evalGroups.add(section.getEid());
                        externalHierarchyLogic.setEvalGroupsForNode(courseSetNode.id, evalGroups);
                    }

                    EnrollmentSet enrollmentSet = section.getEnrollmentSet();

                    for (String instructor: enrollmentSet.getOfficialInstructors()){
                        Set<String> instructorNodePerms = hierarchyService.getPermsForUserNodes(instructor, new String[]{courseSetNode.id});

                        if (!instructorNodePerms.contains(hierarchyUtil.permBeEvaluated)){
                            hierarchyService.assignUserNodePerm(instructor, courseSetNode.id, hierarchyUtil.permBeEvaluated, true);
                        }
                    }

                    for (Enrollment enrollment: cmService.getEnrollments(enrollmentSet.getEid())){
                        Set<String> studentNodePerms = hierarchyService.getPermsForUserNodes(enrollment.getUserId(), new String[]{courseSetNode.id});

                        if (!studentNodePerms.contains(hierarchyUtil.permTakeEvaluation)){
                            hierarchyService.assignUserNodePerm(enrollment.getUserId(), courseSetNode.id, hierarchyUtil.permTakeEvaluation, true);
                        }
                    }

                    for (Membership membership: cmService.getSectionMemberships(section.getEid())) {
                        if (membership.getRole().equalsIgnoreCase("TA") || membership.getRole().equalsIgnoreCase("GIS")) {
                            hierarchyService.assignUserNodePerm(membership.getUserId(), courseSetNode.id, hierarchyUtil.permAssistantRole, true);
                        }
                    }


                }
            }                     
            Set childCourseSets = cmService.getChildCourseSets(courseSet.getEid());
            if (childCourseSets !=null && childCourseSets.size() > 0) {
                loadCourseSetsIntoHierarchy(term, courseSetNode, hierarchyNodes, childCourseSets);
            }
        }
    }

    protected HierarchyNode getNodeByEid(String eid, Map nodes) {
        return (HierarchyNode)nodes.get(eid);
    }

    protected Map loadHierarchyNodes(HierarchyNode rootNode) {
        HashMap map = new HashMap();
        Set<HierarchyNode> nodes = hierarchyService.getChildNodes(rootNode.id, false);
        for (HierarchyNode node : nodes) {
            map.put(node.title, node);
        }
        return map;
    }



    public void setExternalHierarchyLogic(ExternalHierarchyLogic externalHierarchyLogic) {
        this.externalHierarchyLogic = externalHierarchyLogic;
    }

    public void setCmService(CourseManagementService cmService) {
        this.cmService = cmService;
    }

    public void setHierarchyService(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    public void setHierarchyUtil(HierarchyUtil hierarchyUtil) {
        this.hierarchyUtil = hierarchyUtil;
    }

    public void setDepartmentAdminPerm(String departmentAdminPerm) {
        this.departmentAdminPerm = departmentAdminPerm;
    }

    public void setTermPropName(String termPropName) {
        this.termPropName = termPropName;
    }
}
