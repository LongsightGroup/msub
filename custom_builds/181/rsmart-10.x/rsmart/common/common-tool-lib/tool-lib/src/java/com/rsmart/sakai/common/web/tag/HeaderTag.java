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

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.Validator;



/**
 * This tag generates content for a common header.jsp.
 * All jsp pages need to include a header.jsp, which contains:
 * <ul>
 *    <li>html, head, body tags</li>
 *    <li>character encoding</li>
 *    <li>stylesheets</li>
 *    <li>javascript</li>
 *    <li>tag library includes</li>
 * <ul>
 *
 * This tag generates the header content.
 */
public class HeaderTag extends TagSupport
{
   // tag attributes
   private boolean generateOpeningBodyTag = true;




   /**
    * generates the html code for the header content.
    * <br/><br/>
    * @throws JspException   if an error occurrs while writing the html out.
    */
   public int doStartTag() throws JspException {

      StringBuffer       buffer  = new StringBuffer();
      HttpServletRequest request = getRequest();
      JspWriter          out     = getJspWriter();
      String             panel   = (request.getParameter("panel") == null ? "Main" + ToolManager.getCurrentPlacement().getId() : request.getParameter("panel"));
      String             title   = (request.getAttribute("_title") == null ? ToolManager.getCurrentTool().getTitle() : (String) request.getAttribute("_title"));

      buffer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">                                                  \n");
      buffer.append("<head>                                                                                                                     \n");
      buffer.append("    <meta http-equiv=\"Content-Type\"       content=\"text/html; charset=UTF-8\" />                                        \n");
      buffer.append("    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\"                 />                                        \n");
      buffer.append("    <link href=\"/osp-common-tool/css/eport.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\"/>                     \n");
      buffer.append("    <link href=\"" + request.getAttribute("sakai_skin_base") + "\"    type=\"text/css\" rel=\"stylesheet\" media=\"all\"/> \n");
      buffer.append("    <link href=\"" + request.getAttribute("sakai_skin")      + "\" type=\"text/css\" rel=\"stylesheet\" media=\"all\"/>    \n");
      buffer.append("    <title>" + title + "</title>                                                                                           \n");
      buffer.append("    <script type=\"text/javascript\" language=\"javascript\" src=\"/library/js/headscripts.js\"  ></script>                \n");
      buffer.append("    <script type=\"text/javascript\" language=\"javascript\" src=\"/osp-common-tool/js/eport.js\"></script>                \n");
      buffer.append("</head>                                                                                                                    \n");
      buffer.append("\n");
      buffer.append("<script language=\"javascript\">                                         \n");
      buffer.append("    function resetHeight() {                                             \n");
      buffer.append("       setMainFrameHeight(\"" + Validator.escapeJavascript(panel) + "\");\n");
      buffer.append("    }                                                                    \n");
      buffer.append("                                                                         \n");
      buffer.append("    function loaded() {                                                  \n");
      buffer.append("       resetHeight();                                                    \n");
      buffer.append("       parent.updCourier(doubleDeep, ignoreCourier);                     \n");
      buffer.append("       if (parent.resetHeight)                                           \n");
      buffer.append("          parent.resetHeight();                                          \n");
      buffer.append("    }                                                                    \n");
      buffer.append("</script>                                                                \n");
      buffer.append("\n");
      if (generateOpeningBodyTag) {
         buffer.append("<body onload=\"loaded();\">   \n");
         buffer.append("   <div class=\"portletBody\">\n");
      }

      try {
         out.print(buffer.toString());
      } catch (IOException ex) {
         ex.printStackTrace();
         throw new JspTagException(ex.getMessage());
      }
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

   public void setGenerate_opening_body_tag(boolean flag) {
      generateOpeningBodyTag = flag;
   }
}
