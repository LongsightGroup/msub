<%@ page import="org.sakaiproject.fckeditor.spellcheck.GoogleSpellChecker" %>
<%@ page language="java" session="false" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>

<%
   GoogleSpellChecker googleSpellChecker = new GoogleSpellChecker(request.getParameter("textinputs[]"));
   pageContext.setAttribute("googleSpellChecker", googleSpellChecker);
%>


<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link rel="stylesheet" type="text/css" href="../spellerStyle.css"/>
    <script language="javascript" src="../wordWindow.js"></script>
    <script language="javascript">
        var suggs = new Array();
        var words = new Array();
        var textinputs = new Array();
        var error;
        var wordWindowObj = new wordWindow();

        textinputs[0] = decodeURIComponent("<%=request.getParameter("textinputs[]")%>");
        words[0] = [];
        suggs[0] = [];

        wordWindowObj.originalSpellings = ${googleSpellChecker.wordsAsJSON};
        wordWindowObj.suggestions = ${googleSpellChecker.suggestionsAsJSON};
        wordWindowObj.textInputs = textinputs;

        <c:if test="${googleSpellChecker.hasError()}" >
            error = "${googleSpellChecker.error}";
        </c:if>

        function init_spell() {
// check if any error occured during server-side processing
            if (error) {
                alert(error);
            } else {
// call the init_spell() function in the parent frameset
                if (parent.frames.length) {
                    parent.init_spell(wordWindowObj);
                } else {
                    alert('This page was loaded outside of a frameset. It might not display properly');
                }
            }
        }

    </script>

</head>
<!-- <body onLoad="init_spell();"> by FredCK -->
<body onLoad="init_spell();" bgcolor="#ffffff">

<script type="text/javascript">

    wordWindowObj.writeBody();
</script>
</body>
</html>
