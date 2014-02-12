package org.sakaiproject.tool.itunesu.api;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ITunesUSite {
	String name;
	String handle;
	boolean allowSubscription = false;
	String themeHandle;
	List<ITunesUPermission> permissions;
	List<ITunesUSection> sections;
	List<ITunesUTemplate> templates;
	
	public ITunesUSite()
	{
		permissions = new Vector<ITunesUPermission>();
		sections = new Vector<ITunesUSection>();
		templates = new Vector<ITunesUTemplate>();
	}
	
	public ITunesUSite(String name, String handle, boolean allowSubscription,
			String themeHandle, List<ITunesUPermission> permissions,
			List<ITunesUSection> sections, List<ITunesUTemplate> templates) {
		super();
		this.name = name;
		this.handle = handle;
		this.allowSubscription = allowSubscription;
		this.themeHandle = themeHandle;
		this.permissions = permissions;
		this.sections = sections;
		this.templates = templates;
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

	public boolean isAllowSubscription() {
		return allowSubscription;
	}

	public void setAllowSubscription(boolean allowSubscription) {
		this.allowSubscription = allowSubscription;
	}

	public String getThemeHandle() {
		return themeHandle;
	}

	public void setThemeHandle(String themeHandle) {
		this.themeHandle = themeHandle;
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

	public List<ITunesUSection> getSections() {
		return sections;
	}

	public void addSection(ITunesUSection section)
	{
		sections.add(section);
	}
	
	public void setSections(List<ITunesUSection> sections) {
		this.sections = sections;
	}

	public List<ITunesUTemplate> getTemplates() {
		return templates;
	}

	public void addTemplate(ITunesUTemplate template)
	{
		templates.add(template);
	}
	
	public void setTemplates(List<ITunesUTemplate> templates) {
		this.templates = templates;
	}
	
	public static ITunesUSite fromXml(Element element)
	{
		ITunesUSite site = new ITunesUSite();
		
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
						site.setName(node.getTextContent());
					}
					else if (("Handle").equals(nodeName))
					{
						site.setHandle(node.getTextContent());
					}
					else if (("AllowSubscription").equals(nodeName))
					{
						site.setAllowSubscription("true".equals(node.getTextContent()));
					}
					else if (("ThemeHanle").equals(nodeName))
					{
						site.setThemeHandle(node.getTextContent());
					}
					else if (("Permission").equals(nodeName))
					{
						site.addPermission(ITunesUPermission.fromXml((Element) node));
					}
					else if (("Section").equals(nodeName))
					{
						site.addSection(ITunesUSection.fromXml((Element) node));
					} 
					else if (("Template").equals(nodeName))
					{
						site.addTemplate(ITunesUTemplate.fromXml((Element) node));
					}
				}
			}
		}
		return site;
	}
	
	public Element toXml(Document doc)
	{
		Element site = doc.createElement("Site");

		if (name != null)
		{
			Element nameElement = doc.createElement("Name");
			nameElement.setTextContent(name);
			site.appendChild(nameElement);
		}
		
		if (handle != null)
		{
			Element handleElement = doc.createElement("Handle");
			handleElement.setTextContent(handle);
			site.appendChild(handleElement);
		}
		
		Element allowSubscriptionElement = doc.createElement("AllowSubscription");
		allowSubscriptionElement.setTextContent(allowSubscription?"true":"false");
		site.appendChild(allowSubscriptionElement);
		
		if (themeHandle != null)
		{
			Element themeElement = doc.createElement("ThemeHandle");
			themeElement.setTextContent(themeHandle);
			site.appendChild(themeElement);
		}
		
		if (permissions != null && !permissions.isEmpty())
		{
			for(ITunesUPermission permission:permissions)
			{
				site.appendChild(permission.toXml(doc));
			}
		}
		
		if (sections != null && !sections.isEmpty())
		{
			for(ITunesUSection section:sections)
			{
				site.appendChild(section.toXml(doc));
			}
		}
		
		if (templates != null && !templates.isEmpty())
		{
			for(ITunesUTemplate template:templates)
			{
				site.appendChild(template.toXml(doc));
			}
		}
		
		return site;
	}

}
