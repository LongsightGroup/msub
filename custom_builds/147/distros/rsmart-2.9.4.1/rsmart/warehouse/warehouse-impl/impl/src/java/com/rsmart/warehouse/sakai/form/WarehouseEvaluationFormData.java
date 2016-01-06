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
**********************************************************************************/
package com.rsmart.warehouse.sakai.form;

import java.io.Serializable;

import org.sakaiproject.metaobj.shared.model.Id;





/**
 * This class is used to warehouse evaluation form items (instances of of the evaluation form).
 */
public class WarehouseEvaluationFormData implements Serializable {
   // data members
   private Id     formType;        // the form type (each form has a unique id called its form type)
   private Id     formId;          // the id of the form item (the particular instance of the form)
   private Id     submittedBy;     // sakai user id of the person who submitted the form for evaluation
   private Id     evaluatedBy;     // sakai user id of the person who evaluated the submitted form
   private String matrixCriteria;  // the matrix criteria (row) for which this form was submitted
   private String matrixLevel;     // the matrix level    (col) for which this form was submitted
   private String evaluation;      // the evaluation level
   private String comments;        // any comments the evaluator made.




   /**
    * default constructor.
    */
   public WarehouseEvaluationFormData() {
      // no code necessary
   }

   /**
    * constructor.
    */
   public WarehouseEvaluationFormData(Id formType, Id formId, Id submittedBy, Id evaluatedBy, String matrixCriteria, String matrixLevel, String evaluation, String comments) {
      this.formType       = formType;
      this.formId         = formId;
      this.submittedBy    = submittedBy;
      this.evaluatedBy    = evaluatedBy;
      this.matrixCriteria = matrixCriteria;
      this.matrixLevel    = matrixLevel;
      this.evaluation     = evaluation;
      this.comments       = comments;
   }

   public Id getFormType() {
       return formType;
   }

   public void setFormType(Id formType) {
       this.formType = formType;
   }

   public Id getFormId() {
       return formId;
   }

   public void setFormId(Id formId) {
       this.formId = formId;
   }

   public Id getSubmittedBy() {
       return submittedBy;
   }

   public void setSubmittedBy(Id submittedBy) {
      this.submittedBy = submittedBy;
   }

   public Id getEvaluatedBy() {
       return evaluatedBy;
   }

   public void setEvaluatedBy(Id evaluatedBy) {
      this.evaluatedBy = evaluatedBy;
   }

   public String getEvaluation() {
      return evaluation;
   }

   public String getMatrixCriteria() {
      return matrixCriteria;
   }

   public void setMatrixCriteria(String matrixCriteria) {
      this.matrixCriteria = matrixCriteria;
   }

   public String getMatrixLevel() {
      return matrixLevel;
   }

   public void setMatrixLevel(String matrixLevel) {
      this.matrixLevel = matrixLevel;
   }

   public void setEvaluation(String evaluation) {
      this.evaluation = evaluation;
   }

   public String getComments() {
      return comments;
   }

   public void setComments(String comments) {
      this.comments = comments;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("form type......: " + formType       + "/n");
      buffer.append("form id........: " + formId         + "/n");
      buffer.append("submitted by...: " + submittedBy    + "/n");
      buffer.append("evaluated by...: " + evaluatedBy    + "/n");
      buffer.append("matrix criteria: " + matrixCriteria + "/n");
      buffer.append("matrix level...: " + matrixLevel    + "/n");
      buffer.append("evaluation.....: " + evaluation     + "/n");
      buffer.append("comments.......: " + comments             );

      return buffer.toString();
   }
}
