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

/**
 * Email Attachment
 * 
 * @author $Author$
 * @revision $Revision$ $Date$
 */
public class EmailAttachment {

	/** File Name */
	public String fileName;

	/** Attachment Contents */
	public String attachmentContents;

	/** Ext */
	public String extension;

	/**
	 * Constructor
	 * 
	 * @param fileName
	 * @param extension
	 * @param attachmentContents
	 */
	public EmailAttachment(String fileName, String extension,
			String attachmentContents) {
		this.attachmentContents = attachmentContents;
		this.extension = extension;
		this.fileName = fileName;
	}

	public String getAttachmentContents() {
		return attachmentContents;
	}

	public void setAttachmentContents(String attachmentContents) {
		this.attachmentContents = attachmentContents;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
