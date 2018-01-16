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



/**
 *
 */
public class FilterGroupTag extends AbstractFilterTag {
   private int filterCount = 0;
   private String clearButtonKey = "clear_button";
   private boolean showClearButton = false;

   public int getFilterCount(){
      return filterCount;
   }

   public void incrementCount(){
      filterCount++;
   }

   public int doEndTag() throws JspException {
      JspWriter writer = pageContext.getOut();

      try {
         writeJavascript();
         writeFilterButton();
         writeFilterClearButton();
         writer.write("</div>");
         writer.write("<br/>");
      } catch (IOException e) {
         throw new JspException("", e);
      }
      filterCount = 0;
      return EVAL_PAGE;
   }

   protected void writeFilterClearButton() throws IOException, JspException {
      if (isShowClearButton()) {
         JspWriter writer = pageContext.getOut();
         String clearButtonName = resolveMessage(getClearButtonKey());
         String onClick = ("\nwindow.document.location='" + getFilterUrl() + "&" + ViewListController.FILTER_NAME + "=" + ViewListController.FILTER_RESET + "'");
         writer.write("<input type=\"button\" value=\"" + clearButtonName + "\" onclick=\"" + onClick + "\">");
      }
   }

   protected void writeInput(String elementName) throws IOException, JspException {
      // left blank on purpose
   }


   public String getClearButtonKey() {
      return clearButtonKey;
   }

   public void setClearButtonKey(String clearButtonKey) {
      this.clearButtonKey = clearButtonKey;
   }

   public boolean isShowClearButton() {
      return showClearButton;
   }

   public void setShowClearButton(boolean showClearButton) {
      this.showClearButton = showClearButton;
   }
}
