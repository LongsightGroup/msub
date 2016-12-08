package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.metaobj.shared.model.IdentifiableObject;
import org.sakaiproject.warehouse.impl.BeanPropertyAccess;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 10, 2008
 * Time: 10:22:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class IdPropertyAccess extends BeanPropertyAccess {

   public Object getPropertyValue(Object parent) throws Exception {
      Object source = super.getPropertyValue(parent);
      
      if (source == null) {
         return null;
      }
      else if (source instanceof Entity) {
         return ((Entity)source).getId();
      }
      else if (source instanceof IdentifiableObject) {
         return ((IdentifiableObject)source).getId().getValue();
      }
      
      return null;
   }
}
