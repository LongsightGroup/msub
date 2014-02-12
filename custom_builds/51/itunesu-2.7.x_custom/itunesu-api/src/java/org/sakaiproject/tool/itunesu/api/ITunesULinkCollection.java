package org.sakaiproject.tool.itunesu.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ITunesULinkCollection {
	
	String name;
	
	String handle;
	
	String feedUrl;

	public ITunesULinkCollection()
	{
		
	}
	
	public ITunesULinkCollection(String name, String handle, String feedUrl) {
		super();
		this.name = name;
		this.handle = handle;
		this.feedUrl = feedUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getFeedUrl() {
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
	
	public static ITunesULinkCollection fromXml(Element element)
	{
		ITunesULinkCollection links = new ITunesULinkCollection();
		return links;
	}
	
	public Element toXml(Document doc)
	{
		Element links = doc.createElement("LinkCollection");

		return links;
	}

}
