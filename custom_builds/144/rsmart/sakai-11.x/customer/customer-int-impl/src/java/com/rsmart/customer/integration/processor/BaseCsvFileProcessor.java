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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.rsmart.customer.integration.util.FileArchiveUtil;

/**
 * Abstract Base File Processor
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public abstract class BaseCsvFileProcessor extends BaseSeparatedFileProcessor implements FileProcessor {

	/** Log */
	private static final Log logger = LogFactory.getLog(BaseCsvFileProcessor.class);
	
	/**
	 * Process
	 */
	public void processFormattedFile (BufferedReader fr, FileProcessorState state)
        throws Exception
    {
		CSVReader csvr = new CSVReader(fr);
		String[]  line = null;

		while (((line = csvr.readNext()) != null)) {
			state.setRecordCnt( state.getRecordCnt() + 1 );

            boolean headerPresent = state.isHeaderRowPresent();

			if( state.getColumns() != line.length ) {
				state.appendError("Wrong Number Columns Row:, " + state.getRecordCnt()+ "Saw:"+ line.length + ", Expecting: " + state.getColumns());
				state.setErrorCnt( state.getErrorCnt() + 1 );
			}
			else if ((headerPresent && state.getRecordCnt() > 1) || !headerPresent) {
				try {
					line = trimLine(line); 
					processRow(line, state);
					state.setProcessedCnt( state.getProcessedCnt() + 1);
				} catch (Exception err) {
					logger.error("" ,err);
					state.appendError( "Row " + state.getRecordCnt() + " " + err.getMessage() );
					state.setErrorCnt( state.getErrorCnt() + 1 );
				}
			}
		}
		
		fr.close();
	}
	
	protected String[] trimLine(String[] line)
	{
		String trim = "";
		for(int i = 0; i < line.length; i++)
		{
			trim = line[i].trim();
			line[i] = trim;
		}
		
		return line;
	}
}
