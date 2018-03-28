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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rsmart.sakai.common.web.listfilter.ViewListController;



/**
 * this tag displays a column header and then the sakai sort control (an up or down arrow) directly next to it.
 */
public class ListSortTag extends AbstractLocalizableTag {
	 protected final transient Log logger = LogFactory.getLog(getClass());

    // class mnemonics
	 public static String CURRENT_SORTED_COLUMN = "current_sorted_column";
    public static String SCROLL_ORDER          = "sroll_order";
    public static String SCROLL_ORDER_ASC      = "asc";
    public static String SCROLL_ORDER_DESC     = "desc";

    // data members
    private String className;
    private String displayName;
    private String name;
    private String sortUrl;



	/**
    * Default processing of the start tag returning EVAL_BODY_BUFFERED.
    *
    * @return EVAL_BODY_BUFFERED
    * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
    * @see javax.servlet.jsp.tagext.BodyTag#doStartTag
    */

   protected int doStartTagInternal() throws Exception {
      JspWriter writer         = pageContext.getOut();
      boolean   sortingEnabled = ((Boolean) pageContext.getRequest().getAttribute(ViewListController.SORT_ENABLED)).booleanValue();

      // write <div> html tag
      writer.write("<div");
      if (className != null) {
         writer.write(" class=\"" + className + "\"");
      }
      writer.write(">");
      writer.newLine();

      // display text and the sorting control next to it
      if (sortingEnabled) {
         String currCol           = (String)pageContext.getRequest().getAttribute(ViewListController.SORT_COLUMN_PARM);
         String currOrder         = (String)pageContext.getRequest().getAttribute(ViewListController.SORT_ORDER_PARM);
         String sortIcon          = "";
         String nextSortDirection = SCROLL_ORDER_ASC;

         if (this.name.equalsIgnoreCase(currCol)) {
            sortIcon = "<img src=\"/sakai-gradebook-tool/images/sortdescending.gif\">";

            if (currOrder.equalsIgnoreCase(SCROLL_ORDER_ASC)) {
               sortIcon="<img src=\"/sakai-gradebook-tool/images/sortascending.gif\">";
               nextSortDirection = SCROLL_ORDER_DESC;
            }
         }
         String text = resolveMessage(displayName);
         writer.write("<a href=\""+sortUrl+"&"+ViewListController.SORT_COLUMN_PARM+"="+this.name+"&"+ViewListController.SORT_ORDER_PARM+"="+nextSortDirection+"\">" + text + "&nbsp;"+sortIcon+"</a>");
      } else {
         // just throw the text out there!
         writer.write(displayName);
      }
      writer.newLine();
      writer.write("</div>");

      return EVAL_PAGE;
   }

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDisplayName() {
		return displayName;
	}

   public void setDisplayName(String displayName) throws JspException {
      this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSortUrl() {
		return sortUrl;
	}

	public void setSortUrl(String sortUrl) {
		this.sortUrl = sortUrl;
	}
}
