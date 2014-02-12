/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.itunesu.api;

import java.util.HashMap;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author zqian
 */
public class Digester implements ContentHandler
{

	private Stack<String> stack = new Stack<String>();

	private static final String COURSE_TAG = "Course";
	private static final String NAME_TAG = "Name";
	private static final String HANDLE_TAG = "Handle";
	
	public String[] m_lookForElementValues;
	
	public boolean m_found = false;
	
	public String m_courseHandle = "";

	public Digester(String[] lookForElementValues)
	{
		m_lookForElementValues = lookForElementValues;
	}

	public void setDocumentLocator(Locator arg0)
	{
	}

	public void startDocument() throws SAXException
	{
	}

	public void endDocument() throws SAXException
	{
	}

	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException
	{
	}

	public void endPrefixMapping(String arg0) throws SAXException
	{
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException
	{
		if ( COURSE_TAG.equals(localName) || !(stack.isEmpty()) )
		{
			// either starting to process or are already processing a course
			// definition
			stack.push(localName);
		}
	}

	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException
	{
		if ( stack.isEmpty() )
		{
			// must be an element outside a course definition
			return;
		}
		String endedElementName = stack.pop();
		if ( !(m_found) && COURSE_TAG.equals(endedElementName) )
		{
			m_courseHandle = "";
		}
	}

	public void characters(char[] arg0, int arg1, int arg2) throws SAXException
	{
		if ( stack.isEmpty() || (m_found && !("".equals(m_courseHandle))) )
		{
			// either not processing a course definition or already matched
			// on a course name _and_ found the corresponding handle (no 
			// assumptions on element ordering inside a course def)
			return;
		}
		String currentElementName = stack.peek();
		if ( HANDLE_TAG.equals(currentElementName) )
		{
			// endElement clears this when the course def ends and no name
			// name match has been found
			m_courseHandle = new String(arg0, arg1, arg2);
		} 
		else if ( NAME_TAG.equals(currentElementName) )
		{
			lookForElementValue(new String(arg0, arg1, arg2));
		}
	}

	private void lookForElementValue(String s) {
		if (m_lookForElementValues != null)
		{
			for (int i = 0; i<m_lookForElementValues.length && !m_found; i++)
			{
				if (s.contains(m_lookForElementValues[i]))
				{
					m_found = true;
				}
			}
		}
	}

	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException
	{

	}

	public void processingInstruction(String arg0, String arg1)
			throws SAXException
	{

	}

	public void skippedEntity(String arg0) throws SAXException
	{

	}
	
	public boolean found()
	{
		return m_found;
	}
	
	public String getCourseHandle()
	{
		return m_courseHandle;
	}
}
