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

package com.rsmart.customer.integration.job;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;

import com.rsmart.customer.integration.processor.DataProcessor;
import com.rsmart.sakai.common.job.AbstractAdminJob;

/**
 * Integration Job
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public class IntegrationJob extends AbstractAdminJob {

	/** Logger */
	private static Log logger = LogFactory.getLog(IntegrationJob.class);

	/** Data Processors */
	private List dataProcessors;

	/** Email Service */
	private EmailService emailService;
	
	/** Recipient List */
	private List recipientList = new ArrayList();
	
	/** From Address */
	private String fromAddress = null;

    private String recipients;

    /**
	 * Init Method
	 *
	 */
	public void init() {
		fromAddress = "\""
			+ ServerConfigurationService.getString("ui.institution",
					"Sakai") + "\"<no-reply@"
			+ ServerConfigurationService.getServerName() + ">";
        if (getRecipients() != null && getRecipients().length() >0) {
            String[] items = getRecipients().split(",");
            for (int i = 0; i < items.length; i++){
                recipientList.add(items[i].trim());
            }
        }
    }
	
	/**
	 * Execute
	 * 
	 * @param jec
	 */
	public void executeInternal(JobExecutionContext jec) throws JobExecutionException {
		synchronized (this.getClass()) {
			logger.info("Starting Integration Job");

            JobDataMap
                jdm = jec.getMergedJobDataMap();

			if (dataProcessors != null) {
				for (int i = 0; i < dataProcessors.size(); i++) {
					DataProcessor dp = (DataProcessor) dataProcessors.get(i);

                    ProcessorState
                        state = null;

					try {
                        state = dp.init(jdm);
						dp.preProcess(state);
						dp.process(state);
						dp.postProcess(state);
					} catch (Exception err) {
						err.printStackTrace();
					}
					finally {
						sendEmails(dp, state);
                        if (state != null) {
                            state.reset();
                        }
					}
				}
			} else {
				throw new JobExecutionException(
						"Data processors list has not been set.");
			}

			logger.info("Integration Job Complete");
		}
	}

	/**
	 * Send Emails
	 *
	 * @param dp
	 */
	private void sendEmails( DataProcessor dp, ProcessorState state ) {
		if( recipientList != null && recipientList.size() > 0 ) {
			try {				
				for( int i=0; i<recipientList.size(); i++) {
					emailService.send(fromAddress, recipientList.get(i).toString(), dp.getProcessorTitle(), dp.getReport(state), null, null, null);
				}
			}
			catch(Exception err) {
				logger.error(err);
			}
		}
	}
	
	/**
	 * Get DPs
	 * 
	 * @return
	 */
	public List getDataProcessors() {
		return dataProcessors;
	}

	/**
	 * Set DPs
	 * 
	 * @param dataProcessors
	 */
	public void setDataProcessors(List dataProcessors) {
		this.dataProcessors = dataProcessors;
	}

	/**
	 * Get Email Service
	 * 
	 * @return EmailService
	 */
	public EmailService getEmailService() {
		return emailService;
	}

	/**
	 * Set Email Service
	 * 
	 * @param emailService
	 */
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
    /**
	 * Recipient List
	 * 
	 * @return
	 */
	public List getRecipientList() {
		return recipientList;
	}

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

}
