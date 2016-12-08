package com.gradebook2.export.institutional.advisor;

import com.gradebook2.export.institutional.advisor.util.Gradebook2ExportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionResult;
import org.sakaiproject.gradebook.gwt.sakai.InstitutionalAdvisor;
import org.sakaiproject.gradebook.gwt.sakai.SampleInstitutionalAdvisor;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.server.model.FinalGradeSubmissionResultImpl;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Mar 22, 2011
 * Time: 6:50:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class PacificInstitutionalAdvisor extends SampleInstitutionalAdvisor {

    private static final Log log = LogFactory.getLog(PacificInstitutionalAdvisor.class);
    private final String CONTENT_TYPE_TEXT_HTML_UTF8 = "text/html; charset=UTF-8";
    private final char PLUS = '+';
    private final char MINUS = '-';
    private final String FILE_EXTENSION = ".csv";
    private final String FILE_HEADER = "Sakai Eid,University Id,Banner Course Id,Site Id,Total Points,Percentage Points, Letter Grade";

    private ServerConfigurationService serverConfigurationService;
    private GradebookService gradeBookService;
    private String finalGradeSubmissionPath;
    

    private ToolManager toolManager = null;
    private SiteService siteService;

    private UserDirectoryService uds;


    private Gradebook2ExportUtil helper;


    //constants
    private static final String EXTERNAL_SITE_ID=  "site.cm.requested";
    private static final String PACIFICID="PACIFICID";


    public List<String> getExportCourseManagementSetEids(Group group) {
        if (null == group) {
            log.error("ERROR : Group is null");
            return null;
        }
        if (null == group.getProviderGroupId()) {
            log.warn("Group Provider Id is null");
            return null;
        }
        return Arrays.asList(group.getProviderGroupId().split("\\+"));
    }

    public String getExportCourseManagementId(String userEid, Group group, List<String> enrollmentSetEids) {

        if (null == group) {
            log.error("ERROR : Group is null");
            return null;
        }

        if (null == group.getContainingSite()) {
            log.warn("Containing site is null");
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(group.getContainingSite().getTitle());
        stringBuilder.append(" : ");
        stringBuilder.append(group.getTitle());

        return stringBuilder.toString();
    }

    public String getExportUserId(UserDereference dereference) {

        return dereference.getDisplayId();
    }

    public String getFinalGradeUserId(UserDereference dereference) {

        return dereference.getEid();
    }

    public String[] getLearnerRoleNames() {
        String[] roleKeys = {"Student", "Open Campus", "access"};
        return roleKeys;
    }


    public boolean isLearner(Member member) {
        String role = member.getRole() == null ? "" : member.getRole().getId();

        return (role.equalsIgnoreCase("Student")
                || role.equalsIgnoreCase("Open Campus")
                || role.equalsIgnoreCase("Access"))
                && member.isActive();
    }

    public boolean isExportCourseManagementIdByGroup() {
        return false;
    }

   
    public boolean isValidOverrideGrade(String grade, String learnerEid, String learnerDisplayId, Gradebook gradebook, Set<String> scaledGrades) {

        if (scaledGrades.contains(grade))
            return true;

        return false;
    }

    // @Override

    public FinalGradeSubmissionResult submitFinalGrade(List<Map<InstitutionalAdvisor.Column, String>> studentDataList, String gradebookUid) {
        Collection<Group> siteGroups;
        FinalGradeSubmissionResult finalGradeSubmissionResult = new FinalGradeSubmissionResultImpl();

        if (null == finalGradeSubmissionPath || "".equals(finalGradeSubmissionPath)) {
            log.error("ERROR: Null and or empty test failed for finalGradeSubmissionPath");
            // 500 Internal Server Error
            finalGradeSubmissionResult.setStatus(500);
			return finalGradeSubmissionResult;
        }

        String sakaiHomePath = serverConfigurationService.getSakaiHomePath();

        // Test if the path has a trailing file separator
        if (!finalGradeSubmissionPath.endsWith(File.separator)) {
            finalGradeSubmissionPath += File.separator;
        }

        String outputPath = finalGradeSubmissionPath;

        boolean relativePath = false;
        // if the path does not begin with a file separator, save relative to webroot
        if (!finalGradeSubmissionPath.startsWith((File.separator))) {
            ////WARNING: This may not work in Weblogic J2EE containers: getRealPath() is optional
            if (log.isDebugEnabled()) {
                log.debug("found relative path for gradefiles, setting relative to webroot: " + outputPath);
            }

            relativePath = true;

            log.info("found relative path for gradefiles, setting relative to webroot: "
                    + outputPath);
        }

        //response.setContentType(CONTENT_TYPE_TEXT_HTML_UTF8);

        // Getting the siteId
        String siteId = toolManager.getCurrentPlacement().getContext();
        Site site = null;
        String externalSiteId = null;
        try {
            //Return site
            site = siteService.getSite(siteId);

            //retrieve users from site
            siteGroups = site.getGroups();
            siteId = site.getId();

        } catch (Exception e2) {
            log.error("EXCEPTION: Wasn't able to get the siteId");
            // 500 Internal Server Error
            e2.printStackTrace();
            finalGradeSubmissionResult.setStatus(500);
			return finalGradeSubmissionResult;
        }


        // Test if path to final grade submission file exits
        File finalGradesPath = new File(sakaiHomePath + finalGradeSubmissionPath);
        if (!finalGradesPath.exists()) {
            try {
                finalGradesPath.mkdir();
                log.info("Folder wasn't found, so it being created" + sakaiHomePath + finalGradeSubmissionPath);
            }
            catch (SecurityException se) {
                log.error("EXCEPTION: Wasn't able to create final grade submission folder(s)");
                // 500 Internal Server Error

                se.printStackTrace();
                finalGradeSubmissionResult.setStatus(500);
                return finalGradeSubmissionResult;
            }
        }


        if (siteGroups != null && siteGroups.size() > 0 && serverConfigurationService.getBoolean("add.sections.submit.final.grades.gb2",true)) {

            // Using string buffer for thread safety
            StringBuffer finalGradeSubmissionFile = new StringBuffer();
            File finalGradesFile = null;

            for (Group group : siteGroups) {
                finalGradeSubmissionFile = new StringBuffer();
                finalGradeSubmissionFile.append(sakaiHomePath + finalGradeSubmissionPath);
                finalGradeSubmissionFile.append(group.getProviderGroupId() == null ? siteId : group.getProviderGroupId());
                finalGradeSubmissionFile.append(FILE_EXTENSION);
                finalGradesFile = new File(finalGradeSubmissionFile.toString());

                log.info("Writing final grades to " + finalGradesFile.getPath());

                if (finalGradesFile.exists()) {
                    finalGradesFile.delete();
                    log.info("If file exist delete so new File can be created with updated data");
                }

                try {
                    populateGradeExportFile(studentDataList, finalGradesFile, finalGradeSubmissionResult,  site.getId(), gradebookUid, group.getProviderGroupId());

                } catch (IOException e) {

                    log.error("EXCEPTION: Wasn't able to access the final grade submission file");
                    // 500 Internal Server Error

                    e.printStackTrace();
                    finalGradeSubmissionResult.setStatus(500);
                    return finalGradeSubmissionResult;
                }
            }

        } else {

            // Using string buffer for thread safety
            StringBuffer finalGradeSubmissionFile = new StringBuffer();
            File finalGradesFile = null;

            finalGradeSubmissionFile = new StringBuffer();
            finalGradeSubmissionFile.append(sakaiHomePath + finalGradeSubmissionPath);
            finalGradeSubmissionFile.append(site.getProviderGroupId() == null ? siteId : site.getProviderGroupId());
            finalGradeSubmissionFile.append("_");
            finalGradeSubmissionFile.append(helper.formattedDate());
            finalGradeSubmissionFile.append(FILE_EXTENSION);
            finalGradesFile = new File(finalGradeSubmissionFile.toString());

            log.info("Writing final grades to " + finalGradesFile.getPath());

            if (finalGradesFile.exists()) {
                finalGradesFile.delete();
                log.info("If file exist delete so new File can be created with updated data");
            }

            try {
                populateGradeExportFile(studentDataList,  finalGradesFile, finalGradeSubmissionResult,  site.getId(), gradebookUid, site.getProviderGroupId());

            } catch (IOException e) {

                log.error("EXCEPTION: Wasn't able to access the final grade submission file");
                // 500 Internal Server Error

                e.printStackTrace();
                finalGradeSubmissionResult.setStatus(500);
                return finalGradeSubmissionResult;
            }

        }
        return finalGradeSubmissionResult;
    }

    /** Adds data to files that are saved
     *
     * @param studentDataList
     * @param
     * @param finalGradesFile
     * @param 
     * @param site
     * @param gradebookUid
     * @throws IOException
     */
    private void populateGradeExportFile(List<Map<InstitutionalAdvisor.Column, String>> studentDataList, File finalGradesFile, FinalGradeSubmissionResult finalGradeSubmissionResult,  String site, String gradebookUid, String groupProvider) throws IOException {

        FileWriter writer = new FileWriter(finalGradesFile, true);
        writer.write(FILE_HEADER);
        writer.write("\n");

        // Using string buffer for thread safety
        StringBuffer exportData = null;
        //The new list with the total points earned added to the map with in the List
        List<Map<InstitutionalAdvisor.Column, String>> finalStudentList = retrieveStudentIds(studentDataList, gradebookUid);
        for (Map<InstitutionalAdvisor.Column, String> studentData : finalStudentList) {
            exportData = new StringBuffer();

            String eid = studentData.get(InstitutionalAdvisor.Column.FINAL_GRADE_USER_ID);
            exportData = new StringBuffer();
            exportData.append(eid);
            exportData.append(",");
            exportData.append(getUniversityIdFromEid(eid));
            exportData.append(",");
            exportData.append(groupProvider == null ? "":groupProvider);
            exportData.append(",");
            exportData.append(site);
            exportData.append(",");
            exportData.append(decimalPointFormat(studentData.get(InstitutionalAdvisor.Column.RAW_GRADE), 0)); //total points
            exportData.append(",");
            exportData.append(decimalPointFormat(studentData.get(InstitutionalAdvisor.Column.STUDENT_GRADE), null)); //Percentage
            exportData.append(",");
            exportData.append(studentData.get(InstitutionalAdvisor.Column.LETTER_GRADE));
            writer.write(exportData.toString());
            writer.write("\n");
        }
        //close and flush writer
        writer.flush();
        writer.close();

        // 201 Created
        finalGradeSubmissionResult.setStatus(201);
    }


    /**
     * ensures the decimal points of the percentage value
     * @param decimal
     * @return
     */
    private String decimalPointFormat(String decimal, Integer totalPoint){
        int decimalValue = 0;

        if ( null == decimal){
            return "";
        }

        if ( totalPoint == null) {
            decimalValue=2;
        }

        BigDecimal bigDecimal = new BigDecimal(decimal);
        bigDecimal = bigDecimal.setScale(decimalValue, BigDecimal.ROUND_DOWN);
        String decimalPoint = bigDecimal.toString();
        return decimalPoint;

    }




    /**
     * Extracts the student UUIDs
     *
     *
     * @param studentDataList
     * @return
     */
    private List extractIdsFromOriginalList(List<Map<InstitutionalAdvisor.Column, String>> studentDataList){
        List addStudentIds = new ArrayList();
        for (Map<InstitutionalAdvisor.Column, String> studentData : studentDataList) {
             addStudentIds.add(studentData.get(InstitutionalAdvisor.Column.STUDENT_UID));
        }
        return addStudentIds;
    }


    /**
     * Retrieves the totalPoints earned per student and adds it back to the map
     *
     * @param studentDataList
     * @param gradeBookUid
     * @return
     */
    private List retrieveStudentIds(List<Map<InstitutionalAdvisor.Column, String>> studentDataList, String gradeBookUid) {
        List studentsId = new ArrayList();
        List<CourseGradeRecord> data = gradeBookService.getGradeRecords(gradeBookUid, extractIdsFromOriginalList(studentDataList));
        for (Map<InstitutionalAdvisor.Column, String>  studentData : studentDataList) {
            for (CourseGradeRecord courseGradeRecords : data) {
                if (courseGradeRecords.getStudentId().equals(studentData.get(InstitutionalAdvisor.Column.STUDENT_UID))) {
                    studentData.put(InstitutionalAdvisor.Column.RAW_GRADE, String.valueOf(courseGradeRecords.getCalculatedPointsEarned().doubleValue()));
                    studentsId.add(studentData);
                    break;
                }
            }

        }
        return studentsId;
    }

    /**
     * Retrieves the university Id which comes from Pacific LDAP
     * @param eid
     * @return
     */
    private String getUniversityIdFromEid(String eid){
             try {
            User user = uds.getUserByEid(eid);
            if (user != null){
                 return (String) user.getProperties().get(PACIFICID);
            } 
        } catch (Exception e) {
            log.error("can't find user with eid of " + eid + " and property name of: " + PACIFICID, e);
        }
        return "";
    }


    /*
      * IOC setters:
      */
   public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }


    public void setFinalGradeSubmissionPath(String finalGradeSubmissionPath) {

        this.finalGradeSubmissionPath = finalGradeSubmissionPath;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public String getDisplaySectionId(String enrollmentSetEid) {
        return "DisplayId for eid: " + enrollmentSetEid;
    }

    public String getPrimarySectionEid(List<String> eids) {
        if (null == eids || eids.isEmpty()) {
            return "";
        } else {
            return eids.get(0);
        }
    }


    public void setUds(UserDirectoryService uds) {
        this.uds = uds;
    }

    public void setGradeBookService(GradebookService gradeBookService) {
        this.gradeBookService = gradeBookService;
    }


    public void setHelper(Gradebook2ExportUtil helper) {
        this.helper = helper;
    }
}
