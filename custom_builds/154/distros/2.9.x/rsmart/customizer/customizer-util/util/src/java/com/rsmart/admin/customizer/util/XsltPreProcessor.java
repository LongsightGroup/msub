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

import org.sakaiproject.util.Xml;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Hashtable;

import com.rsmart.admin.customizer.util.FileSystemResourceResolver;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 28, 2006
 * Time: 8:25:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class XsltPreProcessor {

   protected final transient ListenableLogger logger = 
      new ListenableLogger(LogFactory.getLog(getClass()));

   private URIResolver resolver = null;
   private File working;
   private boolean runtime = false;

   public XsltPreProcessor() {

   }

   public XsltPreProcessor(boolean runtime) {
      this.runtime = runtime;
   }

   public void process(String sourceDir, String outputDir) {
      Map baseMap = new Hashtable();
      baseMap.put("source", sourceDir);
      baseMap.put("output", outputDir);

      resolver = new FileSystemResourceResolver(baseMap);
      working = new File(outputDir);

      try {
         outputDocument("transform/scrubber/siteTypeRoles.xsl", "site-role-scrubbed.xml", "sites and roles.xml");
         outputDocument("transform/scrubber/tools.xsl", "tools-scrubbed.xml", "tools.xml");
         outputDocument("transform/scrubber/toolCategories.xsl", "myws-scrubbed.xml", "myws.xml");
         outputDocument("transform/scrubber/perms.xsl", "perms-scrubbed.xml", "s_permissions.xml");

         processSiteTypes();
         processSites();
         outputDocument("transform/group-perms.xsl", "perms-scrubbed.xml", "perms-scrubbed.xml");
      } catch (FileNotFoundException e) {
         logger.error("", e);
      } catch (TransformerException e) {
         logger.error("", e);
      } catch (IOException e) {
         logger.error("", e);
      }
   }

   protected void processSiteTypes() throws IOException, FileNotFoundException, TransformerException {
      File inputFilePath = new File(working, "site-role-scrubbed.xml");
      Document siteTypes = Xml.readDocument(inputFilePath.getAbsolutePath());

      Element siteTypesElement = (Element) siteTypes.getDocumentElement().getElementsByTagName("siteTypes").item(0);
      NodeList siteTypesList = siteTypesElement.getElementsByTagName("siteType");

      for (int i=0;i<siteTypesList.getLength();i++) {
         Element siteType = (Element) siteTypesList.item(i);

         String shortTypeId = siteType.getAttribute("shortTypeId");
         if (shortTypeId != null) {
            File siteTypeFilePath = new File(working, "ct_" + shortTypeId + ".xml");
            if (siteTypeFilePath.exists()) {
               outputDocument("transform/scrubber/toolCategories.xsl",
                  shortTypeId + "-scrubbed.xml", "ct_" + shortTypeId + ".xml");
            }
            else {
               logger.warn("worksheet not found for site type: " + shortTypeId);
            }
         }
      }
   }

   protected void processSites() throws IOException, TransformerException {
      File inputFilePath = new File(working, "site-role-scrubbed.xml");
      Document siteTypes = Xml.readDocument(inputFilePath.getAbsolutePath());

      Element sitesElement = (Element) siteTypes.getDocumentElement().getElementsByTagName("sites").item(0);
      NodeList sitesList = sitesElement.getElementsByTagName("site");

      for (int i=0;i<sitesList.getLength();i++) {
         Element site = (Element) sitesList.item(i);

         String typeId = site.getAttribute("typeId");
         if (typeId != null) {
            File siteFilePath = new File(working, "ct_" + typeId + ".xml");
            if (siteFilePath.exists()) {
               outputDocument("transform/scrubber/toolCategories.xsl",
                  typeId + "-scrubbed.xml", "ct_" + typeId + ".xml");
            }
            else {
               logger.warn("worksheet not found for site: " + siteFilePath);
            }
         }
      }
   }

   protected void outputDocument(String transformer, String outputFile, String inputFile)
         throws IOException, TransformerException {
      File inputFilePath = new File(working, inputFile);
      Node sourceNode;
      if (inputFilePath.exists()) {
         sourceNode = Xml.readDocument(inputFilePath.getAbsolutePath());
      }
      else {
         sourceNode = Xml.readDocumentFromStream(getClass().getResourceAsStream("/" + inputFile));
      }
      logger.info("processing file: " + inputFile);
      outputDocument(transformer, outputFile, sourceNode);
   }

   protected void copyFile(String inputFile, File inputFilePath) throws IOException {
      InputStream source = getClass().getResourceAsStream("/" + inputFile);

      inputFilePath.getParentFile().mkdirs();

      FileOutputStream fos = new FileOutputStream(inputFilePath);
      
      int c = 0;
      while (c != -1) {
         c = source.read();
         fos.write(c);
      }
      fos.close();
      source.close();
   }

   protected void outputDocument(String transformer, String outputFile, Node inputNode)
         throws IOException, TransformerException {
      File outputFileFile = new File(working, outputFile);
      if (!outputFileFile.getParentFile().exists()) {
         outputFileFile.getParentFile().mkdirs();
      }
      PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFileFile, false), "UTF-8"));
      StreamResult outputTarget = new StreamResult(out);
      getTransformer(transformer).transform(
         new DOMSource(inputNode), outputTarget);
      out.close();
   }


   protected Transformer getTransformer(String transformPath)
      throws MalformedURLException, TransformerConfigurationException {

      Transformer trans = createTemplate(transformPath).newTransformer();
      trans.setURIResolver(resolver);
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      trans.setErrorListener(new LogErrorListener(logger));
      trans.setParameter("rsmartRuntime", runtime?"true":"false");
      return trans;
   }


   protected Templates createTemplate(String transformPath)
      throws MalformedURLException, TransformerConfigurationException {

      InputStream stream = getClass().getClassLoader().getResourceAsStream(
            transformPath);
      URL url = getClass().getClassLoader().getResource(transformPath);
      String urlPath = url.toString();
      String systemId = urlPath.substring(0, urlPath.lastIndexOf('/') + 1);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Templates templates = transformerFactory.newTemplates(
         new StreamSource(stream, systemId));
      return templates;
   }

   public URIResolver getResolver() {
      return resolver;
   }

   public void setResolver(URIResolver resolver) {
      this.resolver = resolver;
   }

   public File getWorking() {
      return working;
   }

   public void setWorking(File working) {
      this.working = working;
   }

   protected void tranformFile(String transformer, String sourceFile, String sourceDir)
         throws IOException, TransformerException {
      outputDocument(transformer, sourceFile + ".tmp", sourceFile);
      File oldFile = new File(sourceDir, sourceFile);
      File newFile = new File(sourceDir, sourceFile + ".tmp");
      oldFile.delete();

      logger.debug("renaming " + newFile.getPath() + " to " + oldFile.getPath());

      if (!newFile.renameTo(oldFile)) {
         throw new RuntimeException("rename failed: " + oldFile);
      }

   }


   protected String getElementText(Element element) {
      NodeList list = element.getChildNodes();

      for (int i=0;i<list.getLength();i++){
         Node node = list.item(i);
         if (node.getNodeType() == Node.TEXT_NODE) {
            return node.getNodeValue();
         }
      }

      return null;
   }
   
   public void setLogListener(LogListener listener) {
      logger.setListener(listener);   
   }
}
