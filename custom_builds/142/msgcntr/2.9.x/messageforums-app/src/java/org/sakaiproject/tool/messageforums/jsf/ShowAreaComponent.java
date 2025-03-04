/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-app/src/java/org/sakaiproject/tool/messageforums/jsf/ShowAreaComponent.java $
 * $Id: ShowAreaComponent.java 13692 2009-04-24 16:14:32Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.jsf;

import javax.faces.component.UIComponentBase;

/**
 * @author Chen Wen
 * @version $Id: ShowAreaComponent.java 13692 2009-04-24 16:14:32Z jbush $
 * 
 */
public class ShowAreaComponent extends UIComponentBase
{
	public ShowAreaComponent()
	{
		super();
		this.setRendererType("ShowAreaRender");
	}

	public String getFamily()
	{
		return "ShowArea";
	}
}



