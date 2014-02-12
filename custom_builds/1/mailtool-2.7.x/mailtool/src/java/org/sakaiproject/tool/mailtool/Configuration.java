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
 * Configuration for "show renamining role" in Mailtool options
 * 
 * @author kimsooil
 *
 */
public class Configuration {

	private int id;
	private String rid;
	private String realmid;
	private String singular;
	private String singularNew;
	private String plural;
	private String pluralNew;
	
	public Configuration() {
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id=id;
	}
	public String getRoleId()
	{
		return rid;
	}
	public void setRoleId(String rid)
	{
		this.rid=rid;
	}
	public String getRealmid() {
		return realmid;
	}

	public void setRealmid(String r) {
		this.realmid = r;
	}
	public String getSingular()
	{
		return singular;
	}
	public void setSingular(String s)
	{
		this.singular=s;
	}
	public String getPlural()
	{
		return plural;
	}
	public void setPlural(String p)
	{
		this.plural=p;
	}
	public String getSingularNew()
	{
		return singularNew;
	}
	public void setSingularNew(String s)
	{
		this.singularNew=s;
	}
	public String getPluralNew()
	{
		return pluralNew;
	}
	public void setPluralNew(String p)
	{
		this.pluralNew=p;
	} 
}
