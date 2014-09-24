<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<h3>
    <spring:message code="mbp_title_edit"/>
</h3>

<div class="instruction">
    <spring:message code="mbp_edit_instructions"/>
</div>

<form name="revertMessageBundleProperty" method="post" action="revertMessageBundleProperty.osp">
    <osp:form/>
    <spring:bind path="command.id">
        <input type="hidden" name="${status.expression}" value="${status.value}"/>
     </spring:bind>

    <table class="form">
        <spring:bind path="command.moduleName">
            <tr>
                <td class="form_text">
                    <label for="${status.expression}">
                        <spring:message code="mbp_module_label"/>
                    </label>
                </td>
                <td>${status.value}</td>
                <td></td>
            </tr>
        </spring:bind>
        <spring:bind path="command.baseName">
            <tr>
                <td class="form_text">
                    <label for="${status.expression}">
                        <spring:message code="mbp_basename_label"/>
                    </label>
                </td>
                <td>${status.value}</td>
                <td></td>
            </tr>
        </spring:bind>

        <spring:bind path="command.propertyName">
            <tr>
                <td class="form_text">
                    <label for="${status.expression}">
                        <spring:message code="mbp_property_label"/>
                    </label>
                </td>
                <td>${status.value}</td>
                <td></td>
            </tr>
        </spring:bind>

        <spring:bind path="command.locale">
             <tr>
                 <td class="form_text">
                     <label for="${status.expression}">
                         <spring:message code="mbp_locale_label"/>
                     </label>
                 </td>
                 <td>${status.value}</td>
                 <td></td>
             </tr>
         </spring:bind>

        <spring:bind path="command.value">
            <tr>
                <td class="form_text"><label for="${status.expression}" >Current Value</label></td>
                <td> ${status.value}</td>
                <td></td>
            </tr>
        </spring:bind>
        <spring:bind path="command.defaultValue">

        <tr>
            <td class="form_text"><label for="${status.expression}" >New Value</label></td>
            <td> ${status.value}</td>
            <td></td>
        </tr>
        </spring:bind>

    </table>
    <br/><br/>

    <table>
        <tr>
            <td class="form_text"></td>
            <td>
                <input type="submit" name="_target1" class="active" value="<spring:message code='button_revert'  />"/>
                <input type="submit" name="_cancel" value="<spring:message code='button_cancel'/>"/>
            </td>
        </tr>
    </table>
</form>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>