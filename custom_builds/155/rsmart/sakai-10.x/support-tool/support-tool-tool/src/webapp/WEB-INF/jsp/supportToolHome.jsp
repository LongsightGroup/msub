<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<osp:url var="listUrl" value="support-tool.form"/>

<form name="support-tool" method="post" action="${listUrl}">
    <div class="viewNav">
        <table cellspacing="1">

            <tr>
                <td>

                    Search context or ref:
                    <c:if test="${!empty supportBean.context}" >
                        <c:set scope="page" var="searchValue">${supportBean.context}</c:set>
                    </c:if>
                    <c:if test="${!empty supportBean.ref}" >
                        <c:set scope="page" var="searchValue">${supportBean.ref}</c:set>
                    </c:if>
                    <input type="text" name="searchText" value="${searchValue}"/>
                    <select name="searchSelect">
                        <option value="context" <c:if test="${!empty supportBean.context}" >selected</c:if> >Context</option>
                        <option value="ref" <c:if test="${!empty supportBean.ref}" >selected</c:if> >Ref</option>
                    </select>
                </td>
                <td>
                    <div class="emptyStringError">
                        <c:out value="${errorString}"/>
                    </div>
                </td>

            </tr>

            <tr>
                <td>
                    Filter by event type:
                    <select name="filterEventType" size="1">
                            <option value="" <c:if test="${empty supportBean.type}">selected</c:if>>-- Any Event --</option>
                        <c:forEach var="eventType" items="${eventTypes}" varStatus="status">
                            <option value="${eventType}" <c:if test="${eventType eq supportBean.type}">selected</c:if> >${eventType}</option>
                        </c:forEach>
                    </select>
                
                </td>
                </tr>
            <tr>
                <td>
                    Start Date:
                    <rc:dateSelect daySelectId="schedule.startDateBean.day"
                                   yearSelectId  ="schedule.startDateBean.year"
                                   monthSelectId ="schedule.startDateBean.month"
                                   hourSelectId  ="schedule.startDateBean.hour"
                                   minuteSelectId="schedule.startDateBean.minute"
                                   minuteInterval="15"
                                   showTime="true"
                                   showAmPm="false"
                                   hideDate="${command.schedule.recurring}"
                                   earliestYear="1970"
                                   defaultNow="true"
                                   dateSpanId="occurrence_start_date_show"
                                   selected="${supportBean.startDate}"/>
                    End Date:
                    <rc:dateSelect daySelectId="schedule.endDateBean.day"
                                   yearSelectId  ="schedule.endDateBean.year"
                                   monthSelectId ="schedule.endDateBean.month"
                                   hourSelectId  ="schedule.endDateBean.hour"
                                   minuteSelectId="schedule.endDateBean.minute"
                                   showTime="true"
                                   showAmPm="false"
                                   hideDate="${command.schedule.recurring}"
                                   minuteInterval="15"
                                   earliestYear="2009"
                                   defaultNow="true"
                                   selected="${supportBean.endDate}"/>
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" class="active" value="Search"/>
                    <input type="button" name="_clear" value="Clear" onclick="location.href='${listUrl}&_clear&;'"/>
                </td>
            </tr>
        </table>
    </div>
</form>

<div class="listNav">
    <rc:listScroll listUrl="${listUrl}" listScroll="${listScroll}" className="pager"/>
</div>


 <table class="listHier" cellspacing="0">
      <tbody>
           <tr>
              <th><rc:sort name="EVENT_ID"       displayName="EVENT_ID"         sortUrl="${listUrl}"/></th>
              <th><rc:sort name="EVENT_DATE"    displayName="EVENT_DATE"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="EVENT_TYPE"    displayName="EVENT_TYPE"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="REF"    displayName="REF"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="CONTEXT"    displayName="CONTEXT"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="SESSION_ID"    displayName="SESSION_ID"      sortUrl="${listUrl}"/></th>
              <th><rc:sort name="EVENT_CODE"    displayName="EVENT_CODE"      sortUrl="${listUrl}"/></th>
           </tr>

           <c:forEach var="event" items="${events}" varStatus="status">
              <tr>
                 <td>
                   ${event.id}
                 </td>
                 <td>
                    ${event.date}
                 </td>
                 <td>
                    ${event.type}
                 </td>
                 <td>
                    ${event.ref}
                 </td>
                 <td>
                    ${event.context}
                 </td>
                 <td>
                    ${event.sessionId}
                 </td>
                 <td>
                    ${event.code}
                 </td>
              </tr>
           </c:forEach>
      </tbody>
  </table>