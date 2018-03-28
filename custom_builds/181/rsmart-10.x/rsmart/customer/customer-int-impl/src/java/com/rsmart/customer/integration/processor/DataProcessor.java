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

import java.util.Map;

/**
 * Data Processor
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public interface DataProcessor {
	
	/**
	 * Pre Process
	 * 
	 * @throws Exception
	 */
	public void preProcess(ProcessorState state) throws Exception;
	
	/**
	 * Post Process
	 * 
	 * @throws Exception
	 */
	public void postProcess(ProcessorState state) throws Exception;
	
	/**
	 * Process
	 * 
	 * @throws Exception
	 */
	public void process(ProcessorState state) throws Exception;
	
	/**
	 * Get Title
	 * 
	 * @return
	 */
	public String getProcessorTitle();
	
	/**
	 * Get Report
	 * 
	 * @return
	 */
	public String getReport(ProcessorState state);
	
	/**
	 * Init
	 *
	 */
	public ProcessorState init(Map configuration);
	
}
