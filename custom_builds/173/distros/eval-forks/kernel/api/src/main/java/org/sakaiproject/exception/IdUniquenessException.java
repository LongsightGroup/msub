/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai-kernel/rsmart-cle/api/src/main/java/org/sakaiproject/exception/IdUniquenessException.java $
 * $Id: IdUniquenessException.java 26198 2011-04-08 19:28:14Z jcrodriguez $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.exception;

/**
 * <p>
 * IdUniquenessException is thrown whenever a method that is attempting to find a unique resource id as a variation on a requested resource id cannot find a unique id before a specified limit is reached.
 * </p>
 * <p>
 * A string indicating the final attempt to find a unique id is available as part of the exception.
 * </p>
 */
public class IdUniquenessException extends Exception
{
	private String m_id = null;

	public IdUniquenessException(String id)
	{
		m_id = id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}

	public String getMessage()
	{
		return m_id;
	}

}
