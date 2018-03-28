/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;



/**
 * This class is used to model a virtual classroom session group.
 * A virtual classroom group contains a list of individual virtual classroom sessions as well as the attributes that specify how to generate them.
 * <p>
 * A recording of an individual virtual classroom session is treated as a separate entity and will have its own virtual classroom session group.
 * That is, for a recording, a virtual classroom session group will have exactly one VirtualClassroomSession object stored in the virtualClassroomSessions list.
 * </p>
 */
public class VirtualClassroomSessionGroup {
   // data members
   private String    id;                          // id of this virtual classroom group stored in the database
   private String    siteId;                      // id of the site where the virtual classroom tool that created this group resides
   private String    name;                        // name of the virtual classoom group, ie. "anatomy 101, section 2012"
   private int       capacity;                    // the maximum number of users that may participate in an individual virtual classoom session
   private String    instructorId;                // sakai user id of the person who will be leading the individual virtual classoom sessions
   private Schedule schedule;                    // schedule of the individual virtual classroom sessions
   private boolean   addMeetingDateToSessionName; // whether to add the meeting date to the individual virtual classroom session names when they are created.  otherwise, they will have the same name as the virtual classroom session group.
   private List      virtualClassroomSessions;    // individual virtual classroom sessions created according to the schedule
   private String    createdBy;                   // sakai user id who created this virtual classroom session
   private Date      createdOn;                   // date the virtual classroom session was created
   private String    modifiedBy;                  // sakai user id who last modified this virtual classroom session (may be null)
   private Date      modifiedOn;                  // date the virtual classroom session was modified (may be null)
   private int maxTalkers = 1;             // maximum number of simultaneous talkers
   private boolean isPrivate;              // true is session is private, faslse if session is public




   /**
    * default constructor.
    */
   public VirtualClassroomSessionGroup() {
      schedule                   = new Schedule();
      virtualClassroomSessions   = new ArrayList();
      addMeetingDateToSessionName = true;
   }

   /**
    * constructor for a virtual classroom session group.
    * <br/><br/>
    * @param name             name of the virtual classroom session group
    * @param capacity         the maximum number of people allowed to join the individual virtual classroom sessions
    * @param instructorId     sakai user id of the instructor who will lead the individual virtual classroom sessions
    * @param schedule         schedule for the virtual classroom sessions
    */
   public VirtualClassroomSessionGroup(String name, int capacity, String instructorId, Schedule schedule) {
      this.name                        = name;
      this.capacity                    = capacity;
      this.instructorId                = instructorId;
      this.schedule                    = schedule;
      this.virtualClassroomSessions    = new ArrayList();
      this.addMeetingDateToSessionName = true;
   }

   /**
    * copy constructor for a virtual classroom session group.
    * <br/><br/>
    * @param virtualClassroomSessionGroup  virtual classroom session group whose values are to be used to create the copy.
    */
   public VirtualClassroomSessionGroup(VirtualClassroomSessionGroup virtualClassroomSessionGroup) {
      this.id                          = copy(virtualClassroomSessionGroup.id);
      this.siteId                      = copy(virtualClassroomSessionGroup.siteId);
      this.name                        = copy(virtualClassroomSessionGroup.name);
      this.capacity                    = virtualClassroomSessionGroup.capacity;
      this.instructorId                = copy(virtualClassroomSessionGroup.instructorId);
      this.schedule                    = copy(virtualClassroomSessionGroup.schedule);
      this.addMeetingDateToSessionName = virtualClassroomSessionGroup.addMeetingDateToSessionName;
      this.virtualClassroomSessions    = new ArrayList();
      this.createdBy                   = copy(virtualClassroomSessionGroup.createdBy );
      this.createdOn                   = copy(virtualClassroomSessionGroup.createdOn );
      this.modifiedBy                  = copy(virtualClassroomSessionGroup.modifiedBy);
      this.modifiedOn                  = copy(virtualClassroomSessionGroup.modifiedOn);
      this.isPrivate                   = copy(virtualClassroomSessionGroup.isPrivate);
      this.maxTalkers                  = copy(virtualClassroomSessionGroup.maxTalkers);

      for (Iterator i=virtualClassroomSessionGroup.virtualClassroomSessions.iterator(); i.hasNext(); ) {
         VirtualClassroomSession virtualClassroomSession = (VirtualClassroomSession)i.next();
         this.virtualClassroomSessions.add(new VirtualClassroomSession(virtualClassroomSession));
      }
   }

   /**
    * returns the id of the virtual classroom session group.
    * <br/><br/>
    * @return the id of the virtual classroom session group.
    */
   public String getId() {
      return id;
   }

   /*
    * sets the id of the virtual classroom session group.
    * <br/><br/>
    * @param id   the id of the virtual classroom session group.
    */
   public void setId(String id) {
      this.id = id;
   }

   /**
    * returns the id of the site where the virtual classroom tool that created this virtual session group resides.
    * <br/><br/>
    * @return the id of the site where the virtual classroom tool that created this virtual session group resides.
    */
   public String getSiteId() {
      return siteId;
   }

   /*
    * sets the id of the site where the virtual classroom tool that created this virtual session group resides.
    * <br/><br/>
    * @param siteId   the id of the site where the virtual classroom tool that created this virtual session group resides.
    */
   public void setSiteId(String siteId) {
      this.siteId = siteId;
   }

   /**
    * returns the name of the virtual classroom session group.
    * <br/><br/>
    * @return the name of the virtual classroom session group.
    */
   public String getName() {
      return name;
   }

   /**
    * sets the name of the virtual classroom session group.
    * <br/><br/>
    * @param name  the name of the virtual classroom session group.
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * returns the capacity of the individual virtual classroom sessions.
    * <br/><br/>
    * @return the maximum number of users that may participate in an individual virtual classoom session.
    */
   public int getCapacity() {
      return capacity;
   }

   /*
    * sets the capacity of the individual virtual classroom sessions.
    * <br/><br/>
    * @param capacity   the maximum number of users that may participate in an individual virtual classoom session.
    */
   public void setCapacity(int capacity) {
      this.capacity = capacity;
   }

   /**
    * returns the sakai user id of the instructor who will lead the individual virtual classroom sessions.
    * <br/><br/>
    * @return the sakai user id of the instructor who will lead the individual virtual classroom sessions.
    */
   public String getInstructorId() {
      return instructorId;
   }

   /*
    * sets the sakai user id of the instructor who will lead the individual virtual classroom sessions.
    * <br/><br/>
    * @param instructorId   the sakai user id of the instructor who will lead the individual virtual classroom sessions.
    */
   public void setInstructorId(String instructorId) {
      this.instructorId = instructorId;
   }

   /**
    * returns the schedule of the virtual classroom sessions.
    * <br/><br/>
    * @return the schedule of the virtual classroom sessions.
    */
   public Schedule getSchedule() {
      return schedule;
   }

   /**
    * sets the schedule of the virtual classroom sessions.
    * <br/><br/>
    * @param schedule  the schedule of the virtual classroom sessions.
    */
   public void setSchedule(Schedule schedule) {
      this.schedule = schedule;
   }

   /**
    * returns whether the individual virtual classroom sessions will have the meeting date appended to their names.
    * <br/><br/>
    * @return whether the individual virtual classroom sessions will have the meeting date appended to their names.
    */
   public boolean isAddMeetingDateToSessionName() {
      return addMeetingDateToSessionName;
   }

   /**
    * sets whether the individual virtual classroom sessions will have the meeting date appended to their names.
    * <br/><br/>
    * @param addMeetingDateToSessionName  whether the individual virtual classroom sessions will have the meeting date appended to their names.
    */
   public void setAddMeetingDateToSessionName(boolean addMeetingDateToSessionName) {
      this.addMeetingDateToSessionName = addMeetingDateToSessionName;
   }

   /**
    * returns the list of individual virtual classroom sessions.
    * <br/><br/>
    * @return the list of individual virtual classroom sessions.
    */
   public List getVirtualClassroomSessions() {
      return virtualClassroomSessions;
   }

   /**
    * sets the list of individual virtual classroom sessions.
    * <br/><br/>
    * @param virtualClassroomSessions  the list of individual virtual classroom sessions.
    */
   public void setVirtualClassroomSessions(List virtualClassroomSessions) {
      this.virtualClassroomSessions = (virtualClassroomSessions == null ? new ArrayList() : virtualClassroomSessions);
   }

   /**
    * gets the sakai user id of the user who created the virtual classroom session group.
    */
   public String getCreatedBy() {
       return createdBy;
   }

   /**
    * sets the sakai user id of the user who created the virtual classroom session group.
    * <br/><br/>
    * @param userId   the sakai user id of the user who created the virtual classroom session group.
    */
   public void setCreatedBy(String userId) {
       this.createdBy = userId;
   }

   /**
    * gets the date the virtual classroom session group was created.
    */
   public Date getCreatedOn() {
       return createdOn;
   }

   /**
    * sets the date the virtual classroom session group was created.
    * <br/><br/>
    * @param date   the date the virtual classroom session group was created.
    */
   public void setCreatedOn(Date date) {
       this.createdOn = date;
   }

   /**
    * gets the sakai user id of the user who last updated the virtual classroom session group.
    * if the virtual classroom session group has never been updated, this will return null.
    */
   public String getModifiedBy() {
       return modifiedBy;
   }

   /**
    * sets the sakai user id of the user who last updated the virtual classroom session group.
    * <br/><br/>
    * @param userId   the sakai user id of the user who last updated the virtual classroom session group.
    */
   public void setModifiedBy(String userId) {
       this.modifiedBy = userId;
   }

   /**
    * gets the date the last time the virtual classroom session group was updated.
    * if the virtual classroom session group has never been updated, this will return null.
    */
   public Date getModifiedOn() {
       return modifiedOn;
   }

   /**
    * sets the date the last time the virtual classroom session group was updated.
    * <br/><br/>
    * @param date   the date the virtual classroom session group was last updated.
    */
   public void setModifiedOn(Date date) {
       this.modifiedOn = date;
   }

    public int getMaxTalkers() {
        return maxTalkers;
    }

    public void setMaxTalkers(int maxTalkers) {
        this.maxTalkers = maxTalkers;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }
        
    public void setIsPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    

    /**
    * If this virtual classroom session group contains an individual virtual classroom session that is joinable, then it is returned.
    * Otherwise, null is returned.
    */
   public VirtualClassroomSession getJoinableSession() {
      VirtualClassroomSession virtualClassroomSession = null;

      if (!isRecorded() && isInProgress()) {
         // loop over the individual virtual classroom sessions and see if any are joinable.
         VirtualClassroomSession vc = null;
         for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext() && virtualClassroomSession == null; ) {
            vc = (VirtualClassroomSession)i.next();
            if (vc.isJoinable())
               virtualClassroomSession = vc;
         }
      }
      return virtualClassroomSession;
   }

   /**
    * If this virtual classroom session group is a recording, then the virtual classroom session recording is returned.
    * Otherwise, null is returned.
    */
   public VirtualClassroomSession getRecording() {
      VirtualClassroomSession recording = null;

      if (isRecorded())
         recording = (VirtualClassroomSession)virtualClassroomSessions.get(0);

      return recording;
   }

   /**
    * returns whether all the individual virtual classroom sessions have already occurred.
    * <br/><br/>
    * @return true if all the individual virtual classroom sessions have already occurred.
    */
   public boolean isEnded() {
      boolean ended = false;

      if (!isRecorded()) {
         long currentTime = System.currentTimeMillis();
         ended = currentTime > schedule.getEndDate().getTime();
      }

      return ended;
   }

   /**
    * returns whether the individual virtual classroom sessions have begun but not yet finished.
    * <br/><br/>
    * @return true if the individual virtual classroom sessions have begun but not yet finished.
    */
   public boolean isInProgress() {
      if (!schedule.isRecurring()) {
        return !isRecorded() && isStarted() && !isEnded();
      }

       // loop over the individual virtual classroom sessions and see if any are joinable.
       for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext(); ) {
          if (!isRecorded() && ((VirtualClassroomSession)i.next()).isJoinable()) return true;
       }

       return false;
   }

   /**
    * returns whether any of the individual virtual classroom sessions are joinable.
    * <br/><br/>
    * @return true   if any of the individual virtual classroom sessions are joinable and false otherwise.
    */
   public boolean isJoinable() {
      boolean joinable = false;

      if (!isRecorded() && isInProgress()) {
         // loop over the individual virtual classroom sessions and see if any are joinable.
         for (Iterator i=virtualClassroomSessions.iterator(); i.hasNext() && !joinable; )
            joinable = ((VirtualClassroomSession)i.next()).isJoinable();
      }
      return joinable;
   }

   /**
    * returns whether this virtual classroom session group only contains a single recording.
    */
   public boolean isRecorded() {
      return (virtualClassroomSessions.size() == 1 && ((VirtualClassroomSession)virtualClassroomSessions.get(0)).isRecording());
   }

   /**
    * returns whether the first individual virtual classroom session has started.
    * <br/><br/>
    * @return true if the first individual virtual classroom session has started.
    */
   public boolean isStarted() {
      boolean started = false;

      if (!isRecorded()) {
         long currentTime = System.currentTimeMillis();
         started = currentTime > (schedule.getStartDate().getTime() - VirtualClassroomSession.getGracePeriodMS());
      }
      return started;
   }

   /**
    * returns a string representation of this instance.
    */
   public String toString() {
      StringBuffer     buffer        = new StringBuffer();
      SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
      SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm aa");


      buffer.append("id................................: " + id                                            + "\n");
      buffer.append("name..............................: " + name                                          + "\n");
      buffer.append("site id...........................: " + siteId                                        + "\n");
      buffer.append("capacity..........................: " + capacity                                      + "\n");
      buffer.append("instructor id.....................: " + instructorId                                  + "\n");
      buffer.append("schedule..........................: " + schedule                                      + "\n");
      buffer.append("add meeting date to session name..: " + addMeetingDateToSessionName                   + "\n");
      buffer.append("virtual classroom sessions........: " + virtualClassroomSessions.size()               + "\n");
      buffer.append("group start date..................: " + dateFormatter.format(schedule.getStartDate()) + "\n");
      buffer.append("group end   date..................: " + dateFormatter.format(schedule.getStartDate()) + "\n");
      buffer.append("session start time................: " + timeFormatter.format(schedule.getStartDate()) + "\n");
      buffer.append("session end   time................: " + timeFormatter.format(schedule.getStartDate()) + "\n");
      buffer.append("created by........................: " + createdBy                                     + "\n");
      buffer.append("created on........................: " + createdOn                                     + "\n");
      buffer.append("modified by.......................: " + modifiedBy                                    + "\n");
      buffer.append("modified on.......................: " + modifiedOn                                    + "\n");
      buffer.append("is recording......................: " + isRecorded()                                  + "\n");
      buffer.append("has started.......................: " + isStarted()                                   + "\n");
      buffer.append("has ended.........................: " + isEnded()                                     + "\n");
      buffer.append("in progress.......................: " + isInProgress()                                + "\n");
      buffer.append("is joinable.......................: " + isJoinable()                                  + "\n");

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
