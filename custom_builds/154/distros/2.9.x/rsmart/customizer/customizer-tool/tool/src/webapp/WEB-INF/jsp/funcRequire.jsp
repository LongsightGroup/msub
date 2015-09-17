<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>
    <div class="navIntraTool">
       <a href="customizer.osp">
         Customizer
       </a>
       <a href="toolList.osp">
         All Tools
       </a>
    </div>

<h3><spring:message code='func_require_title'/></h3>
<div class="alertMessage">
   <spring:message code='func_require_warn'/>
</div>
<p class="instruction"><spring:message code='func_require_instr'/></p>

<form method="POST" action="funcRequire.osp">

   <input type="hidden" name="toolId" value="<c:out value="${bean.toolId}"/>"/>

   <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List">
      <thead><tr>
         <th>Functions</th>
      </tr></thead>
      <tbody>
           <c:forEach var="func" items="${bean.functions}">
         <tr>
              <td>
                  <input type="checkbox" 
                     name="required"
                     value="<c:out value="${func}"/>"
                     id="req.<c:out value="${func}"/>"
                     <c:if test="${bean.requiredMap[func] == 'true'}">checked </c:if>   
                     />
                  <label for="req.<c:out value="${func}"/>"><c:out value="${func}"/></label>   
              </td>
         </tr>
           </c:forEach>
      </tbody>      
   </table>
      
   <input value="Save Functions" type="submit"/>
   
</form>