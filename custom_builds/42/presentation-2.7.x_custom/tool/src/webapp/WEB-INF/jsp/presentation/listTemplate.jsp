<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<!-- GUID=<c:out value="${newPresentationTemplateId}"/> -->

<osp-c:authZMap prefix="osp.presentation.template." useSite="true" var="can" />

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "org.theospi.portfolio.presentation.bundle.Messages"/>


<script type="text/javascript">
	<!--
		function toggle_visibility(id) {
		   var e = document.getElementById(id);
		   var elabel = document.getElementById('toggle' + id);
		   if(e.style.display == 'block')
		   {
			  e.style.display = 'none';
			  elabel.src='/library/image/sakai/expand.gif'
			  elabel.title='<fmt:message key="hideshowdesc_toggle_show"/>'
			  resizeFrame('shrink');
			}
		   else
		   {
			  e.style.display = 'block';
			  elabel.src='/library/image/sakai/collapse.gif'
			  elabel.title='<fmt:message key="hideshowdesc_toggle_hide"/>'
			  resizeFrame();
			}  
		}
		function resizeFrame(updown) {
		  var frame = parent.document.getElementById( window.name );
		  if( frame ) {
			if(updown=='shrink')
			{
			var clientH = document.body.clientHeight + 30;
		  }
		  else
		  {
		  var clientH = document.body.clientHeight + 30;
		  }
			$( frame ).height( clientH );
		  } else {
			throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
		  }
		}
			//-->

</script>

<c:if test="${can.create || isMaintainer}">
    <div class="navIntraTool">
       <c:if test="${can.create}">
          <a href="<osp:url value="addTemplate.osp"/>" title="<fmt:message key="action_add"/>" >
          <fmt:message key="action_add"/>
          </a>
          <a href="<osp:url value="importTemplate.osp"/>" title="<fmt:message key="action_import_title"/>" >
          <fmt:message key="action_import"/>
          </a>
       </c:if>
       <c:if test="${isMaintainer}">
          <a href="<osp:url value="osp.permissions.helper/editPermissions">
                <osp:param name="message"><fmt:message key="message_permissionsEdit">
	          <fmt:param><c:out value="${tool.title}"/></fmt:param>
		  <fmt:param><c:out value="${worksite.title}"/></fmt:param></fmt:message></osp:param>
                <osp:param name="name" value="presentationTemplate"/>
                <osp:param name="qualifier" value="${worksite.id}"/>
                <osp:param name="returnView" value="listTemplateRedirect"/>
                </osp:url>"
                title="<fmt:message key="action_permissions_title"/>" >
          <fmt:message key="action_permissions"/>
          </a>
       </c:if>
    </div>
</c:if>

<div class="navPanel">
	<div class="viewNav">
		<h3><fmt:message key="title_listTemplate"/></h3>
	</div>	
	<osp:url var="listUrl" value="listTemplate.osp"/>
	<osp:listScroll listUrl="${listUrl}" className="listNav" />
</div>	

<c:if test="${not empty presentationTemplateError}">
   <div class="alertMessage"><fmt:message key="${presentationTemplateError}"/></div>
</c:if>


<c:choose>
	<c:when test="${empty templates}">
		<p class="instruction"><fmt:message key="template_table_empty"/></p>
	</c:when>
	<c:otherwise>
		<table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="<fmt:message key="template_table_summary"/>">
		   <thead>
			  <tr>
				 <th scope="col"><fmt:message key="table_header_name"/></th>
				 <th scope="col"></th>
				 <th scope="col"><fmt:message key="table_header_includeHeader"/></th>
				 <th scope="col"><fmt:message key="table_header_owner"/></th>
			  </tr>
		   </thead>
		   <tbody>
		  <c:forEach var="template" items="${templates}">
			<osp-c:authZMap prefix="osp.presentation.template." qualifier="${template.id}" var="isAuthorizedTo" />
			<tr>
			  <td>
			  		<c:if test="${!(empty template.description)}">
							<a href="#"  onclick="toggle_visibility('<c:out value="${template.id.value}" />')"><img  id="toggle<c:out value="${template.id.value}" />"  src="/library/image/sakai/expand.gif" style="padding-top:4px;width:13px" title="<fmt:message key="hideshowdesc_toggle_show"/>" /></a>
					</c:if>
					<c:if test="${(empty template.description)}">
						<img  src="/library/image/sakai/s.gif" style="width:13px" />
					</c:if>						
				 <c:out value="${template.name}" />
			</td>
			<td class="itemAction">
				 <c:set var="hasFirstAction" value="false" />
				 
					 <c:if test="${isAuthorizedTo.publish && template.published == false}">
						 <c:if test="${hasFirstAction}" > | </c:if>
						 <c:set var="hasFirstAction" value="true" />
						 <a href="<osp:url value="publishTemplate.osp"/>&id=<c:out value="${template.id.value}" />">
							<c:if test="${globalTool}">
							   <fmt:message key="table_action_PublishGlobal"/>
							</c:if>
							<c:if test="${not globalTool}">
								<fmt:message key="table_action_publish"/>
							</c:if>
						 </a>
					 </c:if>
					 
					 <c:if test="${isAuthorizedTo.edit}">
						 <c:if test="${hasFirstAction}" > | </c:if>
						 <c:set var="hasFirstAction" value="true" />
						 <a href="<osp:url value="editTemplate.osp"/>&id=<c:out value="${template.id.value}" />"><fmt:message key="table_action_edit"/></a>
					 </c:if>
			
					 <c:if test="${isAuthorizedTo.delete}">
						 <c:if test="${hasFirstAction}" > | </c:if>
						 <c:set var="hasFirstAction" value="true" />
						 <a onclick="return confirmDeletion();"
						   href="<osp:url value="deleteTemplate.osp"/>&id=<c:out value="${template.id.value}" />"><fmt:message key="table_action_delete"/></a>
					 </c:if>
			
					 <c:if test="${isAuthorizedTo.copy}">
						 <c:if test="${hasFirstAction}" > | </c:if>
						 <c:set var="hasFirstAction" value="true" />
						 <a href="<osp:url value="copyTemplate.osp"/>&id=<c:out value="${template.id.value}" />"><fmt:message key="table_action_copy"/></a>
					 </c:if>
					 
					 <c:if test="${isAuthorizedTo.export}">
						 <c:if test="${hasFirstAction}" > | </c:if>
						 <c:set var="hasFirstAction" value="true" />
						 <a href="<osp:url includeQuestion="false" value="/repository/1=1"/>&manager=presentationManager&templateId=<c:out value="${template.id.value}"/>/<c:out value="${template.name}" />.zip"><fmt:message key="table_action_export"/></a>
					 </c:if>
			  </td>
			  <td><c:out value="${template.includeHeaderAndFooter}" /></td>
			  <td><c:out value="${template.owner.displayName}" /></td>
		
			</tr>
			<c:if test="${!(empty template.description)}">
				<tr class="exclude">
					<td colspan="4">
						<div class="instruction indnt2 textPanelFooter" id="<c:out value="${template.id.value}" />" style="padding:0;margin:0;display:none">
							  <c:out value="${template.description}" />
						</div>	  
					</td>
				</tr>
			</c:if>	
						  
		
		  </c:forEach>
			</tbody>
		  </table>
		 </c:otherwise>
</c:choose>		 
