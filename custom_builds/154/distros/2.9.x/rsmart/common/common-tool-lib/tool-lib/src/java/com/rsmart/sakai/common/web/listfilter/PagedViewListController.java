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
 * Adds support for passing page size and starting index down to the database tier.  This provides better
 * performance and scalability for large data sets then the ViewListController which loads the entire list on every
 * request for a page
 */
abstract public class PagedViewListController extends AbstractViewListController {


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
   public ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
      Map model = new HashMap();

      try {
         setSortParms(httpServletRequest, httpServletResponse, model);
         int pageSize = getPageSize(httpServletRequest, httpServletResponse);
         int totalSize = getListSize(httpServletRequest);
         int startingIndex = getStartingIndex(httpServletRequest, httpServletResponse, model, pageSize, totalSize);
         List filteredList = filterList(httpServletRequest, httpServletResponse, model, getList(httpServletRequest, startingIndex, pageSize));

         model.put(getListName(), filteredList);
         Map moreData = referenceData(httpServletRequest);
         if (moreData != null) {
            model.putAll(referenceData(httpServletRequest));
         }
      } catch (Exception ex) {
         model.put("error", ex.getMessage());
         throw ex;
      }
      return new ModelAndView(getSuccessView(), model);

   }

   /**
    * sub classes should implement this to do a select count(*) from the backend store
    * @param request
    * @return
    */
   abstract protected int getListSize(HttpServletRequest request);

   /**
    * sub classes should override this to make direct calls to the backend store without loading the entire result set.
    * This method is reponsible for doing any filtering and sorting of the list.
    * @param httpServletRequest
    * @param startingIndex
    * @param pageSize
    * @return
    */
   abstract protected List getList(HttpServletRequest httpServletRequest, int startingIndex, int pageSize) ;
}
