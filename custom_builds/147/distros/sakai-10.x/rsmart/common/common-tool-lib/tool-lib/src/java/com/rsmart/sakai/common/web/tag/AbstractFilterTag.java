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
package com.rsmart.sakai.common.web.tag;

import com.rsmart.sakai.common.web.listfilter.ViewListController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.metaobj.utils.mvc.intf.ListScroll;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 *
 */
abstract public class AbstractFilterTag extends AbstractLocalizableTag {
   public final static String ELEMENT_NAME_LIST = "filterlist.elementNames.list";

   protected final transient Log logger = LogFactory.getLog(getClass());

   private String  filterUrl;
   private String  className;
   private String  filterButtonKey = "filter_button";
   private boolean showFilterButton = true;

   // some sort of id is needed to tie the tag to a cookie, generally this would be the controller name
   private String id;




   protected int doStartTagInternal() throws Exception {
      JspWriter writer = pageContext.getOut();

      if (id == null)
         id = (String) pageContext.getRequest().getAttribute(ViewListController.ID_ATTRIBUTE_KEY);

      try {
         if (!(getParent() instanceof FilterGroupTag) || this instanceof FilterGroupTag) {
            writer.write("<div");
            if (className != null) {
               writer.write(" class=\"" + className + "\"");
            }
            writer.write(">\n");
         }
         if (!(this instanceof FilterGroupTag)) {
            if (pageContext.getAttribute(ELEMENT_NAME_LIST) == null) {
               pageContext.setAttribute(ELEMENT_NAME_LIST, new ArrayList());
            }

            List elementNames = (List) pageContext.getAttribute(ELEMENT_NAME_LIST);

            String elementName = "filterList_" + elementNames.size();

            elementNames.add(elementName);
            if (!(getParent() instanceof FilterGroupTag)) {
               writeJavascript();
               writeInput(elementName);
               if (showFilterButton)
                  writeFilterButton();
               writer.write("</div>\n");
//             writer.write("<br/>\n");
            } else {
               writeInput(elementName);
            }
         }
      }
      catch (IOException e) {
         logger.error("", e);
         throw new JspException(e);
      }
      return EVAL_PAGE;
   }

   protected String[] getElementNames(){
      List elementNames = (List) pageContext.getAttribute(ELEMENT_NAME_LIST);
      return (String[]) elementNames.toArray(new String[elementNames.size()]);
   }

   protected void writeJavascript() throws IOException, JspException {
      JspWriter writer = pageContext.getOut();

      String[] elementNames = getElementNames();

      writer.write("<script>\n");
      writer.write("   function getFilterUrl(elementNames) {\n");

      writer.write("      var selected = new Array(" + elementNames.length + ");\n\n");

      for (int i = 0; i<elementNames.length;i++){
         writer.write("      selected[" + i + "] = ospGetElementById('" + elementNames[i] + "');\n");
      }

      // write out the filter url
      if (filterUrl == null) {
         throw new JspException("filterUrl is required when tag is not surrounded by a filterGroup tag");
      }
      writer.write("      window.document.location='" + filterUrl + "&" + ListScroll.STARTING_INDEX_TAG + "=0'");

      for (int i = 0; i<elementNames.length;i++){
         writer.write("+ '&" + ViewListController.FILTER_NAME + "=" + "' + selected[" + i + "].value");
      }
      writer.write(";\n");
      writer.write("   }\n");

      // if there won't be a button to click on to submit the filter, then we need to write the javascript which
      // will automatically submit the filter whenever the user selects a new filter value.
      if (!showFilterButton) {
         StringBuffer buffer = new StringBuffer();

         buffer.append("new Array(");
         for (int i = 0; i<elementNames.length;i++){
            buffer.append(i > 0 ? "," : "");
            buffer.append("'" + elementNames[i] + "'");
         }
         buffer.append(")");

         writer.write("   \n");
         writer.write("   function submitFilter() {\n");
         writer.write("      getFilterUrl(" + buffer.toString() + ");\n");
         writer.write("   }\n");
      }
      writer.write("</script>\n\n");
   }

   abstract protected void writeInput(String elementName) throws IOException, JspException;

   protected void writeFilterButton() throws IOException, JspException {
      JspWriter writer = pageContext.getOut();
      String filterButtonName = resolveMessage(getFilterButtonKey());
      String[] elementNames = getElementNames();

      writer.write("&nbsp;");
      StringBuffer buf = new StringBuffer();
      buf.append("new Array(");
      for (int i = 0; i<elementNames.length;i++){
         if (i > 0) {
            buf.append(",");
         }
         buf.append("'" + elementNames[i] + "'");
      }
      buf.append(")");
      writer.write("<input type=\"button\" value=\"" + filterButtonName + "\" onclick=\"getFilterUrl(" + buf.toString() + ")\">\n");
   }

   /**
    * Default processing of the end tag returning EVAL_PAGE.
    *
    * @return EVAL_PAGE
    * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
    * @see javax.servlet.jsp.tagext.Tag#doEndTag
    */

   public int doEndTag() throws JspException {

      filterUrl = null;
      id = null;

      if (getParent() instanceof FilterGroupTag && !(this instanceof FilterGroupTag)){
         ((FilterGroupTag) getParent()).incrementCount();
      }

      return EVAL_PAGE;
   }

   protected boolean isResetSelected(ServletRequest request) {
      if (request.getParameter(ViewListController.FILTER_NAME) != null &&
            request.getParameter(ViewListController.FILTER_NAME).equals(ViewListController.FILTER_RESET)) {
         return true;
      }
      return false;
   }

   protected int getCookieValueIndex(){
      if (getParent() instanceof FilterGroupTag){
         return ((FilterGroupTag) getParent()).getFilterCount();
      }
      return 0;
   }

   protected String getRequestFilterValue(HttpServletRequest httpRequest){
      String[] values = httpRequest.getParameterValues(ViewListController.FILTER_NAME);
      if (values == null || values.length < getCookieValueIndex()){
         return null;
      }
      return values[getCookieValueIndex()];
   }

   protected String getCookieFilterValue(HttpServletRequest httpRequest){
      Cookie[] cookies =  httpRequest.getCookies();
      for (int i=0;i<cookies.length; i++) {
         if (cookies[i].getName().equals(ViewListController.FILTER_NAME + id)){
            String[] cookieValues = cookies[i].getValue().split("\\" + ViewListController.COOKIE_SEP);
            if (cookieValues.length < getCookieValueIndex()){
               return null;
            }
            return cookieValues[getCookieValueIndex()];
         }
      }
      return null;
   }

   protected String getCurrentValue(ServletRequest request) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      if (isResetSelected(request)) {
         return null;
      }

      // first check request parameters
      String requestValue = getRequestFilterValue(httpRequest);
      if (requestValue != null) {
         return requestValue;
      }
      // don't check cookie if we have a request param
      if (requestValue != null) {
         return null;
      }

      // next check cookie for value
      return getCookieFilterValue(httpRequest);
   }


   protected boolean isSelected(String filterValue, ServletRequest request) {
      String currentValue = getCurrentValue(request);
      return (currentValue != null && currentValue.equals(filterValue));
   }

   /**
    * Release state.
    *
    * @see javax.servlet.jsp.tagext.Tag#release
    */

   public void release() {
      super.release();
      filterUrl = null;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setFilterUrl(String filterUrl) {
      this.filterUrl = filterUrl;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getFilterButtonKey() {
      return filterButtonKey;
   }

   public void setFilterButtonKey(String filterButtonKey) {
      this.filterButtonKey = filterButtonKey;
   }

   public boolean getShowFilterButton() {
      return showFilterButton;
   }

   public void setShowFilterButton(boolean showFilterButton) {
      this.showFilterButton = showFilterButton;
   }

   public String getFilterUrl() {
      return filterUrl;
   }

   public String getClassName() {
      return className;
   }
}
