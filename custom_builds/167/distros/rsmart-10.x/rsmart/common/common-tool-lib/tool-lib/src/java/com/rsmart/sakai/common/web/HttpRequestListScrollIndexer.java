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
package com.rsmart.sakai.common.web;

import org.sakaiproject.metaobj.utils.mvc.intf.ListScrollIndexer;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;



/**
 * Extension to sakai'a ListScrollIndexer to allow us to pass the request object in
 * I was getting some really funky results attempts to just pass in the request.getParameterMap(), this
 * seems to have solved the issue.
 */
public interface HttpRequestListScrollIndexer extends ListScrollIndexer {
    public List indexList(HttpServletRequest request, Map model, List srouceList);
}
