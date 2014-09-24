<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<!--fmt:setBundle basename = "messages"/-->
    <div class="navIntraTool">
       <a href="siteTypes.osp">
         Site Types
       </a>
       <a href="toolList.osp">
         Tools
       </a>
        <a href="messageBundleHome.osp">
          Message Bundle Properties
        </a>
        <a href="reloadTools.osp">
           Reload Tools
        </a>
    </div>

<h3><spring:message code='start_title'/></h3>
<p class="instruction"><spring:message code='start_instructions_1'/></p>
<p class="instruction"><spring:message code='start_instructions_2'/></p>
<p class="instruction">
    <c:set var="arg">
        <a href="http://rsn.rsmart.com" target="_new">
           <spring:message code='rsmart_support'/>
        </a>
    </c:set>
   <spring:message code='start_instructions_3' arguments="${arg}"/>
</p>
<p class="instruction"><spring:message code='start_instructions_4'/></p>

<p class="information"><spring:message code='start_note'/></p>

<c:set var="targetNext" value="_target1" />
<c:set var="step" value="0" />

<br/>
<br/>

<form method="POST" name="cusomizerForm" action="customizer.osp"
     onsubmit="return true;">
    
   <input type="submit" name="<c:out value="${targetNext}"/>" class="active" 
      value="<spring:message code="button_next"/>"/>
</form>

<h3>Customizer History</h3>

		<table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Customizer History">
		   <thead>
			  <tr>
				 <th scope="col">Run Date</th>
				 <th scope="col">Spreadsheet</th>
				 <th scope="col">Destructive Mode</th>
				 <th scope="col">Realm Reset</th>
				 <th scope="col">Apply Site Options</th>
			  </tr>
		   </thead>
		   <tbody>
		  <c:forEach var="customizerRun" items="${runHistory}">
			<tr>
			  <td>
              <fmt:formatDate value="${customizerRun.dateRan}" pattern="MM-dd-yyyy hh:mm a "/>
			  </td>
			  <td>
              <a href="<c:out value='${customizerRun.resourceLink}' />">Spreadsheet Copy</a>
			  </td>
			  <td>
				 <c:out value="${customizerRun.destructiveMode}" />
			  </td>
			  <td>
				 <c:out value="${customizerRun.realmResetString}" />
			  </td>
			  <td>
				 <c:out value="${customizerRun.applySiteOptionsString}" />
			  </td>
      </tr>
      </c:forEach>
      </tbody>
   </table>