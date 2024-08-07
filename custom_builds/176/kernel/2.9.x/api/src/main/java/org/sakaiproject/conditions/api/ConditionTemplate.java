/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/api/src/main/java/org/sakaiproject/conditions/api/ConditionTemplate.java $
 * $Id: ConditionTemplate.java 26198 2011-04-08 19:28:14Z jcrodriguez $
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

import java.util.Map;

/**
 * An interface to define the contract for a template that can be used to produce new <code>Condition</code>s
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public interface ConditionTemplate {
	
	/**
	 * a <code>ConditionTemplate</code> must be able to produce a new <code>Condition</code> from
	 * a <code>Map</code> of parameters
	 * @param params
	 * @return
	 */
	public Condition conditionFromParameters(Map<String, String> params);

}
