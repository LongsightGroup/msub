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
 * This class is used to warehouse the four levels offered by the evaluation form.
 */
public class WarehouseEvaluationFormLevel implements Serializable {
   // data members
   private Id     scaffoldingId;   // id of the matrix scaffolding
   private Id     formType;        // the form type (each form has a unique id called its form type)
   private String evalLevel;       // the evaluation level



   /**
    * default constructor.
    */
   WarehouseEvaluationFormLevel() {
      // no code necessary
   }

   /**
    * constructor.
    * <br/>
    * @param scaffoldingId  id of the matrix scaffolding
    * @param formType       the form type (each form has a unique id called its form type)
    * @param evalLevel          evaluation level
    */
   WarehouseEvaluationFormLevel(Id scaffoldingId, Id formType, String evalLevel) {
      if (scaffoldingId == null)
         throw new IllegalArgumentException("The scaffolding id can not be null.");
      if (formType == null)
         throw new IllegalArgumentException("The form type can not be null.");
      if (evalLevel == null)
         throw new IllegalArgumentException("The evaluation level can not be null.");

      this.scaffoldingId = scaffoldingId;
      this.formType      = formType;
      this.evalLevel     = evalLevel;
   }

   public Id getScaffoldingId() {
      return scaffoldingId;
   }

   public void setScaffoldingId(Id scaffoldingId) {
      if (scaffoldingId == null)
         throw new IllegalArgumentException("The scaffolding id can not be null.");

      this.scaffoldingId = scaffoldingId;
   }

   public Id getFormType() {
      return formType;
   }

   public void setFormType(Id formType) {
      if (formType == null)
         throw new IllegalArgumentException("The form type can not be null.");

      this.formType = formType;
   }

   public String getEvalLevel() {
      return evalLevel;
   }

   public void setEvalLevel(String evalLevel) {
      if (evalLevel == null)
         throw new IllegalArgumentException("The evaluation level can not be null.");

      this.evalLevel = evalLevel;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("scaffoldind id: " + scaffoldingId.toString() + "/n");
      buffer.append("form type.....: " + formType.toString()      + "/n");
      buffer.append("level.........: " + evalLevel                      );

      return buffer.toString();
   }
}
