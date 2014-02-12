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

import java.util.List;

import javax.faces.model.DataModel;


/**
 * TreeSelector, an implementation of RecipientSelector (commented by kimsooil@bu.edu)
 * It calls SelectByTree which calls SelectByUserTable)
 * 
 * @author sgithens
 *
 */
public class TreeSelector implements RecipientSelector {
	SelectByTree m_tree = null;
	List /* EmailGroup */ m_initialgroups = null;

	public void populate(List emailgroups) {
		m_initialgroups = emailgroups;
		
		m_tree = new SelectByTree(emailgroups);
	}

	public List getSelectedUsers() {
		return m_tree.getSelectedUsers();
	}

	public List getSelectedUsersByGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	/** Methods for JSP Fragment **/
	public DataModel getDataModel()
	{
		return m_tree.getRows();
	}

}
