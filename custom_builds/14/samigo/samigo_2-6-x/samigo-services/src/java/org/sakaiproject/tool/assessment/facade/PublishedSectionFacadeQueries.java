/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PublishedSectionFacadeQueries extends HibernateDaoSupport
		implements PublishedSectionFacadeQueriesAPI {
	private static Log log = LogFactory.getLog(PublishedSectionFacadeQueries.class);

	  public IdImpl getId(String id) {
	    return new IdImpl(id);
	  }

	  public IdImpl getId(Long id) {
	    return new IdImpl(id);
	  }

	  public IdImpl getId(long id) {
	    return new IdImpl(id);
	  }

}
