<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<h3>
    <spring:message code="mbp_revert_all"/>
</h3>

<div class="instruction">
    <spring:message code="mbp_revert_instructions"/>
</div>

<form action="messageBundle.osp" name="downloadForm" method="GET">
    <input type="hidden" name="method" value="revertAll">
    <select  name="selectedLocale">
    <c:forEach var="locale" items="${locales}" varStatus="status">
        <option value="${locale}">${locale}</option>
    </c:forEach>
    </select>
    <input type="submit" name="_target1" class="active" value="<spring:message code='button_revert'  />"/>
</form>


<jsp:include page="/WEB-INF/jsp/footer.jsp"/>