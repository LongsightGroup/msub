<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- Main Sakai tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<f:view>
	<f:verbatim>
	<!DOCTYPE html
	 PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	</f:verbatim>
	<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<title>Mailtool</title>
			<%= request.getAttribute("sakai.html.head.css.base") %>
			<%= request.getAttribute("sakai.html.head.css.skin") %>
			<%= request.getAttribute("sakai.html.head.js") %>
		</head>
		<body onload="<%=request.getAttribute("sakai.html.body.onload")%>;">
			<div class="portletBody">
				<h:form>
					<sakai:messages />

						<h:outputText escape="false" value="#{Mailtool.results}"/>
						<h:panelGroup rendered="#{Mailtool.sendMeCopy}">
							<f:verbatim><br/></f:verbatim>
							<h:outputText escape="false" value="#{msgs.result_copysenttothesender}" />
						</h:panelGroup>
						<h:panelGroup rendered="#{Mailtool.archiveMessage and Mailtool.emailArchiveInSite and Mailtool.allowedToArchiveMessage}">
							<f:verbatim><br/></f:verbatim>
							<h:outputText escape="false" value="#{msgs.result_copysenttoemailarchive1} " />
							<h:outputText escape="false" value="#{Mailtool.sitename}" />
							<h:outputText escape="false" value="#{msgs.result_copysenttoemailarchive2}" />
						</h:panelGroup>

					<sakai:button_bar>
						<sakai:button_bar_item
							action="main_onepage"
							styleClass="active"
							value="#{msgs.ok_button}"
							rendered="true"
							immediate="false" />
					</sakai:button_bar>	
				</h:form>
			</div>
		</body>
	</html>
</f:view>