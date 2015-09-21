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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%
		response.setContentType("text/html; charset=UTF-8");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/css/metaobj.css"/>" />
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

        <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js"></script>
        <script language="JavaScript" src="/osp-common-tool/js/eport.js"></script>

        <%
            String panelId = request.getParameter("panel");
            if (panelId == null) {
                panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
            }
        %>

        <script language="javascript">
        function resetHeight() {
            setMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>');
        }

        function loaded() {
            resetHeight();
            parent.updCourier(doubleDeep, ignoreCourier);
            if (parent.resetHeight) {
                parent.resetHeight();
            }
        }
        function createUrl(UrlAdd){

            var initialUrl =  UrlAdd ;
            top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = initialUrl;
            window.location = initialUrl;
        }

        function editProvider(UrlAdd){
            var editValue;
            editValue = document.getElementById("uuid").value;
            var Url = UrlAdd + "?uuid=" + editValue;
            top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = Url;
            window.location = Url;
        }
        </script>
    </head>

    <body onload="loaded();">
        <div class="portletBody">
            <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>

            <div class="navIntraTool">
                <a href="" onclick="createUrl('oauthDisplayProviders.form'); return false;">List</a>
            </div>

            <c:if test="${not empty msg}">
            <div class="alertMessage">${msg}</div>
            </c:if>

            <c:if test="${not empty error}">
            <div class="alertMessage">${error}</div>
            </c:if>