<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="targetPrevious" value="_target1"/>
<c:set var="targetNext" value="_target3"/>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>

<h3><fmt:message key="title_addTemplate3"/></h3>

<%@ include file="/WEB-INF/jsp/presentation/wizardHeader.inc" %>
<div class="instruction">
   <fmt:message key="instructions_template_new3"/>
</div>

<form  method="post" action="addTemplate.osp">
<osp:form/>



      <%@ include file="/WEB-INF/jsp/presentation/addItemDefinition.jsp" %>

<c:choose>
	<c:when test="${template.itemDefinitions['empty']}">
		<p class="instruction"><fmt:message key="addTemplate_thereIsNoContentYet"/></p>
	</c:when>
	<c:otherwise>	
		<table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" style="width:auto" summary="<fmt:message key="table_addTemplate3_summary"/>">
					<thead>
					   <tr>
						  <th scope="col" ><fmt:message key="table_header_sequence"/></th>
						  <th scope="col"><fmt:message key="table_header_title"/></th>
						  <th scope="col"></th>
					   </tr>
					</thead>
				 <tbody>
				   <c:if test="${not template.itemDefinitions['empty']}">
					   <c:forEach var="itemDef" items="${template.sortedItems}">
						 <tr>
						   <td>
						   	<label for="id-<c:out value="${itemDef.sequence}"/>" class="skip"><fmt:message key="table_addTemplate3_input_label"/></label>
							  <input type="text" name="itemSequence" value="<c:out value="${itemDef.sequence}"/>"
								 size="4" maxlength="4"
								  id="id-<c:out value="${itemDef.sequence}"/>"	
								 />
						   </td>
						   <td style="white-space:nowrap">
						   	<c:out value="${itemDef.title}" />
							</td>
						   <td class="itemAction" style="white-space:nowrap">
								<a href="<osp:url value="editItemDefinition.osp"/>&id=<c:out value="${itemDef.id.value}" />"><fmt:message key="action_edit"/></a>
								|
								<a href="<osp:url value="deleteItemDefinition.osp"/>&id=<c:out value="${itemDef.id.value}" />"><fmt:message key="action_delete"/></a>
						   </td>
			
						 </tr>
					   </c:forEach>
				   </c:if>
				 </tbody>
				 </table>
			</c:otherwise>
		</c:choose>
<c:set var="suppress_submit" value="true" />
<%@ include file="/WEB-INF/jsp/presentation/wizardFooter.inc" %>

</form>

