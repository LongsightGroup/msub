/**********************************************************************************
 *
 * $URL: https://source.sakaiproject.org/contrib/etudes/melete/tags/2.8.2/melete-api/src/java/org/etudes/api/app/melete/CcLicenseService.java $
 * $Id: CcLicenseService.java 56408 2008-12-19 21:16:52Z rashmi@etudes.org $  
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 *
 * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/
package org.etudes.api.app.melete;

/**
 * Filename:
 * Description:
 * Author:
 * Date:
 * Copyright 2004, Foothill College
 */
public interface CcLicenseService {
	public abstract boolean isReqAttr();

	public abstract void setReqAttr(boolean reqAttr);

	public abstract boolean isAllowCmrcl();

	public abstract void setAllowCmrcl(boolean allowCmrcl);

	public abstract int getAllowMod();

	public abstract void setAllowMod(int allowMod);

	public abstract String getUrl();

	public abstract void setUrl(String url);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract String toString();

	public abstract boolean equals(Object other);

	public abstract int hashCode();
}