package org.sakaiproject.tool.itunesu.api;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUCourse {
	String name;
	
	String handle;
	
	String identifier;
	
	String instructor;
	
	String description;
	
	List<ITunesUGroup> groups;
	
	List<ITunesULinkCollection> linkCollections;

	public ITunesUCourse()
	{
		groups = new Vector<ITunesUGroup>();
		linkCollections = new Vector<ITunesULinkCollection>();
	}
	
	public ITunesUCourse(String name, String hanle, String identifier, String instructor,
			String description, List<ITunesUGroup> groups,
			String aggregationFileSize,
			List<ITunesULinkCollection> linkCollections) {
		super();
		this.name = name;
		this.handle = hanle;
		this.identifier = identifier;
		this.instructor = instructor;
		this.description = description;
		this.groups = groups;
		this.linkCollections = linkCollections;
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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getInstructor() {
		return instructor;
	}

	public void setInstructor(String instructor) {
		this.instructor = instructor;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addGroup (ITunesUGroup group)
	{
		groups.add(group);
	}
	public List<ITunesUGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<ITunesUGroup> groups) {
		this.groups = groups;
	}

	public List<ITunesULinkCollection> getLinkCollections() {
		return linkCollections;
	}

	public void setLinkCollections(List<ITunesULinkCollection> linkCollections) {
		this.linkCollections = linkCollections;
	}
	
	public static ITunesUCourse fromXml(Element element)
	{
		ITunesUCourse course = new ITunesUCourse();
		
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
						course.setName(node.getTextContent());
					}
					else if (("Handle").equals(nodeName))
					{
						course.setHandle(node.getTextContent());
					}
					else if (("Instructor").equals(nodeName))
					{
						course.setInstructor(node.getTextContent());
					}
					else if (("Description").equals(nodeName))
					{
						course.setDescription(node.getTextContent());
					}
					else if (("Group").equals(nodeName))
					{
						course.addGroup(ITunesUGroup.fromXml((Element) node));
					}
				}
			}
		}
		
		return course;
	}
	
	public Element toXml(Document doc)
	{
		Element course = doc.createElement("Course");

		if (getName() != null)
		{
			Element name = doc.createElement("Name");
			name.setTextContent(getName());
			course.appendChild(name);
		}
		
		if (getHandle() != null)
		{
			Element handle = doc.createElement("Handle");
			handle.setTextContent(getHandle());
			course.appendChild(handle);
		}

		if (getIdentifier() != null)
		{
			Element identifier = doc.createElement("Identifier");
			identifier.setTextContent(getIdentifier());
			course.appendChild(identifier);
		}
		
		if (getDescription() != null)
		{
			Element description = doc.createElement("Description");
			description.setTextContent(getDescription());
			course.appendChild(description);
		}
		
		if (groups != null && !groups.isEmpty())
		{
			for (ITunesUGroup group : groups)
			{
				course.appendChild(group.toXml(doc));
			}
		}
		
		return course;
	}
}
