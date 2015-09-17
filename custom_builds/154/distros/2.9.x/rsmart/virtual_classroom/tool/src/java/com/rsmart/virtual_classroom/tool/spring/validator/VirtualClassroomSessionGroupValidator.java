/***********************************************************************************
 * Copyright (c) 2006 the rSmart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.tool.spring.validator;

import com.rsmart.virtual_classroom.intf.VirtualClassroomService;
import com.rsmart.virtual_classroom.tool.spring.form_bean.VirtualClassroomSessionGroupForm;
import com.rsmart.virtual_classroom.model.Schedule;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.metaobj.shared.model.DateBean;
import org.sakaiproject.user.api.User;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



/*
 * spring mvc validator for the edit_virtual_classroom_session.jsp page.
 */
public class VirtualClassroomSessionGroupValidator implements Validator {
   // data members
   private VirtualClassroomService virtualClassroomService;




   /**
    *  Returns whether or not this validator can validate objects of the given class.
    */
   public boolean supports(Class clazz) {
      return VirtualClassroomSessionGroupForm.class.isAssignableFrom(clazz);
   }

   /**
    * check the field values in the bean.
    */
   public void validate(Object object, Errors errors) {
      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm = (VirtualClassroomSessionGroupForm)object;
      String                           name                             = virtualClassroomSessionGroupForm.getName();

      // validate the name field
      if (name == null || name.trim().length() == 0) {
         errors.rejectValue("name", "error.required");
      } else {
         // these characters are invalid for the name on the elluminate live server
         char[] invalidChars = {'<', '>', '&', '\\', '/', '\"', '\'', '%', '?', '#'};

         for(int i=0; i<invalidChars.length; ++i) {
            if (name.indexOf(invalidChars[i]) != -1) {
               errors.rejectValue("name", "error_validation.invalid_name");
               break;
            }
         }
      }

      Schedule schedule = virtualClassroomSessionGroupForm.getSchedule();

      // validate the capacity
      if (virtualClassroomSessionGroupForm.getCapacity() <= 0)
         errors.rejectValue("capacity", "error.required");

       // validate the maxTalkers
       if (virtualClassroomSessionGroupForm.getMaxTalkers() <= 0)
          errors.rejectValue("maxTalkers", "error.required");
       
       if (virtualClassroomSessionGroupForm.getMaxTalkers() > 6)
          errors.rejectValue("maxTalkers", "error_validation.maxTalkersLimit");


      try {
         if (!virtualClassroomService.hasSufficientCapacity(virtualClassroomService.getCurrentSite().getId(), schedule.getStartDate(), schedule.getEndDate(), virtualClassroomSessionGroupForm.getCapacity()))
            errors.rejectValue("capacity", "error_validation.concurrent_user_capcity_exceded",
                               new Object[] {new Integer(virtualClassroomService.getMaxNumConcurrentUsers())}, "error_validation.concurrent_user_capcity_exceded default message");
      } catch (Exception ex) {
         // nothing that we can do here
      }

      // validate the start and end dates
      if (schedule.getStartDate() == null)
         errors.rejectValue("schedule.startDate", "error.required");

      if (schedule.getEndDate() == null)
         errors.rejectValue("schedule.endDate", "error.required");

      if (schedule.getStartDate() != null && schedule.getEndDate() != null && schedule.getStartDate().getTime() >= schedule.getEndDate().getTime())
         errors.rejectValue("schedule.endDate", "error_validation.invalid_end_date");

      // validate the start and end dates
      if (schedule.isRecurring  ()    &&
         !schedule.getDaysOfWeek()[0] &&
         !schedule.getDaysOfWeek()[1] &&
         !schedule.getDaysOfWeek()[2] &&
         !schedule.getDaysOfWeek()[3] &&
         !schedule.getDaysOfWeek()[4] &&
         !schedule.getDaysOfWeek()[5] &&
         !schedule.getDaysOfWeek()[6])
         errors.rejectValue("schedule.daysOfWeek", "error.required");
   }

   /**
    * retrieves the virtual classroom service injected by spring.
    */
   public VirtualClassroomService getVirtualClassroomService() {
       return virtualClassroomService;
   }

   /**
    * spring uses IoC to inject the virtual classroom service.
    */
   public void setVirtualClassroomService(VirtualClassroomService virtualClassroomService) {
       this.virtualClassroomService = virtualClassroomService;
   }
}
