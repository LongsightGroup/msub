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

import org.sakaiproject.authz.api.GroupProvider;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Feb 6, 2008
 * Time: 3:09:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class DummyGroupProvider implements GroupProvider {
    private Map emptyMap = new HashMap();

    public String getRole(String id, String user) {
        return null;
    }

    public Map getUserRolesForGroup(String id) {
        return emptyMap;
    }

    public Map getGroupRolesForUser(String userId) {
        return emptyMap;
    }

    public String packId(String[] ids) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] unpackId(String id) {
        return new String[0];
    }

    public String preferredRole(String one, String other) {
        return null;  
    }

    public boolean groupExists(String id){
        return false;
    }
}
