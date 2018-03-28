/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.tool.spring.controller;

import com.rsmart.virtual_classroom.util.ScheduleHelper;

import com.rsmart.sakai.common.web.listfilter.ViewListController;
import com.rsmart.virtual_classroom.intf.VirtualClassroomService;
import com.rsmart.virtual_classroom.model.AuthorizationException;
import com.rsmart.virtual_classroom.model.VirtualClassroomException;
import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;
import com.rsmart.virtual_classroom.model.Schedule;
import com.rsmart.virtual_classroom.tool.spring.form_bean.VirtualClassroomSessionGroupForm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.exception.IdUnusedException;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




/**
 * this spring mvc controller performs two functions:
 * <ul>
 *    <li>retrieves the list of virtual classroom sessions</li>
 *    <li>deletes the selected virtual classroom sessions</li>
 * </ul>
 */
public class ListVirtualClassroomSessionsController extends ViewListController {

   // logger
   protected final Log logger = LogFactory.getLog(getClass());

   // filters
   private static final String FILTER_VC_SESSIONS_AND_RECORDINGS = "filter_virtual_classroom_sessions_and_recordings";
   private static final String FILTER_VC_SESSIONS                = "filter_virtual_classroom_sessions";
   private static final String FILTER_VC_SESSIONS_CURRENT        = "filter_virtual_classroom_sessions_current";
   private static final String FILTER_VC_SESSIONS_FUTURE         = "filter_virtual_classroom_sessions_future";
   private static final String FILTER_VC_SESSIONS_PAST           = "filter_virtual_classroom_sessions_past";
   private static final String FILTER_VC_RECORDINGS              = "filter_virtual_classroom_recordings";

   // pre-defined dates
   private static final Date   EARLIEST_DATE             = new Date(0L);                                           // jan 1, 1970
   private static final Date   BIGGEST_FUTURE_DATE       = new Date(System.currentTimeMillis() + 1576800000000L);  // 50 years in the future -  I'll have retired by then, so this date won't matter

   private ScheduleHelper scheduleHelper;

   // services injected by the spring framework
   private SecurityService         securityService;
   private VirtualClassroomService virtualClassroomService;

   // resource bundle injected by the spring framework
   private ResourceBundleMessageSource resourceBundle;




   /**
    * retrieve a list of all virtual classroom sessions from the database.
    * convert them to virtual classroom session forms and return them to the next jsp page.
    */
   public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
      String command = request.getParameter("command");
      Locale locale  = LocaleContextHolder.getLocale();
      String error   = null;

      try {
         if (command == null) {
            // the user will be retrieving a list of virtual classroom session groups
         } else if (command != null && command.equals("delete")) {
            int pageSize = Integer.valueOf(request.getParameter("pageSize")).intValue();

            for(int i=0; i<pageSize; ++i) {
               String param = request.getParameter("check"+i);
               if (param != null) {
                  String virtualClassroomSessionGroupId = param;
                  virtualClassroomService.deleteVirtualClassroomSessionGroup(virtualClassroomSessionGroupId);
               }
            }
         } else if (command != null && command.equals("ping_vc_server")) {
            if (virtualClassroomService.pingVirtualClassroomServer()) {
                error = "Virtual Classroom Server is up and running.";
            } else {
                error = "Virtual Classroom Server can not be successfully reached.";                                                
            }
         } else if (command != null && command.equals("check_server_time_zones")) {
            String vcTimeZone    = virtualClassroomService.getServerTimeZone();
            String sakaiTimeZone = (new GregorianCalendar()).getTimeZone().getDisplayName();
            error = "Virtual Classroom Server time zone: " + vcTimeZone + "    Sakai Server Time Zone: " + sakaiTimeZone;
            error = resourceBundle.getMessage("message_time_zone_check", new Object[]{vcTimeZone, sakaiTimeZone}, locale);
         } else if (command != null && command.equals("join")) {
            String virtualClassroomSessionId = request.getParameter("virtual_classroom_session_id");
            String url                       = virtualClassroomService.getJoinUrl(virtualClassroomSessionId);
            response.setHeader( "Content-Disposition", "attachment; filename=\"meeting.jnlp\"" );            
            response.setContentType("application/x-java-jnlp-file");
            response.getOutputStream().write(url.getBytes());            
            return null;
         } else if (command != null && command.equals("view")) {
            long   elluminateId          = Long.valueOf(request.getParameter("elluminate_id")).longValue();
            String elluminateRecordingId = request.getParameter("elluminate_recording_id");
            String url                   = virtualClassroomService.getViewUrl(elluminateId, elluminateRecordingId);
            response.setHeader( "Content-Disposition", "attachment; filename=\"meeting.jnlp\"" );
            response.setContentType("application/x-java-jnlp-file");
            response.getOutputStream().write(url.getBytes());
            return null;
         } else if (command != null && command.equals("list")) {
            // do nothing.  just fall through to the code below
         } else {
            throw new VirtualClassroomException("unknown command: " + command);
         }
      } catch (Exception ex) {
         logger.error(ex);
         // capture the error so that it can be added to the model and displayed on the page
         error = ex.getMessage();
      }

      // if we get here, then we are getting a list of virtual classroom session groups
      // this will be done by calling the base class's handleRequestInternal() method.
      Map          model        = new HashMap();
      ModelAndView modelandView = new ModelAndView(getSuccessView(), model);
      try {
         modelandView = super.handleRequestInternal(request, response);
      } catch (AuthorizationException ex) {
         // don't do anything.  it'll be handled in the jsp page.
      } catch (Exception ex) {
         // capture the error so that it can be added to the model and displayed on the page
         error = ex.getMessage();
      }
      model = modelandView.getModel();

      // add the error to the model
      if (error != null)
         model.put("error", error);

      // add the site to the model which is needed by the 'Permissions' hyperlink
      model.put("site"        , getCurrentSite());
      model.put("isMaintainer", new Boolean(securityService.isSuperUser()));

      // add the sakai tool to the model which is needed by the 'Permissions' hyperlink title attribute.
      String currentToolId = ToolManager.getCurrentPlacement().getId();
      model.put("tool", SiteService.findTool(currentToolId));

      // store the virtual classroom server's grace period value in the request
      model.put("grace_period", new Integer(virtualClassroomService.getGracePeriod()));

      return modelandView;
   }

   /**
    * retrieve the current list of virtual classroom session groups.
    * <br/><br/>
    * @return a list of virtual classroom session group form objects.
    * <br/><br/>
    * @param httpServletRequest
    * <br/><br/>
    * @return the list of virtual classroom session groups.
    */
   public List getList(HttpServletRequest httpServletRequest) throws AuthorizationException {
      VirtualClassroomSessionGroup     virtualClassroomSessionGroup      = null;
      VirtualClassroomSessionGroupForm virtualClassroomSessionGroupForm  = null;
      List                             virtualClassroomSessionGroups     = null;
      List                             virtualClassroomSessionGroupForms = new ArrayList();
      String                           filterName                        = getFilterName(httpServletRequest);
      int                              sortOrder                         = isAscendingOrder() ? VirtualClassroomService.SORT_ORDER_ASCENDING : VirtualClassroomService.SORT_ORDER_DESCENDING;
      int                              sortField                         = getCurrentSortCol().equals("join") ? VirtualClassroomService.SORT_FIELD_JOIN : (getCurrentSortCol().equals("subject") ? VirtualClassroomService.SORT_FIELD_NAME : VirtualClassroomService.SORT_FIELD_START_DATE);
      String                           siteId                            = getCurrentSite().getId();

      // get the list of virtual classroom sessions from the service
      try {
          if (filterName == null) {
              virtualClassroomSessionGroups = virtualClassroomService.listAllSessionGroupsAndRecordings(siteId, sortField, sortOrder);
          } else if (filterName.equals(FILTER_VC_SESSIONS_AND_RECORDINGS)) {
              virtualClassroomSessionGroups = virtualClassroomService.listAllSessionGroupsAndRecordings(siteId, sortField, sortOrder);
          } else if (filterName.equals(FILTER_VC_SESSIONS)) {
              virtualClassroomSessionGroups = virtualClassroomService.listVirtualClassroomSessionGroups(siteId, sortField, sortOrder);
          } else if (filterName.equals(FILTER_VC_SESSIONS_CURRENT)) {
              virtualClassroomSessionGroups = filterForCurrent(virtualClassroomService.listVirtualClassroomSessionGroups(siteId, sortField, sortOrder));
          } else if (filterName.equals(FILTER_VC_SESSIONS_FUTURE)) {
              virtualClassroomSessionGroups = virtualClassroomService.listVirtualClassroomSessionGroups(siteId, new Date(), BIGGEST_FUTURE_DATE, sortField, sortOrder);
          } else if (filterName.equals(FILTER_VC_SESSIONS_PAST)) {
              virtualClassroomSessionGroups = virtualClassroomService.listVirtualClassroomSessionGroups(siteId, EARLIEST_DATE, new Date(), sortField, sortOrder);
          } else if (filterName.equals(FILTER_VC_RECORDINGS)) {
              virtualClassroomSessionGroups = virtualClassroomService.listRecordings(siteId, sortField, sortOrder);
          } else {
              throw new IllegalArgumentException("Invalid filter value specified: " + filterName);
          }
      } catch (AuthorizationException ex) {
         throw ex;
//       logger.error("Unable to retrieve the list of virtual classroom session groups.", ex);
//       throw new RuntimeException("Unable to retrieve the list of virtual classroom session groups.", ex);
      } catch (VirtualClassroomException ex) {
         logger.error("Unable to retrieve the list of virtual classroom session groups.", ex);
         return new ArrayList();
      }

      // loop through and create form versions of the virtual classroom session group objects returned by the service
      for (Iterator i=virtualClassroomSessionGroups.iterator(); i.hasNext(); ) {
         virtualClassroomSessionGroup     = (VirtualClassroomSessionGroup)i.next();
         virtualClassroomSessionGroupForm = new VirtualClassroomSessionGroupForm(virtualClassroomSessionGroup);

         // set the instructor name
         User instructor = virtualClassroomService.getUser(virtualClassroomSessionGroupForm.getInstructorId());
         if (instructor == null) {
            Locale locale = LocaleContextHolder.getLocale();
            resourceBundle.getMessage("vc_list_no_instructor_specified", null, locale);
            virtualClassroomSessionGroupForm.setInstructorName("none");
         } else {
            virtualClassroomSessionGroupForm.setInstructorName(instructor.getDisplayName());
         }

         // calculate the duration
         Schedule schedule = virtualClassroomSessionGroupForm.getSchedule();
         schedule.calculateDuration();

         // format the schedule
         schedule.setDisplayName(scheduleHelper.getScheduleDisplay(schedule));

         // add the virtual classroom session group form to the list
         virtualClassroomSessionGroupForms.add(virtualClassroomSessionGroupForm);
      }
      return virtualClassroomSessionGroupForms;
   }

    protected List filterForCurrent(List sessionGroups) {
        List filteredList = new ArrayList(sessionGroups);
        for (Iterator i=sessionGroups.iterator();i.hasNext();) {
            VirtualClassroomSessionGroup group = (VirtualClassroomSessionGroup) i.next();
            if (group.getSchedule().getStartDate().after(new Date())
                    || group.getSchedule().getEndDate().before(new Date())){
                filteredList.remove(group);
            }
        }
        return filteredList;

    }

    /**
    * retrieve the current worksite.
    * Instead of throwing an exception if the worksite can not be obtained, simply return null.
    */
   public Site getCurrentSite() {
      Site site = null;
      try {
         Placement placement = ToolManager.getCurrentPlacement();
         String siteId = placement.getContext();
         site = SiteService.getSite(placement.getContext());
      } catch (IdUnusedException ex) {
         logger.warn("Unable to retrieve the current worksite");
      }
      return site;
   }

   /**
    * setter used by spring to inject the security service.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * setter used by spring to inject the virtual classroom service.
    */
   public void setVirtualClassroomService(VirtualClassroomService virtualClassroomService) {
      this.virtualClassroomService = virtualClassroomService;
   }

   public void setScheduleHelper(ScheduleHelper scheduleHelper) {
      this.scheduleHelper = scheduleHelper;
   }

   public void setResourceBundle(ResourceBundleMessageSource resourceBundle) {
      this.resourceBundle = resourceBundle;
   }
}
