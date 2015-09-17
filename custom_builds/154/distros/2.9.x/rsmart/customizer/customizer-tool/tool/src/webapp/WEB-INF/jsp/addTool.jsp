<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>
    <div class="navIntraTool">
       <a href="customizer.osp">
         Customizer
       </a>
    </div>

<h3><spring:message code='add_tool_title'/></h3>
<div class="alertMessage">
   <spring:message code='add_tool_warn'/>
</div>
<p class="instruction"><spring:message code='add_tool_instr'/></p>

<form method="POST" action="addTools.osp">

   <input type="hidden" name="siteType" value="<c:out value="${bean.siteType}"/>"/>
   <input type="hidden" name="siteTypeId" value="<c:out value="${bean.siteTypeId}"/>"/>
   
   <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List">
      <thead>
        <tr>
          <th scope="col">Tool Id</th>
          <th scope="col">Tool Title</th>
          <th scope="col">Actions</th>
        </tr>
      </thead>
      <tbody>
     <c:forEach var="tool" items="${toolList}">
      <tr>
        <td>
           <a name="row<c:out value="${tool.toolId}" />"> </a>
          <c:out value="${tool.toolId}" />
        </td>
        <td>
          <c:out value="${tool.toolTitle}" />
        </td>
        <td>
         <input type="checkbox" 
            name="addToolsToSites"
            value="<c:out value="${tool.toolId}"/>"
            id="tts.<c:out value="${tool.toolId}"/>"/>
         <label for="tts.<c:out value="${tool.toolId}"/>">Add Tool to All Sites</label>   
            &nbsp;
         <input type="checkbox" 
            name="addToolsToType"
            value="<c:out value="${tool.toolId}"/>"
            id="ttt.<c:out value="${tool.toolId}"/>"
            <c:if test="${bean.selectedTools[tool.toolId] == 'true'}">
               disabled checked 
            </c:if>
            />
         <label for="ttt.<c:out value="${tool.toolId}"/>">Add Tool to This Type</label>   
        </td>
   </tr>
   </c:forEach>
   </tbody>
   </table>

   <input value="Save Tool Choices" type="submit" />
   
</form>

