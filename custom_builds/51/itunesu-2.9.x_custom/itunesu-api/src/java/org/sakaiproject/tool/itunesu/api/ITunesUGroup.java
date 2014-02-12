package org.sakaiproject.tool.itunesu.api;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUGroup {
	
	String name;
	
	String handle;
	
	List<ITunesUTrack> tracks;
	
	List<ITunesUPermission> permissions;
	
	public ITunesUGroup()
	{
		tracks = new Vector<ITunesUTrack>();
		permissions = new Vector<ITunesUPermission>();
	}

	public ITunesUGroup(String name, String handle, List<ITunesUTrack> tracks, List<ITunesUPermission> permissions) {
		super();
		this.name = name;
		this.handle = handle;
		this.tracks = tracks;
		this.permissions = permissions;
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

	public List<ITunesUTrack> getTracks() {
		return tracks;
	}
	
	public void addTrack(ITunesUTrack track)
	{
		tracks.add(track);
	}

	public void setTracks(List<ITunesUTrack> tracks) {
		this.tracks = tracks;
	}

	public List<ITunesUPermission> getPermissions() {
		return permissions;
	}

	public void addPermission(ITunesUPermission permission)
	{
		permissions.add(permission);
	}
	
	public void setPermissions(List<ITunesUPermission> permissions) {
		this.permissions = permissions;
	}
	public static ITunesUGroup fromXml(Element element)
	{
		ITunesUGroup group = new ITunesUGroup();
		
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
						group.setName(node.getTextContent());
					}
					else if (("Handle").equals(nodeName))
					{
						group.setHandle(node.getTextContent());
					}
					else if (("Track").equals(nodeName))
					{
						group.addTrack(ITunesUTrack.fromXml((Element) node));
					}
					else if (("Permission").equals(nodeName))
					{
						group.addPermission(ITunesUPermission.fromXml((Element) node));
					}
				}
			}
		}
		return group;
	}
	
	public Element toXml(Document doc)
	{
		Element group = doc.createElement("Group");

		if (getName() != null)
		{
			Element name = doc.createElement("Name");
			name.setTextContent(getName());
			group.appendChild(name);
		}
		
		if (getHandle() != null)
		{
			Element handle = doc.createElement("Handle");
			handle.setTextContent(getHandle());
			group.appendChild(handle);
		}
		
		if (tracks != null && !tracks.isEmpty())
		{
			for(ITunesUTrack track : tracks)
			{
				group.appendChild(track.toXml(doc));
			}
		}
		
		if (permissions != null && !permissions.isEmpty())
		{
			for(ITunesUPermission permission : permissions)
			{
				group.appendChild(permission.toXml(doc));
			}
		}
		return group;
	}
}
