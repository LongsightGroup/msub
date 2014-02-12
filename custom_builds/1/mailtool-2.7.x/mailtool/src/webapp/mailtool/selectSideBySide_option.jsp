<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<h:panelGroup style="height:100%;overflow:hidden;display:block;margin:.5em 0;padding:.3em;color:#555 !important" styleClass="inopPanel" >
	<h:outputText  value="Preview (inactive)"  style="padding:.5em"/>
	<h:panelGrid columns="3" cellspacing="0" cellpadding="0" style="width:auto"  styleClass="sidebyside" columnClasses="sidebyside,attach,sidebyside">
		<h:panelGroup>
			<h:outputText value="#{msgs.sidebyside_select_from_list}" style="display:block"/>
			<h:selectManyListbox disabled="true" size="10" value="#{Option.recipientSelector.sourceSelected}">
				<f:selectItems value="#{Option.recipientSelector.sourceListbox}"/>
			</h:selectManyListbox>
		</h:panelGroup>
		<h:panelGroup>
			<sakai:button_bar>
				<sakai:button_bar_item
					disabled="true"
					action="#{Option.recipientSelector.processAddButton}"
					value=" #{msgs.add_button}  "
					rendered="true"
					immediate="false" />
				</sakai:button_bar>
				<sakai:button_bar>
					<sakai:button_bar_item
					disabled="true"
					action="#{Option.recipientSelector.processRemoveButton}"
					value="#{msgs.remove_button}"
					rendered="true"
					immediate="false" />
			</sakai:button_bar>
		</h:panelGroup>	
		<h:panelGroup>
			<h:outputText value="#{msgs.sidebyside_message_recipients}" style="display:block"/>
			<h:selectManyListbox disabled="true" size="10" value="#{Option.recipientSelector.sinkSelected}">
				<f:selectItems value="#{Option.recipientSelector.sinkListbox}"/>
			</h:selectManyListbox>
		</h:panelGroup>	
	</h:panelGrid>
</h:panelGroup>