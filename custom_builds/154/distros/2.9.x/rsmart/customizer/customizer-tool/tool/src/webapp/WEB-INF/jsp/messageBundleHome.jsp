<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>


<osp:url var="listUrl" value="messageBundleHome.osp"/>


<form name="messageBundleHome" method="post" action="${listUrl}">
<div  class="viewNav">
    <rc:listFilter filterUrl="${listUrl}" showFilterButton="false" showAllFilter="true" localizeValues="false"/>
</div>
<div class="searchNav">
    <input type="text" name="search" value="${searchBean.search}"/>
    <select name="searchLocale">
    <option value=""><spring:message code='mbp_search_locale'/></option>
        <c:forEach var="currentLocale" items="${locales}" varStatus="status">
        <option value="${currentLocale}" <c:if test="${searchBean.locale == currentLocale}">selected</c:if> >   ${currentLocale}</option>
        </c:forEach>
    </select>
    <input type="submit" name="_search" class="active" value="<spring:message code='button_search'  />"/>
    <input type="button" name="_clear" value="<spring:message code='button_clear'/>" onclick="location.href='${listUrl}&_clear&listfilter=-1;'"/>
</div>

</form>

<div class="listNav">
<rc:listScroll listUrl="${listUrl}" listScroll="${listScroll}" className="pager"/>
</div>

<div class ="viewNav"><br/>
    <span class="instructions">
        <spring:message code='current_locale'  />: ${locale} <br/>
        <c:if test="${!searchBean.notEmpty}">
           <spring:message code='mbp_list_instructions'  />
        </c:if>
        <c:if test="${searchBean.notEmpty}">
           <spring:message code='mbp_search_results'  />        
        </c:if>

    </span>
</div>

 <table class="listHier" cellspacing="0">
      <tbody>
           <tr>
              <th><rc:sort name="id"       displayName="mbp_id_label"         sortUrl="${listUrl}"/></th>
              <th><rc:sort name="property"    displayName="mbp_property_label"      sortUrl="${listUrl}"/></th>
               <th><spring:message code="mbp_default_value_label"/></th>
              <th><spring:message code="mbp_value_label"/></th>
              <th><rc:sort name="module"    displayName="mbp_module_label"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="baseName"    displayName="mbp_basename_label"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="locale"    displayName="mbp_locale_label"      sortUrl="${listUrl}"/></th>
           </tr>

           <c:forEach var="property" items="${properties}" varStatus="status">
              <tr>
                 <td>
                   ${property.id}
                 </td>
                  <td>
                    ${property.propertyName}
                        <div class="itemAction">
                            <a href="editMessageBundleProperty.osp?id=${property.id}"><spring:message code='edit_action'/></a>
                            <c:if test="${not empty property.value}"> | <a href="revertMessageBundleProperty.osp?id=${property.id}"><spring:message code='revert_action'/></a></c:if>
                        </div>
                  </td>
                  <td>
                    ${property.defaultValue}
                  </td>
                  <td>
                    ${property.value}
                  </td>
                  <td>
                    ${property.moduleName}
                  </td>
                  <td>
                    ${property.baseName}
                  </td>
                 <td>
                    ${property.locale}
                 </td>

              </tr>
           </c:forEach>
      </tbody>
  </table>


<jsp:include page="/WEB-INF/jsp/footer.jsp"/>