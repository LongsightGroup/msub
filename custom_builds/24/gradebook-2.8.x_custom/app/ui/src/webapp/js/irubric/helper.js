/* Copyright (C) Reazon Systems, Inc.  All rights reserved.*/

/*
	Description
		Encoding the request URL
	Params
		url
			the request URL
	Return
		the encoded URL
 */
function encodeUrl(url) {

	if (url.indexOf("?") > 0) {
		encodedParams = "?";
		parts = url.split("?");
		params = parts[1].split("&");
		for (i = 0; i < params.length; i++) {
			if (i > 0) {
				encodedParams += "&";
			}
			if (params[i].indexOf("=") > 0) {
				// Avoid null values
				p = params[i].split("=");
				encodedParams += (p[0] + "=" + escape(encodeURI(p[1])));
			} else {
				encodedParams += params[i];
			}
		}
		url = parts[0] + encodedParams;
	}
	return url;
}

/*
 Description:
 get the element which is on the same row with iRubric icon
 Params:
 iRubricIconId
 The element id of iRubric icon
 Return:
 the score element id
 */
function getScoreTextBoxId(iRubricIconId) {
	var pos = iRubricIconId.lastIndexOf(":");
	var id = iRubricIconId.substr(0, pos + 1).concat("Score");
	var elementId = document.getElementById(id);
	if (elementId == null) {
		id = iRubricIconId.substr(0, pos + 1).concat("LetterScore");
	}
	return id;
}

/*
 Description:
 Creating a hidden frame to get the value from iRubric system.
 Parameters:
 frmId:	frame id which will be added as into the document dynamically
 link:	the link which is used to fetch data from iRubric system
 Return:
 None
 */
function createRubricFrame(frmId, link) {
	// create a HTML element which type is iFrame
	var rubricFrame = document.createElement("iFrame");

	// set the element id to it
	rubricFrame.id = frmId;

	// make it hidden
	rubricFrame.style.visibility = 'hidden';
	rubricFrame.width = 0;
	rubricFrame.height = 0;

	// set the source to the created iframe
	rubricFrame.src = encodeUrl(link);

	// add it into the current document
	document.body.appendChild(rubricFrame);
}

/*
 Description: build the link to fetch data
 Params:
 purpose:
 the purpose
 rosId:
 the roster student
 rosName:
 the roster student display name
 gdbId:
 the gradebook item id
 agmId:
 the assignment id
 rubricIconId:
 the element id of iRubric icon
 Return:
 a string which is the link to a page on iRubric system
 */
function buildReazonLink(purpose, rosId, rosName, gdbId, agmId, rubricIconId) {
	var lnk = "iRubricLink.jsp?".concat("purpose=", purpose,
			"&rosterStudentId=", rosId, "&rosterStudentDisplayName=", rosName,
			"&gradebookId=", gdbId, "&gradebookItemId=", agmId,
			"&fieldToUpdate=", getScoreTextBoxId(rubricIconId));
	return lnk;
}

/*
 Description
 Build the link used to fetch all grades by roster student
 Params
 rosId
 the roster student id
 Return
 a string containing link to iRubric system to fetch all grades by roster student
 */
function buildGetGradesByRos(rosId) {
	var strLink = "iRubricLink.jsp?".concat("p=gas",
			"&rosterStudentId=", rosId)
	return strLink;
}

/*
 Description
 Build the link used to fetch all grades by gradebook item
 Params
 gdbId
 the gradebook item id
 Return
 a string containing link to iRubric system to fetch all grades by gradebook item
 */
function buildGetGradesByGdbId(gdbItemId) {
	var strLink = "iRubricLink.jsp?".concat("p=gag", "&gradebookItemId=", gdbItemId)
	return strLink;
}

// get grade via hidden IFrame
function createHiddenIframe(purpose, rosterStudentId, assignmentId, iRubricIconId) {
	var frame = document.createElement("iFrame");
	frame.id = "getGradeFrame";
	var lnk = "iRubricLink.jsp?".concat("p=", purpose,
			"&rosterStudentId=", rosterStudentId
			, "&gradebookItemId=", assignmentId
			, "&fieldToUpdate=", getScoreTextBoxId(iRubricIconId));
	// make it hidden
	frame.style.visibility = 'hidden';
	frame.width = 0;
	frame.height = 0;
	frame.src = encodeUrl(lnk);
	document.body.appendChild(frame);

}

// update score textbox
function updateScoreTextbox(fieldToUpdate, newScore) {
	var inp = document.getElementById(fieldToUpdate);
	inp.value = newScore;
	// remove hidden frame
	var getGradeFrame = document.getElementById("getGradeFrame");
	discardElement(getGradeFrame);
}

// gets field to update element
function getFieldToUpdateElement(iRubricIconId, elementId) {
	var pos = iRubricIconId.lastIndexOf(":");
	return iRubricIconId.substr(0, pos + 1).concat(elementId);
}

/*
 Description:
 Confirming the user when the student or gradebook assignment has not been graded yet.
 Params:
 refreshIconId
 the icon which user clicks on
 Return:
 None
 */
function ungradedConfirm(refreshIconId) {
	// do confirming user to grade or not
	var answer = confirm("Student has not been graded yet.  Press OK to grade student or Cancel to ignore.")
	if (answer) {
		// get the link which is used to browse the grading page on iRubric system
		var a = document.getElementById(getFieldToUpdateElement(refreshIconId,
				"grade"));

		// show a popup for grading
		a.onclick();
	}else{
		var getGradeFrame = document.getElementById("getGradeFrame");
		discardElement(getGradeFrame);
	}
}

// confirm to update grade
function updateGradeConfirm(iRubricIconId) {
	var MSG = "After you are done scoring the rubric, please click here to enter the grade into your gradebook.";
	var answer = confirm(MSG)
	if (answer) {
		var a = document.getElementById(getFieldToUpdateElement(iRubricIconId,
				"getgrade"));
		a.onclick();
	}
}

// render the link to update score from iRubric system
function renderLinkUpdate(iRubricIconId){
	var a = document.getElementById(getFieldToUpdateElement(iRubricIconId,
				"getgrade"));
	var updateLink = document.getElementById(getFieldToUpdateElement(iRubricIconId,"updateLink"));
	if(updateLink == null){
		var d = document.createElement("DIV");
		d.id = getFieldToUpdateElement(iRubricIconId,"updateLink");
		d.style.color ="red"
		d.innerHTML = "Grading... Click here to refresh grade";
		
		a.appendChild(d);
	}
}

// remove the link to update score from iRubric system
function removeLinkUpdate(iRubricIconId){
	var d = document.getElementById(getFieldToUpdateElement(iRubricIconId,"updateLink"));
	if(d!=null)
		discardElement(d);
}

/*
 Description
 Showing a popup to browse a iRubric URL
 Parameters
 urlPage
 a iRubric URL to browse
 */
function showPopup(urlPage) {
	window
			.open(
					urlPage,
					'_blank',
					'width=800,height=600,top=20,left=100,menubar=yes,status=yes,location=no,toolbar=yes,scrollbars=yes,resizable=yes');
}

function gradeAll(gradebookItemId, studentIds) {
	$.post("iRubricLink.jsp",
		{
			p: 'ga',
			gradebookItemId: gradebookItemId,
			rosterStudentId: studentIds
		},
		function(response) {
			showPopup(response);			
		}
		
	);
}

/*
 Description
 Getting a frame by element name
 Params
 iFrmName
 an IFrame name
 Return
 An IFrame element id
 */
function getIFrame(iFrmName) {
	if (document.all)
		return document.frames[iFrmName];
	else
		return document.getElementById(iFrmName);
}

/*
 Description:
 Get the content fetched from iRubric system inside the IFrame
 Params:
 ifrm
 the IFrame element
 Return:
 content inside the IFrame element
 */
function getIFrameContentDocument(ifrm) {
	if (document.all)
		return ifrm.document;
	else
		return ifrm.contentDocument;
}

/*
 Description:
	Putting the scores into specified elements
 Params:
	scoreStream
		the scores returned by iRubric system. Its format is <elementId1>,<score1>|<elementId2>,<score2>|...
 Return:
	None
 */
function getAllScores(scoreStream) {
	var a = new Array();

	// split the stream into (element, score) pair
	var my_array = scoreStream.split("|");

	for (i = 0; i < my_array.length; i++) {
		var sub_array = my_array[i].split(",");
		a[sub_array[0]] = sub_array[1];
	}

	// get the first form element in this document
	var frm = document.forms[0];
	if (frm != null) {
		for (i = 0; i < frm.length; i++) {
			var hiddenField = frm.elements[i];
			var fieldId = hiddenField.id;
			if (fieldId.lastIndexOf("Score") > 0) {
				var score = a[hiddenField.alt];
				if (typeof (score) != 'undefined') {
					// TODO: an error might be thrown by the below command in case the result page supports paging
					var txt = document.getElementById(fieldId);
					txt.value = score;
				}
				
				var d = document.getElementById(getFieldToUpdateElement(fieldId,"updateLink"))
				if(d!=null){
					discardElement(d);
				}	
			}
			
		}
		alert("All grades have been refreshed from iRubric system. Please remember to click [Save Changes].");
	}
	
	// remove the IFrame created before, please see ../inc/iRubricGradeInDetailPage.jspf file, line 21
	var allgrades = document.getElementById("allgrades");
	if (allgrades != null) {
		discardElement(allgrades);
	}
}

/*
 Description:
	Alert when no student has been graded
 Params:
	None
 Return:
	None
 */
function alertNoScore() {
	alert('There are no scored rubrics for this gradebook item.  Please click on the iRubric icon to grade students first.');
	// remove the IFrame created before, please see ../inc/iRubricGradeInDetailPage.jspf file, line 21
	var allgrades = document.getElementById("allgrades");
	discardElement(allgrades);
}

function unAttachediRubric() {
	alert('An iRubric has not been attached to this gradebook item.  Please go to the Gradebook Item edit page to attach an iRubric.');
	// remove the IFrame created before, please see ../inc/iRubricGradeInDetailPage.jspf file, line 21
	var allgrades = document.getElementById("allgrades");
	discardElement(allgrades);
}

/*
 Description:
	Alert when invalid returned value from iRubric system.
 Params: 
	IFrame Id
 Return:
	None
 */
function alertInvalidValue(frmId, errorCode) {
	alert('Error ' + errorCode + '. Please contact your system administrator.');
	// remove the IFrame created before, please see ../inc/iRubricGradeInDetailPage.jspf file, line 21

	var frm = document.getElementById(frmId);
	if (frm != null) {
		discardElement(frm);		
	}
}

/*
Description:
 Move iframe to div element
 After that, remove div emlement
 Target: fixed memory leaks bug in firefox
 Source: http://social.msdn.microsoft.com/Forums/en-US/iewebdevelopment/thread/c76967f0-dcf8-47d0-8984-8fe1282a94f5
 Params: element
 Return:
 None
*/
function discardElement(element) {
    var garbageBin = document.getElementById('LeakGarbageBin');
    if (!garbageBin) {
    	garbageBin = document.createElement('DIV');
        garbageBin.id = 'LeakGarbageBin';
        garbageBin.style.display = 'none';
        document.body.appendChild(garbageBin);
    }
    // move the element to the garbage bin
    garbageBin.appendChild(element);
    garbageBin.innerHTML = '';
	removeChildSafe(garbageBin);
}
/*
Description:
 Remove element by tree
 Params: element
 Return:
 None
*/
function removeChildSafe(el) {
    //before deleting el, recursively delete all of its children.
    while(el.childNodes.length > 0) {    	
        removeChildSafe(el.childNodes[el.childNodes.length-1]);
    }
    el.parentNode.removeChild(el);
}