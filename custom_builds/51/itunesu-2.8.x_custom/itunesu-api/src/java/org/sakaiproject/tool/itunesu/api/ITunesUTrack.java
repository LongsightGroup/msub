package org.sakaiproject.tool.itunesu.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUTrack {
	String name;
	
	String handle;
	
	String kind;
	
	String discNumber;
	
	String durationMillisenconds;
	
	String albumName;
	
	String downloadUrl;
	
	String categoryCode;
	
	public ITunesUTrack()
	{
		
	}
	
	public ITunesUTrack(String name, String handle, String kind,
			String discNumber, String durationMillisenconds, String albumName,
			String downloadUrl, String categoryCode) {
		super();
		this.name = name;
		this.handle = handle;
		this.kind = kind;
		this.discNumber = discNumber;
		this.durationMillisenconds = durationMillisenconds;
		this.albumName = albumName;
		this.downloadUrl = downloadUrl;
		this.categoryCode = categoryCode;
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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getDiscNumber() {
		return discNumber;
	}

	public void setDiscNumber(String discNumber) {
		this.discNumber = discNumber;
	}

	public String getDurationMillisenconds() {
		return durationMillisenconds;
	}

	public void setDurationMillisenconds(String durationMillisenconds) {
		this.durationMillisenconds = durationMillisenconds;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	
	public static ITunesUTrack fromXml(Element element)
	{
		ITunesUTrack track = new ITunesUTrack();
		
		NodeList nodes = element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = node.getNodeName();
				if (nodeName != null)
				{
					if ("Name".equals(nodeName))
					{
						track.setName(node.getTextContent());
					}
					else if ("Handle".equals(nodeName))
					{
						track.setHandle(node.getTextContent());
					}
					else if ("Kind".equals(nodeName))
					{
						track.setKind(node.getTextContent());
					}
					else if ("DiscNumber".equals(nodeName))
					{
						track.setDiscNumber(node.getTextContent());
					}
					else if ("DurationMillisenconds".equals(nodeName))
					{
						track.setDurationMillisenconds(node.getTextContent());
					}
					else if ("AlbumName".equals(nodeName))
					{
						track.setAlbumName(node.getTextContent());
					}
					else if ("DownloadCategoryCode".equals(nodeName))
					{
						track.setDownloadUrl(node.getTextContent());
					}
					else if ("category".equals(nodeName))
					{
						track.setCategoryCode(node.getTextContent());
					}
				}
			}
		}
		
		return track;
	}
	
	public Element toXml(Document doc)
	{
		Element track = doc.createElement("Track");

		if (name != null)
		{
			Element nameElement = doc.createElement("Name");
			nameElement.setTextContent(name);
			track.appendChild(nameElement);
		}
		
		if (handle != null)
		{
			Element handleElement = doc.createElement("Handle");
			handleElement.setTextContent(handle);
			track.appendChild(handleElement);
		}
		
		if (kind != null)
		{
			Element kindElement = doc.createElement("Kind");
			kindElement.setTextContent(kind);
			track.appendChild(kindElement);
		}
		
		if (discNumber != null)
		{
			Element discNumberElement = doc.createElement("DiscNumber");
			discNumberElement.setTextContent(discNumber);
			track.appendChild(discNumberElement);
		}
		
		if (durationMillisenconds != null)
		{
			Element durationMillisencondsElement= doc.createElement("DurationMillisenconds");
			durationMillisencondsElement.setTextContent(durationMillisenconds);
			track.appendChild(durationMillisencondsElement);
		}
		
		if (albumName != null)
		{
			Element albumNameElement= doc.createElement("AlbumName");
			albumNameElement.setTextContent(albumName);
			track.appendChild(albumNameElement);
		}
		
		if (downloadUrl != null)
		{
			Element downloadUrlElement= doc.createElement("DownloadUrl");
			downloadUrlElement.setTextContent(downloadUrl);
			track.appendChild(downloadUrlElement);
		}
		
		if (categoryCode != null)
		{
			Element categoryCodeElement= doc.createElement("CategoryCode");
			categoryCodeElement.setTextContent(categoryCode);
			track.appendChild(categoryCodeElement);
		}
		return track;
	}	
}
