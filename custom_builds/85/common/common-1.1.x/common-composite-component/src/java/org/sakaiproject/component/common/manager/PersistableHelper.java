/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.common.manager;

import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.manager.Persistable;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
public class PersistableHelper
{
	private static final String SYSTEM = "SYSTEM";

	private static final Log LOG = LogFactory.getLog(PersistableHelper.class);

	private static final String LASTMODIFIEDDATE = "lastModifiedDate";

	private static final String LASTMODIFIEDBY = "lastModifiedBy";

	private static final String CREATEDDATE = "createdDate";

	private static final String CREATEDBY = "createdBy";

	private SessionManager sessionManager; // dep inj

	public void modifyPersistableFields(Persistable persistable)
	{
		Date now = new Date(); // time sensitive
		if (LOG.isDebugEnabled())
		{
			LOG.debug("modifyPersistableFields(Persistable " + persistable + ")");
		}
		if (persistable == null) throw new IllegalArgumentException("Illegal persistable argument passed!");

		try
		{
			String actor = getActor();

			PropertyUtils.setProperty(persistable, LASTMODIFIEDBY, actor);
			PropertyUtils.setProperty(persistable, LASTMODIFIEDDATE, now);
		}
		catch (Exception e)
		{
			LOG.error(e);
			throw new Error(e);
		}
	}

	public void createPersistableFields(Persistable persistable)
	{
		Date now = new Date(); // time sensitive
		if (LOG.isDebugEnabled())
		{
			LOG.debug("modifyPersistableFields(Persistable " + persistable + ")");
		}
		if (persistable == null) throw new IllegalArgumentException("Illegal persistable argument passed!");

		try
		{
			String actor = getActor();

			PropertyUtils.setProperty(persistable, LASTMODIFIEDBY, actor);
			PropertyUtils.setProperty(persistable, LASTMODIFIEDDATE, now);
			PropertyUtils.setProperty(persistable, CREATEDBY, actor);
			PropertyUtils.setProperty(persistable, CREATEDDATE, now);
		}
		catch (Exception e)
		{
			LOG.error(e);
			throw new Error(e);
		}
	}

	private String getActor()
	{
		LOG.debug("getActor()");

		String actor = null;
		Session session = sessionManager.getCurrentSession();
		if (session != null)
		{
			actor = session.getUserId();
		}
		else
		{
			return SYSTEM;
		}
		if (actor == null || actor.length() < 1)
		{
			return SYSTEM;
		}
		else
		{
			return actor;
		}
	}

	/**
	 * Dependency injection.
	 * 
	 * @param sessionManager
	 *        The sessionManager to set.
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSessionManager(SessionManager " + sessionManager + ")");
		}

		this.sessionManager = sessionManager;
	}
}
