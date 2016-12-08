/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.model;

import java.util.Date;




/**
 * This class is used to model an individual virtual classroom session as well as a recorded virtual classroom session.
 * Individual virtual classroom sessions belong to a parent virtual classroom session group.
 * A recorded virtual classroom session
 */
public class VirtualClassroomSession {
   // class members
   private static int  gracePeriod;        // specifies the time (in minutes) prior to the scheduled start time of the virtual classroom session during which a user can join.
   private static long gracePeriodMs;      // specifies the time (in ms) prior to the scheduled start time of the virtual classroom session during which a user can join.

   // data members
   private String  id;                     // id of this individual virtual classroom session stored in the database
   private String  groupId;                // id of the parent virtual classroom session group
   private String  siteId;                 // id of the site where the virtual classroom tool that created this session resides
   private String  name;                   // name of this individual virtual classroom session
   private Date    startDate;              // start date and time of the virtual classoom session
   private Date    endDate;                // end   date and time of the virtual classoom session
   private String  calendarEventId;        // the id of the calendar event for this session. null otherwise.
   private long    elluminateId;           // unique id generated and returned by the elluminate live server when the virtual classroom session is created
   private String    elluminateModeratorId;  // unique id generated and returned by the elluminate live server when the moderator for the virtual classroom session is set
   private String  elluminateRecordingId;  // unique id generated and returned by the elluminate live server when the virtual classroom session is recorded



   /**
    * default constructor.
    */
   public VirtualClassroomSession() {
      // no code necessary
   }

   /**
    * constructor for an individual virtual classroom session.
    * <br/><br/>
    * @param groupId     id of the parent virtual classroom session group to which this individual virtual classroom session belongs.
    * @param name        name of the individual virtual classroom session
    * @param startDate   start date and time of the individual virtual classroom session
    * @param endDate     end   date and time of the individual virtual classroom session
    */
   public VirtualClassroomSession(String groupId, String siteId, String name, Date startDate, Date endDate) {
      this.groupId   = groupId;
      this.siteId    = siteId;
      this.name      = name;
      this.startDate = startDate;
      this.endDate   = endDate;
   }

   /**
    * copy constructor for an individual virtual classroom session.
    * <br/><br/>
    * @param name                   name of the virtual classroom session
    * @param startDate              start date and time
    * @param elluminateId           id of the original elluminate virtual classroom session
    * @param elluminateRecordingId  id of the recorded elluminate virtual classroom session
    */
   public VirtualClassroomSession(VirtualClassroomSession virtualClassroomSession) {
      this.id                    = copy(virtualClassroomSession.id);
      this.groupId               = copy(virtualClassroomSession.groupId);
      this.siteId                = copy(virtualClassroomSession.siteId);
      this.name                  = copy(virtualClassroomSession.name);
      this.startDate             = copy(virtualClassroomSession.startDate);
      this.endDate               = copy(virtualClassroomSession.endDate);
      this.calendarEventId       = copy(virtualClassroomSession.calendarEventId);
      this.elluminateId          = virtualClassroomSession.elluminateId;
      this.elluminateModeratorId = copy(virtualClassroomSession.elluminateModeratorId);
      this.elluminateRecordingId = copy(virtualClassroomSession.elluminateRecordingId);
   }

   /**
    * constructor for a recorded individual virtual classroom session.
    * <br/><br/>
    * @param name                   name of the virtual classroom session
    * @param startDate              start date and time
    * @param elluminateId           id of the original elluminate virtual classroom session
    * @param elluminateRecordingId  id of the recorded elluminate virtual classroom session
    */
   public VirtualClassroomSession(String name, Date startDate, long elluminateId, String elluminateRecordingId) {
      this.name                  = name;
      this.startDate             = startDate;
      this.elluminateId          = elluminateId;
      this.elluminateRecordingId = elluminateRecordingId;
   }

   /**
    * returns the time in minutes prior to the scheduled start time of the individual virtual classroom sessions during which a user can join.
    * <br/><br/>
    * @return the time in minutes prior to the scheduled start time of the individual virtual classroom sessions during which a user can join.
    */
   public static int getGracePeriod() {
       return gracePeriod;
   }

   /**
    * returns the time in ms prior to the scheduled start time of the individual virtual classroom sessions during which a user can join.
    * <br/><br/>
    * @return the time in ms prior to the scheduled start time of the individual virtual classroom sessions during which a user can join.
    */
   public static long getGracePeriodMS() {
       return gracePeriodMs;
   }

   /**
    * sets the time in minutes prior to the scheduled start time of the individual virtual classroom session during which a user can join.
    * this value is set by the virtual classroom service during it's init() method.
    * <br/><br/>
    * @param period   time in minutes prior to the scheduled start time of the individual virtual classroom session during which a user can join.
    */
   public static void setGracePeriod(int period) {
       gracePeriod   = period;
       gracePeriodMs = (long)gracePeriod * 60L * 1000L;    // convert minutes to ms
   }

   /**
    * returns the id of the virtual classroom session.
    * <br/><br/>
    * @return the id of the virtual classroom session.
    */
   public String getId() {
      return id;
   }

   /*
    * sets the id of the virtual classroom session.
    * <br/><br/>
    * @param id   the id of the virtual classroom session.
    */
   public void setId(String id) {
      this.id = id;
   }

   /**
    * returns the id of the parent virtual classroom session group.
    * <br/><br/>
    * @return the id of the parent virtual classroom session group.
    */
   public String getGroupId() {
      return groupId;
   }

   /*
    * sets the id of the parent virtual classroom session group.
    * <br/><br/>
    * @param groupId   the id of the parent virtual classroom session group.
    */
   public void setGroupId(String groupId) {
      this.groupId = groupId;
   }

   /**
    * returns the id of the site where the virtual classroom tool that created this virtual session resides.
    * <br/><br/>
    * @return the id of the site where the virtual classroom tool that created this virtual session resides.
    */
   public String getSiteId() {
      return siteId;
   }

   /*
    * sets the id of the site where the virtual classroom tool that created this virtual session resides.
    * <br/><br/>
    * @param siteId   the id of the site where the virtual classroom tool that created this virtual session resides.
    */
   public void setSiteId(String siteId) {
      this.siteId = siteId;
   }

   /**
    * returns the name of the virtual classroom session.
    * <br/><br/>
    * @return the name of the virtual classroom session.
    */
   public String getName() {
      return name;
   }

   /**
    * sets the name of the virtual classroom session.
    * <br/><br/>
    * @param name  the name of the virtual classroom session.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * returns the start date and time of the virtual classroom session.
    * <br/><br/>
    * @return the start date and time of the virtual classroom session.
    */
   public Date getStartDate() {
      return startDate;
   }

   /**
    * sets the start date and time of the virtual classroom session.
    * <br/><br/>
    * @param startDate  the start date and time of the virtual classroom session.
    */
   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   /**
    * returns the end date and time of the virtual classroom session.
    * <br/><br/>
    * @return the end date and time of the virtual classroom session.
    */
   public Date getEndDate() {
      return endDate;
   }

   /**
    * sets the end date and time of the virtual classroom session.
    * <br/><br/>
    * @param endDate  the end date and time of the virtual classroom session.
    */
   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }

   /**
    * gets the id for the event added to the user's calendar for the virtual classroom session.
    * <br/><br/>
    * @return the id for the event added to the user's calendar for the virtual classroom session.
    */
   public boolean getIsOnCalendar() {
      return calendarEventId != null;
   }

   /**
    * gets the id for the event added to the user's calendar for the virtual classroom session.
    * <br/><br/>
    * @return the id for the event added to the user's calendar for the virtual classroom session.
    */
   public String getCalendarEventId() {
      return calendarEventId;
   }

   /**
    * sets the id for the event added to the user's calendar for the virtual classroom session.
    * <br/><br/>
    * @param calendarEventId  the id for the event added to the user's calendar for the virtual classroom session.
    */
   public void setCalendarEventId(String calendarEventId) {
      this.calendarEventId = calendarEventId;
   }

   /**
    * gets the id for the virtual classroom session created on the elluminate live server.
    * <br/><br/>
    * @return the id for the virtual classroom session created on the elluminate live server.
    */
   public long getElluminateId() {
      return elluminateId;
   }

   /**
    * sets the id for the virtual classroom session created on the elluminate live server.
    * <br/><br/>
    * @param elluminateId  the id for the virtual classroom session created on the elluminate live server.
    */
   public void setElluminateId(long elluminateId) {
      this.elluminateId = elluminateId;
   }

   /**
    * returns the elluminate moderator id of the virtual classroom session.
    * <br/><br/>
    * @return the elluminate moderator id of the virtual classroom session.
    */
   public String getElluminateModeratorId() {
      return elluminateModeratorId;
   }

   /*
    * sets the elluminate moderator id of the virtual classroom session.
    * <br/><br/>
    * @param elluminateModeratorId   the elluminate moderator id of the virtual classroom session.
    */
   public void setElluminateModeratorId(String elluminateModeratorId) {
      this.elluminateModeratorId = elluminateModeratorId;
   }

   /**
    * gets the id for the recorded virtual classroom session created on the elluminate live server.
    * <br/><br/>
    * @return the id for the recorded virtual classroom session created on the elluminate live server.
    */
   public String getElluminateRecordingId() {
      return elluminateRecordingId;
   }

   /**
    * sets the id for the recorded virtual classroom session created on the elluminate live server.
    * <br/><br/>
    * @param elluminateRecordingId  the id created on the elluminate live server when the the virtual classroom session is recorded.
    */
   public void setElluminateRecordingId(String elluminateRecordingId) {
      this.elluminateRecordingId = elluminateRecordingId;
   }

   /**
    * returns whether this individual virtual classroom session is joinable.
    * To be joinable, the following conditions must be true:
    * <ol>
    *    <li>the individual virtual classroom session is not a recording</li>
    *    <li>session start time - grace period <= current time <= session end time</li>
    * </ol>
    * <br/><br/>
    * @return true if the virtual classroom session is joinable and false otherwise.
    */
   public boolean isJoinable() {
      long currentTime = System.currentTimeMillis();

      return (!isRecording() && currentTime >= (startDate.getTime() - gracePeriodMs) && currentTime <= endDate.getTime());
   }

   /**
    * returns whether the individual virtual classroom session has already occurred.
    * if this is a recorded session, then this method returns false.
    * <br/><br/>
    * @return true if the virtual classroom session has already occurred, or false if it is a recorded session.
    */
   public boolean isOccurred() {
      long currentTime = System.currentTimeMillis();

      return (!isRecording() && currentTime > endDate.getTime());
   }

   /**
    * returns whether the virtual classroom session is a live virtual classroom session or a recording of a virtual classroom session.
    * <br/><br/>
    * @return true if the virtual classroom session is a recording of a virtual classroom session, and false otherwise.
    */
   public boolean isRecording() {
      return elluminateRecordingId != null;
   }

   /**
    * returns whether the individual virtual classroom session is scheduled to take place in the future.
    * To be scheduled for the future, the following conditions must be true:
    * <ol>
    *    <li>the individual virtual classroom session is not a recording</li>
    *    <li>current time &lt; session start time - grace period</li>
    * </ol>
    * <br/><br/>
    * @return true if the virtual classroom session is scheduled to start in the future.
    */
   public boolean isScheduled() {
      long currentTime = System.currentTimeMillis();

      return (!isRecording() && currentTime < (startDate.getTime() - gracePeriodMs));
   }

   /**
    * returns whether the virtual classroom session has already started but has not yet finished.
    * if the session is a recorded session, this method returns false.
    * <br/><br/>
    * @return true if the virtual classroom session has already started but has not yet finished.
    */
   public boolean isStarted() {
      long currentTime = System.currentTimeMillis();

      return (!isRecording() && currentTime >= startDate.getTime() && currentTime <= endDate.getTime());
   }

   /**
    * returns the duration of the virtual classroom session.
    * if the session is a recorded session, then the end time will be null.
    */
   public long getDuration() {
      return (isRecording() ? 0L : endDate.getTime() - startDate.getTime());
   }

   /**
    * returns a string representation of this instance.
    */
   public String toString() {
      StringBuffer buffer = new StringBuffer();

      buffer.append("id.....................: " + id                    + "\n");
      buffer.append("group id...............: " + groupId               + "\n");
      buffer.append("site id................: " + siteId                + "\n");
      buffer.append("name...................: " + name                  + "\n");
      buffer.append("grace period...........: " + gracePeriod           + "\n");
      buffer.append("start date.............: " + startDate             + "\n");
      buffer.append("end date...............: " + endDate               + "\n");
      buffer.append("calendar event id......: " + calendarEventId       + "\n");
      buffer.append("elluminate id..........: " + elluminateId          + "\n");
      buffer.append("elluminate moderator id: " + elluminateModeratorId + "\n");
      buffer.append("elluminate recording id: " + elluminateRecordingId + "\n");
      buffer.append("is joinable............: " + isJoinable()          + "\n");
      buffer.append("scheduled later........: " + isScheduled()         + "\n");
      buffer.append("has occurred...........: " + isOccurred()          + "\n");
      buffer.append("is recording...........: " + isRecording()         + "\n");

      return buffer.toString();
   }

   protected Schedule copy(Schedule schedule) {
      return (schedule == null ? null : new Schedule(schedule));
   }

   protected boolean[] copy(boolean[] array) {
      boolean[] array2 = null;

      if (array != null) {
         array2 = new boolean[array.length];
         for (int i=0; i<array.length; ++i)
            array2[i] = array[i];
      }
      return array2;
   }

   protected Boolean copy(Boolean b) {
      return (b == null ? null : new Boolean(b.booleanValue()));
   }

   protected Character copy(Character c) {
      return (c == null ? null : new Character(c.charValue()));
   }

   protected Date copy(Date date) {
      return (date == null ? null : new Date(date.getTime()));
   }

   protected Integer copy(Integer i) {
      return (i == null ? null : new Integer(i.intValue()));
   }

   protected Long copy(Long l) {
      return (l == null ? null : new Long(l.longValue()));
   }

   protected String copy(String string) {
      return (string == null ? null : new String(string));
   }
}
