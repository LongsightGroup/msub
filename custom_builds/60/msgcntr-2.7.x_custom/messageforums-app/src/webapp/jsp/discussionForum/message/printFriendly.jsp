<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
	<h:form id="msgForum">
<!--jsp/discussionForum/message/printFriendly.jsp-->
		
			<div class="navIntraTool">
				<a id="printIcon" href="javascript:" onClick="javascript:window.print();">
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					<h:outputText value="#{msgs.send_to_printer}" />
				</a>
				<a value="" href="" onClick="window.close();" >
					<h:outputText value="#{msgs.close_window}" />
				</a>
			</div>
			<div class="printBlock">
				<h2>
			      <h:outputText value="#{msgs.cdfm_discussion_forums}" />
      			  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  <h:outputText value="#{ForumTool.selectedForum.forum.title}" />
				  <f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
				  	  <h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
				</h2>
		
	
		<%--rjlowe: Expanded View to show the message bodies, threaded --%>
		<mf:hierDataTable id="expandedThreadedMessages" value="#{ForumTool.pFMessages}" var="message" 
						noarrows="true" styleClass="listHier printTable" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">
			<h:column id="_msg_subject">
					<%-- message has been submitted and has bene denied  approval by moderator--%>
					<h:outputText value="#{msgs.cdfm_msg_denied_label}"  styleClass="messageDenied"  rendered="#{message.msgDenied}" />
					<%-- message has been submitted and is pending approval by moderator--%> 
					<h:outputText value="#{msgs.cdfm_msg_pending_label}" styleClass="messagePending" rendered="#{message.msgPending}" />
					<h:outputText value="#{message.message.title}"  styleClass="title"/>		          	
		          	<h:outputText value=" - #{message.message.author}"/>
                    <h:outputText value="#{message.message.created}">
			   	         <f:convertDateTime pattern="#{msgs.date_format_paren}" />
           			</h:outputText>
						<mf:htmlShowArea value="#{message.message.body}" hideBorder="false" />

			</h:column>
		</mf:hierDataTable>
			</div>
		<h:inputHidden id="mainOrForumOrTopic" value="dfAllMessages" />
		
	</h:form>
</sakai:view>
</f:view>