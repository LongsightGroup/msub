/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.mailtool;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * @author sgithens
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectorComponent extends UIOutput {

	public SelectorComponent() {
		
	}
	
	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		String clientId = getClientId(context);
		
		writer.startElement("div", this);
		writer.writeAttribute("id", "selectdiv", "");
		writer.endElement("div");
		
	}
	
	 
	public void decode(FacesContext context, UIComponent component) {
		String clientId = component.getClientId(context);
		Map requestParameterMap = context.getExternalContext()
									.getRequestParameterMap();
		//logger.debug("SWG:inside Decode");
		for (Iterator i = requestParameterMap.keySet().iterator(); i.hasNext();)
		{
			String key = (String) i.next();
			//logger.debug("SWG:decode key: " + key + " value: " + requestParameterMap.get(key));
			
		}
		UIInput comp = (UIInput) component;
		comp.setSubmittedValue(comp);
	}
}
