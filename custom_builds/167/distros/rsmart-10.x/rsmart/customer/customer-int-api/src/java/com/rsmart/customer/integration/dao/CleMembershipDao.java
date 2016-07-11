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

package com.rsmart.customer.integration.dao;

import java.util.List;

import com.rsmart.customer.integration.model.CleMembership;

/**
 * CLE Membership Dao
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public interface CleMembershipDao {

	/**
	 * Save
	 * 
	 * @param mem
	 * @return String
	 */
	public String saveCleMembership(CleMembership mem);

	/**
	 * Update
	 * 
	 * @param mem
	 */
	public void updateCleMembership(CleMembership mem);

	/**
	 * Delete
	 * 
	 * @param mem
	 */
	public void deleteCleMembership(CleMembership mem);

	/**
	 * Load
	 * 
	 * @param id
	 * @return
	 */
	public CleMembership loadCleMembership(String id);

	/**
	 * 
	 * @param courseNum
	 * @return
	 */
	public List findCleMembership(String courseNum);
	
	/**
	 * List Sections
	 * 
	 * @return
	 */
	public List listSections();
	
	/**
	 * Load All
	 * 
	 * @return List
	 */
	public List loadAll();
	
	/**
	 * Delete All
	 * 
	 * @return int
	 */
	public int deleteAll();
}
