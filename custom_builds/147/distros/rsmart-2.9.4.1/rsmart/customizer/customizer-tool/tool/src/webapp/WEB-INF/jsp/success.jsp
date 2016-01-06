<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>

<h3><spring:message code='start_title'/></h3>
<p class="instruction"><spring:message code='success_instructions_1'/></p>
<p class="instruction">
    <c:set var="arg">
        <a href="http://rsn.rsmart.com" target="_new">
           <spring:message code='rsmart_support'/>
        </a>
    </c:set>
   <spring:message code='success_instructions_2' arguments="${arg}" />
</p>

<br/>
<br/>

<c:set var="targetPrevious" value="_target2" />
<c:set var="step" value="3" />
