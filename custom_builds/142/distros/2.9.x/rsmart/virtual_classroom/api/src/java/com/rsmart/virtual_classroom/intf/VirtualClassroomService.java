/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.intf;

import com.rsmart.virtual_classroom.model.AuthorizationException;
import com.rsmart.virtual_classroom.model.VirtualClassroomException;
import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;

import java.util.Date;
import java.util.List;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;




/**
 * This interface specifies the methods that a virtual classroom service will provide.
 * These methods allow clients to create and attend virtual class room sessions.
 */
public interface VirtualClassroomService {
   // sakai.properties keys
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADDRESS                   = "virtual_classroom.server.address";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_ADAPTER                   = "virtual_classroom.server.adapter";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_CAPACITY          = "virtual_classroom.default_capacity";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_GRACE_PERIOD              = "virtual_classroom.server.grace_period";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY              = "virtual_classroom.server.max_capacity";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_PORT                      = "virtual_classroom.server.port";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT           = "virtual_classroom.username.format";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PRIVATE           = "virtual_classroom.default_private";

   // default values for the sakai.properties if they are not specified
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_ADDRESS           = "localhost";
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_ADAPTER           = "default";
   public static final int    SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_DEFAULT_CAPACITY  = 50;
   public static final int    SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_GRACE_PERIOD      = 60;
   public static final int    SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_MAX_CAPACITY      = 1;
   public static final int    SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_PORT              = 80;
   public static final String SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_DEFAULT_USERNAME_FORMAT   = "lastname_firstname";

   // display formats for usernames when a user joins a virtual classroom session
   public static final String VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_1                          = "sakai_username";
   public static final String VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_2                          = "firstname_lastname";
   public static final String VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_3                          = "lastname_firstname";
   public static final String VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_4                          = "e-mail";
   public static final String VIRTUAL_CLASSROOM_SERVER_USERNAME_FORMAT_5                          = "nickname";

   // user types
   public static final String USER_TYPE_INSTRUCTOR                                                = "instructor";
   public static final String USER_TYPE_REGISTERED                                                = "registered";

   // permissions
   public static final String PERMISSION_CREATE                                                   = "virtual_classroom_session.create";
   public static final String PERMISSION_DELETE                                                   = "virtual_classroom_session.delete";
   public static final String PERMISSION_EDIT                                                     = "virtual_classroom_session.edit";
   public static final String PERMISSION_JOIN                                                     = "virtual_classroom_session.join";
   public static final String PERMISSION_VIEW                                                     = "virtual_classroom_session.view";

   // sort order for methods that return lists
   public static final int    SORT_ORDER_ASCENDING  = 1;
   public static final int    SORT_ORDER_DESCENDING = 2;

   // sort fields for methods that return lists
   public static final int    SORT_FIELD_JOIN       = 1;
   public static final int    SORT_FIELD_NAME       = 2;
   public static final int    SORT_FIELD_START_DATE = 3;



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
   public VirtualClassroomSessionGroup createVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException;

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
// public VirtualClassroomSession createVirtualClassroomSession(VirtualClassroomSession virtualClassroomSession, boolean addEventToCalendar) throws AuthorizationException, VirtualClassroomException;

   /**
    * delete a virtual classroom session recording.
    * <br/><br/>
    * @param elluminateRecordingId   id of the recorded virtual classroom session to be deleted.
    * <br/><br/>
    * @throws AuthorizationException     if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException  if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return recording that was deleted
    */
// public VirtualClassroomSession deleteRecording(String elluminateRecordingId) throws AuthorizationException, VirtualClassroomException;

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
   public VirtualClassroomSessionGroup deleteVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns the currrent worksite.
    */
   public Site getCurrentSite();

   /**
    * returns the default capacity of a virtual classroom session.
    * This value is used when a user goes to create a new virtual classroom session.
    */
   public int getDefaultCapacity();

   /**
    * returns the currrent user.
    */
   public User getCurrentUser();

   /**
    * returns the time (in minutes) prior to the start of a virtual classroom session in which a user can join.
    */
   public int getGracePeriod();

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
   public String getJoinUrl(String id) throws AuthorizationException , VirtualClassroomException;

   /**
    * returns the maximum number of concurrent users the virtual classroom server supports.
    */
   public int getMaxNumConcurrentUsers();

   /**
    * returns the current number of users for the specified site.
    */
   public int getNumMembersOnSite(String siteId);

   /**
    * retrieves a list of url's which will launch a virtual classroom session recording for all the recordings for the specified virtual classroom session.
    * <br/><br/>
    * @param id   id of the virtual classroom session whose recordings are to be retrieved.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return the jnlp xml file need to launch the recording.
    */
// public List getRecordingsJNLP(String id) throws AuthorizationException, VirtualClassroomException;

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
   public String getViewUrl(long elluminateId, String elluminateRecordingId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns the time zone of the machine on which virtual classroom server is running.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return the time zone of the machine on which virtual classroom server is running.
    */
   public String getServerTimeZone() throws AuthorizationException, VirtualClassroomException;

   /**
    * returns the user with the specified sakai user id.
    * If no user can be found with the specified id, null is returned.
    */
   public User getUser(String id);

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
   public VirtualClassroomSessionGroup getVirtualClassroomSessionGroup(String id) throws AuthorizationException, VirtualClassroomException;

   /**
    * determines if the virtual classroom session server has sufficient capacity to handle the specified additional capacity during the given start and end times.
    * Most virtual classroom servers limit the number of allowable concurrent users as part of their licensing.  This method looks at the value of the
    * SAKAI_PROPERTIES_VIRTUAL_CLASSROOM_SERVER_MAX_CAPACITY sakai property which should be set to the virtual classroom server's maximum number of allowabled concurrent users
    * and determines if there is a sufficient number of seats left during the given start and end times to handle the requested additional capacity.
    * <br/><br/>
    * @param siteId               search only virtual classrooms with this siteId
    * @param start                the start date and time during which the virtual classroom server concurrent user capacity is to be checked.
    * @param end                  the end   date and time during which the virtual classroom server concurrent user capacity is to be checked.
    * @param additionalCapacity   the number of additional seats that will be needed during the specified time.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the number of currently scheduled virtual classroom sessions during the specified time period can not be retrieved.
    * <br/><br/>
    * @return  whether or not the virtual classroom server has enough seating capacity left to handle the specified additional capacity during the specified start and end times.
    */
   public boolean hasSufficientCapacity(String siteId, Date start, Date end, int additionalCapacity) throws AuthorizationException, VirtualClassroomException;

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
   public List listAllSessionGroupsAndRecordings(String siteId) throws AuthorizationException, VirtualClassroomException;

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
   public List listAllSessionGroupsAndRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

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
   public List listRecordings(String siteId) throws AuthorizationException, VirtualClassroomException;

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
   public List listRecordings(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session sorted by start date in ascending order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, String virtualClassroomSessionId) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session sorted by the specified field and order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * @param sortField                    specifies which of the three possible fields the sessions are to be sorted on.  It must have the value of either SORT_FIELD_JOIN, SORT_FIELD_NAME, or SORT_FIELD_START_DATE.
    * @param sortOrder                    specifies the whether the sessions are sorted by sort field in ascending or descending order. It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, String virtualClassroomSessionId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai work site that occured between two dates sorted by the start date in ascending order.
    * <br/><br/>
    * @param siteId       search only virtual classrooms with this siteId
    * @param startDate    the date after  which the recording occured.
    * @param endDate      the date before which the recording finished.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, Date startDate, Date endDate) throws AuthorizationException, VirtualClassroomException;

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
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, Date startDate, Date endDate, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * returns a list of all recordings for the specified sakai work site for a given virtual classroom session between two dates sorted by start date in ascending order.
    * <br/><br/>
    * @param siteId                       search only virtual classrooms with this siteId.
    * @param virtualClassroomSessionId    the id of the virtual classroom session for which the recordings are to be returned.
    * @param startDate                    the date after  which the recording occured.
    * @param endDate                      the date before which the recording finished.
    * <br/><br/>
    * @throws AuthorizationException      if the current sakai user does not have sufficient rights to invoke this method.
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, String virtualClassroomSessionId, Date startDate, Date endDate) throws AuthorizationException, VirtualClassroomException;

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
    * @throws VirtualClassroomException   if the command can not be executed on the virtual classroom server.
    * <br/><br/>
    * @return  list of recordings
    */
// public List listRecordings(String siteId, String virtualClassroomSessionId, Date startDate, Date endDate, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

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
   public List listVirtualClassroomSessionGroups(String siteId) throws AuthorizationException, VirtualClassroomException;

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
   public List listVirtualClassroomSessionGroups(String siteId, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

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
   public List listVirtualClassroomSessionGroups(String siteId, Date start, Date end) throws AuthorizationException, VirtualClassroomException;

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
   public List listVirtualClassroomSessionGroups(String siteId, Date start, Date end, int sortField, int sortOrder) throws AuthorizationException, VirtualClassroomException;

   /**
    * pings the external virtual classroom server to make sure it is up and running.
    * <br/><br/>
    * <br/><br/>
    * @return true if the server is up and listening for commands and false otherwise.
    * <br/><br/>
    */
   public boolean pingVirtualClassroomServer();

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
   public void setInstructor(String virtualClassroomSessionGroupId, String instructorId) throws AuthorizationException, VirtualClassroomException;

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
   public VirtualClassroomSessionGroup updateVirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) throws AuthorizationException, VirtualClassroomException;

   public boolean isPrivateDefault();
}
