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

           function submitForm (link, docId, title, type)
           {
               f = document.editProviderForm;

               f.link.value = link;
               f.docId.value = docId;
               f.title.value = title;
               f.type.value = type;

               f.submit();
           }

          function hideshow(which){
            if (!document.getElementById)
            return
            if (which.style.visibility=="visible")
            which.style.display="hidden"
            else
            which.style.display="visible"
    }

        </script>

    </head>
    <body onload="loaded();">
    <script type="text/javascript">

        function setDeleteKey(key)
        {
            var keyElement = document.editProviderForm.elements["newAdditionalHeaderKey"];
            keyElement.value = key;

            return true;
        }

    </script>
    
        <div class="portletBody">
            <p class="step"><spring:message code="instructions.provider.edit"/></p>
            <p class="step"><span class="reqStarInline">*</span> = <spring:message code="instructions.required"/></p>
            <form:form name="editProviderForm" action="editProvider.form" commandName="oAuthAdminToolState" method="post">
                <form:hidden path="currentProvider.UUID" id="uuid"/>
                <h3>Edit a Provider</h3>
                <table style="font-size:8pt">
                    <tbody>
                        <tr>
                            <td><bean:message key="provider.enabled"/></td>
                            <td><form:checkbox path="currentProvider.enabled"/></td>
                        </tr>
                        <tr>
                            <td><bean:message key="provider.s.name"/></td>
                            <td><form:input path="currentProvider.providerName" readonly="true" /></td>
                        </tr>
                        <tr valign="top">
                            <td><spring:message code="label.table.provider.description.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:textarea path="currentProvider.description" rows="4" /></td><td><form:errors path="currentProvider.description"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerDescriptionMessageError }"/></font></td>
                        </tr>

                        <c:if test="${oAuthAdminToolState.isOAuth2Enabled}">
                            <tr>
                                <td><bean:message key="label.table.provider.clientid.header"/> <span class="reqStarInline">*</span></td>
                                <td><form:input path="currentProvider.clientId" /></td><td><form:errors path="currentProvider.clientId"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerClientIdMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td><bean:message key="label.table.provider.clientsecret.header"/> <span class="reqStarInline">*</span></td>
                                <td><form:input path="currentProvider.clientSecret" /></td><td><form:errors path="currentProvider.clientSecret"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerClientSecretMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="label.table.provider.authurl.header"/> <span class="reqStarInline">*</span>
                                </td>
                                <td><form:input path="currentProvider.authUrl"/></td> <td><form:errors path="currentProvider.authUrl"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerAuthUrlMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="label.table.provider.tokenurl.header"/> <span class="reqStarInline">*</span>
                                </td>
                                <td><form:input path="currentProvider.tokenUrl"/></td> <td><form:errors path="currentProvider.tokenUrl"></form:errors><font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerTokenUrlMessageError }"/></font> </td>
                            </tr>
                        </c:if>

                        <c:if test="${not oAuthAdminToolState.isOAuth2Enabled}">
                            <tr>
                                <td><bean:message key="consumer.key"/> <span class="reqStarInline">*</span></td>
                                <td><form:input path="currentProvider.consumerKey" /></td><td><form:errors path="currentProvider.consumerKey"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerConsumerKeyMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="realm"/>
                                </td>
                                <td><form:input path="currentProvider.realm"/></td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="access.token.url"/> <span class="reqStarInline">*</span>
                                </td>
                                <td><form:input path="currentProvider.accessTokenURL"/></td> <td><form:errors path="currentProvider.accessTokenURL"></form:errors> <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerAccessTokenUrlMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="request.token.url"/> <span class="reqStarInline">*</span>
                                </td>
                                <td><form:input path="currentProvider.requestTokenURL"/></td> <td><form:errors path="currentProvider.requestTokenURL"></form:errors><font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerRequestTokenUrlMessageError }"/></font> </td>
                            </tr>
                            <tr>
                                <td>
                                    <bean:message key="user.authorization.url"/> <span class="reqStarInline">*</span>
                                </td>
                                <td><form:input path="currentProvider.userAuthorizationURL"/></td> <td><form:errors path="currentProvider.userAuthorizationURL"></form:errors><font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerUserAuthorizationUrlMessageError }"/></font></td>
                            </tr>
                            <tr>
                                <td><bean:message key="signatureMethod.select"/>  <span class="reqStarInline">*</span></td>
                            </tr>
                            <tr>
                                <td valign="top"><form:radiobutton path="currentProvider.signatureMethod" value="HMAC_SHA1"/>
                                    <bean:message key="consumer.hmacSha1SharedSecret"/></td>
                                <td><form:input path="currentProvider.hmacSha1SharedSecret"/></td>
                                <td>
                                    <form:errors path="currentProvider.hmacSha1SharedSecret"></form:errors>
                                    <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerHmacSha1SharedSecretMessageError }"/></font>
                                </td>
                            </tr>
                            <tr>
                                <td valign="top"><form:radiobutton path="currentProvider.signatureMethod" value="RSA_SHA1"/>
                                    <bean:message key="consumer.rsaSha1Key"/></td>
                                <td><form:textarea path="currentProvider.rsaSha1Key" rows="10"/></td>
                                <td>
                                    <form:errors path="currentProvider.rsaSha1Key"></form:errors>
                                    <font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.providerRsaSha1KeyMessageError }"/></font>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
                <spring:message code="label.additional.parameters"/>
                <table  style="border-width:thin;border-style:solid">
                  <thead style="background-color:lightgray;">
                     <tr>
                       <th scope="col" align="left"><spring:message code="label.header.name"/></th>
                       <th scope="col" align="left"><spring:message code="label.header.value"/> </th>
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
                      <td>
                          <input type="submit" name="delete" value="<spring:message code='label.link.delete'/>" onclick="setDeleteKey('${entry.key}')"/>
                      </tr>
                 </c:forEach>
                </tbody>
                 </table>
                  <br/>
                 <table>
                   <tbody>
                  <tr>
                   <td align="left" colspan="2"><font size="2" color="red"><c:out value="${oAuthAdminToolState.providerErrors.parameterNameAlreadyExists }"/><c:out value="${oAuthAdminToolState.providerErrors.parameterValuesNull }"/></font></td>
                   </tr>
                  <tr>
                  <td align="left">
                   <form:input path="newAdditionalHeaderKey" />
                  </td>
                  <td align="left">
                   <form:input path="newAdditionalHeaderValue" />
                  </td>
                    <td><input type="submit" value="<spring:message code='label.button.add'/>" name="add"></td>
                 </tr>
                   </tr>
                  </tbody>
                 </table>
                   <input type="submit" value="<spring:message code='label.button.update'/>" />
            </form:form>
        </div>
    </body>
</html>