<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename="org.theospi.portfolio.presentation.bundle.Messages"/>

<form method="post" name="wizardform" action="createPresentation.osp" onsubmit="return true;">
<osp:form/>

<%-- The model for this JSP consists of:
  * A CreatePresentationCommandBean bound as "command"
  * A map containing two elements:
    1. availableTemplates, a List<PresentationTemplate> of templates available for use
    2. freeFormTemplateId, the singleton Id referring to the placeholder template for free-form portfolios
--%>

<spring:nestedPath path="command">
<spring:bind path="presentationType">
	<input id="presType" type="hidden" name="${status.expression}"/>
</spring:bind>
<c:set var="showCreate" value="${freeFormEnabled || not empty availableTemplates}" />

<div class="presentationTypeDialog">
    <%-- In case we get here without any available types, which should typically not happen due to links being supressed --%>
    <c:choose>
        <c:when test="${showCreate}">
            <h3><fmt:message key="heading_createPresentation"/></h3>
        </c:when>
        <c:otherwise>
            <h3><fmt:message key="heading_createUnavailable"/></h3>
        </c:otherwise>
    </c:choose>
	<spring:bind path="*">
		<c:if test="${status.error}">
			<%-- FIXME: This needs an appropriate class for error msg --%>
			<div class="messageValidation">
				<c:out value="${status.errorMessage}"/>
			</div>
		</c:if>
	</spring:bind>
	<spring:bind path="templateId">
		<c:if test="${not empty availableTemplates}">
			<ul class="presentationTypeGroup">
				<c:forEach var="template"
					items="${availableTemplates}"
					varStatus="templateStatus">
					<li class="portfolioTypeOption">
						<input type="radio"
							id="${status.expression}-${templateStatus.count}"
							name="${status.expression}"
							value="<c:out value="${template.id.value}"/>"
							onclick="getElementById('presType').value = 'osp.presentation.type.template';" />
						<label for="${status.expression}-${templateStatus.count}"><c:out value="${template.name}"/></label>
						<p class="messageInstruction">
							<c:out value="${template.description}"/>
						</p>
					</li>
				</c:forEach>
			</ul>
		</c:if>
		
		<%-- Handle option to turn free-form off --%>
		<c:if test="${freeFormEnabled}">
			<ul class="presentationTypeGroup">
				<li class="portfolioTypeOption">
					<input type="radio"
						id="${status.expression}-freeForm"
						name="${status.expression}"
						value="${freeFormTemplateId.value}"
						onclick="getElementById('presType').value = 'osp.presentation.type.freeForm';" />
					<label for="${status.expression}-freeForm"><fmt:message key="label_freeForm"/></label>
					<p class="messageInstruction">
						<fmt:message key="addPresentation1_manageYourself"/>
					</p>
				</li>
			</ul>
		</c:if>
	</spring:bind>
    <c:if test="${!showCreate}">
        <div class="presentationTypeGroup">
            <p style="text-align: center;"><fmt:message key="no_portfolio_types" /></p>
            <p style="text-align: center;"><a href="<osp:url value="listPresentation.osp" />"><fmt:message key="return_to_list" /></a></p>
        </div>
    </c:if>
</div>

<c:if test="${showCreate}">
<div class="act">
	<input type="submit" name="submit" value="<fmt:message key="button_create"/>" /> <input type="submit" name="cancel" value="<fmt:message key="button_cancel"/>" />
</div>
</c:if>
</spring:nestedPath>
</form>
