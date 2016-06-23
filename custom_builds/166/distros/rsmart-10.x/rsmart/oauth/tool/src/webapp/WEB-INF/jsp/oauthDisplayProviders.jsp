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

<%--
  Created by IntelliJ IDEA.
  User: lmaxey
  Date: Mar 26, 2010
  Time: 10:33:23 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>




<form:form name="providerListForm" commandName="oAuthAdminToolState">
 <body>
 <font color="red"><c:out value="${oAuthAdminToolState.providerErrors.checkBoxCheckedErrorMessage}" /></font>
 <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List" style="font-size:8pt">
       <thead>
         <tr>
           <th scope="col"></th>
           <th scope="col"><spring:message code="label.table.provider.enabled.header"/> </th>
           <th scope="col"><spring:message code="label.table.provider.name.header"/> </th>
           <c:if test="${not oAuthAdminToolState.isOAuth2Enabled}">
               <th scope="col"><spring:message code="label.table.provider.realm.header"/></th>
           </c:if>
           <th scope="col"><spring:message code="label.table.provider.description.header"/></th>
           <th scope="col"></th>
    </tr>
       </thead>
       <tbody>
        <c:forEach var="provider" items="${oAuthAdminToolState.providers}" begin="0" >
            <tr>
              <td><input type="checkbox" name="uuid" value="${provider.UUID},${provider.providerName}"/></td>
              <td>
                  <c:choose>
                    <c:when test="${provider.enabled}">
                      <spring:message code="enabled"/>
                    </c:when>
                    <c:otherwise>
                        <font color="red">
                            <spring:message code="disabled"/>
                        </font>
                    </c:otherwise>
                  </c:choose>
              </td>
              <td><c:out value="${provider.providerName}"/></td>
              <c:if test="${not oAuthAdminToolState.isOAuth2Enabled}">
                  <td><c:out value="${provider.realm}"/></td>
              </c:if>
              <td><c:out value="${provider.description}"/></td>
              <td><a href="" onclick="addUrl('showAllDataForProvider.form?uuid=<c:out value="${provider.UUID}"/>'); return false;">
                  <spring:message code="label.link.view.full.record"/> </a> </td>
          </tr>
      </c:forEach>
    </tbody>
    </table>
 </div>
 </body>
</form:form>

</html>
