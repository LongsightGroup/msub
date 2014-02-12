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

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

/**
 * SelectorTag: (Comment by kimsooil@bu.edu) don't know what's for
 * 
 * @author sgithens
 *
 */
public class SelectorTag extends UIComponentTag {
	public String getRendererType() { return null; }
	public String getComponentType() { return "MailtoolSelector"; }
	
	private String value = null;
	public void setValue(String value) { this.value = value; }
	
	public void setProperties(UIComponent component) {
		super.setProperties(component);
		
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ValueBinding vb = app.createValueBinding(value);
		component.setValueBinding("value", vb);
	}
	
	public void release() {
		super.release();
		value = null;
	}
}
