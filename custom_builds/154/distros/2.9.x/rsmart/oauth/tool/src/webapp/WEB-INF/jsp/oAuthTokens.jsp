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
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<%@ include file="/WEB-INF/jsp/include.jsp" %>

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
        <script language="JavaScript" src="/osp-common-tool/js/eport.js"></script>
        <%
            String panelId = request.getParameter("panel");
            if (panelId == null)
            {
                panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
            }
        %>

        <script language="javascript">
            function resetHeight()
            {
                setMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>');
            }

            function loaded()
            {
                resetHeight();
                parent.updCourier(doubleDeep, ignoreCourier);
                if (parent.resetHeight)
                {
                    parent.resetHeight();
                }
            }

            function createLink (UrlAdd)
            {
                var initialUrl =  UrlAdd ;
                top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = initialUrl;
                window.location = initialUrl;
            }

            //parsing the values to separate the id and the provider name....provider name is added to the window.alert
            function parseValues(UrlAdd)
            {
                var checkedNames = new Array();
                var checkedIds = new Array();
                var checkBoxValue ="";
                var selectedMembers=document.getElementsByName("uuid");

                for (var i=0; i < selectedMembers.length; i++)
                {
                    if (selectedMembers[i].checked == true)
                    {
                        checkBoxValue = selectedMembers[i].value;
                        var myStringList = checkBoxValue.split(',');

                        checkedIds[i] = myStringList[0];
                        checkedNames[i]=myStringList[1];
                    }//end of if statement
                }//end of for loop
                submitForm(UrlAdd, checkedIds, checkedNames);
            }

            //Submit form
            function submitForm(Url,idArray,nameArray)
            {
                var nameStr, delim="";

                for (var i = 0; i < nameArray.length; i++)
                {
                    nameStr = "\t" + nameArray[i] + delim;
                    delim = "\n";
                }

                if(!window.confirm('<spring:message code="label.message.delete.token.begin"/>\n\n' + nameStr + '\n\n<spring:message code="label.message.delete.token.end"/>'))
                    return;

                var deleteUrl = Url +"?uuid=" + idArray;

                top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = deleteUrl;
                window.location = deleteUrl;
            }
        </script>
    </head>

    <body onload="loaded();">
        <div class="portletBody">
            <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>

            <div class="navIntraTool">
                <a href="" onclick="parseValues('oAuthTokens.form'); return false;">
                    Delete
                </a>
            </div>

            <c:if test="${not empty msg}">
            <div class="alertMessage">${msg}</div>
            </c:if>
            <c:if test="${not empty error}">
            <div class="alertMessage">${error}</div>
            </c:if>

            <form:form name="providerListForm" commandName="oAuthTokenAdminState">
                <font color="red"></font>
                <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List"
                        style="font-size:8pt">
                    <thead>
                        <tr>
                            <th scope="col"></th>
                            <th scope="col"><spring:message code="label.table.provider.name.header"/> </th>
                            <th scope="col"><spring:message code="label.table.provider.description.header"/></th>
                        </tr>
                    </thead>
                    <tbody>
                      <c:forEach var="token" items="${oAuthTokenAdminState.userTokens}" begin="0" >
                        <tr>
                            <td>
                                <input type="hidden" value="${token.provider.providerName}" name="pname" />
                                <input type="checkbox" name="uuid" value="${token.UUID},${token.provider.providerName}" />
                            </td>
                            <td><c:out value="${token.provider.providerName}"/></td>
                            <td><c:out value="${token.provider.description}"/></td>
                        </tr>
                      </c:forEach>
                    </tbody>
                </table>
            </form:form>
        </div>
    </body>
</html>
