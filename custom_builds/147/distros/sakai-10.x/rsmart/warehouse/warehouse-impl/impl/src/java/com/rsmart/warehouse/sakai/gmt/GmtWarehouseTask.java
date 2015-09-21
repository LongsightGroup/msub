package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.gmt.api.GmtService;
import org.sakaiproject.gmt.api.GoalSet;
import org.sakaiproject.warehouse.impl.CallbackWarehouseTask;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 7, 2008
 * Time: 5:27:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class GmtWarehouseTask extends CallbackWarehouseTask {

   private GmtService gmtService;
   
   protected void process() {
      List<GoalSet> goalSets = getGmtService().getGoalSets();
      
      for (GoalSet goalSet : goalSets) {
         try {
            execute(goalSet);
         } catch (Exception e) {
            logger.error("failed to process goal set: " + goalSet.getId(), e);
         }
      }
      
      try {
         flush();
      } catch (Exception e) {
         logger.error("failed to process goal sets", e);
      }
   }

   public GmtService getGmtService() {
      return gmtService;
   }

   public void setGmtService(GmtService gmtService) {
      this.gmtService = gmtService;
   }
}
