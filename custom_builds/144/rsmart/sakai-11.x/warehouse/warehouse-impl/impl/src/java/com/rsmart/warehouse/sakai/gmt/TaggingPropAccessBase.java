package com.rsmart.warehouse.sakai.gmt;

import org.sakaiproject.taggable.api.TaggingProvider;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.warehouse.service.PropertyAccess;
import org.sakaiproject.warehouse.impl.BeanPropertyAccess;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 8, 2008
 * Time: 12:05:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TaggingPropAccessBase extends BeanPropertyAccess {

   private TaggingProvider taggingProvider;
   private TaggingManager taggingManager;
   private PropertyAccess nestedProperty;
   
   public TaggingProvider getTaggingProvider() {
      return taggingProvider;
   }

   public void setTaggingProvider(TaggingProvider taggingProvider) {
      this.taggingProvider = taggingProvider;
   }

   public TaggingManager getTaggingManager() {
      return taggingManager;
   }

   public void setTaggingManager(TaggingManager taggingManager) {
      this.taggingManager = taggingManager;
   }

   public PropertyAccess getNestedProperty() {
      return nestedProperty;
   }

   public void setNestedProperty(PropertyAccess nestedProperty) {
      this.nestedProperty = nestedProperty;
   }
}
