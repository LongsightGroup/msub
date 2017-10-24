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
package com.rsmart.virtual_classroom.util;

import com.rsmart.virtual_classroom.model.Schedule;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 *
 */
public class ScheduleHelper {
   public static final String DATE_FORMAT_MIDDLE      = "date_format_middle";
   public static final String DATE_FORMAT_SHORT       = "date_format_short";
   public static final String DATE_FORM_TIME          = "date_form_time";
   public static final String DATE_FORM_TIME_TIMEZONE = "date_form_time_timezone";
   public static final String SCHEDULE                = "schedule";
   public static final String SCHEDULE_EFFECTIVE      = "schedule_effective";
   public static final String SCHEDULE_EVERYDAY       = "schedule_everyday";
   public static final String SCHEDULE_EVERYWEEKDAY   = "schedule_everyweekday";
   public static final String SCHEDULE_MEETS          = "schedule_meets";
   public static final String SCHEDULE_ON             = "schedule_on";
   public static final String MONDAY_SHORT            = "schedule_day_short_monday";
   public static final String TUESDAY_SHORT           = "schedule_day_short_tuesday";
   public static final String WEDNESDAY_SHORT         = "schedule_day_short_wednesday";
   public static final String THURSDAY_SHORT          = "schedule_day_short_thursday";
   public static final String FRIDAY_SHORT            = "schedule_day_short_friday";
   public static final String SATURDAY_SHORT          = "schedule_day_short_saturday";
   public static final String SUNDAY_SHORT            = "schedule_day_short_sunday";


   private MessageSource messageSource;


   public MessageSource getMessageSource() {
      return messageSource;
   }

   public void setMessageSource(MessageSource messageSource) {
      this.messageSource = messageSource;
   }

   public String getScheduleDisplay(Schedule schedule, Locale locale) {
      return getScheduleDisplay(schedule, locale, false);
   }

   public String getScheduleDisplay(Schedule schedule, boolean includeMeetsPhrase) {
      return getScheduleDisplay(schedule, LocaleContextHolder.getLocale(), includeMeetsPhrase);
   }


   /**
    * Format the schedule into a human readable localized string.  This method does not include the "Meets" part of the phrase.
    *
    * @param schedule
    * @return formatted string
    */
    public String getScheduleDisplay(Schedule schedule) {
      return getScheduleDisplay(schedule, false);
   }

   /**
    * Format the schedule into a human readable localized string.
    *
    * @param schedule
    * @param includeMeetsPhrase  -  prepend "Meets " or not
    * @return
    *
    */
   public String getScheduleDisplay(Schedule schedule, Locale locale, boolean includeMeetsPhrase) {
      String recurringPattern = "";
      SimpleDateFormat dateFormatter = new SimpleDateFormat(messageSource.getMessage(DATE_FORMAT_SHORT, null, locale));
      SimpleDateFormat timeFormatter = new SimpleDateFormat(messageSource.getMessage(DATE_FORM_TIME, null, locale));
      SimpleDateFormat timeFormatterTimezone = new SimpleDateFormat(messageSource.getMessage(DATE_FORM_TIME_TIMEZONE, null, locale));

      String startDate = dateFormatter.format(schedule.getStartDate());
      String endDate = dateFormatter.format(schedule.getEndDate());
      String startTime = timeFormatter.format(schedule.getStartDate());
      String endTime = timeFormatterTimezone.format(schedule.getEndDate());
      Object[] args;
      if (schedule.isRecurring()) {
         if (schedule.getDaysOfWeek()[0] == true
               && schedule.getDaysOfWeek()[1] == true
               && schedule.getDaysOfWeek()[2] == true
               && schedule.getDaysOfWeek()[3] == true
               && schedule.getDaysOfWeek()[4] == true
               && schedule.getDaysOfWeek()[5] == true
               && schedule.getDaysOfWeek()[6] == true) {
            recurringPattern = messageSource.getMessage(SCHEDULE_EVERYDAY, null, locale);
         } else if (schedule.getDaysOfWeek()[0] == false
               && schedule.getDaysOfWeek()[1] == true
               && schedule.getDaysOfWeek()[2] == true
               && schedule.getDaysOfWeek()[3] == true
               && schedule.getDaysOfWeek()[4] == true
               && schedule.getDaysOfWeek()[5] == true
               && schedule.getDaysOfWeek()[6] == false) {
            recurringPattern = messageSource.getMessage(SCHEDULE_EVERYWEEKDAY, null, locale);
         } else {
            if (schedule.getDaysOfWeek()[0]) {
               recurringPattern += " " +messageSource.getMessage(SUNDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[1]) {
               recurringPattern += " " + messageSource.getMessage(MONDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[2]) {
               recurringPattern += " " + messageSource.getMessage(TUESDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[3]) {
               recurringPattern += " " + messageSource.getMessage(WEDNESDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[4]) {
               recurringPattern += " " + messageSource.getMessage(THURSDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[5]) {
               recurringPattern += " " + messageSource.getMessage(FRIDAY_SHORT, null, locale);
            }
            if (schedule.getDaysOfWeek()[6]) {
               recurringPattern += " " + messageSource.getMessage(SATURDAY_SHORT, null, locale);
            }
         }
         String effective = messageSource.getMessage(SCHEDULE_EFFECTIVE, new Object[]{startDate, endDate}, locale);
         args = new Object[]{recurringPattern, startTime, endTime, effective};
      } else {
         String scheduleOn = messageSource.getMessage(SCHEDULE_ON, new Object[]{startDate}, locale);
         args = new Object[]{recurringPattern, startTime, endTime, scheduleOn};
      }

      if (includeMeetsPhrase) {
         return messageSource.getMessage(SCHEDULE_MEETS, args, locale) + " " + messageSource.getMessage(SCHEDULE, args, locale);
      }
      return messageSource.getMessage(SCHEDULE, args, locale);
   }

}