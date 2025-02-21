<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.mv_p"/></title>
                        <!-- stylesheet and script widgets -->
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="collapseAllRowsForSelectList();flagRows();;<%= request.getAttribute("html.body.onload") %>">
  
<!-- content... -->
 <div class="portletBody">
<h:form id="movePool">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
<h3>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.mv_p}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.mv_q}"/>
</h3>

<div class="tier1">
<h:outputText value="#{questionPoolMessages.sel_dest_move} "/>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
<h:outputText rendered="#{questionpool.actionType == 'item' && questionpool.currentItems[1] == null}" escape="false" value="#{questionpool.currentItems[0].text}"/>
 
<h:dataTable rendered="#{questionpool.actionType == 'item' && questionpool.currentItems[1] != null}" id="questions" value="#{questionpool.currentItems}" var="curItem" >
<h:column>
<h:outputText escape="false" value="#{curItem.text}"/>
</h:column>
</h:dataTable>

</div>

<%--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.mv_q_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.mv_p_to}"/>
<br/><br/>
--%>


<div class="longtext tier2">
<%@ include file="/jsf/questionpool/movePoolTree.jsp" %>

<h:inputHidden id="selectedRadioBtn" value="#{questionpool.destPool}"/>

<%--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{questionPoolMessages.click_move}"/>
--%>

</div>

<p class="act">
  <h:commandButton accesskey="#{questionPoolMessages.a_move}" type="submit" immediate="true" id="poolSubmit" value="#{questionPoolMessages.move}"
    action="#{questionpool.movePool}" rendered="#{questionpool.actionType == 'pool'}" styleClass="active">
  </h:commandButton>

  <h:commandButton accesskey="#{questionPoolMessages.a_move}" type="submit" immediate="true" id="itemSubmit" value="#{questionPoolMessages.move}"
    action="#{questionpool.moveQuestion}" rendered="#{questionpool.actionType == 'item'}" styleClass="active">
  </h:commandButton>

  <h:commandButton id="cancel" accesskey="#{questionPoolMessages.a_cancel}" value="#{questionPoolMessages.cancel}" action="poolList"/>
</p>

</h:form>
</div>
</body>
</html>
</f:view>
