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

package com.rsmart.admin.customizer.api;

import org.sakaiproject.event.api.Event;

import javax.xml.transform.TransformerFactory;
import java.io.File;
import java.util.List;
import java.util.Map;

import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;
import com.rsmart.sakaiproject.component.api.UpdatableServerConfigurationService;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Apr 3, 2007
 * Time: 7:39:16 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CustomizerService {

   public static final String CUSTOMIZER_EVENT = "rsn.customizer";
   public static final String FUNCTIONS_REQUIRE = "functions.require";

   public void processXml(File directory, boolean commit, CustomizerProcessingException exp);

   public void processXml(File directory, boolean commit, CustomizerProcessingException exp, String fileId);

   public List<CustomizerRun> listEvents();
   
   public ReloadableToolManager toolManager();
   
   public void changeToolTitle(String toolId, String title);

   public void setFunctionsRequire(String toolId, String functionsRequire);
   
   public UpdatableServerConfigurationService updatableServerConfigurationService();

   /**
    * update perms in the realm and the sites of give type
    * @param realm realm to update.  this will be the source record, 
    * which means that any changes to this are the only ones that will 
    * be changed in the sites of siteType
    * @param siteType the siteType of other sites to change.  set this to null to 
    * change no other sites
    * @param perms a map of role to allowed functions
    */
   public void updatePermissions(String realm, String siteType, Map<String, List<String>> perms);
   
   public void addToolsToCategory(String siteType, List<String> tools);
   
   public void addToolsToCategorySites(String siteType, List<String> tools);

   public void reloadTools();
}
