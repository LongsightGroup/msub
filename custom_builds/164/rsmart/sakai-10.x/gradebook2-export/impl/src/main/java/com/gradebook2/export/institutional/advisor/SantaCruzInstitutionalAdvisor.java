package com.gradebook2.export.institutional.advisor;

import com.gradebook2.export.institutional.advisor.util.Gradebook2ExportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.gradebook.gwt.client.model.FinalGradeSubmissionResult;
import org.sakaiproject.gradebook.gwt.sakai.SampleInstitutionalAdvisor;
import org.sakaiproject.gradebook.gwt.sakai.model.UserDereference;
import org.sakaiproject.gradebook.gwt.server.model.FinalGradeSubmissionResultImpl;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;
import org.sakaiproject.tool.gradebook.Gradebook;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 25, 2011
 * Time: 10:42:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class SantaCruzInstitutionalAdvisor extends SampleInstitutionalAdvisor {

    private static final Log log = LogFactory.getLog(SantaCruzInstitutionalAdvisor.class);

    private final String CONTENT_TYPE_TEXT_HTML_UTF8 = "text/html; charset=UTF-8";
    private final char PLUS = '+';
    private final char MINUS = '-';
    private final String FILE_EXTENSION = ".csv";
    private final String FILE_HEADER = "ID,Name,Grade";

    private ServerConfigurationService serverConfigurationService;

    private String finalGradeSubmissionPath;

    //private GradeBookExportSiteHelper siteHelper;
    private ToolManager toolManager = null;
    private SiteService siteService;
    private UserDirectoryService uds;
    private ContentHostingService chs;
    private SessionManager sessionManager;

    //constant
    private static final String EMP_ID ="emplId";

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
          //  response.setStatus(500);
            e2.printStackTrace();
            log.error("EXCEPTION: Wasn't able to get the siteId", e2);
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
            }
            catch (SecurityException se) {
                log.error("EXCEPTION: Wasn't able to create final grade submission folder(s)");
                // 500 Internal Server Error
                //response.setStatus(500);
                se.printStackTrace();
                log.error("EXCEPTION: Wasn't able to get the siteId", se);
			// 500 Internal Server Error
			finalGradeSubmissionResult.setStatus(500);
			return finalGradeSubmissionResult;
            }
        }

        // Using string buffer for thread safety
        StringBuffer finalGradeSubmissionFile = new StringBuffer();
        File finalGradesFile = null;

        //check to see if there are any sections/"Groups" in the course
        if (null != site) {
            finalGradeSubmissionFile = new StringBuffer();
            finalGradeSubmissionFile.append(sakaiHomePath + finalGradeSubmissionPath);
            finalGradeSubmissionFile.append(site.getTitle());
            finalGradeSubmissionFile.append("_");
            finalGradeSubmissionFile.append(Gradebook2ExportUtil.formattedDate());
            finalGradeSubmissionFile.append(FILE_EXTENSION);
            finalGradesFile = new File(finalGradeSubmissionFile.toString());


            log.info("Writing final grades to " + finalGradesFile.getPath());

            if (finalGradesFile.exists()) {
                finalGradesFile.delete();
                log.info("If file exist delete so new File can be created with updated data");
            }


            try {
                populateGradeExportFile(studentDataList,  finalGradesFile, finalGradeSubmissionResult, site);

            } catch (IOException e) {

                log.error("EXCEPTION: Wasn't able to access the final grade submission file");
                // 500 Internal Server Error
             //   response.setStatus(500);
                e.printStackTrace();
                log.error("EXCEPTION: Wasn't able to get the siteId", e);
			// 500 Internal Server Error
			finalGradeSubmissionResult.setStatus(500);
			return finalGradeSubmissionResult;
            }

        }

        return finalGradeSubmissionResult;
    }


    /**
     * Cleans out the gradebookExport folder
     *
     * @param finalGradeFile
     */
    private void cleanDirectory( File finalGradeFile){

        finalGradeFile.delete();

    }

    /**
     * Creates the csv file data
     *
     *
     * @param studentDataList
     * @param
     * @param finalGradesFile
     * @param site
     * @throws java.io.IOException
     */
    private void populateGradeExportFile(List<Map<Column, String>> studentDataList, File finalGradesFile, FinalGradeSubmissionResult finalGradeSubmissionResult, Site site) throws IOException {

        FileWriter writer = new FileWriter(finalGradesFile, true);

        //Allows the ability to change without changing code
        String fileHeaders = serverConfigurationService.getString("gb2.grade.export.header.files");
        if (fileHeaders != null && fileHeaders.length() > 0){
            writer.write(serverConfigurationService.getString("gb2.grade.export.header.files").trim());
        } else {
            writer.write(FILE_HEADER);
        }

        writer.write("\n");

        // Using string buffer for thread safety
        StringBuffer exportData = null;

        for (Map<Column, String> studentData : studentDataList) {
            exportData = new StringBuffer();
            String eid = studentData.get(Column.FINAL_GRADE_USER_ID);
            String useIdEmp = studentData.get(Column.STUDENT_UID);
            exportData = new StringBuffer();
            exportData.append(getUserEmpId(useIdEmp));
            exportData.append(",");
            exportData.append(studentData.get(Column.STUDENT_NAME));
            exportData.append(",");
            String letterGrade = (String) studentData.get(Column.LETTER_GRADE);
            exportData.append(letterGrade);
            //exportData.append(",");
            //exportData.append(studentData.get(Column.STUDENT_GRADE)); //Calculated Grade

            //Write data to file
            writer.write(exportData.toString());
            writer.write("\n");

        }
        //closing file
        writer.flush();
        writer.close();
        saveFileToResources(site, finalGradesFile);

        /**
         * Leaving the code in here where it saves the file to the gradebook_export directory in
         * case they need to use it for Sungard or whatever they might use. Set the property file
         * local.properties to false and files will be saved to directory
         */
        if (serverConfigurationService.getBoolean("delete.files.gradebook.export.dir", true)){
            cleanDirectory(finalGradesFile);

        }

        // 201 Created
       finalGradeSubmissionResult.setStatus(201);

    }



    /**
     * Adds the submitfinalGrade file to the resource tool of the Course
     *
     *
     * @param site
     * @param finalGradesFile
     */

    private void saveFileToResources(Site site, File finalGradesFile) {
        String contentType = "text/csv";
        String name = Validator.getFileName(finalGradesFile.getName());
         String userId = sessionManager.getCurrentSessionUserId();
        String resourceId = "/user/" + userId + "/";

        //properties for file upload into resources
        ResourcePropertiesEdit props = chs.newResourceProperties();
        props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
        props.addProperty(ResourceProperties.PROP_DESCRIPTION, finalGradesFile.getName());
        props.addProperty(ResourceProperties.PROP_PUBVIEW, "false");

        List attachments = EntityManager.newReferenceList();

        try {
            byte[] bytes = Gradebook2ExportUtil.getBytesFromFile(finalGradesFile);
            ContentResource attachment = chs.addResource(name, resourceId, 10, contentType, bytes, props, 1);
            attachments.add(attachment);
        } catch (Exception e) {
            log.debug("Exception in adding file to Resources" + e);

        }


    }



    private void commitResponse(HttpServletResponse r) {
        // NOT really sure if it would be a good idea to try
        // to commit a response that had already been committed.
        if (!r.isCommitted()) {
            try {
                r.getWriter().flush();
                r.getWriter().close();
            }
            catch (IOException ioe) {
                log.error("Caught IO exception while committing response", ioe);
            }
        }
    }

    /**
     * Retrieve the user Object info for property
     *
     * @param eid
     * @param
     * @return
     */
    private String getUserEmpId(String eid){
        User user = null;
        try {
             user = uds.getUser(eid);

             return (String)user.getProperties().get(EMP_ID);
        } catch (UserNotDefinedException e) {
           log.error("can't find user with emplId of " + eid + " and property name of: " + EMP_ID , e);
        }

        return null;
   }

    /**
     *  This method gets each student's section
     * @param site
     * @param id
     * @return
     */
    private String sectionIdForStudent(Site site, String id){
        Collection collection = site.getGroupsWithMember(id);
        String sectionId = null;
        if ( collection != null && collection.size() > 0){
            Iterator collectionItr = collection.iterator();
            while (collectionItr.hasNext() ){
            Group baseGroup = (Group) collectionItr.next();
            sectionId =baseGroup.getTitle();
              }
        }
        return sectionId;
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

    public ContentHostingService getChs() {
        return chs;
    }

    public void setChs(ContentHostingService chs) {
        this.chs = chs;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
