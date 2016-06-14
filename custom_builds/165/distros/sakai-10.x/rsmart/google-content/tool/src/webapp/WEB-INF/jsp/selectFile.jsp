<%@ include file="/WEB-INF/jsp/include.jsp" %>
<%@ page import="com.rsmart.content.google.api.GoogleDocsService" %>
<%@ page import="com.rsmart.content.google.api.GoogleDocDescriptor" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedList" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="com.rsmart.content.google.helper.GoogleDocumentLinkController" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.util.Iterator" %>

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

<%
		response.setContentType("text/html; charset=UTF-8");
%>

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

       function submitForm (link, docId, title, type, worksheetCount)
       {
           f = document.selectFileForm;

           f.link.value = link;
           f.docId.value = docId;
           f.title.value = title;
           f.type.value = type;
           f.worksheetCount.value = worksheetCount;

           f.submit();
       }
    </script>
    <link rel="StyleSheet" href="css/dtree.css" type="text/css" />
    <script type="text/javascript" src="js/dtree.js"></script>

  </head>

  <body onload="loaded();">
    <div class="portletBody">
      <h3><spring:message code="instructions.select.title"/></h3>
      <p class="instruction indnt2">
        <spring:message code="instructions.select"/>
      </p>
      <form name="selectFileForm" action="selectFile.form" method="POST">
          <input type="hidden" name="link" value=""/>
          <input type="hidden" name="docId" value=""/>
          <input type="hidden" name="title" value=""/>
          <input type="hidden" name="type" value=""/>
          <input type="hidden" name="worksheetCount" value=""/>
          <input type="hidden" name="_target2" value=""/>
            <%

            Log log = LogFactory.getLog(GoogleDocumentLinkController.class);

            Map<String, List<GoogleDocDescriptor>>
                folders = (Map<String, List<GoogleDocDescriptor>>)request.getAttribute("folders");
            List<GoogleDocDescriptor>
                tempFolder = null,
                folder = null;

            if (folders == null)
            {
                log.error ("folders not found in selectFile.jsp");
            %>
            <p>
                <spring:message code="documents.empty" />
            </p>
            <%
            }
            else
            {
            %>
          <div class="dtree">

	      <p><a href="javascript: d.openAll(); resetHeight();">open all</a> | <a href="javascript: d.closeAll();">close all</a></p>

      	  <script type="text/javascript">

          d = new dTree('d');

            <%
            int
                nodeId = 0,
                currFolderId = 0;
            %>
          d.add (<%=nodeId%>, -1, "Google Documents");
            <%

            Iterator folderIterator = folders.entrySet().iterator();

            while (folderIterator.hasNext()) {
                Map.Entry folderEntry = (Map.Entry) folderIterator.next();

                %>
                    d.add (<%=++nodeId%>,0,'<%=folderEntry.getKey().toString()%>','','','','img/folder.gif');
                <%

                currFolderId = nodeId;

                for (GoogleDocDescriptor doc : (List<GoogleDocDescriptor>)folderEntry.getValue()) {
                    String title = StringEscapeUtils.escapeJavaScript(doc.getTitle());

                    %>
                        d.add (<%=++nodeId%>,<%=currFolderId%>,'<%=title%>',"javascript:submitForm('<%=doc.getLink()%>','<%=doc.getDocId()%>','<%=title%>','<%=doc.getType()%>','<%=doc.getWorksheetCount()%>')");
                    <%
                }

                currFolderId++;
            }

            log.debug ("displaying tree");
            %>
		document.write(d);

	</script>

</div>
          <%
          }
          %>
          <input type="submit" name="_cancel" value="<spring:message code='cancel'/>"/>
      </form>
    </div>
  </body>
</html>
