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

package com.rsmart.sakaiproject.tool.impl;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import com.rsmart.sakaiproject.tool.api.DbTool;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Mar 27, 2007
 * Time: 10:40:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbActiveToolDbSupport extends HibernateDaoSupport {


   //CLE-4766 - changed 'tool_id' (the DB column name) to 'toolId' in HQL
   public DbTool findTool(String id) {
      Collection tools = getHibernateTemplate().find(" from DbTool where toolId = ?", id);
      
      if (tools.size() > 0) {
         return (DbTool) tools.iterator().next();
      }
      return null;
   }

   public DbTool storeTool(DbTool dbTool) {
      getHibernateTemplate().saveOrUpdate(dbTool);
      return dbTool;
   }
}
