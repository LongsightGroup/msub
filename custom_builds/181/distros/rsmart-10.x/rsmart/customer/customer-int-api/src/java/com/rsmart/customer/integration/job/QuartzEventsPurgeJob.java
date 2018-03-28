package com.rsmart.customer.integration.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.db.api.SqlService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/12/12
 * Time: 9:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuartzEventsPurgeJob implements Job {
    public static String
    NUMBER_HOURS = "number.hours";

    public static String JOBNAME_FILTER = "jobName.filter";

    private SqlService sqlService;

    private static final Log LOG = LogFactory.getLog(QuartzEventsPurgeJob.class);

    public void runJob() throws JobExecutionException {
        final String noDays = getConfiguredProperty(NUMBER_HOURS);

        if (noDays == null || noDays.trim().length() == 0) {
            throw new JobExecutionException("job improperly configured - number of hours not set for purge cutoff");
        }


        final Calendar cal = Calendar.getInstance();

        int numHours = 0;

        try {
            numHours = Integer.parseInt(noDays);
        } catch (NumberFormatException nfe) {
            throw new JobExecutionException("job improperly configured - number of hours for cutoff must be an integer greater than 1");
        }

        if (numHours < 1) {
            throw new JobExecutionException("job improperly configured - number of hours must be 1 or more");
        }

        cal.add(Calendar.HOUR, -numHours);

        Date cutoffDate = new Date(cal.getTimeInMillis());


        final String jobNameFilter = getConfiguredProperty(JOBNAME_FILTER);

        if (jobNameFilter == null || noDays.trim().length() == 0) {
            throw new JobExecutionException("job improperly configured - jobName filter not set for purge");
        }

        purgeQuartzEvents(jobNameFilter, cutoffDate);

    }


    private void purgeQuartzEvents(String jobNameFilter, Date cutoffDate) {
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = sqlService.borrowConnection();
            stmt = con.prepareStatement("delete from scheduler_trigger_events where jobName like ? and eventTime < ?");
            stmt.setString(1, jobNameFilter);
            stmt.setTimestamp(2, new Timestamp(cutoffDate.getTime()));
            stmt.execute();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
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

    }

    private JobExecutionContext
        executionContext = null;

    public void setJobExecutionContext(JobExecutionContext jec)
    {
        executionContext = jec;
    }

    public JobExecutionContext getJobExecutionContext()
    {
        return executionContext;
    }

    public String getConfiguredProperty (String key)
    {
        return getJobExecutionContext().getMergedJobDataMap().get(key).toString();
    }

    public final void execute(JobExecutionContext jobExecutionContext)
        throws JobExecutionException
    {
        setJobExecutionContext(jobExecutionContext);

        runJob();
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}