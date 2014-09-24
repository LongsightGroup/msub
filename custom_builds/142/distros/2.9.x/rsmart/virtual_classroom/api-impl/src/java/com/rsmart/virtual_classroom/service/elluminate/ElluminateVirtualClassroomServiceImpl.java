/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.service.elluminate;

import com.rsmart.virtual_classroom.util.ScheduleHelper;
import com.rsmart.virtual_classroom.model.AuthorizationException;
import com.rsmart.virtual_classroom.model.CalendarException;
import com.rsmart.virtual_classroom.model.Schedule;
import com.rsmart.virtual_classroom.model.SortComparator;
import com.rsmart.virtual_classroom.model.VirtualClassroomException;
import com.rsmart.virtual_classroom.model.VirtualClassroomSession;
import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;
import com.rsmart.virtual_classroom.model.XmlException;
import com.rsmart.virtual_classroom.service.VirtualClassroomServiceImpl;
import org.hibernate.Hibernate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.dao.DataAccessException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Placement;
import org.w3c.dom.Document;

import javax.xml.soap.*;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * This class is an impleme ntation of the virtual classroom service interface.
 * This implementation uses an external server called the elluminate live virtual classroom server which is made by a company called elluminate.
 * Thus, all the details of this implementation are specific to the elluminate live virtual classroom server only.
 * <p>
 * The elluminate live server excepts commands via http.  The elluminate live server in turn returns results and errors as xml documents also via http.
 * Clients, like this implementation of the virtual classroom service interface, send commands to the elluminate server by opening a URLConnection to the server,
 * and then sending the appropriate command and parameters.
 * </p>
 * <p>
 * Thus, every method in this class will have the following three local variables:
 * <ol>
      <li>URLConnection elluminateServerConnection = null;     // http connection to the elluminate server                                   </li>
      <li>String        command                    = null;     // elluminate command to retrieve a meeting                                   </li>
      <li>SOAPBody body                   = null;     // xml result returned from the elluminate server after executing the command </li>
 * </ol>
 * </p>
 */
public class ElluminateVirtualClassroomServiceImpl extends VirtualClassroomServiceImpl {
   // logger
   private final transient Log logger = LogFactory.getLog(getClass());

   // the elluminate live server expects dates to be in this format
   private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z");

   // the elluminate server comes with a built in "admin" user called "server_support" whose elluminate id is 1.
   private static final int              ELLUMINATE_FACILITATOR_ID = 1;

   // virtual classroom server properties that are elluminate specific which obtained from sakai.properties
   private String adapter;          // the name the adapter on the elluminate live server will use to communicate with the elluminate live server.

   private ElluminateXmlResponseParser elluminateXmlResponseParser;
   private static final String PARTICIPANT_ROLE = "3";
   private static final String MODERATOR_ROLE = "2";


    /**
    * default constructor.
    */
   public ElluminateVirtualClassroomServiceImpl() {
      // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated.
    */
   public void init() {
      super.init();
      elluminateXmlResponseParser = new ElluminateXmlResponseParser();
      String errorMessage = "  The virtual classroom tool will not function properly without this value set to a valid value.";

      // read in the values from the sakai.properties file pertaining to the elluminate virtual classroom server
      adapter = getServerConfigurationService().getString(SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADAPTER        );

      // parse the virtual classroom server adapter for sakai
      if (adapter == null || adapter.trim().length() == 0)
         logger.error("No virtual classroom server adapter was specified in sakai.properties via " + SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADAPTER + " property." + errorMessage);

      // set the url which will be used for all communications with the elluminate live server
      setUrl(getServer() + ":" + getPort() + "/webservice.event");

      // display the encoding scheme - needed to correctly handle chinese characters
      logger.warn("sendCommand(), file encoding: " + System.getProperty("file.encoding") + ", uri encoding: " + System.getProperty("URIEncoding"));
   }

   /**
    * builds a jnlp (java network launch protocol).
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    */
   public void buildJNLP() throws AuthorizationException, VirtualClassroomException {
      logger.info("buildJNLP()");
   }

   /**
    * creates a recording url.
    * note: according to the elluminate sdk tech support, this method does not work with elluminate server 6.5, manager 2.x.
    * <br/><br/>
    * @param sessionId     id of the virtual classroom session for which the recording will be created.
    * @param recordingId   id of the recording to be created.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the url to use to view the virtual classroom session recording.
    */
   public String createRecordingURL(long sessionId, long recordingId) throws AuthorizationException, VirtualClassroomException {
      logger.info("createRecordingURL(), vc session id: " + sessionId + ", recording id: " + recordingId);

      return null;
   }

   /**
    * create a user on the elluminate server with the moderator role.
    * <br/><br/>
    * @param username    username for the person to be created.
    * @param firstName   first name of the user to be created.
    * @param lastName    last  name of the user to be created.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  the id generated by the elluminate server for the user if the user did not previously exist, or the user's id if they already existed.
    */
   private String createUser(String username, String firstName, String lastName, String email, String role) throws AuthorizationException, VirtualClassroomException {
      logger.info("createUser(), username: " + username + ", first name: " + firstName + ", last name: " + lastName);

      String elluminateUserId;

      // see if the user already exists on the elluminate server
      elluminateUserId = getElluminateUserId(username);

      // if the user doesn't already exist on the elluminate server, then create them
      if (elluminateUserId == null || elluminateUserId.length() == 0) {
         String        command                    = "createUser";
         Map args = new HashMap();
         args.put("firstName", firstName);
         args.put("lastName", lastName);
         args.put("loginName", username);
         //TODO what to do about this ?
         args.put("loginPassword", "password");
         if (email == null || email.length() == 0) {
            email = "not avaliable";
         }
         args.put("email", email);
         args.put("role",role);
         SOAPBody body                   = sendCommand(command, args);

         elluminateXmlResponseParser.extractCreateUserResult(body);
         // now that the user has been created on the elluminate server, go and get their elluminate user id
         elluminateUserId = getElluminateUserId(username);
      }
      return elluminateUserId;
   }

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
   public VirtualClassroomSessionGroup createVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException {
      logger.info("createVirtualClassroomSessionGroup(), virtual classroom session group: " + virtualClassroomSessionGroup);

      // check parameter values
      Schedule schedule = virtualClassroomSessionGroup.getSchedule();

      if (virtualClassroomSessionGroup.getName() == null || virtualClassroomSessionGroup.getName().trim().length() ==0)
         throw new VirtualClassroomException("name is a required parameter and can not be null or empty.");
      if (virtualClassroomSessionGroup.getCapacity() <= 0)
         throw new VirtualClassroomException("capacity is a required parameter and must be greater than 0.");
      if (!hasSufficientCapacity(virtualClassroomSessionGroup.getSiteId(), schedule.getStartDate(), schedule.getEndDate(), virtualClassroomSessionGroup.getCapacity()))
         throw new VirtualClassroomException("The Virtual Classroom Server has too many concurrent users scheduled during the specified time.");
      if (schedule.getStartDate() == null)
         throw new VirtualClassroomException("start date is a required parameter and can not be null.");
      if (schedule.getEndDate() == null)
         throw new VirtualClassroomException("end date is a required parameter and can not be null.");
      if (schedule.getEndDate().getTime() <= schedule.getStartDate().getTime())
         throw new VirtualClassroomException("end date\\time can not occur at or before the start date\\time.");
      if (virtualClassroomSessionGroup.getSchedule().isRecurring()){
        if (!setUpValidDate(virtualClassroomSessionGroup)){
            throw new VirtualClassroomException("Make sure the recurring day(s) you have checked is/are between Dates selected");
          }
      }
      // check permissions
      if (!checkPermission(virtualClassroomSessionGroup, getSiteReference(virtualClassroomSessionGroup.getSiteId()), PERMISSION_CREATE)) {
         User user = getCurrentUser();
         throw new AuthorizationException(user.getId() + "(" + user.getDisplayName() + ") does not have permission to create new virtual classroom sessions.");
      }

      // set who is creating the virtual classroom session
      virtualClassroomSessionGroup.setCreatedBy(getCurrentUser().getId());
      virtualClassroomSessionGroup.setCreatedOn(new Date());

      // set the instructor of the virtual classroom session group if it has not already been set
      if (virtualClassroomSessionGroup.getInstructorId() == null || virtualClassroomSessionGroup.getInstructorId().trim().length() == 0) {
         User   user = getCurrentUser();
         String type = user.getType();
         virtualClassroomSessionGroup.setInstructorId(user.getId());
      }

      // set the site id in the case the caller hasn't already populated it
      if (virtualClassroomSessionGroup.getSiteId() == null) {
         virtualClassroomSessionGroup.setSiteId(getCurrentSite().getId());
      }

      // save the virtual classroom session information to the sakai database
      String groupId = (String)(getHibernateTemplate().save(virtualClassroomSessionGroup));

      // create the individual virtual classroom sessions
      try {
         String                  siteId                   = virtualClassroomSessionGroup.getSiteId();
         List                    dates                    = schedule.calculateDates();
         long                    duration                 = schedule.getDuration();
         Date                    startDate                = null;
         Date                    endDate                  = null;
         String                  name                     = null;
         VirtualClassroomSession virtualClassroomSession  = null;
         ArrayList               virtualClassroomSessions = new ArrayList();
         int                     j                        = 1;
         String                  sessionNumber            = null;


         for (Iterator i=dates.iterator(); i.hasNext(); ++j) {
            startDate     = (Date)i.next();
            endDate       = new Date(startDate.getTime() + duration);
            sessionNumber = " " + getMessageSource().getMessage("virtual_classroom_session_number", new Object[]{new Integer(j)}, getLocale());
            name          = virtualClassroomSessionGroup.getName() + (virtualClassroomSessionGroup.isAddMeetingDateToSessionName() && virtualClassroomSessionGroup.getSchedule().isRecurring() ? sessionNumber : "");

            virtualClassroomSession = new VirtualClassroomSession(groupId, siteId, name, startDate, endDate);
            virtualClassroomSession = createVirtualClassroomSession(virtualClassroomSession, virtualClassroomSessionGroup.getInstructorId(), schedule.isAddEventToCalendar(), virtualClassroomSessionGroup.isPrivate(), virtualClassroomSessionGroup.getMaxTalkers());
            virtualClassroomSessions.add(virtualClassroomSession);
         }
         virtualClassroomSessionGroup.setVirtualClassroomSessions(virtualClassroomSessions);
      } catch (VirtualClassroomException ex) {
         // something went wrong trying to create the individual elluminate virtual classroom session or setting the join url
         // todo: if any of the individual virtual classroom sessions were created on the elluminate server, delete them
         throw ex;
      } catch (CalendarException ex) {
         // something went wrong trying to add the events for the individual virtual classroom sessions to the calendar
         throw new VirtualClassroomException(ex);
      } catch (DataAccessException ex) {
         // something went wrong trying to save the data to the database
         // todo: delete the elluminate live virtual classroom sessions
         throw new VirtualClassroomException(ex);
      }
      return virtualClassroomSessionGroup;
   }

   /**
    * creates an individual virtual classroom session.
    * <br/><br/>
    * @param virtualClassroomSession  the individual virtual classroom session to create.
    *                                 Only the group id, name, start date, and end date of the virtual classroom session need be set.
    *                                 The id, siteId, calendarEventId, and elluminate id will be generated and set for you.
    *                                 The elluminateModeratorId and elluminateRecordingId may be null.
    * @param instructorId             sakai user id of the person who will be the instructor.
    * @param addEventToCalendar       whether an even should be added to the site's calendar.
    * <br/><br/>
    * @param aPrivate
    *@param maxTalkers @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be saved to the sakai database, an event can not be added to the calendar,
    *                                     or if the individual virtual classroom session can not be created on the elluminate live server. If the result
    *                                     returned by the elluminate server can not be parsed into a valid xml document, an XmlException will
    *                                     be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  the newly created individual virtual classroom session with the id, siteId, calendarEventId, and elluminate id set.
    */
   private VirtualClassroomSession createVirtualClassroomSession(VirtualClassroomSession virtualClassroomSession, String instructorId, boolean addEventToCalendar, boolean isPrivate, int maxTalkers) throws AuthorizationException, CalendarException, VirtualClassroomException {
      logger.info("createVirtualClassroomSession(), virtual classroom session: " + virtualClassroomSession);

      // check parameter values
      if (virtualClassroomSession.getName() == null || virtualClassroomSession.getName().trim().length() ==0)
         throw new VirtualClassroomException("name is a required parameter and can not be null or empty.");
      if (virtualClassroomSession.getStartDate() == null)
         throw new VirtualClassroomException("start date is a required parameter and can not be null.");
      if (virtualClassroomSession.getEndDate() == null)
         throw new VirtualClassroomException("end date is a required parameter and can not be null.");
      if (virtualClassroomSession.getEndDate().getTime() <= virtualClassroomSession.getStartDate().getTime())
         throw new VirtualClassroomException("end date\\time can not occur at or before the start date\\time.");
      


//    if (virtualClassroomSession.getCapacity() <= 0)
//       throw new VirtualClassroomException("capacity is a required parameter and must be greater than 0.");
//    if (!hasSufficientCapacity(virtualClassroomSession.getSiteId(), virtualClassroomSession.getStart(), virtualClassroomSession.getEnd(), virtualClassroomSession.getCapacity()))
//       throw new VirtualClassroomException("The Virtual Classroom Server has too many concurrent users scheduled during the specified time.");
      if (instructorId == null || instructorId.trim().length() == 0)
         throw new VirtualClassroomException("instructor id a required parameter and must not be empty.");

      try {
         // create the moderator user if they aren't created already
         User user = getUser(instructorId);
         String elluminateUserId  = createUser(user.getEid(), user.getFirstName(), user.getLastName(), user.getEmail(), MODERATOR_ROLE);

         // create the virtual classroom session on the elluminate server
         String                  command                           = "createMeeting";

         Map args = new HashMap();
         args.put("name", virtualClassroomSession.getName());
         args.put("start", String.valueOf(virtualClassroomSession.getStartDate().getTime()));
         args.put("end", String.valueOf(virtualClassroomSession.getEndDate().getTime()));
         args.put("private", String.valueOf(isPrivate));
         args.put("facilitator", elluminateUserId);

         SOAPBody                body                          = sendCommand(command, args);
         VirtualClassroomSession elluminateVirtualClassroomSession = elluminateXmlResponseParser.extractCreateMeetingResult(body);

         virtualClassroomSession.setElluminateId(elluminateVirtualClassroomSession.getElluminateId());
         logger.info("createVirtualClassroomSession(), elluminate virtual classroom session created: \n" + virtualClassroomSession);

         //update the max talkers if necessary
         if (maxTalkers != 1) {
             command = "updateMeetingParameters";
             args = new HashMap();
             args.put("meetingId", String.valueOf(elluminateVirtualClassroomSession.getElluminateId()));
             args.put("maxTalkers", String.valueOf(maxTalkers));
             sendCommand(command, args);
             elluminateVirtualClassroomSession = elluminateXmlResponseParser.extractCreateMeetingResult(body);
         }

         // set the elluminate moderator of the individual virtual classroom session on the elluminate server
         virtualClassroomSession.setElluminateModeratorId(elluminateUserId);

         // add an event to the calendar
         if (addEventToCalendar) {
            CalendarEvent calendarEvent = createCalendarEvent(virtualClassroomSession.getName(), "virtual classroom session", virtualClassroomSession.getSiteId(), virtualClassroomSession.getStartDate(), virtualClassroomSession.getEndDate());
            virtualClassroomSession.setCalendarEventId(calendarEvent.getId());
         }

         // save the individual virtual classroom session information to the sakai database
         getHibernateTemplate().save(virtualClassroomSession);

      } catch (VirtualClassroomException ex) {
         // something went wrong trying to create the elluminate virtual classroom session or setting the join url
         // todo: if the virtual classroom session was created on the elluminate server, delete it
         VirtualClassroomException newEx = new VirtualClassroomException(virtualClassroomSession.getName() + ": " + ex.getMessage());
         newEx.setStackTrace(ex.getStackTrace());
         logger.error(ex);
         throw newEx;
      } catch (CalendarException ex) {
         // something went wrong trying to add the event to the calendar
         throw ex;
      } catch (DataAccessException ex) {
         // something went wrong trying to save the data to the database
         // todo: delete the elluminate live virtual classroom session
         throw new VirtualClassroomException(ex);
      }
      return virtualClassroomSession;
   }


     /**
     * validates the recurring value is within the startDate and endDate
     * @param virtualClassroomSession
     * @return
     */
    private boolean setUpValidDate(VirtualClassroomSessionGroup virtualClassroomSession) {

      //int dayOfWeek = 3;
      List listOfDates = new ArrayList();


      String daysofWeek = virtualClassroomSession.getSchedule().getDaysOfWeekString();


      boolean isSetUpDateValid = true;
      Date endDate  = virtualClassroomSession.getSchedule().getEndDate();
      Date startDate = virtualClassroomSession.getSchedule().getStartDate();

       // Creates two calendars instances
       Calendar calStartDate = Calendar.getInstance();
       Calendar calEndDate = Calendar.getInstance();
        //new Calendar Dates
        Calendar calStartDate2 = Calendar.getInstance();
        Calendar calEndDate2  = Calendar.getInstance();

        //set Date into Calendar Objects
        calStartDate.setTime(startDate);
        calEndDate.setTime(endDate);

       //setting date values into Calendar Objects --Start Date--
       calStartDate2.set(calStartDate.get(Calendar.YEAR), calStartDate.get(Calendar.MONTH),calStartDate.get(Calendar.DAY_OF_MONTH));

        //setting date values into Calendar Objects --End Date--
       calEndDate2.set(calEndDate.get(Calendar.YEAR), calEndDate.get(Calendar.MONTH),calEndDate.get(Calendar.DAY_OF_MONTH));


       long startMiliSeconds = calStartDate2.getTimeInMillis();
       long endMiliSeconds = calEndDate2.getTimeInMillis();

       long differenceMiliSeconds = endMiliSeconds - startMiliSeconds;

       //calculate number of days between Dates..you have to add one to include the current day
       long daysBetween = differenceMiliSeconds /( 24 * 60 * 60 * 1000);


       //create date Objects add to List
       for (int j = 0; j < daysBetween + 1; j++){
        	if (j == 0){
        		calStartDate.add(Calendar.DAY_OF_WEEK, 0);
        	}else{
             calStartDate.add(Calendar.DAY_OF_WEEK, 1);
        	}
             Date newDate = calStartDate.getTime();
             listOfDates.add(newDate);
         }

         if ( listOfDates.size() > 7){
             isSetUpDateValid = true;
             return isSetUpDateValid;
         }


         /**
          * Iterate through Date/Calendar object to prepare DAY_OF_WEEK if "isSetUpDateValid is false" The date setup is invalid
          * Throw VirtualClassroomException
          */

         List listOfDayOfWeeks = new ArrayList();
         for( int i = 0; i < listOfDates.size(); i++){
           Date newDate = (Date)listOfDates.get(i);
           Calendar newCal = Calendar.getInstance();
           newCal.setTime(newDate);
           listOfDayOfWeeks.add(String.valueOf(newCal.get(Calendar.DAY_OF_WEEK)));
         }


         //checking if day of the week is in List if not send error
        // isSetUpDateValid = listOfDayOfWeeks.containsAll(parseDaysOfWeek(daysofWeek));
         isSetUpDateValid = compareValues(listOfDayOfWeeks, parseDaysOfWeek(daysofWeek));

       return isSetUpDateValid;
    }

    /**
     * Days_Of_the_week comes in the format: 0011000  this means Tuesday and Wednesday were selected
     * 1 = true  and 0 = false
     *
     * @param daysOfWeek
     * @return
     */
     private List parseDaysOfWeek(String daysOfWeek){
        int stringIndex = 0;
        char [] charArray = daysOfWeek.toCharArray();
        List daysOfWeekList = new ArrayList();
        for ( char c  : charArray ){
            stringIndex++;
            if ( Character.toString(c).equals( "1" )) {
             daysOfWeekList.add(""+stringIndex);
            }
        }
        return daysOfWeekList;
     }


    /**
     * 
     * @param listOfDaysWeek
     * @param dayOfWeekList
     * @return
     */
   private boolean compareValues(List listOfDaysWeek, List dayOfWeekList){
        boolean sameValues = false;
        List booleanValue = new ArrayList();
        if ( listOfDaysWeek.size() > dayOfWeekList.size()){
             for ( int j =0; j < dayOfWeekList.size(); j++){
                if ( listOfDaysWeek.contains((String)dayOfWeekList.get(j))){
                     sameValues = true;
                     booleanValue.add(sameValues);
                     }else{
                     sameValues =false;
                     booleanValue.add(sameValues);
                    }
                 }
        }else{
            for ( int k =0; k < listOfDaysWeek.size(); k++){
                if ( dayOfWeekList.contains((String)listOfDaysWeek.get(k))){
                    sameValues = true;
                    booleanValue.add(sameValues);
                 }else{
                    sameValues =false;
                    booleanValue.add(sameValues);
                }
             }
         }  
        return booleanValue.contains(true);
    }




    /**
    * delete a recording of an individual virtual classroom session.
    * <br/><br/>
    * @param elluminateRecordingId   id of the recording to be deleted.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return recording that was deleted
    */
   private VirtualClassroomSession deleteVirtualClassroomRecording(String elluminateRecordingId) throws AuthorizationException, VirtualClassroomException {
      logger.info("deleteVirtualClassroomRecording(), recording id: " + elluminateRecordingId);

      VirtualClassroomSession virtualClassroomSession = null;

      // delete the virtual classroom session recording from the elluminate live server
      String                  command                    = "deleteRecording";
      Map args = new HashMap();
      args.put("recordingId", elluminateRecordingId);


      SOAPBody                body                   = sendCommand(command, args);
      VirtualClassroomSession recording                  = elluminateXmlResponseParser.extractDeleteRecordingResult(body);

      return recording;
   }

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
   public VirtualClassroomSessionGroup deleteVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException {
      logger.info("deleteVirtualClassroomSessionGroup(), id: " + id);

      VirtualClassroomSessionGroup virtualClassroomSessionGroup = getVirtualClassroomSessionGroup(id);

      // check permissions
      if (!checkPermission(virtualClassroomSessionGroup, PERMISSION_DELETE)) {
         throw new AuthorizationException("You do not have permission to delete virtual classroom session groups.");
      }

      try {
         if (virtualClassroomSessionGroup.isRecorded()) {
            VirtualClassroomSession virtualClassroomRecording = virtualClassroomSessionGroup.getRecording();
            deleteVirtualClassroomRecording(virtualClassroomRecording.getElluminateRecordingId());
         } else {
            // delete the child individual virtual classroom sessions
            VirtualClassroomSession virtualClassroomSession  = null;
            List                    virtualClassroomSessions = getVirtualClassroomSessions(virtualClassroomSessionGroup.getId());

            for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext(); ) {
               virtualClassroomSession = (VirtualClassroomSession)i.next();
               deleteVirtualClassroomSession(virtualClassroomSession);
            }
            // delete the parent virtual classroom session group
            getHibernateTemplate().delete(virtualClassroomSessionGroup);
         }
      } catch (CalendarException ex) {
         // something went wrong trying to delete the events for the child individual virtual classroom sessions from the calendar
         throw new VirtualClassroomException(ex);
      } catch (DataAccessException ex) {
         // something went wrong deleting the virtual classroom session group or the child individual virtual classroom sessions from the sakai database
         throw new VirtualClassroomException(ex);
      } catch (VirtualClassroomException ex) {
         // something went wrong deleting the child individual virtual classroom sessions from the elluminate server
         // todo: restore the sakai database
      }
      logger.info("deleteVirtualClassroomSessionGroup(), virtual classroom session group deleted: \n" + virtualClassroomSessionGroup);

      return virtualClassroomSessionGroup;
   }

   /**
    * deletes an individual virtual classroom session.
    * <br/><br/>
    * @param virtualClassroomSession    the individual virtual classroom session to be deleted.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be deleted from the sakai database, the event in the calendar can not be deleted,
    *                                     or if the virtual classroom session can not be deleted on the elluminate live server. If the result
    *                                     returned by the elluminate server can not be parsed into a valid xml document, an XmlException will
    *                                     be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the individual virtual classroom session that was deleted.
    */
   private VirtualClassroomSession deleteVirtualClassroomSession(VirtualClassroomSession virtualClassroomSession) throws AuthorizationException, CalendarException, VirtualClassroomException {
      logger.info("deleteVirtualClassroomSession(), \n" + virtualClassroomSession);

      try {
         // delete the event from the calendar
         if (virtualClassroomSession.getIsOnCalendar())
            deleteCalendarEvent(virtualClassroomSession.getCalendarEventId(), virtualClassroomSession.getSiteId());

         // delete the virtual classroom session information from the sakai database
         getHibernateTemplate().delete(virtualClassroomSession);

         // delete the virtual classroom session from the elluminate live server
         String                  command                           = "deleteMeeting";
         Map args = new HashMap();
         args.put("meetingId", String.valueOf(virtualClassroomSession.getElluminateId()));

         SOAPBody                body                          = sendCommand(command, args);
         VirtualClassroomSession elluminateVirtualClassroomSession = elluminateXmlResponseParser.extractDeleteMeetingResult(body);
      } catch (CalendarException ex) {
         // something went wrong trying to delete the event from the calendar
         throw ex;
      } catch (DataAccessException ex) {
         // something went wrong deleting the individual virtual classroom session information from the sakai database
         throw new VirtualClassroomException(ex);
      } catch (VirtualClassroomException ex) {
         // something went wrong deleting the individual virtual classroom session from the elluminate server
         // todo: restore the sakai database
      }
      logger.info("deleteVirtualClassroomSession(), virtual classroom session deleted: \n" + virtualClassroomSession);

      return virtualClassroomSession;
   }

   /**
    * this method generates an md5 hash of the virtual classroom session id and username.
    */
/* private byte[] generateAuthenticationToken(String virtualClassroomSessionId, String username) throws VirtualClassroomException {
      byte[] hash = null;
      try {
         MessageDigest messageDigest = MessageDigest.getInstance("md5");
         hash = messageDigest.digest((virtualClassroomSessionId + username).getBytes());
      } catch (NoSuchAlgorithmException ex) {
         throw new VirtualClassroomException(ex);
      }
      return hash;
   }
*/
   /**
    * retrieve the current worksite.
    * Instead of throwing an exception if the worksite can not be obtained, simply return null.
    */
   public Site getCurrentSite() {
      Site site = null;
      try {
         Placement placement = ToolManager.getCurrentPlacement();
         String siteId = placement.getContext();
         site = SiteService.getSite(placement.getContext());
      } catch (IdUnusedException ex) {
         logger.warn("Unable to retrieve the current worksite");
      }
      return site;
   }
   /**
    * retrieves a virtual classroom session from the elluminate live server.
    * <br/><br/>
    * @param elluminateId     id of the virtual classroom session you want to retrieve from the elluminate live server.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the virtual classroom session with the specified id.
    */
   private VirtualClassroomSession getElluminateVirtualClassroomSession(long elluminateId) throws AuthorizationException, VirtualClassroomException {
      logger.info("getElluminateVirtualClassroomSession(), elluminate id: " + elluminateId);

      String                  command                    = "getMeeting";
      Map args = new HashMap();
      args.put("meetingId", String.valueOf(elluminateId));

      SOAPBody                body                   = sendCommand(command, args);
      VirtualClassroomSession virtualClassroomSession    = elluminateXmlResponseParser.extractGetMeetingResult(body);

      logger.info("getElluminateVirtualClassroomSession(), retrieved elluminate live virtual classroom session: \n" + virtualClassroomSession);
      return virtualClassroomSession;
   }

   /**
    * return the url for elluminate help.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If theommand can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the url of the elluminate help.
    */
   private String getHelpUrl() throws VirtualClassroomException {
      logger.info("getHelpUrl()");

      String        command  = "helpUrl";
      SOAPBody body = sendCommand(command, new HashMap());
      String        helpUrl  = elluminateXmlResponseParser.extractHelpUrlResult(body);

      return helpUrl;
   }


   /**
    * retrieves the url which will allow a user to join an invidual virtual classroom session.  If the meeting
    * is private we ensure that the user has been created and is participant of the meeting.
    * <br/><br/>
    * @param id    id of the individual virtual classroom session to join
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke the  method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return   the url which will allow the user to join the individual virtual classroom session.
    */
   public String getJoinUrl(String id) throws AuthorizationException , VirtualClassroomException {
       logger.info("getJoinUrl(), id: " + id);

       VirtualClassroomSession virtualClassroomSession = getVirtualClassroomSession(id);
       User user = getCurrentUser();
       String elluminateUserName = getDisplayedUsername(user);
       String elluminateUserId = getElluminateUserId(user.getEid());

       VirtualClassroomSessionGroup group = getVirtualClassroomSessionGroup(virtualClassroomSession.getGroupId());
       elluminateUserId = prepareUserParticipation(virtualClassroomSession.getElluminateId(), user, elluminateUserId, group.isPrivate());

       String command = "buildMeetingJNLP";
       Map args = new HashMap();

       if (elluminateUserId != null) {
           args.put("userId", elluminateUserId);
       } else {
           args.put("userName", elluminateUserName);
       }
       args.put("meetingId", String.valueOf(virtualClassroomSession.getElluminateId()));
       args.put("meetingName", virtualClassroomSession.getName());
       args.put("startDate", String.valueOf(virtualClassroomSession.getStartDate().getTime()));
       args.put("endDate", String.valueOf(virtualClassroomSession.getEndDate().getTime()));

       SOAPBody body = sendCommand(command, args);
       return elluminateXmlResponseParser.extractJoinMeetingUrlResult(body);
   }

    private String prepareUserParticipation(long elluminateMeetingId, User user, String elluminateUserId, boolean isPrivate) throws VirtualClassroomException {
        Map args = new HashMap();
        String command;
        if (isPrivate) {
            // make sure user exists
            if (elluminateUserId == null) {
                //create user
                elluminateUserId = createUser(user.getEid(), user.getFirstName(), user.getLastName(), user.getEmail(), PARTICIPANT_ROLE);
            }

            // check user is a participant
            command = "isParticipant";
            args.put("userId", elluminateUserId);
            args.put("meetingId", String.valueOf(elluminateMeetingId));
            SOAPBody body = sendCommand(command, args);

            boolean isParticipant, isModerator;
            isParticipant = elluminateXmlResponseParser.extractResult(body);

            // check user is a moderator
            command = "isModerator";
            args.put("userId", elluminateUserId);
            args.put("meetingId", String.valueOf(elluminateMeetingId));
            body = sendCommand(command, args);

            isModerator = elluminateXmlResponseParser.extractResult(body);

            if (!isParticipant && !isModerator) {

                // load existing participants
                command = "listParticipants";
                args.put("meetingId", String.valueOf(elluminateMeetingId));
                sendCommand(command, args);
                Map participants = elluminateXmlResponseParser.extractListParticipantsResult(body);

                if (!participants.containsKey(elluminateUserId)) {
                    // add user as a participant
                    command = "addParticipant";
                    args.put("meetingId", String.valueOf(elluminateMeetingId));
                    StringBuffer userList = new StringBuffer();
                    for (Iterator i = participants.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Map.Entry) i.next();
                        userList.append(entry.getKey() + "=" + entry.getValue() + ";");
                    }
                    userList.append(elluminateUserId + "=" + PARTICIPANT_ROLE + ";");
                    args.put("users", userList.toString());
                    sendCommand(command, args);
                    //elluminateXmlResponseParser.extractListParticipantsResult(body);
                }
            }
        }
        return elluminateUserId;
    }


    /**
    * retrieves a list of url's which will launch a virtual classroom session recording for all the recordings for the specified virtual classroom session.
    * <br/><br/>
    * @param id   id of the virtual classroom session whose recordings are to be retrieved.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  a list of url's which will retireve a jnlp file which will launch the virtual classroom session recording.
    */
/* public List getRecordingsJNLP(String id) throws AuthorizationException, VirtualClassroomException {
      logger.info("getRecordingsJNLP(), id: " + id);

      VirtualClassroomSession virtualClassroomSession    = getVirtualClassroomSession(id);
      URLConnection           elluminateServerConnection = getUrlConnection();
      String                  command                    = "getRecordingsJNLP&meetingId=" + virtualClassroomSession.getElluminateId();
      Document                document                   = sendCommand(elluminateServerConnection, command);
      List                    recordingUrls              = elluminateXmlResponseParser.extractRecordingsJNLPResult(document);

      return recordingUrls;
   }
*/
   /**
    * returns the time zone of the machine on which virtual classroom server is running.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the time zone of the machine on which virtual classroom server is running.
    */
   public String getServerTimeZone() throws AuthorizationException, VirtualClassroomException {
      logger.info("getServerTimeZone()");

      String        command                    = "serverTimeZone";
      SOAPBody body                   = sendCommand(command, new HashMap());
      String        serverTimeZone             = elluminateXmlResponseParser.extractServerTimeZoneResult(body);

      return serverTimeZone;
   }


   /**
    * return the elluminate user id of the person who has the given username.
    * <br/><br/>
    * @param username     unique username of the user whose elluminate id is to be retrieved.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  if the user is found, then the elluminate id of the user is returned.  Otherwise, null is returned.
    */
   private String getElluminateUserId(String username) throws AuthorizationException, VirtualClassroomException {
      logger.info("getElluminateUserId(), username: " + username);

      String        command                    = "getUser";
      Map args = new HashMap();
      args.put("userName", username);

      try {
          SOAPBody body                   = sendCommand(command, args);
          return elluminateXmlResponseParser.extractGetUserIdResult(body);
      } catch (VirtualClassroomException e) {
          logger.info("can't find user [" + username + "] in elluminate");
          return null;
      }

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
  public String getViewUrl(long elluminateId, String elluminateRecordingId) throws AuthorizationException, VirtualClassroomException {
      logger.info("getViewUrl(), elluminate id: " + elluminateId);

      String        command                    = "buildRecordingJNLP";
      Map args = new HashMap();
      args.put("recordingId", elluminateRecordingId);
      args.put("userIP", getCurrentUser().getDisplayName());

      SOAPBody body                   = sendCommand(command, args);
      String        url                        = elluminateXmlResponseParser.extractViewMeetingUrlResult(body);

      logger.info("getViewUrl(), url: " + url);
      return url;
   }

   /**
    * gets an individual virtual classroom session from the database.
    * <br/><br/>
    * @param id     id of the individual virtual classroom session you want to retrieve.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the individual virtual classroom session can not be retrieved from the database.
    * <br/><br/>
    * @return the individual virtual classroom session with the specified id.
    */
   private VirtualClassroomSession getVirtualClassroomSession(String id) throws AuthorizationException, VirtualClassroomException {
      logger.info("getVirtualClassroomSession(), id: " + id);

      VirtualClassroomSession virtualClassroomSession = null;

      try {
         String query = "select vc from VirtualClassroomSession vc where vc.id=?";
         List   list  = getHibernateTemplate().find(query, new Object[]{id});

         if (list != null && list.size() == 1)
            virtualClassroomSession = (VirtualClassroomSession)list.get(0);
         else
            throw new VirtualClassroomException("Unable to retrieve the individual virtual classroom session with id " + id);
      } catch (DataAccessException ex) {
         throw new VirtualClassroomException("Unable to retrieve the individual virtual classroom session with id " + id, ex);
      }
      logger.info("getVirtualClassroomSession(), retrieved the individual virtual classroom session: \n" + virtualClassroomSession);

      return virtualClassroomSession;
   }

   /**
    * returns a list of all virtual classroom sessions that were created for the current sakai work site and will start during the specified start and end dates sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId   search only virtual classrooms with this siteId
    * @param start    the lower bound of the time period during which virtual classroom sessions will start.
    * @param end      the upper bound of the time period during which virtual classroom sessions will start.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the data can not be loaded from the sakai database.
    * <br/><br/>
    * @return a list of VirtualClassroomSession objects.
    */
   private List getVirtualClassroomSessions(String siteId, Date start, Date end) throws AuthorizationException, VirtualClassroomException {
      logger.info("getVirtualClassroomSessions(), site id: " + siteId + ", start: " + start + ", end: " + end);

      String query = "select vc from VirtualClassroomSession vc where vc.siteId=? and vc.startDate>=? and vc.endDate<=? order by vc.startDate";
      List virtualClassroomSessions = getHibernateTemplate().find(query, new Object[]{siteId, start, end});

      return virtualClassroomSessions;
   }

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
   public VirtualClassroomSessionGroup getVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException {
      logger.info("getVirtualClassroomSessionGroup(), id: " + id);

      VirtualClassroomSessionGroup virtualClassroomSessionGroup = null;

      try {
         String query = "select vcsg from VirtualClassroomSessionGroup vcsg where vcsg.id=?";
         List   list  = getHibernateTemplate().find(query, new Object[]{id});

         if (list != null && list.size() == 1)
            virtualClassroomSessionGroup = (VirtualClassroomSessionGroup)list.get(0);
         else
            throw new VirtualClassroomException("Unable to retrieve the virtual classroom session group with id " + id);
      } catch (DataAccessException ex) {
         throw new VirtualClassroomException("Unable to retrieve the virtual classroom session group with id " + id, ex);
      }
      logger.info("getVirtualClassroomSessionGroup(), retrieved virtual classroom session group: \n" + virtualClassroomSessionGroup);

      return virtualClassroomSessionGroup;
   }

   /**
    * gets a list of the invidual virtual classroom sessions belonging to the spoecified virtual classroom session group parent from the database.
    * <br/><br/>
    * @param groupId     id of the virtual classroom session group whose child invidual virtual classroom sessions are to be retrieved.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the individual virtual classroom sessions can not be retrieved from the database.
    * <br/><br/>
    * @return the virtual classroom session group with the specified id.
    */
   private List getVirtualClassroomSessions(String groupId) throws AuthorizationException, VirtualClassroomException {
      logger.info("getVirtualClassroomSessions(), group id: " + groupId);

      VirtualClassroomSession virtualClassroomSession  = null;
      ArrayList               virtualClassroomSessions = new ArrayList();

      try {
         String query = "select vc from VirtualClassroomSession vc where vc.groupId=?";
         List   list  = getHibernateTemplate().find(query, new Object[]{groupId});

         for (Iterator i=list.iterator(); i.hasNext(); ) {
            virtualClassroomSession = (VirtualClassroomSession)i.next();
            virtualClassroomSessions.add(virtualClassroomSession);
         }
      } catch (DataAccessException ex) {
         throw new VirtualClassroomException("Unable to retrieve the individual virtual classroom sessions for group " + groupId, ex);
      }
      logger.info("getVirtualClassroomSessions(), retrieved " + virtualClassroomSessions.size() + " individual virtual classroom sessions.");

      return virtualClassroomSessions;
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
   public List listAllSessionGroupsAndRecordings(String siteId) throws AuthorizationException, VirtualClassroomException {
      return listAllSessionGroupsAndRecordings(siteId, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }

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
   public List listAllSessionGroupsAndRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listAllSessionsAndRecordings(), site id: " + siteId + ", " + sortParameters(sortField, sortOrder));

      // check permissions
      if (!checkPermission(null, PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view virtual classroom session groups or recordings.");

      List virtualClassroomRecordings    = listRecordings(siteId);
      List virtualClassroomSessionGroups = listVirtualClassroomSessionGroups(siteId);

      // add any recordings we found to the list of sessions we found
      virtualClassroomSessionGroups.addAll(virtualClassroomRecordings);
      // sort the list
      Collections.sort(virtualClassroomSessionGroups, new SortComparator(sortField, sortOrder));

      return virtualClassroomSessionGroups;
   }

   /**
    * returns a list of all recordings for the specified sakai worksite sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId     search only virtual classrooms with this siteId.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
   public List listRecordings(String siteId) throws AuthorizationException, VirtualClassroomException {
      return listRecordings(siteId, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }

   /**
    * returns a list of all recordings for the specified sakai work site sorted by the specified field and order.
    * <p>
    * Since recordings are created on the elluminate live server, they will not be found in the sakai database.
    * This presents a problem because all the recordings created by the virtual classroom tool located on various worksites will not have the site id of the worksite stored to indicate
    * which worksite the recording was created on.  Therefore, the only way to retrieve the recordings for a particular worksite is to first find all the existing virtual classroom sessions
    * for a given worksite.  Once you have that, you can search the elluminate server for any recordings corresponding to those virtual classroom sessions.  A serious problem arises though,
    * for which there is no work around.  If you create a virtual classroom session, record it, and then delete the virtual classroom session, the recording is irretrievable and lost forever.
    * The recording will still exist on the elluminate server, but there is no way for us to link it to the particular worksite that for which it was created.
    * </p>
    * <p>
    * The individual virtual classroom session for which a recording is created is stored in the sakai database.
    * Since the individual virtual classroom session stored in the sakai database contains the elluminate id of the virtual classroom session that created the recording, and since
    * the elluminate server stores that id with the recording, we can retrieve the recordings for a given work site using one of two methods:
    * <ol>
    *    <li>retrieve all recordings from the elluminate server and filter out those that have corresponding individual virtual classroom sessions for the given worksite in the sakai database</li>
    *    <li>get the list of all individual virtual classroom sessions for the specified given worksite from the sakai database and query the elluminate server if it has any corresponding recordings./li>
    * </ol>
    * The first method has the potential problem of retrieving a large number of recordings that we are not interested in.
    * The second method has the problem of making numerous calls over the network, one for each individual virtual classroom session in the sakai database.
    * </p>
    * <p>
    * It was decided at the time of this writing to choose the second method.
    * If performance is not acceptable, perhaps the first method can be implemented, or some other scheme altogether can be devised.
    * </p>
    * <br/><br/>
    * @param siteId     search only individual virtual classrooms with this siteId.
    * @param sortField  specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder  specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings as a list of VirtualClassroomSessionGroup objects.
    */
   public List listRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listRecordings(), site id: " + siteId + ", " + sortParameters(sortField, sortOrder));

      List recordings = new ArrayList();

      // check permissions
      if (!checkPermission(null, PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view recordings of virtual classroom sessions.");

      // retrieve the list of virtual classroom session groups for the given worksite
      List virtualClassroomSessionGroups = listVirtualClassroomSessionGroups(siteId, sortField, sortOrder);

      String command = "listRecordings";
      SOAPBody body = sendCommand(command, new HashMap());
      Map recordingsMap = elluminateXmlResponseParser.extractListRecordingsResult(body);

      // retrieve the individual virtual classroom sessions for the specified work site that have already occurred
      for(Iterator i=virtualClassroomSessionGroups.iterator(); i.hasNext(); ) {
         VirtualClassroomSessionGroup virtualClassroomSessionGroup = (VirtualClassroomSessionGroup)i.next();

         // retrieve the individual virtual classroom sessions
         List virtualClassroomSessions = virtualClassroomSessionGroup.getVirtualClassroomSessions();
         for(Iterator j=virtualClassroomSessions.iterator(); j.hasNext(); ) {

            // retrieve the recordings for each of the individual virtual classroom sessions from the elluminate server
            VirtualClassroomSession  virtualClassroomSession  = (VirtualClassroomSession)j.next();
            SimpleDateFormat         dateFormatter            = null;
            int                      n                        = 1;
            int                      numRecordingsForSession  = 0;

            List list = (List)recordingsMap.get(String.valueOf(virtualClassroomSession.getElluminateId()));
            if (list != null) {
               dateFormatter           = new SimpleDateFormat(getMessageSource().getMessage(ScheduleHelper.DATE_FORMAT_SHORT, null, getLocale()));
   
               // create a virtual classroom session group for each recording found
            for(Iterator k=list.iterator(); k.hasNext(); n++) {
                  VirtualClassroomSessionGroup virtualClassroomRecordingGroup  = new VirtualClassroomSessionGroup(virtualClassroomSessionGroup);
                  ArrayList                    virtualClassroomRecordings      = new ArrayList();
                  VirtualClassroomSession      virtualClassroomRecording       = (VirtualClassroomSession)k.next();
                  Schedule                     schedule                        = virtualClassroomRecordingGroup.getSchedule();

                  virtualClassroomRecordingGroup.setVirtualClassroomSessions(virtualClassroomRecordings);

                  schedule.setRecurring         (false);
                  schedule.setStartDate         (virtualClassroomSession.getStartDate());
                  schedule.setEndDate           (virtualClassroomSession.getEndDate  ());
                  schedule.setAddEventToCalendar(false);
                  schedule.calculateDuration();
                  schedule.setNonRecurringAndDaysOfWeek();

                  virtualClassroomRecording.setGroupId(virtualClassroomRecordingGroup.getId());
                  virtualClassroomRecording.setSiteId (virtualClassroomRecordingGroup.getSiteId());
                  virtualClassroomRecording.setEndDate(virtualClassroomSession.getEndDate());
                  if (virtualClassroomRecordingGroup.isAddMeetingDateToSessionName()) {
                     virtualClassroomRecording.setName(virtualClassroomRecording.getName() + " - " + dateFormatter.format(virtualClassroomRecording.getStartDate()) + (numRecordingsForSession == 1 ? "" : " - " + n));
                     virtualClassroomRecordingGroup.setName(virtualClassroomRecording.getName());
                  }

                  virtualClassroomRecordings.add(virtualClassroomRecording);
                  virtualClassroomRecordingGroup.setVirtualClassroomSessions(virtualClassroomRecordings);
                  recordings.add(virtualClassroomRecordingGroup);
               }
            }
         }
      }
      // sort the list
      Collections.sort(recordings, new SortComparator(sortField, sortOrder));

      return recordings;
   }

   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session sorted by start date in ascending order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, String virtualClassroomSessionId) throws AuthorizationException, VirtualClassroomException {
      return listRecordings(siteId, virtualClassroomSessionId, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }
*/
   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session sorted by the specified field and order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * @param sortField                    specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder                    specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, String virtualClassroomSessionId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listRecordings(), site id: " + siteId + ", vc session id: " + virtualClassroomSessionId + ", " + sortParameters(sortField, sortOrder));

      VirtualClassroomSession virtualClassroomSession = getVirtualClassroomSession(virtualClassroomSessionId);

      // check permissions
      if (!checkPermission(virtualClassroomSession, PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view virtual classroom session recordings.");

      // since recordings are created on the elluminate live server, they will not be found in the sakai database unless we put them there.
      URLConnection elluminateServerConnection = getUrlConnection();
      String        command                    = "listRecordings&meetingId=" + virtualClassroomSession.getElluminateId();
      SOAPBody body                   = sendCommand(elluminateServerConnection, command);
      List          recordings                 = elluminateXmlResponseParser.extractListRecordingsResult(document);

      // sort the list
      Collections.sort(recordings, new SortComparator(sortField, sortOrder));

      return recordings;
   }
*/
   /**
    * returns a list of all recordings for the specified sakai work site that occured between two dates sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId       search only virtual classrooms with this siteId
    * @param startDate    the date after  which the recording occured.
    * @param endDate      the date before which the recording finished.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, Date startDate, Date endDate) throws AuthorizationException, VirtualClassroomException {
      return listRecordings(siteId, startDate, endDate, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }
*/
   /**
    * returns a list of all recordings for the specified sakai work site that occured between two dates sorted by the specified field and order.
    * <br/><br/>
    * @param siteId      search only virtual classrooms with this siteId.
    * @param startDate   the date after  which the recording occured.
    * @param endDate     the date before which the recording finished.
    * @param sortField   specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder   specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, Date startDate, Date endDate, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listRecordings(), site id: " + siteId + ", start: " + startDate + ", end: " + endDate + ", " + sortParameters(sortField, sortOrder));

      // check permissions
      if (!checkPermission(null, PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view virtual classroom session recordings.");

      // since recordings are created on the elluminate live server, they will not be found in the sakai database unless we put them there.
      URLConnection elluminateServerConnection = getUrlConnection();
      String        command                    = "listRecordings&startDate=" + dateFormatter.format(startDate) + "&endDate=" + dateFormatter.format(endDate);
      SOAPBody body                   = sendCommand(elluminateServerConnection, command);
      List          recordings                 = elluminateXmlResponseParser.extractListRecordingsResult(document);

      // sort the list
      Collections.sort(recordings, new SortComparator(sortField, sortOrder));

      return recordings;
   }
*/
   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session between two dates sorted by start date in ascending order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * @param startDate                    the date after  which the recording occured.
    * @param endDate                      the date before which the recording finished.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, String virtualClassroomSessionId, Date startDate, Date endDate) throws AuthorizationException, VirtualClassroomException {
      return listRecordings(siteId, virtualClassroomSessionId, startDate, endDate, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }
*/
   /**
    * returns a list of all recordings for a given virtual classroom session between two dates sorted by the specified field and order.
    * <br/><br/>
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * @param startDate                    the date after  which the recording occured.
    * @param endDate                      the date before which the recording finished.
    * @param sortField                    specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder                    specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return  list of recordings
    */
/* public List listRecordings(String siteId, String virtualClassroomSessionId, Date startDate, Date endDate, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listRecordings(), site id: " + siteId + ", vc session id: " + virtualClassroomSessionId + ", start: " + startDate + ", end: " + endDate + ", " + sortParameters(sortField, sortOrder));

      VirtualClassroomSession virtualClassroomSession = getVirtualClassroomSession(virtualClassroomSessionId);

      // check permissions
      if (!checkPermission(virtualClassroomSession, PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view virtual classroom session recordings.");

      // since recordings are created on the elluminate live server, they will not be found in the sakai database unless we put them there.
      URLConnection elluminateServerConnection = getUrlConnection();
      String        command                    = "listRecordings&meetingId=" + virtualClassroomSession.getElluminateId() + "&startDate=" + dateFormatter.format(startDate) + "&endDate=" + dateFormatter.format(endDate);
      SOAPBody body                   = sendCommand(elluminateServerConnection, command);
      List          recordings                 = elluminateXmlResponseParser.extractListRecordingsResult(document);

      // sort the list
      Collections.sort(recordings, new SortComparator(sortField, sortOrder));

      return recordings;
   }
*/
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
   public List listVirtualClassroomSessionGroups(String siteId) throws AuthorizationException, VirtualClassroomException {
      return listVirtualClassroomSessionGroups(siteId, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }

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
   public List listVirtualClassroomSessionGroups(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listVirtualClassroomSessionGroups(), site id: " + siteId + ", " + sortParameters(sortField, sortOrder));

      // check permissions
      if (!checkPermission(null, PERMISSION_VIEW)) {
         throw new AuthorizationException("You do not have permission to view virtual classroom session groups.");
      }

      String query = "select vcsg from VirtualClassroomSessionGroup vcsg where vcsg.siteId=? order by vcsg." + getDatabaseSortField(sortField) + " " + getDatabaseSortOrder(sortOrder);
      List virtualClassroomSessionGroups = getHibernateTemplate().find(query, new Object[]{siteId});

      // sort the list
      Collections.sort(virtualClassroomSessionGroups, new SortComparator(sortField, sortOrder));

      // retrieve the individual virtual classroom sessions for each of the virtual classroom session groups
      VirtualClassroomSessionGroup virtualClassroomSessionGroup = null;
      List                         virtualClassroomSessions     = null;

      for (Iterator i=virtualClassroomSessionGroups.iterator(); i.hasNext(); ) {
         virtualClassroomSessionGroup = (VirtualClassroomSessionGroup)i.next();
         virtualClassroomSessions     = getVirtualClassroomSessions(virtualClassroomSessionGroup.getId());
         virtualClassroomSessionGroup.setVirtualClassroomSessions(virtualClassroomSessions);
      }
      return virtualClassroomSessionGroups;
   }

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
   public List listVirtualClassroomSessionGroups(String siteId, Date start, Date end) throws AuthorizationException, VirtualClassroomException {
      return listVirtualClassroomSessionGroups(siteId, start, end, SORT_FIELD_START_DATE, SORT_ORDER_ASCENDING);
   }

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
   public List listVirtualClassroomSessionGroups(String siteId, Date start, Date end, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException {
      logger.info("listVirtualClassroomSessionGroups(), site id: " + siteId + ", start: " + start + ", end: " + end + ", " + sortParameters(sortField, sortOrder));

      // check permissions
      if (!checkPermission(null, getSiteReference(siteId), PERMISSION_VIEW))
         throw new AuthorizationException("You do not have permission to view virtual classroom sessions.");

      String query = "select vcsg from VirtualClassroomSessionGroup vcsg where vcsg.siteId=? and vcsg.schedule.startDate>=? and vcsg.schedule.endDate<=? order by vcsg." + getDatabaseSortField(sortField) + " " + getDatabaseSortOrder(sortOrder);
      List virtualClassroomSessionGroups = getHibernateTemplate().find(query, new Object[]{siteId, start, end});

      // sort the list
      Collections.sort(virtualClassroomSessionGroups, new SortComparator(sortField, sortOrder));

      // retrieve the individual virtual classroom sessions for each of the virtual classroom session groups
      VirtualClassroomSessionGroup virtualClassroomSessionGroup = null;
      List                         virtualClassroomSessions     = null;

      for (Iterator i=virtualClassroomSessionGroups.iterator(); i.hasNext(); ) {
         virtualClassroomSessionGroup = (VirtualClassroomSessionGroup)i.next();
         virtualClassroomSessions     = getVirtualClassroomSessions(virtualClassroomSessionGroup.getId());
         virtualClassroomSessionGroup.setVirtualClassroomSessions(virtualClassroomSessions);
      }
      return virtualClassroomSessionGroups;
   }

   /**
    * pings the external elluminate server to make sure it is up and running.
    * <br/><br/>
    * <br/><br/>
    * @return true if the server is up and listening for commands and false otherwise.
    * <br/><br/>
    */
   public boolean pingVirtualClassroomServer() {
      logger.info("pingVirtualClassroomServer()");

      String        command           = "getSeatMaximum";
      try {
        SOAPBody body                   = sendCommand(command, new HashMap());

        boolean       result            = elluminateXmlResponseParser.extractTestElmResult(body);
        return result;
      } catch (Exception e) {
          logger.warn("can't ping server" + e.getMessage());
          return false;
      }
   }

   private SOAPBody sendCommand(String command, Map args) throws VirtualClassroomException {
      SOAPConnection connection = null;
      try {
         SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
         connection = soapConnectionFactory.createConnection();
         SOAPFactory soapFactory = SOAPFactory.newInstance();

         MessageFactory factory = MessageFactory.newInstance();
         SOAPMessage message = factory.createMessage();
         
         SOAPHeader header = message.getSOAPHeader();
         SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
         SOAPHeaderElement headerElement = header.addHeaderElement(envelope.createName("BasicAuth", "h", "http://soap-authentication.org/basic/2001/10/"));
         SOAPElement nameElement = headerElement.addChildElement("Name").addTextNode(getServerConfigurationService().getString("elluminate.sys.user", "serversupport"));
         nameElement.setValue(getServerConfigurationService().getString("elluminate.sys.user", "serversupport"));
         headerElement.addChildElement("Password").addTextNode(getServerConfigurationService().getString("elluminate.sys.pass", "elluminate"));
         headerElement.setMustUnderstand(true);

         SOAPBody body = message.getSOAPBody();

         Name bodyName = soapFactory.createName(
               "request", "m",
               "http://www.soapware.org/");
         SOAPBodyElement bodyElement = body.addBodyElement(bodyName);

         Name adapterName = soapFactory.createName("adapter");
         bodyElement.addChildElement(adapterName).addTextNode(adapter);

         Name commandName = soapFactory.createName("command");
         bodyElement.addChildElement(commandName).addTextNode(command);


         for (Iterator i = args.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            Name name = soapFactory.createName(key);
            bodyElement.addChildElement(name).addTextNode(value);

         }
      //   if (logger.isInfoEnabled()) {
             message.writeTo(System.out);
             System.out.println();
      //   }
         SOAPMessage response = connection.call(message, getUrl());

      //   if (logger.isInfoEnabled()) {
             response.writeTo(System.out);
             System.out.println();
      //   }

         SOAPBody soapBody = response.getSOAPBody();

         return soapBody;

      } catch (Exception e) {
         throw new VirtualClassroomException("Unable to send the command " + command + " to the elluminate server.  " + e.getMessage(), e);

      } finally {
         if (connection != null) {
            try {
               connection.close();
            } catch (SOAPException e) {
            }
         }
      }

   }



   /**
    * sets the moderator for an individual virtual classroom session.
    * <br/><br/>
    * @param elluminateSessionId   elluminate id of the virtual classroom session to set the moderator for.
    * @param userId                sakai user id of the moderator.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   If the command can not be executed on the elluminate live virtual classroom server.
    *                                     If the result returned by the elluminate server can not be parsed into a valid xml document,
    *                                     an XmlException will be nested inside and can be retrieved with the getCause() method.
    * <br/><br/>
    * @return the elluminate id of the moderator
    */
   private String setModerator(long elluminateSessionId, String userId) throws AuthorizationException, VirtualClassroomException {
      logger.info("setModerator(), vc session id: " + elluminateSessionId + ", user id: " + userId);

      // look up the sakai user specified by the saki user id
      User user = null;
      try {
         user = getUserDirectoryService().getUser(userId);
      } catch (UserNotDefinedException ex) {
         throw new VirtualClassroomException("The specified moderator " + userId + " is not a valid sakai user.");
      }

      // set the moderator for the virtual classroom session on the elluminate server
      String          elluminateUserId         = createUser(user.getEid(), user.getFirstName(), user.getLastName(), user.getEmail(), MODERATOR_ROLE);
      String        command                    = "setFacilitator";
      Map args = new HashMap();
      args.put("meetingId", String.valueOf(elluminateSessionId));
      args.put("userId", String.valueOf(elluminateUserId));


      SOAPBody body                   = sendCommand(command, args);

      elluminateXmlResponseParser.extractSetFacilitatorResult(body);

      return elluminateUserId;
   }

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
   public void setInstructor(String virtualClassroomSessionGroupId, String instructorId) throws AuthorizationException, VirtualClassroomException {
      VirtualClassroomSessionGroup virtualClassroomSessionGroup = getVirtualClassroomSessionGroup(virtualClassroomSessionGroupId);
      VirtualClassroomSession      virtualClassroomSession      = null;

      virtualClassroomSessionGroup.setInstructorId(instructorId);
      List virtualClassroomSessions = getVirtualClassroomSessions(virtualClassroomSessionGroupId);

      // set the instructor for each of the individual virtual classroom sessions
      for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext(); ) {
         virtualClassroomSession = (VirtualClassroomSession)i.next();
         String elluminateModeratorId = setModerator(virtualClassroomSession.getElluminateId(), instructorId);
         virtualClassroomSession.setElluminateModeratorId(String.valueOf(elluminateModeratorId));
         getHibernateTemplate().save(virtualClassroomSession);
      }

      try {
         // save the virtual classroom session group information to the sakai database
         getHibernateTemplate().save(virtualClassroomSessionGroup);
      } catch (DataAccessException ex) {
         // something went wrong trying to save the data to the database
         // todo: roll back the moderator for the elluminate live virtual classroom session
         throw new VirtualClassroomException(ex);
      }
   }

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
   public VirtualClassroomSessionGroup updateVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException {
      logger.info("updateVirtualClassroomSessionGroup(), virtual classroom session: \n: " + virtualClassroomSessionGroup);

      // check parameter values
      if (virtualClassroomSessionGroup.getName() == null || virtualClassroomSessionGroup.getName().trim().length() ==0)
         throw new VirtualClassroomException("name is a required parameter and can not be null or empty.");
      if (virtualClassroomSessionGroup.getSchedule().getStartDate() == null)
         throw new VirtualClassroomException("start date is a required parameter and can not be null.");
      if (virtualClassroomSessionGroup.getSchedule().getEndDate() == null)
         throw new VirtualClassroomException("end date is a required parameter and can not be null.");
      if (virtualClassroomSessionGroup.getSchedule().getEndDate().getTime() <= virtualClassroomSessionGroup.getSchedule().getStartDate().getTime())
         throw new VirtualClassroomException("end date\\time can not occur at or before the start date\\time.");

      // check permissions
      if (!checkPermission(virtualClassroomSessionGroup, PERMISSION_EDIT)) {
         throw new AuthorizationException("You do not have permission to update virtual classroom session groups.");
      }

      // set who is modifying the virtual classroom session
      virtualClassroomSessionGroup.setModifiedBy(getCurrentUser().getId());
      virtualClassroomSessionGroup.setModifiedOn(new Date());

      // delete the existing virtual classroom session group and then create brand new ones
      deleteVirtualClassroomSessionGroup(virtualClassroomSessionGroup.getId());
      virtualClassroomSessionGroup = createVirtualClassroomSessionGroup(virtualClassroomSessionGroup);

      return virtualClassroomSessionGroup;
   }

    /**
    * returns the http parameter values in the command string encoded to utf-8.
    * <br/><br/>
    * @throws VirtualClassroomException  if any of the http parameters can not be encoded to utf-8.
    */
   private String UrlEncode(String elluminateCommand) throws VirtualClassroomException {
      StringBuffer buffer    = new StringBuffer();
      String       parameter = null;
      String       value     = null;
      int          prevIndex = 0;

      for (int i=elluminateCommand.indexOf("=", prevIndex), j=0; i!=-1; i=elluminateCommand.indexOf("=", prevIndex)) {
         j         = elluminateCommand.indexOf("&", i) != -1 ? elluminateCommand.indexOf("&", i) : elluminateCommand.length();
         parameter = elluminateCommand.substring(prevIndex, i);
         value     = elluminateCommand.substring(i + 1    , j);

         if (prevIndex != 0)
            buffer.append("&");
         buffer.append(parameter);
         buffer.append("=");
         try {
            buffer.append(URLEncoder.encode(value, "UTF-8"));
         } catch (UnsupportedEncodingException ex) {
            throw new VirtualClassroomException("Unable to utf-8 encode the elluminate parameter " + parameter + " in the command " + elluminateCommand + ".", ex);
         }
         prevIndex = j + 1;
      }
      return buffer.toString();
   }
}
