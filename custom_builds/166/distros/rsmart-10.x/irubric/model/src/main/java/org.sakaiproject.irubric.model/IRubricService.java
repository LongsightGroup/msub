package org.sakaiproject.irubric.model;

import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/23/13
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
  public interface IRubricService {
     static final String PURPOSE = "purpose";
   	 static final String CMD_VIEW = "v";
   	 static final String CMD_ATTACH = "a";
   	 static final String CMD_GRADE = "g";
   	 static final String CMD_GRADE_ALL = "ga";
   	 static final String CMD_GET_GRADE = "gg";
   	 static final String CMD_GET_GRADES_BY_GDB = "gag";
   	 static final String CMD_GET_GRADES_BY_ROS = "gas";
     static final String XTOKEN = "xtoken";

   	 static final String NULL_STRING = "null";
   	 static final String EMPTY_STRING = "";
   
   	 static final String P_CERTIFICATE_ID = "certID";
   	 static final String P_SITE_ID = "siteId";
   	 static final String P_SITE_TITLE = "siteTitle";
   	 static final String P_GDB_ITEM_NAME = "gradebookItemName";
   	 static final String P_GDB_ITEM_ID = "gradebookItemId";
   	 static final String P_GDB_ITEM_ENTRY_TYPE = "gradeEntryType";
   	 static final String P_GDB_ITEM_CAL = "cw";
   	 static final String P_POINTS_POSSIBLE = "pointsPossible";
   	 static final String P_ACADEMIC_ID = "academicSessionId";
   	 static final String P_PROP_SITE_SECTION_EID = "site.cm.requested";
   	 static final String P_USR_ID = "userId";
   	 static final String P_USR_FNAME = "userFirstName";
   	 static final String P_USR_LNAME = "userLastName";
   	 static final String P_USR_ROLE = "userRole";
   	 static final String DATA_STUDENTS = "dataStudents";
   	 static final String P_ROS_ID = "rosterStudentId";
   	 static final String P_ROS_FNAME = "rosterStudentFirstName";
   	 static final String P_ROS_LNAME = "rosterStudentLastName";
   	 static final String P_ENS = "enrollmentstatus";
   	 static final String P_EN_SET = "enrollmentset";
   	 static final String P_ROLE_TYPE = "userroletype";
     static final String P_RUB_ID = "rubricId";

   	 static final String CATEGORY_OPT_NONE = "noCategories";
   	 static final String CATEGORY_OPT_CAT_ONLY = "onlyCategories";
   	 static final String CATEGORY_OPT_CAT_AND_WEIGHT = "categoriesAndWeighting";
   	 static final String ENTRY_OPT_POINTS = "points";
   	 static final String ENTRY_OPT_PERCENT = "percent";
   	 static final String ENTRY_OPT_LETTER = "letterGrade";
   	 static final String ROLE_TYPE_EVALUATOR = "evaluator";
   	 static final String ROLE_TYPE_EVALUATEE = "evaluatee";

    GradebookService getGradebookService();

    SiteService getSiteService();

    int getTimeout();

    String getIrubricRootUrl();

    String getIrubricInitReqUrl();

    String getIrubricRedirectUrl();

    String getInitReqURL();

    void updateAssignmetByRubric(Long gradebookItemId, String iRubricId,
                                 String iRubricTitle);

    String getUserRoleType(String roleName);

    void buildPostDataForAttach(StringBuilder dataBuilder, String gradebookUid, Long gradebookItemId) throws Exception;

    void buildPostDataForGrade(String gradebookUid, StringBuilder dataBuilder,
                               Long assignmentId, String rosterStudentId) throws Exception;

    Double getPointsPossible();

    String processOneGrade(String sGrade);

    String buildDefaultPostData(String currentSiteId) throws Exception;

    void addGradebookParams(String gradebookUid, Long gradebookItemId,
    			StringBuilder dataBuilder);

    void addRosterParams(String rosterStudentId,
    			StringBuilder dataBuilder, String siteId) throws Exception;

    IRubricManager getiRubricManager();

    void saveGradeFromGB2(String gradebookUuid, long gradebookItemId, String strScoreStream);

    boolean isIRubricAvailable(long gradebookItemId);

    void refreshGrades(String gradebookUid, String gradebookItemIdStr);

    String doiRubricAuthentication(String postData);

    String getData(String secToken);
}

