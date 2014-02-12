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

/**
 * EmailRole (can be a role, a section, a group)
 * 
 * @author kimsooil
 *
 */
public class EmailRole {
	String m_realmid = "";
	String m_roleid = "";
	String m_rolesingular = "";
	String m_roleplural = "";
	String roletype="";
	
	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural)
	{
		m_realmid = realmid;
		m_roleid = roleid;
		m_rolesingular = rolesingular;
		m_roleplural = roleplural;
	}

	public EmailRole(String realmid, String roleid, String rolesingular, String roleplural, String rtype)
	{
		m_realmid = realmid;
		m_roleid = roleid;
		m_rolesingular = rolesingular;
		m_roleplural = roleplural;
		roletype=rtype;
	}
	public String getRealmid() {
		return m_realmid;
	}
	
	public void setRealmid(String m_realmid) {
		this.m_realmid = m_realmid; 
	}
	
	public String getRoleid() {
		return m_roleid;
	}
	public void setRoleid(String m_roleid) {
		this.m_roleid = m_roleid;
	}
	public String getRoleplural() {
		return m_roleplural;
	}
	public void setRoleplural(String m_roleplural) {
		this.m_roleplural = m_roleplural;
	}
	public String getRolesingular() {
		return m_rolesingular;
	}
	public void setRolesingular(String m_rolesingular) {
		this.m_rolesingular = m_rolesingular;
	}

	public String getRoletype() {
		return roletype;
	}

	public void setRoletype(String roletype) {
		this.roletype = roletype;
	}
}
