package com.rsmart.warehouse.sakai.assignment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.warehouse.impl.CallbackWarehouseTask;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 7, 2008
 * Time: 11:06:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentWarehouseTask extends CallbackWarehouseTask {

   private SqlService sqlService;
   private AssignmentService assignmentService;
    private final Log logger = LogFactory.getLog(AssignmentWarehouseTask.class);
   
   
   protected void process() {
      // getItems then call execute for each item...
      List<String> assignmentContentList = getSqlService().dbRead("SELECT CONTENT_ID FROM assignment_content");
      
      for (String assignmentContent : assignmentContentList) {
         try {
            AssignmentContent content = getAssignmentService().getAssignmentContent(assignmentContent);
            execute(content);
         } catch (Exception e) {
            logger.error("failed to process assignment content: " + assignmentContent, e);
         }
      }
      try {
         flush();
      } catch (Exception e) {
            logger.error("failed to process assignment content", e);
      }
   }

   public AssignmentService getAssignmentService() {
      return assignmentService;
   }

   public void setAssignmentService(AssignmentService assignmentService) {
      this.assignmentService = assignmentService;
   }
   
   public SqlService getSqlService() {
      return sqlService;
   }

   public void setSqlService(SqlService sqlService) {
      this.sqlService = sqlService;
   }
}
