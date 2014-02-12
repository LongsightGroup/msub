package org.sakaiproject.tool.itunesu.api;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * the iTunesU Section element in ShowTree result
 * @author zqian
 *
 */
public class ITunesUSection {
	
	String name;
	
	String handle;
	
	List<ITunesUCourse> courses;
	
	public ITunesUSection()
	{
		courses = new Vector<ITunesUCourse>();
	}

	public ITunesUSection(String name, String handle,
			List<ITunesUCourse> courses, String aggregationFileSize) {
		super();
		this.name = name;
		this.handle = handle;
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
	
	public static ITunesUSection fromXml(Element element)
	{
		ITunesUSection section = new ITunesUSection();
		
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
						section.setName(node.getTextContent());
					}
					else if (("Handle").equals(nodeName))
					{
						section.setHandle(node.getTextContent());
					}
					else if (("Course").equals(nodeName))
					{
						section.addCourse(ITunesUCourse.fromXml((Element) node));
					}
				}
			}
		}
		return section;
	}
	
	public Element toXml(Document doc)
	{
		Element section = doc.createElement("Section");
		
		if (getName() != null)
		{
			Element name = doc.createElement("Name");
			name.setTextContent(getName());
			section.appendChild(name);
		}
		
		if (getHandle() != null)
		{
			Element handle = doc.createElement("Handle");
			handle.setTextContent(getHandle());
			section.appendChild(handle);
		}
		
		if (courses != null && !courses.isEmpty())
		{
			for(ITunesUCourse course:courses)
			{
				section.appendChild(course.toXml(doc));
			}
		}

		return section;
	}
	
}
