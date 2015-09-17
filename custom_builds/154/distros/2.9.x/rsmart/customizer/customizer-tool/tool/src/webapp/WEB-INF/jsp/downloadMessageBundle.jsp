<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<h3>
    <spring:message code="mbp_download_bundle"/>
</h3>

<div class="instruction">
    <spring:message code="mbp_download_instructions"/>
</div>

<form action="messageBundle.osp" name="downloadForm" method="GET">
    <input type="hidden" name="method" value="download">
    <select  name="selectedLocale">
        <c:forEach var="locale" items="${locales}" varStatus="status">
            <option value="${locale}">${locale}</option>
        </c:forEach>
    </select>
    <select name="module">
        <option value=""><spring:message code='mbp_select_module'/></option>
        <c:forEach var="module" items="${modules}" varStatus="status">
        <option value="${module}" >${module}</option>
        </c:forEach>
    </select>
    <input type="submit" name="_target1" class="active" value="<spring:message code='button_download'  />"/>

</form>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>