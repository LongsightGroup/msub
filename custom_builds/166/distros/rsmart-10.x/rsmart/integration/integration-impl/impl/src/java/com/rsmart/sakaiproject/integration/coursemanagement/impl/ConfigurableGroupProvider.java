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

package com.rsmart.sakaiproject.integration.coursemanagement.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Feb 4, 2008
 * Time: 11:26:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurableGroupProvider implements GroupProvider, BeanFactoryAware {
   
   private GroupProvider delegate;
   private BeanFactory beanFactory;
   private List dependantObjects;
   private ServerConfigurationService serverConfigurationService;
   private ConfigurableBeanFactory childBeanFactory;
   
   protected final transient Log logger = LogFactory.getLog(getClass());
   
   private static final String GROUP_PROVIDER_CONFIG = "groupProvider.xml";
   private static final String PROVIDER_BEAN_ID = "org.sakaiproject.authz.api.GroupProvider.configurableCustom";
   
   public void init() {
      String configFilePath = getServerConfigurationService().getSakaiHomePath() + GROUP_PROVIDER_CONFIG;
      File configFile = new File(configFilePath);
      
      if (configFile.exists()) {
         DefaultListableBeanFactory factory = new DefaultListableBeanFactory(getBeanFactory());
         setChildBeanFactory(factory);

         XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) getBeanFactory());
         reader.setBeanClassLoader(getClass().getClassLoader());
         reader.loadBeanDefinitions(new FileSystemResource(configFile));

         factory.preInstantiateSingletons();
         Object provider = factory.getBean(PROVIDER_BEAN_ID);
         if (provider != null && provider instanceof GroupProvider) {
            setDelegate((GroupProvider) provider);
         }
         else {         
            logger.error("provder config file: " + configFilePath + " does not contain a provider bean");
         }
      }
      else {
         logger.info("Provider config file doesn't exist");   
      }
   }

   public boolean groupExists(String id){
        return getDelegate().groupExists(id);
   }
    
   public void destroy() {
      if (getChildBeanFactory() != null) {
         getChildBeanFactory().destroySingletons();
      }
   }
   
   public String getRole(String id, String user) {
      return getDelegate().getRole(id, user);
   }

   public Map getUserRolesForGroup(String id) {
      return getDelegate().getUserRolesForGroup(id);
   }

   public Map getGroupRolesForUser(String userId) {
      return getDelegate().getGroupRolesForUser(userId);
   }

   public String packId(String[] ids) {
      return getDelegate().packId(ids);
   }

   public String[] unpackId(String id) {
      return getDelegate().unpackId(id);
   }

   public String preferredRole(String one, String other) {
      return getDelegate().preferredRole(one, other);
   }

   public GroupProvider getDelegate() {
      if (delegate == null) {
         throw new RuntimeException("group provider was not configured in " + 
            getServerConfigurationService().getSakaiHomePath() + GROUP_PROVIDER_CONFIG);
      }
      return delegate;
   }

   public void setDelegate(GroupProvider delegate) {
      this.delegate = delegate;
   }

   public BeanFactory getBeanFactory() {
      return beanFactory;
   }

   public void setBeanFactory(BeanFactory beanFactory) {
      this.beanFactory = beanFactory;
   }

   public List getDependantObjects() {
      return dependantObjects;
   }

   public void setDependantObjects(List dependantObjects) {
      this.dependantObjects = dependantObjects;
   }

   public ServerConfigurationService getServerConfigurationService() {
      return serverConfigurationService;
   }

   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   public ConfigurableBeanFactory getChildBeanFactory() {
      return childBeanFactory;
   }

   public void setChildBeanFactory(ConfigurableBeanFactory childBeanFactory) {
      this.childBeanFactory = childBeanFactory;
   }
}
