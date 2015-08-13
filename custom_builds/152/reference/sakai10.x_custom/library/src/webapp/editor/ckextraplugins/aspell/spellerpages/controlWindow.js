/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msub/rsmart.com/reference/trunk/library/src/webapp/editor/ckextraplugins/aspell/spellerpages/controlWindow.js $
 * $Id: controlWindow.js 121459 2013-03-19 18:07:51Z ktakacs@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
////////////////////////////////////////////////////
// controlWindow object
////////////////////////////////////////////////////
function controlWindow( controlForm ) {
	// private properties
	this._form = controlForm;

	// public properties
	this.windowType = "controlWindow";
	this.noSuggestionSelection = "- No suggestions -";
	// set up the properties for elements of the given control form
	this.suggestionList  = this._form.sugg;
	this.evaluatedText   = this._form.misword;
	this.replacementText = this._form.txtsugg;
	this.undoButton      = this._form.btnUndo;

	// public methods
	this.addSuggestion = addSuggestion;
	this.clearSuggestions = clearSuggestions;
	this.selectDefaultSuggestion = selectDefaultSuggestion;
	this.resetForm = resetForm;
	this.setSuggestedText = setSuggestedText;
	this.enableUndo = enableUndo;
	this.disableUndo = disableUndo;
}

function resetForm() {
	if( this._form ) {
		this._form.reset();
	}
}

function setSuggestedText() {
	var slct = this.suggestionList;
	var txt = this.replacementText;
	var str = "";
	if( (slct.options[0].text) && slct.options[0].text != this.noSuggestionSelection ) {
		str = slct.options[slct.selectedIndex].text;
	}
	txt.value = str;
}

function selectDefaultSuggestion() {
	var slct = this.suggestionList;
	var txt = this.replacementText;
	if( slct.options.length == 0 ) {
		this.addSuggestion( this.noSuggestionSelection );
	} else {
		slct.options[0].selected = true;
	}
	this.setSuggestedText();
}

function addSuggestion( sugg_text ) {
	var slct = this.suggestionList;
	if( sugg_text ) {
		var i = slct.options.length;
		var newOption = new Option( sugg_text, 'sugg_text'+i );
		slct.options[i] = newOption;
	 }
}

function clearSuggestions() {
	var slct = this.suggestionList;
	for( var j = slct.length - 1; j > -1; j-- ) {
		if( slct.options[j] ) {
			slct.options[j] = null;
		}
	}
}

function enableUndo() {
	if( this.undoButton ) {
		if( this.undoButton.disabled == true ) {
			this.undoButton.disabled = false;
		}
	}
}

function disableUndo() {
	if( this.undoButton ) {
		if( this.undoButton.disabled == false ) {
			this.undoButton.disabled = true;
		}
	}
}
