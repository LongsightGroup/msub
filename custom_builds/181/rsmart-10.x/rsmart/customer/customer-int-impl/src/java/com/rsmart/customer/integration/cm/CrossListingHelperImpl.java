package com.rsmart.customer.integration.cm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 5/29/12
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrossListingHelperImpl implements CrossListingHelper {
	private static Log logger = LogFactory.getLog(CrossListingHelperImpl.class);
    private SqlService sqlService;
    public void init(){

    }

    public String getCrossListingId(String eid) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = sqlService.borrowConnection();
            ps = con.prepareStatement("select cross_listing from cm_member_container_t where enterprise_id =? and class_discr=? ");
            ps.setString(1, eid);
            ps.setString(2, "org.sakaiproject.coursemanagement.impl.CourseOfferingCmImpl");
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
           logger.error(e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
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
        return null;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}
