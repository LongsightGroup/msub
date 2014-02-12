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

import javax.faces.model.DataModel;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

/**
 * FoothillSelector, it's renamed to "Scrolling list" (commented by kimsooil@bu.edu)
 * 
 * @author sgithens
 *
 */
public class FoothillSelector implements RecipientSelector {
	List /* EmailGroup */ m_initialgroups = null;
	int m_poscount = 0;
	
	public class ListboxEntry
	{
		int m_position;
		EmailUser m_user = null;
		String m_label = "";
		boolean m_selected = false;
		
		public ListboxEntry(int position, EmailUser user)
		{
			m_position = position;
			m_user = user;
			m_label = user.getDisplayname();
		}
		
		public int getPosition()
		{
			return m_position;
		}
		
		public String getLabel()
		{
			return m_label;
		}
		
		public EmailUser getEmailUser()
		{
			return m_user;
		}
		
		public void setSelected(boolean value)
		{
			m_selected = value;
		}
		
		public boolean isSelected()
		{
			return m_selected;
		}
	}
	
	
	public class ListboxGroup
	{
		String m_label = "";
		List /* ListboxEntry */ m_entries = new ArrayList();
		EmailGroup m_emailgroup = null;
		
		public String getLabel() { return m_label; }
		public EmailGroup getEmailGroup() { return m_emailgroup; }
		public List /* ListboxEntry */ getListboxEntries() { return m_entries; }
		
		
		public ListboxGroup(EmailGroup group)
		{
			m_emailgroup = group;
			m_label = group.getRolePlural();
			
			String rtype=group.getEmailrole().roletype;
			if (rtype.equals("group")){
				m_label += "(G)";
			}
			else if (rtype.equals("section")){
				m_label += "(S)";
			}
			
			for (Iterator i = group.getEmailusers().iterator(); i.hasNext();)
			{
				EmailUser user = (EmailUser) i.next();
				ListboxEntry entry = new ListboxEntry(m_poscount, user);
				m_entries.add(entry);
				m_poscount++;
			}
		}
	}
	
	
	/* This is going to be a List of Lists of type ListboxGroup */
	List m_listboxGroups = new ArrayList();
	
	public FoothillSelector()
	{
		
	}
	
	public void populate(List emailgroups) {
		m_initialgroups = emailgroups;
		m_listboxGroups.clear();
		
		for (Iterator i = emailgroups.iterator(); i.hasNext();)
		{
			EmailGroup group = (EmailGroup) i.next();
			ListboxGroup listgroup = new ListboxGroup(group);
			m_listboxGroups.add(listgroup);
		}
		
	}

	public List /* EmailUser */ getSelectedUsers() 
	{
		List users = new ArrayList();
		
		for (Iterator i = this.m_listboxGroups.iterator(); i.hasNext();)
		{
			ListboxGroup group = (ListboxGroup) i.next();
			for (Iterator k = group.getListboxEntries().iterator(); k.hasNext();)
			{
				ListboxEntry entry = (ListboxEntry) k.next();
				if (entry.isSelected() == true)
				{
					users.add(entry.getEmailUser());
				}
			}
		}
		
		
		return users;
	}

	public List getSelectedUsersByGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	/** Methods for JSP Fragment **/
	public List /* SelectItemGroup */ getListbox()
	{
		List selectItemGroups = new ArrayList();
		
		for (Iterator i = this.m_listboxGroups.iterator(); i.hasNext();)
		{
			List itemsingroup = new ArrayList();
			ListboxGroup group = (ListboxGroup) i.next();
			
			SelectItemGroup siGroup = new SelectItemGroup();
			siGroup.setValue(group.getLabel()); siGroup.setLabel(group.getLabel());
			
			SelectItem[] /* SelectItems */ selectitems = new SelectItem[group.getListboxEntries().size()];
			
			for (int j = 0; j < group.getListboxEntries().size(); j++)
			{
				ListboxEntry entry = (ListboxEntry) group.getListboxEntries().get(j);
				SelectItem item = new SelectItem();
				item.setLabel(entry.getLabel());
				item.setValue(Integer.toString(entry.getPosition()));
				selectitems[j] = item;
				
			}
			
			siGroup.setSelectItems(selectitems);
			
			selectItemGroups.add(siGroup);
		}
		
		
		return selectItemGroups;
	}

	public List /* String */ getSelectedItems()
	{
		List items = new ArrayList();
		
		for (Iterator i = m_listboxGroups.iterator(); i.hasNext();)
		{
			ListboxGroup group = (ListboxGroup) i.next();
			for (Iterator j = group.getListboxEntries().iterator(); j.hasNext();)
			{
				ListboxEntry entry = (ListboxEntry) j.next();
				if (entry.isSelected() == true)
				{
					items.add(Integer.toString(entry.getPosition()));
				}
			}
		}
		
		return items;
	}
	
	public void setSelectedItems(List /* ? */ items)
	{
		
		Iterator i = items.iterator();
		String next;
		if (i.hasNext() == true)
			next = (String) i.next();
		else
			return;
		
		for (Iterator j = this.m_listboxGroups.iterator(); j.hasNext();)
		{
			ListboxGroup group = (ListboxGroup) j.next();
			for (Iterator k = group.getListboxEntries().iterator(); k.hasNext();)
			{
				ListboxEntry entry = (ListboxEntry) k.next();
				if (next.equals(Integer.toString(entry.getPosition())))
				{
					entry.setSelected(true);
					if (i.hasNext() == true)
						next = (String) i.next();
					else
						return;
				}
				else
					entry.setSelected(false);
				
			}
		}
		
	}
	
	
	public List /* String */ getCheckedRoles()
	{
		return new ArrayList();
	}
	
	public void setCheckedRoles(List /* ? */ items)
	{
	
	}
	
	public List /* SelectItem */ getRolesChecklist()
	{
		List selectItems = new ArrayList();
		
		for (Iterator i = this.m_initialgroups.iterator(); i.hasNext();)
		{
			EmailGroup e = (EmailGroup) i.next();
			SelectItem item = new SelectItem();
			item.setLabel(e.getRolePlural());
			selectItems.add(item);
		}
		
		return selectItems;
	}
	
	
	public String processSelectAll()
	{
		for (Iterator i = this.m_listboxGroups.iterator(); i.hasNext();)
		{
			ListboxGroup group = (ListboxGroup) i.next();
			for (Iterator j = group.getListboxEntries().iterator(); j.hasNext();)
			{
				ListboxEntry entry = (ListboxEntry) j.next();
				entry.setSelected(true);
			}
		}
		
		return "main";
	}
	
	public String processSelectNone()
	{
		for (Iterator i = this.m_listboxGroups.iterator(); i.hasNext();)
		{
			ListboxGroup group = (ListboxGroup) i.next();
			for (Iterator j = group.getListboxEntries().iterator(); j.hasNext();)
			{
				ListboxEntry entry = (ListboxEntry) j.next();
				entry.setSelected(false);
			}
		}
		
		return "main";
	}
	public DataModel getDataModel()
	{
		return null;
	}
}
