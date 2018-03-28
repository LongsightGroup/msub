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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RsmartDateSelectTag extends AbstractLocalizableTag {
   protected final transient Log logger = LogFactory.getLog(getClass());

   private String yearSelectId;
   private String daySelectId;
   private String monthSelectId;
   private String earliestYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 5);
   private String latestYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 5);
   private Date selected;
   private boolean showTime;
   private String hourSelectId;
   private String minuteSelectId;
   private int minuteInterval = 15;
   private boolean showAmPm = true;
   private boolean defaultNow = true;
   private boolean hideDate;

   public static final String AM_KEY = "am";
   public static final String PM_KEY = "pm";
   public static final String JAN_KEY = "month_short_jan";
   public static final String FEB_KEY = "month_short_feb";
   public static final String MAR_KEY = "month_short_mar";
   public static final String APR_KEY = "month_short_apr";
   public static final String MAY_KEY = "month_short_may";
   public static final String JUN_KEY = "month_short_jun";
   public static final String JUL_KEY = "month_short_jul";
   public static final String AUG_KEY = "month_short_aug";
   public static final String SEP_KEY = "month_short_sep";
   public static final String OCT_KEY = "month_short_oct";
   public static final String NOV_KEY = "month_short_nov";
   public static final String DEC_KEY = "month_short_dec";

   public String dateSpanId = "RsmartDateSelectTag.date";


 protected int doStartTagInternal() throws Exception {
    StringBuffer buffer = new StringBuffer();

      try {
         // set date to now (rounding minutes down to zero)
         if (selected == null && defaultNow) {
            Calendar now = new GregorianCalendar();
            now.setTime(new Date());
            now.set(Calendar.MINUTE, 0);
            selected = now.getTime();
         }

         if (!hideDate){
            buffer.append("<span id=\"" + dateSpanId + "\" name=\"" + dateSpanId + "\">");
            buffer.append("<select name=\"" + getMonthSelectId() + "\" id=\"" + getMonthSelectId() + "\" onchange=\"blur();\">\n");
            buffer.append("<option value=\"\">\n");
            for (int i=1; i<13; i++){
               buffer.append("<option value=\"" + i + "\"");
               if (getMonthSelected() == i){
                  buffer.append("selected=\"selected\"");
               }
               buffer.append(">" + getMonthName(i) + "</option>\n");
            }
            buffer.append("</select>");

            buffer.append("<select name=\"" + getDaySelectId() + "\" id=\"" + getDaySelectId() + "\" onchange=\"blur();\">\n");
            buffer.append("<option value=\"\">\n");
            for (int i=1; i<=31; i++){
               buffer.append("<option value=\"" + i + "\"");
               if (getDaySelected() == i){
                  buffer.append("selected=\"selected\"");
               }
               buffer.append(">" + i + "</option>\n");
            }
            buffer.append("</select>");

            buffer.append("<select name=\"" + getYearSelectId() + "\" id=\"" + getYearSelectId() + "\" onchange=\"blur();\">\n");
            buffer.append("<option value=\"\">\n");
            for (int i=Integer.parseInt(earliestYear); i<Integer.parseInt(latestYear)+1; i++){
               buffer.append("<option value=\"" + i + "\"");
               if (getYearSelected() == i){
                  buffer.append("selected=\"selected\"");
               }
               buffer.append(">" + i + "</option>\n");
            }

            buffer.append("</select>");
            buffer.append("</span>");

            pageContext.getOut().write(buffer.toString());

            //spit out popup widget before time
            pageContext.getOut().write("<script type=\"text/javascript\" src=\"/library/calendar/sakai-calendar.js\"></script>\n" +
                  "<script type=\"text/javascript\" src=\"/osp-common-tool/js/eport.js\"></script>\n" +
                  "<script type=\"text/javascript\">osp_dateselectionwidgetpopup('" +
                  getYearSelectId() + "', '" + getMonthSelectId() + "', '" + getDaySelectId() + "');</script>\n");

         }

         if (showTime) {
            buffer = new StringBuffer();

            String functionName = "setMinutes_" + getMinuteSelectId().replaceAll("\\.","_");
            buffer.append("<script>\nfunction " + functionName + "(elementId){\nvar interval = " + minuteInterval+ ";\nvar intervalRatio = 60/interval; \nvar selected = ospGetElementById('" + getHourSelectId() + "');\nvar minutes = ospGetElementById('" + getMinuteSelectId() + "'); \nminutes.value = ((selected.selectedIndex - 1 ) % intervalRatio ) * interval; \n} \n</script>\n");
            buffer.append("<select name=\"" + getHourSelectId() + "\" id=\"" + getHourSelectId() + "\" onchange=\"blur();" + functionName + "('" + getMinuteSelectId() + "');\">\n");
            buffer.append("<option value=\"\">\n");
            for (int currentHour=1; currentHour<25; currentHour++){
               for (int currentMinute=0; currentMinute<60; currentMinute++){
                  buffer.append("<option value=\"" + currentHour + "\"");
                  String time;
                  String minute = String.valueOf(currentMinute);
                  if (currentMinute < 10) {
                     minute = "0" + minute;
                  }

                  if (!showAmPm) {
                     time = currentHour + ":" + minute;
                  } else if (currentHour < 12) {
                     time = currentHour + ":" + minute +" " + resolveMessage(AM_KEY);
                  } else if (currentHour == 12) {
                     time = "12:" + minute +" "+resolveMessage(PM_KEY);
                  } else {
                     time = String.valueOf(currentHour-12) + ":" + minute + " " + resolveMessage(PM_KEY);
                  }

                  if (getHourSelected() == currentHour && getMinuteSelected() == currentMinute){
                     buffer.append("selected=\"selected\"");
                  }


                  buffer.append(">" + time + "</option>\n");
                  currentMinute += minuteInterval-1;
               }
            }
            buffer.append("</select>\n");
            buffer.append("<input type=\"hidden\" id=\"" + getMinuteSelectId() +  "\" name=\"" + getMinuteSelectId() + "\" value=\"" + getMinuteSelected() + "\">\n");
            pageContext.getOut().write(buffer.toString());
         }


      } catch (IOException e) {
         logger.error("", e);
         throw new JspException(e);
      }

      return EVAL_BODY_INCLUDE;
   }

   public boolean isDefaultNow() {
      return defaultNow;
   }

   public void setDefaultNow(boolean defaultNow) {
      this.defaultNow = defaultNow;
   }

   public boolean getShowAmPm() {
      return showAmPm;
   }

   public void setShowAmPm(boolean showAmPm) {
      this.showAmPm = showAmPm;
   }

   public int getMinuteInterval() {
      return minuteInterval;
   }

   public void setMinuteInterval(int minuteInterval) {
      if (60 % minuteInterval != 0) {
         throw new RuntimeException(minuteInterval + " is not a factor of 60");
      }
      this.minuteInterval = minuteInterval;
   }

   public String getHourSelectId() {
      return hourSelectId;
   }

   public void setHourSelectId(String hourSelectId) {
      this.hourSelectId = hourSelectId;
   }

   public String getMinuteSelectId() {
      return minuteSelectId;
   }

   public void setMinuteSelectId(String minuteSelectId) {
      this.minuteSelectId = minuteSelectId;
   }


   protected String getMonthName(int month) throws JspException {

      switch (month) {
         case 1 : return resolveMessage(JAN_KEY);
         case 2 : return resolveMessage(FEB_KEY);
         case 3 : return resolveMessage(MAR_KEY);
         case 4 : return resolveMessage(APR_KEY);
         case 5 : return resolveMessage(MAY_KEY);
         case 6 : return resolveMessage(JUN_KEY);
         case 7 : return resolveMessage(JUL_KEY);
         case 8 : return resolveMessage(AUG_KEY);
         case 9 : return resolveMessage(SEP_KEY);
         case 10 : return resolveMessage(OCT_KEY);
         case 11 : return resolveMessage(NOV_KEY);
         case 12 : return resolveMessage(DEC_KEY);
      }
      throw new JspException(month + " is not a valid month");
   }


   protected Calendar getCalendar(){
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(selected);
      return calendar;
   }

   protected int getMonthSelected(){
      if (selected == null) return -1;
      return getCalendar().get(Calendar.MONTH)+1; // Calendar indexes months starting at 0
   }

   protected int getHourSelected(){
      if (selected == null) return -1;
      return getCalendar().get(Calendar.HOUR_OF_DAY);
   }

   protected int getMinuteSelected(){
      if (selected == null) return -1;
      return getCalendar().get(Calendar.MINUTE);
   }

   protected int getYearSelected(){
      if (selected == null) return -1;
      return getCalendar().get(Calendar.YEAR);
   }

   protected int getDaySelected(){
      if (selected == null) return -1;
      return getCalendar().get(Calendar.DAY_OF_MONTH);
   }

   public String getEarliestYear() {
      return earliestYear;
   }

   public void setEarliestYear(String earliestYear) {
      this.earliestYear = earliestYear;
   }

   public String getLatestYear() {
      return latestYear;
   }

   public void setLatestYear(String lastestYear) {
      this.latestYear = lastestYear;
   }

   public Date getSelected() {
      return selected;
   }

   public void setSelected(Date selected) {
      this.selected = selected;
   }

   public boolean isShowTime() {
      return showTime;
   }

   public void setShowTime(boolean showTime) {
      this.showTime = showTime;
   }

   public String getYearSelectId() {
      return yearSelectId;
   }

   public void setYearSelectId(String yearSelectId) {
      this.yearSelectId = yearSelectId;
   }

   public String getDaySelectId() {
      return daySelectId;
   }

   public void setDaySelectId(String daySelectId) {
      this.daySelectId = daySelectId;
   }

   public String getMonthSelectId() {
      return monthSelectId;
   }

   public void setMonthSelectId(String monthSelectId) {
      this.monthSelectId = monthSelectId;
   }

   public boolean isHideDate() {
      return hideDate;
   }


   public String getDateSpanId() {
      return dateSpanId;
   }

   public void setDateSpanId(String dateSpanId) {
      this.dateSpanId = dateSpanId;
   }

   public void setHideDate(boolean hideDate) {
      this.hideDate = hideDate;
   }
}
