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
import org.sakaiproject.metaobj.utils.mvc.intf.ListScroll;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



/**
 *
 */
public class HttpRequestListScrollIndexerImpl implements ListScrollIndexer, HttpRequestListScrollIndexer {
   private ListScrollIndexer listScrollIndexerDelegate;

   public List indexList(HttpServletRequest request, Map model, List srouceList) {
      Map requestMap = new HashMap();
      requestMap.put(ListScroll.ENSURE_VISIBLE_TAG, request.getParameter(ListScroll.ENSURE_VISIBLE_TAG));
      requestMap.put(ListScroll.STARTING_INDEX_TAG, request.getParameter(ListScroll.STARTING_INDEX_TAG));
      return indexList(requestMap, model, srouceList);
   }

   public List indexList(Map request, Map model, List sourceList) {
      return listScrollIndexerDelegate.indexList(request, model, sourceList);
   }

     public List indexList(Map request, Map model, List sourceList, boolean hideOnePageScroll){
      return listScrollIndexerDelegate.indexList(request, model, sourceList, hideOnePageScroll);
  }

   public ListScrollIndexer getListScrollIndexerDelegate() {
      return listScrollIndexerDelegate;
   }

   public void setListScrollIndexerDelegate(ListScrollIndexer listScrollIndexerDelegate) {
      this.listScrollIndexerDelegate = listScrollIndexerDelegate;
   }
}
