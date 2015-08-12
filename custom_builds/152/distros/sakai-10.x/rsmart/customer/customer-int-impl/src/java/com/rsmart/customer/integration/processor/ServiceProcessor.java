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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Service Processor
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public abstract class ServiceProcessor extends BaseProcessor implements DataProcessor {
	private static final Log logger = LogFactory.getLog(ServiceProcessor.class);
	/**
	 * Process
	 */
	public void process(ProcessorState state) throws Exception {
		state.setStartDate( new java.util.Date() );
		
		processRows(state);
		
		state.setEndDate( new java.util.Date() );

        logger.info( getProcessorTitle() + " ended " + state.getEndDate() );
        logger.info( getReport(state) );

        /* CLE-4700
           the only output given is if logger is set to info or above.  error output should
           be given in lower levels (eg. ERROR, WARN).  So if we are not in INFO level logging
           and we have errors, just dump the errors and skip the rest of the report.
         */
        if (state.getErrorCnt() > 0 && !logger.isInfoEnabled())
        {
            StringBuilder sb = new StringBuilder ("Errors were encountered while processing.");

            for (Object error : state.getErrorList())
            {
                sb.append(error.toString()).append("\n");
            }

            logger.error(sb.toString());
        }

	}
	
	/**
	 * Override
	 * 
	 * @throws Exception
	 */
	public abstract void processRows(ProcessorState state) throws Exception;
}
