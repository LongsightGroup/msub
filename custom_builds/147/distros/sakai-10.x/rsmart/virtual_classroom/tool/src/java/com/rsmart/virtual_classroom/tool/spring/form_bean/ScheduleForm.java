/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.tool.spring.form_bean;



import org.sakaiproject.metaobj.shared.model.DateBean;
import org.springframework.beans.BeanUtils;

import java.util.Date;

import com.rsmart.virtual_classroom.model.Schedule;


/**
 * This class is used to construct beans which can then be used by the spring web framework to edit a schedule.
 * The only difference between this web form bean and the model bean is that the start and end date fields have a
 * corresponding date bean which is used by the &lt;rc:dateSelect&gt; tag defined in the rsmart-common.tld.
 */
public class ScheduleForm extends Schedule {
   // data members
   private DateBean startDateBean;
   private DateBean endDateBean;


   /**
    * default constructor.
    */
   public ScheduleForm() {
      startDateBean = new DateBean();
      endDateBean   = new DateBean();
   }

   /**
    * constructor.
    */
   public ScheduleForm(Schedule schedule) {
      BeanUtils.copyProperties(schedule, this);
   }

   /**
    * returns the date bean corresponding to the model's start date.
    */
   public DateBean getStartDateBean() {
      return startDateBean;
   }

   /**
    * sets the date bean corresponding to the model's start date.
    */
   public void setStartDate(Date meetingStartDate) {
      this.startDateBean = new DateBean(meetingStartDate);
      super.setStartDate(meetingStartDate);
   }

   /**
    * sets the date bean corresponding to the model's start date.
    */
   public void setStartDateBean(DateBean meetingStartDateBean) {
      this.startDateBean = meetingStartDateBean;
   }

   /**
    * sets the date bean corresponding to the model's end date.
    */
   public void setEndDate(Date meetingEndDate) {
      this.endDateBean = new DateBean(meetingEndDate);
      super.setEndDate(meetingEndDate);
   }

   /**
    * sets the date bean corresponding to the model's end date.
    */
   public void setEndDateBean(DateBean meetingEndDateBean) {
      this.endDateBean = meetingEndDateBean;
   }

   /**
    * returns the date bean corresponding to the model's end date.
    */
   public DateBean getEndDateBean() {
      return endDateBean;
   }
}
