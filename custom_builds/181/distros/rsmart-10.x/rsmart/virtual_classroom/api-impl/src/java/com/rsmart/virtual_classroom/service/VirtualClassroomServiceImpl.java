/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.service;

import com.rsmart.virtual_classroom.intf.VirtualClassroomService;
import com.rsmart.virtual_classroom.model.AuthorizationException;
import com.rsmart.virtual_classroom.model.CalendarException;
import com.rsmart.virtual_classroom.model.VirtualClassroomException;
import com.rsmart.virtual_classroom.model.VirtualClassroomSession;
import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * This base class contains code and methods that are common to all implementations of the virtual classroom service interface.
 */
public abstract class VirtualClassroomServiceImpl extends HibernateDaoSupport implements VirtualClassroomService {
   // logger
   private final transient Log logger = LogFactory.getLog(getClass());

   // data members populated by spring in the components.xml file
   private CalendarService            calendarService;
   private FunctionManager            functionManager;
   private MessageSource              messageSource;
   private SecurityService            securityService;
   private ServerConfigurationService serverConfigurationService;
   private SiteService                siteService;
   private TimeService                timeService;
// private UsageSessionService        usageSessionService;
   private UserDirectoryService       userDirectoryService;

   // virtual classroom server properties obtained from sakai.properties
   private String server;           // ip address or dns name of the machine where the virtual classroom server is running.
   private int    port;             // port on which the virtual classroom server is listening.
   private int    defaultCapacity;  // default capacity to use when creating a new virtual classroom session
   private int    gracePeriod;      // specifies the time (in minutes) prior to the scheduled start time of the virtual classroom session during which a user can join. The options are 0, 15, 30, 45, and 60 minutes.
   private int    maxCapacity;      // specifies the maximum number of concurrent users allowed by the virtual classroom server.
   private String usernameFormat;   // specifies the format that a user's name will be displayed when a user joins a virtual classroom session.  valid values are: sakai_username, firstname_lastname, lastname_firstname, nickname, e-mail

   private String url;              // constructed url pointing to the virtual classroom session server

   private boolean isPrivateDefault;

   /**
    * default constructor.
    */
   public VirtualClassroomServiceImpl() {
      // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated.
    */
   public void init() {
      String defaultCapacityS;
      String gracePeriodS;
      String maxCapacityS;
      String portS;
      String errorMessage = "  The virtual classroom tool will not function properly without this property set to a valid value.  Using default value of ";

      // read in the values from the sakai.properties file pertaining to the external virtual classroom server
      server           = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADDRESS         );
      if (!server.toLowerCase().startsWith("http")) {
          server = "http://" + server;
      }
      defaultCapacityS = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_CAPACITY);
      portS            = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT            );
      gracePeriodS     = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD    );
      maxCapacityS     = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY    );
      isPrivateDefault = serverConfigurationService.getBoolean(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PRIVATE, false );
      usernameFormat   = serverConfigurationService.getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT );
      url              = "http://" + server + ":" + port;

      // parse the virtual classroom server address
      if (server == null || server.trim().length() == 0) {
         logger.error("No virtual classroom server address was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADDRESS +
                      " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_ADDRESS + ".");
         server = "http://" + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_ADDRESS;
      }

      // parse the virtual classroom server port
      if (portS == null || portS.trim().length() == 0) {
         logger.error("No virtual classroom server port was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT +
                      " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT + ".");
         port = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT;
      } else {
         try {
            port = Integer.valueOf(portS).intValue();
            if (port <= 0) {
               logger.error("The virtual classroom server port (" + port + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT +
                            " property is less than or equal to 0." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT + ".");
               port = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT;
            } else if (port >= 65536) {
               logger.error("The virtual classroom server port (" + port + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT +
                            " property is greater than or equal to 65536." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT + ".");
               port = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT;
            }
         } catch (NumberFormatException ex) {
            logger.error("Invalid virtual classroom server port  (" + portS + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT +
                         " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT + ".");
            port = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT;
         }
      }

      // parse the default capacity
      if (defaultCapacityS == null || defaultCapacityS.trim().length() == 0) {
         logger.error("No virtual classroom server default capacity was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_CAPACITY +
                      " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY + ".");
         defaultCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY;
      } else {
         try {
            defaultCapacity = Integer.valueOf(defaultCapacityS).intValue();
            if (defaultCapacity <= 0) {
               logger.error("The default capcity (" + defaultCapacity + ") for the virtual classroom server specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD +
                            " property is less than or equal to 0." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY + ".");
               defaultCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY;
            }
         } catch (NumberFormatException ex) {
            logger.error("Invalid virtual classroom server default capacity (" + defaultCapacityS + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD +
                         " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY + ".");
            defaultCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY;
         }
      }

      // parse the grace period
      if (gracePeriodS == null || gracePeriodS.trim().length() == 0) {
         logger.error("No virtual classroom server grace period was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD +
                      " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD +".");
         gracePeriod = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD;
      } else {
         try {
            gracePeriod = Integer.valueOf(gracePeriodS).intValue();
            if (gracePeriod <= 0) {
               logger.error("The grace period (" + gracePeriod + ") for the virtual classroom server specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD +
                            " property is less than or equal to 0." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD + ".");
               gracePeriod = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD;
            }
         } catch (NumberFormatException ex) {
            logger.error("Invalid grace period (" + gracePeriodS + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD +
                         " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD + ".");
            gracePeriod = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD;
         }
      }

      // parse the maximum number of concurrent users
      if (maxCapacityS == null || maxCapacityS.trim().length() == 0) {
         logger.error("No maximum number of concurrent users for the virtual classroom server was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY +
                      " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY + ".");
         maxCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY;
      } else {
         try {
            maxCapacity = Integer.valueOf(maxCapacityS).intValue();
            if (maxCapacity <= 0) {
               logger.error("The maximum number of concurrent users (" + maxCapacity + ") for the virtual classroom server specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY +
                            " property is less than or equal to 0." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY + ".");
               maxCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY;
            }
         } catch (NumberFormatException ex) {
            logger.error("Invalid maximum capacity (" + maxCapacityS + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY +
                         " property." + errorMessage + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY + ".");
            maxCapacity = SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY;
         }
      }

      // parse the username format
      if (usernameFormat == null || usernameFormat.trim().length() == 0) {
         logger.error("No virtual classroom server user name format was specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT + " property." + errorMessage);
      } else {
         if (!usernameFormat.equals(VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_1) &&
             !usernameFormat.equals(VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_2) &&
             !usernameFormat.equals(VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_3) &&
             !usernameFormat.equals(VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_4) &&
             !usernameFormat.equals(VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_5))
            logger.error("Invalid username format (" + usernameFormat + ") specified in sakai.properties via the " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT + " property." + errorMessage + ".  Valid values are: " +
                         VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_1 + ", " +
                         VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_2 + ", " +
                         VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_3 + ", " +
                         VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_4 + ", " +
                         VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_5 + ".");
      }

      // store the grace persiod in the model of the virtual classroom session
      VirtualClassroomSession.setGracePeriod(gracePeriod);

      logger.info("init(), server: " + server + ", port: " + port + ",  url: " + url);
      logger.info("init(), grace period: " + gracePeriod + ", max concurrent users: " + maxCapacity);

      // register permissions with sakai
      //functionManager.registerFunction(PERMISSION_CREATE);
      //functionManager.registerFunction(PERMISSION_DELETE);
      //functionManager.registerFunction(PERMISSION_EDIT  );
      //functionManager.registerFunction(PERMISSION_JOIN  );
      //functionManager.registerFunction(PERMISSION_VIEW  );
   }

   /**
    * called by the spring framework.
    */
   public void destroy() {
      // no code necessary
   }

   /**
    * returns the instance of the CalendarService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the CalendarService injected by the spring framework specified in the components.xml file via IoC.
    */
   public CalendarService getCalendarService() {
      return calendarService;
   }

   /**
    * called by the spring framework to initialize the CalendarService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param calendarService   the implementation of the CalendarService interface provided by the spring framework.
    */
   public void setCalendarService(CalendarService calendarService) {
      this.calendarService = calendarService;
   }

   /**
    * returns the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public FunctionManager getFunctionManager() {
      return functionManager;
   }

   /**
    * called by the spring framework to initialize the FunctionManager data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param functionManager   the implementation of the FunctionManager interface provided by the spring framework.
    */
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }

   /**
    * returns the locale of the jvm in which this service is running.
    * <br/><br/>
    * @return the locale of the jvm in which this service is running.
    */
   public Locale getLocale() {
      return LocaleContextHolder.getLocale();
   }

   /**
    * returns the instance of the MessageSource injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the MessageSource injected by the spring framework specified in the components.xml file via IoC.
    */
   public MessageSource getMessageSource() {
      return messageSource;
   }

   /**
    * called by the spring framework to initialize the MessageSource data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param messageSource   the message source specified in components.xml.
    */
   public void setMessageSource(MessageSource messageSource) {
      this.messageSource = messageSource;
   }


   /**
    * returns the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SecurityService getSecurityService() {
      return securityService;
   }

   /**
    * called by the spring framework to initialize the SecurityService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param securityService   the implementation of the SecurityService interface provided by the spring framework.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * returns the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    */
   public ServerConfigurationService getServerConfigurationService() {
       return serverConfigurationService;
   }

   /**
    * called by the spring framework to initialize the ServerConfigurationService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param serverConfigurationService   the implementation of the ServerConfigurationService interface provided by the spring framework.
    */
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
       this.serverConfigurationService = serverConfigurationService;
   }


   /**
    * returns the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SiteService getSiteService() {
      return siteService;
   }

   /**
    * called by the spring framework to initialize the SiteService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param siteService   the implementation of the SiteService interface provided by the spring framework.
    */
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   /**
    * returns the instance of the TimeService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the TimeService injected by the spring framework specified in the components.xml file via IoC.
    */
   public TimeService getTimeService() {
      return timeService;
   }

   /**
    * called by the spring framework to initialize the TimeService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param timeService   the implementation of the TimeService interface provided by the spring framework.
    */
   public void setTimeService(TimeService timeService) {
      this.timeService = timeService;
   }

   /**
    * returns the instance of the UserDirectoryService injected by the spring framework specified in the components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the UserDirectoryService injected by the spring framework specified in the components.xml file via IoC.
    */
   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   /**
    * called by the spring framework to initialize the UserDirectoryService data member specified in the components.xml file via IoC.
    * <br/><br/>
    * @param userDirectoryService   the implementation of the UserDirectoryService interface provided by the spring framework.
    */
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   /**
    * returns the default capacity of a virtual classroom session.  This value is used when a user goes to create a new virtual classroom session.
    */
   public int getDefaultCapacity() {
      return defaultCapacity;
   }

   /**
    * returns the time (in minutes) prior to the start of a virtual classroom session in which a user can join.
    */
   public int getGracePeriod() {
      return gracePeriod;
   }

   /**
    * returns the maximum number of concurrent users allowed by the virtual classroom server.
    */
   public int getMaxCapacity() {
       return maxCapacity;
   }

   /**
    * returns the port on which the virtual classroom server is listening.
    */
   public int getPort() {
       return port;
   }

   /**
    * returns the ip address or dns name of the machine where the virtual classroom server is running.
    */
   public String getServer() {
       return server;
   }

   /**
    * returns the url of the virtual classroom session server.
    */
   public String getUrl() {
       return url;
   }

   /**
    * sets the url of the virtual classroom session server.
    */
   public void setUrl(String url) {
      this.url = url;
   }

    /**
    * returns the format that a user's name will be displayed when a user joins a virtual classroom session.
    * valid values are: sakai_username, firstname_lastname, lastname_firstname, nickname, e-mail.
    */
   public String getUsernameFormat() {
       return usernameFormat;
   }

    /**
    * check whether the current user has permission to execute the specified action on the given virtual classroom session group.
    * <br/><br/>
    * @param virtualClassroomSessionGroup  the virtual classroom session group on which the user wants to perform the specified action (may be null).
    * @param action                        the action the user wishes to perform and whose permission must be checked against the user's role..
    * <br/><br/>
    * @return  whether the current user has permission to execute the specified action.
    * <br/><br/>
    * @throws IllegalArgumentException    if the specified permission action is not a supported action.
    */
   protected boolean checkPermission(VirtualClassroomSessionGroup virtualClassroomSessionGroup, String action) throws IllegalArgumentException {
      Site    site          = getCurrentSite();
      String  siteReference = null;

      if (site != null)
         siteReference = site.getReference();

      return checkPermission(virtualClassroomSessionGroup, siteReference, action);
   }

   /**
    * check whether the current user has permission to execute the specified action on the given virtual classroom session group.
    * <br/><br/>
    * @param virtualClassroomSessionGroup  the virtual classroom session group on which the user wants to perform the specified action (may be null).
    * @param action                        the action the user wishes to perform and whose permission must be checked against the user's role..
    * <br/><br/>
    * @return  whether the current user has permission to execute the specified action.
    * <br/><br/>
    * @throws IllegalArgumentException    if the specified permission action is not a supported action.
    */
   protected boolean checkPermission(VirtualClassroomSessionGroup virtualClassroomSessionGroup, String siteReference, String action) throws IllegalArgumentException {
      User    user          = getCurrentUser();
      boolean hasPermission = false;

      if (action == null || action.trim().length() == 0) {
         throw new IllegalArgumentException("the action can not be null or empty.");
      } else if (securityService.isSuperUser()) {
         hasPermission = true;         
      } else if (action.equals(PERMISSION_CREATE)) {
         hasPermission = securityService.unlock(user, action, siteReference);
      } else if (action.equals(PERMISSION_DELETE)) {
         hasPermission = securityService.unlock(user, action, siteReference) && (virtualClassroomSessionGroup == null || !virtualClassroomSessionGroup.isJoinable());
      } else if (action.equals(PERMISSION_EDIT)) {
         hasPermission = securityService.unlock(user, action, siteReference) && !virtualClassroomSessionGroup.isJoinable();
      } else if (action.equals(PERMISSION_JOIN)) {
         hasPermission = securityService.unlock(user, action, siteReference) &&  virtualClassroomSessionGroup.isJoinable();
      } else if (action.equals(PERMISSION_VIEW)) {
         hasPermission = securityService.unlock(user, action, siteReference);
      } else {
         throw new IllegalArgumentException(action + " is not a supported action.  It must be one of the following: " +
                                            PERMISSION_CREATE      + ", " +
                                            PERMISSION_DELETE      + ", " +
                                            PERMISSION_EDIT        + ", " +
                                            PERMISSION_JOIN        + ", " +
                                            PERMISSION_VIEW);
      }
      return hasPermission;
   }

   /**
    * adds an event to the calendar tool on the current worksite.
    * <br/><br/>
    * @param title        the name of the event.
    * @param description  a description of the event being added.
    * @param siteId       the siteId that holds the calendar tool we want to work with
    * @param startDate    the starting date\\time of the event.
    * @param endDate      the ending   date\\time of the event.
    * <br/><br/>
    * @throws CalendarException   if the event can not be added to the calendar.
    */
   protected CalendarEvent createCalendarEvent(String title, String description, String siteId, Date startDate, Date endDate) throws CalendarException {
      CalendarEvent calendarEvent = null;

      try {
         // add event to first calendar we find, we assume sites have only 1 calendar tool!!
         Site              site          = siteService.getSite(siteId);
         Collection        calendarTools = site.getTools("sakai.schedule");
         ToolConfiguration toolConfig    = (ToolConfiguration)calendarTools.iterator().next();
         String            calendarId    = calendarService.calendarReference(toolConfig.getContext(), SiteService.MAIN_CONTAINER);
         Calendar          calendar      = calendarService.getCalendar(calendarId);
         Time              startTime     = timeService.newTime(startDate.getTime());
         Time              endTime       = timeService.newTime(endDate  .getTime());
         TimeRange         range         = timeService.newTimeRange(startTime, endTime);
         String            eventType     = "Class session";    // built-in event type.  todo: explore adding a new type for virtual classroom session
         String            location      = messageSource.getMessage("calander_event_location", null, getLocale());
         ArrayList         attachments   = new ArrayList();

         calendarEvent = calendar.addEvent(range, title, description, eventType, location, attachments);
      } catch (Exception ex) {
         throw new CalendarException("error attempting to add a calendar event for the virtual classroom session, check if schedule tool has been added  " + title, ex);
      }
      return calendarEvent;
   }

   /**
    * deletes a event from the calendar tool on the current worksite.
    * <br/><br/>
    * @param eventId      the id of the event.
    * <br/><br/>
    * @param siteId
    * @throws CalendarException   if the event can not be added to the calendar.
    */
   protected CalendarEvent deleteCalendarEvent(String eventId, String siteId) throws CalendarException {
      CalendarEventEdit calendarEvent = null;

      try {
         // delete event from first calendar we find, we assume sites have only 1 calendar tool!!
         Site              site          = siteService.getSite(siteId);
         Collection        calendarTools = site.getTools("sakai.schedule");
         ToolConfiguration toolConfig    = (ToolConfiguration)calendarTools.iterator().next();
         String            calendarId    = calendarService.calendarReference(toolConfig.getContext(), SiteService.MAIN_CONTAINER);
         Calendar          calendar      = calendarService.getCalendar(calendarId);

         calendarEvent = calendar.getEditEvent(eventId, CalendarService.EVENT_REMOVE_CALENDAR);
         calendar.removeEvent(calendarEvent);
      } catch (Exception ex) {
         throw new CalendarException("error attempting to delete a calendar event for a virtual classroom session.", ex);
      }
      return calendarEvent;
   }

   /**
    * retrieve the current worksite.
    * Instead of throwing an exception if the worksite can not be obtained, simply return null.
    */
   public Site getCurrentSite() {
      Site site = null;
      try {
         Placement placement = ToolManager.getCurrentPlacement();
         String siteId = placement.getContext();
         site = siteService.getSite(placement.getContext());
      } catch (IdUnusedException ex) {
         logger.warn("Unable to retrieve the current worksite");
      }
      return site;
   }

   /**
    * returns the currrent user.
    */
   public User getCurrentUser() {
      return userDirectoryService.getCurrentUser();
   }

   /**
    * returns the equivalent sql column in the rcm_virtual_classroom_group table corresponding to the specified sort field.
    */
   protected String getDatabaseSortField(int sortField) {
      String column = null;

      switch (sortField) {
         case SORT_FIELD_JOIN:
              column = "schedule.endDate";
         break;
         case SORT_FIELD_NAME:
              column = "name";
         break;
         case SORT_FIELD_START_DATE:
              column = "schedule.startDate";
         break;
         default:
            throw new IllegalArgumentException("Invalid sort field: " + sortField + ".  It must be either SORT_FIELD_JOIN (" + SORT_FIELD_JOIN + "), SORT_FIELD_NAME (" + SORT_FIELD_NAME + "), or SORT_FIELD_START_DATE (" + SORT_FIELD_START_DATE + ").");
      }
      return column;
   }

   /**
    * returns the equivalent sql string corresponding to the specified sort order.
    */
   protected String getDatabaseSortOrder(int sortOrder) {
      String order = null;

      switch (sortOrder) {
         case SORT_ORDER_ASCENDING:
              order = "asc";
         break;
         case SORT_ORDER_DESCENDING:
              order = "desc";
         break;
         default:
            throw new IllegalArgumentException("Invalid sort order: " + sortOrder + ".  It must be either SORT_ORDER_ASCENDING (" + SORT_ORDER_ASCENDING + ") or SORT_ORDER_DESCENDING (" + SORT_ORDER_DESCENDING + ").");
      }
      return order;
   }

   /**
    * returns the user's name formatted according to the preference specified in the sakai.properties file specified by the <strong>SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT</strong> property.
    */
   protected String getDisplayedUsername(User user) {
      return user.getDisplayName();
   }

   /**
    * returns the maximum number of concurrent users the virtual classroom server supports.
    */
   public int getMaxNumConcurrentUsers() {
      return maxCapacity;
   }


   /**
    * returns the current number of users for the specified site.
    */
   public int getNumMembersOnSite(String siteId) {
      int numMembersOnSite = 0;

      try {
         Site site = siteService.getSite(siteId);

         if (site != null) {
            Set members = site.getMembers();
            numMembersOnSite = (members == null ? 0 : members.size());
         }
      } catch (IdUnusedException ex) {
         // can't find the specified site.
         // just return 0.
      }
      return numMembersOnSite;
   }

   /**
    * returns the reference corresponding to the specified site id.
    * <p>
    * the site reference is simply the site id prefixed with "/site/".
    * If no site with the specified id can be found, null is returned.
    * </p>
    */
   protected String getSiteReference(String siteId) {
      String siteReference = null;

      try {
         Site site = siteService.getSite(siteId);

         if (site != null)
            siteReference = site.getReference();
      } catch (IdUnusedException ex) {
         // can't find the specified site.
         // just return null.
      }
      return siteReference;
   }

   /**
    * returns the user with the specified sakai user id.
    * If no user can be found with the specified id, null is returned.
    */
   public User getUser(String id) {
      User user = null;

      try {
         if (id != null)
            user = userDirectoryService.getUser(id);
      } catch (UserNotDefinedException ex) {
         // don't do anything.   just return null for the user.
      }
      return user;
   }

   /**
    * retrieves the url which will allow a user to view a recording of an individual virtual classroom session.
    * <br/><br/>
    * @param elluminateId           id of the individual virtual classroom session recording to be viewed
    * @param elluminateRecordingId  recording id of the individual virtual classroom session recording to be viewed
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return   the url which will allow the user to view the recording of the individual virtual classroom session.
    */
   public abstract String getViewUrl(long elluminateId, String elluminateRecordingId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a string representation of the specified sort field and sort order.  Useful for log messages.
    * <br/><br/>
    * @param sortField  specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder  specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @return  a string representation of the specified sort field and sort order.
    */
   protected String sortParameters(int sortField, int sortOrder) {
      StringBuffer buffer = new StringBuffer();

      buffer.append("sort field: ");
      if (sortField == SORT_FIELD_JOIN)
         buffer.append("join");
      else if (sortField == SORT_FIELD_NAME)
         buffer.append("name");
      else if (sortField == SORT_FIELD_START_DATE)
         buffer.append("start date");
      buffer.append(", sort order: ");
      buffer.append(sortOrder == SORT_ORDER_ASCENDING ? "ascending" : "descending");

      return buffer.toString();
   }

   /**
    * creates an individual virtual classroom session.
    * <br/><br/>
    * @param virtualClassroomSession  The individual virtual classroom session to create.  The id field and the elluminate id do not need to be set, as they will be generated for you.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be saved to the sakai database, an event can not be added to the calendar,
    *                                     or if the individual virtual classroom session can not be created on the elluminate live server. If the result
    *                                     returned by the elluminate server can not be parsed into a valid xml document, an XmlException will
    *                                     be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  the newly created individual virtual classroom session.
    */
// public abstract VirtualClassroomSession createVirtualClassroomSession(VirtualClassroomSession virtualClassroomSession, boolean addEventToCalendar) throws AuthorizationException, VirtualClassroomException;

   /**
    * creates a virtual classroom session group and its child individual virtual classroom sessions.
    * This method will create the virtual classroom session group and then create all the individual virtual classroom sessions specified by the virtual classroom session group's schedule.
    * <br/><br/>
    * @param virtualClassroomSessionGroup  The virtual classroom session group to create.
    *                                      The id, siteId, createdBy, and createdOn fields will be set for you.
    *                                      If no instructorId has been set, then the sakai admin user will be set as the instructor.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be saved to the sakai database, an event can not be added to the calendar, or
    *                                     if the individual virtual classroom sessions can not be created on the elluminate live server. If the result
    *                                     returned by the elluminate server can not be parsed into a valid xml document, an XmlException will
    *                                     be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  the newly created virtual classroom session group.
    */
   public abstract VirtualClassroomSessionGroup createVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException;

   /**
    * deletes a virtual classroom session group and all its child individual virtual classroom sessions.
    * This method will delete the virtual classroom session group and all the individual virtual classroom sessions specified by the virtual classroom session group's schedule.
    *
    * <br/><br/>
    * @param id    the id of the virtual classroom session group to be deleted.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be deleted from the sakai database, the event in the calendar can not be deleted,
    *                                     or if the child individual virtual classroom sessions can not be deleted on the elluminate live server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document, an XmlException
    *                                     will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the virtual classroom session group that was deleted.
    */
   public abstract VirtualClassroomSessionGroup deleteVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException;

   /**
    * retrieves the url which will allow a user to join an invidual virtual classroom session.
    * <br/><br/>
    * @param id    id of the individual virtual classroom session to join
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke the  method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return   the url which will allow the user to join the individual virtual classroom session.
    */
   public abstract String getJoinUrl(String id) throws AuthorizationException , VirtualClassroomException;

   /**
    * gets a virtual classroom session group from the database.
    * <br/><br/>
    * @param id     id of the virtual classroom session group you want to retrieve.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the virtual classroom session group can not be retrieved from the database.
    * <br/><br/>
    * @return the virtual classroom session group with the specified id.
    */
   public abstract VirtualClassroomSessionGroup getVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException;

   /**
    * determines if the virtual classroom session server has sufficient capacity to handle the specified additional capacity during the given start and end times.
    * Most virtual classroom servers limit the number of allowable concurrent users as part of their license.  This method looks at the value of the
    * SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY sakai property which should be set to the virtual classroom server's maximum number of allowabled concurrent users
    * and determines if there is a sufficient number of seats left during the given start and end times to handle the requested additional capacity.
    * <br/><br/>
    * @param siteId               search only virtual classrooms associated with this siteId
    * @param start                the start date and time during which the virtual classroom server concurrent user capacity is to be checked.
    * @param end                  the end   date and time during which the virtual classroom server concurrent user capacity is to be checked.
    * @param additionalCapacity   the number of additional seats that will be needed during the specified time.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the number of currently scheduled virtual classroom sessions during the specified time period can not be retrieved.
    * <br/><br/>
    * @return  whether or not the virtual classroom server has enough seating capacity left to handle the specified additional capacity during the specified start and end times.
    */
   public boolean hasSufficientCapacity(String siteId, Date start, Date end, int additionalCapacity) throws AuthorizationException, VirtualClassroomException {

      boolean canSupportAdditionalCapacity = maxCapacity - additionalCapacity >= 0;

      if (canSupportAdditionalCapacity) {
/*       List                    virtualClassroomSessions    = listVirtualClassroomSessions(siteId, start, end);
         VirtualClassroomSession virtualClassroomSession     = null;
         int                     numScheduledConcurrentUsers = 0;

         for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext(); ) {
            virtualClassroomSession     = (VirtualClassroomSession)i.next();
            numScheduledConcurrentUsers += virtualClassroomSession.getCapacity();
         }
         canSupportAdditionalCapacity = maxCapacity - numScheduledConcurrentUsers - additionalCapacity >= 0;
 */
      }
      return canSupportAdditionalCapacity;
   }

   /**
    * returns a list of all virtual classroom session groups (containing both individual virtual classroom sessions and recordings) that were created for the specified sakai work site
    * sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId    search only virtual classroom session groups with this siteId.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listAllSessionGroupsAndRecordings(String siteId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all virtual classroom session groups (containing both individual virtual classroom sessions and recordings) that were created for the specified sakai work site
    * sorted by the specified field and order.
    * <br/><br/>
    * @param siteId     search only virtual classroom groups with this siteId.
    * @param sortField  specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder  specifies the whether the session groups are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listAllSessionGroupsAndRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai worksite sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId     search only virtual classrooms with this siteId.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
   public abstract List listRecordings(String siteId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai work site sorted by the specified field and order.
    * <br/><br/>
    * @param siteId     search only virtual classrooms with this siteId.
    * @param sortField  specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder  specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings as a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all virtual classroom session groups that were created for the current sakai work site sorted by start date in ascending order.
    * <br/><br/>
    * @param siteId       include only virtual classroom session groups associated with this siteId
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listVirtualClassroomSessionGroups(String siteId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all virtual classroom session groups that were created for the current sakai work site sorted by the specified field and order.
    * <br/><br/>
    * @param siteId       include only virtual classroom session groups associated with this siteId
    * @param sortField    specifies which of the three possible fields the session groups are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder    specifies the whether the session groups are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listVirtualClassroomSessionGroups(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all virtual classroom session groups that were created for the current sakai work site and will begin some time between the specified dates sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId   search only virtual classroom groups with this siteId
    * @param start    the lower bound of the time period during which individual virtual classroom sessions will start.
    * @param end      the upper bound of the time period during which individual virtual classroom sessions will start.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listVirtualClassroomSessionGroups(String siteId, Date start, Date end) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all virtual classroom session groups that were created for the current sakai work site and will begin some time between the specified dates sorted by the specified field and order.
    * <br/><br/>
    * <br/><br/>
    * @param siteId       search only virtual classroom groups with this siteId
    * @param start        the lower bound of the time period during which individual virtual classroom sessions will start.
    * @param end          the upper bound of the time period during which individual virtual classroom sessions will start.
    * @param sortField    specifies which of the three possible fields the session groups are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder    specifies the whether the session groups are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSessionGroup objects.
    */
   public abstract List listVirtualClassroomSessionGroups(String siteId, Date start, Date end, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * sets the instructor for a given virtual classroom session group and all its child invidvidual virtual classroom sessions.
    * <br/><br/>
    * @param virtualClassroomSessionGroupId   the id of the virtual classroom session group which will have its instructor set.
    * @param instructorId                     the sakai user id of the instructor
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    */
   public abstract void setInstructor(String virtualClassroomSessionGroupId, String instructorId) throws AuthorizationException, VirtualClassroomException;

   /**
    * updates the details of a given virtual classroom session group and its child individual virtual classroom sessions.
    * <br/><br/>
    * @param virtualClassroomSessionGroup  The virtual classroom session group to update.
    *                                      The id field must contain the id of the virtual classroom session group to be updated.
    *                                      The modifiedBy and modifiedOn fields will be set for you.
    *                                      All other fields should contain the new values to update the virtual classroom session group.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be updated in the sakai database, the calendar can not be updated for the event,
    *                                     or if the individual virtual classroom sessions can not be created on the elluminate live server. If the result
    *                                     returned by the elluminate server can not be parsed into a valid xml document, an XmlException will
    *                                     be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the newly updated virtual classroom session group.
    */
   public abstract VirtualClassroomSessionGroup updateVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException;

   public boolean isPrivateDefault() {
        return isPrivateDefault;
   }

   public boolean getIsPrivateDefault() {
        return isPrivateDefault;
   }

   public void setPrivateDefault(boolean privateDefault) {
        isPrivateDefault = privateDefault;
   }

   public void setIsPrivateDefault(boolean privateDefault) {
         isPrivateDefault = privateDefault;
   }

}
