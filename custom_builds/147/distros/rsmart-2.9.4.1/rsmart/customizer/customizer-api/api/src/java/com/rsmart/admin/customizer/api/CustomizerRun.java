package com.rsmart.admin.customizer.api;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 15, 2008
 * Time: 3:29:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomizerRun {
   
   public static final String DESTRUCTIVE_MODE_PROP = "destructiveMode";
   public static final String REALM_RESET_PROP = "realmReset";
   public static final String APPLY_SITE_OPTIONS = "applySiteOptions";

   private Date dateRan;
   private String resourceId;
   private String resourceLink;
   private boolean destrutiveMode;
   private Boolean realmReset;
   private Boolean applySiteOptions;

   public CustomizerRun(Date dateRan, String resourceId, ContentResource res, String eventCode) {
      this.dateRan = dateRan;
      this.resourceId = resourceId;
      this.resourceLink = res.getUrl();
      this.destrutiveMode = eventCode.equalsIgnoreCase("m");
      ResourceProperties props = res.getProperties();
      String realmResetProp = props.getProperty(REALM_RESET_PROP);
      if (realmResetProp != null) {
         this.realmReset = new Boolean(realmResetProp);
      }
      String applySiteOptionsProp = props.getProperty(APPLY_SITE_OPTIONS);
      if (applySiteOptionsProp != null) {
         this.applySiteOptions = new Boolean(applySiteOptionsProp);
      }
   }

   public Date getDateRan() {
      return dateRan;
   }

   public void setDateRan(Date dateRan) {
      this.dateRan = dateRan;
   }

   public String getResourceId() {
      return resourceId;
   }

   public void setResourceId(String resourceId) {
      this.resourceId = resourceId;
   }

   public String getDestructiveMode() {
      return isDestrutiveMode()?"Yes":"No";
   }
   
   public boolean isDestrutiveMode() {
      return destrutiveMode;
   }

   public void setDestrutiveMode(boolean destrutiveMode) {
      this.destrutiveMode = destrutiveMode;
   }

   public String getResourceLink() {
      return resourceLink;
   }

   public void setResourceLink(String resourceLink) {
      this.resourceLink = resourceLink;
   }

   public Boolean getRealmReset() {
      return realmReset;
   }

   public String getRealmResetString() {
      if (realmReset == null) {
         return "?";
      }
      return getRealmReset()?"Yes":"No";
   }

   public void setRealmReset(Boolean realmReset) {
      this.realmReset = realmReset;
   }

   public Boolean getApplySiteOptions() {
      return applySiteOptions;
   }

   public String getApplySiteOptionsString() {
      if (applySiteOptions == null) {
         return "?";
      }
      return getApplySiteOptions()?"Yes":"No";
   }

   public void setApplySiteOptions(Boolean applySiteOptions) {
      this.applySiteOptions = applySiteOptions;
   }
}
