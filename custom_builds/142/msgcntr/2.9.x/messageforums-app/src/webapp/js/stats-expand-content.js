function toggleMessage(obj, messageId) {
	var msgRow = getMessageRow(messageId);
	
	var img = $($(obj).find("img.toggle").get(0));
	if (img.attr("src").match("collapse")) {
            img.attr("src", "/sakai-messageforums-tool/images/expand.gif");
    } else {
            img.attr("src", "/sakai-messageforums-tool/images/collapse.gif");
    }
	
	if (msgRow.size() == 0) {
		createMessageRow(obj, messageId);
	} else {
		if (msgRow.css('display') == 'none') {
			msgRow.show();
		} else {
			msgRow.hide();
		}
	}
	
	eval(getMainFrameHeightFunction(obj));
}

function createMessageRow(obj, messageId) {
	var row = '';
	
	var tr = $(obj).parents("tr").get(0);
	var hiddenContent = $(tr).find("td.message-content > span").get(0);
	
	row += '<tr id="msgRow_'+messageId+'">';
	row +=		'<td>';
	row +=			'<img src="/sakai-messageforums-tool/images/folder.gif" alt="Message" />';
	row +=		'</td>';
	row +=		'<td colspan="4" style="border: dashed 1px #888;">';
	row +=			$(hiddenContent).html();
	row +=		'</td>';
	row += '</tr>';
	
	$(tr).after(row);
}

function getMessageRow(messageId) {
	return $("#msgRow_"+messageId);
}

function getMainFrameHeightFunction(obj) {
	var body = $(obj).parents("body").get(0);
	var onload = $(body).attr("onload");
	var commands = onload.split(";");

	return commands[0];
}
