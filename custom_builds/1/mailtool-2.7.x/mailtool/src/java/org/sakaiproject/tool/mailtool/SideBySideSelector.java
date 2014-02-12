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

import javax.faces.model.DataModel;


/**
 * SideBySideSelector, an implementation of RecipientSelector (commented by kimsooil@bu.edu)
 *  
 * @author sgithens
 *
 */
public class SideBySideSelector implements RecipientSelector {
	List /* emailgroups */ m_initialgroups = null;
	
	SideBySideModel m_model = null;
	
	

	public void populate(List emailgroups) {
		//logger.debug("SWG SideBySide Popsize: " + emailgroups.size());
		m_model = new SideBySideModel(emailgroups);
	}

	public List /* EmailUser */ getSelectedUsers() {
		return m_model.getSelectedUsers();
	}

	public List getSelectedUsersByGroup() {
		// TODO Auto-generated method stub
		return new ArrayList();
	}

	// Methods for JSP Fragment
	public List /* SelectItem */ getSourceListbox() 
	{    
		return m_model.getSourceSelectItems();
	}
	
	public List /* SelectItem */ getSinkListbox()
	{   
		return m_model.getSinkSelectItems();
	}
	
	public List getSourceSelected()
	{
		return m_model.getSinkSelectItems();
	}
	
	public void setSourceSelected(List /* String */ selecteditems)
	{
		m_model.setSourceSelectedObjects(selecteditems);
	}

	public List getSinkSelected()
	{
		return m_model.getSinkSelectItems();
	}
	
	public void setSinkSelected(List /* String */ selecteditems)
	{
		m_model.setSinkSelectedObjects(selecteditems);
	}
	
	public String processAddButton()
	{
		m_model.actionSource();
		return "main";
	}
	
	public String processRemoveButton()
	{
		m_model.actionSink();
		return "main";
	}
	public DataModel getDataModel()
	{
		return null;
	}
}
