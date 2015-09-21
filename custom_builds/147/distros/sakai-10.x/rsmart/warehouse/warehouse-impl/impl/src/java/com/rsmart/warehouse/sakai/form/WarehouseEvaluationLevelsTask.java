/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005, 2006, 2007 The Sakai Foundation.
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
**********************************************************************************/
package com.rsmart.warehouse.sakai.form;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.theospi.portfolio.matrix.MatrixManager;
import org.theospi.portfolio.matrix.model.Scaffolding;
import org.theospi.portfolio.matrix.model.ScaffoldingCell;
import org.theospi.portfolio.matrix.model.WizardPageDefinition;
import org.sakaiproject.warehouse.impl.BaseWarehouseTask;

import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactDefinitionManager;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;



/**
 * This data warehouse task returns a list of forms found in the content resources.
 */
public class WarehouseEvaluationLevelsTask extends BaseWarehouseTask {
   // logger
   private final transient Log logger = LogFactory.getLog(getClass());

   // sakai services injected by spring
   private MatrixManager                       matrixManager;
   private StructuredArtifactDefinitionManager structuredArtifactDefinitionManager;




   /**
    * implements the abstract method declared in BaseWarehouseTask.
    * returns the list of evaluation levels supported by the evaluation form.
    */
   protected Collection<WarehouseEvaluationFormLevel> getItems() {
      Collection<WarehouseEvaluationFormLevel> evaluationLevels = new ArrayList<WarehouseEvaluationFormLevel>();
      List<Scaffolding> list = matrixManager.getScaffoldingForWarehousing();
      // find the General Education Scaffoldings
      for(Scaffolding scaffolding : list) {
         if (scaffolding.getTitle().equals("General Education")) {
            // we are assuming that the scaffolding has the same evaluation form for each cell.
            // therefore, we need only look at one scaffolding cell, say the first one, to retrieve the evaluation form
            Set<ScaffoldingCell> scaffoldingCells     = scaffolding.getScaffoldingCells();
            ScaffoldingCell      scaffoldingCell      = scaffoldingCells.iterator().next();
            WizardPageDefinition wizardPageDefinition = scaffoldingCell.getWizardPageDefinition();
            Id                   formType             = wizardPageDefinition.getEvaluationDevice();

            // get the evaluation form from the content resource
            StructuredArtifactDefinitionBean structuredArtifactDefinitionBean = structuredArtifactDefinitionManager.loadHome(formType);

            // parse the form's schema in order to determine the available values for the evaluation
            Document                        document  = null;
            try {
               SAXBuilder                   builder   = new SAXBuilder();
                                            document  = builder.build(new ByteArrayInputStream(structuredArtifactDefinitionBean.getSchema()));
               Namespace                    namespace = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema");
               Element                      root      = document.getRootElement();
               Element                      node      = root.getChild("element", namespace).getChild("complexType", namespace).getChild("sequence", namespace).getChild("element", namespace)
                                                            .getChild("simpleType", namespace).getChild("restriction", namespace);
               Collection<Element>          children = node.getChildren("enumeration", namespace);

               for (Element n : children) {
                  String                       level           = n.getAttribute("value").getValue();
                  WarehouseEvaluationFormLevel evaluationLevel = new WarehouseEvaluationFormLevel(scaffolding.getId(), formType, level);
                  evaluationLevels.add(evaluationLevel);
                  logger.debug("evaluation level: " + evaluationLevel);
               }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
         }
      }
      return evaluationLevels;
   }

   /**
    * returns the instance of the MatrixManager injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the MatrixManager injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public MatrixManager getMatrixManager() {
      return matrixManager;
   }

   /**
    * called by the spring framework to initialize the MatrixManager data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param matrixManager   the implementation of the MatrixManager interface provided by the spring framework.
    */
   public void setMatrixManager(MatrixManager matrixManager) {
      this.matrixManager = matrixManager;
   }

   /**
    * returns the instance of the StructuredArtifactDefinitionManager injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the StructuredArtifactDefinitionManager injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public StructuredArtifactDefinitionManager getStructuredArtifactDefinitionManager() {
      return structuredArtifactDefinitionManager;
   }

   /**
    * called by the spring framework to initialize the StructuredArtifactDefinitionManager data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param structuredArtifactDefinitionManager   the implementation of the StructuredArtifactDefinitionManager interface provided by the spring framework.
    */
   public void setStructuredArtifactDefinitionManager(StructuredArtifactDefinitionManager structuredArtifactDefinitionManager) {
      this.structuredArtifactDefinitionManager = structuredArtifactDefinitionManager;
   }

   /**
    * print out a jdom xml document to the console.
    * <br/>
    * @param document   jdom document to print out.
    */
   private void printXml(Document document) {
      XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
      try {
         xmlOutputter.output(document, System.out);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
}
