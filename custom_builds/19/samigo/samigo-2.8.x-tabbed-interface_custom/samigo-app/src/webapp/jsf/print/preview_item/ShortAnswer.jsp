<%--
include file for delivering short answer essay questions
should be included in file importing DeliveryMessages
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <%-- questionBlock --%>
  <h:outputText styleClass="questionBlock" escape="false" value="#{question.itemData.text}" />
  <h:outputText value="<br />" escape="false" />
  
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys || printSettings.showKeysFeedback}">
    <h:outputLabel value="#{printMessages.answer_point}: "/>
  	<h:outputText escape="false" value="#{question.itemData.score} #{authorMessages.points_lower_case}" />
  	<h:outputText value="<br />" escape="false" />
    <h:outputLabel value="#{printMessages.answer_model}: "/>
    <h:outputText rendered="#{question.key != null && question.key != '' && question.key != 'null'}" escape="false" value="#{question.key}" />
    <h:outputText rendered="#{question.key == null || question.key == '' || question.key == 'null'}" escape="false" value="--------" />
    <f:verbatim><br/></f:verbatim>
  </h:panelGroup>
  
  <%-- Show feedback answer --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeysFeedback}">
  	<h:outputLabel value="#{commonMessages.feedback}: " />
  	<h:outputText escape="false" value="#{question.itemData.generalItemFeedback}" rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback != ''}"/>
  	<h:outputText escape="false" value="--------" rendered="#{question.itemData.generalItemFeedback == null || question.itemData.generalItemFeedback == ''}"/>
  </h:panelGroup>
  

