package com.rsmart.customer.integration.processor.cle.cm;

import org.hibernate.Query;
import org.sakaiproject.db.api.SqlService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.coursemanagement.api.Section;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Mar 16, 2010
 * Time: 8:47:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class SectionHibernateHelperImpl extends HibernateDaoSupport {
    private static final Log log = LogFactory.getLog(SectionHibernateHelperImpl.class);
    private Section outerSection;
    private Session outerSession;
    private SqlService sqlService;


    public SectionHibernateHelperImpl(){}

    public void init() {
                log.info("Initializing " + getClass().getName());
        }

        public void destroy() {
                log.info("Destroying " + getClass().getName());
        }

    

    /**
     * This method deletes the section meeting based on the sectionId in the  CM_MEMBER_CONTAINER_T
     *  Uses the doInhibernate inner class to get a Session
     *
     *
     * @param section
     */

    public boolean deleteSectionMeetings(final Section section){

       log.info("**********************************" + getClass().getName() + "Nethods:deleteSectionMeetings*******************************************");

        Connection con =  null;
        PreparedStatement ps = null;
        try {
            con = sqlService.borrowConnection();
            ps = con.prepareStatement("delete from cm_meeting_t where section_id = ( select member_container_id from cm_member_container_t where enterprise_id   = ?)");
            ps.setString(1, section.getEid());
            boolean retVal =  ps.execute();
            return retVal;
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
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

        return false;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}
