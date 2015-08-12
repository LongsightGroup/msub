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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%
		response.setContentType("text/html; charset=UTF-8");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" media="all" href="<c:url value="/css/metaobj.css"/>" />
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
    <script language="JavaScript" src="/osp-common-tool/js/eport.js"></script>
  <%
      String panelId = request.getParameter("panel");
      if (panelId == null) {
         panelId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
      }

  %>

  <script language="javascript">
   function resetHeight() {
      setMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>');
   }



   function loaded() {
      resetHeight();
      parent.updCourier(doubleDeep, ignoreCourier);
      if (parent.resetHeight) {
         parent.resetHeight();
      }
   }


    function createLink (UrlAdd){
          alert(UrlAdd);
          var initialUrl =  UrlAdd ;
          top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = initialUrl;
          alert(initialUrl);
          window.location = initialUrl;
    }




    function dynamicUrl(Url){

        var urlString;
        if (Url =='beginEdit.form'){
            urlString='beginEdit.form';
        } else if (Url =='deleteProvider.form') {
           urlString=='deleteProvider.form';
        }  else if (Url == 'oauthAddFile'){
           urlString=='oauthAddFile.form'
        }

        top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = urlString;
        window.location = urlString;

    }

    //parsing the values to separate the id and the provider name....provider name is added to the window.alert
   function parseValues(UrlAdd){
       var checkedNames = new Array();
       var checkedIds = new Array();
       var checkBoxValue ="";
       var editValue;

       var selectedMembers=document.getElementsByName("uuid");

       for (var i=0; i < selectedMembers.length; i++)
           {
            if (selectedMembers[i].checked == true){
                checkBoxValue =selectedMembers[i].value;
                var myStringList = checkBoxValue.split(',');
                checkedIds[i] = myStringList[0];
                checkedNames[i]=myStringList[1];

                if (UrlAdd =='beginEdit.form'){
                    editValue = checkedIds[i];
                    break;
                    }

               }//end of if statement
         }  //end of for loop

        if ( checkedIds.length == 0 && checkedNames.length == 0){
            if ( UrlAdd == 'deleteProvider.form'){
               var editUrl = UrlAdd +"?uuid=none";
               top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = editUrl;
               window.location = editUrl;
               return;
            }else if (UrlAdd == 'beginEdit.form'){
               var editUrl = UrlAdd +"?uuid=none";
               top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = editUrl;
               window.location = editUrl;
               return;
            }


        }

       if (UrlAdd == 'deleteProvider.form'){
            var formattedNames = removeEmptyValues(checkedNames);
            submitForm(UrlAdd, checkedIds, formattedNames);
        }
       if (UrlAdd == 'beginEdit.form'){
            var editUrl = UrlAdd +"?uuid=" + editValue;
            top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = editUrl;
            window.location = editUrl;
         }
   }

   function removeEmptyValues(checkedNames){
      var names="";
      var checkedNam ="";
      var spiltData = checkedNames.join().split(",");

       for (var i=0; i < spiltData.length; i++){
  	     checkedNam = spiltData[i];

  	    if ( checkedNam.length > 1){
  	       names= names + spiltData[i] + "," +"\n";
         }
      }

       return names;
   }



    //Submit form
    function submitForm(Url,idArray,nameArray)
    {
        var deleteUrl = Url;
        var x=window.confirm("'<spring:message code="label.messsage.confirm.delete"/> ''" + '\n'+  nameArray);
        if (x)
        {
            deleteUrl = Url +"?uuid=" + idArray;
            top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = deleteUrl;
            window.location = deleteUrl;
        }
        else
        {
        // back to the page
        }
    }


  function addUrl(UrlAdd){
      var  initialUrl =  UrlAdd;
      top.frames['<%= org.sakaiproject.util.Web.escapeJavascript(panelId)%>'].location.href = initialUrl;
      window.location = initialUrl;
   }

  </script>
  </head>

  <body onload="loaded();">
      <div class="portletBody">
         <c:if test="${not empty requestScope.panelId}"><div class="ospEmbedded"></c:if>
 <div class="navIntraTool">
    <a href="" onclick="addUrl('newProvider.form'); return false;">
      Add
     </a>
    <a href="" onclick="parseValues('beginEdit.form'); return false;">
      Edit
     </a>
    <a href="" onclick="parseValues('deleteProvider.form'); return false;">  Delete
     </a>

 </div>

<c:if test="${not empty msg}">
<div class="alertMessage">${msg}</div>
</c:if>
<c:if test="${not empty error}">
<div class="alertMessage">${error}</div>
</c:if>
