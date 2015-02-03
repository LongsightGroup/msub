/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

//Quartz job to auto release grades
package org.sakaiproject.job.gradebook;

import java.util.List;
import org.apache.commons.logging.Log;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.app.scheduler.jobs.SpringConfigurableJobBeanWrapper;

public class AutoReleaseJob implements Job {
	
	//Matches the bean id
	final static String beanId = "gradebookAutoReleaseJob";
	
	//Matches the jobName
	final static String jobName = "Auto Release Grades Job";
	
	//For overriding the security advisor
	protected SecurityAdvisor sa;
	//All required services
	protected SecurityService securityService;
	protected GradebookService gradebookService;
    protected SchedulerManager schedulerManager;
	
    /**
     * @return Returns the schedulerManager.
     */
    public SchedulerManager getSchedulerManager()
    {
      return schedulerManager;
    }

    /**
     * @param schedulerManager
     *          The schedulerManager to set.
     */
    public void setSchedulerManager(SchedulerManager schedulerManager)
    {
      this.schedulerManager = schedulerManager;
    }
    
	public GradebookService getGradebookService() {
		return gradebookService;
	}
	
	public void setGradebookService (GradebookService gradebookService) {
		this.gradebookService=gradebookService;
	}
	
	public SecurityService getSecurityService() {
		return securityService;
	}
	
	public void setSecurityService (SecurityService securityService) {
		this.securityService=securityService;
	}

	//This needs our own security advisor
	public AutoReleaseJob() {
		sa = new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference) {
				return SecurityAdvice.ALLOWED;
			}
		};
	}
	/**
	 * Setup a security advisor.
	 */
	public void pushAdvisor() {
		// setup a security advisor
		securityService.pushAdvisor(sa);
	}

	/**
	 * Remove our security advisor.
	 */
	public void popAdvisor() {
		securityService.popAdvisor(sa);
	}
	
	private static final Log LOG = LogFactory.getLog(AutoReleaseJob.class);
	private String configMessage;

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		LOG.info("Auto releasing gradebook items past date. " + getConfigMessage());
		//Need the check all overdue submissions
		//Need to override security
		pushAdvisor();
		try {
			List<GradableObject> past = gradebookService.getPastAutoReleaseDate();
			//Need to submit all overdue submissions
			for(GradableObject p : past) {
				LOG.info(p);
				Assignment a = gradebookService.getAssignment(p.getGradebook().getUid(), p.getName());
				//Release the item
				a.setReleased(true);
				//Clear the auto release date since this is released
				a.setAutoReleaseDate(null);
				gradebookService.updateAssignment(p.getGradebook().getUid(), p.getName(), a);
				LOG.debug("Auto releasing gb item " + p);
			}
		} 
		finally {
			popAdvisor();
		}
	}
	
	public String getConfigMessage() {
		return configMessage;
	}

	public void setConfigMessage(String configMessage) {
		this.configMessage = configMessage;
	}
	
	public void init() {
		LOG.info("init()");
		//Is there some other way to schedule this?!?
		try {
		    Scheduler sched = schedulerManager.getScheduler();
		    if (sched == null) {
		      LOG.error("Scheduler is down!");
		      return;
		    }	
			JobDetail jobDetail = new JobDetail( "Auto Release Job", Scheduler.DEFAULT_GROUP, SpringConfigurableJobBeanWrapper.class);
			jobDetail.getJobDataMap().put("org.sakaiproject.api.app.scheduler.JobBeanWrapper.bean", beanId);
			jobDetail.getJobDataMap().put("org.sakaiproject.api.app.scheduler.JobBeanWrapper.jobType",jobName);
			CronTrigger trigger =  new CronTrigger( "Auto Release Job", Scheduler.DEFAULT_GROUP);
			trigger.setCronExpression( "0 0 0 * * ? *" );
			sched.scheduleJob(jobDetail, trigger);
		} catch (Exception e) {
			//This can probably just be a debug
			LOG.info(e.getMessage());
		}
	}
}
