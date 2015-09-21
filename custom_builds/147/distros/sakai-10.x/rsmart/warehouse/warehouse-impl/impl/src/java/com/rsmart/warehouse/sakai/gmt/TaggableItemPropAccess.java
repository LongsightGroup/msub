package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.taggable.api.TaggableItem;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2008
 * Time: 11:50:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaggableItemPropAccess extends TaggingPropAccessBase {

   public Object getPropertyValue(Object source) throws Exception {
      String itemRef = (String) super.getPropertyValue(source);
      if (itemRef == null) {
         return null;
      }
      
      TaggableItem item = 
         getTaggingManager().getItem(itemRef, getTaggingProvider(), false);
      
      return getNestedProperty().getPropertyValue(item);
      
   }
}
