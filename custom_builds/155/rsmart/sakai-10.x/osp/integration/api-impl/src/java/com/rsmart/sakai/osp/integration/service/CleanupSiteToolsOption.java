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

import org.theospi.portfolio.admin.model.IntegrationOption;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 7, 2006
 * Time: 10:49:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleanupSiteToolsOption extends IntegrationOption {

   private String siteId;
   private List tools;
   private List ignorePages;

   public String getSiteId() {
      return siteId;
   }

   public void setSiteId(String siteId) {
      this.siteId = siteId;
   }

   public List getTools() {
      return tools;
   }

   public void setTools(List tools) {
      this.tools = tools;
   }

   public List getIgnorePages() {
      return ignorePages;
   }

   public void setIgnorePages(List ignorePages) {
      this.ignorePages = ignorePages;
   }
}
