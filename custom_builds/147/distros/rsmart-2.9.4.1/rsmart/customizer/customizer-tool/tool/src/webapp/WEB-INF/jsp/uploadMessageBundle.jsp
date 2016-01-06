<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<h3>
    <spring:message code="mbp_upload_bundle"/>
</h3>

<div class="instruction">
    <spring:message code="mbp_upload_instructions"/>
</div>


<form method="post" action="uploadMessageBundle.osp" enctype="multipart/form-data">
    <input type="file" name="file"/><br/>
<spring:bind path="command.locale">
    <select name="${status.expression}">
        <c:forEach var="locale" items="${locales}" varStatus="status">
            <option value="${locale}">${locale}</option>
        </c:forEach>
    </select>
</spring:bind>
    <br/>
    <table>
        <tr>
            <td class="form_text"></td>
            <td>
                <input type="submit" name="_target1" class="active" value="<spring:message code='button_upload'  />"/>
                <input type="submit" name="_cancel" value="<spring:message code='button_cancel'/>"/>
            </td>
        </tr>
    </table>
</form>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>