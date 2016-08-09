<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

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
    <title>Launching...</title>
    <script type="text/javascript" language="JavaScript" src="/library/js/headscripts.js">
    </script>
  </head>

  <body>
      <div class="portletBody">
         <script>
         window.open("${url}", "_blank", "");
         </script>
         Your course is launching, if you do not see it in a new window click <a href="${url}" target="_blank">here</a>.
      </div>
   </body>
</html>


<script>
   history.back()
</script>