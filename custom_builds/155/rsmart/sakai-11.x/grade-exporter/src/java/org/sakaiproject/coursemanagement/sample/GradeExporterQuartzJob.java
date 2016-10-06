/**********************************************************************************
*
* GradeExporterQuartzJob.java
* by Earle Nietzel
*    John Bush
*
***********************************************************************************
*
 * Copyright (c) 2008 The Sakai Foundation
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.coursemanagement.sample;

import java.util.regex.Pattern;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 */
public class GradeExporterQuartzJob implements Job {
	protected static Pattern termEidPattern = Pattern.compile(".*(?i)term=");
	protected static Pattern gradePeriodPattern = Pattern.compile(".*(?i)period=");
	protected GradeExporter exporter;

	public void execute(JobExecutionContext context) throws JobExecutionException {
		String jobName = context.getJobDetail().getKey().getName();
		if (jobName != null) {
			String[] splitJobName = termEidPattern.split(jobName);
			if (splitJobName.length == 2) {
				// if a term was supplied in the job name it will be used
				if (splitJobName[1].indexOf(' ') == -1)
					exporter.setTermEid(splitJobName[1]);
				else
					exporter.setTermEid(splitJobName[1].substring(0, splitJobName[1].indexOf(' ')));
			} else {
				exporter.setTermEid(null);
			}
			splitJobName = gradePeriodPattern.split(jobName);
			if (splitJobName.length == 2) {
				// if a period was supplied in the job name it will be used
				if (splitJobName[1].indexOf(' ') == -1)
					exporter.setGradePeriod(splitJobName[1]);
				else
					exporter.setGradePeriod(splitJobName[1].substring(0, splitJobName[1].indexOf(' ')));
			} else {
				exporter.setGradePeriod(null);
			}

		}
		exporter.execute();
	}

	public void setExporter(GradeExporter exporter) {
		this.exporter = exporter;
	}

}
