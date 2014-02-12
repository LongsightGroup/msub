<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>

<div class="alertMessage">
   <fmt:message key="pres_confirmDelete"/>
</div>

<form method="post">
   <div class="act">
      <input name="continue" type="submit" value="<fmt:message key="button_continue" />" class="active" accesskey="s" />
      <input name="cancel" type="submit" value="<fmt:message key="button_cancel" />"  accesskey="x" />
   </div>
</form>
