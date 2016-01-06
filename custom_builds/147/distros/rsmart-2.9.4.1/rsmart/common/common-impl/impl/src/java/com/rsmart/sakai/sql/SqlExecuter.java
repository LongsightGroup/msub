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

package com.rsmart.sakai.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Aug 2, 2007
 * Time: 10:46:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlExecuter {

   private static final Log log = LogFactory.getLog(SqlExecuter.class);

   private SqlService sqlService;
   private boolean autoDdl;
   private String scriptName;

  public void init() {
     if (autoDdl && (sqlService != null)) {
        if (log.isInfoEnabled()) log.info("About to call sqlService.ddl with " + scriptName);
        sqlService.ddl(this.getClass().getClassLoader(), scriptName);
     }
  }

  public void setSqlService(SqlService sqlService) {
     this.sqlService = sqlService;
  }

  public void setAutoDdl(boolean autoDdl) {
     this.autoDdl = autoDdl;
  }


    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
}
