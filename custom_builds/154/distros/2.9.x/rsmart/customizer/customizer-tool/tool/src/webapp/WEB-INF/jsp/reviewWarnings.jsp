<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>

<h3><spring:message code='start_title'/></h3>
<p class="instruction">
    <c:set var="arg">
        <a href="http://rsn.rsmart.com" target="_new">
           <spring:message code='rsmart_support'/>
        </a>
    </c:set>
   <spring:message code='display_warning_instructions' arguments="${arg}"/>
</p>


<c:set var="targetNext" value="_target3" />
<c:set var="targetPrevious" value="_target1" />
<c:set var="step" value="2" />
<form method="POST" name="cusomizerForm" action="customizer.osp"
     onsubmit="return true;">
   
   <spring:bind path="form.exception">
      <div class="alertMessage">
         <c:forEach var="anError" items="${status.value.errors}">
            <p><c:out value='${anError}' /></p>  
         </c:forEach>
         <c:forEach var="aWarning" items="${status.value.warnings}">
            <p><c:out value='${aWarning}' /></p>  
         </c:forEach>
      </div>
   </spring:bind>
   <br/>
   <br/>
   
   <input type="submit" name="<c:out value="${targetNext}"/>" class="active" 
      value="<spring:message code="button_process"/>"/>
   <input type="submit" name="<c:out value="${targetPrevious}"/>" 
      value="<spring:message code="button_back"/>"/>
   
</form>