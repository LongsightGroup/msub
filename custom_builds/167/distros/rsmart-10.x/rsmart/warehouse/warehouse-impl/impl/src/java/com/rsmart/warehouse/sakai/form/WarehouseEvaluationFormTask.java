/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
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
package com.rsmart.warehouse.sakai.form;

import org.jdom.Document;

import org.sakaiproject.content.api.ContentResource;

import org.sakaiproject.metaobj.shared.model.Artifact;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;



/**
 * This data warehouse task returns a list of forms found in the content resources.
 */
public class WarehouseEvaluationFormTask extends BaseChildWarehouseFormTask {

   // logger
   protected final Log logger = LogFactory.getLog(getClass());

   /**
    * this method takes the evaluation form instance and retrieves the data that is to be warehoused from it.
    * <br/><br/>
    * @param formType   the type of form the form instance is an instance of
    * @param formId     the form instance's unique id
    * @param resource   the form instance as a  resource
    * @param artifact   the form instance as an artifact
    * @param document   the form instance as a  jdom document
    * <br/><br/>
    * @return Object   returns a bean which contains the form instance's data which will be warehoused to the database..
    */
   public Object retrieveFormData(Id formType, Id formId, ContentResource resource, Artifact artifact, Document document) {

      String displayName    = artifact.getDisplayName();
      String matrixCriteria = parseMatrixCriteria(displayName);
      String matrixLevel    = parseMatrixLevel   (displayName);
      Id     submittedBy    = getIdManager().getId(parseSubmitter(displayName));
      Id     evaluatedBy    = artifact.getOwner().getId();
      String evaluation     = document.getRootElement().getChild("structuredData").getChild("evaluation").getChild("evaluationLevel").getText();
      
      String comments = "";
      
      // Comment not required
      if( document.getRootElement().getChild("structuredData").getChild("evaluation").getChild("comments") != null ) {
    	  comments = document.getRootElement().getChild("structuredData").getChild("evaluation").getChild("comments").getText();
      }
      
      return new WarehouseEvaluationFormData(formType, formId, submittedBy, evaluatedBy, matrixCriteria, matrixLevel, evaluation, comments);
   }

   /**
    * parse the matrix name from the display name.
    * The display name is of the form: matrix name-criteria-level-submitter-form name.
    * ex: General Education-Written Communication - Level 1-asok-Evaluation
    * <br/><br/>
    * @param displayName  the full display name of the form instance found in a matrix cell
    * <br/><br/>
    * @return String the name of the matrix
    */
   public static String parseMatrixName(String displayName) {
      String[] parts = displayName.split("-");

      return (parts != null && parts.length >= 5 ? parts[0].trim() : null);
   }

   public static String parseMatrixCriteria(String displayName) {
      String[] parts = displayName.split("-");

      return (parts != null && parts.length >= 5 ? parts[1].trim() : null);
   }

   public static String parseMatrixLevel(String displayName) {
      String[] parts = displayName.split("-");

      return (parts != null && parts.length >= 5 ? parts[2].trim() : null);
   }

   /**
    * parse the submitter's eid from the display name.
    * returns the submitter's internal id.
    * <br/><br/>
    * @param displayName  the full display name of the form instance found in a matrix cell
    * <br/><br/>
    * @return String the name of the matrix    */
   public String parseSubmitter(String displayName) {
      String[] parts   = displayName.split("-");
      String   userEid = (parts != null && parts.length >= 5 ? parts[3].trim() : null);
      User user    = null;

      if (userEid != null) {
         try {
            user = getUserDirectoryService().getUserByEid(userEid);
         } catch (UserNotDefinedException ex) {
            // nothing to do - just set the user to null
         }
      }
      return (user == null ? null : user.getId());
   }

   public static String parseFormName(String displayName) {
      String[] parts = displayName.split("-");

      return (parts != null && parts.length >= 5 ? parts[4].trim() : null);
   }
}
