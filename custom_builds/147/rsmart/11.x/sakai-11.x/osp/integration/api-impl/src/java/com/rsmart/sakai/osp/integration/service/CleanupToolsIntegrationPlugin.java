/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.sakai.osp.integration.service;

import org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin;
import org.theospi.portfolio.admin.model.IntegrationOption;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 7, 2006
 * Time: 9:41:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleanupToolsIntegrationPlugin implements SakaiIntegrationPlugin {

   private static final Log log = LogFactory.getLog(CleanupToolsIntegrationPlugin.class);

   private String title;
   private String description;
   private List potentialIntegrations;
   private boolean active = true;

   private SiteService siteService;

   public IntegrationOption updateOption(IntegrationOption option) {
      if (isActive()) {
         CleanupSiteToolsOption siteOption = (CleanupSiteToolsOption) option;
         try {
            Site site = getSiteService().getSite(siteOption.getSiteId());
            List pages = site.getPages();
            List pagesToRemove = new ArrayList();

            for (Iterator i=pages.iterator();i.hasNext();) {
               processPage((SitePage)i.next(), siteOption, pagesToRemove);
            }

            for (Iterator i=pagesToRemove.iterator();i.hasNext();) {
               site.removePage((SitePage)i.next());
            }

            getSiteService().save(site);

         } catch (IdUnusedException e) {
            log.error("", e);
         } catch (PermissionException e) {
            log.error("", e);
         }
      }

      return option;
   }

   protected void processPage(SitePage sitePage,
                              CleanupSiteToolsOption siteOption, List pagesToRemove) {
      if (siteOption.getIgnorePages().contains(sitePage.getTitle())) {
         return;
      }

      ToolConfiguration tool = (ToolConfiguration)sitePage.getTools().get(0);

      if (!siteOption.getTools().contains(tool.getToolId())) {
         pagesToRemove.add(sitePage);
      }
   }

   public boolean executeOption(IntegrationOption option) {
      updateOption(option);

      return true;
   }

   public List getPotentialIntegrations() {
      return potentialIntegrations;
   }

   public void setPotentialIntegrations(List potentialIntegrations) {
      this.potentialIntegrations = potentialIntegrations;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isActive() {
      return active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public SiteService getSiteService() {
      return siteService;
   }

   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }
}
