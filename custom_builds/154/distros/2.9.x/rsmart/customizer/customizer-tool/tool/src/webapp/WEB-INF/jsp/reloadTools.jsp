<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%
		response.setContentType("text/html; charset=UTF-8");
%>

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>


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
    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
    </script>
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
  </script>
  </head>

  <body onload="loaded();">
      <div class="portletBody">
         <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>

<div class="navIntraTool">
    <a href="customizer.osp">
      <%= org.sakaiproject.tool.cover.ToolManager.getCurrentTool().getTitle()%>
    </a>
</div>

<c:if test="${not empty msg}">
<div class="messageSuccess">${msg}</div>
</c:if>
<c:if test="${not empty error}">
<div class="messageError">${error}</div>
</c:if>



<h3>
    <spring:message code="reload_tools_title"/>
</h3>

<div class="instruction">
    <spring:message code="reload_tool_instructions"/>
</div>

<form action="reloadTools.osp" name="downloadForm" method="POST">
    <input type="submit" name="_target1" class="active" value="<spring:message code='button_process'  />"/>
    <input type="submit" name="_cancel" value="<spring:message code='button_cancel'/>"/>
</form>


                      <c:if test="${not empty requestScope.panelId}"></div></c:if>
      </div>
   </body>
</html>