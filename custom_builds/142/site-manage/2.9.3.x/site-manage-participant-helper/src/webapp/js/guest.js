
var m_disclaimed = false;
var m_oldelem;
var m_policy_text;
var m_continue_e;

var m_guestBox;


function initGuestPolicyCheck(button_id, officialParticipantsBox, nonOfficialParticipantsBox) {
    
    m_policy_text = "default policy text";
    if(typeof(add_guest_policy_text) != undefined) { // this should be defined globally via UIVerbatim
    	m_policy_text = add_guest_policy_text;
    	}
    	
    m_continue_e = document.getElementById(button_id);
    m_guestBox= document.getElementById(nonOfficialParticipantsBox);
    //m_guestBox.onKeyUp = checkStatus;
	//checkStatus();
    //m_continue_e.form.onSubmit = validateEmailAddress;

}

function isTextareaEmpty(id) {
    
    var t = document.getElementById(id);
	
	if(null === t || null === t.value) {
	    alert("Error: no textarea value found: " + id);
	    return true;
	    }
	    
	return t.value.replace(/\s*/, "").length === 0;

}

function canSubmitFormWGuest() {


	if (m_disclaimed) {
		if (m_guestBox.value === m_oldelem) {
			return true;
		}
	}
	var regexp = /\\+/g;
	if ( !isTextareaEmpty(m_guestBox.id) && !window.confirm(m_policy_text.replace(regexp, '\\')) ) {
		
		 return false;
	} else {
		m_disclaimed = true;
		m_oldelem = m_guestBox.value;
		return true;
	}
}

function check_email(emailAddress) {

	var ok = "1234567890qwertyuiop[]asdfghjklzxcvbnm.@-_QWERTYUIOPASDFGHJKLZXCVBNM";

	for (var i = 0; i < emailAddress.length; i++) {

		if (ok.indexOf(emailAddress.charAt(i)) < 0) {
			return false;
		}
	}

	var re = /(@.*@)|(\.\.)|(^\.)|(^@)|(@$)|(\.$)|(@\.)/;
	var re_two = /^.+\@(\[?)[a-zA-Z0-9\-\.]+\.([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;

	if (!emailAddress.match(re) && emailAddress.match(re_two)) {

		return true;
	}

	return false;
}

function validateEmailAddress() {

	var containertext = m_guestBox.value.split("\n");
	var hasInvalidEmailAddresses = false;

	for (var x in containertext) {
	  if (containertext.hasOwnProperty(x)) {
		var address = containertext[x].replace(/^\s*/,"").replace(/\s*$/,""); //trim whitespace
		if ("" === address) {
			continue;
		}
		if (!check_email(address)) {
			alert("\"" + address + "\"\nis not a valid email address");
			hasInvalidEmailAddresses = true;
		}
		// TODO: put trimmed values back into textarea?
	  }
	}
	
	return !hasInvalidEmailAddresses;
}


function canContinue() {
	return validateEmailAddress() && canSubmitFormWGuest();
}

