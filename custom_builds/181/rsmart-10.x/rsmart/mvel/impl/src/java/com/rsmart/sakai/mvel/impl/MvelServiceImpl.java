package com.rsmart.sakai.mvel.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.ActivityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.BasicConfigItem;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsmart.sakai.mvel.api.MvelService;

/**
 * MvelServiceImpl
 *
 * @author Earle Nietzel
 * Created on Nov 29, 2012
 * 
 */
public class MvelServiceImpl implements MvelService {
	private static final Log LOG = LogFactory.getLog(MvelServiceImpl.class);
	
	private ObjectMapper mapper;
	private ParserContext context;
	
    private SessionManager sessionManager;
	private AuthzGroupService authzGroupService;
	private CalendarService calendarService;
	private EventTrackingService eventTrackingService;
	private SecurityService securityService;
	private ServerConfigurationService serverConfigurationService;
	private SiteService siteService;
	private SqlService sqlService;
	private ToolManager toolManager;
	private UsageSessionService usageSessionService;
	private UserDirectoryService userDirectoryService;
	private ContentHostingService contentHostingService;
	private EntityManager entityManager;
	private ActivityService activityService;
	private VariableResolverFactory sakaiVariableResolverFactory;

	public void init() {
		Map<String, Object> sakaiVars = new HashMap<String, Object>();
		
		/* check services for null before adding them to the ParserContext
		 * this avoids errors when running unit tests
		 * TODO maybe think about mocking these up
		 */ 
		if (authzGroupService != null) 			{ sakaiVars.put("authzGroupService", authzGroupService); };
		if (calendarService != null) 			{ sakaiVars.put("calendarService", calendarService); };
		if (eventTrackingService != null) 		{ sakaiVars.put("eventTrackingService", eventTrackingService); };
		if (securityService != null) 			{ sakaiVars.put("securityService", securityService); };
		if (serverConfigurationService != null) { sakaiVars.put("serverConfigurationService", serverConfigurationService); };
		if (sessionManager != null)				{ sakaiVars.put("sessionManager", sessionManager); };
		if (siteService != null) 				{ sakaiVars.put("siteService", siteService); };
		if (sqlService != null) 				{ sakaiVars.put("sqlService", sqlService); };
		if (toolManager != null) 				{ sakaiVars.put("toolManager", toolManager); };
		if (usageSessionService != null) 		{ sakaiVars.put("usageSessionService", usageSessionService); };
		if (userDirectoryService != null) 		{ sakaiVars.put("userDirectoryService", userDirectoryService); };
		if (contentHostingService != null) 		{ sakaiVars.put("contentHostingService", contentHostingService); };
		if (entityManager != null) 				{ sakaiVars.put("entityManager", entityManager); };
		if (activityService != null) 			{ sakaiVars.put("activityService", activityService); };
		
		sakaiVariableResolverFactory = new MapVariableResolverFactory(sakaiVars);
		
		context = new ParserContext();
		
		context.addImport(BasicConfigItem.class);
		context.addImport(SelectionType.class);
		context.addImport(SortType.class);

		mapper = new ObjectMapper();
	}

	@Override
	public Object evaluate(String expression) {
		return evaluate(expression, new HashMap<String, Object>());
	}

	@Override
	public Object evaluate(String expression, Map<String, Object> parameters) {
		if (parameters == null) {
			parameters = new HashMap<String, Object>();
		}
		
		VariableResolverFactory topVariableResolverFactory = new MapVariableResolverFactory(parameters);
		topVariableResolverFactory.setNextFactory(sakaiVariableResolverFactory);

		Serializable compiledExpr = MVEL.compileExpression(decodeBase64String(expression), context);
		//return (Object) MVEL.eval(expression, parameters);
		return (Object) MVEL.executeExpression(compiledExpr, topVariableResolverFactory);
	}

	@Override
	public String evaluateAsString(String expression) {
		return evaluateAsString(expression, new HashMap<String, Object>());
	}
	
	@Override
	public String evaluateAsString(String expression, Map<String, Object> parameters) {

		Object result = evaluate(decodeBase64String(expression), parameters);
		
		if (result instanceof String) {
			return (String) result;
		}
		if (result instanceof Number) {
			return String.valueOf(result);
		}
		if (result instanceof Boolean) {
			return String.valueOf(result);
		}
		if (result instanceof Collection) {
			StringBuffer str = new StringBuffer();
			Collection coll = (Collection) result;
			for (Iterator iterator = coll.iterator(); iterator.hasNext();) {
				Object obj = (Object) iterator.next();
				str.append(obj.toString());
				str.append("\n");
			}
			return str.toString();
		}
		
		return result.toString();
	}

	@Override
	public String evaluateAsJson(String expression) {
		return evaluateAsJson(expression, new HashMap<String, Object>());
	}

	@Override
	public String evaluateAsJson(String expression, Map<String, Object> parameters) {
		
		Object result = evaluate(decodeBase64String(expression), parameters);
		
		return "This method is not yet implemented.";
	}
	
	@Override
	public String evaluateAsXml(String expression) {
		return evaluateAsXml(expression, new HashMap<String, Object>());
	}

	@Override
	public String evaluateAsXml(String expression, Map<String, Object> parameters) {
		Object result = evaluate(decodeBase64String(expression), parameters);
		
		return "This method is not yet implemented.";
	}
	
	@Override
	public Object evaluate(String expression, String json) {
		return evaluate(expression, deserializeJson(json));
	}

	@Override
	public String evaluateAsString(String expression, String json) {
		return evaluateAsString(expression, deserializeJson(json));
	}

	@Override
	public String evaluateAsJson(String expression, String json) {
		return evaluateAsJson(expression, deserializeJson(json));
	}

	@Override
	public String evaluateAsXml(String expression, String json) {
		return evaluateAsXml(expression, deserializeJson(json));
	}
	
	private String decodeBase64String(String text) {
		
		if (Base64.isBase64(text)) {
			text = new String(Base64.decodeBase64(text));
		}
		return text;
	}
	
	private Map<String, Object> deserializeJson(String json) {
		Map<String,Object> map = null;
		
		if (StringUtils.isBlank(json)) {
			LOG.warn("Called deserializeJson() with an empty String");
			map = Collections.emptyMap();
			return map;
		}
		
		try {
			map = mapper.readValue(json, new TypeReference<Map<String, Object>>() { });
		} catch (JsonParseException jpe) {
			LOG.error("Error while parsing JSON: " + json, jpe);
		} catch (JsonMappingException jme) {
			LOG.error("Error while mapping JSON: " + json, jme);
		} catch (IOException ioe) {
			LOG.error("IO Error reading JSON: " + json, ioe);
		} finally {
			if (map == null) {
				LOG.warn("Could not deserialize json: " + json);
				map = Collections.emptyMap();
			}
		}
		return map;
	}
	
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}
}
