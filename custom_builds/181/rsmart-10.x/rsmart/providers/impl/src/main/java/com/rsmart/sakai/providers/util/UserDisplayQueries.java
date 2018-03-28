package com.rsmart.sakai.providers.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 4/23/12
 * Time: 2:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDisplayQueries extends SimpleJdbcTemplate {
    private static final Log log = LogFactory.getLog(UserDisplayQueries.class);

    public UserDisplayQueries(DataSource dataSource) {
        super(dataSource);
    }

    public String getEnrollmentSections(String siteId, String eid) {
        String appendSite = "/site/" + siteId + "/group%";
        String query = "select cm_enrollment_t.USER_ID, cm_enrollment_t.GRADING_SCHEME from sakai_realm, cm_member_container_t, cm_enrollment_set_t, cm_enrollment_t  " +
                " where sakai_realm.REALM_ID like '" + appendSite + "' and" +
                " sakai_realm.PROVIDER_ID = cm_member_container_t.ENTERPRISE_ID and " +
                " cm_member_container_t.CLASS_DISCR = 'org.sakaiproject.coursemanagement.impl.SectionCmImpl' and " +
                " cm_enrollment_set_t.ENROLLMENT_SET_ID = cm_member_container_t.ENROLLMENT_SET and " +
                " cm_enrollment_t.ENROLLMENT_SET = cm_enrollment_set_t.ENROLLMENT_SET_ID and User_id=?";
        List<Map<String, Object>> gradeSchema = null;
        try {
            gradeSchema = queryForList(query, new Object[]{eid});
            if (gradeSchema == null || gradeSchema.isEmpty()) {
                log.debug("No results were returned from query in siteID" + siteId + "For GradeSchema" + "with user:" + eid);
                return null;
            }
        } catch (Exception e) {
            log.debug("Exception in UserDisplayQueries.class.getName():getEnrollmentSections()", e);
            return null;
        }
        String schemaMap = null;

        for ( Map<String,Object> data : gradeSchema ){
             schemaMap = (String) data.get("GRADING_SCHEME");
             break;

        }
        return schemaMap != null ? schemaMap : null;


    }
}
