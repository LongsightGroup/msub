/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/kernel-impl/src/main/java/org/sakaiproject/event/impl/UsageSessionServiceSqlMySql.java $
 * $Id: UsageSessionServiceSqlMySql.java 26198 2011-04-08 19:28:14Z jcrodriguez $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.event.impl;

/**
 * methods for accessing session usage data in a mysql database.
 */
public class UsageSessionServiceSqlMySql extends UsageSessionServiceSqlDefault
{

	@Override
	public String getSessionsCountSql() {
		return "select TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_NAME='SAKAI_SESSION' ORDER BY CREATE_TIME LIMIT 1;";
	}
}
