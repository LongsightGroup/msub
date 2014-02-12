<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/mailtool" prefix="mailtool" %>
 

	<h:panelGrid columns="3" cellspacing="0" cellpadding="0" style="margin:0" styleClass="sidebyside">
		<h:panelGroup>
			<h:selectManyListbox id="selectTo" size="10" value="#{Mailtool.recipientSelector.selectedItems}">
				<f:selectItems value="#{Mailtool.recipientSelector.listbox}"/>
			</h:selectManyListbox>
		</h:panelGroup>
		<h:panelGroup>	
			<sakai:button_bar>
				<sakai:button_bar_item
					action="#{Mailtool.recipientSelector.processSelectAll}"
					value="#{msgs.select_all_button}"
					immediate="false" />
			</sakai:button_bar>
			<sakai:button_bar>
				<sakai:button_bar_item
					action="#{Mailtool.recipientSelector.processSelectNone}"
					value="#{msgs.select_none_button}"
					immediate="false" />
			</sakai:button_bar>
		</h:panelGroup>
	</h:panelGrid>

<%--
<h:panelGroup rendered="#{Mailtool.currentMode=='options' }" style="height:100%;overflow:hidden;display:block;margin:.5em 0;padding:.3em;color:#555 !important" styleClass="inopPanel" >
	<h:outputText  value="Preview (inactive)"  style="padding:.5em"/>
	<h:panelGrid columns="3" cellspacing="0" cellpadding="0" styleClass="sidebyside">
		<h:panelGroup>
			<h:selectManyListbox disabled="true" id="selectTo2" size="10" value="#{Mailtool.recipientSelector.selectedItems}">
				<f:selectItems value="#{Mailtool.recipientSelector.listbox}"/>
			</h:selectManyListbox>
		</h:panelGroup>
		<h:panelGroup>
			<sakai:button_bar>
				<sakai:button_bar_item
					disabled="true"
					action="#{Mailtool.recipientSelector.processSelectAll}"
					value="#{msgs.select_all_button}"
					rendered="true"
					immediate="false" />
			</sakai:button_bar>
			<sakai:button_bar>
				<sakai:button_bar_item
					disabled="true"
					action="#{Mailtool.recipientSelector.processSelectNone}"
					value="#{msgs.select_none_button}"
					rendered="true"
					immediate="false" />
			</sakai:button_bar>
		</h:panelGroup>	
	</h:panelGrid>
</h:panelGroup>
--%>