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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;



/**
 *
 */
public class FilterTextBoxTag extends AbstractFilterTag {
   private String size;
   private String maxlength;
   protected void writeInput(String elementName) throws IOException, JspException {
      JspWriter writer = pageContext.getOut();
      writer.write("<input type=\"text\" id=\"" + elementName +  "\" name=\"" + elementName);
      if (size != null && size.length() > 0) {
         writer.write(" size=\"" + size + "\"");
      }
      if (maxlength != null && maxlength.length() > 0) {
         writer.write(" maxlength=\"" + maxlength + "\"");
      }
      String value = getCurrentValue(pageContext.getRequest());
      if (value == null) {
         value = "";
      }
      writer.write(" value=\"" + value + "\"");
      writer.write("\">");
   }

   public String getSize() {
      return size;
   }

   public void setSize(String size) {
      this.size = size;
   }

   public String getMaxlength() {
      return maxlength;
   }

   public void setMaxlength(String maxlength) {
      this.maxlength = maxlength;
   }
}
