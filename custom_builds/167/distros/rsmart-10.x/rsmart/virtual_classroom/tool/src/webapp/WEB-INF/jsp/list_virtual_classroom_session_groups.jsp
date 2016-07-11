<%@ include file="/WEB-INF/jsp/include.jsp" %>
<jsp:include page="/WEB-INF/jsp/header.jsp"/>

<c:set scope="request" var="date_format"><spring:message code="date_format"     /></c:set>
<c:set scope="request" var="dateFormat" ><spring:message code="date_format_short"/></c:set>

<!-- store the user's permissions in the variable 'can' -->
<osp:authZMap prefix="virtual_classroom_session." var="can"/>

<c:if test="${can.delete}">
   <script language="javascript">
   function setChecked(pageSize) {
      // whether the user checked or unchecked the "check_all" box at the top of the list
      var checked = (document.virtual_classroom_session_groups_list_form.check_all.checked ? "checked" : false);

      for(i=0; i<pageSize; ++i) {
         if (document.getElementById("check"+i) != null)
            document.getElementById("check"+i).checked = checked;
      }
   }

   function deleteSelectedVCSessions() {
      document.virtual_classroom_session_groups_list_form.command.value="delete";
      document.virtual_classroom_session_groups_list_form.submit();
   }
   </script>
</c:if>
<c:if test="${isMaintainer}">
   <script language="javascript">
   function pingVirtualClassroomServer() {
      document.virtual_classroom_session_groups_list_form.command.value="ping_vc_server";
      document.virtual_classroom_session_groups_list_form.submit();
   }
   function checkServerTimeZones() {
      document.virtual_classroom_session_groups_list_form.command.value="check_server_time_zones";
      document.virtual_classroom_session_groups_list_form.submit();
   }
   </script>
</c:if>
<c:if test="${can.join}">
   <script language="javascript">
   function joinVirtualClassroomSession(id) {
      document.virtual_classroom_session_groups_list_form.command.value="join";
      document.virtual_classroom_session_groups_list_form.virtual_classroom_session_id.value=id;
      document.virtual_classroom_session_groups_list_form.submit();
   }

   function viewRecordedVirtualClassroomSession(id, recordingId) {
      document.virtual_classroom_session_groups_list_form.command.value="view";
      document.virtual_classroom_session_groups_list_form.elluminate_id.value=id;
      document.virtual_classroom_session_groups_list_form.elluminate_recording_id.value=recordingId;
      document.virtual_classroom_session_groups_list_form.submit();
   }
   </script>
</c:if>
<c:if test="${can.view}">
   <script language="javascript">
   function submitFilter(pageSize) {
      document.virtual_classroom_session_groups_list_form.command.value="list";
      document.virtual_classroom_session_groups_list_form.submit();
   }
   </script>
</c:if>

<div class="navIntraTool">
    <c:if test="${can.create}">
       <a href="<osp:url value='edit_virtual_classroom_session_group.form'/>&resetForm=true" title="<spring:message code='title_add_new_virtual_classroom_session'/>"><spring:message code="vc_list_command_add"/></a>
    </c:if>
    <c:if test="${can.delete}">
       <a href="#" onClick="deleteSelectedVCSessions()" title="<spring:message code='title_delete_selected_vc_sessions'/>"><spring:message code="vc_list_command_delete"/></a>
    </c:if>
    <c:if test="${isMaintainer}">
       <a href="<osp:url value="osp.permissions.helper/editPermissions">
                <osp:param name="message">
                  <spring:message code='title_set_permissions' arguments='${tool.title},${site.title}'/>
                </osp:param>
                <osp:param name="name" value="virtualClassroom"/>                
                <osp:param name="qualifier" value="${tool.id}"/>
                <osp:param name="returnView" value="list_virtual_classroom_session_groups"/>
                <osp:param name="session.${lastViewKey}" value="/list_virtual_classroom_session_groups.form"/>
             </osp:url>"
             title="<spring:message code='title_set_permissions' arguments='${tool.title},${site.title}'/>"><spring:message code="vc_list_command_permissions"/></a>
    </c:if>

    <c:if test="${isMaintainer}">
       <a href="#" onClick="pingVirtualClassroomServer()" title="<spring:message code='title_ping_virtual_classroom_server'/>"><spring:message code="vc_list_command_ping_vc_server"  /></a>
      <!-- <a href="#" onClick="checkServerTimeZones()"       title="<spring:message code='title_check_server_time_zones'      />"><spring:message code="vc_list_command_check_time_zones"/></a>-->
    </c:if>
</div>

<h3><spring:message code="title_virtual_classroom_list"/></h3>

<!-- <div class="instruction"><spring:message code="vc_list_instructions"/></div> -->
<c:if test="${not empty       error}"><div class="validation">${      error}</div></c:if>
<c:if test="${not empty param.error}"><div class="validation">${param.error}</div></c:if>

<c:if test="${!can.view}">
   <div class="validation"><spring:message code="error_authorization.view"/></div>
</c:if>
<c:if test="${can.view}">
   <osp:url var="listUrl" value="list_virtual_classroom_session_groups.form"/>
   <table cellspacing="0" id="rsmart_filter_pager_id">
      <tr>
         <td align="left" valign="bottom"><br/><rc:listFilter filterUrl="${listUrl}" showFilterButton="false"/></td>
         <td align="right" id="rsmart_filter_pager_id_td_2"><rc:listScroll listUrl="${listUrl}" listScroll="${listScroll}" className="pager"/></td>
      </tr>
   </table>

   <form name="virtual_classroom_session_groups_list_form" method="post" action="list_virtual_classroom_session_groups.form">
      <input name="command"                      type="hidden" value="list"                    />
      <input name="pageSize"                     type="hidden" value="${list_scroll_page_size}"/>
      <input name="virtual_classroom_session_id" type="hidden" value=""                        />
      <input name="elluminate_id"                type="hidden" value=""                        />
      <input name="elluminate_recording_id"      type="hidden" value=""                        />

      <table class="listHier" cellspacing="0">
          <tbody>
               <tr>
                  <th>&nbsp;                                                                                            </th>
                  <th><rc:sort name="join"       displayName="vc_list_column_header_join"         sortUrl="${listUrl}"/></th>
                  <th><rc:sort name="subject"    displayName="vc_list_column_header_subject"      sortUrl="${listUrl}"/></th>
                  <th><spring:message                   code="vc_list_column_header_meeting_time"                     /></th>
                  <th><spring:message                   code="vc_list_column_header_duration"                         /></th>
                  <th><rc:sort name="instructor" displayName="vc_list_column_header_moderator"    sortUrl="${listUrl}"/></th>
                  <th><c:if test="${can.delete}"><input name="check_all" type="checkbox" onChange='setChecked("${list_scroll_page_size}");'/><spring:message code="vc_list_column_header_check_all"/></c:if><c:if test="${!can.delete}">&nbsp;</c:if></th>
               </tr>

               <c:forEach var="virtual_classroom_session_group_form" items="${virtual_classroom_session_group_forms}" varStatus="status">
                  <tr>
                     <td>
                        ${list_scroll_begin_index + status.index}.
                     </td>
                     <td>
                        <c:if test="${ virtual_classroom_session_group_form.inProgress}">
                           <c:if test="${ virtual_classroom_session_group_form.joinable}"><c:if test="${can.join}"><a href="#" onClick='joinVirtualClassroomSession        ("${virtual_classroom_session_group_form.joinableSession.id}");'                                                                                title='<spring:message code="title_vc_session_click_to_join"/>'                            ><spring:message code="vc_list_join"  /></a>  </c:if><c:if test="${!can.join}"><div title='<spring:message code="error_authorization.join"/>'          >&nbsp;</div></c:if></c:if>
                           <c:if test="${!virtual_classroom_session_group_form.joinable}"><c:if test="${can.join}">                                                                                                                                                                                                   <div title='<spring:message code="title_vc_session_cant_join_yet" arguments="${grace_period}"/>'><spring:message code="vc_list_future"/></div></c:if><c:if test="${!can.join}">&nbsp;                                                                         </c:if></c:if>
                        </c:if>
                        <c:if test="${!virtual_classroom_session_group_form.inProgress}">
                           <c:if test="${ virtual_classroom_session_group_form.recorded}"><c:if test="${can.join}"><a href="#" onClick='viewRecordedVirtualClassroomSession("${virtual_classroom_session_group_form.recording.elluminateId}", "${virtual_classroom_session_group_form.recording.elluminateRecordingId}");' title='<spring:message code="title_vc_session_click_to_view"/>'                            ><spring:message code="vc_list_watch" /></a>  </c:if><c:if test="${!can.join}"><div title='<spring:message code="error_authorization.view_recording"/>'>&nbsp;</div></c:if></c:if>
                           <c:if test="${!virtual_classroom_session_group_form.recorded and !virtual_classroom_session_group_form.ended}">                                                                                                                                                                          <div title='<spring:message code="title_vc_session_not_started"/>'                              ><spring:message code="vc_list_future"/></div></c:if>
                           <c:if test="${!virtual_classroom_session_group_form.recorded and  virtual_classroom_session_group_form.ended  }">                                                                                                                                                                          <div title='<spring:message code="title_vc_session_already_occurred"/>'                         ><spring:message code="vc_list_past"  /></div></c:if>
                        </c:if>
                     </td>
                     <td>
                        <c:if test="${ can.edit}">
                           <c:if test="${ virtual_classroom_session_group_form.recorded}">${virtual_classroom_session_group_form.name}</c:if>
                           <c:if test="${!virtual_classroom_session_group_form.recorded}">
                              <c:if test="${ virtual_classroom_session_group_form.inProgress}">${virtual_classroom_session_group_form.name}</c:if>
                              <c:if test="${!virtual_classroom_session_group_form.inProgress}">
                                 <a href='<osp:url value="edit_virtual_classroom_session_group.form"><osp:param name="id" value="${virtual_classroom_session_group_form.id}"/></osp:url>' title='<spring:message code="title_vc_session_click_to_edit"/>'>${virtual_classroom_session_group_form.name}</a>
                              </c:if>
                           </c:if>
                        </c:if>
                        <c:if test="${!can.edit}">${virtual_classroom_session_group_form.name}</c:if>
                     <td>
                        ${virtual_classroom_session_group_form.schedule.displayName}
                     </td>
                     <td>
                        <c:if test="${ virtual_classroom_session_group_form.recorded}"><spring:message code="vc_list_recording"/>                                      </c:if>
                        <c:if test="${!virtual_classroom_session_group_form.recorded}"><rc:duration value="${virtual_classroom_session_group_form.schedule.duration}"/></c:if>
                     </td>
                     <td>${virtual_classroom_session_group_form.instructorName}</td>
                     <td>
                        <c:if test="${!can.delete}">&nbsp;</c:if>
                        <c:if test="${ can.delete}">
                           <c:if test="${ virtual_classroom_session_group_form.inProgress}">&nbsp;</c:if>
                           <c:if test="${!virtual_classroom_session_group_form.inProgress}"><input id='<c:out value="check${status.index}"/>' name='<c:out value="check${status.index}"/>' type="checkbox" title='<spring:message code="title_vc_session_mark_for_deletion"/>' value="${virtual_classroom_session_group_form.id}"/></c:if>
                        </c:if>
                     </td>
                  </tr>
               </c:forEach>
          </tbody>
      </table>
   </form>
</c:if>
<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
