<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>
    <div class="navIntraTool">
       <a href="customizer.osp">
         Customizer
       </a>
    </div>

<h3><spring:message code='edit_tool_title'/></h3>
<div class="alertMessage">
   <spring:message code='edit_tool_warn'/>
</div>
<p class="instruction"><spring:message code='edit_tool_instr'/></p>

<form method="POST" action="toolList.osp">

   <input type="hidden" value="" name="editToolId"/>
   <input type="hidden" value="" name="cancelEdit"/>
   
   <c:set var="editing" value="${bean.toolId != null && bean.toolId != ''}" />
   
   <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List">
      <thead>
        <tr>
          <th scope="col">Tool Id</th>
          <th scope="col">Tool Title</th>
          <th scope="col">Actions</th>
        </tr>
      </thead>
      <tbody>
     <c:forEach var="tool" items="${tools}">
      <tr>
        <td>
           <a name="row<c:out value="${tool.id}" />"> </a>
          <c:out value="${tool.id}" />
        </td>
        <td>
          <c:out value="${tool.title}" />
        </td>
        <td>
           <!-- <c:out value="${bean.toolId}" /> -->
           <c:if test="${bean.toolId == tool.id}">
<spring:bind path="bean.toolTitle">
<input type="text" id="<c:out value="${status.expression}"/>" name="<c:out value="${status.expression}"/>"
      value="<c:out value="${status.value}" />" />
</spring:bind>              
                        <span class="navIntraTool">
                <a href="#"
   onclick="document.forms[0]['editToolId'].value='';;
              document.forms[0].submit();return false;">save &amp; apply</a> &nbsp;
                <a href="#"
   onclick="document.forms[0]['cancelEdit'].value='true';
              document.forms[0].submit();return false;">cancel</a>
               </span>
           </c:if>
           <c:if test="${!editing}">
                <a href="#"
   onclick="document.forms[0]['editToolId'].value='<c:out value="${tool.id}" />';;
              document.forms[0].submit();return false;">edit</a>
           </c:if>
           <c:if test="${!editing}">
                <a href="funcRequire.osp?toolId=<c:out value="${tool.id}" />">set required functions</a>
           </c:if>
        </td>
   </tr>
   </c:forEach>
   </tbody>
   </table>

</form>

