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

package com.rsmart.customer.integration.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.rsmart.customer.integration.dao.CleMembershipDao;
import com.rsmart.customer.integration.model.CleMembership;

/**
 * CLE Membership Dao Impl
 * 
 * @author dhelbert
 * @revision $Revision$ $Date$
 */
public class CleMembershipDaoImpl extends HibernateTemplate implements
		CleMembershipDao {

	/**
	 * Get DAO Class
	 * 
	 * @return Class
	 */
	private Class getDaoClass() {
		return CleMembership.class;
	}

	/**
	 * Delete Membership
	 * 
	 * @param mem
	 */
	public void deleteCleMembership(CleMembership mem) {
		delete(mem);
	}

	/**
	 * Find Cle Membership
	 * 
	 * @param section
	 */
	public List findCleMembership(String courseNumber) {
		Object[] os = new Object[1];
		os[0] = courseNumber;

		List list = find(
				"select s from CleMembership as s where s.courseNumber = ?", os);

		return list;
	}

	/**
	 * List Sections
	 * 
	 * @return List
	 */
	public List listSections() {
		Connection connection = null;
		PreparedStatement stmt = null;
		Session session = getSession();
		List<String> list = new ArrayList<String>();
		
		try {
			connection = session.connection();
			stmt = connection.prepareStatement("select distinct coursenumber from clemembership");

			ResultSet rs = stmt.executeQuery();
			
			while( rs.next() ) {
				list.add( rs.getString(1) );
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		
		return list;
	}
	
	/**
	 * Delete All Membership
	 * 
	 * @return int
	 */
	public int deleteAll() {
		Connection connection = null;
		PreparedStatement stmt = null;
		Session session = getSession();
		int cnt = 0;
		
		try {
			connection = session.connection();
			stmt = connection.prepareStatement("delete from clemembership");
			cnt = stmt.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {
			}
		}
		
		return cnt;
	}

	/**
	 * Load All
	 * 
	 * @return List
	 */
	public List loadAll() {
		return loadAll(getDaoClass());
	}

	/**
	 * 
	 */
	public CleMembership loadCleMembership(String id) {
		return (CleMembership) load(getDaoClass(), id);
	}

	/**
	 * 
	 */
	public String saveCleMembership(CleMembership mem) {
		return (String) save(mem);
	}

	/**
	 * 
	 */
	public void updateCleMembership(CleMembership mem) {
		update(mem);
	}

}
