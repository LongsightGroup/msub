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

package com.rsmart.sakaiproject.tool.api;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ActiveToolManager;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Mar 27, 2007
 * Time: 10:43:37 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ReloadableToolManager extends ActiveToolManager {

   public void changeTool(Tool newReg);

   public boolean isStealthed(Tool toolReg);
    
   public final static String TOOL_REG_EVENT = "TOOL_REGISTERED";
   public static final String TOOL_CHANGED_EVENT = "TOOL_CHANGED";
   public static final String TOOLS_RELOADED_EVENT = "TOOLS_RELOADED";


   public void reloadTools();
}
