<%@ page import="java.util.List" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%--
  ~ Copyright 2011 The rSmart Group
  ~
  ~ The contents of this file are subject to the Mozilla Public License
  ~ Version 1.1 (the "License"); you may not use this file except in
  ~ compliance with the License. You may obtain a copy of the License at
  ~ http://www.mozilla.org/MPL/
  ~
  ~ Software distributed under the License is distributed on an "AS IS"
  ~ basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  ~ License for the specific language governing rights and limitations
  ~ under the License.
  ~
  ~ Contributor(s): duffy
  --%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link href="<c:out value="${sakai_skin_base}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <link href="<c:out value="${sakai_skin}"/>"
          type="text/css"
          rel="stylesheet"
          media="all" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <title><%= org.sakaiproject.tool.cover.ToolManager.getCurrentTool().getTitle()%></title>
    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
    </script>
      <%
          String panelId = request.getParameter("panel");
          if (panelId == null) {
             panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
          }

      %>

    <script type="text/javascript" >
       function resetHeight() {
          setMainFrameHeight('<%= org.sakaiproject.util.Validator.escapeJavascript(panelId)%>');
       }

       function loaded() {
          resetHeight();
          parent.updCourier(doubleDeep, ignoreCourier);
          if (parent.resetHeight) {
             parent.resetHeight();
          }
       }
    </script>
  </head>

  <body onload="loaded();">
    <div class="portletBody">
        <h3><spring:message code="instructions.properties.title"/></h3>
        <p clase="instruction indnt2">
            <spring:message code="instructions.properties"/>
        </p>
        <form:form>
            <table class="form">
                <spring:bind path="command.description">
                    <tr>
                        <td class="form_text">
                            <label for="${status.expression}"><spring:message code="label.description"/></label>
                        </td>
                        <td>
                            <textarea name="${status.expression}" rows="5" cols="80"
                                   title='Description'>${status.value}</textarea>
                        </td>
                        <td><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
                    </tr>
                </spring:bind>
                <spring:bind path="command.copyright">
                    <tr>
                        <td>
                            <label for="${status.expression}"><spring:message code="label.copyright.status"/></label>
                        </td>
                        <td>
                            <select name="${status.expression}" id="${status.expression}">
                                <c:choose>
                                    <c:when test="${status.value}">
                                        <option value="" selected="selected"/>
                                    </c:when>
                                    <c:otherwise>
                                        <option value=""/>
                                    </c:otherwise>
                                </c:choose>
                                <option value=""></option>
                            <%
                                List<String>
                                    copyrightValues = (List<String>)request.getAttribute("copyrightValues");

                                for (String value : copyrightValues)
                                {
                                    %>
                                    <c:choose>
                                        <c:when test="${status.value && value == status.value}">
                                            <option value="<%=value%>" selected="selected">
                                                <%=value%>
                                            </option>
                                        </c:when>
                                        <c:otherwise>
                                            <option value="<%=value%>">
                                                <%=value%>
                                            </option>
                                        </c:otherwise>
                                    </c:choose>
                                    <%
                                }
                            %>
                            </select>
                        </td>
                    </tr>
                </spring:bind>
                <spring:bind path="command.copyrightAlertSelected">
                    <tr>
                        <td>
                            <label for="${status.expression}"><spring:message code="label.copyright.alert"/></label>
                        </td>
                        <td>
                            <input type="checkbox" id="${status.expression}" name="${status.expression}" value="true"/>
                            <spring:message code="copyright.checkbox.description"/>
                        </td>
                    </tr>
                </spring:bind>
                    <tr>
                        <td colspan="3">
                            <table>
                                <spring:bind path="command.showSelected">
                                    <tr>
                                        <td>
                                            <input type="radio" name="${status.expression}" id="showItemRadio" value="true"
                                                   checked="${status.value}"/>
                                        </td>
                                        <td>
                                            <label for="showItemRadio"><spring:message code="label.show"/></label>
                                        </td>
                                        <td colspan="2"/>
                                    </tr>
                                </spring:bind>
                                 <tr>
                                    <td/>
                                    <td>
                                <spring:bind path="command.releaseSelected">
                                        <input type="checkbox" id="releaseCheck" name="${status.expression}" value="true"/>
                                </spring:bind>
                                    </td>
                                     <td class="form_text">
                                         <label for="releaseCheck">
                                             <spring:message code="label.release"/>
                                         </label>
                                     </td>
                                <spring:bind path="command.startDate">
                                    <td>
                                       <rc:dateSelect daySelectId="startDateBean.day"
                                           yearSelectId  ="startDateBean.year"
                                           monthSelectId ="startDateBean.month"
                                           showTime="false"
                                           hideDate="false"
                                           earliestYear="2009"
                                           dateSpanId="occurrence_start_date_show"
                                           selected="${command.startDate}"/>
                                       <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
                                    </td>
                                </spring:bind>
                                 </tr>
                                 <tr>
                                     <td/>
                                     <td>
                                 <spring:bind path="command.retractSelected">
                                         <input type="checkbox" id="retractCheck" name="${status.expression}" value="true"/>
                                 </spring:bind>
                                     </td>
                                      <td class="form_text">
                                          <label for="retractCheck">
                                              <spring:message code="label.retract"/>
                                          </label>
                                      </td>
                                 <spring:bind path="command.endDate">
                                    <td>
                                       <rc:dateSelect daySelectId="endDateBean.day"
                                          yearSelectId  ="endDateBean.year"
                                          monthSelectId ="endDateBean.month"
                                          showTime="false"
                                          hideDate="false"
                                          earliestYear="2009"
                                          selected="${command.endDate}"/>
                                      <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
                                   </td>
                                 </spring:bind>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <spring:bind path="command.showSelected">
                            <td>
                                <input type="radio" name="${status.expression}" id="hideItemRadio" value="false"/>
                            </td>
                            <td>
                                <label for="hideItemRadio"><spring:message code="label.hide"/></label>
                            </td>
                        </spring:bind>
                    </tr>
                    <tr>
                        <td colspan="4">
                            <input type="submit" name="_cancel" value="<spring:message code='cancel'/>"/>
                            <input type="submit" name="_target2" value="<spring:message code='previous'/>"/>
                            <input type="submit" name="_finish" value="<spring:message code='finish'/>"/>
                        </td>
                    </tr>
            </table>
        </form:form>
    </div>
</body>