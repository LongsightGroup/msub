package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.gmt.api.GmtService;
import org.sakaiproject.gmt.api.Goal;
import org.sakaiproject.warehouse.service.PropertyAccess;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 7, 2008
 * Time: 5:52:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinksFromGoalPropAccess implements PropertyAccess {

   private GmtService gmtService;
   
   public Object getPropertyValue(Object source) throws Exception {
      return getGmtService().getLinks((Goal)source);
   }

   public GmtService getGmtService() {
      return gmtService;
   }

   public void setGmtService(GmtService gmtService) {
      this.gmtService = gmtService;
   }
}
