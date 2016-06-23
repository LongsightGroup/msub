package com.rsmart.customer.integration.job;

import com.rsmart.sakai.common.job.AbstractAdminJob;
import com.rsmart.userdataservice.UserDataService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.user.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 7/27/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsnUserSyncJob extends AbstractAdminJob {
    	/** Logger */
	private static Log logger = LogFactory.getLog(RsnUserSyncJob.class);
    private static final String LAST_RSN_SYNC_PROPERTY = "last.rsnUser.sync";
    // useful to switch to this for testing...
    //private static final long ONE_HOUR_IN_MILLIS = 60 * 1000;
    private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;

    private SqlService sqlService;
    private UserDirectoryService uds;
    private UserDataService userDataService;
    /**
	 * Init Method
	 *
	 */
	public void init() {

    }

	/**
	 * Execute
	 *
	 * @param jec
	 */
	public void executeInternal(JobExecutionContext jec) throws JobExecutionException {
		synchronized (this.getClass()) {
			logger.info("Starting RsnUserSyncJob Job");

            Connection conn = null;
            ResultSet rs = null;
            PreparedStatement ps = null;

            try {
                conn = sqlService.borrowConnection();
                ps = conn.prepareStatement("SELECT EID FROM sakai_user_id_map");
                rs = ps.executeQuery();
                while (rs.next()) {
                    boolean updateRsnInfo = true;
                    String eid = rs.getString("EID");
                    try {
                        User user = uds.getUserByEid(eid);
                        if (user != null) {
                            String lastRsnSyncValue = user.getProperties().getProperty(LAST_RSN_SYNC_PROPERTY);
                            if (lastRsnSyncValue != null) {
                                Date lastSyncDate = new Date(Long.valueOf(lastRsnSyncValue));
                                if ((new Date().getTime() - lastSyncDate.getTime()) < ONE_HOUR_IN_MILLIS) {
                                    updateRsnInfo = false;
                                }
                            }
                            if (updateRsnInfo) {
                                userDataService.saveOrUpdateRsnUser(user);
                                logger.info("updating user in rsn_tables with eid: " + user.getEid());
                                user.getProperties().addProperty(LAST_RSN_SYNC_PROPERTY, String.valueOf(new Date().getTime()));
                            }
                        }
                    } catch (UserNotDefinedException e) {
                        logger.debug("can't find user eith eid: " + eid, e);
                    }

                }
            } catch (SQLException e) {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e1) {
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e1) {
                    }
                }

                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e1) {
                    }
                }

            }

			logger.info("RsnUserSyncJob Complete");
		}
	}

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setUds(UserDirectoryService uds) {
        this.uds = uds;
    }

    public void setUserDataService(UserDataService userDataService) {
        this.userDataService = userDataService;
    }
}
