<%@ include file="/WEB-INF/jsp/include.jsp"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>

<c:set var="targetNext" value="_target1" />

    <c:if test="${empty template.name}">
    <h3><fmt:message key="title_addTemplate1"/></h3>
    </c:if>
    <c:if test="${not empty template.name}">
    <h3><fmt:message key="title_editTemplate1"/></h3>
    </c:if>
    <%@ include file="/WEB-INF/jsp/presentation/wizardHeader.inc"%>
    
    
<form method="post" action="addTemplate.osp"><osp:form />
    <spring:bind path="template.name">
		<%--gsilver:unsure what the attribute "lenght" below meant (maxlength or size) am guessing maxlength--%>
		<c:if test="${status.error}">
        	<p class="shorttext validFail">
		</c:if>	
		<c:if test="${!status.error}">
        	<p class="shorttext">
		</c:if>
            <label for="<c:out value="${status.expression}"/>-id"><span class="reqStar">*</span><fmt:message key="label_name"/></label>
            <input
                type="text" name="<c:out value="${status.expression}"/>" id="<c:out value="${status.expression}"/>-id"
               maxlength="50" value="<c:out value="${status.value}"/>" />
           <c:if test="${status.error}">
	            <span class="alertMessageInline" style="border:none"><c:out value="${status.errorMessage}" /></span>
        </c:if>

        </p>
    </spring:bind>
    
    
    <spring:bind path="template.description">
		<c:if test="${status.error}">
        	<p class="longtext validFail">
		</c:if>	
		<c:if test="${!status.error}">
        	<p class="longtext">
		</c:if>
            <label class="block" for="<c:out value="${status.expression}"/>"><fmt:message key="label_description"/>
				<c:if test="${status.error}">
						<span class="alertMessageInline" style="border:none"><c:out value="${status.errorMessage}" /></span>
				</c:if>
			</label>
            	<textarea name="description" id="description" cols="80" rows="5"><c:out
                value="${template.description}" /></textarea>
        </p>
    </spring:bind>
    
    
    <spring:bind path="template.includeHeaderAndFooter">
            <h4><fmt:message key="legend_showWithinPortfolioNavigation"/></h4>
            <div class="checkbox indnt1">
                <input type="radio" id="showWithinYes"
                    name="<c:out value="${status.expression}"/>" value="true"
                    <c:if test="${status.value}">checked="checked"</c:if> />
                    <label for="showWithinYes"><fmt:message key="label_yes"/></label>
            </div>
            <div class="checkbox indnt1">
                <input type="radio" id="showWithinNo"
                    name="<c:out value="${status.expression}"/>" value="false"
                    <c:if test="${status.value == false}">checked="checked"</c:if> />
                    <label for="showWithinNo"><fmt:message key="label_no"/></label>
            </div>
    </spring:bind>  

    
<c:set var="suppress_previous" value="true" />
<c:set var="suppress_submit" value="true" />
<c:set var="suppress_save" value="true" />
<%@ include file="/WEB-INF/jsp/presentation/wizardFooter.inc"%>
</form>
