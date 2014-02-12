<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>


<table width="100%">
<tr><td height="100"> &nbsp; </td></tr>
<tr><td align="center">
<c:choose>
<c:when test="${noPagesFound}">
<fmt:message key="presentation_no_pages_founc"/>
</c:when>
<c:otherwise>
<fmt:message key="presentation_not_found"/>
</c:otherwise>
</c:choose>
</td></tr></table>
