/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/tags/1.0/api/src/java/org/sakaiproject/assignment2/logic/entity/XmlImportable.java $
 * $Id: XmlImportable.java 61480 2009-06-29 18:39:09Z swgithen@mtu.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

package org.sakaiproject.assignment2.logic.entity;

import org.sakaiproject.importer.api.Importable;

/**
 * A simple generic wrapper to add new types of importable objects without needing
 * to subclass each of them.
 * 
 * THIS IS CURRENTLY UNUSED. It's here on a speculative basis for upcoming 
 * import/archive/merge development.
 * <p>
 * TODO This would subclass AbstractImportable if AbstractImportable was bumped up to
 * the importer.api package for looser coupling. Currently, the AbstractImportable JAR
 * brings in unrelated dependencies such as the QTI class.
 * 
 * This work is largely from UC Berkeley's implementation in the gradebook
 */
public class XmlImportable implements Importable {
    private String typeName;
    private String xmlData;
    private String guid;
    private String legacyGroup;
    private String contextPath;
    private Importable parent;

    public XmlImportable() {
    }

    /**
     * Create an importable object in one line.
     * 
     * @param typeName identifies what type of domain data is serialized in the XML
     * @param xmlData XML string describing the data itself, suitable for archiving
     * or merging
     */
    public XmlImportable(String typeName, String xmlData) {
        this.typeName = typeName;
        this.xmlData = xmlData;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getXmlData() {
        return xmlData;
    }

    /**
     * Since this class doesn't parse the data, this property could easily be renamed
     * "setDataDescription" and this class renamed "StringImportable".
     * But since XML is central to our archive / export / merge plans, it seems
     * worth advertising that the data can be parsed as such.
     * @param xmlData
     */
    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    public Importable getParent() {
        return parent;
    }
    public void setParent(Importable parent) {
        this.parent = parent;
    }
    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    public String getLegacyGroup() {
        return legacyGroup;
    }
    public void setLegacyGroup(String legacyGroup) {
        this.legacyGroup = legacyGroup;
    }
    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
