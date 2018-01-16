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

package com.rsmart.customer.integration.util;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * File Archive Util
 * 
 * @author dhelbert
 * @revision $Revision$ $Date$
 */
public class FileArchiveUtil {
	
	/** Directory */
	private String directory;

	/** Date Format */
	private String dateFormat;
	
	/**
	 * Method to creat archive folder.
	 * 
	 * @return File
	 */
	public File createArchiveFolder() {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		String newFolderName = sdf.format( new java.util.Date() );
		String path = directory + System.getProperty("file.separator") +newFolderName;
		File f = new File(path);
		
		if( !f.exists() ) {
			f.mkdir();
		}
		
		return f;
	}
	
	/**
	 * Get Directory
	 * 
	 * @return
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Set Directory
	 * 
	 * @param directory
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	
	
	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FileArchiveUtil util = new FileArchiveUtil();
		util.setDirectory("D:\\temp");
		util.setDateFormat("yyyy-MM-dd");
		
		File f = util.createArchiveFolder();
		
		if( f.exists() ) {
			System.out.println("Hello Folder!");
		}
		else {
			System.out.println("Folder Not Found");
		}
	}
}
