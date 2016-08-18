/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/api/src/main/java/org/sakaiproject/conditions/api/ConditionTemplateSet.java $
 * $Id: ConditionTemplateSet.java 26198 2011-04-08 19:28:14Z jcrodriguez $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.conditions.api;

import java.util.Set;

/**
 * A <code>Set</code> of <code>ConditionTemplate</code>, decorated with methods for getting
 * an id and a display name
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public interface ConditionTemplateSet {
	
	/**
	 * get the wrapped <code>Set</code> of <code>ConditionTemplate</code>s
	 * @return
	 */
	public Set<ConditionTemplate> getConditionTemplates();
	
	/**
	 * get a unique name for this <code>Set</code>
	 * @return
	 */
	public String getId();
	
	/**
	 * get a display-friendly name for this <code>Set</code>
	 * @return
	 */
	public String getDisplayName();

}
