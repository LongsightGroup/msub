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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.utils.mvc.intf.ListScroll;
import org.sakaiproject.javax.Filter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
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
abstract public class AbstractViewListController extends AbstractController {
   protected final Log logger = LogFactory.getLog(getClass());

   public final static String FILTER_NAME           = "listfilter";
   public final static String FILTER_RESET          = "-1";
   public final static String FILTERS_ATTRIBUTE_KEY = "filters";
   public final static String LIST_ATTRIBUTE_KEY    = "list";
   public final static String ID_ATTRIBUTE_KEY      = "com.rsmart.sakai.common.web.listfilter.tagid";
   public final static String SORT_COLUMN_PARM      = "sortCol";
   public final static String SORT_ORDER_PARM       = "sortOrder";
   public final static String SORT_ENABLED          = "sortEnabled";


   private String successView  = null;
   private Map     filters     = new HashMap();
   private String  listName    = LIST_ATTRIBUTE_KEY;
   private String  id          = getClass().getName();
   private boolean applyFilter = false;
   private List    filterNames = new ArrayList();

   //need to be spring-a-fied for sorting to work
   private boolean enableSorting = false;
   private String defaultSortOrder;
   private String defaultSortCol;

   // use threadlocal so these stateful values are thread safe
   private ThreadLocal currentSortOrder = new ThreadLocal();
   private ThreadLocal currentSortCol = new ThreadLocal();


   public static String SCROLL_SIZE = "sroll_size";
   public static final String COOKIE_SEP       = "|";
   public static final String COOKIE_SEP_SPLIT = "\\|";

   abstract public ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws java.lang.Exception ;

   protected List indexList(List list, int startingIndex, int pageSize, int totalSize) {
      int endIndex = (startingIndex + pageSize >= totalSize) ? totalSize : startingIndex + pageSize;
      return list.subList(startingIndex, endIndex);
   }

   protected void setSortParms(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map model) {


      if (isEnableSorting()) {
         model.put(SORT_ENABLED, new Boolean(true));

         if (httpServletRequest.getParameter(SORT_COLUMN_PARM) != null) {
            setCurrentSortCol(httpServletRequest.getParameter(SORT_COLUMN_PARM), httpServletResponse);
         } else {
            String colFromCookie = getCookieValue("sortColumn_" + getClass().getPackage().getName() + "." + getClass().getName(), httpServletRequest);

            if (colFromCookie != null) {
               setCurrentSortCol(colFromCookie, httpServletResponse);

            } else {
               if (getDefaultSortCol() != null) {
                  setCurrentSortCol(getDefaultSortCol(), httpServletResponse);
               } else {
                  throw new RuntimeException("If enableSorting you must set the default sort and default order values in your config...");
               }
            }
         }

         if (httpServletRequest.getParameter(SORT_ORDER_PARM) != null) {
            setCurrentSortOrder(httpServletRequest.getParameter(SORT_ORDER_PARM), httpServletResponse);
         } else {

            String orderFromCookie = getCookieValue("sortOrder_" + getClass().getPackage().getName() + "." + getClass().getName(), httpServletRequest);
            if (orderFromCookie != null) {
               setCurrentSortOrder(orderFromCookie, httpServletResponse);
            } else {

               if (getDefaultSortOrder() != null) {
                  setCurrentSortOrder(getDefaultSortOrder(), httpServletResponse);
               } else {
                  throw new RuntimeException("If enableSorting you must set the default sort and default order values in your config...");
               }
            }
         }
         model.put(SORT_COLUMN_PARM, getCurrentSortCol());
         model.put(SORT_ORDER_PARM, getCurrentSortOrder());

      } else {
         model.put(SORT_ENABLED, new Boolean(false));
      }

   }

   protected String getCookieValue(String name, HttpServletRequest httpServletRequest) {

      Cookie[] cookies = httpServletRequest.getCookies();

      if (cookies != null) {
         for (int f = 0; f < cookies.length; f++) {
            Cookie c = cookies[f];

            if (c.getName().equals(name)) {
               return c.getValue();
            }
         }
      }

      return null;
   }

   public String[] getFilterValues(HttpServletRequest request) {
      String[] filterValues = request.getParameterValues(FILTER_NAME);
      String cookieName = getCookieName(id);

      if (filterValues == null || filterValues.length == 0) {
         String cookieValue = getCookieValue(cookieName, request);
         if (cookieValue != null) {
            return cookieValue.split(COOKIE_SEP_SPLIT);
         }
      }

      return filterValues;
   }

   public String getFilterName(HttpServletRequest request) {
      String filterName = request.getParameter(FILTER_NAME);
      String cookieName = getCookieName(id);

      if (filterName == null || filterName.length() == 0) {
         return getCookieValue(cookieName, request);
      }
      return filterName;
   }

   protected int getPageSize(HttpServletRequest request, HttpServletResponse response){
      int perPage = -1;
      boolean foundPageSize = true;

      if (request.getParameter("sroll_size") != null) {
         try {
            perPage = Integer.parseInt((String) request.getParameter("sroll_size"));
         } catch (NumberFormatException nfe) {
            logger.error(nfe.getMessage(), nfe);
         }
      }

      if (perPage == -1) {
         //then the previous page (current or not) did not have the drop down
         //so lets look at the Cookie
         perPage = getPageSizeInCookie(request);
         if (perPage == -1) {
            perPage = 10;
            foundPageSize = false;
         }
      }

      if (foundPageSize) {
         setPageSizeCookie(perPage, request, response);
      }

      return perPage;
   }

   protected int getStartingIndex(HttpServletRequest request, HttpServletResponse response, Map model, int perPage, int total) {
      int startingIndex = 0;

      String ensureVisible = request.getParameter(ListScroll.ENSURE_VISIBLE_TAG);

      if (ensureVisible != null) {
         int visibleIndex = Integer.parseInt(ensureVisible);
         int startingPage = (visibleIndex / perPage);
         startingIndex = startingPage * perPage;
      } else {
         String newStart = request.getParameter(ListScroll.STARTING_INDEX_TAG);

         if (newStart != null) {
            startingIndex = Integer.parseInt(newStart);
            if (startingIndex < 0) {
               startingIndex = 0;
            }
         }
      }

      if (startingIndex > total) {
         int lastPage = (int) Math.ceil(((double) total) / ((double) perPage));
         lastPage--;
         startingIndex = lastPage * perPage;
      }

      int endingIndex = startingIndex + perPage;

      if (endingIndex > total) {
         endingIndex = total;
      }

      ListScroll ls = new ListScroll(perPage, total, startingIndex);
      //ls.setPerPage((foundPageSize ? perPage : -1));

      model.put("listScroll", ls);

      return startingIndex;
   }

   public List filterList(HttpServletRequest request, HttpServletResponse response, Map model, List list) {
      List filterList = (isApplyFilter()) ? new ArrayList(getFilters().keySet()) : getFilterNames();
      model.put(FILTERS_ATTRIBUTE_KEY, filterList);
      model.put(ID_ATTRIBUTE_KEY, id);


      String filterName = getFilterName(request);
      String cookieName = getCookieName(id);

      if (filterName != null && filterName.equals(FILTER_RESET)) {
         Cookie[] cookies = request.getCookies();

         for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(cookieName)) {
               cookies[i].setMaxAge(0);
               response.addCookie(cookies[i]);
               break;
            }
         }
         return list;
      }


      if (filterName == null || !filterList.contains(filterName)) {
         return list;
      }

      response.addCookie(createNewCookie(cookieName,getFilterValues(request)));

      //only apply filter if directed to do so,
      if (this.isApplyFilter()) {
         Date startDate = new Date();
         Filter filter = (Filter) getFilters().get(filterName);
         List filteredList = new FilteredList(list, filter);
         logger.debug("filter took " + String.valueOf(new Date().getTime() - startDate.getTime()) + " millis");
         return filteredList;
      }
      return list;
   }

   protected Cookie createNewCookie(String cookieName, String[] filterValues) {
      StringBuffer buf = new StringBuffer();
      for (int i=0;i<filterValues.length;i++){
         if (i>0){
            buf.append(COOKIE_SEP);
         }
         buf.append(filterValues[i]);
      }
      Cookie cookie = new Cookie(cookieName, buf.toString());

      return cookie;
   }

   protected int getPageSizeInCookie(HttpServletRequest request) {


      Cookie[] cookies = request.getCookies();

      int pageSize = -1;

      if (cookies != null) {
         for (int f = 0; f < cookies.length; f++) {
            Cookie c = cookies[f];

            if (c.getName().equals("SCROLL_LIST_PAGE_SIZE")) {

               pageSize = Integer.parseInt(c.getValue());

            }
         }
      }
      return pageSize;

   }


   protected void setPageSizeCookie(int val, HttpServletRequest request, HttpServletResponse response) {

      Cookie scrollCookie = new Cookie("SCROLL_LIST_PAGE_SIZE", String.valueOf(val));
      scrollCookie.setMaxAge(100000000);

      response.addCookie(scrollCookie);

   }

   protected String getCookieName(String id) {
      return FILTER_NAME + id;
   }


   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   /**
    * Default implementation returns null. Subclasses can override this to set reference data used in the view.
    *
    * @param httpServletRequest
    * @return a Map with reference data entries, or null if none
    */
   public Map referenceData(HttpServletRequest httpServletRequest) {
      return null;
   }

   public String getListName() {
      return listName;
   }

   public void setListName(String listName) {
      this.listName = listName;
   }

   public String getSuccessView() {
      return successView;
   }

   public void setSuccessView(String successView) {
      this.successView = successView;
   }


   public Map getFilters() {
      return filters;
   }

   public void setFilters(Map filters) {
      this.filters = filters;
   }

   public boolean isApplyFilter() {
      return applyFilter;
   }

   /**
    * set this to false if you do not want the listfilterer to apply the filter.  This
    * means the getList() impl will have to take care of that.  You can use the getFilterName() in the
    * list to get the selected filter out of the request
    *
    * @param applyFilter
    */
   public void setApplyFilter(boolean applyFilter) {
      this.applyFilter = applyFilter;
   }

   public List getFilterNames() {
      return filterNames;
   }

   public void setFilterNames(List filterNames) {
      this.filterNames = filterNames;
   }


   public String getDefaultSortCol() {
      return defaultSortCol;
   }

   public void setDefaultSortCol(String defaultCol) {
      this.defaultSortCol = defaultCol;
   }

   public String getDefaultSortOrder() {
      return defaultSortOrder;
   }

   public void setDefaultSortOrder(String defaultSortOrder) {
      this.defaultSortOrder = defaultSortOrder;
   }

   public boolean isEnableSorting() {
      return enableSorting;
   }

   public void setEnableSorting(boolean enableSorting) {
      this.enableSorting = enableSorting;
   }

   public String getCurrentSortCol() {
      return (String) currentSortCol.get();
   }

   public void setCurrentSortCol(String currentSortCol, HttpServletResponse reponse) {
      this.currentSortCol.set(currentSortCol);
      reponse.addCookie(new Cookie("sortColumn_" + getClass().getPackage().getName() + "." + getClass().getName(), currentSortCol));
   }

   public String getCurrentSortOrder() {
      return (String) currentSortOrder.get();
   }

   public void setCurrentSortOrder(String currentSortOrder, HttpServletResponse reponse) {
      this.currentSortOrder.set(currentSortOrder);
      reponse.addCookie(new Cookie("sortOrder_" + getClass().getPackage().getName() + "." + getClass().getName(), currentSortOrder));
   }

   /**
    * @return true if current sort order is "asc"
    */
   protected boolean isAscendingOrder() {
      if (getCurrentSortOrder() == null || getCurrentSortOrder().equalsIgnoreCase("asc")) {
         return true;
      }
      return false;
  }
}
