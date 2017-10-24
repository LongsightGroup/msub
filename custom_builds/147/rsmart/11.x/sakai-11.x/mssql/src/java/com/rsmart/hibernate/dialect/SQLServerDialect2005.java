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
package com.rsmart.hibernate.dialect;

import org.hibernate.dialect.SQLServerDialect;

import java.sql.Types;

/**
 * Set up nvarchar & other defaults for SQL Server 2005
 * <br>Creation Date: May 1, 2006
 *
 * @author Mike DeSimone, mike.[at].rsmart.com
 * @version $Revision$
 */
public class SQLServerDialect2005 extends SQLServerDialect {

   public SQLServerDialect2005() {
      super();
      registerColumnType( Types.CHAR, "nchar(1)" );

      // 8000 limit, but nvarchar takes 2 bytes per character
      registerColumnType( Types.VARCHAR, 4000, "nvarchar($l)" );
      registerColumnType( Types.VARCHAR, "nvarchar(max)" );

      registerColumnType( Types.VARBINARY, 8000, "varbinary($l)" );
      registerColumnType( Types.VARBINARY, "varbinary(max)" );

      registerColumnType( Types.BLOB, "varbinary(max)" );
      registerColumnType( Types.CLOB, "nvarchar(max)" );
   }
}
/**********************************************************************************
 *
 * $Header:  $
 *
 **********************************************************************************/