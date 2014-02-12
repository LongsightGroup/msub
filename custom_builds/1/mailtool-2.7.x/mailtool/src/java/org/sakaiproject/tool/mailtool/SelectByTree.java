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
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.PhaseId;

/**
 * SelectByTree, it's renamed to "Users by Role" (commented by kimsooil@bu.edu)
 * 
 * @author sgithens
 *
 */
public class SelectByTree {
	public class TableEntry
	{
		private EmailGroup m_emailgroup = null; /* Keep this around for good measure */
		private boolean m_collapsed = true;
		private EmailRole m_emailrole = null;
		private SelectByUserTable m_usertable = null;
		
		/* JSF Widgets */
		private boolean m_allSelected = false;
		private boolean m_groupAware=false;
		
		public TableEntry(EmailGroup emailgroup)
		{
			m_emailgroup = emailgroup;
			m_emailrole = emailgroup.getEmailrole();
			m_usertable = new SelectByUserTable(emailgroup.getEmailusers());
		}
		
		public DataModel getUserTable()
		{
			return m_usertable.getUserRows();
		}
		
		public String getPluralRolename()
		{
			return m_emailrole.getRoleplural();
		}
		public String getSingularRolename()
		{
			return m_emailrole.getRolesingular();
		}

		public boolean isCollapsed()
		{
			return m_collapsed;
		}
		
		public void setCollapsed(boolean collapsed)
		{
			m_collapsed = collapsed;
		}
		
		public void actionExpand(ActionEvent event)
		{
			m_collapsed = false;
		}
		
		public void actionCollapse(ActionEvent event)
		{
			m_collapsed = true;
		}
		
		public void setAllSelected(boolean value) {
			m_allSelected = value;
		}
		public void processSelectAll(ValueChangeEvent event)
		{
			PhaseId phaseId = event.getPhaseId();
			if (phaseId.equals(PhaseId.ANY_PHASE))
			{
			event.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
			event.queue();
			}
			else if (phaseId.equals(PhaseId.UPDATE_MODEL_VALUES))
			{
//			 do you method here
				boolean allornot = ((Boolean) event.getNewValue()).booleanValue();
				setAllSelected(allornot);
				m_usertable.switchSelections(allornot);
//				FacesContext.getCurrentInstance().renderResponse();

			}
		}
		
		public boolean isAllSelected() { return m_allSelected; }

		public boolean isGroupAware() {
			return m_groupAware;
		}

		public void setGroupAware(boolean aware) {
			m_groupAware = aware;
		}
	}

//	protected List /* TableEntry */ m_tablerows = new ArrayList();
	public List /* TableEntry */ m_tablerows = new ArrayList();
	
	public SelectByTree(List /* EmailGroup */ groups)
	{
		m_tablerows.clear();
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			EmailGroup egroup = (EmailGroup) i.next();
			TableEntry te = new TableEntry(egroup);
			
			//te.m_groupAware = egroup.getRolePlural().equals("Students") ? true : false;			
			m_tablerows.add(te);
		}
	}
	
	public DataModel getRows()
	{
		DataModel returnmodel = new ListDataModel();
		returnmodel.setWrappedData(m_tablerows);
		return returnmodel;
	}
	
	public List /* EmailUser */ getSelectedUsers()
	{
		List /* EmailUser */ selectedusers = new ArrayList();
		
		for (Iterator i = m_tablerows.iterator(); i.hasNext();)
		{
			TableEntry te = (TableEntry) i.next();
//			if ((te.isCollapsed() == true) && (te.isAllSelected() == true))
			if (te.isAllSelected() == true)
			{
				selectedusers.addAll(te.m_emailgroup.getEmailusers());
			}
			else
			{
				selectedusers.addAll(te.m_usertable.getSelectedUsers());
			}
		}
		
		return selectedusers;
	}
}
