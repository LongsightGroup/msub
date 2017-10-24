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

package com.rsmart.sakai.site;

import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.rsmart.sakai.common.security.SuperUserSecurityAdvisor;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Aug 1, 2007
 * Time: 12:44:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class SiteInitializer {
    private String siteId;
    private List tools = new ArrayList();
    private SiteService siteService;
    private static Log logger = LogFactory.getLog(SiteInitializer.class);


    public void init(){
        Site site = null;
        try {
            site = getSiteService().getSite(siteId);
        } catch (IdUnusedException e) {
            logger.error("can't find site with id=" + siteId, e);
        }



        for (Iterator<ToolBean> i = getTools().iterator(); i.hasNext(); ){
            ToolBean toolBean = i.next();                                        
            ToolConfiguration toolConfig = getToolForCommonId(site, toolBean.getToolId());
            if (toolConfig == null){
                SitePage page = site.addPage();
                page.setTitle(toolBean.getPageName());
                page.addTool(toolBean.getToolId());

                Session sakaiSession = SessionManager.getCurrentSession();
                String currentUserId = sakaiSession.getUserId();

                sakaiSession.setUserId("admin");
                sakaiSession.setUserEid("admin");

                SuperUserSecurityAdvisor securityAdvisor = new SuperUserSecurityAdvisor();
                securityAdvisor.setSuperUser("admin");
                SecurityService.pushAdvisor(securityAdvisor);
                
                try {
                    siteService.save(site);
                } catch (IdUnusedException e) {
                    logger.error("can't add tool to site", e);
                } catch (PermissionException e) {
                    logger.error("can't add tool to site", e);
                } finally {
                    sakaiSession.setUserId(currentUserId);
                    sakaiSession.setUserEid(currentUserId);
                }
            }
        }
    }

   protected ToolConfiguration getToolForCommonId(Site site, String toolId) {
      List<SitePage> pages = site.getOrderedPages();
      
      for (SitePage page : pages) {
         List<ToolConfiguration> tools = page.getTools();
         for (ToolConfiguration tool : tools) {
            if (tool.getToolId().equals(toolId)) {
               return tool;
            }
         }
      }
      return null;
   }

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

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
