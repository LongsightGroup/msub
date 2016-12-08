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

package com.rsmart.customer.integration.processor.cle;

import com.rsmart.customer.integration.processor.BaseCsvFileProcessor;
import com.rsmart.customer.integration.dao.CleMembershipDao;
import com.rsmart.customer.integration.model.CleMembership;
import com.rsmart.customer.integration.processor.ProcessorState;

import java.util.Map;

/**
 * CLE Membership Processor
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public class CleMembershipProcessor extends BaseCsvFileProcessor {

	/** CLE Membership Dao */
	private CleMembershipDao cleMembershipDao;
	
	/**
	 * Get Tilte
	 * 
	 */
	public String getProcessorTitle() {
		return "CLE Membership Processor";
	}

	/**
	 * Pre Process
	 * 
	 * @throws Exception
	 */
	public void preProcess(ProcessorState state) throws Exception {
		state.setDeleteCnt( cleMembershipDao.deleteAll() );
	}

	/**
	 * Process Row
	 * 
	 * @param data
	 */
	public void processRow(String[] data, ProcessorState state) throws Exception {
		CleMembership mem = new CleMembership();
		mem.setUserName(data[0]);
		mem.setCourseNumber(data[1]);
		mem.setRole(data[2]);

      if (data.length > 3) {
         mem.setActive(Boolean.parseBoolean(data[3]));
      }

      processCleMembership(mem, state);
	}

	/**
	 * Process CLE Site Membership
	 * 
	 * @param mem
	 * @throws Exception
	 */
	private void processCleMembership(CleMembership mem, ProcessorState state) throws Exception {
		cleMembershipDao.saveCleMembership(mem);
		state.incrementInsertCnt();
	}

	/**
	 * Get member DAO
	 * 
	 * @return CleMembershipDao
	 */
	public CleMembershipDao getCleMembershipDao() {
		return cleMembershipDao;
	}

	/**
	 * Set Member DAO
	 * 
	 * @param cleMembershipDao
	 */
	public void setCleMembershipDao(CleMembershipDao cleMembershipDao) {
		this.cleMembershipDao = cleMembershipDao;
	}
}
