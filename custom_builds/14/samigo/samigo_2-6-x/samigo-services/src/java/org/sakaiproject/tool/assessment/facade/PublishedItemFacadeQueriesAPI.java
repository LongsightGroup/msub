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

import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public interface PublishedItemFacadeQueriesAPI {

	public IdImpl getItemId(String id);

	public IdImpl getItemId(Long id);

	public IdImpl getItemId(long id);

	public PublishedItemFacade getItem(Long itemId, String agent);
	
	public PublishedItemFacade getItem(String itemId);
	
	public void deleteItemContent(Long itemId, String agent);
}
