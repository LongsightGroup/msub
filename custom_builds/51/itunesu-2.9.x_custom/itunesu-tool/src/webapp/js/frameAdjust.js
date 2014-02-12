// CREATED SINCE DEFAULT setMainFrameHeight DOES NOT 
// EXPAND IFRAME ENOUGH FOR FF

// set the parent iframe's height to hold our entire contents
function setMainFrameHeight(id, isFF)
{
	// some browsers need a moment to finish rendering so the height and scroll are correct
	setTimeout("setMainFrameHeightNow('"+id+"'," + isFF +")",1);
}

function setMainFrameHeightNow(id, isFF)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{
		// reset the scroll
		parent.window.scrollTo(0,0);

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 		
		var offsetH = document.body.offsetHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}
	
		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		var newHeight = height + 10;
		if (isFF) newHeight += 40;

		// but not too big!
		if (newHeight > 32760) newHeight = 32760;

		// capture my current scroll position
		var scroll = findScroll();

		// resize parent frame (this resets the scroll as well)
		objToResize.height=newHeight + "px";

		// reset the scroll, unless it was y=0)
		if (scroll[1] > 0)
		{
			var position = findPosition(frame);
			parent.window.scrollTo(position[0]+scroll[0], position[1]+scroll[1]);
		}

// optional hook triggered after the head script fires.

		if (parent.postIframeResize){ 
			parent.postIframeResize(id);
		}
	}
}

// find the object position in its window
// inspired by http://www.quirksmode.org/js/findpos.html
function findPosition(obj)
{
	var x = 0;
	var y = 0;
	if (obj.offsetParent)
	{
		x = obj.offsetLeft;
		y = obj.offsetTop;
		while (obj = obj.offsetParent)
		{
			x += obj.offsetLeft;
			y += obj.offsetTop;
		}
	}
	return [x,y];
}

// find my scroll
// inspired by http://www.quirksmode.org/viewport/compatibility.html
function findScroll()
{
	var x = 0;
	var y = 0;
	if (self.pageYOffset)
	{
		x = self.pageXOffset;
		y = self.pageYOffset;
	}
	else if (document.documentElement && document.documentElement.scrollTop)
	{
		x = document.documentElement.scrollLeft;
		y = document.documentElement.scrollTop;
	}
	else if (document.body)
	{
		x = document.body.scrollLeft;
		y = document.body.scrollTop;
	}
	
	return [x,y];
}

