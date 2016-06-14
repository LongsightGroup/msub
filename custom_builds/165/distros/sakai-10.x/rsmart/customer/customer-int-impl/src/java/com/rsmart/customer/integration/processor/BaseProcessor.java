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

package com.rsmart.customer.integration.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract Base Processor
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public abstract class BaseProcessor implements DataProcessor
{


	/**
	 * Init Processor
	 *
	 */
	public ProcessorState init(Map configuration)
    {
        BaseProcessorState
            bps = new BaseProcessorState();

        bps.setConfiguration(configuration);

        return bps;
	}
	
	/**
	 * Get Title
	 * 
	 * @return String
	 */
	public abstract String getProcessorTitle();

    /**
     * Pre Process
     *
     * @throws Exception
     */
    public void preProcess(ProcessorState state) throws Exception {
    }

    /**
     * Post Process
     *
     * @throws Exception
     */
    public void postProcess(ProcessorState state) throws Exception {
    }

    /**
     * Process
     *
     * @throws Exception
     */
    public void process(ProcessorState state) throws Exception {
    }


	/**
	 * Get Report String
	 * 
	 * @return String
	 */
	public String getReport(ProcessorState state) {
		String reportTxt = "\n";
		reportTxt += getProcessorTitle() + "\n\n";

        if (state == null)
        {
            reportTxt += "Processor state appears not to have created successfully. No reporting data is available.";

            return reportTxt;
        }
		
		reportTxt += "Records   " + state.getRecordCnt() + "\n";
		reportTxt += "Processed " + state.getProcessedCnt() + "\n";
		reportTxt += "Errors    " + state.getErrorCnt() + "\n";
		reportTxt += "Inserts   " + state.getInsertCnt() + "\n";
		reportTxt += "Updates   " + state.getUpdateCnt() + "\n";
		reportTxt += "Unchanged " + state.getIgnoreCnt() + "\n";
		reportTxt += "Deletes   " + state.getDeleteCnt() + "\n";
		reportTxt += "Start     " + state.getStartDate() + "\n";
		reportTxt += "End       " + (state.getEndDate() == null ? "" : state.getEndDate()) + "\n";
        reportTxt += "Configuration: " + "\n";

        Map config = state.getConfiguration();

        String sep = "";
        for (String key : (Set<String>)config.keySet())
        {
            reportTxt += "\t" + key + "\t->\t" + config.get(key).toString() + "\n";
        }
        reportTxt += "\n";

		reportTxt += "Messages: \n";
		
		StringBuffer sb = new StringBuffer();

        List errorList = state.getErrorList();

		for(int i=0; i<errorList.size(); i++) {
			sb.append( "\n* " + errorList.get(i) );
		}
		
		reportTxt += sb.toString();
		
		return reportTxt;
	}
}
