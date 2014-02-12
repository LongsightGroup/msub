<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%-- Main Sakai tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<f:view>

	<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
			<title>Mailtool</title>
			<%= request.getAttribute("sakai.html.head.css.base") %>
			<%= request.getAttribute("sakai.html.head.css.skin") %>
			<%= request.getAttribute("sakai.html.head.js") %>
		</head>
		<body onload="<%=request.getAttribute("sakai.html.body.onload")%>;">
		<div class="portletBody">
			<h:form id="mainForm" enctype="multipart/form-data">
				<h:panelGroup rendered="#{Mailtool.allowedToConfigure}">
					<sakai:tool_bar>
						<sakai:tool_bar_item value="#{msgs.options_toolbar}" action="#{Mailtool.processGoToOptions}" immediate="true" />
					</sakai:tool_bar>
				</h:panelGroup>
				<sakai:messages />
				<%--
				<h:panelGroup rendered="#{not Mailtool.allowedToSend}">
					<f:verbatim><br/></f:verbatim> 
					<h:outputText escape="false" value="#{msgs.no_mail_permission}<br/>" />
				</h:panelGroup>
				<h:panelGrid columns="2" rendered="#{Mailtool.allowedToSend}" cellspacing="0" cellpadding="3" columnClasses="mail-header, mail-inputs">
				--%>
				<sakai:view_title  value="#{msgs.compose_toolbar}" />
				<%--above should be instead:
				<sakai:view_title value="#somethingfromthemessagebundle"/>
				--%>
				<h:panelGrid columns="2" cellspacing="0" cellpadding="0" border="0" styleClass="jsfFormTable">
					<h:panelGroup style="display:block;height:100%;overflow:hidden;float:right;padding:0 .5em 0 0">
						<h:outputText rendered="#{Mailtool.replyToSelected != 'otheremail'}" value="#{msgs.from}" style="float:right" />
						<h:outputText rendered="#{Mailtool.replyToSelected == 'otheremail'}" value="#{msgs.replyto}" style="float:right"/>
					</h:panelGroup>
					<h:panelGroup>
						<h:outputText rendered="#{Mailtool.replyToSelected == 'yes' }" value="#{Mailtool.currentUser.displayname} <#{Mailtool.currentUser.email}>"/>
						<h:outputText rendered="#{Mailtool.replyToSelected == 'otheremail' }" value="#{Mailtool.replyToOtherEmail}"/>
						<h:outputText rendered="#{Mailtool.replyToSelected == 'no' }" value="#{msgs.noreply}"/>
					</h:panelGroup>
					<%--
							<h:outputText escape="false" value="View: " />
							<h:selectOneListbox onchange="submit(); return false;" size="1" id="viewChoice" value="#{Mailtool.viewChoice}">
								<f:selectItems value="#{Mailtool.viewChoiceDropdown}" />
							</h:selectOneListbox>
					--%>
					<h:outputText escape="false" value="#{msgs.send_mail_to} " style="display:block;height:100%;overflow:hidden;float:right;padding:0 .5em 0 0"/>
					<h:panelGroup style="font-weight:bold;display:block;height:100%;overflow:hidden">
						<%--
						
									<h:panelGrid columns="2" >
											<h:selectBooleanCheckbox value="#{Mailtool.allUsersSelected }" />
											<h:outputText value="All Users" style="font-weight: bold" />
									</h:panelGrid>
						--%>
						<f:subview id="selectByUser" rendered="#{Mailtool.selectByUser}"><jsp:include page="selectByUser.jsp" /></f:subview>
<%--						<f:subview id="selectByRole" rendered="#{Mailtool.selectByRole}"><jsp:include page="selectByRole.jsp" /></f:subview>
--%>						<f:subview id="selectByTree" rendered="#{Mailtool.selectByTree}"><jsp:include page="selectByTree.jsp" /></f:subview>
						<f:subview id="selectSideBySide" rendered="#{Mailtool.selectSideBySide}"><jsp:include page="selectSideBySide.jsp" /></f:subview>
						<f:subview id="selectByFoothill" rendered="#{Mailtool.selectByFoothill}"><jsp:include page="selectByFoothill.jsp" /></f:subview>
					</h:panelGroup>
					<h:outputText value="" />
					<h:panelGroup>
						<h:outputLabel value="#{msgs.otherrecipients} " styleClass="block" for="email"/>
<%--						<h:inputText value="#{Mailtool.otherEmails}" size="40" id="email"/>
--%>
						<h:inputText value="#{Mailtool.otherEmails}" size="40" validator="#{Mailtool.validateEmail}" id="email"/>
						<h:outputText value="#{msgs.otherrecipients_instruction}" styleClass="instruction" style="display:block" />
					</h:panelGroup>
					<h:outputText value="#{msgs.message_subject} " style="display:block;height:100%;overflow:hidden;float:right;padding:.5em .5em 0 0" />
					<h:panelGrid columns="2">
						<%--
								<h:outputText value="#{Mailtool.subjectPrefix}"/>
						--%>
					<h:inputText value="#{Mailtool.messageSubject}"	size="60" required="true">
						<f:validateLength minimum="1"/>
					</h:inputText>

					</h:panelGrid>
					<h:panelGroup rendered="#{not Mailtool.attachClicked and Mailtool.maxNumAttachment!=0}" style="height:100%;display:block;overflow:hidden;vertical-align: middle;padding:.2em .5em 0 0">
						<h:graphicImage alt="attachment_img" url="/images/paperclip.gif"  width="15" height="15"  style="float:right;vertical-align: middle;" />
					</h:panelGroup>
					<h:panelGroup rendered="#{not Mailtool.attachClicked and Mailtool.maxNumAttachment!=0}">
						<h:panelGroup rendered="#{Mailtool.num_files == 0}"><h:commandLink action="#{Mailtool.toggle_attachClicked}"><h:outputText value="#{msgs.attachlink}" /></h:commandLink></h:panelGroup>
						<h:panelGroup rendered="#{Mailtool.num_files != 0}"><h:commandLink action="#{Mailtool.toggle_attachClicked}"><h:outputText value="#{msgs.attachanotherlink}" /></h:commandLink></h:panelGroup>
						<f:verbatim><br/></f:verbatim>
					</h:panelGroup>
					<h:panelGroup  rendered="#{Mailtool.attachClicked}" style="height:100%;display:block;overflow:hidden;vertical-align: middle;padding:1em .5em 0 0">
							<h:graphicImage alt="attachment_img" url="/images/paperclip.gif"  width="15" height="15"   style="float:right" />
					</h:panelGroup>
					<h:panelGrid rendered="#{Mailtool.attachClicked}" columns="5" border="0" cellspacing="0" cellpadding="0" styleClass="listHier lines nolines hierItemBlockWrapper" style="margin:0">
						<sakai:inputFileUpload size="50" valueChangeListener="#{Mailtool.processFileUpload}" />
						<h:commandButton value="#{msgs.attachbutton}" type="submit"  action="#{Mailtool.toggle_attachClicked}" />
						<h:outputText value=" #{msgs.size_prefix} " />
						<h:outputText value="#{msgs.max_upload_size}" />
						<h:outputText value=" #{msgs.size_suffix}" />
					</h:panelGrid>
					<h:panelGroup> 
						<h:outputText value=" " />
					</h:panelGroup>
					<h:panelGroup>
						<h:dataTable value="#{Mailtool.allAttachments}" var="attachment"  cellspacing="0" cellpadding="0" width="100%" styleClass="listHier lines nolines" style="margin:0" columnClasses="attach,,itemAction">
							<h:column><h:outputText  value="  "/></h:column>
							<h:column><h:outputText  value="#{attachment.filename} "/></h:column>
							<h:column><h:outputText value="#{attachment.size/1000}"><f:convertNumber pattern="(### KB)"/></h:outputText></h:column>
							<h:column>
								<h:commandLink action="#{Mailtool.processRemoveFile}"><h:outputText value="#{msgs.removelink}" />
									<f:param name="id" value="#{attachment.filename}" />
								</h:commandLink>
							</h:column>
						</h:dataTable>
					</h:panelGroup>
					<h:panelGroup>
						<h:outputText  value="  "/>
					</h:panelGroup>
					<h:panelGroup>
						<h:outputText value="#{msgs.message_body}"/>
						<h:panelGroup>
							<h:panelGroup rendered="#{Mailtool.textFormat=='htmltext'}">

								<sakai:rich_text_area  rows="10" columns="70" value="#{Mailtool.messageBody}"/>
<%--
								<sakai:inputRichText rows="25" cols="70" value="#{Mailtool.messageBody}"/>
--%>
							</h:panelGroup>
							<h:panelGroup rendered="#{Mailtool.textFormat=='plaintext'}">
								<h:inputTextarea rows="10" cols="70" value="#{Mailtool.messageBody}"/>
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup>
							<h:panelGrid columns="1">
								<h:panelGroup>
									<h:selectBooleanCheckbox value="#{Mailtool.sendMeCopy}" id="sendmeacopy"/>
									<h:outputLabel for ="sendmeacopy"  value="#{msgs.sendmeacopy}" />
								</h:panelGroup>	
								<h:panelGroup rendered="#{Mailtool.emailArchiveInSite and Mailtool.allowedToArchiveMessage}">
									<h:selectBooleanCheckbox value="#{Mailtool.archiveMessage}" id="addtoarchive" />
									<h:outputLabel for ="addtoarchive" value="#{msgs.addtoemailarchive}" />
								</h:panelGroup>
							</h:panelGrid>	
							<h:panelGrid columns="2" cellspacing="5" cellpadding="0" border="0" styleClass="act">
								<sakai:button_bar>
									<sakai:button_bar_item action="#{Mailtool.processSendEmail}" value="#{msgs.send_mail_button}" immediate="false" styleClass="active" />
								</sakai:button_bar>	
								<sakai:button_bar>
									<sakai:button_bar_item action="#{Mailtool.processCancelEmail}" value="#{msgs.cancel_mail_button}" immediate="true" />
								</sakai:button_bar>
							</h:panelGrid>							
						</h:panelGroup>
					</h:panelGroup>	
				</h:panelGrid>
			</h:form>
		</div>	
		</body>
	</html>
</f:view>