<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>
    <div class="navIntraTool">
       <a href="customizer.osp">
         Customizer
       </a>
       <a href="siteTypes.osp">
         Site Types
       </a>
    </div>

<h3><spring:message code='edit_site_type_perms_title'/></h3>
<div class="alertMessage">
   <spring:message code='edit_site_type_perms_warn'/>
</div>
<p class="instruction"><spring:message code='edit_site_type_perms_instr'/></p>
 
<form method="POST" action="siteTypePerms.osp">

   <input type="hidden" name="siteType" value="<c:out value="${bean.siteType}"/>"/>
   <input type="hidden" name="realm" value="<c:out value="${bean.realm}"/>"/>

   <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List">
      <thead><tr>
         <th>Functions</th>
     <c:forEach var="role" items="${bean.roles}">
        <th>
            <c:out value="${role.role.id}"/>   
        </th>
     </c:forEach>
      </tr></thead>
      <tbody>
           <c:forEach var="func" items="${bean.functions}">
         <tr>
              <td>
                  <c:out value="${func}"/>   
              </td>
              
              <c:forEach var="role" items="${bean.roles}">
                 <td>
                     <input type="checkbox" 
                        name="perms"
                        value="<c:out value="${func}"/>~<c:out value="${role.role.id}"/>"
                        <c:if test="${role.authzMap[func] == 'true'}">checked </c:if>   
                        />
                 </td>
              </c:forEach>
         </tr>
           </c:forEach>
      </tbody>      
   </table>
      
   <input value="Save Permissions" type="submit"/>
   
</form>