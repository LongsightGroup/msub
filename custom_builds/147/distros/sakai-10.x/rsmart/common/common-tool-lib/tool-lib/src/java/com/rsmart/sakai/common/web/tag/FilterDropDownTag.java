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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 *
 */
public class FilterDropDownTag extends AbstractFilterTag {
   private List filters = new ArrayList();
   private boolean showAllFilter = false;
   private boolean localizeValues = true;

    protected void writeInput(String elementName) throws IOException, JspException {
      JspWriter writer = pageContext.getOut();

      writer.write("<select id=\"" + elementName +  "\" name=\"" + elementName + "\"");
      if (!getShowFilterButton()) {
         writer.write(" onchange=\"submitFilter();\"");
      }
      writer.write(">\n");
      if (isShowAllFilter()) {
         writer.write("   <option value=\"-1\" " + isResetSelected(pageContext.getRequest()) + ">-all-</option>\n");
      }
      Iterator i = getFilters().iterator();
      while (i.hasNext()) {
         String filterName = (String) i.next();
         String localizedName = (localizeValues) ? resolveMessage(filterName) : filterName;
         if (isSelected(filterName, pageContext.getRequest())){
            writer.write("   <option selected=\"selected\" value=\"" + filterName + "\">" + localizedName + "</option>\n");
         } else {
            writer.write("   <option value=\"" + filterName + "\">" + localizedName + "</option>\n");
         }
      }
      writer.write("</select>\n");
   }

   public boolean isShowAllFilter() {
      return showAllFilter;
   }

   public void setShowAllFilter(boolean showAllFilter) {
      this.showAllFilter = showAllFilter;
   }

   public int doEndTag() throws JspException {
      filters = null;
      return super.doEndTag();
   }

   protected int doStartTagInternal() throws Exception {
      if (filters == null) {
         filters = (List) pageContext.getRequest().getAttribute(ViewListController.FILTERS_ATTRIBUTE_KEY);
      }
      return super.doStartTagInternal();
   }

   public List getFilters() {
      return filters;
   }

   public void setFilters(List filters) {
      this.filters = filters;
   }

    public boolean isLocalizeValues() {
        return localizeValues;
    }

    public void setLocalizeValues(boolean localizeValues) {
        this.localizeValues = localizeValues;
    }
}
