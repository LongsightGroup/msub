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

package com.rsmart.admin.customizer.impl;

import com.rsmart.admin.customizer.api.CustomizerProcessingException;
import com.rsmart.admin.customizer.api.CustomizerService;
import com.rsmart.admin.customizer.api.CustomizerRun;
import com.rsmart.admin.customizer.api.RoleWrapper;
import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;
import com.rsmart.sakai.common.security.SuperUserSecurityAdvisor;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.Validator;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.*;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlReaderFinishedException;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.authz.api.*;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.theospi.portfolio.admin.intf.SakaiIntegrationPlugin;
import org.theospi.portfolio.admin.model.IntegrationOption;
import org.theospi.portfolio.portal.intf.PortalManager;
import org.theospi.portfolio.portal.model.SiteType;
import org.theospi.portfolio.portal.model.ToolCategory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 7:50:46 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CustomizerServiceImpl implements CustomizerService, BeanFactoryAware {

   protected Log logger = LogFactory.getLog(CustomizerServiceImpl.class);
      
   private static Object lock = new Object();
   private BeanFactory parent;
   private boolean destructiveMode = false;
   
   protected abstract PortalManager portalManager();
   public abstract ReloadableToolManager toolManager();
   public abstract SiteService siteService();
   protected abstract AuthzGroupService authzGroupService();
   public abstract SecurityService securityService();
   private EventTrackingService eventService = org.sakaiproject.event.cover.EventTrackingService.getInstance();


   public void reloadTools(){
       toolManager().reloadTools();

      // Post an event
      Event event = eventService.newEvent(ReloadableToolManager.TOOLS_RELOADED_EVENT, "all", true);
      eventService.post(event);

   }

   public void changeToolTitle(String toolId, String title) {
      Tool toolReg = toolManager().getTool(toolId);
      org.sakaiproject.tool.impl.ToolImpl newToolReg = copyTool(toolReg);

      newToolReg.setTitle(title);

      Properties
          finalProperties = newToolReg.getFinalConfig(),
          mutableProperties = newToolReg.getMutableConfig();

      mutableProperties.setProperty(Tool.TITLE_IS_CUSTOMIZED, Boolean.TRUE.toString());
      newToolReg.setRegisteredConfig(finalProperties, mutableProperties);

      // got through all pages and change title... could take a while
      List<ToolConfiguration> toolInst = siteService().findTools(toolReg.getId());
      
      for (ToolConfiguration tc : toolInst) {
         try {
            Site site = siteService().getSite(tc.getSiteId());
            SitePage page = site.getPage(tc.getPageId());
            
            if (page.getTitle().equals(tc.getTitle()) && page.getTools().size() == 1) {
               page.setTitle(title);
            }
            ToolConfiguration siteTool = site.getTool(tc.getId());
            siteTool.setTitle(title);
            siteService().save(site);
         } catch (IdUnusedException e) {
            logger.error("failed to find site or page", e);
         } catch (PermissionException e) {
            logger.error("failed to save page", e);
         }
      }
      
      toolManager().changeTool(newToolReg);

      // Post an event
      Event event = eventService.newEvent(ReloadableToolManager.TOOL_CHANGED_EVENT, toolId, true);
      eventService.post(event);

   }

   public void addToolsToCategory(String siteType, List<String> tools) {
      for (String toolId : tools) {
         Tool toolReg = toolManager().getTool(toolId);
         org.sakaiproject.tool.impl.ToolImpl newToolReg = copyTool(toolReg);
         
         if (!newToolReg.getCategories().contains(siteType)) {
            Set newCats = new HashSet(newToolReg.getCategories());
            newCats.add(siteType);
            newToolReg.setCategories(newCats);
            toolManager().changeTool(newToolReg);
            // Post an event
            Event event = eventService.newEvent(ReloadableToolManager.TOOL_CHANGED_EVENT, toolId, true);
            eventService.post(event);

         }
      }
   }

   public void addToolsToCategorySites(String siteType, List<String> tools) {
       Session sakaiSession = SessionManager.getCurrentSession();
       String currentUserId = sakaiSession.getUserId();

       sakaiSession.setUserId("admin");
       sakaiSession.setUserEid("admin");


      SuperUserSecurityAdvisor securityAdvisor = new SuperUserSecurityAdvisor();
      securityAdvisor.setSuperUser("admin");
      securityService().pushAdvisor(securityAdvisor);

       try {
          List<Site> sites = getSites(siteType);
          for (String toolId : tools) {
             Tool tool = toolManager().getTool(toolId);
             for (Site site : sites) {
                addToolToSite(site, tool);
             }
          }
       } finally {
           sakaiSession.setUserId(currentUserId);
           sakaiSession.setUserEid(currentUserId);
       }
      
   }

   protected void addToolToSite(Site site, Tool tool) {

       site.loadAll();
       SitePage page = site.addPage();
       page.setTitle(tool.getTitle());
       page.addTool(tool.getId());

       try {
           siteService().save(site);
       } catch (IdUnusedException e) {
           logger.error("can't add tool to site", e);
       } catch (PermissionException e) {
           logger.error("can't add tool to site", e);
       }
   }
   
   protected org.sakaiproject.tool.impl.ToolImpl copyTool(Tool toolReg) {
      org.sakaiproject.tool.impl.ToolImpl newToolReg = new org.sakaiproject.tool.impl.ToolImpl();

      newToolReg.setAccessSecurity(toolReg.getAccessSecurity());
      newToolReg.setCategories(toolReg.getCategories());
      newToolReg.setDescription(toolReg.getDescription());
      newToolReg.setHome(toolReg.getHome());
      newToolReg.setId(toolReg.getId());
      newToolReg.setKeywords(toolReg.getKeywords());
      newToolReg.setRegisteredConfig(toolReg.getFinalConfig(), toolReg.getMutableConfig());
      newToolReg.setTitle(toolReg.getTitle());
      return newToolReg;
   }

   public void setFunctionsRequire(String toolId, String functionsRequire) {
      Tool toolReg = toolManager().getTool(toolId);
      org.sakaiproject.tool.impl.ToolImpl newToolReg = copyTool(toolReg);

      Properties props = toolReg.getMutableConfig();
      props.setProperty(FUNCTIONS_REQUIRE, functionsRequire);
      newToolReg.setRegisteredConfig(toolReg.getFinalConfig(), props);

      toolManager().changeTool(newToolReg);

      // Post an event
      Event event = eventService.newEvent(ReloadableToolManager.TOOL_CHANGED_EVENT, toolId, true);
      eventService.post(event);

   }
   
   protected abstract EventTrackingService eventTrackingService();
   protected abstract SqlService sqlService();
   
   /**
    * @return chs
    */
   protected abstract ContentHostingService contentHostingService();

   public void processXml(File directory, boolean commit, CustomizerProcessingException exp) {
      processXml(directory, commit, exp, null);
   }

   public void processXml(File directory, boolean commit, CustomizerProcessingException exp, String fileId) {

      ClassLoader current = Thread.currentThread().getContextClassLoader();
      
      try {
         Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
         File source = new File(directory, "source");
         File output = new File(directory, "output");
         // processSiteCategories(source, commit, exp);
         processToolReg(source, output, commit, exp);
         processToolOrder(source, output, commit, exp);
         
         if (destructiveMode) {
            exp.addWarning("WARNING:  all site template and realm data will be re-created");
            processSiteTemplates(source, output, commit, exp);
            processRealms(source, output, commit, exp);
         }
         
         processOspPerms(source, output, commit, exp);
         
         if (commit && fileId != null) {
            logEvent(fileId);
         }
      }
      finally{
         Thread.currentThread().setContextClassLoader(current);
      }
   }

   public List<CustomizerRun> listEvents() {
      return sqlService().dbRead(
         "SELECT EVENT_DATE, REF, SESSION_ID, EVENT_CODE FROM SAKAI_EVENT WHERE EVENT = ? ORDER BY EVENT_DATE",
         new Object[]{CUSTOMIZER_EVENT}, new CustomizerRunReader(contentHostingService()));
   }

   protected void logEvent(String fileId) {
      ContentResource attachment = null;
      try {
         ContentResource resource = null;
         resource = contentHostingService().getResource(fileId);
         ResourceProperties props = resource.getProperties();
         props.addProperty(CustomizerRun.DESTRUCTIVE_MODE_PROP, destructiveMode + "");
         props.addProperty(CustomizerRun.APPLY_SITE_OPTIONS, 
            updatableServerConfigurationService().getString(
               "applySiteOptions@org.theospi.portfolio.admin.intf.SakaiIntegrationService.rsmart", "false"));
         props.addProperty(CustomizerRun.REALM_RESET_PROP, 
            updatableServerConfigurationService().getString("realm.reset", "false"));
         byte[] bytes = resource.getContent();
         String contentType = resource.getContentType();
         String filename = Validator.getFileName(fileId);
         String resourceId = Validator.escapeResourceName(filename);
         
         String siteId = ToolManager.getCurrentPlacement().getContext();
         String toolName = ToolManager.getCurrentPlacement().getTitle();
         
         attachment = contentHostingService().addAttachmentResource(
            resourceId, siteId, toolName, contentType, bytes, props);
      } catch (PermissionException e) {
         logger.error("", e);
      } catch (IdUnusedException e) {
         logger.error("", e);
      } catch (TypeException e) {
         logger.error("", e);
      } catch (InconsistentException e) {
         logger.error("", e);
      } catch (IdUsedException e) {
         logger.error("", e);
      } catch (ServerOverloadException e) {
         logger.error("", e);
      } catch (IdInvalidException e) {
         logger.error("", e);
      } catch (OverQuotaException e) {
         logger.error("", e);
      }

      Event event = eventTrackingService().newEvent(CUSTOMIZER_EVENT, attachment.getId(), destructiveMode);
      
      eventTrackingService().post(event);
   }

   protected void processToolOrder(File source, File output, boolean commit, 
                                   CustomizerProcessingException exp) {
      if (!updatableServerConfigurationService().isUseDb()) {
         exp.addWarning("WARNING:  Tool order is not configured to use the DB.  " +
            "This means that the spreadsheet categories and tool order will not be used.");
         return;
      }
      
      File file = new File(source, 
         "kernel/component-manager/src/main/bundle/org/sakaiproject/config/toolOrder.xml");
         
      if (commit) {
         try {
            updatableServerConfigurationService().updateTools(new FileInputStream(file));
         } catch (Exception e) {
            exp.addError(e.getLocalizedMessage());
            logger.error("", e);
         }
      }
      else {
         try {
            updatableServerConfigurationService().testUpdateTools(new FileInputStream(file));
         } catch (Exception e) {
            exp.addError(e.getLocalizedMessage());
            logger.error("", e);
         }
      }
   }

   protected void processOspPerms(File source, File output, boolean commit,
                                  CustomizerProcessingException exp) {
      File file = new File(source, "rsmart/osp/common/component/src/webapp/WEB-INF/components.xml");

      PropertyPlaceholderConfigurer config = new PropertyPlaceholderConfigurer();
      Properties props = new Properties();
      props.setProperty("auto.ddl", "false");
      config.setProperties(props);
      XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(file), parent);
      config.postProcessBeanFactory(factory);

      Collection<CustomizerPermissionInjector> customizers =
         factory.getBeansOfType(CustomizerPermissionInjector.class).values();

      if (commit) {
         boolean first = true;

         for (Iterator<CustomizerPermissionInjector> i=customizers.iterator();i.hasNext();) {
            CustomizerPermissionInjector injector = i.next();
            if (first) {
               first = false;
               injector.getPermissionInjectorService().clearPermissions();
            }
            injector.execute();
         }
      }
   }

   private void processRealms(File source, File output, boolean commit, CustomizerProcessingException exp) {
      File file = new File(source, "osp/common/component/src/webapp/WEB-INF/components.xml");

      PropertyPlaceholderConfigurer config = new PropertyPlaceholderConfigurer();
      Properties props = new Properties();
      props.setProperty("realm.reset", "true");
      props.setProperty("auto.ddl", "true");
      config.setProperties(props);
      XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(file), parent);
      config.postProcessBeanFactory(factory);

      CustomizerSakaiDefaultPermsManager permsManager = (CustomizerSakaiDefaultPermsManager) factory.getBean(
         "org.theospi.portfolio.security.model.SakaiDefaultPermsManager.sakaiTools.runtime",
         CustomizerSakaiDefaultPermsManager.class);

      List <CustomizerRealmManagerImpl> realmManagers = (List<CustomizerRealmManagerImpl>) permsManager.getRealmManagers();

      if (commit) {
         for (Iterator<CustomizerRealmManagerImpl> i=realmManagers.iterator();i.hasNext();) {
            i.next().execute();
         }
         permsManager.execute();
      }
   }

   protected void processSiteTemplates(File source, File output,
                                       boolean commit, CustomizerProcessingException exp) {
      File file = new File(source, "rsmart/osp/integration/component/src/webapp/WEB-INF/components.xml");

      PropertyPlaceholderConfigurer config = new PropertyPlaceholderConfigurer();
      Properties props = new Properties();    
      props.setProperty("realm.reset", "true");
      config.setIgnoreUnresolvablePlaceholders(true);
      config.setProperties(props);
      XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(file), parent);
      config.postProcessBeanFactory(factory);
      
      PluginList plugins = (PluginList) factory.getBean("com.rsmart.admin.customizer.impl.PluginList", 
         PluginList.class);
      
      if (commit) {
         for (Iterator<String> i = plugins.getIntegrationPlugins().iterator();i.hasNext();) {
            executePlugin((SakaiIntegrationPlugin) factory.getBean(i.next(), SakaiIntegrationPlugin.class));
         }
      }
   }

   protected void executePlugin(SakaiIntegrationPlugin plugin) {
      for (Iterator i=plugin.getPotentialIntegrations().iterator();i.hasNext();) {
         if (!plugin.executeOption((IntegrationOption) i.next())) {
            break;
         }
      }
   }

   protected void processToolReg(File source, File output, boolean commit,
                                 CustomizerProcessingException exp) {
      File toolFile = new File(output, "tools-scrubbed.xml");

      Document doc = Xml.readDocument(toolFile.getAbsolutePath());

      NodeList list = doc.getDocumentElement().getElementsByTagName("tool");

      for (int i=0;i<list.getLength();i++) {
         Element element = (Element) list.item(i);
         if (!element.getAttribute("fake").equals("true")) {
            File regFile = new File(source, element.getAttribute("regFile"));
            processToolRegFile(regFile, commit, exp);
         }
      }

   }

   protected void processToolRegFile(File regFile, boolean commit, CustomizerProcessingException exp) {
      List<Tool> tools = toolManager().parseTools(regFile);
      
      if (tools.size() == 0) {
         exp.addError("unable to find tool registration in: " + regFile.getAbsolutePath());
         return;
      }

      Tool tool = tools.get(0);
      if (toolManager().getActiveTool(tool.getId()) == null) {
         exp.addWarning("tool " + tool.getId() + " is not installed.");
         return;
      }
      
      if (commit) {
         toolManager().changeTool(tool);

          // Post an event
          Event event = eventService.newEvent(ReloadableToolManager.TOOL_CHANGED_EVENT, tool.getId(), true);
          eventService.post(event);

      }
   }

   /**
    * Callback that supplies the owning factory to a bean instance.
    * <p>Invoked after population of normal bean properties but before an init
    * callback like InitializingBean's afterPropertiesSet or a custom init-method.
    *
    * @param beanFactory owning BeanFactory (may not be <code>null</code>).
    *                    The bean can immediately call methods on the factory.
    * @throws org.springframework.beans.BeansException
    *          in case of initialization errors
    * @see org.springframework.beans.factory.BeanInitializationException
    */
   public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      parent = beanFactory;
   }

   protected void processSiteCategories(File source, boolean commit, CustomizerProcessingException exp) {
      File file = new File(source, "osp/portal/component/src/webapp/WEB-INF/components.xml");
      
      XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(file), parent);
      
      SiteTypeMapHolder mapHolder = (SiteTypeMapHolder) factory.getBean(
         "com.rsmart.admin.customizer.impl.SiteTypeMapHolder");
      
      File categoryDir = new File(source, "osp/portal/webapp/src/webapp/WEB-INF/category");

      try {
         processCategoryFiles((Collection<SiteType>)mapHolder.getSiteTypes().values(), categoryDir);
      } catch (IOException e) {
         logger.error("", e);
         exp.addError(e.getLocalizedMessage());
      }
      
      if (commit) {
         portalManager().storeComponentsSiteTypes(mapHolder.getSiteTypes());   
      }
   }

   protected void processCategoryFiles(Collection<SiteType> siteTypes, File categoryDir) throws IOException {
      for (Iterator<SiteType> i=siteTypes.iterator();i.hasNext();) {
         processCategoryFiles(i.next(), categoryDir);
      }
   }

   protected void processCategoryFiles(SiteType siteType, File categoryDir) throws IOException {
      for (Iterator<ToolCategory> i = siteType.getToolCategories().iterator();i.hasNext();) {
         processCategoryFiles(i.next(), categoryDir);
      }
   }

   protected void processCategoryFiles(ToolCategory toolCategory, File categoryDir) throws IOException {
      if (toolCategory.getKey().equals(ToolCategory.UNCATEGORIZED.getKey())) {
         return;
      }
      
      String homeFile = toolCategory.getHomePagePath();

      if (homeFile == null) {
         logger.warn("found null home page path for category: " + toolCategory.getKey());
         return;
      }

      toolCategory.getPages().put("en_US", readFile(categoryDir, homeFile));
   }

   protected byte[] readFile(File categoryDir, String homeFile) throws IOException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      FileInputStream is = new FileInputStream(new File(categoryDir, homeFile));
      int c = is.read();
      while (c != -1) {
         bos.write(c);
         c = is.read();
      }
      bos.flush();

      return bos.toByteArray();
   }

   public boolean isDestructiveMode() {
      return destructiveMode;
   }

   public void setDestructiveMode(boolean destructiveMode) {
      this.destructiveMode = destructiveMode;
   }
   
   private class CustomizerRunReader implements SqlReader {
      private ContentHostingService chs;

      private CustomizerRunReader(ContentHostingService chs) {
         this.chs = chs;
      }

      /**
       * Read fields from this result set, creating one object which is returned.
       *
       * @param result The SQL ResultSet, set to the proper record.
       * @return The object read.
       */
      public Object readSqlResultRecord(ResultSet result) throws SqlReaderFinishedException {

         try {
            String resourceId = result.getString("REF");
            ContentResource res = chs.getResource(resourceId);
            
            return new CustomizerRun(result.getTimestamp("EVENT_DATE"), resourceId, res, 
               result.getString("EVENT_CODE"));
         } catch (SQLException e) {
            throw new RuntimeException(e);
         } catch (TypeException e) {
            throw new RuntimeException(e);
         } catch (IdUnusedException e) {
            throw new RuntimeException(e);
         } catch (PermissionException e) {
            throw new RuntimeException(e);
         }
      }
   }

   /**
    * update perms in the realm and the sites of give type
    *
    * @param realm    realm to update.  this will be the source record,
    *                 which means that any changes to this are the only ones that will
    *                 be changed in the sites of siteType
    * @param siteType the siteType of other sites to change.  set this to null to
    *                 change no other sites
    * @param perms    a map of role to allowed functions
    */
   public void updatePermissions(String realm, String siteType, Map<String, List<String>> perms) {
      List<String> allFunctions = FunctionManager.getRegisteredFunctions();

      try {
         AuthzGroup group = authzGroupService().getAuthzGroup(realm);
         Set<Role> roles = group.getRoles();
         
         
         Map<String, RoleWrapper> roleMap = new HashMap();
         
         for (Role role : roles) {
            RoleWrapper rw = new RoleWrapper(role, allFunctions);
            roleMap.put(role.getId(), rw);
         }
         
         // update this realm
         for (String roleId : perms.keySet()) {
            // this call should update role wrapper with the ones that are changed
            updatePermissions(roleMap.get(roleId), perms.get(roleId));
         }
         
         authzGroupService().save(group);
         
         // todo: now we need to update all other realms with
         // the values in the roleWrapper role maps
         List<Site> sites = getSites(siteType);

         for (Site site : sites) {
            updatePermissions(site, roleMap);
         }
         
      } catch (GroupNotDefinedException e) {
         logger.error("group not defined", e);
      } catch (AuthzPermissionException e) {
         logger.error("unable to save authz", e);
      } catch (IdUnusedException e) {
         logger.error("unable to save site info", e);
      } catch (PermissionException e) {
         logger.error("unable to save site info", e);
      }

   }

   protected List<Site> getSites(String siteType) {
      List<Site> sites = (List<Site>)siteService().getSites(SiteService.SelectionType.ANY, 
         siteType, null, null, SiteService.SortType.CREATED_ON_ASC, null);
      return sites;
   }

   protected void updatePermissions(Site site, Map<String, RoleWrapper> roleMap) throws IdUnusedException, 
      PermissionException, GroupNotDefinedException, AuthzPermissionException {
      
      site.getRoles(); // will create authzgroup if not already created
      AuthzGroup siteAuthz = authzGroupService().getAuthzGroup(site.getReference());

      for (String roleId : roleMap.keySet()) {
         Role role = siteAuthz.getRole(roleId);
         
         if (role != null) {
            RoleWrapper rw = roleMap.get(roleId);
            
            for (Map.Entry<String, Boolean> entry : rw.getAuthzMap().entrySet()) {
               if (entry.getValue()) {
                  role.allowFunction(entry.getKey());
               }
               else {
                  role.disallowFunction(entry.getKey());
               }
            }
         }
      }
      authzGroupService().save(siteAuthz);
   }

   /**
    * take the role.authzMap and weed out all the ones that are not changed
    * and then update based on authzMap
    * @param role
    * @param allowed
    */
   protected void updatePermissions(RoleWrapper role, List<String> allowed) {
      Set<String> allFunctions = new HashSet(role.getAuthzMap().keySet());
      for (String function : allFunctions) {
         boolean oldVal = role.getAuthzMap().get(function);
         boolean newVal = !oldVal;
         
         boolean changed = ((oldVal && !allowed.contains(function)) || 
            (!oldVal && allowed.contains(function)));
         
         role.getAuthzMap().put(function, newVal);
         
         if (changed && newVal) {
            role.getRole().allowFunction(function);
         }
         else if (changed && !newVal) {
            role.getRole().disallowFunction(function);
         }
         else {
            // get rid of it so we don't update the value on other sites
            role.getAuthzMap().remove(function);
         }
      }
   }
}
