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

import java.util.HashMap;

/**
 * Test Processor
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public class TestBaseFileProcessor extends BaseSeparatedFileProcessor {

	public String getProcessorTitle() {
		return "Test";
	}
	
	public void processRow(String[] data, ProcessorState state) throws Exception {
		for( int i=0; i<data.length; i++ ) {
			System.out.print(data[i]);
			System.out.print("|");
		}
		
		System.out.println();
	}

	public static void main(String[] args ) {
		TestBaseFileProcessor test = new TestBaseFileProcessor();
		test.setFilename(args[0]);
		test.setToken(",");
		test.setColumns(Integer.parseInt(args[1]));
		//test.setArchive(true);
		
		//FileArchiveUtil fileArchiveUtil = new FileArchiveUtil();
		//fileArchiveUtil.setDirectory(args[2]);
		//test.setFileArchiveUtil(fileArchiveUtil);

        ProcessorState
            state = test.init(new HashMap());

		try {
			test.process(state);
		}
		catch(Exception err) {
			err.printStackTrace();
		}
	}
}
