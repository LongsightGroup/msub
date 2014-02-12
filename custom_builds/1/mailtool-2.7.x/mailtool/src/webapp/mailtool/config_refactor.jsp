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
			<title>Options</title>
				<%= request.getAttribute("sakai.html.head.css.base") %>
				<%= request.getAttribute("sakai.html.head.css.skin") %>
				<%= request.getAttribute("sakai.html.head.js") %>
		</head>
		<body onload="<%=request.getAttribute("sakai.html.body.onload")%>;">
			<div class="portletBody">
				<h:form id="optionsForm">
					<sakai:tool_bar>
						<sakai:tool_bar_item value="#{msgs.compose_toolbar}" action="#{Option.processGoToComposeByCancel}" immediate="true" />
					</sakai:tool_bar>
					<sakai:messages />
					<sakai:view_title  value="#{msgs.options_toolbar}" />
					<h:outputText value="#{msgs.options_instruction}"  styleClass="instruction" style="display: block;"/>
					<h:panelGrid columns="2" border="0" cellspacing="0" cellpadding="0"  styleClass="jsfFormTable itemSummary">
						<h:panelGroup style="padding: 0pt; overflow: hidden; display: block; height: 100%; float: right;">
							<h:outputLabel value="#{msgs.options_chooseselectionview}" for="viewChoice"/>
						</h:panelGroup>
						<h:panelGroup>
							<h:selectOneListbox onchange="submit(); return false;" size="1" id="viewChoice" value="#{Option.viewChoice}">
									<f:selectItems value="#{Option.viewChoiceDropdown}" />
							</h:selectOneListbox>
							<f:subview id="selectByUser" rendered="#{Option.selectByUser}"><jsp:include page="selectByUser_option.jsp" /></f:subview>
							<f:subview id="selectByTree" rendered="#{Option.selectByTree}"><jsp:include page="selectByTree_option.jsp" /></f:subview>
							<f:subview id="selectSideBySide" rendered="#{Option.selectSideBySide}"><jsp:include page="selectSideBySide_option.jsp" /></f:subview>
							<f:subview id="selectByFoothill" rendered="#{Option.selectByFoothill}"><jsp:include page="selectByFoothill_option.jsp" /></f:subview>
						</h:panelGroup>
						<h:panelGroup style="padding: 0pt; overflow: hidden; display: block; height: 100%; float: right;">
							<h:outputText value="#{msgs.options_copies}" />
						</h:panelGroup>	

						<h:panelGroup styleClass="checkbox">
							<h:selectBooleanCheckbox value="#{Option.sendMeCopy}" id="sendMeACopyID"/>
							<h:outputLabel value="#{msgs.sendmeacopy}" for="sendMeACopyID"/>
						</h:panelGroup>
						<h:panelGroup>
							<h:outputText value=" " />
						</h:panelGroup>

						<h:panelGroup rendered="#{Option.emailArchiveInSite and Mailtool.allowedToArchiveMessage}"styleClass="checkbox">
							<h:selectBooleanCheckbox value="#{Option.archiveMessage}" id="achiveMessageID"/>
							<h:outputLabel value="#{msgs.addtoemailarchive}" for="achiveMessageID" />
						</h:panelGroup>
						<h:panelGroup rendered="#{not Option.emailArchiveInSite or not Mailtool.allowedToArchiveMessage}" style="height:100%;overflow:hidden;display:block;color:#555 !important" styleClass="checkbox">
							<h:selectBooleanCheckbox disabled="true" value="#{Option.archiveMessage}" id="achiveMessageID2"/>
							<h:outputLabel value="#{msgs.addtoemailarchive}" for="achiveMessageID2" style="color:#777;white-space:nowrap"/>
						</h:panelGroup>

						<h:panelGroup style="padding: 0pt; overflow: hidden; display: block; height: 100%; float: right;">
							<h:outputText value="#{msgs.replyto}" />
						</h:panelGroup>
						<h:panelGroup>
							<h:selectOneRadio layout="pageDirection" value="#{Option.replyToSelected }" onclick="submit()" styleClass="checkbox">
								<f:selectItem itemLabel="#{msgs.options_replylabel1}" itemValue="yes"/>
								<f:selectItem itemLabel="#{msgs.options_replylabel2}" itemValue="no"/> 
							</h:selectOneRadio>

						</h:panelGroup>
						<h:panelGroup style="padding: 0pt; overflow: hidden; display: block; height: 100%; float: right;">
							<h:outputText value="#{msgs.options_messageformat}" />
						</h:panelGroup>
						<h:panelGroup>
							<h:selectOneRadio layout="pageDirection" value="#{Option.textFormat}" onclick="submit()"  styleClass="checkbox">
								<f:selectItem itemLabel="#{msgs.options_textformat1}" itemValue="htmltext"/>
								<f:selectItem itemLabel="#{msgs.options_textformat2}" itemValue="plaintext"/>
							</h:selectOneRadio>
						</h:panelGroup>
						<f:facet name="footer">
						  <h:panelGroup rendered="#{Option.showRenamingRoles}">
							<h:panelGroup rendered="#{not Option.showRenamingRolesClicked }">
							  <h:commandLink action="#{Option.toggle_showRemainingRoleClicked}" value=" #{msgs.options_showrenameroles}" style="text-decoration: underline"/>
							</h:panelGroup>
							<h:panelGrid rendered="#{Option.showRenamingRolesClicked }" columns="1" styleClass="jsfFormTable itemSummary">
							  <h:panelGroup>
								<f:verbatim><h4></f:verbatim>
									<h:outputText value="#{msgs.options_renameroles} - " />
							  		<h:commandLink action="#{Option.toggle_showRemainingRoleClicked}" value=" #{msgs.options_hiderenameroles}"  style="text-decoration: underline;font-weight:normal"/>
								<f:verbatim></h4></f:verbatim>
							  </h:panelGroup>
								<h:outputText value="#{msgs.options_renamerolesinstruction}"  styleClass="instruction" style="display: block;"/>
								<h:dataTable value="#{Option.renamedRoles}" var="role"  cellspacing="0" cellpadding="0"  width="100%" >
									<h:column>
										<f:verbatim><h5></f:verbatim>
											<h:outputText value="#{role.roleId}: "/>
										<f:verbatim></h5></f:verbatim>
										<h:outputLabel value="#{msgs.options_renameroles_singular}" for="rolesingular"/>
										<f:verbatim><br/></f:verbatim>
										<h:inputText size="20" value="#{role.singularNew}" id="rolesingular"/>
										<f:verbatim><br/></f:verbatim>
										<h:outputText value="#{msgs.examplesign} #{role.singular }" styleClass="instruction" style="display: block;"/>
										<f:verbatim><br/></f:verbatim>
										<h:outputLabel value="#{msgs.options_renameroles_plural}" for="roleplural"/>
										<f:verbatim><br/></f:verbatim>
										<h:inputText size="20" value="#{role.pluralNew}" id="roleplural"/>
										<f:verbatim><br/></f:verbatim>
										<h:outputText value="#{msgs.examplesign} #{role.plural }" styleClass="instruction" style="display: block;"/>
									</h:column>
								</h:dataTable>
							</h:panelGrid>
						  </h:panelGroup>
						</f:facet>
					</h:panelGrid>
						<sakai:button_bar>
						<sakai:button_bar_item
							action="#{Option.processUpdateOptions}"
							value="#{msgs.options_updatebutton}"
							styleClass="active"
							rendered="true"
							immediate="false" />
						<sakai:button_bar_item
							action="#{Option.processGoToComposeByCancel}"
							value="#{msgs.options_cancelbutton}"
							rendered="true"
							immediate="false" />
					</sakai:button_bar>
				</h:form>
			</div>
		</body>
	</html>
</f:view>