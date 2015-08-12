/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */
package com.rsmart.virtual_classroom.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * This class represents a schedule which describes a recurring event.
 * For example, suppose your taking a calculus class that meets every tuesday and thursday from 9:30 to 10:45, beginning septemeber 7th, 2006 and ending december 7th, 2006.
 * You can store this information in a Schedule object.
 * <p>
 * The daysOfWeekS data member is used to store the daysOfWeek data member in hibernate.
 * Thus, after loading an instance of a schedule from the database, you must call the setDaysOfWeek(String daysOfWeek) method, and conversely,
 * before saving an instance of a schedule to the database, you must call the setDaysOfWeekS() method.
 * </p>
 */
public class Schedule {
   // class members                      ms     sec   min   hrs
   private static final long ONE_HOUR = 1000L * 60L * 60L;
   private static final long ONE_DAY  = 1000L * 60L * 60L * 24L;


   // data members
   private String    displayName;           // used to store a formatted version of the schedule (via one of the getXXXXDescription() methods or via one of the the scheduleService.getScheduleDisplay() methods)
   private Date      startDate;             // start date and time
   private Date      endDate;               // end   date and time
   private long      duration;              // duration of an individual event
   private boolean   addEventToCalendar;    // whether to create entries on the sakai calendar tool for the events
   private String    timeZone;              // what time zone the startDate and endDate are in
   private boolean   recurring;             // is this a one time event or a recurring event?
   private boolean[] daysOfWeek;            // sunday=0, monday=1, ..., saturday=6
   private String    daysOfWeekString;      // internal field used to persist and load the daysOfWeek field


   /**
    * default constructor.
    * Sets default values for the data members:
    * <ol>
    *    <li><i>startDate</i> time to today's date with the time set to the next whole hour</li>
    *    <li><i>endDate</i> time to one hour after the start date's time</li>
    *    <li><i>addEventToCalendar</i> to true</li>
    *    <li><i>recurring</i> to true</li>
    * </ol>
    */
   public Schedule() {
      // create a calendar set to the current date and time with the minutes set to 0.
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(new Date());
      calendar.set(Calendar.MINUTE      , 0);
      calendar.set(Calendar.SECOND      , 0);
      calendar.set(Calendar.MILLISECOND , 0);

      startDate          = new Date(calendar.getTime().getTime() + ONE_HOUR);    // make the start time one hour from now
      endDate            = new Date(startDate.getTime()          + ONE_HOUR);    // make the end time one hour from the start
      duration           = endDate.getTime() - startDate.getTime();
      addEventToCalendar = true;
      timeZone           = null;
      recurring          = true;
      daysOfWeek         = new boolean[7];
      daysOfWeekString   = "0000000";
      displayName        = getLongDescription();
   }

   /**
    * constructor.
    */
   public Schedule(Date startDate, Date endDate, boolean addEventToCalendar, String timeZone, boolean recurring, boolean[] daysOfWeek) {
      this.startDate          = startDate;
      this.endDate            = endDate;
      this.duration           = endDate.getTime() - startDate.getTime();
      this.addEventToCalendar = addEventToCalendar;
      this.timeZone           = timeZone;
      this.recurring          = recurring;
      setDaysOfWeek(daysOfWeek);
      this.displayName        = getLongDescription();
   }

   /**
    * copy constructor.
    */
   public Schedule(Schedule schedule) {
      this.displayName        = copy(schedule.displayName);
      this.startDate          = copy(schedule.startDate);
      this.endDate            = copy(schedule.endDate);
      this.duration           = schedule.duration;
      this.addEventToCalendar = schedule.addEventToCalendar;
      this.timeZone           = copy(schedule.timeZone);
      this.recurring          = schedule.recurring;
      this.daysOfWeek         = copy(schedule.daysOfWeek);
      this.daysOfWeekString   = copy(schedule.daysOfWeekString);
   }

   public int hashCode() {
      return getId().hashCode();
   }

   /**
    * @return id based on the start, end dates, and daysOfWeek
    */
   public String getId(){
      StringBuffer buf = new StringBuffer();
      SimpleDateFormat format = new SimpleDateFormat("yyyMMddhhmma");
      buf.append(format.format(startDate));
      buf.append(format.format(endDate));
      for (int i=0;i<daysOfWeek.length;i++){
         buf.append(daysOfWeek[i] ? 1:0);
      }
      return buf.toString();
   }

   public String getDisplayName() {
      return displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public boolean[] getDaysOfWeek() {
      return daysOfWeek;
   }

   /**
    * sets the days of the week boolean array as well as the corresponding daysOfWeekSring field.
    */
   public void setDaysOfWeek(boolean[] daysOfWeek) {
      if (daysOfWeek == null || daysOfWeek.length != 7)
         throw new IllegalArgumentException("days of week argument did not have a size of 7");

      this.daysOfWeek = daysOfWeek;

      StringBuffer buffer = new StringBuffer();

      for (int i=0; i<7; ++i)
         buffer.append(daysOfWeek[i] ? '1' : '0');
      daysOfWeekString = buffer.toString();
   }

   public void setDaysOfWeekFromString() {
      for (int i=0; i<7; ++i)
         this.daysOfWeek[i] = daysOfWeekString.charAt(i) == '1';
   }

   public String getDaysOfWeekString() {
      return daysOfWeekString;
   }

   public void setDaysOfWeekString(String daysOfWeekString) {
      this.daysOfWeekString = daysOfWeekString;

      for (int i=0; i<7; ++i)
         daysOfWeek[i] = daysOfWeekString.charAt(i) == '1';
   }

   public void setDaysOfWeekStringFromArray() {
      setDaysOfWeek(getDaysOfWeek());
   }

   public Date getStartDate() {
      return startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public Date getEndDate() {
      return endDate;
   }

   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }

   public long getDuration() {
      return duration;
   }

   public void setDuration(long duration) {
      this.duration = duration;
   }

   /**
    * calculates the day of the week for a non-recurring event.
    */
   public void setNonRecurringAndDaysOfWeek() {
      Calendar calendar = new GregorianCalendar();
      int      day      = 0;

      this.recurring = false;
      calendar.setTime(startDate);
      day = calendar.get(Calendar.DAY_OF_WEEK);        // java calendar has sun=1, mon=2, ..., sat=7
      day--;                                           // whereas we use    sun=0, mon=1, ..., sat=6

      boolean[] dayOfWeek = new boolean[7];
      dayOfWeek[day] = true;
      setDaysOfWeek(dayOfWeek);
   }

   /**
    * calculates the list of start dates/times for each of the individual events specified by the schedule.
    */
   public List calculateDates() {
      Calendar  calendar = new GregorianCalendar();
      ArrayList dates    = new ArrayList();
      Date      date     = null;
      int       day      = 0;

      if (recurring) {
         for (long millis=startDate.getTime(); millis <= endDate.getTime() - duration; millis += ONE_DAY) {
            date = new Date(millis);
            calendar.setTime(date);
            day = calendar.get(Calendar.DAY_OF_WEEK);        // java calendar has sun=1, mon=2, ..., sat=7
            day--;                                           // whereas we use    sun=0, mon=1, ..., sat=6
            if (daysOfWeek[day])
               dates.add(date);
         }
      } else {
         dates.add(new Date(startDate.getTime()));
      }
      calculateDuration();
      return dates;
   }

   /**
    * calculates the duration of an individual event in the schedule based on the recurring flag, start date, and end date.
    */
   public void calculateDuration() {
      if (isRecurring()) {
         // create a calendar set to the start date's date but with the end date's time
         Calendar calendar = new GregorianCalendar();
         calendar.setTime(startDate);

         // get the year, month, and date fields of the start date
         int year  = calendar.get(Calendar.YEAR );
         int month = calendar.get(Calendar.MONTH);
         int day   = calendar.get(Calendar.DATE );

         // set the end date's year, month, and day to that of the start date
         calendar.setTime(endDate);
         calendar.set(Calendar.YEAR , year);
         calendar.set(Calendar.MONTH, month);
         calendar.set(Calendar.DATE , day);

         // calculate the duration
         duration = calendar.getTime().getTime() - startDate.getTime();
      } else {
         duration = endDate.getTime() - startDate.getTime();
      }
   }

   public boolean isAddEventToCalendar() {
      return addEventToCalendar;
   }

   public void setAddEventToCalendar(boolean addEventToCalendar) {
      this.addEventToCalendar = addEventToCalendar;
   }

   public String getTimeZone() {
      return timeZone;
   }

   public void setTimeZone(String timeZone) {
      this.timeZone = timeZone;
   }

   public boolean isRecurring() {
      return recurring;
   }

   public void setRecurring(boolean recurring) {
      this.recurring = recurring;
   }

   /**
    * determine if two schedule objects are equal.
    */
   public boolean equals(Object object) {
      boolean equal = false;

      if (!(object instanceof Schedule)) {
         equal = false;
      } else {
         Schedule schedule = (Schedule)object;

         if (!schedule.getStartDate().equals   (this.getStartDate())        ||
             !schedule.getEndDate  ().equals   (this.getEndDate  ())        ||
             schedule.getTimeZone  ()        != this.getTimeZone()          ||
             schedule.isAddEventToCalendar() != this.isAddEventToCalendar() ||
             schedule.isRecurring()          != this.isRecurring()) {
            equal = false;
         } else if (schedule.isRecurring() && (
            schedule.getDaysOfWeek()[0] != this.getDaysOfWeek()[0] ||
            schedule.getDaysOfWeek()[1] != this.getDaysOfWeek()[1] ||
            schedule.getDaysOfWeek()[2] != this.getDaysOfWeek()[2] ||
            schedule.getDaysOfWeek()[3] != this.getDaysOfWeek()[3] ||
            schedule.getDaysOfWeek()[4] != this.getDaysOfWeek()[4] ||
            schedule.getDaysOfWeek()[5] != this.getDaysOfWeek()[5] ||
            schedule.getDaysOfWeek()[6] != this.getDaysOfWeek()[6])) {
            equal = false;
         } else {
            equal = true;
         }
      }
      return equal;
   }

   /**
    * return a string representation of the days of the week.
    */
   private String formatDaysOfWeek() {
      StringBuffer buffer = new StringBuffer();
      String[]     days   = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

      if (daysOfWeek[0] && daysOfWeek[1] && daysOfWeek[2] && daysOfWeek[3] && daysOfWeek[4] && daysOfWeek[5] && daysOfWeek[6]) {
         buffer.append("every day");
      } else {
         for(int i=0; i<7; ++i) {
            if (daysOfWeek[i]) {
               buffer.append(days[i]);
               buffer.append(" ");
            }
         }
      }
      return buffer.toString().trim();
   }

   /**
    * returns just days of the week formatted.
    */
   public String getShortDescription() {
      return formatDaysOfWeek();
   }

   /**
    * returns the days of the week formatted along with the time.
    */
   public String getMediumDescription() {
      StringBuffer     buffer        = new StringBuffer();
      SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm aa");
      SimpleDateFormat zoneFormatter = new SimpleDateFormat("hh:mm aa z");

      if (recurring) {
         buffer.append(formatDaysOfWeek()                         +
                       " from " + timeFormatter.format(startDate) +
                       " to "   + zoneFormatter.format(endDate  ));
      } else {
         buffer.append(formatDaysOfWeek()                         +
                       " from " + timeFormatter.format(startDate) +
                       " to "   + zoneFormatter.format(endDate  ));
      }
      return buffer.toString();
   }

   /**
    * returns the the days of the week, meeting time, and start and end dates formatted.
    */
   public String getLongDescription() {
      StringBuffer     buffer        = new StringBuffer();
      SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
      SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm aa");
      SimpleDateFormat zoneFormatter = new SimpleDateFormat("hh:mm aa z");

      if (recurring) {
         buffer.append(formatDaysOfWeek()                          +
                       " from "  + timeFormatter.format(startDate) +
                       " to "    + zoneFormatter.format(endDate  ) +
                       " from "  + dateFormatter.format(startDate) +
                       " until " + dateFormatter.format(endDate  ));
      } else {
         buffer.append(formatDaysOfWeek()                            +
                      ", " + dateFormatter.format(startDate) + ", "  +
                      " from " + timeFormatter.format(startDate)     +
                      " to "   + zoneFormatter.format(endDate  ));
      }
      return buffer.toString();
   }

   /**
    * returns a string representation of the schedule.
    */
   public String toString() {
      return getLongDescription();
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