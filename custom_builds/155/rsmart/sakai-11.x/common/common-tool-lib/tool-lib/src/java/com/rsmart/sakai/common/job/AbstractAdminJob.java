/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */
package com.rsmart.sakai.common.job;

import com.rsmart.sakai.common.security.SuperUserSecurityAdvisor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import java.util.Date;
import java.util.List;



/**
 * The class takes care of security issues when running jobs as admin.  This is handled by using a SuperUserSecurityAdvisor.
 * This advisor will allow all permissions.
 */
abstract public class AbstractAdminJob implements Job {
   protected final Log logger = LogFactory.getLog(getClass());
   private SessionManager sessionManager;
   private String adminUser;
   private SecurityService securityService;

   /**
    * Child classes should implement their work in this guy.
    * @param jobExecutionContext
    * @throws JobExecutionException
    */
   protected abstract void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException;

   /**
    * Switchs the current user to the adminUser and pushes a SecurityAdvisor onto the security stack to avoid authz issues.
    * Then we invoke executeInternal, and switch the current user back.
    * @param jobExecutionContext
    * @throws JobExecutionException
    */
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
      logger.info(this.getClass().getName() + " starting...");

      Session sakaiSession = sessionManager.getCurrentSession();
      String currentUserId = sakaiSession.getUserId();

      sakaiSession.setUserId(adminUser);
      sakaiSession.setUserEid(adminUser);

      SuperUserSecurityAdvisor securityAdvisor = new SuperUserSecurityAdvisor();
      securityAdvisor.setSuperUser(adminUser);
      securityService.pushAdvisor(securityAdvisor);

      	boolean jobRunning = false;
		try {
		      List<JobExecutionContext> jobs = jobExecutionContext.getScheduler().getCurrentlyExecutingJobs();
		      for (JobExecutionContext job : jobs) {
		    	  // compare using job name but ensure its a different instance than the current one
		    	  if (job.getJobDetail().getKey().getName().equals(jobExecutionContext.getJobDetail().getKey()) 
		    			  && !job.getTrigger().getFireInstanceId().equals(jobExecutionContext.getTrigger().getFireInstanceId())) {
		    		  jobRunning = true;
		    	  }
		      }
		} catch (SchedulerException e) {
			logger.warn("Could not get currently executing jobs.", e);
		}

		Date now = new Date();

		if (jobRunning) {
			logger.warn("There's another instance of job " + jobExecutionContext.getJobDetail().getKey() 
					+ " running, cancelling this instance " + jobExecutionContext.getTrigger().getFireInstanceId());
		} else {
			executeInternal(jobExecutionContext);
		}

		Date later = new Date();

      sakaiSession.setUserId(currentUserId);
      sakaiSession.setUserEid(currentUserId);

      logger.info(this.getClass().getName() + " finished in " +  (later.getTime() - now.getTime()) + " millis");
   }

   public SessionManager getSessionManager() {
      return sessionManager;
   }

   /**
    * inject the SessionManager service
    * @param session
    */
   public void setSessionManager(SessionManager session) {
      this.sessionManager = session;
   }

   public String getAdminUser() {
      return adminUser;
   }

   /**
    * the job will execute as this user.  Typically, you want to inject "admin" here
    * @param adminUser
    */
   public void setAdminUser(String adminUser) {
      this.adminUser = adminUser;
   }

   public SecurityService getSecurityService() {
      return securityService;
   }

   /**
    * inject the SecurityService
    * @param securityService
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }
}
