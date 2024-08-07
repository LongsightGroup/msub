package org.sakaiproject.sitemanage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;



public class ETSUserNotificationProviderImpl implements UserNotificationProvider {
	
	private static Log M_log = LogFactory.getLog(ETSUserNotificationProviderImpl.class);
	
	private static String NOTIFY_ADDED_PARTICIPANT ="sitemange.notifyAddedParticipant";

	private static String NOTIFY_NEW_USER ="sitemanage.notifyNewUserEmail"; 
	
	private static String NOTIFY_TEMPLATE_USE = "sitemanage.notifyTemplateUse";
	
	// to send an email to course authorizer based on course site request
	private static String NOTITY_COURSE_REQUEST_AUTHORIZER = "sitemanage.notifyCourseRequestAuthorizer";
	
	// to send an email to course site requestor
	private static String NOTIFY_COURSE_REQUEST_REQUESTER = "sitemanage.notifyCourseRequestRequester";
	
	// to send an email to support team about course request
	private static String NOTIFY_COURSE_REQUEST_SUPPORT = "sitemanage.notifyCourseRequestSupport";
	
	private static String NOTIFY_SITE_CREATION = "sitemanage.notifySiteCreation";
	
	private static final String ADMIN = "admin";
	
	private EmailService emailService; 
	
	
	
	public void setEmailService(EmailService es) {
		emailService = es;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService scs) {
		serverConfigurationService = scs;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService uds) {
		userDirectoryService = uds;
	}
	
	private EmailTemplateService emailTemplateService;
	public void setEmailTemplateService(EmailTemplateService ets) {
		emailTemplateService = ets;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager s) {
		this.sessionManager = s;
	}

	public void init() {
		//nothing realy to do
		M_log.info("init()");
		
		
		//do we need to load data?
		Map<String, String> replacementValues = new HashMap<String, String>();
		
		// put placeholders for replacement values 
		replacementValues.put("userName", "");
        replacementValues.put("userEid", "");
        replacementValues.put("localSakaiName", "");
        replacementValues.put("currentUserName", "");
        replacementValues.put("currentUserDisplayName", "");
        replacementValues.put("localSakaiURL", "");
        replacementValues.put("siteName", "");
        replacementValues.put("productionSiteName", "");
        replacementValues.put("newNonOfficialAccount", "false");
        replacementValues.put("newPassword", "");
        replacementValues.put("productionSiteName", "");
        
    	loadTemplate("notifyAddedParticipants.xml", NOTIFY_ADDED_PARTICIPANT);
    	loadTemplate("notifyNewuser.xml", NOTIFY_NEW_USER);
    	loadTemplate("notifyTemplateUse.xml", NOTIFY_TEMPLATE_USE);
    	loadTemplate("notifyCourseRequestAuthorizer.xml", NOTITY_COURSE_REQUEST_AUTHORIZER);
    	loadTemplate("notifyCourseRequestRequester.xml", NOTIFY_COURSE_REQUEST_REQUESTER);
    	loadTemplate("notifyCourseRequestSupport.xml", NOTIFY_COURSE_REQUEST_SUPPORT);
    	loadTemplate("notifySiteCreation.xml", NOTIFY_SITE_CREATION);
			
	}
	
	public void notifyAddedParticipant(boolean newNonOfficialAccount,
			User user, String siteTitle) {
		
		String from = serverConfigurationService.getBoolean(NOTIFY_FROM_CURRENT_USER, false)?
				getCurrentUserEmailAddress():getSetupRequestEmailAddress();
		//we need to get the template
		


		if (from != null) {
			String productionSiteName = serverConfigurationService.getString(
					"ui.service", "");
			String emailId = user.getEmail();
			String to = emailId;
			String headerTo = emailId;
			String replyTo = from;
			Map<String, String> rv = new HashMap<String, String>();
			rv.put("productionSiteName", productionSiteName);

			
			String content = "";
			/*
			 * $userName
			 * $localSakaiName
			 * $currentUserName
			 * $localSakaiUrl
			 */
			 Map<String, String> replacementValues = new HashMap<String, String>();
			 replacementValues.put("userName", user.getDisplayName());
			 replacementValues.put("userEid", user.getEid());
			 replacementValues.put("localSakaiName",serverConfigurationService.getString(
	    				"ui.service", ""));
			 replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
			 replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
			 String nonOfficialAccountUrl = serverConfigurationService.getString("nonOfficialAccount.url", null);
			 replacementValues.put("hasNonOfficialAccountUrl", nonOfficialAccountUrl!=null?Boolean.TRUE.toString().toLowerCase():Boolean.FALSE.toString().toLowerCase());
			 replacementValues.put("nonOfficialAccountUrl",nonOfficialAccountUrl);
			 replacementValues.put("siteName", siteTitle);
			 replacementValues.put("productionSiteName", productionSiteName);
			 replacementValues.put("newNonOfficialAccount", Boolean.valueOf(newNonOfficialAccount).toString().toLowerCase());
			 
			 // send email
			 emailTemplateServiceSend(NOTIFY_ADDED_PARTICIPANT, null, user, from, to, headerTo, replyTo, replacementValues);
				

		} // if

	}

	public void notifyNewUserEmail(User user, String newUserPassword,
			String siteTitle) {
		
		
		String from = getSetupRequestEmailAddress();
		String productionSiteName = serverConfigurationService.getString(
				"ui.service", "");
		String newUserEmail = user.getEmail();
		String to = newUserEmail;
		String headerTo = newUserEmail;
		String replyTo = from;
		String content = "";

		if (from != null && newUserEmail != null) {
			/*
			 * $userName
			 * $localSakaiName
			 * $currentUserName
			 * $localSakaiUrl
			 */
			Map<String, String> replacementValues = new HashMap<String, String>();
			replacementValues.put("userName", user.getDisplayName());
			replacementValues.put("localSakaiName",serverConfigurationService.getString("ui.service", ""));
			replacementValues.put("currentUserName",userDirectoryService.getCurrentUser().getDisplayName());
			replacementValues.put("userEid", user.getEid());
			replacementValues.put("localSakaiUrl", serverConfigurationService.getPortalUrl());
			replacementValues.put("newPassword",newUserPassword);
			replacementValues.put("siteName", siteTitle);
			replacementValues.put("productionSiteName", productionSiteName);

			// send email
			emailTemplateServiceSend(NOTIFY_NEW_USER, null, user, from, to, headerTo, replyTo, replacementValues);
		}
	}
	
	public void notifyTemplateUse(Site templateSite, User currentUser, Site site) {
		// send an email to track who are using the template
		String from = getSetupRequestEmailAddress();
		// send it to the email archive of the template site
		// TODO: need a better way to get the email archive address
		//String domain = from.substring(from.indexOf('@'));
		String templateEmailArchive = templateSite.getId() + "@" + serverConfigurationService.getServerName();
		String to = templateEmailArchive;
		String headerTo = templateEmailArchive;
		String replyTo = templateEmailArchive;
		String content = "";					

		if (from != null && templateEmailArchive != null) {
			Map<String, String> replacementValues = new HashMap<String, String>();
			replacementValues.put("templateSiteTitle", templateSite.getTitle());
			replacementValues.put("templateSiteId", templateSite.getId());
			replacementValues.put("currentUserDisplayName", currentUser.getDisplayName());
			replacementValues.put("currentUserDisplayId", currentUser.getDisplayId());
			SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
			dform.applyPattern("yyyy-MM-dd HH:mm:ss");
			String dateDisplay = dform.format(new Date());
			replacementValues.put("currentDate", dateDisplay);
			replacementValues.put("newSiteId", site.getId());
			replacementValues.put("newSiteTitle", site.getTitle());
			
			emailTemplateServiceSend(NOTIFY_TEMPLATE_USE, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		}
	}
	
	public boolean notifyCourseRequestAuthorizer(String instructorId, String requestEmail, String replyToEmail, String termTitle, String requestSectionInfo, String siteTitle, String siteId, String additionalInfo, String serverName)
	{
		try {
			User instructor = userDirectoryService.getUserByEid(instructorId);
			ResourceLoader rb = new ResourceLoader(instructorId, "UserNotificationProvider");
			StringBuffer buf = new StringBuffer();
			String to = instructor.getEmail();	
			String from = requestEmail;
			String headerTo = to;
			String replyTo = replyToEmail;
			User currentUser = userDirectoryService.getCurrentUser();
			String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
			String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
			
			
			Map<String, String> replacementValues = new HashMap<String, String>();
			replacementValues.put("currentUserDisplayName", currentUserDisplayName);
			replacementValues.put("currentUserDisplayId", currentUserDisplayId);
			replacementValues.put("termTitle", termTitle);
			replacementValues.put("requestSectionInfo", requestSectionInfo);
			replacementValues.put("siteTitle", siteTitle);
			replacementValues.put("siteId", siteId);
			replacementValues.put("specialInstruction", additionalInfo);
			replacementValues.put("serverName", serverName);
			
			return emailTemplateServiceSend(NOTITY_COURSE_REQUEST_AUTHORIZER, null, instructor, from, to, headerTo, replyTo, replacementValues) != null? true:false;
			
		}
		catch (Exception e)
		{
			M_log.warn(this + " cannot find user " + instructorId);
			return false;
		}
	}
	
	public String notifyCourseRequestSupport(String requestEmail, String serverName, String request, String termTitle, int requestListSize, String requestSectionInfo,
			String officialAccountName, String siteTitle, String siteId, String additionalInfo, boolean requireAuthorizer, String authorizerNotified, String authorizerNotNotified)
	{
		ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
		

		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
			
			
		// To Support
		String from = currentUserEmail;
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("requestSectionInfo", requestSectionInfo);
		replacementValues.put("requestListSize", String.valueOf(requestListSize));
		replacementValues.put("siteTitle", siteTitle);
		replacementValues.put("siteId", siteId);
		replacementValues.put("specialInstruction", additionalInfo);
		replacementValues.put("serverName", serverName);
		
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
		replacementValues.put("dateDisplay", dateDisplay);
		
		replacementValues.put("requireAuthorizer", String.valueOf(requireAuthorizer));
		replacementValues.put("authorizerNotified", authorizerNotified);
		replacementValues.put("authorizerNotNotified", authorizerNotNotified);
		
		try
		{
			return emailTemplateServiceSend(NOTIFY_COURSE_REQUEST_SUPPORT, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
		}
		catch (Exception e)
		{
			M_log.warn(this + " problem in send site request email to support for " + currentUserDisplayName );
			return "";
		}
	}
	
	public void notifyCourseRequestRequester(String requestEmail, String supportEmailContent, String termTitle)
	{
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserId = currentUser!=null?currentUser.getId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
		

		ResourceLoader rb = new ResourceLoader(currentUserId, "UserNotificationProvider");
		
		String from = requestEmail;
		String to = currentUserEmail;
		String headerTo = to;
		String replyTo = to;
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("currentUserEmail", currentUserEmail);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("supportEmailContent", supportEmailContent);
		replacementValues.put("requestEmail", requestEmail);
		
		emailTemplateServiceSend(NOTIFY_COURSE_REQUEST_REQUESTER, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
	}
	
	public void notifySiteCreation(Site site, List notifySites, boolean courseSite, String termTitle, String requestEmail) {
		User currentUser = userDirectoryService.getCurrentUser();
		String currentUserDisplayName = currentUser!=null?currentUser.getDisplayName():"";
		String currentUserDisplayId = currentUser!=null?currentUser.getDisplayId():"";
		String currentUserId = currentUser!=null?currentUser.getId():"";
		String currentUserEmail = currentUser!=null?currentUser.getEmail():"";
		
		SimpleDateFormat dform = ((SimpleDateFormat) DateFormat.getDateInstance());
        dform.applyPattern("yyyy-MM-dd HH:mm:ss");
        String dateDisplay = dform.format(new Date());
		
		ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
		
		String from = currentUserEmail;
		String to = requestEmail;
		String headerTo = requestEmail;
		String replyTo = currentUserEmail;
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("currentUserDisplayName", currentUserDisplayName);
		replacementValues.put("currentUserDisplayId", currentUserDisplayId);
		replacementValues.put("currentUserEmail", currentUserEmail);
		replacementValues.put("dateDisplay", dateDisplay);
		replacementValues.put("termTitle", termTitle);
		replacementValues.put("serverName", serverConfigurationService.getServerName());
		replacementValues.put("siteTitle", site!=null?site.getTitle():"");
		replacementValues.put("siteId", site!=null?site.getId():"");
		replacementValues.put("courseSite", String.valueOf(courseSite));
		
		StringBuffer buf = new StringBuffer();
		if (notifySites!= null)
		{
			int nbr_sections = notifySites.size();
			replacementValues.put("numSections", String.valueOf(nbr_sections));
			for (int i = 0; i < nbr_sections; i++) {
				String course = (String) notifySites.get(i);
				buf.append(course + "\n");
			}
		}
		else
		{
			replacementValues.put("numSections", "0");
		}
		replacementValues.put("sections", buf.toString());
		
		emailTemplateServiceSend(NOTIFY_SITE_CREATION, (new ResourceLoader()).getLocale(), currentUser, from, to, headerTo, replyTo, replacementValues);
	
	}
	
	/*
	 *  Private methods
	 */
	
	private String getSetupRequestEmailAddress() {
		String from = serverConfigurationService.getString("setup.request",
				null);
		if (from == null) {
			M_log.warn(this + " - no 'setup.request' in configuration");
			from = "postmaster@".concat(serverConfigurationService
					.getServerName());
		}
		return from;
	}

	@SuppressWarnings("unchecked")
	private void loadTemplate(String templateFileName, String templateRegistrationString) 
	{
		M_log.info(this + " loading template " + templateFileName);
		//we need a user session to avoid potential NPE's
		Session sakaiSession = sessionManager.getCurrentSession();
		try {
			sakaiSession.setUserId(ADMIN);
		    sakaiSession.setUserEid(ADMIN);
			InputStream in = ETSUserNotificationProviderImpl.class.getClassLoader().getResourceAsStream(templateFileName);
			Document document = new SAXBuilder(  ).build(in);
			List<Element> it = document.getRootElement().getChildren("emailTemplate");
			
			for (int i =0; i < it.size(); i++) {
				Element xmlTemplate = (Element)it.get(i);
				xmlToTemplate(xmlTemplate, templateRegistrationString);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally
		{
			sakaiSession.setUserId(null);
		    sakaiSession.setUserEid(null);
		}
	}

	private void xmlToTemplate(Element xmlTemplate, String key) {
		String subject = xmlTemplate.getChildText("subject");
		String body = xmlTemplate.getChildText("message");
		String locale = xmlTemplate.getChildText("locale");
		String versionString = xmlTemplate.getChildText("version");
		Locale loc = null;
		
		if (locale != null && !"".equals(locale)) {
			loc = LocaleUtils.toLocale(locale);
		}
		
		
		if (!emailTemplateService.templateExists(key, loc))
		{
			EmailTemplate template = new EmailTemplate();
			template.setSubject(subject);
			template.setMessage(body);
			template.setLocale(locale);
			template.setKey(key);
			template.setVersion(Integer.valueOf(1));//setVersion(versionString != null ? Integer.valueOf(versionString) : Integer.valueOf(0));	// set version
			template.setOwner("admin");
			template.setLastModified(new Date());
			this.emailTemplateService.saveTemplate(template);
			M_log.info(this + " user notification template " + key + " added");
		}
		else
		{
			EmailTemplate existingTemplate = this.emailTemplateService.getEmailTemplate(key, new Locale(locale));
			String oVersionString = existingTemplate.getVersion() != null ? existingTemplate.getVersion().toString():null;
			if ((oVersionString == null && versionString != null) || (oVersionString != null && versionString != null && !oVersionString.equals(versionString)))
			{
				existingTemplate.setSubject(subject);
				existingTemplate.setMessage(body);
				existingTemplate.setLocale(locale);
				existingTemplate.setKey(key);
				existingTemplate.setVersion(versionString != null ? Integer.valueOf(versionString) : Integer.valueOf(0));	// set version
				existingTemplate.setOwner("admin");
				existingTemplate.setLastModified(new Date());
				this.emailTemplateService.updateTemplate(existingTemplate);
			M_log.info(this + " user notification template " + key + " updated to newer version");
			}
		}
			
	}

	private String getCurrentUserEmailAddress() {
		User currentUser = userDirectoryService.getCurrentUser();
		String email = currentUser != null ? currentUser.getEmail():null;
		if (email == null || email.length() == 0) {
			email = getSetupRequestEmailAddress();
		}
		return email;
	}
	
	/**
	 * use EmailTemplateService to send email
	 * @param templateName
	 * @param user
	 * @param from
	 * @param to
	 * @param headerTo
	 * @param replyTo
	 * @param replacementValues
	 * @return the email content
	 */
	private String emailTemplateServiceSend(String templateName, Locale locale, User user, String from, String to, String headerTo, String replyTo, Map<String, String> replacementValues) {
		M_log.debug("getting template: " + templateName);
		RenderedTemplate template = null;
		try { 
			if (locale == null)
			{
				// use user's locale
				template = emailTemplateService.getRenderedTemplateForUser(templateName, user!=null?user.getReference():"", replacementValues);
			}
			else
			{
				// use local
				template = emailTemplateService.getRenderedTemplate(templateName, locale, replacementValues);
			}
			if (template != null)
			{
				List<String> headers = new ArrayList<String>();
				headers.add("Precedence: bulk");
				
				String content = template.getRenderedMessage();	
				emailService.send(from, to, template.getRenderedSubject(), content, headerTo, replyTo, headers);
				return content;
			}
       }
       catch (Exception e) {
    	   M_log.warn(this + e.getMessage());
    	   return null;
       }
       return null;
	}
	
	public void notifySiteImportCompleted(String toEmail, String siteId, String siteTitle){
		if(toEmail != null && !"".equals(toEmail)){
			String headerTo = toEmail;
			String replyTo = toEmail;
			ResourceLoader rb = new ResourceLoader("UserNotificationProvider");
			String message_subject = rb.getFormattedMessage("java.siteImport.confirmation.subject", new Object[]{siteTitle});
			String message_body = rb.getFormattedMessage("java.siteImport.confirmation", new Object[]{siteId, siteTitle});
			emailService.send(getSetupRequestEmailAddress(), toEmail, message_subject, message_body, headerTo, replyTo, null);
		}
	}
}
