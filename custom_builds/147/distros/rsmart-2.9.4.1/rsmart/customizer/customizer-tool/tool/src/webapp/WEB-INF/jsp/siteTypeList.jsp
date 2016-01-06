<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<fmt:setLocale value="${locale}"/>
<fmt:setBundle basename = "messages"/>
    <div class="navIntraTool">
       <a href="customizer.osp">
         Customizer
       </a>
    </div>

<h3><spring:message code='edit_site_types'/></h3>
<div class="alertMessage">
   <spring:message code='edit_site_types_warn'/>
</div>
<p class="instruction"><spring:message code='edit_site_types_instr'/></p>

<form method="POST" action="siteTypes.osp">

   <c:set var="notMoving" value="${bean.movingToolId == null || bean.movingToolId == ''}" />
   <c:set var="notRenaming" value="${bean.renamingCatId == null || bean.renamingCatId == ''}" />
   
   <input type="hidden" name="movingToolId" value="<c:out value="${bean.movingToolId}"/>"/>
   <input type="hidden" name="renamingCatId" value="<c:out value="${bean.renamingCatId}"/>"/>
   <input type="hidden" name="catToolMovedFrom" value="<c:out value="${bean.catToolMovedFrom}"/>"/>
   <input type="hidden" name="catToolMovedTo" value="<c:out value="${bean.catToolMovedTo}"/>"/>
   <input type="hidden" name="siteTypeExpanded" value="<c:out value="${bean.siteTypeExpanded}"/>"/>
   <input type="hidden" name="categoryExpanded" value="<c:out value="${bean.categoryExpanded}"/>"/>

   <input type="hidden" name="catMoveUpId"/>
   <input type="hidden" name="catMoveDownId"/>
   <input type="hidden" name="toolMoveUpId"/>
   <input type="hidden" name="toolMoveDownId"/>
   
   <c:if test="${notRenaming}">
      <input type="hidden" name="newCategoryName" value="ignore"/>
   </c:if>
   
   <table class="listHier lines nolines" cellspacing="0" cellpadding="0" border="0" summary="Tool List">
      <tbody>
     <c:forEach var="siteType" items="${siteTypes}">
      <tr>
        <td colspan="3">
          <c:if test="${siteType.siteType.id.value == bean.siteTypeExpanded}">
           <a href="#" onclick="document.forms[0]['siteTypeExpanded'].value='';
              document.forms[0].submit();return false;">
            <c:out value="${siteType.siteType.name}" />
           </a>
          </c:if>
          <c:if test="${siteType.siteType.id.value != bean.siteTypeExpanded}">
           <a href="#" onclick="document.forms[0]['siteTypeExpanded'].value=
              '<c:out value="${siteType.siteType.id.value}"/>';
              document.forms[0].submit();return false;">
            <c:out value="${siteType.siteType.name}" />
           </a>
           </c:if>
        </td>
        <td>
            <c:if test="${siteType.realmName}">
               <a href="siteTypePerms.osp?realm=<c:out value="${siteType.realm}"/>&siteType=<c:out 
                  value="${siteType.siteType.name}" />">Edit Perms</a>
            </c:if>
            <c:if test="${siteType.siteType.id.value == bean.siteTypeExpanded}">
               &nbsp;
               <a href="#" onclick="document.forms[0]['renamingCatId'].value='new';
                  document.forms[0]['newCategoryName'].value='new category';
                  document.forms[0].submit();return false;">
            add category
           </a>
               &nbsp;
           <a href="addTools.osp?siteType=<c:out 
              value="${siteType.siteType.name}" />&siteTypeId=<c:out 
              value="${siteType.siteType.id.value}" />">add tools</a>
            </c:if>
        </td>
      </tr>
        <c:if test="${siteType.siteType.id.value == bean.siteTypeExpanded}">
           <c:set var="lastCatId" value=""/>
           <c:forEach var="category" items="${siteType.toolCategories}" varStatus="rowCounterOther">
              <c:if test="${!category.category.uncategorized || lastCatId != 'uncategorized'}">
               <tr>
                  <td width="5px">
                     &nbsp;
                  </td>
                  <td colspan="2">
                     <c:if test="${!category.category.uncategorized}">
                        <c:if test="${category.category.id.value == bean.renamingCatId}">
                           <input type="hidden" name="submittingChangedName" value="true"/>
                           
                           <spring:bind path="bean.newCategoryName">
                              <input type="text" id="<c:out value="${status.expression}"/>" 
                                     name="<c:out value="${status.expression}"/>"
                                     value="<c:out value="${status.value}" />" />
                              <c:if test="${status.error}">
                                 <span class="alertMessageInline" style="border:none">
                                       <c:out value="${status.errorMessage}"/>
                                 </span>
                              </c:if>
                           </spring:bind>
                        </c:if>
                        <c:if test="${category.category.id.value != bean.renamingCatId}">
                           <c:out value="${category.category.name}" />
                        </c:if>
                        <c:set var="lastCatId" value="${category.category.id.value}"/>
                     </c:if>
                     <c:if test="${category.category.uncategorized}">
                        Uncategorized
                        <c:set var="lastCatId" value="uncategorized"/>
                     </c:if>
                  </td>
                  <td>
                     <c:if test="${!notMoving && !category.category.uncategorized && category.category.id.value != bean.catToolMovedFrom}">
                        <span class="navIntraTool">
                        <a href="#" onclick="document.forms[0]['catToolMovedTo'].value=
                           '<c:out value="${category.category.id.value}"/>';
                           document.forms[0].submit();return false;">select category</a>
                        </span>
                     </c:if>
                     <c:if test="${notMoving && notRenaming && !category.category.uncategorized}">
                        <a href="#" onclick="document.forms[0]['renamingCatId'].value=
                           '<c:out value="${category.category.id.value}"/>';
                           document.forms[0]['newCategoryName'].value=
                           '<c:out value="${category.category.name}"/>';
                           document.forms[0].submit();return false;">rename category</a>
                     </c:if>
                     <c:if test="${category.category.id.value == bean.renamingCatId && category.category.id != null}">
                        <span class="navIntraTool">
                        <a href="#" onclick="document.forms[0].submit();return false;">save</a>
                        &nbsp;
                        <a href="#" onclick="document.forms[0]['renamingCatId'].value='';
                           document.forms[0].submit();return false;">cancel</a>
                        </span>
                     </c:if>
                     
                     <c:if test="${notMoving && notRenaming && !category.category.uncategorized && category.moveUp}">
                        <a href="#" onclick="document.forms[0]['catMoveUpId'].value=
                        '<c:out value="${category.category.id.value}"/>';
                           document.forms[0].submit();return false;"><img src="/library/image/sakai/arrowUp.gif"/></a>
                     </c:if>
                     <c:if test="${notMoving && notRenaming && !category.category.uncategorized && category.moveDown}">
                        <a href="#" onclick="document.forms[0]['catMoveDownId'].value=
                        '<c:out value="${category.category.id.value}"/>';
                           document.forms[0].submit();return false;"><img src="/library/image/sakai/arrowDown.gif"/></a>
                     </c:if>
                  </td>
               </tr>
              </c:if>
              <c:forEach var="tool" items="${category.tools}" varStatus="rowCounter">
                  <tr <c:if test="${bean.movingToolId == tool.toolId}">style="background:#00ff00"</c:if>>
                     <td width="5px">
                        &nbsp;
                     </td>
                     <td width="5px">
                        &nbsp;
                     </td>
                     <td>
                        <c:out value="${tool.toolId}" />
                     </td>
                     <td>
                        <c:if test="${bean.movingToolId == tool.toolId}">
                        <span class="navIntraTool">
                           <a href="#" onclick="document.forms[0]['movingToolId'].value=
                              '';document.forms[0]['catToolMovedFrom'].value=
                              '';
                              document.forms[0].submit();return false;">cancel</a>
                           </span>
                        </c:if>
                        <c:if test="${notMoving && notRenaming}">
                           <a href="#" onclick="document.forms[0]['movingToolId'].value=
                              '<c:out value="${tool.toolId}"/>';document.forms[0]['catToolMovedFrom'].value=
                              '<c:out value="${category.category.id.value}"/>';
                              document.forms[0].submit();return false;">change category</a>
                        </c:if>

                     <c:if test="${notMoving && notRenaming && !rowCounter.first}">
                        <a href="#" onclick="document.forms[0]['toolMoveUpId'].value=
                              '<c:out value="${tool.toolId}"/>';document.forms[0]['catToolMovedFrom'].value=
                              '<c:out value="${category.category.id.value}"/>';
                              document.forms[0].submit();return false;"><img src="/library/image/sakai/arrowUp.gif"/></a>
                     </c:if>
                     <c:if test="${notMoving && notRenaming && !rowCounter.last}">
                        <a href="#" onclick="document.forms[0]['toolMoveDownId'].value=
                              '<c:out value="${tool.toolId}"/>';document.forms[0]['catToolMovedFrom'].value=
                              '<c:out value="${category.category.id.value}"/>';
                              document.forms[0].submit();return false;"><img src="/library/image/sakai/arrowDown.gif"/></a>
                     </c:if>
                     </td>
                  </tr>
            </c:forEach>
         </c:forEach>
        </c:if>
   </c:forEach>
   </tbody>
   </table>

</form>

