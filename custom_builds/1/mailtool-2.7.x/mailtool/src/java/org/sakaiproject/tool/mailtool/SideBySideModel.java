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

import javax.faces.model.SelectItem;

/**
 * SideBySideModel, just slightly modified for group-awareness by kimsooil@bu.edu
 * 
 * @author sgithens
 *
 */
public class SideBySideModel {
	
	/** This will be the 'object' we hook up to in
	 * 
	 *         <h:selectManyListbox value="ListBoxEntry"
	 */
	public class ListboxEntry
	{
		/* This could be a EmailGroup, or EmailUser, or
		 * String for a dummy divider
		 */
		int m_position;
		
		Object m_data = null;
		String m_label = "";
		
		boolean m_sourceSelected = false;
		boolean m_sinkSelected = false;
		boolean m_divider = false;
		boolean m_isgroup = false;
		boolean m_isuser = false;
		
		
		public int getPosition()
		{
			return m_position;
		}
		
		public ListboxEntry(int position)
		{
			m_position = position;
		}
		
		public String getLabel()
		{
			return m_label;
		}
		
		public void setData(EmailGroup group)
		{
			m_data = group;
			m_label = "All " + group.getEmailrole().getRoleplural();
			
			String rtype = group.getEmailrole().roletype;

			if (rtype.equals("group")){ m_label +="(G)";}
			else if (rtype.equals("section")) {m_label += "(S)";}

	//		String rolename=group.getEmailrole().getRoleplural();
	//		m_label +=(rolename.equals("maintain") || rolename.equals("access")) ? " Users" : "s";
			m_isgroup = true;
		}
		
		public void setData(EmailUser user)
		{
			m_data = user;
			m_label = user.getDisplayname();
			m_isuser = true;
		}
		
		public void setDataAsDivider()
		{
			m_data = "divider";
			m_label = "-------------";
			m_divider = true;
		}

		public void setDataAsDivider(String more)
		{
			m_data = "divider";
			m_label = "-------------"+more;
			m_divider = true;
		}
		
		public boolean isDivider()
		{
			return m_divider;
		}
		
		public boolean isGroup()
		{
			return m_isgroup;
		}
		
		public boolean isUser()
		{
			return m_isuser;
		}
		
		public Object getData()
		{
			return m_data;
		}
		
		public void setSourceSelected(boolean selected)
		{
			m_sourceSelected = selected;
		}
		
		public boolean isSourceSelected()
		{
			return m_sourceSelected;
		}
		
		public void setSinkSelected(boolean selected)
		{
			m_sinkSelected = selected;
		}
		
		public boolean isSinkSelected()
		{
			return m_sinkSelected;
		}
		
	}
	
	
	
	
	protected List /* ListboxEntry */ m_listboxdata = null;
	
	public List /* EmailUser */ getSelectedUsers()
	{
		List users = new ArrayList();
		
		for (Iterator i = m_listboxdata.iterator(); i.hasNext();)
		{
			ListboxEntry entry = (ListboxEntry) i.next();
			if ((entry.isGroup() == true) && (entry.isSinkSelected() == true))
			{
				EmailGroup group = (EmailGroup) entry.getData();
				users.addAll(group.getEmailusers());
			}
			else if ((entry.isUser() == true) && (entry.isSinkSelected() == true))
			{
				users.add(entry.getData());
			}
		}
		
		
		return users;
	}
	
	public SideBySideModel(List /* EmailGroup */ groups)
	{
		m_listboxdata = new ArrayList();
		
		int count = 0;
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			EmailGroup group = (EmailGroup) i.next();
			ListboxEntry entry = new ListboxEntry(count);
			entry.setData(group);
			entry.setSourceSelected(true);
			m_listboxdata.add(entry);
			count++;
		}
/*		
		ListboxEntry diventry = new ListboxEntry(count);
		diventry.setDataAsDivider();
		diventry.setSourceSelected(true);
		m_listboxdata.add(diventry);
		count++;
*/		
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			EmailGroup group = (EmailGroup) i.next();

			String rtype = group.getEmailrole().roletype;
			String rname = group.getEmailrole().getRolesingular();
			ListboxEntry diventry = new ListboxEntry(count);
			diventry.setDataAsDivider("["+rname+"]");
			diventry.setSourceSelected(true);
			m_listboxdata.add(diventry);
			count++;


			List users = group.getEmailusers();
			for (Iterator j = users.iterator(); j.hasNext();)
			{
				EmailUser user = (EmailUser) j.next();
				ListboxEntry entry = new ListboxEntry(count);
				entry.setData(user);
				entry.setSourceSelected(true);
				m_listboxdata.add(entry);
				count++;
				
			}
/*			
			if (i.hasNext() == true)
			{
				ListboxEntry entry = new ListboxEntry(count);
				entry.setDataAsDivider();
				entry.setSourceSelected(true);
				m_listboxdata.add(entry);
				count++;
			}
*/			
		}
	}
	
	public List /* SelectItem */ getSourceSelectItems()
	{
		List items = new ArrayList();
		
		for (Iterator i = m_listboxdata.iterator(); i.hasNext();)
		{
			ListboxEntry entry = (ListboxEntry) i.next();
			if (entry.isSourceSelected() == true)
			{
				SelectItem item = new SelectItem();
				item.setLabel(entry.getLabel());
				item.setValue(Integer.toString(entry.getPosition()));
				
				items.add(item);
			}
		}
		
		return items;
	}
	
	public List /* SelectItem */ getSinkSelectItems()
	{
		List items = new ArrayList();
		
		for (Iterator i = m_listboxdata.iterator(); i.hasNext();)
		{
			ListboxEntry entry = (ListboxEntry) i.next();
			if (entry.isSinkSelected() == true)
			{
				SelectItem item = new SelectItem();
				item.setLabel(entry.getLabel());
				item.setValue(Integer.toString(entry.getPosition()));
				
				items.add(item);
			}
		}
		
		return items;
	}
	
	protected List m_sinkSelectedObjects = new ArrayList();
	public void setSinkSelectedObjects(List /* String */ items)
	{
		m_sinkSelectedObjects = items;
	}
	
	protected List m_sourceSelectedObjects = new ArrayList();
	public void setSourceSelectedObjects(List /* String */ items)
	{
		m_sourceSelectedObjects = items; 
	}
	
	public void actionSource()
	{
		for (Iterator i = m_sourceSelectedObjects.iterator(); i.hasNext();)
		{
			Integer integer = new Integer((String) i.next());
			ListboxEntry entry = (ListboxEntry) this.m_listboxdata.get(integer.intValue());
			if (entry.isDivider() == false)
			{
				entry.setSourceSelected(false);
				entry.setSinkSelected(true);
			}
		}
	}
	
	public void actionSink()
	{
		for (Iterator i = m_sinkSelectedObjects.iterator(); i.hasNext();)
		{
			Integer integer = new Integer((String) i.next());
			ListboxEntry entry = (ListboxEntry) this.m_listboxdata.get(integer.intValue());
			entry.setSourceSelected(true);
			entry.setSinkSelected(false);
		}
	}
}
