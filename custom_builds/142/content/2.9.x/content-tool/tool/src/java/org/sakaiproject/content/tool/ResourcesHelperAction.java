/**********************************************************************************
 * $URL:	 $
 * $Id:	$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

public class ResourcesHelperAction extends VelocityPortletPaneledAction 
{
	/** the logger for this class */
	 private static final Log logger = LogFactory.getLog(ResourcesHelperAction.class);
	 
	 private static final ResourceConditionsHelper conditionsHelper = new ResourceConditionsHelper();
	 
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("types");
	private static ResourceLoader contentResourceBundle = new ResourceLoader("content");
	
	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";

	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";

	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
	
	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";
	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
	
	protected  static final String CREATE_FOLDERS_TEMPLATE = "resources/sakai_create_folders";
	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
	protected  static final String CREATE_UPLOADS_TEMPLATE = "resources/sakai_create_uploads";
	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
	protected static final String CREATE_URLS_TEMPLATE = "resources/sakai_create_urls";
	
	public static final String MODE_MAIN = "main";
	protected static final String PREFIX = "ResourceTypeHelper.";
	
	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";
	
	protected static final String REPLACE_CONTENT_TEMPLATE = "resources/sakai_replace_file";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = PREFIX + "content_type_image_service";
	
	private static final String STATE_COPYRIGHT_FAIRUSE_URL = PREFIX + "copyright_fairuse_url";

	private static final String STATE_COPYRIGHT_NEW_COPYRIGHT = PREFIX + "new_copyright";
	
	/** copyright related info */
	private static final String STATE_COPYRIGHT_TYPES = PREFIX + "copyright_types";

	private static final String STATE_DEFAULT_COPYRIGHT = PREFIX + "default_copyright";
	
	private static final String STATE_DEFAULT_COPYRIGHT_ALERT = PREFIX + "default_copyright_alert";
	
	/** state attribute for the maximum size for file upload */
	static final String STATE_FILE_UPLOAD_MAX_SIZE = PREFIX + "file_upload_max_size";
	
	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = PREFIX + "mycopyright";
	
	private static final String STATE_NEW_COPYRIGHT_INPUT = PREFIX + "new_copyright_input";
  
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = PREFIX + "prevent_public_display";
	
	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = PREFIX + "usingCreativeCommons";
	
	/** name of state attribute for the default retract time */
	protected static final String STATE_DEFAULT_RETRACT_TIME = PREFIX + "default_retract_time";

	public String buildAccessContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		logger.debug(this + ".buildAccessContext()");
		String template = ACCESS_TEXT_TEMPLATE;
		return template;
	}


	
	public String buildCreateContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		logger.debug(this + ".buildCreateContext()");
		String template = CREATE_UPLOAD_TEMPLATE;
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();

		ListItem parent = new ListItem(pipe.getContentEntity());
		if(parent.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = CREATE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = CREATE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			template = CREATE_URL_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = CREATE_UPLOAD_TEMPLATE;
		}
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		return template;
	}

	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		logger.debug(this + ".buildMainPanelContext()");
		// context.put("sysout", System.out);
		context.put("tlang", rb);
		
		context.put("validator", new Validator());
		context.put("copyright_alert_url", COPYRIGHT_ALERT_URL);
		context.put("DOT", ListItem.DOT);
		context.put("calendarMap", new HashMap());

                String ezproxy = ServerConfigurationService.getString("content.ezproxy.prefix", "");

                if (ezproxy != null && ezproxy != "") {
                    context.put("ezproxyPrefix", ezproxy);
                } else {
                    context.put("ezproxyPrefix", false);
                }
		
		context.put("dateFormat", getDateFormatString());
		
		String mode = (String) state.getAttribute(ResourceToolAction.STATE_MODE);

		if (mode == null)
		{
			initHelper(portlet, context, data, state);
		}

		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) != null)
		{
			context.put("itemAlertMessage", state.getAttribute(ResourcesAction.STATE_MESSAGE));
			state.removeAttribute(ResourcesAction.STATE_MESSAGE);
		}
		
		ContentTypeImageService contentTypeImageService = (ContentTypeImageService) state.getAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		context.put("contentTypeImageService", contentTypeImageService);
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			String attributes = "ResourcesHelperAction.buildMainPanelContext() SAK-8449 dump of state.attributes:\n";
			List<String> attrNames = state.getAttributeNames();
			for(String attrName : attrNames)
			{
				Object val = state.getAttribute(attrName);
				if(val instanceof Collection)
				{
					int i = 0;
					for(Object obj : (Collection) val)
					{
						attributes += "\t" + attrName + "[" + i + "] ==> " + obj + "\n";
						i++;
					}
				}
				else
				{
					attributes += "\t" + attrName + " ==> " + val + "\n";
				}
			}
			attributes += "ResourcesHelperAction.buildMainPanelContext() SAK-8449 dump of toolSession.attributes:\n";
			Enumeration toolNames = toolSession.getAttributeNames();
			while(toolNames.hasMoreElements())
			{
				String name = (String) toolNames.nextElement();
				Object val = toolSession.getAttribute(name);
				if(val instanceof Collection)
				{
					int i = 0;
					for(Object obj : (Collection) val)
					{
						attributes += "\t" + name + "[" + i + "] ==> " + obj + "\n";
						i++;
					}
				}
				else
				{
					attributes += "\t" + name + " ==> " + val + "\n";
				}
			}
			logger.debug(attributes, new Throwable());
			return null;
		}
		if(pipe.isActionCompleted())
		{
			return null;
		}
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		String actionId = pipe.getAction().getId();
		
		context.put("GROUP_ACCESS", AccessMode.GROUPED);
		context.put("SITE_ACCESS", AccessMode.SITE);
		context.put("INHERITED_ACCESS", AccessMode.INHERITED);
		
		context.put("TYPE_FOLDER", ResourceType.TYPE_FOLDER);
		context.put("TYPE_HTML", ResourceType.TYPE_HTML);
		context.put("TYPE_TEXT", ResourceType.TYPE_TEXT);
		context.put("TYPE_UPLOAD", ResourceType.TYPE_UPLOAD);
		context.put("TYPE_URL", ResourceType.TYPE_URL);

		String template = "";

		switch(pipe.getAction().getActionType())
		{
		case CREATE:
			template = buildCreateContext(portlet, context, data, state);
			break;
		case REVISE_CONTENT:
			template = buildReviseContext(portlet, context, data, state);
			break;
		case REPLACE_CONTENT:
			template = buildReplaceContext(portlet, context, data, state);
			break;
		case NEW_UPLOAD:
			template = buildUploadFilesContext(portlet, context, data, state);
			break;
		case NEW_FOLDER:
			template = buildNewFoldersContext(portlet, context, data, state);
			break;
		case NEW_URLS:
			template = buildNewUrlsContext(portlet, context, data, state);
			break;
		default:
			// hmmmm
			break;
		}
		
		return template;
	}

	protected String buildNewUrlsContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	 {
		logger.debug(this + ".buildNewUrlsContext()");
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.metadataGroupsIntoContext(context);
		// model.setPubviewPossible(! preventPublicDisplay);
				
		context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		if(model.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}

                String ezproxy = ServerConfigurationService.getString("content.ezproxy.prefix", "");
                
                if (ezproxy != null && ezproxy != "") {
                    context.put("ezproxyPrefix", ezproxy);
                } else {
                    context.put("ezproxyPrefix", false);
                }
		
		ResourcesAction.copyrightChoicesIntoContext(state, context);
		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		ResourceConditionsHelper.buildConditionContext(context, state);
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);
		
		return CREATE_URLS_TEMPLATE;
	 }



	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	private String buildNewFoldersContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		logger.debug(this + ".buildNewFoldersContext()");
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();

		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.metadataGroupsIntoContext(context);
		// model.setPubviewPossible(! preventPublicDisplay);
		
		context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
				
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}	

		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		ResourceConditionsHelper.buildConditionContext(context, state);

		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		return CREATE_FOLDERS_TEMPLATE;
	}



	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildReplaceContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		logger.debug(this + ".buildReplaceContext()");
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem item = new ListItem(pipe.getContentEntity());
		item.setPubviewPossible(! preventPublicDisplay);
		
		if(item.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		context.put("item", item);
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		return REPLACE_CONTENT_TEMPLATE;
	}



	public String buildReviseContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		logger.debug(this + ".buildReviseContext()");
		String template = REVISE_TEXT_TEMPLATE;
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();
		String mimetype = pipe.getMimeType();
		
		ListItem item = new ListItem(pipe.getContentEntity());
		context.put("item", item);
		
		// context.put("inDropbox", ContentHostingService.isInDropbox(pipe.getContentEntity().getId()));
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get(ResourceTypeRegistry.class);
		if(registry != null)
		{
			ResourceType typedef = registry.getType(typeId);
			if(typedef != null)
			{
				context.put("hasNotificationDialog", typedef.hasNotificationDialog());
			}
		}
		
		if(item.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		context.put("pipe", pipe);

		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = REVISE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = REVISE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			template = REVISE_URL_TEMPLATE;
		}
		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_HTML.equals(mimetype))
		{
			template = REVISE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_UPLOAD.equals(typeId) && mimetype != null && ResourceType.MIME_TYPE_TEXT.equals(mimetype))
		{
			template = REVISE_TEXT_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = REVISE_UPLOAD_TEMPLATE;
		}
		
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);

		return template;
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildUploadFilesContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		logger.debug(this + ".buildUploadFilesContext()");
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		if(max_file_size_mb == null)
		{
			max_file_size_mb = "20";
		}
		String upload_limit = rb.getFormattedMessage("upload.limit", new String[]{ max_file_size_mb });
		context.put("upload_limit", upload_limit);
		
		String uploadMax = ServerConfigurationService.getString("content.upload.max");
		String instr_uploads= rb.getFormattedMessage("instr.uploads", new String[]{ uploadMax});
		context.put("instr_uploads", instr_uploads);

        Boolean dragAndDrop = ServerConfigurationService.getBoolean("content.upload.dragndrop", false);
        context.put("dragAndDrop", dragAndDrop);

//		int max_bytes = 1024 * 1024;
//		try
//		{
//			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
//		}
//		catch(Exception e)
//		{
//			// if unable to parse an integer from the value
//			// in the properties file, use 1 MB as a default
//			max_file_size_mb = "1";
//			max_bytes = 1024 * 1024;
//		}

		
		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		context.put(ResourcesAction.PIPE_INIT_ID, pipe.getInitializationId());

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		ListItem parent = new ListItem(pipe.getContentEntity());
		parent.setPubviewPossible(! preventPublicDisplay);
		ListItem model = new ListItem(pipe, parent, defaultRetractDate);
		model.metadataGroupsIntoContext(context);
		// model.setPubviewPossible(! preventPublicDisplay);
				
		//context.put("model", model);
		context.put("type", model.getResourceTypeDef());
		
		context.put("pipes", pipes);
		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		if(model.isDropbox)
		{
			String dropboxNotificationsProperty = getDropboxNotificationsProperty();
			context.put("dropboxNotificationAllowed", Boolean.valueOf(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(dropboxNotificationsProperty)));
		}
		
		ResourcesAction.copyrightChoicesIntoContext(state, context);
		ResourcesAction.publicDisplayChoicesIntoContext(state, context);
		
		String defaultCopyrightStatus = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(STATE_DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		String defaultCopyrightAlert = (String) state.getAttribute(STATE_DEFAULT_COPYRIGHT_ALERT);
		if(defaultCopyrightAlert == null || defaultCopyrightAlert.trim().equals(""))
		{
			defaultCopyrightAlert = ServerConfigurationService.getString("default.copyright.alert");
			state.setAttribute(STATE_DEFAULT_COPYRIGHT_ALERT, defaultCopyrightAlert);
		}
		
		if("true".equalsIgnoreCase(defaultCopyrightAlert))
		{
			model.setCopyrightAlert(true);
		}
		
		context.put("defaultCopyrightStatus", defaultCopyrightStatus);
		
		ResourceConditionsHelper.buildConditionContext(context, state);
	
		int requestStateId = ResourcesAction.preserveRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST});
		context.put("requestStateId", requestStateId);
		context.put("model", model);

		return CREATE_UPLOADS_TEMPLATE;
	}

	public void doCancel(RunData data)
	{
		logger.debug(this + ".doCancel()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		//Tool tool = ToolManager.getCurrentTool();
		//String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		//toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		if(pipe != null)
		{
			String pipe_init_id = pipe.getInitializationId();
			String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
			{
				pipe.setErrorEncountered(true);
				pipe.setActionCanceled(false);
			}
			else
			{
				pipe.setErrorEncountered(false);
				pipe.setActionCanceled(true);
			}
			
			pipe.setActionCompleted(false);
	
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		
		}
		
		
	}
	
	public void doContinue(RunData data)
	{
		logger.debug(this + ".doContinue()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		String content = params.getString("content");
		if(content == null)
		{
			addAlert(state, rb.getString("text.notext"));
			return;
		}
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
//		Tool tool = ToolManager.getCurrentTool();
//		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
//		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		if(pipe != null)
		{
			String pipe_init_id = pipe.getInitializationId();
			String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
			{
					// in this case, prevent upload to wrong folder
					pipe.setErrorMessage(rb.getString("alert.try-again"));
					pipe.setActionCanceled(false);
					pipe.setErrorEncountered(true);
					pipe.setActionCompleted(false);
					return;
			}
				
			toolSession.setAttribute(ResourceToolAction.ACTION_PIPE, pipe);

		}
		
		String resourceType = pipe.getAction().getTypeId();
		String mimetype = pipe.getMimeType();
		
		ListItem item = new ListItem(pipe.getContentEntity());
		// notification
		int noti = determineNotificationPriority(params, item.isDropbox, item.userIsMaintainer());

		pipe.setRevisedMimeType(pipe.getMimeType());
		if(ResourceType.TYPE_TEXT.equals(resourceType) || ResourceType.MIME_TYPE_TEXT.equals(mimetype))
		{
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_TEXT);
			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
			pipe.setNotification(noti);

		}
		else if(ResourceType.TYPE_HTML.equals(resourceType) || ResourceType.MIME_TYPE_HTML.equals(mimetype))
		{
			StringBuilder alertMsg = new StringBuilder();
			content = FormattedText.processHtmlDocument(content, alertMsg);
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
			pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
			pipe.setNotification(noti);
			if (alertMsg.length() > 0)
			{
				addAlert(state, alertMsg.toString());
				return;
			}
		}
		else if(ResourceType.TYPE_URL.equals(resourceType))
		{
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
			pipe.setNotification(noti);
		}
		else if(ResourceType.TYPE_FOLDER.equals(resourceType))
		{
			MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
			int count = params.getInt("folderCount");
			mfp.setFileCount(count);
			
			List<ResourceToolActionPipe> pipes = mfp.getPipes();
			for(int i = 0; i < pipes.size(); i++)
			{
				ResourceToolActionPipe fp = pipes.get(i);
				String folderName = params.getString("folder" + (i + 1));
				fp.setFileName(folderName);
				fp.setNotification(noti);
			}
		}
		
		try 
		{
			pipe.setRevisedContent(content.getBytes(ResourcesAction.UTF_8_ENCODING));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
		} 
		catch (UnsupportedEncodingException e) 
		{
			logger.warn( this + ": " + e.toString() );
			addAlert(state, rb.getString("alert.utf8encoding"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
		}
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}
	
	public void doCreateFolders(RunData data)
	{
		logger.debug(this + ".doCreateFolders()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		String pipe_init_id = pipe.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
		
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			pipe.setErrorMessage(rb.getString("alert.try-again"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
			return;
		}
		
		String resourceType = pipe.getAction().getTypeId();
		
		int count = params.getInt("fileCount");
		pipe.setFileCount(count);
		
		int lastIndex = params.getInt("lastIndex");
		
		ContentEntity entity = pipe.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}

		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		int actualCount = 0;
		for(int i = 0; i <= lastIndex && actualCount < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			ResourceToolActionPipe fp = pipes.get(actualCount);
			String folderName = params.getString("content" + ListItem.DOT + i);
			if(folderName == null || folderName.trim().equals(""))
			{
				continue;
			}
			
			fp.setFileName(folderName);
			
			ListItem newFolder = (ListItem) fp.getRevisedListItem();
			if(newFolder == null)
			{
				if(parent == null)
				{
					newFolder = new ListItem(folderName);
				}
				else
				{
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = TimeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}

					newFolder = new ListItem(fp, parent, defaultRetractDate);
					newFolder.setName(folderName);
					newFolder.setId(folderName);
				}
			}
			if(ListItem.isOptionalPropertiesEnabled())
			{
				newFolder.initMetadataGroups(null);
			}

			// capture properties
			newFolder.captureProperties(params, ListItem.DOT + i);
			if (newFolder.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFolder.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFolder.getConditionAssignmentPoints() }));
				return;
			}

			fp.setRevisedListItem(newFolder);
			
			ResourceConditionsHelper.saveCondition(newFolder, params, state, i);
			
			actualCount++;
		}

		if(actualCount > 0)
		{
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
			
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}
		else
		{
			addAlert(state, rb.getString("alert.nofldr"));
		}

	}
	
	public void doReplace(RunData data)
	{
		logger.debug(this + ".doReplace()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe == null)
		{
			return;
		}
		
		String pipe_init_id = pipe.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
			
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			pipe.setErrorMessage(rb.getString("alert.try-again"));
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(true);
			pipe.setActionCompleted(false);
			logger.debug(this + ".doReplace() setting error on pipe");
			String uploadMax = ServerConfigurationService.getString("content.upload.max");
			addAlert(state, rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
			return;
		}
		
		FileItem fileitem = null;
		try
		{
			fileitem = params.getFileItem("content");
		}
		catch(Exception e)
		{
			logger.warn("Exception ", e);
		}
		
		if(fileitem == null)
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1024 * 1024;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1024 * 1024;
			}
			
			String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
			//max_file_size_mb + "MB " + rb.getString("exceeded2"));
		}
		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
		{
			addAlert(state, rb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			InputStream stream;
				stream = fileitem.getInputStream();
				if(stream == null)
				{
					byte[] bytes = fileitem.get();
					pipe.setRevisedContent(bytes);
				}
				else
				{
					 pipe.setRevisedContentStream(stream);
				}
				String contentType = fileitem.getContentType().replaceAll("\"", "");
				//pipe.setRevisedContent(bytes);
				pipe.setRevisedMimeType(contentType);
				pipe.setFileName(filename);
				
				if(ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType))
				{
					pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
				}
				else if(pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) != null)
				{
					pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, (String) pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING));
				}
				
			ListItem newFile = new ListItem(pipe.getContentEntity());
			// notification
			int noti = determineNotificationPriority(params, newFile.isDropbox, newFile.userIsMaintainer());
			newFile.setNotification(noti);
			
			pipe.setRevisedListItem(newFile);
			
			pipe.setActionCanceled(false);
			pipe.setErrorEncountered(false);
			pipe.setActionCompleted(true);
			
			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}


	}
	
	public void doAddUrls(RunData data)
	{
		logger.debug(this + ".soAddUrls()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(mfp == null)
		{
			return;
		}
		
		String pipe_init_id = mfp.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
	
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			mfp.setErrorMessage(rb.getString("alert.try-again"));
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(true);
			mfp.setActionCompleted(false);
			return;
		}
		
		int count = params.getInt("fileCount");
		mfp.setFileCount(count);
		
		int lastIndex = params.getInt("lastIndex");
		
		ContentEntity entity = mfp.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}
		
		List<String> alerts = new ArrayList<String>();
		
		List<ResourceToolActionPipe> pipes = mfp.getPipes();
		
		int actualCount = 0;
		for(int i = 0; i <= lastIndex && actualCount < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			
			ResourceToolActionPipe pipe = pipes.get(actualCount);
			
			String url = params.getString("content" + ListItem.DOT + i );
				if(url == null)
				{
					continue;
				}
				else
				{
					try
					 {
						 url = ResourcesAction.validateURL(url.trim());
					 }
					 catch (MalformedURLException e)
					 {
						addAlert(state, rb.getFormattedMessage("url.invalid", new String[]{url}));
						 continue;
					 }
					
					 try {
						 pipe.setRevisedContent(url.getBytes(ResourcesAction.UTF_8_ENCODING));
					 } catch (UnsupportedEncodingException e) {
						 pipe.setRevisedContent(url.getBytes());
					 }
				}
				
				pipe.setFileName(Validator.escapeResourceName(url.trim()));
				pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
				
			ListItem newFile = (ListItem) pipe.getRevisedListItem();
			if(newFile == null)
			{
				if(parent == null)
				{
					newFile = new ListItem(pipe.getFileName());
				}
				else
				{
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = TimeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}

					newFile = new ListItem(mfp, parent, defaultRetractDate);
					newFile.setName(new String(pipe.getRevisedContent()).trim());
					newFile.setId(pipe.getFileName());
				}
			}
			
			if(ListItem.isOptionalPropertiesEnabled())
			{
				newFile.initMetadataGroups(null);
			}
			
			// capture properties
			newFile.captureProperties(params, ListItem.DOT + i);
			if (newFile.numberFieldIsInvalid) {
				addAlert(state, rb.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFile.numberFieldIsOutOfRange) {
				addAlert(state, rb.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
				return;
			}
			// notification
			int noti = determineNotificationPriority(params, newFile.isDropbox, newFile.userIsMaintainer());
			newFile.setNotification(noti);
			
			//alerts.addAll(newFile.checkRequiredProperties());
							
			pipe.setRevisedListItem(newFile);
			
			// capture properties
			newFile.captureProperties(params, ListItem.DOT + i);
			if (newFile.numberFieldIsInvalid) {
				addAlert(state, contentResourceBundle.getString("conditions.invalid.condition.argument"));
				return;
			}
			if (newFile.numberFieldIsOutOfRange) {
			    addAlert(state, contentResourceBundle.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
				return;
			}
			ResourceConditionsHelper.saveCondition(newFile, params, state, i);
				
			actualCount++;
			
		}
		if(! alerts.isEmpty())
		{
			for(String alert: alerts)
			{
				addAlert(state, alert);
			}
		}

		if(actualCount < 1)
		{
			addAlert(state, rb.getString("url.noinput"));
			return;
		}

		mfp.setActionCanceled(false);
		mfp.setErrorEncountered(false);
		mfp.setActionCompleted(true);
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}

	/**
	 * @return
	 */
	protected String getDropboxNotificationsProperty()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		Properties props = placement.getPlacementConfig();
		String dropboxNotifications = props.getProperty(ResourcesAction.DROPBOX_NOTIFICATIONS_PROPERTY);
		if(dropboxNotifications == null)
		{
			dropboxNotifications = ResourcesAction.DROPBOX_NOTIFICATIONS_DEFAULT_VALUE;
		}
		
		logger.debug(this + ".getDropboxNotificationsProperty() dropboxNotifications == " + dropboxNotifications);

		return dropboxNotifications;
	}

	/**
	 * @param params
	 * @param newFile
	 * @return
	 */
	protected int determineNotificationPriority(ParameterParser params, boolean contextIsDropbox, boolean userIsMaintainer) 
	{
		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (contextIsDropbox)
		{
			boolean notification = false;
			
			if(userIsMaintainer)	// if the user is a site maintainer
			{
				notification = params.getBoolean("notify_dropbox");
				if(notification)
				{
					noti = NotificationService.NOTI_REQUIRED;
				}
			}
			else
			{
				String notifyDropbox = getDropboxNotificationsProperty();
				if(ResourcesAction.DROPBOX_NOTIFICATIONS_ALWAYS.equals(notifyDropbox))
				{
					noti = NotificationService.NOTI_OPTIONAL;
				}
				else if(ResourcesAction.DROPBOX_NOTIFICATIONS_ALLOW.equals(notifyDropbox))
				{
					notification = params.getBoolean("notify_dropbox");
	  				if(notification)
	   				{
	   					noti = NotificationService.NOTI_OPTIONAL;
	   				}
				}
			}
			logger.debug(this + ".doAddUrls() noti == " + noti);
		}
		else
		{
			// read the notification options
			String notification = params.getString("notify");
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("o".equals(notification))
			{
				noti = NotificationService.NOTI_OPTIONAL;
			}
		}
		return noti;
	}
	
	
	public void doUpload(RunData data)
	{
		logger.debug(this + ".doUpload()");
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		int requestStateId = params.getInt("requestStateId", 0);
		ResourcesAction.restoreRequestState(state, new String[]{ResourcesAction.PREFIX + ResourcesAction.REQUEST}, requestStateId);
		
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(mfp == null)
		{
			logger.debug(this + ".doUpload() mfp is null");
			return;
		}
		
		String pipe_init_id = mfp.getInitializationId();
		String response_init_id = params.getString(ResourcesAction.PIPE_INIT_ID);
		
		if(pipe_init_id == null || response_init_id == null || ! response_init_id.equalsIgnoreCase(pipe_init_id))
		{
			// in this case, prevent upload to wrong folder
			mfp.setErrorMessage(rb.getString("alert.try-again"));
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(true);
			mfp.setActionCompleted(false);
			logger.debug(this + ".doUpload() setting error on pipe");
			String uploadMax = ServerConfigurationService.getString("content.upload.max");
			addAlert(state, rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
			return;
		}
		
		logger.debug(" after doUpload() setting error on pipe");
		int count = params.getInt("fileCount");
		mfp.setFileCount(count);
		if(count < 1)
		{
			mfp.setFileCount(1);
		}
		
		int lastIndex = params.getInt("lastIndex");
		
		List<String> allAlerts = new ArrayList<String>();
		
		ContentEntity entity = mfp.getContentEntity();
		ListItem parent = null;
		if(entity != null && entity instanceof ContentCollection)
		{
			ContentCollection containingCollection = (ContentCollection) entity;
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			parent = new ListItem(entity);
			parent.setPubviewPossible(! preventPublicDisplay);
		}
		
		List<ResourceToolActionPipe> pipes = mfp.getPipes();
	
		logger.debug(this + ".doUpload() iterating through pipes");

		int uploadCount = 0;
		
		for(int i = 0, c = 0; i <= lastIndex && c < count; i++)
		{
			String exists = params.getString("exists" + ListItem.DOT + i);
			if(exists == null || "".equals(exists))
			{
				continue;
			}
			
			ResourceToolActionPipe pipe = pipes.get(c);
			
			FileItem fileitem = null;
			try
			{
				fileitem = params.getFileItem("content" + ListItem.DOT + i );
			}
			catch(Exception e)
			{
				logger.warn("Exception ", e);
			}
			
			if(fileitem == null)
			{
				String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
				int max_bytes = 1024 * 1024;
				try
				{
					max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
				}
				catch(Exception e)
				{
					// if unable to parse an integer from the value
					// in the properties file, use 1 MB as a default
					max_file_size_mb = "1";
					max_bytes = 1024 * 1024;
				}
				
				String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
				// "The user submitted a file to upload but it was too big!"
				addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
				//max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
			{
				// no file selected -- skip this one
			}
			else if (fileitem.getFileName().length() > 0)
			{
				String filename = Validator.getFileName(fileitem.getFileName());
				pipe.setRevisedContentStream( fileitem.getInputStream() );
				String contentType = fileitem.getContentType().replaceAll("\"", "");
				pipe.setRevisedMimeType(contentType);
				
				// If no encoding specified, default to UTF-8 encoding
				if ( (ResourceType.MIME_TYPE_HTML.equals(contentType) || ResourceType.MIME_TYPE_TEXT.equals(contentType)) &&
						pipe.getPropertyValue(ResourceProperties.PROP_CONTENT_ENCODING) == null)
				{
						pipe.setRevisedResourceProperty(ResourceProperties.PROP_CONTENT_ENCODING, ResourcesAction.UTF_8_ENCODING);
				}
				
				pipe.setFileName(filename);
					 
				ListItem newFile = (ListItem) pipe.getRevisedListItem();
				if(newFile == null)
				{
					if(parent == null)
					{
						newFile = new ListItem(filename);
					}
					else
					{
						Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
						if(defaultRetractDate == null)
						{
							defaultRetractDate = TimeService.newTime();
							state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
						}

						newFile = new ListItem(pipe, parent, defaultRetractDate);
						newFile.setName(filename);
						newFile.setId(filename);
					}
				}

				if(ListItem.isOptionalPropertiesEnabled())
				{
					newFile.initMetadataGroups(null);
				}

				// capture properties
				newFile.captureProperties(params, ListItem.DOT + i);
				
				// notification
				int noti = determineNotificationPriority(params, newFile.isDropbox, newFile.userIsMaintainer());
				newFile.setNotification(noti);
				// allAlerts.addAll(newFile.checkRequiredProperties());
				
				pipe.setRevisedListItem(newFile);
				
				// capture properties
				newFile.captureProperties(params, ListItem.DOT + i);
				if (newFile.numberFieldIsInvalid) {
					addAlert(state, contentResourceBundle.getString("conditions.invalid.condition.argument"));
					return;
				}
				if (newFile.numberFieldIsOutOfRange) {
				    addAlert(state, contentResourceBundle.getFormattedMessage("conditions.condition.argument.outofrange", new String[] { newFile.getConditionAssignmentPoints() }));
					return;
				}
				ResourceConditionsHelper.saveCondition(newFile, params, state, i);
				
				uploadCount++;
				
			}
			c++;
			
		}
		logger.debug(this + ".doUpload() checking upload count");
		
		if(uploadCount < 1 && state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
		{
			logger.debug(this + ".doUpload() no files uploaded");

			HttpServletRequest req = data.getRequest();
			String status = (String) req.getAttribute("upload.status");
			logger.debug("Printing out upload.status: " + status);
			if(status == null)
			{
				logger.warn("No files uploaded; upload.status == null");
			}
			else if("ok".equals(status))
			{
				logger.warn("No files uploaded; upload.status == ok");
			}
			else if("size_limit_exceeded".equals(status))
			{
				String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
				int max_bytes = 1024 * 1024;
				try
				{
					max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
				}
				catch(Exception e)
				{
					// if unable to parse an integer from the value
					// in the properties file, use 1 MB as a default
					max_file_size_mb = "1";
					max_bytes = 1024 * 1024;
				}
				
				String max_bytes_string = ResourcesAction.getFileSizeString(max_bytes, rb);
				
				addAlert(state, rb.getFormattedMessage("size.exceeded", new Object[]{ max_bytes_string }));
			}
			else if("exception".equals(status))
			{
				logger.warn("No files uploaded; upload.status == exception");
				addAlert(state, rb.getString("choosefile7"));
			}
		}
		logger.debug(this + ".doUpload() checking allAlerts");
		if(! allAlerts.isEmpty())
		{
			for(String alert: allAlerts)
			{
				addAlert(state, alert);
			}
		}

		logger.debug(this + ".doUpload() checking messages");
		if(state.getAttribute(ResourcesAction.STATE_MESSAGE) == null)
		{
			mfp.setActionCanceled(false);
			mfp.setErrorEncountered(false);
			mfp.setActionCompleted(true);

			logger.debug(this + ".doUpload() no error messages");

			toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		}

	}
	
	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		logger.debug(this + ".initHelper()");
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		//toolSession.setAttribute(ResourceToolAction.STARTED, Boolean.TRUE);
		//state.setAttribute(ResourceToolAction.STATE_MODE, MODE_MAIN);
		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
		{
			String usingCreativeCommons = ServerConfigurationService.getString("copyright.use_creative_commons");
			if( usingCreativeCommons != null && usingCreativeCommons.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_TYPES) == null)
		{
			if (ServerConfigurationService.getStrings("copyrighttype") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
			}
		}

		if (state.getAttribute(STATE_DEFAULT_COPYRIGHT_ALERT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright.alert") != null)
			{
				state.setAttribute(STATE_DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
			}
		}

		if (state.getAttribute(STATE_NEW_COPYRIGHT_INPUT) == null)
		{
			if (ServerConfigurationService.getString("newcopyrightinput") != null)
			{
				state.setAttribute(STATE_NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_FAIRUSE_URL) == null)
		{
			if (ServerConfigurationService.getString("fairuse.url") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
			}
		}

		if (state.getAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.new") != null)
			{
				state.setAttribute(STATE_COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
			}
		}

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			String uploadMax = ServerConfigurationService.getString("content.upload.max");
			String uploadCeiling = ServerConfigurationService.getString("content.upload.ceiling");
			
			if(uploadMax == null && uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, "1");
			}
			else if(uploadCeiling == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
			}
			else if(uploadMax == null)
			{
				state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, null);
			}
			else
			{
				int maxNum = Integer.MAX_VALUE;
				int ceilingNum = Integer.MAX_VALUE;
				try
				{
					maxNum = Integer.parseInt(uploadMax);
				}
				catch(Exception e)
				{
				}
				try
				{
					ceilingNum = Integer.parseInt(uploadCeiling);
				}
				catch(Exception e)
				{
				}

				if(ceilingNum < maxNum)
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadCeiling);
				}
				else
				{
					state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, uploadMax);
				}
			}
			
		}
		
		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
		String siteType = null;
		Site site;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			siteType = site.getType();
			if(siteTypes != null)
			{
				for(int i = 0; i < siteTypes.length; i++)
				{
					if ((StringUtils.trimToNull(siteTypes[i])).equals(siteType))
					{
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
						break;
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			// allow public display
		}
		catch(NullPointerException e)
		{
			// allow public display
		}

		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, org.sakaiproject.content.cover.ContentTypeImageService.getInstance());
	}
	
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
		throws ToolException
	{
		logger.debug(this + ".toolModeDispatch()");
		SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);
		
		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = ToolManager.getCurrentTool();
		
			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
			logger.debug(this + ".toolModeDispatch() url == " + url);
		
			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				logger.warn(this + ".toolModeDispatch() IOException", e);
			}
			logger.debug(this + ".toolModeDispatch() returning");
			return;
		}
		logger.debug(this + ".toolModeDispatch() calling super.toolModeDispatch(" + methodBase + ", " + "methodExt" + ", " + req + ", " + res+ ")");
		
		super.toolModeDispatch(methodBase, methodExt, req, res);
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String dragDropFile = request.getHeader("x-file-name");

        if(dragDropFile != null){
            if (request.getAttribute("file") == null) {
                //If there is an x-file-name attribute but no file attribute, then it is a folder.
                addAlert(getState(request), rb.getString("alert.can-not-upload-folder"));
            } else {
                String uploadMax = ServerConfigurationService.getString("content.upload.max");
                String siteQuota = ServerConfigurationService.getString("siteQuota@org.sakaiproject.content.api.ContentHostingService");
                Long fileSize = Long.parseLong(request.getHeader("content-length"));
                if ((uploadMax != null && !"".equals(uploadMax)) && (fileSize / 1024L / 1024L) > Long.parseLong(uploadMax)) {
                    addAlert(getState(request), rb.getFormattedMessage("alert.over-per-upload-quota", new Object[]{uploadMax}));
                } else if ((siteQuota != null && !"".equals(siteQuota)) && (fileSize / 1024L / 1024L) > Long.parseLong(siteQuota)) {
                    addAlert(getState(request), rb.getFormattedMessage("alert.over-site-upload-quota", new Object[]{siteQuota}));
                } else {
                    doDragDropUpload(request, response);
                }
            }
        } else{
            super.doPost(request, response);
        }
    }

    private void doDragDropUpload(HttpServletRequest request, HttpServletResponse response) {
        String uploadFileName = request.getHeader("x-file-name");
        SessionState state = getState(request);
        ToolSession toolSession = SessionManager.getCurrentToolSession();
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        if (uploadFileName != null && !uploadFileName.isEmpty()) {
            InputStream is = null;
            ContentResourceEdit resource = null;
            try {
                String resourceGroup = toolSession.getAttribute("resources.request.create_wizard_collection_id").toString();
                String resourceName = getUniqueFileName(uploadFileName, resourceGroup);
                resource = ContentHostingService.addResource(resourceGroup + resourceName);
                if (resource != null) {
                    ResourcePropertiesEdit resourceProps = resource.getPropertiesEdit();
                    resourceProps.addProperty(ResourcePropertiesEdit.PROP_DISPLAY_NAME, resourceName);
                    resourceProps.addProperty(ResourcePropertiesEdit.PROP_COPYRIGHT_CHOICE, ServerConfigurationService.getString("default.copyright"));
                    resource.setContentType(mimeTypesMap.getContentType(resourceName));

                    DiskFileItem uploadFile = (DiskFileItem) request.getAttribute("file");
                    if(uploadFile != null){
                        resource.setContent(uploadFile.getInputStream());
                        ContentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
                    }
                    if (uploadFileName.contains(".zip")) {
                        ZipContentUtil zipContentUtil = new ZipContentUtil();
                        zipContentUtil.extractArchive(EntityManager.newReference(resource.getReference()));
                    }
                }
            } catch (OverQuotaException e) {
			    addAlert(state, rb.getString("alert.over-site-upload-quota"));
                logger.warn("Drag and drop upload failed: " + e, e);
            } catch (Exception e) {
                logger.warn("Drag and drop upload failed: " + e, e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (uploadFileName.contains(".zip")) {
                        //remove the zip file after unpacking it
                        try {
                            ContentHostingService.removeResource(resource.getId());
                        } catch (Exception e) {
                            logger.warn("Unable to remove zip file: " + e, e);
                        }
                    }
                } catch (IOException e) {
                    logger.warn("Caught exception: " + e, e);
                }
            }
        }
    }

    private String getUniqueFileName(String uploadFileName, String resourceGroup) throws org.sakaiproject.exception.PermissionException, org.sakaiproject.exception.TypeException {
        String resourceId = "";
        boolean isNameUnique = false;
        String fileName = uploadFileName;
        int attempt = 0;
        while (!isNameUnique) {
            try {
                resourceId = resourceGroup + fileName;
                ContentResource tempEdit = ContentHostingService.getResource(resourceId);
                if(tempEdit != null){
                    attempt++;
                    StringBuffer fileNameBuffer = new StringBuffer();
                    if(attempt > 1){
                        fileNameBuffer.append(fileName.substring(0, fileName.lastIndexOf("-")));
                    } else{
                        fileNameBuffer.append(fileName.substring(0, fileName.lastIndexOf(".")));
                    }
                    fileNameBuffer.append("-");
                    fileNameBuffer.append(attempt);
                    fileNameBuffer.append(fileName.substring(fileName.lastIndexOf("."), fileName.length()));
                    fileName = fileNameBuffer.toString();
                }
            } catch (IdUnusedException e) {
                isNameUnique = true;
            }
        }
        return fileName;
    }
}
