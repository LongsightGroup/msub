package com.rsmart.sakaiproject.component.model;

import org.sakaiproject.metaobj.shared.model.IdentifiableObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Nov 26, 2007
 * Time: 5:53:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToSiteType extends IdentifiableObject {
   
   private String name;
   private List<ToCategory> categories;
   
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public List<ToCategory> getCategories() {
      return categories;
   }

   public void setCategories(List<ToCategory> categories) {
      this.categories = categories;
   }
}
