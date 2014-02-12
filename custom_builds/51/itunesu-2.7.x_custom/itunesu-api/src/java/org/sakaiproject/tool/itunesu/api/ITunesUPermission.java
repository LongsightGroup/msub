package org.sakaiproject.tool.itunesu.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUPermission {

	String credential;
	
	String access;
	
	public ITunesUPermission()
	{	
	}

	public ITunesUPermission(String credential, String access) {
		super();
		this.credential = credential;
		this.access = access;
	}

	public String getCredential() {
		return credential;
	}

	public void setCredential(String credential) {
		this.credential = credential;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}
	
	public static ITunesUPermission fromXml(Element element)
	{
		ITunesUPermission permission = new ITunesUPermission();
		
		NodeList nodes = element.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = node.getNodeName();
				if (nodeName != null)
				{
					if ("Credential".equals(nodeName))
					{
						permission.setCredential(node.getTextContent());
					}
					else if (("Access").equals(nodeName))
					{
						permission.setAccess(node.getTextContent());
					}
				}
			}
		}
		return permission;
	}
	
	public Element toXml(Document doc)
	{
		Element permission = doc.createElement("Permission");
		
		if (getCredential() != null)
		{
			Element credential = doc.createElement("Credential");
			credential.setTextContent(getCredential());
			permission.appendChild(credential);
		}
		
		if (getAccess() != null)
		{
			Element access = doc.createElement("Access");
			access.setTextContent(getAccess());
			permission.appendChild(access);
		}
		
		return permission;
	}
	
}
