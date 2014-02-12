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

package org.sakaiproject.tool.assessment.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;

public class PublishedItemService extends ItemService {
	private static Log log = LogFactory.getLog(PublishedItemService.class);

	public ItemFacade getItem(Long itemId, String agentId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId, agentId);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}

		return item;
	}
	
	public ItemFacade getItem(String itemId) {
		PublishedItemFacade item = null;
		try {
			item = PersistenceService.getInstance()
					.getPublishedItemFacadeQueries().getItem(itemId);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}

		return item;
	}

	public void deleteItemContent(Long itemId, String agentId) {
		try {
			PersistenceService.getInstance().getPublishedItemFacadeQueries()
					.deleteItemContent(itemId, agentId);
		} catch (Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
}
