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

package com.rsmart.customer.integration.processor;

/**
 * Database Processor Interface
 * 
 * @author dhelbert
 * @version $Revision$ $Date$
 */
public interface DatabaseProcessor extends DataProcessor {

	public void setTableName( String tableName );
	
	public void setUrl( String url );
	
	public void setDriver( String driver );
	
	public void setUsername( String username );
	
	public void setPassword( String password );
}
