/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.impl.conversion.impl;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 *
 */
public class SchemaConversionDriver
{
	private static final Log log = LogFactory.getLog(SchemaConversionDriver.class);
	
	private Properties p;
	private String base;

	public SchemaConversionDriver() {
		
	}
	public void load(Properties p, String base) {
		this.p = p;
		this.base = base;
	}
	
	public String getHandler() {
		return p.getProperty(base);
	}
	public String getHandlerClass() {
		return p.getProperty(base+".handler.class");
	}
	/**
	 * An SQL statement to select the next list of items to process, It should
	 * select these from the Register table, in such a way as to ensure that
	 * they are only selected by the current node. it will take the first column
	 * returned as the unique id of the item eg select id from
	 * migrate_content_collection where status = 'pending';
	 * 
	 * @return
	 */
	public String getSelectNextBatch() {
		return p.getProperty(base+".select.next.batch");
	}

	/**
	 * SQL to mark the id as being worked on in the register table eg eg update
	 * migrate_content_collection set status = 'locked' where id = ?;
	 * 
	 * @return
	 */
	public String getMarkNextBatch() {
		return p.getProperty(base+".mark.next.batch");

	}

	/**
	 * SQL to mark the is as completed in the register table eg parameter 1 is
	 * the ID update migrate_content_collection set status = 'completed' where
	 * id = ?;
	 * 
	 * @return
	 */
	public String getCompleteNextBatch() {
		return p.getProperty(base+".complete.next.batch");
	}

	/**
	 * SQL to select the record form the table to be converted, colums are
	 * passed for processing to getSource select * from content_collection where
	 * collection_id = ?;
	 * 
	 * @return
	 */
	public String getSelectRecord() {
		return p.getProperty(base+".select.record");
	}

	/**
	 * SQL to select the final set of objects for validation processing
	 * @return
	 */
	public String getSelectValidateRecord()
	{
		return p.getProperty(base+".select.validate.record");
	}

	/**
	 * SQL to Update the target record after conversion, the prepared statement
	 * is passed to convert source for polulating eg update content_collection
	 * set xml = ? where collection_id = ?
	 * 
	 * @return
	 */
	public String getUpdateRecord() {
		return p.getProperty(base+".update.record");
	}

	/**
	 * SQL to drop the migration regisgter eg drop table
	 * migrate_content_collection
	 * 
	 * @return
	 */
	public String[] getDropMigrateTable()
	{
		return this.getMultiValuedProperty(".drop.migrate.table");
	}
	
	/**
	 * SQL to check if the migration register exists and has been populated with
	 * pendign records column 1 should be 0 if this is not the case select
	 * count(*) from migrate_content_collection;
	 * 
	 * @return
	 */
	public String getCheckMigrateTable() {
	return p.getProperty(base+".check.migrate.table");
	}

	/**
	 * SQL to populate the migration table with ID's in the correct state.
	 * insert into migrate_content_collection (id, status) select collection_id,
	 * 'pending' from content_collection
	 * 
	 * @return
	 */
	public String[] getCreateMigrateTable()
	{
		return getMultiValuedProperty(".create.migrate.table");
	}

	protected String[] getMultiValuedProperty(String key)
    {
		String[] sql = null;
	    int count = 0;
		String countStr = p.getProperty(base + key  + ".count");
		if(countStr == null || countStr.trim().equals(""))
		{
			count = 0;
		}
		else
		{
			try
			{
				count = Integer.parseInt(countStr);
			}
			catch (Exception e)
			{
				count = 0;
				log.warn(this + ":getMultiValuedProperty " + e.getMessage());
			}
		}
		if(count <= 0)
		{
			String stmt = p.getProperty(base + key);
			if(stmt != null && ! stmt.trim().equals(""))
			{
				sql = new String[]{ stmt.trim() };
			}
		}
		else
		{
			sql = new String[count];
			for(int i = 0; i < count; i++)
			{
				sql[i] = new String(p.getProperty(base + key + "." + i));
			}
		}

	    return sql;
    }
	
	/**
	 * SQL to populate the migration table with ID's in the correct state.
	 * insert into migrate_content_collection (id, status) select collection_id,
	 * 'pending' from content_collection
	 * 
	 * @return
	 */
	public String getPopulateMigrateTable()
	{
		return p.getProperty(base + ".populate.migrate.table");
	}
	
	/**
	 * An array of strings identifying the names of columns that need to be added before the conversion can proceed.
	 * Null if no new columns need to be added.
	 * @return
	 */
	public String[] getNewColumnNames() {
		String nameStr = p.getProperty(base + ".new.columns.names");
		//System.out.println(base + ".new.columns.names == " + nameStr);
		String[] names = null;
		if(nameStr != null && ! nameStr.trim().equals(""))
		{
			names = nameStr.split(",");
		}
		//System.out.println(base + ".new.columns.names == " + names);
		return names;
	}
	
	/**
	 * An array of strings identifying the datatypes of columns that need to be added before the conversion can proceed.
	 * Each element in the array returned by getNewColumnNames() has a corresponding datatype in this array at the
	 * same index as in the array returned by getNewColumnNames(). The return value may be null if no new columns need 
	 * to be added, in which case getNewColumnNames() returns null.
	 * 
	 * @return
	 */
	public String[] getNewColumnTypes() {
		String typeStr = p.getProperty(base + ".new.columns.types");
		//System.out.println(base + ".new.columns.types == " + typeStr);
		String[] types = null;
		if(typeStr != null && ! typeStr.trim().equals(""))
		{
			types = typeStr.split(",");
		}
		//System.out.println(base + ".new.columns.types == " + types);
		return types;
	}
	
	/**
	 * An array of strings identifying the qualifiers for columns that need to be added before the conversion can proceed.
	 * For example, a qualifier might be the SQL to specify a default value or some other declaration made at the time a 
	 * column is created. Each element in the array returned by getNewColumnNames() has a corresponding entry in this array 
	 * at the same index as in the array returned by getNewColumnNames(). The qualifier for any or all columns may be a 
	 * string containing single space character if the column has no qualifier. The return value may be null if no new columns 
	 * need to be added, in which case getNewColumnNames() returns null.
	 * 
	 * @return
	 */
	public String[] getNewColumnQualifiers() {
		String qualifierStr = p.getProperty(base + ".new.columns.qualifiers");
		//System.out.println(base + ".new.columns.qualifiers == " + qualifierStr);
		String[] qualifiers = null;
		if(qualifierStr != null && ! qualifierStr.trim().equals(""))
		{
			qualifiers = qualifierStr.split(",");
		}
		//System.out.println(base + ".new.columns.qualifiers == " + qualifiers);
		return qualifiers;
	}
	
	public String getTestNewColumn(String name) {
		String sql = p.getProperty(base + ".new.columns.test");
		sql = sql.replaceAll("<name>", name);
		return sql;
	}
	
	public String getAddNewColumn(String name, String type, String qualifier) {
		String sql = p.getProperty(base + ".new.columns.add");
		sql = sql.replaceAll("<name>", name).replaceAll("<type>", type).replaceAll("<qualifier>", qualifier);
		return sql;
	}

	public String getSelectDuplicates()
	{
		return p.getProperty(base+".select.duplicates");
	}
	
	public String getRemoveDuplicates()
	{
		return p.getProperty(base+".remove.duplicates");
	}

}
