/**********************************************************************************
* $URL:https://source.sakaiproject.org/svn/osp/trunk/warehouse/api/src/java/org/theospi/portfolio/warehouse/intf/ChildWarehouseTask.java $
* $Id:ChildWarehouseTask.java 9134 2006-05-08 20:28:42Z chmaurer@iupui.edu $
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

import java.sql.Connection;
import java.util.Collection;

import org.jdom.Document;

import org.quartz.JobExecutionException;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.metaobj.shared.model.Artifact;
import org.sakaiproject.metaobj.shared.model.Id;
import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;
import org.sakaiproject.warehouse.service.ReportableChildTask;


/**
 * this interface specifies the methods for a child warehouse task to warehouse form data.
 * the real business logic is done in the <i>retrieveFormData</i>() method.
 */
public interface ChildWarehouseFormTask extends ReportableChildTask {

   /**
    * this method performs business logic to collect the form items which are to be warehoused.
    * <br/><br/>
    * @param parent      property access parent
    * @param sads        a collection of structured artifact definition beans which contain the definition of the form whose instances are to be warehoused.
    * @param connection  jdbc connection used to save the form data to the database.
    * <br/><br/>
    * @throws JobExecutionException   if an error occurs while warehousing the data.
    */
   public void execute(Object parent, Collection<StructuredArtifactDefinitionBean> sads, Connection connection) throws JobExecutionException;

   /**
    * prepares the jdbc insert statement.
    * <br/><br/>
    * @param connection  jdbc connection used to save the form data to the database.
    */
   public void prepare(Connection connection);

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
   public Object retrieveFormData(Id formType, Id formId, ContentResource resource, Artifact artifact, Document document);
}
