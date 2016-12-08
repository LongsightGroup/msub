<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ page import="com.rsmart.content.google.helper.CommandBean" %>

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

          CommandBean
              cb = (CommandBean)request.getAttribute("command");

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
  </head>

  <body onload="loaded();">
    <div class="portletBody">
      <h3><spring:message code="instructions.selectWorksheet.title"/></h3>
      <p class="instruction indnt2">
          <spring:message code="instructions.selectWorksheet" arguments="${command.worksheetCount}"/>
      </p>
      <form:form>
          <spring:message code='label.selectWorksheet'/>

          <spring:bind path="command.worksheet">
              <select name="${status.expression}" id="${status.expression}">
              <%

                  for (int i = 0; i < cb.getWorksheetCount(); i++)
                  {
                      int option = i + 1;
                      %>
                      <c:choose>
                          <c:when test="${option == status.value}">
                              <option value="<%=option%>" selected="selected">
                                  <%=option%>
                              </option>
                          </c:when>
                          <c:otherwise>
                              <option value="<%=option%>">
                                  <%=option%>
                              </option>
                          </c:otherwise>
                      </c:choose>
                      <%
                  }
              %>
               </select>
          </spring:bind><br/>
          <input type="submit" name="_cancel" value="<spring:message code='cancel'/>"/>
          <input type="submit" name="_target1" value="<spring:message code='previous'/>"/>
          <input type="submit" name="_target3" value="<spring:message code='next'/>"/>
      </form:form>
    </div>
  </body>
</html>
