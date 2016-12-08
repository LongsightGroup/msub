<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<script language="javascript">
   function changeOccurrence() {
      var recurring = !document.edit_virtual_classroom_session_group_form.elements["schedule.recurring"][0].checked;

      document.edit_virtual_classroom_session_group_form.switchOccurrence.value = "true";
      document.edit_virtual_classroom_session_group_form.submit();
   }
</script>
<h3>
   <c:if test="${not command.updating}"><spring:message code="title_virtual_classroom_add" /></c:if>
   <c:if test="${    command.updating}"><spring:message code="title_virtual_classroom_edit"/></c:if>
</h3>
<div class="instruction">
   <c:if test="${not command.updating}"><spring:message code="vc_session_add_instructions" /></c:if>
   <c:if test="${    command.updating}"><spring:message code="vc_session_edit_instructions"/></c:if>
</div>

<form name="edit_virtual_classroom_session_group_form" method="post" action="edit_virtual_classroom_session_group.form">
   <osp:form/>
   <input type="hidden" id="switchOccurrence" name="switchOccurrence" value="false"/>
   <spring:bind path="command.id"><input type="hidden" name="${status.expression}" value="${status.value}"></spring:bind>

<!--  <div class="s_list_head"><div class="sect_ind"><div class="s_list_col2"><spring:message code="field_header_vc_edit_start_and_end_time"/></div></div></div> -->
   <table class="form">
      <spring:bind path="command.name">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_name"/></label></td>
            <td><input name="${status.expression}" type="text" length="37" maxlength="99" value="${status.value}" title='<spring:message code="field_title_vc_edit_name"/>'/></td>
            <td><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
         </tr>
      </spring:bind>

      <spring:bind path="command.capacity">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_capacity"/></label></td>
            <td><input type="text" name="${status.expression}" length="37" maxlength="6"  value="${status.value}" title='<spring:message code="field_title_vc_edit_capacity"/>'/><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
         </tr>
      </spring:bind>
       <spring:bind path="command.maxTalkers">
          <tr>
             <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_maxTalkers"/></label></td>
             <td><input type="text" name="${status.expression}" length="37" maxlength="6"  value="${status.value}" title='<spring:message code="field_title_vc_edit_maxTalkers"/>'/><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
          </tr>
       </spring:bind>
      <spring:bind path="command.schedule.startDate">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_start_time"/></label></td>
            <td>
               <rc:dateSelect daySelectId="schedule.startDateBean.day"
                   yearSelectId  ="schedule.startDateBean.year"
                   monthSelectId ="schedule.startDateBean.month"
                   hourSelectId  ="schedule.startDateBean.hour"
                   minuteSelectId="schedule.startDateBean.minute"
                   minuteInterval="15"
                   showTime="true"
                   hideDate="${command.schedule.recurring}"
                   earliestYear="2009"
                   defaultNow="true"
                   dateSpanId="occurrence_start_date_show"
                   selected="${command.schedule.startDate}"/>
               <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
            </td>
         </tr>
      </spring:bind>
      <spring:bind path="command.schedule.endDate">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_end_time"/></label></td>
            <td>
               <rc:dateSelect daySelectId="schedule.endDateBean.day"
                  yearSelectId  ="schedule.endDateBean.year"
                  monthSelectId ="schedule.endDateBean.month"
                  hourSelectId  ="schedule.endDateBean.hour"
                  minuteSelectId="schedule.endDateBean.minute"
                  showTime="true"
                  hideDate="${command.schedule.recurring}"
                  minuteInterval="15"
                  earliestYear="2009"
                  defaultNow="true"
                  selected="${command.schedule.endDate}"/>
              <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
           </td>
        </tr>
      </spring:bind>
      <spring:bind path="command.schedule.timeZone">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_time_zone"/></label></td>
            <td>
               <select name="${status.expression}">
                  <rc:timeZones id="tz" localeRef="localeRef" locale="true" style="LONG">
                     <option value="${tz.zoneId}" <c:if test="${tz.zoneId == timezone}">selected="selected"</c:if>>${tz.zoneId} (${tz.displayName})</option>
                  </rc:timeZones>
               </select>
               <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
            </td>
         </tr>
      </spring:bind>
      <spring:bind path="command.schedule.recurring">
         <tr>
            <td class="form_text"><span class="form_req">* </span><label for="${status.expression}"><spring:message code="field_label_vc_edit_occurence"/></label></td>
            <td>
               <input type="radio" id="${status.expression}" name="${status.expression}"  value="false" onclick="changeOccurrence()" <c:if test="${!command.schedule.recurring}">checked="checked"</c:if>/><spring:message code="field_label_vc_edit_occurence_one_time" /> &nbsp;&nbsp;&nbsp;
               <input type="radio" id="${status.expression}" name="${status.expression}"  value="true"  onclick="changeOccurrence()" <c:if test="${ command.schedule.recurring}">checked="checked"</c:if>/><spring:message code="field_label_vc_edit_occurence_recurring"/>
               <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
            </td>
         </tr>
      </spring:bind>
   </table>
   <br/><br/>

   <c:if test="${command.schedule.recurring}">
      <div class="s_list_head"><div class="sect_ind"><div class="s_list_col2"><spring:message code="field_header_vc_edit_recurrence_pattern"/></div></div></div>
      <table>
         <tr>
           <td class="form_text"><span class="form_req">* </span><spring:message code="field_label_vc_edit_recurrence_pattern"/></td>
           <td>
              <spring:bind path="command.schedule.daysOfWeek[1]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_monday"   /></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[2]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_tuesday"  /></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[3]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_wednesday"/></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[4]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_thursday" /></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[5]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_friday"   /></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[6]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_saturday" /></spring:bind>
              <spring:bind path="command.schedule.daysOfWeek[0]"><input type="hidden" name="_${status.expression}"/><input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if>/> <spring:message code="schedule_day_long_sunday"   /></spring:bind>
              <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
           </td>
        </tr>
       </table>
      <br/><br/>

      <div class="s_list_head"><div class="sect_ind"><div class="s_list_col2"><spring:message code="field_header_vc_edit_start_and_end_date"/></div></div></div>
      <table>
         <spring:bind path="command.schedule.startDate">
            <tr>
               <td class="form_text"><span class="form_req">* </span><spring:message code="field_label_vc_edit_start_date"/></td>
               <td>
                  <rc:dateSelect daySelectId="schedule.startDateBean.day"
                      yearSelectId="schedule.startDateBean.year"
                      monthSelectId="schedule.startDateBean.month"
                      hourSelectId="schedule.startDateBean.hour"
                      minuteSelectId="schedule.startDateBean.minute"
                      showTime="false"
                      earliestYear="2009"
                      defaultNow="true"
                      selected="${command.schedule.startDate}" />
                  <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
               </td>
            </tr>
         </spring:bind>
         <spring:bind path="command.schedule.endDate">
            <tr>
               <td class="form_text"><span class="form_req">* </span><spring:message code="field_label_vc_edit_end_date"/></td>
               <td>
                  <rc:dateSelect daySelectId="schedule.endDateBean.day"
                      yearSelectId="schedule.endDateBean.year"
                      monthSelectId="schedule.endDateBean.month"
                      hourSelectId="schedule.endDateBean.hour"
                      minuteSelectId="schedule.endDateBean.minute"
                      showTime="false"
                      earliestYear="2009"
                      defaultNow="true"
                      selected="${command.schedule.endDate}" />
                  <c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if>
               </td>
            </tr>
         </spring:bind>
      </table>
      <br/><br/>
   </c:if>

   <div class="s_list_head"><div class="sect_ind"><div class="s_list_col2"><spring:message code="field_header_vc_edit_options"/></div></div></div>
   <table>
      <spring:bind path="command.schedule.addEventToCalendar">
         <tr>
            <td><input type="checkbox" name="${status.expression}"  value="true" <c:if test="${status.value}">checked="checked"</c:if>/><input type="hidden" name="_${status.expression}"/><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
            <td class="form_text" style="text-align: left"><spring:message code="field_label_vc_edit_add_to_calendar"/></td>
         </tr>
      </spring:bind>
      <spring:bind path="command.addMeetingDateToSessionName">
         <tr>
            <td><input type="checkbox" name="${status.expression}"  value="true" <c:if test="${status.value}">checked="checked"</c:if>/><input type="hidden" name="_${status.expression}"/><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
            <td class="form_text"><spring:message code="field_label_vc_edit_add_meeting_date"/></td>
         </tr>
      </spring:bind>
      <spring:bind path="command.isPrivate">
           <tr>
              <td><input type="checkbox" name="${status.expression}"  value="true" <c:if test="${status.value}">checked="checked"</c:if>/><input type="hidden" name="_${status.expression}"/><c:if test="${status.error}"><div class="validation">${status.errorMessage}</div></c:if></td>
              <td class="form_text" style="text-align: left"><spring:message code="field_label_vc_edit_is_private"/></td>
           </tr>
       </spring:bind>
   </table>
   <br/><br/>

   <table>
      <tr>
         <td class="form_text"></td>
         <td>
            <input type="submit" name="_target1" class="active" value="<spring:message code='button_save'  />"/>
            <input type="submit" name="_cancel"                 value="<spring:message code='button_cancel'/>"/>
         </td>
      </tr>
   </table>
</form>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
