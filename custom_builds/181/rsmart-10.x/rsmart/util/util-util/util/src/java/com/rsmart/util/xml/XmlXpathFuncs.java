/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): jbush
 */

package com.rsmart.util.xml;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.Xml;

import java.util.StringTokenizer;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Sep 6, 2006
 * Time: 3:03:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class XmlXpathFuncs {

   protected final static transient Log logger = LogFactory.getLog(XmlXpathFuncs.class);

   public static NodeList tokenize(String string, String token) {
      Document doc = Xml.createDocument();

      Element parent = doc.createElement("parent");

      StringTokenizer st = new StringTokenizer(string, token);

      while(st.hasMoreElements()) {
         String tok = st.nextToken();
         Element data = doc.createElement("data");
         data.appendChild(doc.createTextNode(tok));
         parent.appendChild(data);
      }

      return parent.getElementsByTagName("data");
   }

   public static NodeList subsequence(NodeList list, int start, int length) {
      start = start - 1; // zero index

      if (list.getLength() == 0) {
         return list;
      }

      Element parent = Xml.createDocument().createElement("parent");

      for (int i=start;i<start + length;i++) {
         Node node = list.item(i);
         if (node != null) {
            parent.appendChild(parent.getOwnerDocument().importNode(node, true));
         }
      }

      return parent.getChildNodes();
   }

   public static Node findOrder(Node row, String searchString) {
      NodeList cells = row.getChildNodes();

      for (int i=0;i<cells.getLength();i++) {
         Node cell = cells.item(i);

         if (cell.getNodeName().equals("Cell")) {
            NodeList data = cell.getChildNodes();
            for (int j=0;j<data.getLength();j++) {
               String nodeText = getElementText(data.item(j));
               if (nodeText != null && nodeText.equals(searchString)) {
                  return cell;
               }
            }
         }
      }
      return null;
   }

   public static String getElementText(Node element) {
      NodeList list = element.getChildNodes();

      for (int i=0;i<list.getLength();i++){
         Node node = list.item(i);
         if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
         }
      }

      return null;
   }
   
   public static String translateKey(String id) {
      StringBuffer string = new StringBuffer(id);
      
      for (int i=0;i<string.length();i++) {
         if (replaceChar(string.charAt(i))) {
            string.setCharAt(i, '_');
         }
      }
      
      return string.toString();
   }

   protected static boolean replaceChar(char c) {
      if (c == '.' || c == '_') {
         return false;
      }
      
      if (Character.isLetterOrDigit(c)) {
         return false;
      }

      return true;
   }

   public static String formatMessage(String format, String param1) {
      return formatMessage(format,  new Object[]{param1});
   }

   public static String formatMessage(String format, String param1, String param2) {
      return formatMessage(format,  new Object[]{param1, param2});
   }

   public static String formatMessage(String format, String param1, String param2, String param3) {
      return formatMessage(format,  new Object[]{param1, param2, param3});
   }

   public static String formatMessage(String format, Object[] params) {
      return MessageFormat.format(format, params);
   }
}
