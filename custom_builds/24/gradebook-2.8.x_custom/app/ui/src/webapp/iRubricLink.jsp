<?xml version="1.0" encoding="UTF-8" ?>
<%-- Copyright (C) Reazon Systems, Inc.  All rights reserved. --%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%--- Call servlet to process data and send http request to iRubric server ---%>
<%
	String cmd = request.getParameter("p");
	if(cmd!=null && cmd.equals("a")){
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />
		</jsp:forward>
<%
	}else if(cmd!=null && cmd.equals("v")){ 
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />
			<jsp:param name="rosterStudentId" value='<%= request.getParameter("rosterStudentId")%>' />
		</jsp:forward>
<%
	}else if(cmd!=null && cmd.equals("gg")){ 
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />
			<jsp:param name="rosterStudentId" value='<%= request.getParameter("rosterStudentId")%>' />
			<jsp:param name="fieldToUpdate" value='<%= request.getParameter("fieldToUpdate")%>' />
		</jsp:forward>
<%
	}else if(cmd!=null && cmd.equals("g")){ 
 %>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />
			<jsp:param name="rosterStudentId" value='<%= request.getParameter("rosterStudentId")%>' />
		</jsp:forward>
<%
	}
	else if(cmd!=null && cmd.equals("ga")){
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />			
			<jsp:param name="rosterStudentId" value='<%= request.getParameter("rosterStudentId")%>' />
		</jsp:forward>
 <%
	}else if(cmd!=null && cmd.equals("gas")){ 
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>
			<jsp:param name="rosterStudentId" value='<%= request.getParameter("rosterStudentId")%>' />
			
			
		</jsp:forward>
<%
	}else if(cmd!=null && cmd.equals("gag")){ 
%>
		<jsp:forward page="/IRubricServlet">
			<jsp:param name="p" value='<%= cmd%>'/>			
			<jsp:param name="gradebookItemId" value='<%= request.getParameter("gradebookItemId")%>' />
		</jsp:forward>
<%
	}else{ 
%>
	<P>You can't access this page!</P>
<%
	}
%>