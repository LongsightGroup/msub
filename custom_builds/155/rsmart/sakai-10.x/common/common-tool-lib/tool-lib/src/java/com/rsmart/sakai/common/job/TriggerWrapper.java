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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper;



/**
 * Automatically schedules jobs as startup time.  Simply add a TriggerWrapper instance to your spring config, and wire
 * up the JobWrapper, name for the trigger, and cronexpression.
 */
public class TriggerWrapper {
   protected final Log logger = LogFactory.getLog(getClass());

   private String name;
   private String cronExpression;
   private SpringJobBeanWrapper jobWrapper;

   public void init() {
      Scheduler scheduler = jobWrapper.getSchedulerManager().getScheduler();
      if (scheduler == null) {
         logger.error("Scheduler is down!");
         return;
      }

      try {
         Trigger trigger = scheduler.getTrigger(name, Scheduler.DEFAULT_GROUP);
         // only create the job if it isn't there already
         if (trigger == null) {
            JobDetail jd = new JobDetail(jobWrapper.getJobType(), Scheduler.DEFAULT_GROUP, jobWrapper.getJobClass(), false, true, true);
            jd.getJobDataMap().put(JobBeanWrapper.SPRING_BEAN_NAME, jobWrapper.getBeanId());
            jd.getJobDataMap().put(JobBeanWrapper.JOB_TYPE, jobWrapper.getJobType());
            scheduler.addJob(jd, false);

            trigger = new CronTrigger(name, Scheduler.DEFAULT_GROUP,
                  jobWrapper.getJobType(), Scheduler.DEFAULT_GROUP, cronExpression);
            scheduler.scheduleJob(trigger);
         }
      }
      catch (Exception e) {
         logger.error("Failed to create trigger: " + e.getMessage(), e);
      }

   }

   public String getCronExpression() {
      return cronExpression;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setCronExpression(String cronExpression) {
      this.cronExpression = cronExpression;
   }

   public org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper getJobWrapper() {
      return jobWrapper;
   }

   public void setJobWrapper(org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper jobWrapper) {
      this.jobWrapper = jobWrapper;
   }
}
