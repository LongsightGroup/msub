<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
    <a href="messageBundleHome.osp">
      <spring:message code='mbp_list_action'/>
    </a>
   <a href="messageBundle.osp?method=showDownloadForm">
     <spring:message code='mbp_download_action'/>
   </a>
   <a href="uploadMessageBundle.osp">
      <spring:message code='mbp_upload_action'/>
   </a>
    <a href="messageBundle.osp?method=showRevertAllForm">
       <spring:message code='mbp_revertall_action'/>
    </a>
</div>

<c:if test="${not empty msg}">
<div class="messageSuccess">${msg}</div>
</c:if>
<c:if test="${not empty error}">
<div class="messageError">${error}</div>
</c:if>

