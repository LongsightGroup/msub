<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
	  <script type="text/javascript" src="/samigo-app/js/saveForm.js"></script>	  	  
      <title> <h:outputText value="#{delivery.assessmentTitle}"/>
      </title>
      <style type="text/css">
        .TableColumn {
          text-align: center
        }
        .TableColumnLeft {
          text-align: left
        }
       .TableClass {
         border-style: dotted;
         border-width: 0.5px;
         border-color: light grey;
       }
      </style>
      </head>
	
      <body onload="<%= request.getAttribute("html.body.onload") %>; setLocation(); checkRadio(); SaveFormContentAsync('deliverAssessment.faces', 'takeAssessmentForm', 'takeAssessmentForm:save', 'takeAssessmentForm:lastSubmittedDate1', 'takeAssessmentForm:lastSubmittedDate2',  <h:outputText value="#{delivery.autoSaveRepeatMilliseconds}"/>, true); setTimeout('setLocation2()',2)" >
 
      <h:outputText value="<a name='top'></a>" escape="false" />
      
      <%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>
      
      <script type="text/javascript">
		
		function whichradio(obj){ 

          var myId = String(obj.id);
          //such as : takeAssessmentForm:_id48:0:_id105:1:deliverMatrixChoicesSurvey:matrixSurveyRadioTable:0:_id1198_0:myRadioId1
          //find the table id for mutiple matrix questions tables on the same display page 
          //take care of two different question, one set as forceRanking, another is not
          var myIdParts = myId.split(":");

          var node_list = document.getElementsByTagName('input');
          for (var i=0; i<node_list.length; i++) {
            var node = node_list[i];		
            if (node.getAttribute('type') == 'hidden' && node.id.endsWith('forceRanking')){
              var nodeIdParts = node.id.split(":");
              if(nodeIdParts[4]==myIdParts[4] && node.value == 'true'){
                //find the radio button table(s)
                var tables = document.getElementsByTagName('table');

                for(var i=0; i<tables.length; i++){
                  var mytable = tables[i];
                  var mytableParts = mytable.id.split(":");
                  if(mytable.id.endsWith('matrixSurveyRadioTable') && mytableParts[4] == myIdParts[4]){
                    //found the right table
                    break;
                  }
                }

                //index will be the begining of 'matrixSurveyRadioTable'
                var index = myId.indexOf("matrixSurveyRadioTable");
                var strBefore = myId.substring(0,index+'matrixSurveyRadioTable'.length);
                //remove table no 
                var strAfter = myId.substring(index+'matrixSurveyRadioTable'.length+2);
                //find rows of mytable	
                var iRow = mytable.getElementsByTagName('tr');
                //one header row before the row containing the radio button
                for (var i=0; i<iRow.length-1;i++){
                    //alert(i);
                  //construct radio button id in the same column
                  var currentRadioButtonId= strBefore+":"+i+strAfter;
                  //alert(currentRadioButtonId);
                  var button=document.getElementById(currentRadioButtonId);
                  var buttonIdStr = String(button.id);
                  if(button.getAttribute('type') == 'radio' && button.checked == true && buttonIdStr != myId){
                    obj.checked = false;
                    alert("You are only allowed one selection per column, please try again.");
                    return;
                  }
                }
                return;
              }
            }
          }
		}


      </script>
      
 
       <div id="timer-warning" style="display:none" title="&nbsp;<span class='skip'>
         <h:outputText value="#{deliveryMessages.five_minutes_left1} " />
	     <h:outputText value="#{deliveryMessages.five_minutes_left2}" />
	     <h:outputText value="#{deliveryMessages.five_minutes_left3}" /></span>">
      	 <h:panelGrid columns="1" rowClasses="TableColumn, TableColumnLeft, TableColumnLeft" width="100%"  border="0">
           <h:outputText value="<b>#{deliveryMessages.five_minutes_left1}</b>" escape="false"/>
      	   <h:outputText value="<br/>#{deliveryMessages.five_minutes_left2}" escape="false"/>
      	   <h:outputText value="#{deliveryMessages.five_minutes_left3}"  escape="false"/>
      	 </h:panelGrid>
      </div>
      
		<div id="timer-expired-warning" style="display:none;">
			<h3><h:outputText value="#{deliveryMessages.time_expired1}" /></h3>
      		<p><h:outputText value="#{deliveryMessages.time_expired3}" /></p>
      		<div id="squaresWaveG">
				<div id="squaresWaveG_1" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_2" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_3" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_4" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_5" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_6" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_7" class="squaresWaveG">
				</div>
				<div id="squaresWaveG_8" class="squaresWaveG">
				</div>
			</div>
			
		</div>
 
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data"
   onsubmit="saveTime(); serializeImagePoints()">

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script type="text/JavaScript">

function checkRadio()
{
  for (i=0; i<document.forms[0].elements.length; i++)
  {
    if (document.forms[0].elements[i].type == "radio")
    {
      if (document.forms[0].elements[i].defaultChecked == true)
      {
        document.forms[0].elements[i].click();
      }
    }
  }
}

var formatByQuestion = '<h:outputText value="#{delivery.settings.formatByQuestion}" />';
function setLocation()
{
    // reset questionindex to avoid a Safari bug
	partIndex = document.forms[0].elements['takeAssessmentForm:partIndex'].value;
	questionIndex = document.forms[0].elements['takeAssessmentForm:questionIndex'].value;
 	if (!formatByQuestion)
           document.forms[0].elements['takeAssessmentForm:questionIndex'].value = "0";

	formatByPart = document.forms[0].elements['takeAssessmentForm:formatByPart'].value;
	formatByAssessment = document.forms[0].elements['takeAssessmentForm:formatByAssessment'].value;
	
    //alert("partIndex = " + partIndex);
    //alert("questionIndex = " + questionIndex);
	//alert("formatByPart = " + formatByPart);
	//alert("formatByAssessment = " + formatByAssessment);
	// We don't want to set the location when the index points to fist question on the page
	// We only set the location in following cases:
	// 1. If it is formatByPart, we set the location when it is not the first question of each part
	// 2. If it is formatByAssessment, we set the location when:
	//    a. it is not the first question of the first part
	//    b. it is a question in any parts other than the first one
	if ((formatByPart == 'true' && questionIndex != 0) || (formatByAssessment == 'true' && ((partIndex == 0 && questionIndex !=0) || partIndex != 0))) {
		window.location = '#p' + ++partIndex + 'q' + ++questionIndex;
		//alert("from TOC:" + window.location);
	}
}

var redrawAnchorName = '<h:outputText value="#{delivery.redrawAnchorName}" />';
function setLocation2()
{
	//alert("redrawAnchorName=" + redrawAnchorName);	
	if (redrawAnchorName != null && redrawAnchorName != "") {
		window.location = '#' + redrawAnchorName;
		//alert("from redraw: window.location..." + window.location);
	}
}

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'false';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded/10;
 }
}

function disableRationale(){
	var textAreas = document.getElementsByTagName("textarea");
	//alert(textAreas[0].id);
	//alert(textAreas[0].id.endsWith('rationale'));
	if (textAreas.length == 1 && textAreas[0].id.endsWith('rationale')) {
		textAreas[0].disabled = true;
	}
}

function enableRationale(){
	var textAreas = document.getElementsByTagName("textarea");
	//alert(textAreas[0].id);
	//alert(textAreas[0].id.endsWith('rationale'));
	if (textAreas.length == 1 && textAreas[0].id.endsWith('rationale')) {
		textAreas[0].disabled = false;
	}

	/* Somehow the following for-loop becomes an infinite look of enableRationale(). No time to look into this now. Use above work around. 
	   Should come back later to figure out the reason.
	for(i=0; i < textAreas.length; i++){
		alert(i);
		if (textAreas[i].id.endsWith('rationale')) {
        textAreas[i].disabled = false;
		return;
		}
    }
	*/
}

// modified from tompuleo.com
String.prototype.endsWith = function(txt)
{
  var rgx;
  rgx = new RegExp(txt+"$");

  return this.match(rgx) != null; 
}

function clickSaCharCountLink(field){
var insertlinkid= field.id.replace("getAaCharCount", "hiddenlink");

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}
</script>


<h:panelGroup rendered="#{delivery.actionString =='gradeAssessment' || delivery.actionString =='reviewAssessment' }" >
	<f:verbatim>
		<script language='javascript' src='/samigo-app/js/jquery.dynamiclist.student.preview.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.student.preview.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.author.preview.js'></script>
	</f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.actionString !='gradeAssessment' && delivery.actionString !='reviewAssessment' }" >
	<f:verbatim>
		<script language='javascript' src='/samigo-app/js/jquery.dynamiclist.student.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.student.js'></script>
		<script language='javascript' src='/samigo-app/js/selection.author.preview.js'></script>
	</f:verbatim>
</h:panelGroup>

<link href="/samigo-app/css/imageQuestion.student.css" type="text/css" rel="stylesheet" media="all" />
<link href="/samigo-app/css/imageQuestion.author.css" type="text/css" rel="stylesheet" media="all" />

<script type="text/JavaScript">
	var dynamicListMap = [];		
	jQuery(window).load(function(){
		
		$('div[id^=sectionImageMap_]').each(function(){
			var myregexp = /sectionImageMap_(\d+_\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var serializedImageMapId = $(this).find('input:hidden[id$=serializedImageMap]').attr('id').replace(/:/g, '\\:');
			
			var dynamicList = new DynamicList(serializedImageMapId, 'imageMapTemplate_'+sequence, 'pointerClass', 'imageMapContainer_'+sequence);
			dynamicList.fillElements();
			
			dynamicListMap[sequence] = dynamicList;
			
		});	
		
		$('input:hidden[id^=hiddenSerializedCoords_]').each(function(){
			var myregexp = /hiddenSerializedCoords_(\d+_\d+)_(\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var label = parseInt(matches[2])+1;
			
			var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'answerImageMapContainer_'+sequence);
			try {
				sel.setCoords(JSON.parse(this.value));
				sel.setText(label);
			}catch(err){}
			
		});	
	});
	
	function resetImageMap(key) {
		if(dynamicListMap[key] !== undefined)
			dynamicListMap[key].resetElements();
	}
	
	function serializeImagePoints(){
		for(var key in dynamicListMap)
			dynamicListMap[key].serializeElements();
	}
</script>

<h:inputHidden id="partIndex" value="#{delivery.partIndex}"/>
<h:inputHidden id="questionIndex" value="#{delivery.questionIndex}"/>
<h:inputHidden id="formatByPart" value="#{delivery.settings.formatByPart}"/>
<h:inputHidden id="formatByAssessment" value="#{delivery.settings.formatByAssessment}"/>
<h:inputHidden id="lastSubmittedDate1" value="#{delivery.assessmentGrading.submittedDate.time}" 
   rendered ="#{delivery.assessmentGrading.submittedDate!=null}"/>
<h:inputHidden id="lastSubmittedDate2" value="0"
   rendered ="#{delivery.assessmentGrading.submittedDate==null}"/>

<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton id="done" value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE -->
<h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false"  />

<!-- HEADING -->
<f:subview id="assessmentDeliveryHeading">
<%@ include file="/jsf/delivery/assessmentDeliveryHeading.jsp" %>
</f:subview>

<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->
<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<!-- h:inputHidden id="ItemIdent" value="#{item.ItemIdent}"/ -->
<!-- h:inputHidden id="ItemIdent2" value="#{item.itemNo}"/ -->
<!-- h:inputHidden id="currentSection" value="#{item.currentSection}"/ -->
<!-- h:inputHidden id="insertPosition" value="#{item.insertPosition}"/ -->
<%-- PART/ITEM DATA TABLES --%>

<h:panelGrid columns="1" width="100%" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}" border="0">
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_1}"/>
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_2}" escape="false"/>
      <h:outputText value="#{deliveryMessages.linear_no_contents_warning_3}" escape="false"/>
</h:panelGrid>

<h:panelGroup rendered="#{!delivery.pageContents.isNoParts || delivery.navigation ne '1'}">
<f:verbatim><div class="tier1"></f:verbatim>
  <h:dataTable width="100%" value="#{delivery.pageContents.partsContents}" var="part" border="0">
    <h:column>
     <!-- f:subview id="parts" -->
      <f:verbatim><h4></f:verbatim>
      <h:panelGrid columns="2" width="100%" columnClasses="navView,navList">
       <h:panelGroup>
      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
      <h:outputText value=" #{deliveryMessages.dash} #{part.nonDefaultText}" escape="false"/>
         </h:panelGroup>
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{deliveryMessages.ans_q}, " / -->
      <h:outputText value="#{part.pointsDisplayString} #{part.maxPoints} #{deliveryMessages.pt}" 
         rendered="#{delivery.actionString=='reviewAssessment'}"/>
</h:panelGrid>
      <f:verbatim></h4></f:verbatim>
      <h:outputText value="#{part.description}" escape="false"/>

  <!-- PART ATTACHMENTS -->
  <%@ include file="/jsf/delivery/part_attachment.jsp" %>
   <f:verbatim><div class="tier2"></f:verbatim>

   <h:outputText value="#{deliveryMessages.no_question}" escape="false" rendered="#{part.noQuestions}"/>

      <h:dataTable width="100%" value="#{part.itemContents}" var="question">
        <h:column>

<h:panelGrid columns="2" width="100%" columnClasses="navView,navList">
         <h:panelGroup>
           <f:verbatim><h5></f:verbatim>
           <h:outputText value="<a name='p#{part.number}q#{question.number}'></a>" escape="false" />
           <h:outputText value="#{deliveryMessages.q} #{question.sequence} #{deliveryMessages.of} #{part.numbering}"/>
           <f:verbatim></h5></f:verbatim>
         </h:panelGroup>
<h:panelGroup>
<h:outputText value=" #{question.pointsDisplayString} #{question.maxPoints} #{deliveryMessages.pt}" rendered="#{delivery.actionString=='reviewAssessment'}"/>

        <h:outputText value="#{question.maxPoints} #{deliveryMessages.pt}" rendered="#{delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag && delivery.actionString!='reviewAssessment'}" />
</h:panelGroup>
</h:panelGrid>
          <f:verbatim><div class="tier3"></f:verbatim>
          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
           <f:subview id="deliverAudioRecording">
           <%@ include file="/jsf/delivery/item/deliverAudioRecording.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
           <f:subview id="deliverFileUpload">
           <%@ include file="/jsf/delivery/item/deliverFileUpload.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 11}">
	       <f:subview id="deliverFillInNumeric">
	       <%@ include file="/jsf/delivery/item/deliverFillInNumeric.jsp" %>
	       </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 8}">
           <f:subview id="deliverFillInTheBlank">
           <%@ include file="/jsf/delivery/item/deliverFillInTheBlank.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 9}">
           <f:subview id="deliverMatching">
            <%@ include file="/jsf/delivery/item/deliverMatching.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 16}"><!-- // IMAGEMAP_QUESTION -->
           <f:subview id="deliverImageMapQuestion">
            <%@ include file="/jsf/delivery/item/deliverImageMapQuestion.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup
            rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 3 || question.itemData.typeId == 12}">
           <f:subview id="deliverMultipleChoiceSingleCorrect">
           <%@ include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 2}">
           <f:subview id="deliverMultipleChoiceMultipleCorrect">
           <%@ include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 5}">
           <f:subview id="deliverShortAnswer">
           <%@ include file="/jsf/delivery/item/deliverShortAnswer.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 4}">
           <f:subview id="deliverTrueFalse">
           <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
           </f:subview>
           </h:panelGroup>
           
           <h:panelGroup rendered="#{question.itemData.typeId == 13}">
           <f:subview id="deliverMatrixChoicesSurvey">
           <%@ include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp" %>
           </f:subview>
           </h:panelGroup>
          
           <f:verbatim></div></f:verbatim>

        </h:column>
      </h:dataTable>
<f:verbatim></div></f:verbatim>
     <!-- /f:subview -->

    </h:column>
  </h:dataTable>
<f:verbatim></div></f:verbatim>
</h:panelGroup>

  <f:verbatim><br/></f:verbatim>

<!-- 1. special case: linear + no question to answer -->
<h:panelGrid columns="2" border="0" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}">
  <h:panelGrid columns="1" width="100%" border="0" columnClasses="act">
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmit}"  id="submitForm3" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.continue}" 
      onclick="pauseTiming='false'; disableSubmit()" />
  </h:panelGrid>

  <h:panelGrid columns="1" width="100%" border="0">
  <h:commandButton value="#{commonMessages.cancel_action}" type="submit"
     action="select" rendered="#{delivery.pageContents.isNoParts && delivery.navigation eq '1'}" />
  </h:panelGrid>
</h:panelGrid>

<!-- 2. normal flow -->
<h:panelGrid columns="6" border="0" rendered="#{!(delivery.pageContents.isNoParts && delivery.navigation eq '1')}">
  <%-- PREVIOUS --%>
  <h:panelGrid columns="1" border="0">
	<h:commandButton id="previous" type="submit" value="#{deliveryMessages.previous}"
    action="#{delivery.previous}"
    disabled="#{!delivery.previous}" 
    onclick="disablePrevious()" onkeypress="" 
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.navigation ne '1' && ((delivery.previous && delivery.continue) || (!delivery.previous && delivery.continue) || (delivery.previous && !delivery.continue))}" />
  </h:panelGrid>

  <%-- NEXT --%>
  <h:panelGrid columns="1" border="0" columnClasses="act">
    <h:commandButton id="next1" type="submit" value="#{commonMessages.action_next}"
    action="#{delivery.next_page}" disabled="#{!delivery.continue}"
    onclick="disableNext()" onkeypress="" 
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && (delivery.previous && !delivery.continue)}" />

    <h:commandButton id="next" type="submit" value="#{commonMessages.action_next}"
    action="#{delivery.next_page}" styleClass="active"
    onclick="disableNext()" onkeypress="" 
	rendered="#{(delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl')
              && delivery.continue}" />

  </h:panelGrid>


  <h:panelGrid columns="1" border="0">
           <h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false" />
  </h:panelGrid>

  <%-- SAVE --%>
  <h:panelGrid columns="1" border="0" >
  <h:commandButton id="save" type="submit" value="#{commonMessages.action_save}"
    action="#{delivery.save_work}" onclick="disableSave();" rendered="#{delivery.actionString=='previewAssessment'
                 || delivery.actionString=='takeAssessment'
                 || delivery.actionString=='takeAssessmentViaUrl'}" />
  </h:panelGrid>

  <h:panelGrid columns="1"  border="0">
  <%-- EXIT --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}" id="saveAndExit"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 || delivery.actionString=='takeAssessment'
                 || (delivery.actionString=='takeAssessmentViaUrl' && !delivery.anonymousLogin))
              && delivery.navigation ne '1' && !delivery.hasTimeLimit}"  
    onclick="pauseTiming='false'; disableSaveAndExit();" />

  <%-- SAVE AND EXIT DURING PAU WITH ANONYMOUS LOGIN--%>
  <h:commandButton  type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{(delivery.actionString=='takeAssessmentViaUrl' && delivery.anonymousLogin) && !delivery.hasTimeLimit}"
    onclick="pauseTiming='false'; disableQuit()" /> 

  <%-- SAVE AND EXIT FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}" id="saveAndExit2"
    rendered="#{(delivery.actionString=='previewAssessment'  
                 ||delivery.actionString=='takeAssessment'
                 || (delivery.actionString=='takeAssessmentViaUrl' && !delivery.anonymousLogin))
            && delivery.navigation eq '1' && delivery.continue && !delivery.hasTimeLimit}"
    onclick="disableSaveAndExit2();" />
  </h:panelGrid>

  <h:panelGrid columns="1" width="100%" border="0" columnClasses="act">
  <%-- SUBMIT FOR GRADE --%>
  <h:commandButton id="submitForGrade" type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.confirmSubmit}" styleClass="active"
    rendered="#{(delivery.actionString=='takeAssessment' ||delivery.actionString=='takeAssessmentViaUrl' || delivery.actionString=='previewAssessment') 
             && delivery.navigation ne '1' 
             && !delivery.continue}"
    onclick="disableSubmitForGrade()" />

  <%-- SUBMIT FOR GRADE DURING PAU --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit}"
    action="#{delivery.confirmSubmit}"  id="submitForm1" styleClass="active"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl' && delivery.continue && delivery.anonymousLogin}"
    onclick="pauseTiming='false'; disableSubmit1();" />

  <%-- SUBMIT FOR GRADE FOR LINEAR ACCESS --%>
  <h:commandButton type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmit}"  id="submitForm" styleClass="active"
      rendered="#{(delivery.actionString=='takeAssessment'
                   || delivery.actionString=='takeAssessmentViaUrl'
				   || delivery.actionString=='previewAssessment')
				   && delivery.navigation eq '1' && !delivery.continue}" 
      onclick="pauseTiming='false'; disableSubmit()" />

  </h:panelGrid>
</h:panelGrid>

	<h:commandLink id="hiddenReloadLink" action="#{delivery.same_page}" value="">
	</h:commandLink>

<f:verbatim></p></f:verbatim>

<!-- DONE BUTTON IN PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</h:form>
<!-- end content -->
<f:verbatim></div></f:verbatim>
<script type="text/JavaScript">fixImplicitLabeling();</script>
    </body>
  </html>
</f:view>
