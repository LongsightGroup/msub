<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
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

<jsp:include page="/WEB-INF/jsp/headerAdd.jsp"/>

<script type="text/javascript">

    function setDeleteKey(key)
    {
        var keyElement = document.addProviderForm.elements["newAdditionalHeaderKey"];
        keyElement.value = key;

        return true;
    }

</script>

            <p class="step"><spring:message code="instructions.provider.add"/></p>
            <p class="step"><span class="reqStarInline">*</span> = <spring:message code="instructions.required"/></p>
            <form:form name="addProviderForm" action="addProvider.form" commandName="oAuthAdminToolState">

                <table  style="font-size:8pt">
                    <tbody>
                        <tr>
                            <td><spring:message code="label.table.provider.enabled.header"/></td>
                            <td><form:checkbox path="currentProvider.enabled" /></td>
                            <td>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.name.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:input path="currentProvider.providerName" /></td>
                            <td>
                                <form:errors path="currentProvider.providerName"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providersNameMessageError }"/>
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerdNameExistMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td><spring:message code="label.table.provider.description.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:textarea path="currentProvider.description" rows="4" /></td>
                            <td>
                                <form:errors path="currentProvider.description"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerDescriptionMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.consumerkey.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:input path="currentProvider.consumerKey" /></td>
                            <td>
                                <form:errors path="currentProvider.consumerKey"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerConsumerKeyMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.realm.header"/></td>
                            <td><form:input path="currentProvider.realm"/></td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.accesstokenurl.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:input path="currentProvider.accessTokenURL"/></td>
                            <td>
                                <form:errors path="currentProvider.accessTokenURL"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerAccessTokenUrlMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.requesttokenurl.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:input path="currentProvider.requestTokenURL"/></td>
                            <td>
                                <form:errors path="currentProvider.requestTokenURL"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerRequestTokenUrlMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.userauthorizationurl.header"/> <span class="reqStarInline">*</span></td>
                            <td><form:input path="currentProvider.userAuthorizationURL"/></td>
                            <td>
                                <form:errors path="currentProvider.userAuthorizationURL"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerUserAuthorizationUrlMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td><spring:message code="label.table.provider.signatureMethod.select"/>  <span class="reqStarInline">*</span></td>
                        </tr>
                        <tr>
                            <td valign="top"><form:radiobutton path="currentProvider.signatureMethod" value="HMAC_SHA1"/>
                                <spring:message code="label.table.provider.hmacsha1sharedsecret.header"/></td>
                            <td><form:input path="currentProvider.hmacSha1SharedSecret"/></td>
                            <td>
                                <form:errors path="currentProvider.hmacSha1SharedSecret"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerHmacSha1SharedSecretMessageError }"/>
                                </font>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top"><form:radiobutton path="currentProvider.signatureMethod" value="RSA_SHA1"/>
                                <spring:message code="label.table.provider.rsasha1key.header"/></td>
                            <td><form:textarea path="currentProvider.rsaSha1Key" rows="10"/></td>
                            <td>
                                <form:errors path="currentProvider.rsaSha1Key"></form:errors>
                                <font size="2" color="red">
                                    <c:out value="${oAuthAdminToolState.providerErrors.providerRsaSha1KeyMessageError }"/>
                                </font>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <br/>
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
                            <td><c:out value="${entry.key}" /></td>
                            <td><c:out value="${entry.value}" /></td>
                            <td>
                                <input type="submit" name="delete" value="<spring:message code='label.link.delete'/>" onclick="setDeleteKey('${entry.key}')"/>
                            </td>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
                <br/>
                <table>
                    <tbody>
                        <form:errors path="currentProvider.rsaSha1Key"></form:errors>
                        <font size="2" color="red">
                            <c:out value="${oAuthAdminToolState.providerErrors.parameterNameAlreadyExists}"/>
                        </font>
                        <tr>
                            <td align="left"><form:input path="newAdditionalHeaderKey" /></td>
                            <td align="left"><form:input path="newAdditionalHeaderValue" /></td>
                            <td><input type="submit" value="<spring:message code='label.button.add'/>" name="add"></td>
                        </tr>
                    </tbody>
                </table>
                <br/>
                <input type="submit" name="submit" value="<spring:message code='label.button.submit'/>">
                <input type="submit" name="cancel" value="<spring:message code='label.button.cancel'/>"/>
            </form:form>
        </div>
    </body>
</html>