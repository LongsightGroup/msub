/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.authz.api.PermissionsHelper;
import uk.org.ponder.messageutil.MessageLocator;

import uk.ac.cam.caret.sakai.rsf.helper.HelperViewParameters;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Uses the ResourcePicker to permit adding resources to the page.
 * 
 * This producer is used both for adding resources and for adding multimedia.
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * 
 */
public class PermissionsHelperProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "PermissionsHelper";

	public String getViewID() {
		return VIEW_ID;
	}

	private SimplePageBean simplePageBean;

        public void setSimplePageBean(SimplePageBean bean) {
	    simplePageBean = bean;
	}

	// helper tool
	public static final String HELPER = "sakai.permissions.helper";

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;

	public void setSiteService(SiteService service) {
		siteService = service;
	}

	private ToolManager toolManager;

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	private MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		if (!simplePageBean.canEditPage() && !simplePageBean.canEditSite())
		    return;

		// this is purely a site config, so no permission other than caneditpage needed

		// parameters for helper
		ToolSession session = sessionManager.getCurrentToolSession();
		Site site = null;

                try {
		    site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
                } catch (Exception impossible) {
		    impossible.printStackTrace();
                }

		session.setAttribute(PermissionsHelper.TARGET_REF, site.getReference());
		session.setAttribute(PermissionsHelper.DESCRIPTION, messageLocator.getMessage("simplepage.editpermissions") + " " +  site.getTitle());
		session.setAttribute(PermissionsHelper.PREFIX, "lessonbuilder.");

		UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
		UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);
	}

	public List reportNavigationCases() {
		List l = new ArrayList();
		l.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return l;
	}

	public ViewParameters getViewParameters() {
		return new HelperViewParameters();
	}
}
