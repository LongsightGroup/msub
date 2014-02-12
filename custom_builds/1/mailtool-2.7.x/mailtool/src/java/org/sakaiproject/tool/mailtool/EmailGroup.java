/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.mailtool;

import java.util.ArrayList;
import java.util.List;

/**
 * EmailGroup in Mailtool (Can be roles, groups, sections in the site), commented by kimsooil@bu.edu
 * 
 * @author sgithens
 *
 */
public class EmailGroup {
	protected EmailRole m_emailrole = null;
	protected List /* EmailUser */ m_emailusers = new ArrayList();
	
	public EmailGroup(EmailRole emailrole, List /* EmailUser */ emailusers)
	{
		m_emailrole = emailrole;
		m_emailusers = emailusers;
	}

	public String getRolePlural() 
	{
		return m_emailrole.getRoleplural();
	}

	public EmailRole getEmailrole() {
		return m_emailrole;
	}
	public void setEmailrole(EmailRole m_emailrole) {
		this.m_emailrole = m_emailrole;
	}
	public List getEmailusers() {
		return m_emailusers;
	}
	public void setEmailusers(List m_emailusers) {
		this.m_emailusers = m_emailusers;
	}

}