/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.tool.spring.controller;

import com.rsmart.sakai.common.web.springframework.AbstractCancelableController;
import com.rsmart.virtual_classroom.intf.VirtualClassroomService;
import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;
import com.rsmart.virtual_classroom.tool.spring.form_bean.ScheduleForm;
import com.rsmart.virtual_classroom.tool.spring.form_bean.VirtualClassroomSessionGroupForm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.CalendarValidator;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.sakaiproject.component.cover.ServerConfigurationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;


/**
 *
 */
public class EditVirtualClassroomSessionController extends AbstractCancelableController {
   // logger
   protected final Log logger = LogFactory.getLog(getClass());

   // data members
   private VirtualClassroomService virtualClassroomService;



   /**
    * adjusts a date and time to the specified time zone.
    */
   protected Date adjustToTimeZone(Date date, String timeZone) {
      TimeZone tz       = TimeZone.getTimeZone(timeZone);
      Calendar calendar = Calendar.getInstance();

      calendar.setTime(date);
      CalendarValidator.adjustToTimeZone(calendar, tz);
      return calendar.getTime();
   }

   /**
    * determines if we are editing a new virtual classroom session or an existing one.
    * If we are editing a new session, a new blank virtual classroom session form bean is created and returned.
    * Otherwise, the existing virtual classroom session to be edited is retrieved and a form bean instance is populated with its values.
    */
   protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm = null;
      String                           id                               = httpServletRequest.getParameter("id");

      if (id == null || id.trim().length()==0) {
         virtualClassroomSessionGroupForm = new VirtualClassroomSessionGroupForm();
         virtualClassroomSessionGroupForm.setSiteId(virtualClassroomService.getCurrentSite().getId());

         // initialize private/public
          virtualClassroomSessionGroupForm.setIsPrivate(virtualClassroomService.isPrivateDefault());

         // initialize the capacity
         virtualClassroomSessionGroupForm.setCapacity(virtualClassroomService.getDefaultCapacity());

         // initialize the time zone to the one the machine this code is running on
         virtualClassroomSessionGroupForm.getSchedule().setTimeZone(TimeZone.getDefault().getID());
      } else {
         virtualClassroomSessionGroupForm = new VirtualClassroomSessionGroupForm(virtualClassroomService.getVirtualClassroomSessionGroup(id));
      }
      return virtualClassroomSessionGroupForm;
   }

   /**
    * called by the spring mvc framework to copy the form values from the date beans to the dates.
    */
   protected void onBind(HttpServletRequest request, Object command, BindException errors) throws Exception {
      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm = (VirtualClassroomSessionGroupForm)command;
      ScheduleForm                     scheduleForm                     = (ScheduleForm)(virtualClassroomSessionGroupForm.getSchedule());

      // set dates from those in dateBeans
      scheduleForm.setStartDate(scheduleForm.getStartDateBean().getDate());
      scheduleForm.setEndDate  (scheduleForm.getEndDateBean  ().getDate());
   }

   protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm = (VirtualClassroomSessionGroupForm)command;
      ScheduleForm                     scheduleForm                     = (ScheduleForm)(virtualClassroomSessionGroupForm.getSchedule());

      // adjust dates using timezone
      scheduleForm.setStartDate(adjustToTimeZone(scheduleForm.getStartDate(), scheduleForm.getTimeZone()));
      scheduleForm.setEndDate  (adjustToTimeZone(scheduleForm.getEndDate  (), scheduleForm.getTimeZone()));
   }

   protected boolean isOccurrenceSwitch(HttpServletRequest request) {
      return (request.getParameter("switchOccurrence") != null && request.getParameter("switchOccurrence").equalsIgnoreCase("true"));
   }

   protected boolean suppressValidation(HttpServletRequest request) {
      return (isOccurrenceSwitch(request) ? true : super.suppressValidation(request));
   }

   /**
    *
    */
   protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
      // when done, redirect to the main page and specify the "command" http parameter
      ModelAndView modelAndView = null;

      if (isCancel(request)) {
         modelAndView = new ModelAndView(getSuccessView(), "command", "list");
      } else if (isOccurrenceSwitch(request)) {
         modelAndView = new ModelAndView("edit_virtual_classroom_session_group", "command", command);
      } else {
         try {
            doSubmitAction(command);
            modelAndView = new ModelAndView(getSuccessView(), "command", "list");
         } catch (Exception ex) {
            modelAndView = new ModelAndView(getSuccessView(), "command", "list");
            Map model = modelAndView.getModel();
            model.put("error", ex.getMessage());
            logger.warn(ex);
         }
      }
      return modelAndView;
   }

   /**
    * called by the spring mvc framework after the user has finished editing the virtual classroom session and has submitted the form.
    */
   protected void doSubmitAction(Object command) throws Exception {
      logger.warn("doSubmitAction()");

      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm = (VirtualClassroomSessionGroupForm)command;
      VirtualClassroomSessionGroup     virtualClassroomSessionGroup     = virtualClassroomSessionGroupForm.getModel();

      virtualClassroomSessionGroup.setSiteId(virtualClassroomService.getCurrentSite().getId());
      virtualClassroomSessionGroup.getSchedule().calculateDuration();
      virtualClassroomSessionGroup.getSchedule().setDaysOfWeekStringFromArray();

      if (virtualClassroomSessionGroupForm.isUpdating())
         virtualClassroomService.updateVirtualClassroomSessionGroup(virtualClassroomSessionGroup);
      else
         virtualClassroomService.createVirtualClassroomSessionGroup(virtualClassroomSessionGroup);
   }

   /**
    * method called by spring framework to inject the virtual classroom service using IoC.
    */
   public void setVirtualClassroomService(VirtualClassroomService virtualClassroomService) {
      this.virtualClassroomService = virtualClassroomService;
   }
}
