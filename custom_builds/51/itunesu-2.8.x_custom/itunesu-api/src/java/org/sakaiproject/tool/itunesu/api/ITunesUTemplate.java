package org.sakaiproject.tool.itunesu.api;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUTemplate {
	
	String name;
	
	String handle;
	
	List<ITunesUPermission> permissions;
	
	List<ITunesUCourse> courses;
	
	public ITunesUTemplate()
	{
		
	}
	
	public ITunesUTemplate(String name, String handle,
			List<ITunesUPermission> permission, List<ITunesUCourse> courses) {
		super();
		this.name = name;
		this.handle = handle;
		this.permissions = permission;
		this.courses = courses;
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

	public List<ITunesUCourse> getCourses() {
		return courses;
	}

	public void addCourse(ITunesUCourse course)
	{
		courses.add(course);
	}
	
	public void setCourses(List<ITunesUCourse> courses) {
		this.courses = courses;
	}
	
	public static ITunesUTemplate fromXml(Element element)
	{
		ITunesUTemplate template = new ITunesUTemplate();
		
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
						template.setName(node.getTextContent());
					}
					else if ("Handle".equals(nodeName))
					{
						template.setHandle(node.getTextContent());
					}
					else if ("Permission".equals(nodeName))
					{
						template.addPermission(ITunesUPermission.fromXml((Element) node));
					}
					else if ("Course".equals(nodeName))
					{
						template.addCourse(ITunesUCourse.fromXml((Element) node));
					}
				}
			}
		}
		
		return template;
	}
	
	public Element toXml(Document doc)
	{
		Element template = doc.createElement("template");
		
		if (name != null)
		{
			Element nameElement = doc.createElement("Name");
			nameElement.setTextContent(name);
			template.appendChild(nameElement);
		}
		
		if (handle != null)
		{
			Element handleElement = doc.createElement("Handle");
			handleElement.setTextContent(handle);
			template.appendChild(handleElement);
		}
		
		if (permissions != null && !permissions.isEmpty())
		{
			for(ITunesUPermission permission:permissions)
			{
				template.appendChild(permission.toXml(doc));
			}
		}
		
		if (courses != null && !courses.isEmpty())
		{
			for(ITunesUCourse course:courses)
			{
				template.appendChild(course.toXml(doc));
			}
		}
		return template;
	}

}
