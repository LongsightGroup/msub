package com.gradebook2.export.institutional.advisor;

import com.gradebook2.export.institutional.advisor.util.GradeBookExportSiteHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionResult;
import org.sakaiproject.gradebook.gwt.sakai.SampleInstitutionalAdvisor;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.server.model.FinalGradeSubmissionResultImpl;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.gradebook.Gradebook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 25, 2011
 * Time: 10:42:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class SGUInstitutionalAdvisor extends SampleInstitutionalAdvisor {

    private static final Log log = LogFactory.getLog(SGUInstitutionalAdvisor.class);

    private final String CONTENT_TYPE_TEXT_HTML_UTF8 = "text/html; charset=UTF-8";
    private final char PLUS = '+';
    private final char MINUS = '-';
    private final String FILE_EXTENSION = ".csv";
    private final String FILE_HEADER = "Term-CRN,User EID,Student Banner ID,PercentGrade,Letter_Grade";

    private ServerConfigurationService serverConfigurationService;
    private NumberFormat nf = NumberFormat.getInstance();

    private String finalGradeSubmissionPath;

    private GradeBookExportSiteHelper siteHelper;
    private ToolManager toolManager = null;
    private SiteService siteService;
    private UserDirectoryService uds;


    //constant
    private static final String EXTERNAL_SITE_ID=  "externalSiteId";
    private static final String EXTERNAL_SECTION_ID= "externalSectionId";
    private String studentBannerIdPropertyName = "studentBannerID";


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

        return dereference.getEid();
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

    public FinalGradeSubmissionResult submitFinalGrade(List<Map<Column, String>> studentDataList, String gradebookUid) {

        FinalGradeSubmissionResult finalGradeSubmissionResult = new FinalGradeSubmissionResultImpl();

        Collection<Group> siteGroups;
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



        // Getting the siteId
        String siteId = toolManager.getCurrentPlacement().getContext();
        Site site = null;
        String externalSiteId = null;
        try {
            //Return site
            site = siteService.getSite(siteId);

            if (site != null) {
                externalSiteId = (String)site.getProperties().get(EXTERNAL_SITE_ID);
            }

            if (externalSiteId == null) {
                log.error("can't find property with name: " + EXTERNAL_SITE_ID + " in site: " + siteId + " substituting siteId instead");
                externalSiteId = siteId;
            }

            //retrieve users from site
            siteGroups = site.getGroups();
            siteId = site.getId();

        } catch (Exception e2) {
            log.error("EXCEPTION: Wasn't able to get the siteId");
            // 500 Internal Server Error
            finalGradeSubmissionResult.setStatus(500);
            e2.printStackTrace();
            return finalGradeSubmissionResult;
        }

        try {

			siteId = URLEncoder.encode(siteId, "utf-8");

		} catch (UnsupportedEncodingException e) {

			log.error("EXCEPTION: Wasn't able to url encode the siteId", e);
			// 500 Internal Server Error
			finalGradeSubmissionResult.setStatus(500);
			return finalGradeSubmissionResult;
		}


        // Test if path to final grade submission file exits
        File finalGradesPath = new File(sakaiHomePath + finalGradeSubmissionPath);
        if (!finalGradesPath.exists()) {
            try {
                finalGradesPath.mkdir();
                log.info("Folder wasn't found, so it being created" + sakaiHomePath + finalGradeSubmissionPath);
//                if (!finalGradesPath.mkdirs()) {
//                    log.error("Wasn't able to create final grade submission folder(s)");
//                    // 500 Internal Server Error
//                    response.setStatus(500);
//                    return;
//                }
            }
            catch (SecurityException se) {
                log.error("EXCEPTION: Wasn't able to create final grade submission folder(s)");
                // 500 Internal Server Error
                finalGradeSubmissionResult.setStatus(500);
                se.printStackTrace();
                return finalGradeSubmissionResult;
            }
        }

        // Using string buffer for thread safety
        StringBuffer finalGradeSubmissionFile = new StringBuffer();
        File finalGradesFile = null;

        //check to see if there are any sections/"Groups" in the course
        if (null != siteGroups && siteGroups.size() > 0 && externalSectionIdsExist(siteGroups, siteId)) {

            for (Group group : siteGroups) {
                String siteGroupExternalId = (String)group.getProperties().get(EXTERNAL_SECTION_ID);
                Set sectionMembers = group.getUsers();
                finalGradeSubmissionFile = new StringBuffer();
                finalGradeSubmissionFile.append(sakaiHomePath + finalGradeSubmissionPath);
                finalGradeSubmissionFile.append(siteGroupExternalId);
                 /* This commented out and not deleted in case the naming convention changes*/
               //finalGradeSubmissionFile.append("_");
               // finalGradeSubmissionFile.append(group.getTitle());
               //finalGradeSubmissionFile.append("_");
               // finalGradeSubmissionFile.append(Gradebook2ExportUtil.formattedDate());
                finalGradeSubmissionFile.append(FILE_EXTENSION);
                finalGradesFile = new File(finalGradeSubmissionFile.toString());

                if ( finalGradesFile.exists()){
                    finalGradesFile.delete();
                    log.info("If file exist delete so new File can have updated data");
                }



                log.info("Writing final grades to " + finalGradesFile.getPath());


                try {
                    //gives you the users/students in this group/section
                    List<Map<Column, String>> groupUserList = siteHelper.getUserGroupData(studentDataList, sectionMembers);

                    populateGradeExportFile(groupUserList, finalGradeSubmissionResult, finalGradesFile, siteGroupExternalId);


                } catch (IOException e) {

                    log.error("EXCEPTION: Wasn't able to access the final grade submission file");
                    // 500 Internal Server Error
                    finalGradeSubmissionResult.setStatus(500);
                    e.printStackTrace();
                    return finalGradeSubmissionResult;
                }

            }


        } else {

            finalGradeSubmissionFile = new StringBuffer();
            finalGradeSubmissionFile.append(sakaiHomePath + finalGradeSubmissionPath);
            finalGradeSubmissionFile.append(externalSiteId);
           /* This commented out and not deleted in case the naming convention changes*/
           //finalGradeSubmissionFile.append("_");
           //finalGradeSubmissionFile.append(site.getTitle());
           //finalGradeSubmissionFile.append("_");
           //finalGradeSubmissionFile.append(Gradebook2ExportUtil.formattedDate());
            finalGradeSubmissionFile.append(FILE_EXTENSION);
            finalGradesFile = new File(finalGradeSubmissionFile.toString());


            log.info("Writing final grades to " + finalGradesFile.getPath());

            if (finalGradesFile.exists()) {
                finalGradesFile.delete();
                log.info("If file exist delete so new File can be created with updated data");
            }


            try {
                populateGradeExportFile(studentDataList, finalGradeSubmissionResult, finalGradesFile, externalSiteId);

            } catch (IOException e) {

                log.error("EXCEPTION: Wasn't able to access the final grade submission file");
                // 500 Internal Server Error
                finalGradeSubmissionResult.setStatus(500);
                e.printStackTrace();
                return finalGradeSubmissionResult;
            }

        }

        return finalGradeSubmissionResult;
    }

    private boolean externalSectionIdsExist(Collection<Group> siteGroups, String siteId) {
        for (Group group : siteGroups) {
            String siteGroupExternalId = (String)group.getProperties().get(EXTERNAL_SECTION_ID);
            if (siteGroupExternalId == null || siteGroupExternalId.length() == 0) {
                log.info("can't find " + EXTERNAL_SECTION_ID + " property in group " + group.getId() + " dumping gradebook data at site level");
                return false;
            }
        }
        log.info("found " + EXTERNAL_SECTION_ID + " properties in groups for site " + siteId + " dumping gradebook data at section level");
        return true;
    }

    private void populateGradeExportFile(List<Map<Column, String>> studentDataList, FinalGradeSubmissionResult finalGradeSubmissionResult, File finalGradesFile, String siteGroupExternalId) throws IOException {

        FileWriter writer = new FileWriter(finalGradesFile, true);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        //writer.write(FILE_HEADER);
        //writer.write("\n");

        // Using string buffer for thread safety
        StringBuffer exportData = null;


        for (Map<Column, String> studentData : studentDataList) {
            exportData = new StringBuffer();

            String eid = studentData.get(Column.FINAL_GRADE_USER_ID);

            exportData = new StringBuffer();
            exportData.append(siteGroupExternalId);
            exportData.append(",");
            exportData.append(eid);
            exportData.append(",");
            exportData.append(getStudentBannerIdFromEid(eid));
            exportData.append(",");
            String data = studentData.get(Column.RAW_GRADE);
            if(data != null && data.length() > 0)
            {
            	try
            	{
            		exportData.append(nf.format(Double.valueOf(data))); //Percentage
            	}
            	catch (Exception e)
            	{
            		log.debug("Student Grade is not a valid number: " + data, e);
            		exportData.append("");
            	}
            }
            else
            {
            	exportData.append(""); //Percentage
            }
            exportData.append(",");
            exportData.append(studentData.get(Column.LETTER_GRADE));
            writer.write(exportData.toString());
            writer.write("\n");


        }
        //close and flush writer
        writer.flush();
        writer.close();


        // 201 Created
        finalGradeSubmissionResult.setStatus(201);


    }

    private String getStudentBannerIdFromEid(String eid)  {
        try {
            User user = uds.getUserByEid(eid);
            return (String) user.getProperties().get(studentBannerIdPropertyName);
        } catch (Exception e) {
            log.error("can't find user with eid of " + eid + " and property name of: " + studentBannerIdPropertyName, e);
        }
        return null;
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



    public void setSiteHelper(GradeBookExportSiteHelper siteHelper) {
        this.siteHelper = siteHelper;
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

    public void setStudentBannerIdPropertyName(String studentBannerIdPropertyName) {
        this.studentBannerIdPropertyName = studentBannerIdPropertyName;
    }

    public void setUds(UserDirectoryService uds) {
        this.uds = uds;
    }
}
