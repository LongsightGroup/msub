/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/polls/tool/src/java/org/sakaiproject/poll/tool/producers/PermissionsProducer.java $
 * $Id: PermissionsProducer.java 23942 2010-12-09 16:48:03Z jbush $
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

package org.sakaiproject.poll.tool.producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolSession;

import uk.ac.cam.caret.sakai.rsf.helper.HelperViewParameters;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class PermissionsProducer implements ViewComponentProducer,NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "votePermissions";



	
	private MessageLocator messageLocator;


	private static final String PERMISSION_PREFIX ="poll";
	
	public static final String HELPER = "sakai.permissions.helper";

	
	private static final Log LOG = LogFactory.getLog(PermissionsProducer.class);

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	public void setMessageLocator(MessageLocator messageLocator) {

		this.messageLocator = messageLocator;
	}



	

	Map<String, String> perms = null;

	public void setRoleperms(Map<String, String> perms)
	{
		this.perms = perms;
	}

	private ExternalLogic externalLogic;    
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	public Site site;


	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {

		ToolSession session = externalLogic.getCurrentToolSession();
		session.setAttribute(PermissionsHelper.TARGET_REF, site.getReference());
	    session.setAttribute(PermissionsHelper.DESCRIPTION, messageLocator.getMessage("set.perms", new Object[]{site.getTitle()}));
	    session.setAttribute(PermissionsHelper.PREFIX, PERMISSION_PREFIX + ".");
	    
	    List<String> perms = externalLogic.getPermissionKeys();
	    HashMap<String, String> pRbValues = new HashMap<String, String>();
	    for (int i = 0 ; i < perms.size(); i++) {
	    	String perm = perms.get(i);
	    	String descr = messageLocator.getMessage("desc-" + perm);
	    	pRbValues.put("desc-" + perm, descr);
	    }
	    
	    session.setAttribute("permissionDescriptions",  pRbValues);
	    UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
	    UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "", null);



	}

	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		togo.add(new NavigationCase(null, new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("Success", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		togo.add(new NavigationCase("Cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		return togo;
	}

	public ViewParameters getViewParameters() {
		return new HelperViewParameters();
	}	

}
