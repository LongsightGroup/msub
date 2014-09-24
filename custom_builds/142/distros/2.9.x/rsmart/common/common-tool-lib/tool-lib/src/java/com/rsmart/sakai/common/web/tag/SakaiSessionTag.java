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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;



/**
 * This tag places the sakai session attribute specified by this tag's <i>name</i> attribute into the http session.
 */
public class SakaiSessionTag extends TagSupport
{
   // tag attributes
   private String name = null;      // name of sakai session attribute to place in the http session




   /**
    * places the specified sakai session attribute in the http session.
    * <br/><br/>
    * @throws JspException   if an error occurrs.
    */
   public int doStartTag() throws JspException {

      HttpSession httpSession  = getSession();
      Session     sakaiSession = SessionManager.getCurrentSession();
      Object      attribute    = sakaiSession.getAttribute(name);

      if (attribute != null)
         httpSession.setAttribute(name, attribute);
      return(SKIP_BODY);
   }

   /**
    * returns the http request.
    */
   public HttpServletRequest getRequest() {
      return((HttpServletRequest)pageContext.getRequest());
   }

   /**
    * returns the http response.
    */
   public HttpServletResponse getResponse() {
      return((HttpServletResponse)pageContext.getResponse());
   }

   /**
    * returns the servlet configuration.
    */
   public ServletConfig getServletConfig() {
      return(pageContext.getServletConfig());
   }

   /**
    * returns the http session.
    */
   public HttpSession getSession() {
      return(getRequest().getSession());
   }

   /**
    * returns the jsp writer.
    */
   public JspWriter getJspWriter() {
      return(pageContext.getOut());
   }

   /**
    * called by the jsp container when it encounters the <i>name</i> attribute.
    */
   public void setAttribute(String name) {
      this.name = name;
   }
}
