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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.quartz.JobExecutionException;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.metaobj.shared.ArtifactFinder;
import org.sakaiproject.metaobj.shared.ArtifactFinderManager;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.mgt.PresentableObjectHome;
import org.sakaiproject.metaobj.shared.mgt.ReadableObjectHome;
import org.sakaiproject.metaobj.shared.model.Artifact;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;
import org.sakaiproject.user.api.UserDirectoryService;
import org.theospi.portfolio.matrix.MatrixManager;

import org.sakaiproject.warehouse.service.ItemIndexInParentPropertyAccess;
import org.sakaiproject.warehouse.service.ParentPropertyAccess;
import org.sakaiproject.warehouse.service.PropertyAccess;
import org.sakaiproject.warehouse.service.ReportableChildTask;
import org.sakaiproject.warehouse.impl.ChildFieldWrapper;




/**
 * this implementation
 */
public abstract class BaseChildWarehouseFormTask implements ChildWarehouseFormTask {
   // data members injected by the spring framework via IoC.
   private List<Object>            fields;           // list of fields in the model object and how to retrieve their values for the sql insert statement
   private List<ChildFieldWrapper> complexFields;    // list of complex fields (those that contain sub fields)
   private String                  insertStmt;       // sql statement which inserts a row into the database table
   private String                  clearStmt;        // sql statement which deletes all the rows from the database table
   private int                     batchSize;        // how many form items to write out to the data warehouse database at one time.
   private boolean                 isPrepared;       // whether the sql insert statement has been prepared by the jdbc connection

   // sakai services injected by spring
   private ArtifactFinderManager               artifactFinderManager;
   private ContentHostingService               contentHostingService;
   private IdManager                           idManager;
   private MatrixManager                       matrixManager;
   private UserDirectoryService                userDirectoryService;
   private String tableName;


   /**
    * default constructor.
    */
   public BaseChildWarehouseFormTask() {
      batchSize   = 100;
      isPrepared = false;
   }

   /**
    * this method performs invokes <i>getItems</i>() method which performs business logic to collect the form items which are to be warehoused.
    * <br/><br/>
    * @param parent      parent property access
    * @param sads        a collection of structured artifact definition beans which contain the definition of the form whose instances are to be warehoused.
    * @param connection  jdbc connection used to save the form data to the database.
    * <br/><br/>
    * @throws JobExecutionException   if an error occurs while warehousing the data.
    */
   public void execute(Object parent, Collection<StructuredArtifactDefinitionBean> sads, Connection connection) throws JobExecutionException {
      PreparedStatement preparedStatement = null;

      try {
         isPrepared = false;
         preparedStatement = connection.prepareStatement(getInsertStmt());

         int current = 0;
         // for each form definition
         for (StructuredArtifactDefinitionBean sad : sads) {
            // for each instance of the form
            for (Object item : getItems(sad)) {
               processItem(parent, item, preparedStatement, current);
               preparedStatement.addBatch();
               current++;
               if (current > batchSize) {
                  current = 0;
                  preparedStatement.executeBatch();
               }
               preparedStatement.clearParameters();
            }
            if (current > 0) {
               preparedStatement.executeBatch();
            }
         }
      } catch (SQLException e) {
         throw new JobExecutionException(new Exception("sql insert statement: " + insertStmt, e));
      } catch (NullPointerException e) {
         throw new JobExecutionException(new Exception(getClass().getName() + ".execute() - items parameter is null. sql insert statement: " + insertStmt, e));
      } finally {
         try {
            preparedStatement.close();
         } catch (Exception e) {
            // nothing to do here.
         }
      }
   }

   /**
    * This method is run before execute.  It ensures that the prepare functionality is only executed once.
    * All rows in the database are deleted.
    * <br/><br/>
    * @param connection   jdbc connection
    */
   public void prepare(Connection connection) {
      if (isPrepared)
         return;

      try {
         connection.createStatement().execute(getClearStmt());
         isPrepared = true;

         if (complexFields != null) {
            for (ChildFieldWrapper complexField : complexFields) {
               complexField.getTask().prepare(connection);
            }
         }
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * save the form item to the database.
    * <br/><br/>
    * @param parent             parent property access
    * @param item               form item to br written to the database
    * @param preparedStatement  jdbc prepared insert statement
    * @param itemIndex          index
    * <br/><br/>
    * @throws JobExecutionException   if an error occurs while writing the form item to the database.
    */
   protected void processItem(Object parent, Object item, PreparedStatement preparedStatement, int itemIndex) throws JobExecutionException {

      try {
         int index = 1;
         for (Object field : fields) {
            if (field instanceof PropertyAccess) {
               PropertyAccess pa = (PropertyAccess)field;
               preparedStatement.setObject(index, pa.getPropertyValue(item));
            } else if (field instanceof ParentPropertyAccess) {
               ParentPropertyAccess pa = (ParentPropertyAccess)field;
               preparedStatement.setObject(index, pa.getPropertyValue(parent, item));
            } else if (field instanceof ItemIndexInParentPropertyAccess){
               preparedStatement.setInt(index, itemIndex);
            }
            index++;
         }

         // now, lets look for complex fields
         if (complexFields != null) {
            for (ChildFieldWrapper complexField : complexFields) {
               Object              property = complexField.getPropertyAccess().getPropertyValue(item);
               Collection <Object> items    = null;

               // if the complex field isn't a Collection then build a collection out of the single class instance in the complex field
               if(property instanceof Collection) {
                  items = (Collection<Object>)property;
               } else {
                  items = new ArrayList<Object>();
                  if(property != null)
                     items.add(property);
               }

               // item becomes the new parent, items is the complex field (Collection)
               complexField.getTask().execute(item, items, preparedStatement.getConnection());
            }
         }
      } catch (Exception e) {
         throw new JobExecutionException("error trying to prepare '" + insertStmt + "'", e, false);
      }
   }

   /**
    * performs the business logic of retrieving the instances of the form specified in the structured artifact definition bean.
    * returns a collection of form items which will be stored in the database table.
    * <br/><br/>
    * @param sad   structured artifact definition bean which contains the definition of the form whose instances are to be warehoused.
    * <br/><br/>
    * @return      a collection of form items which will be stored in the database table.
    */
   protected Collection<Object> getItems(StructuredArtifactDefinitionBean sad) {

      ArrayList<Object> formData = new ArrayList<Object>();
      Id                formType = sad.getId();

      // warehouse all the instancese of the form
      if (formType != null) {
         List<ContentResource> resources = (List<ContentResource>)contentHostingService.getAllResources("/");

         for(ContentResource resource : resources) {
            ResourceProperties properties        = resource.getProperties();
            String             structureProperty = properties.getNamePropStructObjType();
            String             structureValue    = properties.getProperty(structureProperty);

            if (structureValue != null && structureValue.equals(formType.toString())) {
            /* for(Iterator<String> i=properties.getPropertyNames(); i.hasNext(); ) {
                  String property = i.next();
                  Object value    = properties.get(property);
                  logger.info(property + " : " + value);
               } */

               ArtifactFinder     artifactFinder = artifactFinderManager.getArtifactFinderByType(formType.toString());
               String             resourceId     = resource.getId();
               String             formIdS        = contentHostingService.getUuid(resourceId);
               Id                 formId         = idManager.getId(formIdS);
               Artifact           artifact       = artifactFinder.load(formId);
               ReadableObjectHome home           = artifact.getHome();

               if (home instanceof PresentableObjectHome) {
                   Element  root      = ((PresentableObjectHome)home).getArtifactAsXml(artifact);
                   Document document  = new Document(root);
                   Object   formDatum = retrieveFormData(formType, formId, resource, artifact, document);
                   formData.add(formDatum);
               }
             }
         }
      }
      return formData;
   }

   /**
    * this method takes the form instance and retrieves the specific data that is to be warehoused from it.
    * <br/><br/>
    * @param formType   the type of form the form instance is an instance of
    * @param formId     the form instance's unique id
    * @param resource   the form instance as a  resource
    * @param artifact   the form instance as an artifact
    * @param document   the form instance as a  jdom document
    * <br/><br/>
    * @return Object   returns a bean which contains the form instance's data which will be warehoused to the database..
    */
   public abstract Object retrieveFormData(Id formType, Id formId, ContentResource resource, Artifact artifact, Document document);

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

   /**
    * returns the instance of the ArtifactFinderManager injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the ArtifactFinderManager injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public ArtifactFinderManager getArtifactFinderManager() {
      return artifactFinderManager;
   }

   /**
    * called by the spring framework to initialize the ArtifactFinderManager data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param artifactFinderManager   the implementation of the ArtifactFinderManager interface provided by the spring framework.
    */
   public void setArtifactFinderManager(ArtifactFinderManager artifactFinderManager) {
      this.artifactFinderManager = artifactFinderManager;
   }

   /**
    * returns the instance of the ContentHostingService injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the ContentHostingService injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public ContentHostingService getContentHostingService() {
      return contentHostingService;
   }

   /**
    * called by the spring framework to initialize the ContentHostingService data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param contentHostingService   the implementation of the ContentHostingService interface provided by the spring framework.
    */
   public void setContentHostingService(ContentHostingService contentHostingService) {
      this.contentHostingService = contentHostingService;
   }

   /**
    * returns the instance of the IdManager injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the IdManager injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public IdManager getIdManager() {
      return idManager;
   }

   /**
    * called by the spring framework to initialize the IdManager data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param idManager   the implementation of the IdManager interface provided by the spring framework.
    */
   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
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
    * returns the instance of the UserDirectoryService injected by the spring framework specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @return the instance of the UserDirectoryService injected by the spring framework specified in the resource-components.xml file via IoC.
    */
   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   /**
    * called by the spring framework to initialize the UserDirectoryService data member specified in the resource-components.xml file via IoC.
    * <br/><br/>
    * @param userDirectoryService   the implementation of the UserDirectoryService interface provided by the spring framework.
    */
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   public List<Object> getFields() {
      return fields;
   }

   public void setFields(List<Object> fields) {
      this.fields = fields;
   }

   public List<ChildFieldWrapper> getComplexFields() {
      return complexFields;
   }

   public void setComplexFields(List<ChildFieldWrapper> complexFields) {
      this.complexFields = complexFields;
   }

   public String getInsertStmt() {
      return insertStmt;
   }

   public void setInsertStmt(String insertStmt) {
      this.insertStmt = insertStmt;
   }

   public int getBatchSize() {
      return batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public String getClearStmt() {
      return clearStmt;
   }

   public void setClearStmt(String clearStmt) {
      this.clearStmt = clearStmt;
   }

   public List<ReportableChildTask> getChildren() {
      return new ArrayList();
   }

   public String parseClearStatement() {
      String [] strings = this.clearStmt.split(" ");
      
      if (strings.length > 0) {
         return strings[strings.length-1];
      }
      return "unknown";
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   public String getTableName() {
      if (tableName == null) {
         return parseClearStatement();
      }
      return tableName;
   }
}
