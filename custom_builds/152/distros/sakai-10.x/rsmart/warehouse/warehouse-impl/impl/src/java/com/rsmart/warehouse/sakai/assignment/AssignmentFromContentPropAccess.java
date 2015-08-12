package com.rsmart.warehouse.sakai.assignment;

import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.warehouse.service.PropertyAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 7, 2008
 * Time: 11:35:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentFromContentPropAccess implements PropertyAccess {

   private AssignmentService assignmentService;
   
   public Object getPropertyValue(Object source) throws Exception {
      AssignmentContent content = (AssignmentContent) source;
      Collection collection = new ArrayList();
      for (Iterator i = getAssignmentService().getAssignments(content);i.hasNext();) {
         collection.add(i.next());
      }
      return collection;
   }

   public AssignmentService getAssignmentService() {
      return assignmentService;
   }

   public void setAssignmentService(AssignmentService assignmentService) {
      this.assignmentService = assignmentService;
   }
}
