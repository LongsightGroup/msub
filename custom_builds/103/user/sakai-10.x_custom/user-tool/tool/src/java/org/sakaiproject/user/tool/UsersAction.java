/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.tool;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.api.UserDirectoryService.PasswordRating;
import org.sakaiproject.user.cover.AuthenticationManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.tool.PasswordPolicyHelper.TempUser;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

import au.com.bytecode.opencsv.CSVReader;

/**
 * <p>
 * UsersAction is the Sakai users editor.
 * </p>
 */
public class UsersAction extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("admin");
		
	//private static final String XLS_MIME_TYPE="application/vnd.ms-excel";
	private static final String CSV_MIME_TYPE="text/csv";
	
	//the column headings in the imported file, which will be used as the primary user attributes
	private static final String IMPORT_USER_ID="user id";
	private static final String IMPORT_FIRST_NAME="first name";
	private static final String IMPORT_LAST_NAME="last name";
	private static final String IMPORT_EMAIL="email";
	private static final String IMPORT_PASSWORD="password";
	private static final String IMPORT_TYPE="type";

	// SAK-23568
	private static final PasswordPolicyHelper pwHelper = new PasswordPolicyHelper();
	private static final String MSG_KEY_PASSWORD_WEAK = "pw.weak";
	private static final String MSG_KEY_PW_STRENGTH_INFO = "pw.strengthInfo";

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return UserDirectoryService.searchUsers(search, first, last);
		}

		return UserDirectoryService.getUsers(first, last);
	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return UserDirectoryService.countSearchUsers(search);
		}

		return UserDirectoryService.countUsers();
	}

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		PortletConfig config = portlet.getPortletConfig();

		if (state.getAttribute("single-user") == null)
		{
			state.setAttribute("single-user", new Boolean(config.getInitParameter("single-user", "false")));
			state.setAttribute("include-password", new Boolean(config.getInitParameter("include-password", "true")));
		}

		if (state.getAttribute("create-user") == null)
		{
			state.setAttribute("create-user", new Boolean(config.getInitParameter("create-user", "false")));
			state.setAttribute("create-login", new Boolean(config.getInitParameter("create-login", "false")));
		}

		if (state.getAttribute("create-type") == null)
		{
			state.setAttribute("create-type", config.getInitParameter("create-type", ""));
		}
		
		if (state.getAttribute("user.recaptcha-enabled") == null)
		{
			String publicKey = ServerConfigurationService.getString("user.recaptcha.public-key", "");
			String privateKey = ServerConfigurationService.getString("user.recaptcha.private-key", "");
			Boolean systemEnabled = ServerConfigurationService.getBoolean("user.recaptcha.enabled", false);
			Boolean toolEnabled = Boolean.parseBoolean(config.getInitParameter("user.recaptcha-enabled", "false"));
			Boolean enabled = systemEnabled && toolEnabled;
			if (enabled)
			{
				if (publicKey == null || publicKey.length() == 0)
				{
					Log.warn("chef", "recaptcha is enabled but no public key is found.");
					enabled = Boolean.FALSE;
				}
				if (privateKey == null || privateKey.length() == 0)
				{
					Log.warn("chef", "recaptcha is enabled but no private key is found.");
					enabled = Boolean.FALSE;
				}
			}
			state.setAttribute("user.recaptcha-public-key", publicKey);
			state.setAttribute("user.recaptcha-private-key", privateKey);
			state.setAttribute("user.recaptcha-enabled", enabled);
		}

	} // initState

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		boolean createUser = ((Boolean) state.getAttribute("create-user")).booleanValue();
		
		UsersActionState sstate = (UsersActionState)getState(context, rundata, UsersActionState.class);
		String status = sstate.getStatus();

		String[] userTypes = ServerConfigurationService.getStrings("user.type.selector");
		if (userTypes != null && userTypes.length > 0)
		{
			context.put("userTypes", userTypes);
		}

		// if not logged in as the super user, we won't do anything
		String template = null;

		// for the create-user create-login case, we set this in the do so we can process the redirect here
		if (state.getAttribute("redirect") != null)
		{
			state.removeAttribute("redirect");
			Session s = SessionManager.getCurrentSession();
			// TODO: Decide if this should be in "getPortalUrl"
			// I don't think so but could be convinced - /chuck
			String controllingPortal = (String) s.getAttribute("sakai-controlling-portal");
			String portalUrl = ServerConfigurationService.getPortalUrl();
			if ( controllingPortal != null ) {
				portalUrl = portalUrl + "/" + controllingPortal;
			}
 
			sendParentRedirect((HttpServletResponse) ThreadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE),
					portalUrl);
			return template;
		}

		// put $action into context for menus, forms and links
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		//put successMessage into context and remove from state
	    context.put("successMessage", state.getAttribute("successMessage"));
	    state.removeAttribute("successMessage");

		// SAK-23568
		pwHelper.addJavaScriptParamsToContext(context);

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");

		if ((singleUser) && (mode != null) && (mode.equals("edit")))
		{
			template = buildEditContext(state, context);
		}
		else if (singleUser)
		{
			String id = SessionManager.getCurrentSessionUserId();
			state.setAttribute("user-id", id);
			template = buildViewContext(state, context);
		}
		else if (createUser)
		{
			template = buildCreateContext(state, context);
		}
		else if (mode == null)
		{
			template = buildListContext(state, context);
		}
		else if (mode.equals("new"))
		{
			template = buildNewContext(state, context);
		}
		else if (mode.equals("edit"))
		{
			template = buildEditContext(state, context);
		}
		else if (mode.equals("confirm"))
		{
			template = buildConfirmRemoveContext(state, context);
		}
		else if (mode.equals("import"))
		{
			template = buildImportContext(state, context);
		}
		else if (mode.equals("mode_helper") && StringUtils.equals(status, "processImport")) {
			//returning from helper after uploading file
			template = buildProcessImportContext(state, rundata, context);
		}
		else
		{
			Log.warn("chef", "UsersAction: mode: " + mode);
			template = buildListContext(state, context);
		}

		String prefix = (String) getContext(rundata).get("template");
		return prefix + template;

	} // buildNormalContext

	/**
	 * Build the context for the main list mode.
	 */
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context
		context.put("service", UserDirectoryService.getInstance());

		// put all (internal) users into the context
		context.put("users", prepPage(state));

		// build the menu
		Menu bar = new MenuImpl();
		if (UserDirectoryService.allowAddUser())
		{
			bar.add(new MenuEntry(rb.getString("useact.newuse"), null, true, MenuItem.CHECKED_NA, "doNew"));
			bar.add(new MenuEntry(rb.getString("import.user.file"), null, true, MenuItem.CHECKED_NA, "doImport"));
		}

		// add the paging commands
		//addListPagingMenus(bar, state);
		int pageSize = Integer.valueOf(state.getAttribute(STATE_PAGESIZE).toString()).intValue();
		int currentPageNubmer = Integer.valueOf(state.getAttribute(STATE_CURRENT_PAGE).toString()).intValue();
		int startNumber = pageSize * (currentPageNubmer - 1) + 1;
		int endNumber = pageSize * currentPageNubmer;

		int totalNumber = 0;
		Object[] params;
		ArrayList list = new ArrayList();
		list.add(new Integer[]{Integer.valueOf(5)});
		list.add(new Integer[]{Integer.valueOf(10)});
		list.add(new Integer[]{Integer.valueOf(20)});
		list.add(new Integer[]{Integer.valueOf(50)});
		list.add(new Integer[]{Integer.valueOf(100)});
		list.add(new Integer[]{Integer.valueOf(200)});

		try
		{
			totalNumber = Integer.valueOf(state.getAttribute(STATE_NUM_MESSAGES).toString()).intValue();
		}
		catch (java.lang.NullPointerException ignore) {}
		catch (java.lang.NumberFormatException ignore) {}

		if (totalNumber < endNumber) endNumber = totalNumber;

		params = new Object[]{startNumber, endNumber, totalNumber};

		context.put("startNumber", Integer.valueOf(startNumber));
		context.put("endNumber", Integer.valueOf(endNumber));
		context.put("totalNumber", Integer.valueOf(totalNumber));
		context.put("params", params);
		context.put("list", list);
		pagingInfoToContext(state, context);

		// add the search commands
		addSearchMenus(bar, state, rb.getString("useact.search"));

		// add the refresh commands
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		return "_list";

	} // buildListContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildNewContext(SessionState state, Context context)
	{
		// put the service in the context
		context.put("service", UserDirectoryService.getInstance());

		// name the html form for user edit fields
		context.put("form-name", "user-form");
		
		// include the password fields?
		context.put("incPw", state.getAttribute("include-password"));

		context.put("incType", Boolean.valueOf(true));

    context.put("superUser", Boolean.valueOf(UserDirectoryService.allowAddUser()));

		String value = (String) state.getAttribute("valueEid");
		if (value != null) context.put("valueEid", value);

		value = (String) state.getAttribute("valueFirstName");
		if (value != null) context.put("valueFirstName", value);

		value = (String) state.getAttribute("valueLastName");
		if (value != null) context.put("valueLastName", value);

		value = (String) state.getAttribute("valueEmail");
		if (value != null) context.put("valueEmail", value);

		value = (String) state.getAttribute("valueType");
		if (value != null) context.put("valueType", value);
		
		//optional attributes list
		context.put("optionalAttributes", getOptionalAttributes());
		
		return "_edit";

	} // buildNewContext

	/**
	 * Build the context for the create user mode.
	 */
	private String buildCreateContext(SessionState state, Context context)
	{
		// put the service in the context
		context.put("service", UserDirectoryService.getInstance());

		// is the type to be pre-set
		context.put("type", state.getAttribute("create-type"));

		// password is required when using Gateway New Account tool
		// attribute "create-user" is true only for New Account tool
		context.put("pwRequired", state.getAttribute("create-user"));

		String value = (String) state.getAttribute("valueEid");
		if (value != null) context.put("valueEid", value);

		value = (String) state.getAttribute("valueFirstName");
		if (value != null) context.put("valueFirstName", value);

		value = (String) state.getAttribute("valueLastName");
		if (value != null) context.put("valueLastName", value);

		value = (String) state.getAttribute("valueEmail");
		if (value != null) context.put("valueEmail", value);
				
		if ((Boolean)state.getAttribute("user.recaptcha-enabled"))
		{
			ReCaptcha captcha = ReCaptchaFactory.newReCaptcha((String)state.getAttribute("user.recaptcha-public-key"), (String)state.getAttribute("user.recaptcha-private-key"), false);
	        String captchaScript = captcha.createRecaptchaHtml((String)state.getAttribute("recaptcha-error"), null);
	        state.removeAttribute("recaptcha-error");
	        context.put("recaptchaScript", captchaScript);
		}

		return "_create";

	} // buildCreateContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildEditContext(SessionState state, Context context)
	{
		
		// put the service in the context
		context.put("service", UserDirectoryService.getInstance());

		// name the html form for user edit fields
		context.put("form-name", "user-form");

		// get the user to edit
		UserEdit user = (UserEdit) state.getAttribute("user");
		context.put("user", user);
		
		// is super user/admin user?
		context.put("superUser", Boolean.valueOf(UserDirectoryService.allowAddUser()));

		// include the password fields?
		context.put("incPw", state.getAttribute("include-password"));

		// include type fields (not if single user)
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		context.put("incType", Boolean.valueOf(!singleUser));

		// build the menu
		// we need the form fields for the remove...
		boolean menuPopulated = false;
		Menu bar = new MenuImpl();
		if ((!singleUser) && (UserDirectoryService.allowRemoveUser(user.getId())))
		{
			bar.add(new MenuEntry(rb.getString("useact.remuse"), null, true, MenuItem.CHECKED_NA, "doRemove", "user-form"));
			menuPopulated = true;
		}

		if (menuPopulated)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		String value = (String) state.getAttribute("valueEid");
		if (value != null) context.put("valueEid", value);

		value = (String) state.getAttribute("valueFirstName");
		if (value != null) context.put("valueFirstName", value);

		value = (String) state.getAttribute("valueLastName");
		if (value != null) context.put("valueLastName", value);

		value = (String) state.getAttribute("valueEmail");
		if (value != null) context.put("valueEmail", value);

		value = (String) state.getAttribute("valueType");
		if (value != null) context.put("valueType", value);
		
		//optional attributes lists
		context.put("optionalAttributes", getOptionalAttributes());
		context.put("currentAttributes", getCurrentAttributes((UserEdit) state.getAttribute("user")));


		return "_edit";

	} // buildEditContext

	/**
	 * Build the context for the view user mode.
	 */
	private String buildViewContext(SessionState state, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled())
		{
			Log.debug("chef", this + ".buildViewContext");
		}

		// get current user's id
		String id = (String) state.getAttribute("user-id");

		// get the user and put in state as "user"
		try
		{
			User user = UserDirectoryService.getUser(id);
			context.put("user", user);

			// name the html form for user edit fields
			context.put("form-name", "user-form");

			state.setAttribute("mode", "view");

			// make sure we can do an edit
			try
			{
				UserEdit edit = UserDirectoryService.editUser(id);
				UserDirectoryService.cancelEdit(edit);
				context.put("enableEdit", "true");
			}
			catch (UserNotDefinedException e)
			{
			}
			catch (UserPermissionException e)
			{
			}
			catch (UserLockedException e)
			{
			}

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (UserNotDefinedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			Object[] params = new Object[]{id};
			addAlert(state, rb.getFormattedMessage("useact.use_notfou", params));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

		return "_view";

	} // buildViewContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildConfirmRemoveContext(SessionState state, Context context)
	{
		// get the user to edit
		UserEdit user = (UserEdit) state.getAttribute("user");
		context.put("user", user);

		return "_confirm_remove";

	} // buildConfirmRemoveContext

	/**
	 * Build the context for the import mode.
	 */
	private String buildImportContext(SessionState state, Context context) {
		
		//render the template		
		return "_import";

	} // buildImportContext
	
	/**
	 * Build the context for processing the files
	 */
	private String buildProcessImportContext(SessionState state, RunData data, Context context) {
		
		//process the attachments (there will be only one)
		UsersActionState sstate = (UsersActionState)getState(context, data, UsersActionState.class);
		
		try {
			Reference attachment = (Reference)sstate.getAttachments().get(0);
			processImportedUserFile(state, context, attachment);
		} catch (IndexOutOfBoundsException e) {
			//no attachment, carry on, will render correctly
		}
				
		//render the template		
		return "_import";

	} // buildProcessImportContext
	
	/**
	 * doNew called when "eventSubmit_doNew" is in the request parameters to add a new user
	 */
	public void doNew(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "new");

		// mark the user as new, so on cancel it can be deleted
		state.setAttribute("new", "true");

		// disable auto-updates while not in list mode
		disableObservers(state);

	} // doNew

	/**
	 * doImport called when "eventSubmit_doImport" is clicked. This actuall imports the users that were uploaded.
	 */
	public void doImport(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		UsersActionState sstate = (UsersActionState)getState(context, data, UsersActionState.class);
		
		state.setAttribute("mode", "import");
				
		Log.debug("chef", "doImport");
			
		List<ImportedUser> users = (List<ImportedUser>)state.getAttribute("importedUsers");
		if(users !=null && users.size() > 0) {
		
			for(ImportedUser user: users) {
				try {
					User newUser = UserDirectoryService.addUser(null, user.getEid(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getType(), user.getProperties());
				}
				catch (UserAlreadyDefinedException e){
					//ok, just skip
					continue;
				}
				catch (UserIdInvalidException e) {
					addAlert(state, rb.getString("useact.theuseid2") + ": " + user.getEid());
					Log.error("chef", "Import user error: " + e.getClass() + ":" + e.getMessage());
					//try to import the rest
					continue;
				}
				catch (UserPermissionException e){
					addAlert(state, rb.getString("useact.youdonot3"));
					Log.error("chef", "Import user error: " + e.getClass() + ":" + e.getMessage());
					//this is bad so return
					return;
				} 
			}
			
			//set a message to show it was successful
			state.setAttribute("successMessage", rb.getString("import.success"));
			
			//cleanup
			state.removeAttribute("importedUsers");
			state.removeAttribute("mode");
			
			// make sure auto-updates are enabled
			enableObserver(state);
		}
		
	} // doImport
	
	
	
	/**
	 * doEdit called when "eventSubmit_doEdit" is in the request parameters to edit a user
	 */
	public void doEdit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");
		state.removeAttribute("user");
		state.removeAttribute("newuser");

		// get the user
		try
		{
			UserEdit user = UserDirectoryService.editUser(id);
			state.setAttribute("user", user);
			state.setAttribute("mode", "edit");

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (UserNotDefinedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			Object[] params = new Object[]{id};
			addAlert(state, rb.getFormattedMessage("useact.use_notfou", params));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (UserPermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("useact.youdonot1", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (UserLockedException e)
		{
			addAlert(state, rb.getFormattedMessage("useact.somels", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doEdit

	/**
	 * doModify called when "eventSubmit_doModify" is in the request parameters to edit a user
	 */
	public void doModify(RunData data, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled())
		{
			Log.debug("chef", this + ".doModify");
		}

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");
		state.removeAttribute("user");
		state.removeAttribute("newuser");

		// get the user
		try
		{
			UserEdit user = UserDirectoryService.editUser(id);
			state.setAttribute("user", user);
			state.setAttribute("mode", "edit");

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (UserNotDefinedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			Object[] params = new Object[]{id};
			addAlert(state, rb.getFormattedMessage("useact.use_notfou", params));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (UserPermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("useact.youdonot1", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (UserLockedException e)
		{
			addAlert(state, rb.getFormattedMessage("useact.somels", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doModify

	/**
	 * doSave called when "eventSubmit_doSave" is in the request parameters to save user edits
	 */
	public void doSave(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		// commit the change
		UserEdit edit = (UserEdit) state.getAttribute("user");
		if (edit != null)
		{
			try
			{
				UserDirectoryService.commitEdit(edit);
			}
			catch (UserAlreadyDefinedException e)
			{
				// TODO: this means the EID value is not unique... when we implement EID fully, we need to check this and send it back to the user
				Log.warn("chef", "UsersAction.doSave()" + e);
				addAlert(state, rb.getString("useact.theuseid1"));
				return;
			}
		}

		User user = edit;
		if (user == null)
		{
			user = (User) state.getAttribute("newuser");
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");
		state.removeAttribute("valueEid");
		state.removeAttribute("valueFirstName");
		state.removeAttribute("valueLastName");
		state.removeAttribute("valueEmail");
		state.removeAttribute("valueType");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

		if ((user != null) && ((Boolean) state.getAttribute("create-login")).booleanValue())
		{
			try
			{
				// login - use the fact that we just created the account as external evidence
				Evidence e = new ExternalTrustedEvidence(user.getEid());
				Authentication a = AuthenticationManager.authenticate(e);
				if (!UsageSessionService.login(a, (HttpServletRequest) ThreadLocalManager.get(RequestFilter.CURRENT_HTTP_REQUEST)))
				{
					addAlert(state, rb.getString("useact.tryloginagain"));
				}
			}
			catch (AuthenticationException ex)
			{
				Log.warn("chef", "UsersAction.doSave: authentication failure: " + ex);
			}

			// redirect to home (on next build)
			state.setAttribute("redirect", "");
		}

	} // doSave

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters to cancel user edits
	 */
	public void doCancel(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");
		if (user != null)
		{
			// if this was a new, delete the user
			if ("true".equals(state.getAttribute("new")))
			{
				// remove
				try
				{
					UserDirectoryService.removeUser(user);
				}
				catch (UserPermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("useact.youdonot2", new Object[]{user.getId()}));
				}
			}
			else
			{
				UserDirectoryService.cancelEdit(user);
			}
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");
		state.removeAttribute("valueEid");
		state.removeAttribute("valueFirstName");
		state.removeAttribute("valueLastName");
		state.removeAttribute("valueEmail");
		state.removeAttribute("valueType");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doCancel
	
	/**
	 * doCancelImport called when "eventSubmit_doCancelImport" is in the request parameters to cancel user imports
	 */
	public void doCancelImport(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		//cleanup session
		state.removeAttribute("importedUsers");
		
		//also cleanup our state handler (I think this should be combined into SessionState)
		UsersActionState sstate = (UsersActionState)getState(context, data, UsersActionState.class);
		sstate.setAttachments(new ArrayList());
		sstate.setStatus(null);

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doCancelImport

	/**
	 * doRemove called when "eventSubmit_doRemove" is in the request par ameters to confirm removal of the user
	 */
	public void doRemove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		// set mode so we can skip some checks in readUserForm
		state.setAttribute("mode", "remove");

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the user
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");

		// remove
		try
		{
			UserDirectoryService.removeUser(user);
		}
		catch (UserPermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("useact.youdonot2", new Object[]{user.getId()}));
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");
                state.removeAttribute("valueEid");
                state.removeAttribute("valueFirstName");
                state.removeAttribute("valueLastName");
                state.removeAttribute("valueEmail");
                state.removeAttribute("valueType");

		// go to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel user removal
	 */
	public void doCancel_remove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// return to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_remove

	/**
	 * Check to see if password meets requirements set in password policy.
	 * If current user is admin, ignores password policy.
	 *
	 * @author plukasew, bjones86 - SAK-23568
	 *
	 * @param pw the password
	 * @param user the user
	 * @param state the session state
	 * @return true if password is valid or if current user is admin
	 */
	private boolean validatePassword(String pw, User user, SessionState state) {
		if (pw != null && !SecurityService.isSuperUser() && pwHelper.validatePassword(pw, user) == PasswordRating.FAILED) {
			addAlert(state, rb.getString(MSG_KEY_PASSWORD_WEAK) + " " + rb.getString(MSG_KEY_PW_STRENGTH_INFO));
			return false;
		}
		return true;
	}

	/**
	 * Read the user form and update the user in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readUserForm(RunData data, SessionState state)
	{
		// boolean parameters and values
		// --------------Mode--singleUser-createUser-typeEnable
		// Admin New-----new---false------false------true
		// Admin Update--edit--false------false------true
		// Gateway New---null---false------true-------false
		// Account Edit--edit--true-------false------false

		// read the form
		String id = StringUtils.trimToNull(data.getParameters().getString("id"));
		String eid = StringUtils.trimToNull(data.getParameters().getString("eid"));
		state.setAttribute("valueEid", eid);
		String firstName = StringUtils.trimToNull(data.getParameters().getString("first-name"));
		state.setAttribute("valueFirstName", firstName);
		String lastName = StringUtils.trimToNull(data.getParameters().getString("last-name"));
		state.setAttribute("valueLastName", lastName);
		String email = StringUtils.trimToNull(data.getParameters().getString("email"));
		state.setAttribute("valueEmail", email);
		String pw = StringUtils.trimToNull(data.getParameters().getString("pw"));
        String pwConfirm = StringUtils.trimToNull(data.getParameters().getString("pw0"));

        String pwcur = StringUtils.trimToNull(data.getParameters().getString("pwcur"));
        
        Integer disabled = Integer.valueOf(StringUtils.trimToNull(data.getParameters().getString("disabled")) != null ? "1" : "0" );
        
        String mode = (String) state.getAttribute("mode");
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		boolean createUser = ((Boolean) state.getAttribute("create-user")).booleanValue();

		boolean typeEnable = false;
		String type = null;
		if ((mode != null) && (mode.equalsIgnoreCase("new")))
		{
			typeEnable = true;
		}
		else if ((mode != null) && (mode.equalsIgnoreCase("edit")) && (!singleUser))
		{
			typeEnable = true;
		}

		if (typeEnable)
		{
			// for the case of Admin User tool creating new user
			type = StringUtils.trimToNull(data.getParameters().getString("type"));
			state.setAttribute("valueType", type);
		}
		else
		{
			if (createUser)
			{
				// for the case of Gateway Account tool creating new user
				type = (String) state.getAttribute("create-type");
			}
		}
		
		if ((Boolean)state.getAttribute("user.recaptcha-enabled"))
		{
			String challengeField = data.getParameters().getString("recaptcha_challenge_field");
			String responseField = data.getParameters().getString("recaptcha_response_field");
			if (challengeField == null) challengeField = "";
			if (responseField == null) responseField = "";
			ReCaptcha captcha = ReCaptchaFactory.newReCaptcha((String)state.getAttribute("user.recaptcha-public-key"), (String)state.getAttribute("user.recaptcha-private-key"), false);
			ReCaptchaResponse response = captcha.checkAnswer(data.getRequest().getRemoteAddr(), challengeField, responseField);
			if (!response.isValid())
			{
				addAlert(state, rb.getString("useact.capterr"));
		        state.setAttribute("recaptcha-error", response.getErrorMessage());
				return false;
			}
		}
		
		
		//insure valid email address
		//email.matches(".+@.+\\..+")
		if(email != null && !EmailValidator.getInstance().isValid(email)) {
				addAlert(state, rb.getString("useact.invemail"));	
				return false;
		}
		
		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");
		
		//process any additional attributes
		//we continue processing these until we get an empty attribute KEY
		//counter starts at 1
		
		//data is of the form:
		//	optionalAttr_1:att1
		//	optionalAttrValue_1:value1
		//	optionalAttr_2:att2
		//	optionalAttrValue_2:value2
		
		int count = 1;
		boolean continueProcessingOptionalAttributes = true;
		
		ResourcePropertiesEdit properties;
		if(user == null) {
			properties = new BaseResourcePropertiesEdit();
		} else {
			properties = user.getPropertiesEdit();
		}
		
		//remove all properties that are in the confugred list
		//then add back in only the ones that were sent
		//this allows us to remove items via javascript and they get persisted to the db on form save
		Map<String,String> configuredProperties = getOptionalAttributes();
		for(String cp: configuredProperties.keySet()) {
			properties.removeProperty(cp);
		}
		
		
		while(continueProcessingOptionalAttributes) {
			
			//this stores the key
			String optionalAttributeKey = data.getParameters().getString("optionalAttr_"+count);
			
			if(StringUtils.isBlank(optionalAttributeKey)){
				continueProcessingOptionalAttributes = false;
				break;
			}
			
			String optionalAttributeValue = data.getParameters().getString("optionalAttrValue_"+count);
			
			//only single values properties
			//any null ones will wipe out existing ones
			//and any duplicate ones will override previous ones (currently)
			properties.addProperty(optionalAttributeKey, optionalAttributeValue);
			
			//System.out.println("optionalAttributeKey: " + optionalAttributeKey + ", optionalAttributeValue: " + optionalAttributeValue);
			
			count++;
		}
		

		// add if needed
		if (user == null)
		{
			// make sure we have eid
			if (eid == null)
			{
				addAlert(state, rb.getString("usecre.eidmis"));
				return false;
			}
			
			// if in create mode, make sure we have a password
			if (createUser)
			{
				if (pw == null)
				{
					addAlert(state, rb.getString("usecre.pasismis"));
					return false;
				}				
			}

			// make sure we have matching password fields
			if (StringUtil.different(pw, pwConfirm))
			{
				addAlert(state, rb.getString("usecre.pass"));
				return false;
			}

			// SAK-23568 - make sure password meets policy requirements
			TempUser tempUser = new TempUser(eid, null, null, null, eid, pw, null);
			if (!validatePassword(pw, tempUser, state)) {
				return false;
			}

			try
			{
				// add the user in one step so that all you need is add not update permission
				// (the added might be "anon", and anon has add but not update permission)
				
				//SAK-18209 only an admin user should be able to specify a ID
				if (!SecurityService.isSuperUser()) {
					id = null;
				}
				User newUser = UserDirectoryService.addUser(id, eid, firstName, lastName, email, pw, type, properties);

                                if (SecurityService.isSuperUser()) {
                                        if(disabled == 1){
                                                try {
                                                        UserEdit editUser = UserDirectoryService.editUser(newUser.getId());
                                                        editUser.getProperties().addProperty("disabled", "true");
                                                        newUser = editUser;
                                                } catch (UserNotDefinedException e) {
                                                        addAlert(state, rb.getString("usecre.disableFailed"));
                                                        return false;
                                                } catch (UserLockedException e) {
                                                        addAlert(state, rb.getString("usecre.disableFailed"));
                                                        return false;
                                                }
                                        }
                                }


				// put the user in the state
				state.setAttribute("newuser", newUser);
			}
			catch (UserAlreadyDefinedException e)
			{
				addAlert(state, rb.getString("useact.theuseid1"));
				return false;
			}
			catch (UserIdInvalidException e)
			{
				addAlert(state, rb.getString("useact.theuseid2"));
				return false;
			}
			catch (UserPermissionException e)
			{
				addAlert(state, rb.getString("useact.youdonot3"));
				return false;
			}
		}

		// update
		else
		{
			if (!user.isActiveEdit())
			{
				try
				{
					// add the user in one step so that all you need is add not update permission
					// (the added might be "anon", and anon has add but not update permission)
					user = UserDirectoryService.editUser(user.getId());
	
					// put the user in the state
					state.setAttribute("user", user);
				}
				catch (UserLockedException e)
				{
					addAlert(state, rb.getString("useact.somels"));
					return false;
				}
				catch (UserNotDefinedException e)
				{
					Object[] params = new Object[]{id};
					addAlert(state, rb.getFormattedMessage("useact.use_notfou", params));
					
					return false;
				}
				catch (UserPermissionException e)
				{
					addAlert(state, rb.getString("useact.youdonot3"));
					return false;
				}
			}
            // Still needs super user to change super user password
            // If the current user isn't a super user but is trying to change the password or email of a super user print an error
            if (!SecurityService.isSuperUser() && SecurityService.isSuperUser(user.getId())) {
                addAlert(state, rb.getString("useact.youdonot4"));
                return false;
            }

			// eid, pw, type might not be editable
			if (eid != null) user.setEid(eid);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			if (type != null) user.setType(type);
			
			//add in the updated props
			user.getPropertiesEdit().addAll(properties);
			
            if (UserDirectoryService.allowAddUser()) {

				if(disabled == 1){
					user.getProperties().addProperty("disabled", "true");
				}else{
					user.getProperties().removeProperty("disabled");
				}
			}
			
			//validate the password only for local users
			if (!isProvidedType(user.getType())) {
			
				// make sure the old password matches, but don't check for super users
                if (!UserDirectoryService.allowAddUser()) {

					if (!user.checkPassword(pwcur)) {
						addAlert(state, rb.getString("usecre.curpass"));
						return false;
					}
				}

				if (mode == null || !mode.equalsIgnoreCase("remove")) {
					// make sure we have matching password fields
					if (StringUtil.different(pw, pwConfirm))
					{
						addAlert(state, rb.getString("usecre.pass"));
						return false;
					}

					// SAK-23568 - make sure password meets policy requirements
					if (!validatePassword(pw, user, state)) {
						return false;
					}

					if (pw != null) user.setPassword(pw);
				}
			}
		}

		return true;
	}
	
	/**
	 * Get the Map of optional attributes from sakai.properties
	 * 
	 * First list defines the attribute , second the display value. If no display value the attribute name is used.
	 * 
	 * Format is:
	 * 
	 * user.additional.attribute.count=3
	 * user.additional.attribute.1=att1
	 * user.additional.attribute.2=att2
	 * user.additional.attribute.3=att3
	 *
	 * user.additional.attribute.display.att1=Attribute 1
	 * user.additional.attribute.display.att2=Attribute 2
	 * user.additional.attribute.display.att3=Attribute 3
	 * @return
	 */
	private Map<String,String> getOptionalAttributes() {
		
		Map<String,String> atts = new LinkedHashMap<String,String>();
		
		String configs[] = ServerConfigurationService.getStrings("user.additional.attribute");
		if (configs != null) {
			for (int i = 0; i < configs.length; i++) {
				String key = configs[i];
				if (!key.isEmpty()) {
					String value = ServerConfigurationService.getString("user.additional.attribute.display." + key, key);
					atts.put(key, value);
				}
			}
		}
		
		return atts;
		
	}
	
	/**
	 * Gets the current attributes (properties) for a user. Converts the ResourceProperties into a Map
	 * @param user
	 * @return
	 */
	private Map<String,String> getCurrentAttributes(UserEdit user) {
		
		Map<String,String> atts = new LinkedHashMap<String,String>();
		
		ResourceProperties rprops = user.getProperties();
		
		// no props
		if(rprops == null) {
			return atts;
		}
		
		Iterator<String> props = user.getProperties().getPropertyNames();
		
		while(props.hasNext()){
			String prop = props.next();
			atts.put(prop, rprops.getProperty(prop));
		}
		
		return atts;
	}
	
	public void doAttachments(RunData rundata, Context context) {
		
		// use special form of the helper for the admin workspace
		ToolSession session = SessionManager.getCurrentToolSession();
        session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS, new Boolean(true).toString());
		
		// use the helper
		startHelper(rundata.getRequest(), "sakai.filepicker");
		
		// setup the parameters for the helper
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		UsersActionState sstate = (UsersActionState)getState( context, rundata, UsersActionState.class );
		
		state.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, sstate.getAttachments());
		state.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_SINGLE);
		
		//set return status
		sstate.setStatus("processImport");
	}
	
	
	
	
	
	
	
	// ********
	// ******** functions copied from VelocityPortletStateAction ********
	// ********
	/**
	 * Get the proper state for this instance (if portlet is not known, only context).
	 * 
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(Context context, RunData rundata, Class stateClass)
	{
		return getState(((JetspeedRunData) rundata).getJs_peid(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet is known).
	 * 
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(VelocityPortlet portlet, RunData rundata, Class stateClass)
	{
		if (portlet == null)
		{
			Log.warn("chef", ".getState(): portlet null");
			return null;
		}

		return getState(portlet.getID(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet id is known).
	 * 
	 * @param peid
	 *        The portlet id.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(String peid, RunData rundata, Class stateClass)
	{
		if (peid == null)
		{
			Log.warn("chef", ".getState(): peid null");
			return null;
		}

		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			if (state != null) return state;

			// if there's no "state" object in there, make one
			state = (ControllerState) stateClass.newInstance();
			state.setId(peid);

			// remember it!
			ss.setAttribute("state", state);

			return state;
		}
		catch (Exception e)
		{
			Log.warn("chef", "getState: " + e.getClass() + ":" + e.getMessage());
		}

		return null;

	} // getState

	/**
	 * Release the proper state for this instance (if portlet is not known, only context).
	 * 
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(Context context, RunData rundata)
	{
		releaseState(((JetspeedRunData) rundata).getJs_peid(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet is known).
	 * 
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(VelocityPortlet portlet, RunData rundata)
	{
		releaseState(portlet.getID(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet id is known).
	 * 
	 * @param peid
	 *        The portlet id being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(String peid, RunData rundata)
	{
		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			// recycle the state object
			state.recycle();

			// clear out the SessionState for this Portlet
			ss.removeAttribute("state");

			ss.clear();

		}
		catch (Exception e)
		{
			Log.warn("chef", "releaseState: " + e.getClass() + ":" + e.getMessage());
		}

	} // releaseState

	// ******* end of copy from VelocityPortletStateAction
	
	
	private void processImportedUserFile(SessionState state, Context context, Reference file) {
		
		try{
			ContentResource resource = ContentHostingService.getResource(file.getId());
			String contentType = resource.getContentType();
			
			//check mime type
			if(!StringUtils.equals(contentType, CSV_MIME_TYPE)) {
				addAlert(state, rb.getString("import.error"));
				return;
			}
			//SAK-21405 SAK-21884 original parse method, auto maps column headers to bean properties
			/*
			HeaderColumnNameTranslateMappingStrategy<ImportedUser> strat = new HeaderColumnNameTranslateMappingStrategy<ImportedUser>();
			strat.setType(ImportedUser.class);

			//map the column headers to the field names in the ImportedUser class
			Map<String, String> map = new HashMap<String, String>();
			map.put("user id", "eid");
			map.put("first name", "firstName");
			map.put("last name", "lastName");
			map.put("email", "email");
			map.put("password", "password");
			map.put("type", "type");
			map.put("properties", "rawProps"); //specially formatted string, see ImportedUser class.
			
			strat.setColumnMapping(map);

			CsvToBean<ImportedUser> csv = new CsvToBean<ImportedUser>();
			List<ImportedUser> list = new ArrayList<ImportedUser>();
			
			list = csv.parse(strat, new CSVReader(new InputStreamReader(resource.streamContent())));
			*/
			
			//SAK-21884 manual parse method so we can support arbitrary columns
			CSVReader reader = new CSVReader(new InputStreamReader(resource.streamContent()));
		    String [] nextLine;
		    int lineCount = 0;
		    List<ImportedUser> list = new ArrayList<ImportedUser>();
		    Map<Integer,String> mapping = null;
		    
		    while ((nextLine = reader.readNext()) != null) {
		        
		    	if(lineCount == 0) {
		        	//header row, capture it
		    		mapping = mapHeaderRow(nextLine);
		        } else {
		        	//map the fields into the object
		        	list.add(mapLine(nextLine, mapping));
		        }
		    	
		        lineCount++;
		    }
			
			state.setAttribute("importedUsers", list);
			context.put("importedUsers", list);
			
		} catch (Exception e) {
			Log.error("chef", "Error reading imported file: " + e.getClass() + " : " + e.getMessage());
			addAlert(state, rb.getString("import.error"));
			return;
		}
		
		return;

	}
	
	/**
	 * Takes the header row from the CSV to determines the position of the columns so that we can 
	 * correctly parse any arbitrary CSV file. This is required because when we iterate over the rest of the lines, 
	 * we need to know what the column header is, so we can set the approriate ImportedUser property
	 * or add into the ResourceProperties list, which ever is required.
	 * 
	 * @param line	the already split line
	 * @return
	 */
	private Map<Integer,String> mapHeaderRow(String[] line) {
		
		Map<Integer,String> mapping = new LinkedHashMap<Integer,String>();
		
		for(int i=0;i<line.length;i++){
			mapping.put(i, line[i]);
		}
		
		return mapping;
		
	}
	
	/**
	 * Takes a row of data and maps it into the appropriate ImportedUser properties
	 * We have a fixed list of properties, anything else goes into ResourceProperties
	 * @param line
	 * @param mapping
	 * @return
	 */
	private ImportedUser mapLine(String[] line, Map<Integer,String> mapping){
		
		ImportedUser u = new ImportedUser();
		ResourceProperties p = new BaseResourcePropertiesEdit();
		
		for(Map.Entry<Integer,String> entry: mapping.entrySet()) {
			int i = entry.getKey();
			String col = entry.getValue();
			
			//now check each of the main properties in turn to determine which one to set, otherwise set into props
			if(StringUtils.equals(col, IMPORT_USER_ID)) {
				u.setEid(line[i]);
			} else if(StringUtils.equals(col, IMPORT_FIRST_NAME)) {
				u.setFirstName(line[i]);
			} else if(StringUtils.equals(col, IMPORT_LAST_NAME)) {
				u.setLastName(line[i]);
			} else if(StringUtils.equals(col, IMPORT_EMAIL)) {
				u.setEmail(line[i]);
			} else if(StringUtils.equals(col, IMPORT_PASSWORD)) {
				u.setPassword(line[i]);
			} else if(StringUtils.equals(col, IMPORT_TYPE)) {
				u.setType(line[i]);
			} else {
				//only add if not blank
				if(StringUtils.isNotBlank(line[i])) {
					p.addProperty(col, line[i]);
				}
			}
		}
		
		u.setProperties(p);
		
		return u;
	}
	
	/**
	 * Check to see if the type is in the list of known provided types
	 * @param userType User's type
	 * @return
	 */
	private boolean isProvidedType(String userType) {
		boolean provided = false;
		String[] providedTypes = ServerConfigurationService.getStrings("user.type.provided");
		if (providedTypes != null && providedTypes.length > 0) {
			List<String> typeList = Arrays.asList(providedTypes);
			if (typeList.contains(userType))
				provided = true;
		}
		return provided;
	}
}
