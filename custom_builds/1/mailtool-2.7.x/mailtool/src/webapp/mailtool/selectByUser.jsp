<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
 
<%-- Users Table --%>
<%-- var=row is a SelectByUserTable.TableEntry      --%>

<h:dataTable value="#{Mailtool.recipientSelector.dataModel}" var="row" border="0" styleClass="listHier lines nolines" columnClasses="attach,,attach" style="width:auto;margin-top:0">
	<h:column>
		<h:selectBooleanCheckbox id="col1check" value="#{row.selected1}" />
	</h:column>
	<h:column>
		<h:outputLabel value="#{row.user1.displayname}" for="col1check" style="white-space:nowrap"/>
	</h:column>
	<h:column>
		<h:selectBooleanCheckbox rendered="#{row.render2}" value="#{row.selected2}" id="col2check" />
	</h:column>
	<h:column>
		<h:outputLabel rendered="#{row.render2}" value="#{row.user2.displayname}" for="col2check" style="white-space:nowrap" />
	</h:column>
</h:dataTable>

<%--
	<h:panelGroup rendered="#{Mailtool.currentMode=='options' }" style="height:100%;overflow:hidden;display:block;margin:.5em 0;padding:.3em;color:#555 !important" styleClass="inopPanel" >
		<h:outputText  value="Preview (inactive)"  style="padding:.5em"/>
		<h:dataTable value="#{Mailtool.recipientSelector.dataModel}" var="row"  columnClasses="attach,,attach,"  cellpadding="0" cellspacing="0" style="width:auto;font-weight:normal" border="0" styleClass="listHier lines nolines"> 
			<h:column>
				<h:selectBooleanCheckbox disabled="true" id="col1check2" value="#{row.selected1}" />
			</h:column>
			<h:column>
				<h:outputLabel value="#{row.user1.displayname}" for="col1check2" style="color:#777;white-space:nowrap" />
			</h:column>
			<h:column>
				<h:selectBooleanCheckbox disabled="true" rendered="#{row.render2}" value="#{row.selected2}" id="col2check2" />
			</h:column>
			<h:column>
				<h:outputLabel rendered="#{row.render2}" value="#{row.user2.displayname}" for="col2check2"  style="color:#777;white-space:nowrap"/>
			</h:column>
		</h:dataTable>
	</h:panelGroup>
--%>