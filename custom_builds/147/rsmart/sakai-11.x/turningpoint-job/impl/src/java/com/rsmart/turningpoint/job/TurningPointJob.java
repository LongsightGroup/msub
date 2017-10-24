package com.rsmart.turningpoint.job;

import com.rsmart.sakai.common.job.AbstractAdminJob;
import com.rsmart.turningpoint.api.TurningPointJobService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TurningPointJob extends AbstractAdminJob {

    protected final Log logger = LogFactory.getLog(getClass());
    private TurningPointJobService turningPointJobService;

    public TurningPointJobService getTurningPointJobService() {
		return turningPointJobService;
	}

	public void setTurningPointJobService(
			TurningPointJobService turningPointJobService) {
		this.turningPointJobService = turningPointJobService;
	}

	public void init() {
    	logger.info("Initialize TurningPoint Synchronization job");
    	//super.init();
    }

    public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("Starting TurningPoint Synchronization job");
        
        try
        {
        	turningPointJobService.updateUserDevices();
        }
        catch(Exception e)
        {
        	logger.error("Error Executing TurningPoint Helper Job", e);
        }

    }

}