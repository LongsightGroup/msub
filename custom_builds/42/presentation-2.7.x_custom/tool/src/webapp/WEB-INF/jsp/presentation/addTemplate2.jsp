<%@ include file="/WEB-INF/jsp/include.jsp"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<c:set var="targetPrevious" value="_target0" />
<c:set var="targetNext" value="_target2" />

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>

<form method="post" action="addTemplate.osp">
    <osp:form />
    <input type="hidden" name="pickerField" value="" />
    <input type="hidden" name="validate" value="true" />
    <spring:bind path="template.id">
        <c:set var="templateId" value="${status.value}" />
    </spring:bind>

    <h3><fmt:message key="title_addTemplate2"/></h3>
    <%@ include file="/WEB-INF/jsp/presentation/wizardHeader.inc"%>
    <p class="instruction">
        <fmt:message key="instructions_template_new2"/>
        <fmt:message key="instructions_requiredFields"/>
    </p>

    <spring:bind path="template.renderer">
		<c:if test="${status.error}">
        	<p class="shorttext validFail">
		</c:if>	
		<c:if test="${!status.error}">
        	<p class="shorttext">
		</c:if>
            <span class="reqStar">*</span>
            <label  for="rendererName"><fmt:message key="label_basicTemplateOutline"/></label>
            <input type="text" id="rendererName" disabled="disabled"
                value="<c:out value="${rendererName}"/>" />
            <input type="hidden" name="renderer" id="renderer"
                value="<c:out value="${status.value}"/>" />
            <input type="hidden" name="returnPage" id="returnPage"
                value="<c:out value="${currentPage-1}"/>" />
            <a href="javascript:callPicker('<c:out value="${TEMPLATE_RENDERER}"/>');">
            <fmt:message key="action_pickFile"/> </a>
            
            <script type="text/javascript">
               function callPicker(pickerField) {
                  document.getElementById('insertTarget').innerHTML="<input type='hidden' name='_target4' id='_target4' value='picker' />"
                  document.forms[0].pickerField.value=pickerField;
                  document.forms[0].validate.value='false';
                  document.forms[0].submit()
               }
            </script>
            
			<c:if test="${status.error}">
	            <span class="alertMessageInline" style="border:none"><c:out value="${status.errorMessage}" /></span>
			</c:if>
		</p>	
    </spring:bind>
  
     <spring:bind path="template.propertyFormType">  
		<c:if test="${status.error}">
        	<p class="shorttext validFail">
		</c:if>	
		<c:if test="${!status.error}">
        	<p class="shorttext">
		</c:if>
         <label for="<c:out value="${status.expression}"/>-id"><fmt:message key="label_outlineOptionsFormType"/></label>    
            <select name="<c:out value="${status.expression}"/>" <c:out value="${localDisabledText}"/>  id="<c:out value="${status.expression}"/>-id">
                     <option value=""><fmt:message key="select_form_text" /></option>
                  <c:forEach var="formType" items="${propertyFormTypes}">
                     <option  
                        value="<c:out value="${formType.id}"/>" <c:if test="${status.value==formType.id}"> selected</c:if>><c:out value="${formType.name}"/></option>
                  </c:forEach>
               </select>
				<c:if test="${status.error}">
					<span class="alertMessageInline" style="border:none"><c:out value="${status.errorMessage}" /></span>
				</c:if>
			</p>
     </spring:bind>


    <c:set var="suppress_submit" value="true" />
    <%@ include file="/WEB-INF/jsp/presentation/wizardFooter.inc"%>
    <p id="insertTarget" />
</form>


