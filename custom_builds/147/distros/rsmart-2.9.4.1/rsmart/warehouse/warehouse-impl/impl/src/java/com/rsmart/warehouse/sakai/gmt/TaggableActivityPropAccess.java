package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.taggable.api.TaggableActivity;
import org.sakaiproject.warehouse.impl.BeanPropertyAccess;
import org.sakaiproject.warehouse.service.PropertyAccess;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2008
 * Time: 9:36:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaggableActivityPropAccess extends TaggingPropAccessBase {

   public Object getPropertyValue(Object source) throws Exception {
      String activityRef = (String) super.getPropertyValue(source);
      if (activityRef == null) {
         return null;
      }
      
      TaggableActivity activity = 
         getTaggingManager().getActivity(activityRef, getTaggingProvider());
      
      return getNestedProperty().getPropertyValue(activity);
   }

}
