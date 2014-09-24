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

package com.rsmart.admin.customizer.util;

import org.w3c.dom.*;
import org.sakaiproject.util.Xml;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 25, 2006
 * Time: 10:16:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExcelPreProcessor extends XsltPreProcessor {

   public ExcelPreProcessor() {
   }

   public ExcelPreProcessor(boolean runtime) {
      super(runtime);
   }

   public void processDocument(String fileName, String sourceDir, String outputDir) {
      processDocument(Xml.readDocument(fileName), sourceDir, outputDir);
   }
   
   public void processDocument(InputStream file, String sourceDir, String outputDir) {
      processDocument(Xml.readDocumentFromStream(file), sourceDir, outputDir);
   }

   public void processDocument(Document mainDoc, String sourceDir, String outputDir) {

      Map baseMap = new Hashtable();
      baseMap.put("source", sourceDir);
      baseMap.put("output", outputDir);

      setResolver(new FileSystemResourceResolver(baseMap));
      setWorking(new File(outputDir));

      NodeList worksheets = mainDoc.getDocumentElement().getElementsByTagName("Worksheet");

      for (int i=0;i<worksheets.getLength();i++) {
         processWorksheet(outputDir, (Element) worksheets.item(i));
      }
   }

   protected void processWorksheet(String outputDir, Element worksheetNode) {
      String name = worksheetNode.getAttribute("ss:Name");

      Document outputDoc = Xml.createDocument();
      Element outputElement = outputDoc.createElement("Worksheet");
      outputDoc.appendChild(outputElement);

      outputElement.setAttribute("xmlns:o", "urn:schemas-microsoft-com:office:office");
      outputElement.setAttribute("xmlns:x", "urn:schemas-microsoft-com:office:excel");
      outputElement.setAttribute("xmlns:dt", "uuid:C2F41010-65B3-11d1-A29F-00AA00C14882");
      outputElement.setAttribute("xmlns:html", "http://www.w3.org/TR/REC-html40");
      outputElement.setAttribute("xmlns:ss", "urn:schemas-microsoft-com:office:spreadsheet");

      NodeList tables = worksheetNode.getElementsByTagName("Table");

      for (int i=0;i<tables.getLength();i++) {
         processTable(outputDoc, outputElement, (Element) tables.item(i), i);
      }

      try {
         outputDocument("transform/scrubber/normalizeSpace.xsl", name +".xml", outputDoc);
      } catch (FileNotFoundException e) {
         logger.error(e);
      } catch (IOException e) {
         logger.error(e);
      } catch (TransformerException e) {
         logger.error(e);
      }
   }

   protected void processTable(Document outputDoc, Element worksheetElement,
                               Element tableElement, int index) {
      Element outputTable = outputDoc.createElement("Table");
      outputTable.setAttribute("order", "" + index);

      NodeList rows = tableElement.getElementsByTagName("Row");
      Groupings groupings = new Groupings();

      int lastRow = 0;
      for (int i=0;i<rows.getLength();i++) {
         lastRow = processRow(outputDoc, outputTable, (Element) rows.item(i), lastRow, groupings);
      }

      worksheetElement.appendChild(outputTable);
   }

   protected int processRow(Document outputDoc, Element outputTable, Element row,
                            int lastRow, Groupings groupings) {
      Element outputRow = outputDoc.createElement("Row");

      String rowIndex = row.getAttribute("ss:Index");

      if (rowIndex != null && rowIndex.length() > 0) {
         int docCellIndex = Integer.valueOf(rowIndex).intValue();

         // create the empty cells
         for (;lastRow < docCellIndex;lastRow++) {
            Element emptyRow = outputDoc.createElement("Row");
            emptyRow.setAttribute("order", "" + lastRow);
            outputTable.appendChild(emptyRow);
         }

      }
      else if (lastRow == 0){
         Element emptyRow = outputDoc.createElement("Row");
         emptyRow.setAttribute("order", "" + lastRow);
         outputTable.appendChild(emptyRow);
         lastRow++;
      }
      else {
         lastRow++;
      }

      outputRow.setAttribute("order", "" + lastRow);
      outputRow.setAttribute("index", row.getAttribute("ss:Index"));

      NodeList cells = row.getElementsByTagName("Cell");

      int lastCell = 0;

      for (int i=0;i<cells.getLength();i++) {
         lastCell = processCell(outputDoc, outputRow, (Element)cells.item(i), lastCell);
      }

      outputTable.appendChild(outputRow);

      groupChanged(outputRow, groupings);

      outputRow.setAttribute("group1", "" + groupings.getGroup1());
      outputRow.setAttribute("group2", "" + groupings.getGroup2());
      outputRow.setAttribute("group3", "" + groupings.getGroup3());
      return lastRow;
   }

   protected void groupChanged(Element outputRow, Groupings groupings) {
      NodeList cells = outputRow.getElementsByTagName("Cell");

      if (cells.getLength() >= 2) {
         if (elementHasText((Element)cells.item(1))) {
            groupings.setGroup1(groupings.getGroup1()+1);
            groupings.setGroup2(groupings.getGroup2()+1);
            groupings.setGroup3(groupings.getGroup3()+1);
            return;
         }
      }

      if (cells.getLength() >= 3) {
         if (elementHasText((Element)cells.item(2))) {
            groupings.setGroup2(groupings.getGroup2()+1);
            groupings.setGroup3(groupings.getGroup3()+1);
            return;
         }
      }

      if (cells.getLength() >= 4) {
         if (elementHasText((Element)cells.item(3))) {
            groupings.setGroup3(groupings.getGroup3()+1);
         }
      }

   }

   protected boolean elementHasText(Element element) {
      Node cellData = element.getElementsByTagName("Data").item(0);

      if (cellData == null) {
         return false;
      }

      NodeList list = cellData.getChildNodes();

      for (int i=0;i<list.getLength();i++){
         Node node = list.item(i);
         if (node.getNodeType() == Node.TEXT_NODE) {
            if (node.getNodeValue().trim().length() > 0) {
               return true;
            }
         }
      }
      return false;
   }

   protected int processCell(Document outputDoc, Element outputRow, Element cell, int lastCell) {
      String cellIndex = cell.getAttribute("ss:Index");

      if (cellIndex != null && cellIndex.length() > 0) {
         int docCellIndex = Integer.valueOf(cellIndex).intValue();

         // create the empty cells
         for (;lastCell < docCellIndex;lastCell++) {
            Element emptyCell = outputDoc.createElement("Cell");
            emptyCell.setAttribute("order", "" + lastCell);
            outputRow.appendChild(emptyCell);
         }

      }
      else if (lastCell == 0){
         Element emptyCell = outputDoc.createElement("Cell");
         emptyCell.setAttribute("order", "" + lastCell);
         outputRow.appendChild(emptyCell);
         lastCell++;
      }
      else {
         lastCell++;
      }

      Element importedCell = (Element) outputDoc.importNode(cell, true);

      cleanCell(importedCell);

      importedCell.setAttribute("order", "" + lastCell);

      outputRow.appendChild(importedCell);

      String merge = cell.getAttribute("ss:MergeAcross");

      if (merge != null && merge.length() > 0) {
         // might have to insert some cells
         int cells = Integer.valueOf(merge).intValue();
         for (int i=0;i<cells;i++) {
            lastCell++;
            Element emptyCell = outputDoc.createElement("Cell");
            emptyCell.setAttribute("order", "" + lastCell);
            outputRow.appendChild(emptyCell);
         }
      }
      return lastCell;
   }

   protected void cleanCell(Element element) {
      NodeList children = element.getChildNodes();

      for (int i=0;i<children.getLength();i++) {
         Node node = children.item(i);
         if (node instanceof Element) {
            cleanCell((Element) node);
         }
      }

      NamedNodeMap attribs = element.getAttributes();

      while (attribs.getLength() > 0) {
         Node node = attribs.item(0);
         element.removeAttributeNode((Attr) node);
      }
   }

}
