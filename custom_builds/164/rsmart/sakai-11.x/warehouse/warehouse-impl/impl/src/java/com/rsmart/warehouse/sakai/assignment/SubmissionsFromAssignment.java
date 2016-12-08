package com.rsmart.warehouse.sakai.assignment;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.warehouse.service.PropertyAccess;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 7, 2008
 * Time: 3:42:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionsFromAssignment implements PropertyAccess {

   private AssignmentService assignmentService;
   
   public Object getPropertyValue(Object source) throws Exception {
      Assignment assign = (Assignment) source;
      return getAssignmentService().getSubmissions(assign);
   }

   public AssignmentService getAssignmentService() {
      return assignmentService;
   }

   public void setAssignmentService(AssignmentService assignmentService) {
      this.assignmentService = assignmentService;
   }
}
