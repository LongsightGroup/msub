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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="bean" uri="http://java.sun.com/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/headerAdd.jsp"/>


            <form:form name="editProviderForm" action="editProvider.form" commandName="oAuthAdminToolState" method="post">
                <form:hidden id="uuid" path="currentProvider.UUID"/>
                    <table style="font-size:8pt" >
                        <tbody>
                            <tr>
                                <td><bean:message key="status"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${oAuthAdminToolState.currentProvider.enabled}">
                                            <bean:message key="enabled"/>
                                        </c:when>
                                        <c:otherwise>
                                            <bean:message key="disabled"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            <tr>
                                <td><bean:message key="provider.s.name"/></td>
                                <td><c:out value="${oAuthAdminToolState.currentProvider.providerName}"/></td>
                            </tr>
                            <tr>
                                <td><bean:message key="description"/></td>
                                <td><c:out value="${oAuthAdminToolState.currentProvider.description}"/></td>
                            </tr>

                            <c:if test="${oAuthAdminToolState.isOAuth2Enabled}">
                                <tr>
                                    <td><bean:message key="client.id"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.clientId}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="client.secret"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.clientSecret}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="auth.url"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.authUrl}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="token.url"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.tokenUrl}"/></td>
                                </tr>
                            </c:if>

                            <c:if test="${not oAuthAdminToolState.isOAuth2Enabled}">
                                <tr>
                                    <td><bean:message key="consumer.key"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.consumerKey}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="realm"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.realm}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="access.token.url"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.accessTokenURL}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="request.token.url"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.requestTokenURL}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="user.authorization.url"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.userAuthorizationURL}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="signatureMethod"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${oAuthAdminToolState.currentProvider.signatureMethod.method eq 'HMAC_SHA1'}">
                                                <c:out value="HMAC-SHA1"/>
                                            </c:when>
                                            <c:when test="${oAuthAdminToolState.currentProvider.signatureMethod.method eq 'RSA_SHA1'}">
                                                <c:out value="RSA-SHA1"/>
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="Unable to determine signature method"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td><bean:message key="consumer.hmacSha1SharedSecret"/></td>
                                    <td><c:out value="${oAuthAdminToolState.currentProvider.hmacSha1SharedSecret}"/></td>
                                </tr>
                                <tr>
                                    <td><bean:message key="consumer.rsaSha1Key"/></td>
                                    <td><code><c:out value="${oAuthAdminToolState.currentProvider.rsaSha1Key}"/></code></td>
                                </tr>
                            </c:if>
                        </tbody>
                        </table>
    <br/>
    <spring:message code="label.additional.parameters"/>
    <table  style="border-width:thin;border-style:solid">
      <thead style="background-color:lightgray;">

         <tr>
           <th scope="col"><spring:message code="label.header.name"/></th>
           <th scope="col"><spring:message code="label.header.value"/> </th>
           <th scope="col"></th>
        </tr>
       </thead>
      <tbody>
       <c:forEach var="entry" items="${oAuthAdminToolState.currentProvider.additionalHeaders}" begin="0" >
          <tr>
          <td>
           <c:out value="${entry.key}" />
          </td>
          <td>
           <c:out value="${entry.value}" />
          </td>
              
         </tr>
     </c:forEach>
     </tbody>
  </table>
 <!--  </td>
   </tr>
    </table> -->   
   </form:form>
    </div>  
  </body>
</html>