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

package com.rsmart.sakaiproject.tool.impl;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.impl.ActiveToolComponent;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;
import com.rsmart.sakaiproject.tool.api.DbTool;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Mar 27, 2007
 * Time: 10:39:28 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DbActiveToolManager extends ActiveToolComponent
   implements Observer, ReloadableToolManager {


    /**
    * @return the DbActiveToolDbSupport collaborator.
    */
   protected abstract DbActiveToolDbSupport dbSupport();
   private EventTrackingService eventService = org.sakaiproject.event.cover.EventTrackingService.getInstance();

    public void init(){
        super.init();
        // get notified of events to watch for a reset
        eventService.addObserver(this);
    }


   /**
    * @inheritDoc
    */
   public void register(Tool tool, ServletContext context) {
      DbTool dbTool = dbSupport().findTool(tool.getId());

      if (dbTool == null) {
         dbTool = new DbTool(tool);
         dbTool = dbSupport().storeTool(dbTool);
      }

      tool = createTool(dbTool);

      super.register(tool, context);

		// Post an event
		Event event = eventService.newEvent(TOOL_REG_EVENT, tool.getId(), false);
		eventService.post(event);
   }

    /**
     *  Addresses CLE-4766
     *  This method is overriden to circumvent the use of locale bundles as the authoritative source of the tool
     *  name as is done in the superclass' version of getLocalizedToolProperty. The primacy of the locale bundles
     *  made it impossible to override tool titles using the customizer tool.  This fix breaks SAK-8908 for any tool
     *  which has been renamed by the customizer (SAK-8908 should still be fixed for any non-customized tools)
     */
    @Override
    public String getLocalizedToolProperty(String toolId, String key)
    {
        // Only allow title overrides if the customizer.allowTitleCustomization is on
        if (ServerConfigurationService.getBoolean("customizer.allowTitleCustomization", false) )
        {
            return null;
        }
        return super.getLocalizedToolProperty(toolId, key);
    }

    protected Tool createTool(DbTool dbTool) {
      DbActiveTool tool = new DbActiveTool();

      tool.setId(dbTool.getToolId());
      tool.setTitle(dbTool.getTitle());
      tool.setDescription(dbTool.getDescription());
      tool.setHome(dbTool.getHome());
      tool.setAccessSecurity(dbTool.getAccessSecurityObject());
      tool.setCategories(new HashSet(dbTool.getCategories()));
      tool.setKeywords(new HashSet(dbTool.getKeywords()));
      tool.setRegisteredConfig(
              convertProperties(dbTool.getFinalConfig()),
              convertProperties(dbTool.getMutableConfig()));


      return tool;
   }

   protected Properties convertProperties(Map config) {
      Properties returned = new Properties();
      returned.putAll(config);
      return returned;
   }

   public void changeTool(Tool tool) {
      DbTool dbTool = dbSupport().findTool(tool.getId());
      dbTool.copyFrom(tool);
      dbSupport().storeTool(dbTool);
      DbActiveTool existing = (DbActiveTool)getActiveTool(tool.getId());

      super.register(new DbActiveTool(tool), existing.getServletContext());
   }

   public boolean isStealthed(Tool tool) {
      if (m_toolIdsToHide == null) return false;
      return (Arrays.binarySearch(m_toolIdsToHide, tool.getId()) >= 0);
   }

   public void reloadTools() {

      for (Iterator i =m_tools.keySet().iterator(); i.hasNext();) {
          String toolId = (String) i.next();
          DbTool dbTool = dbSupport().findTool(toolId);
          Tool tool = createTool(dbTool);
          DbActiveTool existing = (DbActiveTool)getActiveTool(toolId);
          super.register(new DbActiveTool(tool), existing.getServletContext());
      }
   }

    public void update(java.util.Observable observable, java.lang.Object arg){
        if (arg instanceof Event) {
            Event event = (Event)arg;

            if (event.getEvent().equals(ReloadableToolManager.TOOL_CHANGED_EVENT) ||
                    event.getEvent().equals(ReloadableToolManager.TOOLS_RELOADED_EVENT)) {
                reloadTools();
            }
        }
    }


   public class DbActiveTool extends MyActiveTool {

      /**
       * Construct
       */
      public DbActiveTool() {
      }

      /**
       * Construct from a Tool
       */
      public DbActiveTool(Tool t) {
         super(t);
      }
      
      public ServletContext getServletContext() {
         return m_servletContext;
      }
   }
   
}
