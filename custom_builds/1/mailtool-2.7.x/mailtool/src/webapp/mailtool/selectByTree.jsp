<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<%--
Mailtool.userTree returns DataModel from SelectByTree.getRows
--%>
                                              
<%-- Tree Table --%>

	<h:panelGroup rendered="#{Mailtool.groupAwareRoleExist}">
		<h:dataTable value="#{Mailtool.recipientSelector_GroupAwareRole.dataModel}" var="garow"  border="0" cellpadding="0" cellspacing="0">
				<h:column>
						<h:panelGroup>
							<h:selectBooleanCheckbox id="selectAllGroupAwareRoleCheckbox1" value="#{garow.allSelected}" immediate="true" onclick="submit();" valueChangeListener="#{garow.processSelectAll}"/>
							<h:outputLabel for="selectAllGroupAwareRoleCheckbox1" value=" #{msgs.usersbyrole_all_prefix}  #{garow.pluralRolename} " />
							<h:outputText value=" - "  />
							<h:panelGroup rendered="#{not Mailtool.groupAwareRoleviewClicked}"  style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_groupAwareRoleviewClicked}" value=" #{msgs.usersbyrole_select} #{garow.pluralRolename}" style="text-decoration: underline"/>
							</h:panelGroup>
							<h:panelGroup rendered="#{Mailtool.groupAwareRoleviewClicked}" style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_groupAwareRoleviewClicked}" value=" #{msgs.usersbyrole_collapse} #{garow.pluralRolename}" style="text-decoration: underline"/>
							</h:panelGroup>
						</h:panelGroup>
						<h:panelGroup rendered="#{Mailtool.num_groups > 0}">	
							<h:outputText value=" | "  />
							<h:panelGroup rendered="#{not Mailtool.groupviewClicked}" style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_groupviewClicked}" value=" #{msgs.usersbyrole_selectgroups}" style="text-decoration: underline"/>
							</h:panelGroup>
							<h:panelGroup rendered="#{Mailtool.groupviewClicked}" style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_groupviewClicked}" value=" #{msgs.usersbyrole_collapsegroups}" style="text-decoration: underline"/>
							</h:panelGroup>
						</h:panelGroup>	
						<h:panelGroup rendered="#{Mailtool.num_sections > 0}">
							<h:outputText value=" | "  />
							<h:panelGroup rendered="#{not Mailtool.sectionviewClicked}" style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_sectionviewClicked}" value=" #{msgs.usersbyrole_selectsections}" style="text-decoration: underline"/>
							</h:panelGroup>	
							<h:panelGroup rendered="#{Mailtool.sectionviewClicked}" style="font-weight:normal">
								<h:commandLink action="#{Mailtool.toggle_sectionviewClicked}" value=" #{msgs.usersbyrole_collapsesections}" style="text-decoration: underline"/>
							</h:panelGroup>			
						</h:panelGroup>

						<h:dataTable rendered="#{Mailtool.groupAwareRoleviewClicked}" value="#{garow.userTable}" var="user_row"  styleClass="listHier nolines hierItemBlockWrapper" columnClasses="attach,,attach,"  cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal">
							<h:column>
								<h:selectBooleanCheckbox id="indCol1Checkbox11" value="#{user_row.selected1}"  />
							</h:column>
							<h:column>
								<h:outputLabel for="indCol1Checkbox11" value="#{user_row.user1.displayname}" style="white-space:nowrap" />
							</h:column>
							<h:column>
								<h:selectBooleanCheckbox id="indCol2Checkbox22" value="#{user_row.selected2}" rendered="#{user_row.render2}" style="margin-left:2em;"/>
							</h:column>
							<h:column>
							<h:outputLabel for="indCol2Checkbox22" rendered="#{user_row.render2}" value="#{user_row.user2.displayname}" style="white-space:nowrap" />
							</h:column>
						</h:dataTable>
					</h:column>
		</h:dataTable>
	
		<h:dataTable rendered="#{Mailtool.groupviewClicked }" value="#{Mailtool.recipientSelector_Group.dataModel}" var="grouprow"  border="0" cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal" styleClass="listHier nolines  hierItemBlockWrapper">
			<h:column>
				<h:selectBooleanCheckbox id="selectAllGroupCheckbox" value="#{grouprow.allSelected}" immediate="true" onclick="submit();" valueChangeListener="#{grouprow.processSelectAll}"/>
				<h:outputLabel for="selectAllGroupCheckbox" value="#{grouprow.pluralRolename} #{msgs.usersbyrole_group}" />
				<h:outputText value=" - "  />
				<h:panelGroup rendered="#{grouprow.collapsed}">
					<h:commandLink actionListener="#{grouprow.actionExpand}" value="#{msgs.usersbyrole_select_individuals_link}" style="text-decoration: underline"/>
				</h:panelGroup>
				<h:panelGroup rendered="#{not grouprow.collapsed}">
					<h:commandLink actionListener="#{grouprow.actionCollapse}" value="#{msgs.usersbyrole_collapse}" style="text-decoration: underline"/>
					<h:dataTable value="#{grouprow.userTable}" var="guser_row"  styleClass="listHier nolines" columnClasses="attach,,attach,"  cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal">
						<h:column>
							<h:selectBooleanCheckbox id="indCol1Checkbox3" value="#{guser_row.selected1}"  />
						</h:column>
						<h:column>
							<h:outputLabel for="indCol1Checkbox3" value="#{guser_row.user1.displayname}" style="white-space:nowrap" />
						</h:column>
						<h:column>
							<h:selectBooleanCheckbox id="indCol2Checkbox4" value="#{guser_row.selected2}" rendered="#{guser_row.render2}" style="margin-left:2em;"/>
						</h:column>
						<h:column>
							<h:outputLabel for="indCol2Checkbox4" rendered="#{guser_row.render2}" value="#{guser_row.user2.displayname}" style="white-space:nowrap" />
						</h:column>
					</h:dataTable>
				</h:panelGroup>
			</h:column>
		</h:dataTable>	
	
		<h:dataTable rendered="#{Mailtool.sectionviewClicked}" value="#{Mailtool.recipientSelector_Section.dataModel}" var="sectionrow"  border="0" cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal" styleClass="listHier nolines  hierItemBlockWrapper">
			<h:column>
				<h:selectBooleanCheckbox id="selectAllSectionCheckbox" value="#{sectionrow.allSelected}" immediate="true" onclick="submit();" valueChangeListener="#{sectionrow.processSelectAll}"/>
				<h:outputLabel for="selectAllSectionCheckbox" value="#{sectionrow.pluralRolename} #{msgs.usersbyrole_section}" />
				<h:outputText value=" - "  />
				<h:panelGroup rendered="#{sectionrow.collapsed}">
					<h:commandLink actionListener="#{sectionrow.actionExpand}" value="#{msgs.usersbyrole_select_individuals_link}" style="text-decoration: underline"/>
				</h:panelGroup>
				<h:panelGroup rendered="#{not sectionrow.collapsed}">
					<h:commandLink actionListener="#{sectionrow.actionCollapse}" value="#{msgs.usersbyrole_collapse}" style="text-decoration: underline"/>
					<h:dataTable value="#{sectionrow.userTable}" var="suser_row"  styleClass="listHier nolines" columnClasses="attach,,attach,"  cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal">
						<h:column>
							<h:selectBooleanCheckbox id="indCol1Checkbox5" value="#{suser_row.selected1}"  />
						</h:column>
						<h:column>
							<h:outputLabel for="indCol1Checkbox5" value="#{suser_row.user1.displayname}" style="white-space:nowrap" />
						</h:column>
						<h:column>
							<h:selectBooleanCheckbox id="indCol2Checkbox6" value="#{suser_row.selected2}" rendered="#{suser_row.render2}" style="margin-left:2em;"/>
						</h:column>
						<h:column>
							<h:outputLabel for="indCol2Checkbox6" rendered="#{suser_row.render2}" value="#{suser_row.user2.displayname}" style="white-space:nowrap" />
						</h:column>
					</h:dataTable>
				</h:panelGroup>
			</h:column>
		</h:dataTable>	
	</h:panelGroup>

	<h:dataTable value="#{Mailtool.recipientSelector.dataModel}" var="row"  border="0" style="margin:0;width:100%" cellpadding="0" cellspacing="0">
		<h:column>
			<h:selectBooleanCheckbox id="selectAllCheckbox" value="#{row.allSelected}" immediate="true" onclick="submit();" valueChangeListener="#{row.processSelectAll}"/>
			<h:outputLabel for="selectAllCheckbox" value="#{msgs.usersbyrole_all_prefix} #{row.pluralRolename} " />
			<h:outputText value=" - "  />
			<h:panelGroup rendered="#{row.collapsed}">
				<h:commandLink actionListener="#{row.actionExpand}" value="#{msgs.usersbyrole_select} #{row.pluralRolename} " style="text-decoration: underline"/>
			</h:panelGroup>
			<h:panelGroup rendered="#{not row.collapsed}">
				<h:commandLink actionListener="#{row.actionCollapse}" value="#{msgs.usersbyrole_collapse} #{row.pluralRolename} " style="text-decoration: underline"/>
			</h:panelGroup>
			<h:panelGroup rendered="#{not row.collapsed}">
				<h:dataTable value="#{row.userTable}" var="user_row"  styleClass="listHier nolines hierItemBlockWrapper" columnClasses="attach,,attach,"  cellpadding="0" cellspacing="0" style="margin-left:2em;width:auto;font-weight:normal">
					<h:column>
						<h:selectBooleanCheckbox id="indCol1Checkbox1" value="#{user_row.selected1}"  />
					</h:column>
					<h:column>
						<h:outputLabel for="indCol1Checkbox1" value="#{user_row.user1.displayname}" style="white-space:nowrap" />
					</h:column>
					<h:column>
						<h:selectBooleanCheckbox id="indCol2Checkbox2" value="#{user_row.selected2}" rendered="#{user_row.render2}" style="margin-left:2em;"/>
					</h:column>
					<h:column>
						<h:outputLabel for="indCol2Checkbox2" rendered="#{user_row.render2}" value="#{user_row.user2.displayname}" style="white-space:nowrap" />
					</h:column>
				</h:dataTable>
			</h:panelGroup>
		</h:column>
	</h:dataTable>
