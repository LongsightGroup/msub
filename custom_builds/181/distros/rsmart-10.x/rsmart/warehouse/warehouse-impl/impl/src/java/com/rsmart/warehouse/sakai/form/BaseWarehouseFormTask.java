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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobExecutionException;

import org.sakaiproject.warehouse.util.db.DbLoader;
import org.sakaiproject.warehouse.service.DataWarehouseManager;
import org.sakaiproject.warehouse.service.WarehouseTask;
import org.sakaiproject.warehouse.service.ChildWarehouseTask;
import org.sakaiproject.warehouse.service.ReportableChildTask;

import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactDefinitionManager;
import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.BeanNameAware;


/**
 * This data warehouse task is a container of child warehouse form tasks.
 * The child warehouse tasks warehouse form instances found in the content resources.
 * Only the form instances which are instances of the form types listed in the spring configuration file are warehoused.
 */
public class BaseWarehouseFormTask implements WarehouseTask, BeanNameAware {
   // logger
   protected final Log logger = LogFactory.getLog(getClass());

   // data members injected by spring via IoC
   private DataSource                          dataSource;                  // jdbc connection
   private String                              tableDdlResource;            // name of the file which specifies the database warehouse tables to be used by the child warehouse task
   private DataWarehouseManager                dataWarehouseManager;        // controls the execution of all warehouse tasks
   private Map<String, ChildWarehouseFormTask> formTasks;                   // list of child warehouse tasks.  keys   - form definition's external xml type,
   private String beanName;
                                                                            //                                 values - corresponding child warehouse task

   // sakai services injected by spring
   private StructuredArtifactDefinitionManager structuredArtifactDefinitionManager;



   /**
    * executes all of the child warehouse tasks.
    */
   public void execute() throws JobExecutionException {
      Connection connection = null;   // jdbc connection

      try {
         // get a jdbc connection
         connection = getDataSource().getConnection();
         connection.setAutoCommit(true);

         // iterate over the form types and process only the ones specified in the spring configuration file that we want to warehouse
         for(String externalType : formTasks.keySet()) {
            // since a form definition is often imported multiple times, we need to find all occurrences of the form definition
            ArrayList<StructuredArtifactDefinitionBean> sads = new ArrayList<StructuredArtifactDefinitionBean>();
            for(StructuredArtifactDefinitionBean sad : structuredArtifactDefinitionManager.getSADForWarehousing()) {
               if (sad.getExternalType() != null && sad.getExternalType().equals(externalType))
                  sads.add(sad);
            }
            // execute the corresponding child warehouse task
            ChildWarehouseFormTask childWarehouseFormTask = formTasks.get(externalType);
            childWarehouseFormTask.prepare(connection);
            childWarehouseFormTask.execute(null, sads, connection);
         }
      } catch (SQLException ex) {
         throw new JobExecutionException(ex);
      } catch(JobExecutionException ex) {
         throw ex;
      } finally {
         close(connection);
      }
   }

   /**
    * This method loads the tables and registers the task.
    * <p>
    * This function is called after the task bean properties have been set.
    * Children are singletons where there bean init function is this method.
    * </p>
    */
   public void init() {
      logger.info("init()");

      Connection connection = null;
      try {
         if (getDataWarehouseManager().isAutoDdl()) {
            InputStream tableDdl = getTableDdl();
            if (tableDdl != null) {
               // get a jdbc connection
               connection = getDataSource().getConnection();
               connection.setAutoCommit(true);
               // create the database tables
               DbLoader loader = new DbLoader(connection, getDialect());
               loader.runLoader(tableDdl);
            }
         }
         // register this parent warehouse task with the data warehouse manager
         getDataWarehouseManager().registerTask(this);
      } catch (SQLException e) {
         throw new RuntimeException(e);
      } finally {
         close(connection);
      }
   }

   /**
    * closes the specified jdbc connection.
    * <br/><br/>
    * @param connection   jdbc connection to the data warehouse database.
    */
   private void close(Connection connection) {
      if (connection != null) {
         try {
            connection.close();
         } catch (Exception e) {
            // can't do anything with this
         }
      }
   }

   /**
    * reads in the database table file and returns it as an input stream.
    * <br/><br/>
    * @return InputStream  input stream to the xml file describing the layout of the data warehouse table.
    */
   public InputStream getTableDdl() {
      if (getTableDdlResource() != null) {
         return getClass().getResourceAsStream(getTableDdlResource());
      }
      return null;
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
    * returns the data source which has the jdbc connection.
    * <br/><br/>
    * @return DataSource   data source which holds the jdbc connection to the data warehouse database.
    */
   public DataSource getDataSource() {
      return dataSource;
   }

   /**
    * injects the data source via spring's IoC.
    * <br/><br/>
    * @param dataSource   data source which holds the jdbc connection to the data warehouse database.
    */
   public void setDataSource(DataSource dataSource) {
      this.dataSource = dataSource;
   }

   /**
    * returns the list of child warehouse form tasks.
    * <br/><br/>
    * @return List<ChildWarehouseFormTask>  list of child warehouse tasks to run
    */
   public Map<String, ChildWarehouseFormTask> getFormTasks() {
      return formTasks;
   }

   /**
    * injects the child warehouse form tasks via spring's IoC.
    * <br/><br/>
    * @param formTasks  list of child warehouse form tasks to run
    */
   public void setFormTasks(Map<String, ChildWarehouseFormTask> formTasks) {
      this.formTasks = formTasks;
   }

   /**
    * returns the name of the file which specifies the database tables to be used by the child warehouse form tasks.
    * <br/><br/>
    * @return String  path to the xml file containing the data warehouse table definition.
    */
   public String getTableDdlResource() {
      return tableDdlResource;
   }

   /**
    * injects the name of the file which specifies the database tables to be used by the child warehouse form tasks via spring's IoC
    * <br/><br/>
    * @param tableDdlResource  path to the xml file containing the data warehouse table definition.
    */
   public void setTableDdlResource(String tableDdlResource) {
      this.tableDdlResource = tableDdlResource;
   }

   /**
    * returns the data warehouse manager which controls the execution of all warehouse tasks.
    * <br/><br/>
    * @return DataWarehouseManager  the manager which oversees the running of all the data warehouse tasks.
    */
   public DataWarehouseManager getDataWarehouseManager() {
      return dataWarehouseManager;
   }

   /**
    * injects the data warehouse manager which controls the execution of all warehouse tasks.
    * <br/><br/>
    * @param dataWarehouseManager  the manager which oversees the running of all the data warehouse tasks.
    */
   public void setDataWarehouseManager(DataWarehouseManager dataWarehouseManager) {
      this.dataWarehouseManager = dataWarehouseManager;
   }

   public Dialect getDialect() {
      return getDataWarehouseManager().getDialect();
   }

   public String getBeanName() {
      return beanName;
   }

   public void setBeanName(String beanName) {
      this.beanName = beanName;
   }

   public List<ReportableChildTask> getTasks() {
      List<ReportableChildTask> returned = new ArrayList<ReportableChildTask>();
      return returned;
   }

}
