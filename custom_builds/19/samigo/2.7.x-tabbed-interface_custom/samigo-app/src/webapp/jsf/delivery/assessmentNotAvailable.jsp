<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{deliveryMessages.invalid_assessment}"/></title>
      </head>
<!--
* $Id$
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
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<h3><h:outputText value="#{deliveryMessages.assessment_not_available}"/></h3>
<h:form id="redirectLoginForm">
 <div class="validation">
<h:outputText  value="#{deliveryMessages.assessment_not_available_message}" />
 </div>

<p class="act">
       <h:commandButton accesskey="#{deliveryMessages.a_return}" value="#{deliveryMessages.button_return}" type="submit"
         styleClass="active" action="select" rendered="#{delivery.actionString=='takeAssessment'}">
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>


  <h:commandButton accesskey="#{deliveryMessages.a_next}" value="#{deliveryMessages.button_continue}" type="button" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"
       style="act" onclick="javascript:window.open('#{delivery.portal}/login','_top')" onkeypress="javascript:window.open('#{delivery.portal}/login','_top')" />
</p>
</h:form>
</div>
      </body>
    </html>
  </f:view>

