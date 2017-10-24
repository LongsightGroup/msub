<%--
  ~ Copyright 2011 The rSmart Group
  ~
  ~ The contents of this file are subject to the Mozilla Public License
  ~ Version 1.1 (the "License"); you may not use this file except in
  ~ compliance with the License. You may obtain a copy of the License at
  ~ http://www.mozilla.org/MPL/
  ~
  ~ Software distributed under the License is distributed on an "AS IS"
  ~ basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing rights and limitations
  ~ under the License.
  ~
  ~ Contributor(s): duffy
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%
		response.setContentType("text/html; charset=UTF-8");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link href="<c:out value="${sakai_skin_base}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <link href="<c:out value="${sakai_skin}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title><%= org.sakaiproject.tool.cover.ToolManager.getCurrentTool().getTitle()%></title>
    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
    </script>
      <%
          String panelId = request.getParameter("panel");
          if (panelId == null) {
             panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
          }

      %>

    <script type="text/javascript" >
       function resetHeight() {
          setMainFrameHeight('<%= org.sakaiproject.util.Validator.escapeJavascript(panelId)%>');
       }

       function loaded() {
          resetHeight();
          parent.updCourier(doubleDeep, ignoreCourier);
          if (parent.resetHeight) {
             parent.resetHeight();
          }
       }
    </script>
    <link rel="StyleSheet" href="css/dtree.css" type="text/css" />
    <script type="text/javascript" src="js/dtree.js"></script>

  </head>

  <body onload="loaded();">
    <div class="portletBody">
      <h3>Error Connecting to Google Document</h3>
      <p class="instruction indnt2">
        An error was encountered connecting or communicating with Google Documents.
      </p>

    </div>
  </body>
</html>
