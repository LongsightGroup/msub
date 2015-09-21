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
package com.rsmart.sakai.common.web.listfilter;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Adds support for filtering and paging lists and should be used with the listFilter and listScroll tags.
 * Subclasses need to implement getList() and inject the
 * listScrollIndexer, and the successView. This controller can automatically apply filters.  To use this feature
 * you need to implement the org.sakaiproject.util.Filter for each filter you want to use.  The inject a map
 * of these (name, filter impl) into your controller.  Turn on applyFilter, and away you go.  For large datasets,
 * this approach will be much slower (although more elegant) than applying the filter in getList().  applyFilter
 * is off be default. When applyFilter is off, the controller expects filterNames to be populated with a list of
 * keys that represent the filters.  The tag will use these keys to present a localized drop down.  The selected key
 * is available using the getFilterName() method.
 * <p/>
 */
abstract public class ViewListController extends AbstractViewListController {

   /**
    * calls getList() and filters and pages the list and puts it in request as getListName().
    * Then calls referenceData() so subclasses can add more stuff into the model.
    * Finally, we return the successView
    *
    * @param httpServletRequest
    * @param httpServletResponse
    * @return
    * @throws Exception
    */
   public ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws java.lang.Exception {
      Map model = new HashMap();

      try {
         setSortParms(httpServletRequest, httpServletResponse, model);
         int pageSize = getPageSize(httpServletRequest, httpServletResponse);
         List list = getList(httpServletRequest);
         List filteredList = filterList(httpServletRequest, httpServletResponse, model, list);
         int totalSize = filteredList.size();
         int startingIndex = getStartingIndex(httpServletRequest, httpServletResponse, model, pageSize, totalSize);
         List subList = indexList(filteredList, startingIndex, pageSize, totalSize);

         model.put(getListName(), subList);

         Map moreData = referenceData(httpServletRequest);
         if (moreData != null) {
            model.putAll(referenceData(httpServletRequest));
         }
      } catch (Exception ex) {
         logger.error("",ex);
         model.put("error", ex.getMessage());
         throw ex;
      }
      return new ModelAndView(getSuccessView(), model);

   }


   /**
    * all subclasses must implement this to provide the list.  If applyFilter is set to true, the controller
    * expects the filterMap to be populated with a list of filter name, filter impls.  In such a case the controller
    * will apply the filter.  If applyFilter is set to false, then getList() should filter the list.   Use getFilterName()
    * to retrieve the name of the filter that was selected in the UI.
    *
    * @param httpServletRequest
    * @return the list
    */
   protected abstract List  getList(HttpServletRequest httpServletRequest);
}
