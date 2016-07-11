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
package com.rsmart.sakai.common.web.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.NoSuchMessageException;




/**
 * displays a long number representing a duration in milliseconds as a string.
 * <p>
 * Date date1 = new Date();
 * Date date2 = new Date();
 * long duration = date2.getTime() - date1.getTime();
 *
 * use this tag to display the duration.
 * </p>
 */
public class DurationTag extends AbstractLocalizableTag {
   // logger
   protected final transient Log logger = LogFactory.getLog(getClass());

   // class members
   private static final long DURATION_ONE_SECOND = 1000L;                            // in ms
   private static final long DURATION_ONE_MINUTE = 1000L * 60L;
   private static final long DURATION_ONE_HOUR   = 1000L * 60L * 60L;
   private static final long DURATION_ONE_DAY    = 1000L * 60L * 60L * 24L;
   private static final long DURATION_ONE_WEEK   = 1000L * 60L * 60L * 24L * 7L;

   // data members
   private String week;                // localized versions of these strings
   private String weeks;
   private String day;
   private String days;
   private String hour;
   private String hours;
   private String minute;
   private String minutes;
   private String second;
   private String seconds;
   private String open;
   private String separator;

   // tag attributes
   private long duration;



   /**
    * default constructor.
    */
   public DurationTag() throws JspException, NoSuchMessageException {
      // set default values for these strings
      week      = "week";
      weeks     = "weeks";
      day       = "day";
      days      = "days";
      hour      = "hour";
      hours     = "hours";
      minute    = "minute";
      minutes   = "minutes";
      second    = "second";
      seconds   = "seconds";
      open      = "open";
      separator = ",";
   }

   /**
    * output the duration as a string.
    * <br/><br/>
    * @return int   whether to process the body of the tag.
    */
   protected int doStartTagInternal() throws Exception {
      StringBuffer buffer        = new StringBuffer();
      long         numWeeks      = 0L;
      long         numDays       = 0L;
      long         numHours      = 0L;
      long         numMinutes    = 0L;
      long         numSeconds    = 0L;
      boolean      needSeparator = false;

      // this should be called once during the constructor, but it causes an exception to be thrown when the tag is contructed.
      // for this reason, it was moved here.
      setLocalizedDurationStrings();

      if (duration == -1L) {
         buffer.append(open);
      } else if (duration == 0L) {
         buffer.append("0 " + seconds);
      } else if (duration > 0) {
         // calculate the number of weeks in the duration
         numWeeks = duration / DURATION_ONE_WEEK;
         if (numWeeks > 0) {
            buffer.append("" + numWeeks + " " + (numWeeks==1 ? week : weeks));
            duration = duration % DURATION_ONE_WEEK;
            needSeparator = true;
         }
         // calculate the number of days in the duration
         numDays = duration / DURATION_ONE_DAY;
         if (numDays > 0) {
            buffer.append((needSeparator ? separator + " " : "") + numDays + " " + (numDays==1 ? day : days));
            duration = duration % DURATION_ONE_DAY;
            needSeparator = true;
         }
         // calculate the number of hours in the duration
         numHours = duration / DURATION_ONE_HOUR;
         if (numHours > 0) {
            buffer.append((needSeparator ? separator + " " : "") + numHours + " " + (numHours==1 ? hour : hours));
            duration = duration % DURATION_ONE_HOUR;
            needSeparator = true;
         }
         // calculate the number of minutes in the duration
         numMinutes = duration / DURATION_ONE_MINUTE;
         if (numMinutes > 0) {
            buffer.append((needSeparator ? separator + " "  : "") + numMinutes + " " + (numMinutes==1 ? minute : minutes));
            duration = duration % DURATION_ONE_MINUTE;
            needSeparator = true;
         }
         // calculate the number of seconds in the duration
         numSeconds = duration % DURATION_ONE_SECOND;
         if (numSeconds > 0) {
            buffer.append((needSeparator ? separator + " "  : "") + numMinutes + " " + (numMinutes==1 ? minute : minutes));
         }
      } else {
         throw new JspException("Invalid value: " + duration);
      }

      try {
         buffer.toString();
         pageContext.getOut().write(buffer.toString());
      } catch (IOException e) {
         logger.error("", e);
         throw new JspException(e);
      }
      return EVAL_BODY_INCLUDE;
   }

   /**
    * set the values of the durations for the given locale.
    */
    private void setLocalizedDurationStrings() throws JspException, NoSuchMessageException {
      week      = resolveMessage("duration_week"     );
      weeks     = resolveMessage("duration_weeks"    );
      day       = resolveMessage("duration_day"      );
      days      = resolveMessage("duration_days"     );
      hour      = resolveMessage("duration_hour"     );
      hours     = resolveMessage("duration_hours"    );
      minute    = resolveMessage("duration_minute"   );
      minutes   = resolveMessage("duration_minutes"  );
      second    = resolveMessage("duration_second"   );
      seconds   = resolveMessage("duration_seconds"  );
      open      = resolveMessage("duration_open"     );
      separator = resolveMessage("duration_separator");
   }

   /**
    * called by the jsp container to set the "value" tag attribute.
    */
   public void setValue(long value) {
      duration = value;
   }
}
