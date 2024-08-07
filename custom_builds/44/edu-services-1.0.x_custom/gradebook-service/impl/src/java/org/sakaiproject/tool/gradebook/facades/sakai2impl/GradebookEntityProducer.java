/**********************************************************************************
*
* $Id: GradebookEntityProducer.java 75077 2010-03-23 18:01:38Z arwhyte@umich.edu $
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Implements the Sakai EntityProducer approach to integration of tool-specific
 * storage with site management.
 */
public class GradebookEntityProducer extends BaseEntityProducer implements ContextObserver, EntityTransferrer, HandlesImportable {
    private static final Log log = LogFactory.getLog(GradebookEntityProducer.class);

    private String[] toolIdArray;
    private GradebookFrameworkService gradebookFrameworkService;
    private GradebookService gradebookService;

	public void setToolIds(List toolIds) {
		if (log.isDebugEnabled()) log.debug("setToolIds(" + toolIds + ")");
		if (toolIds != null) {
			toolIdArray = (String[])toolIds.toArray(new String[0]);
		}
	}

	public String[] myToolIds() {
		return toolIdArray;
	}

	public void contextCreated(String context, boolean toolPlacement) {
		// Only create Gradebook storage if the Gradebook tool is actually
		// part of the new site.
		if (toolPlacement && !gradebookFrameworkService.isGradebookDefined(context)) {
			if (log.isDebugEnabled()) log.debug("Gradebook being added to context " + context);
			gradebookFrameworkService.addGradebook(context, context);
		}
	}

	public void contextUpdated(String context, boolean toolPlacement) {
		if (toolPlacement) {
			if (!gradebookFrameworkService.isGradebookDefined(context)) {
				if (log.isDebugEnabled()) log.debug("Gradebook being added to context " + context);
				gradebookFrameworkService.addGradebook(context, context);
			}
		} else {
			if (gradebookFrameworkService.isGradebookDefined(context)) {
				// We've been directed to leave Gradebook data in place when
				// the tool is removed from a site, just in case the site
				// owner changes their mind later.
				if (log.isDebugEnabled()) log.debug("Gradebook removed from context " + context + " but associated data will remain until context deletion");
			}
		}
	}

	public void contextDeleted(String context, boolean toolPlacement) {
		if (gradebookFrameworkService.isGradebookDefined(context)) {
			if (log.isDebugEnabled()) log.debug("Gradebook being deleted from context " + context);
			try {
				gradebookFrameworkService.deleteGradebook(context);
			} catch (GradebookNotFoundException e) {
				if (log.isWarnEnabled()) log.warn(e);
			}
		}
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids) {
		String fromGradebookXml = gradebookService.getGradebookDefinitionXml(fromContext);
		gradebookService.mergeGradebookDefinitionXml(toContext, fromGradebookXml);
	}
	
	public void transferCopyEntitiesWithSettings(String fromContext, String toContext, List ids) {
		String fromGradebookXml = gradebookService.getGradebookDefinitionXml(fromContext);
		gradebookService.transferGradebookDefinitionXml(fromContext, toContext, fromGradebookXml);
	}

	public void setGradebookFrameworkService(GradebookFrameworkService gradebookFrameworkService) {
		this.gradebookFrameworkService = gradebookFrameworkService;
	}

	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}
		
	////////////////////////////////////////////////////////////////
	// TODO Speculative support for future migration / import / export starts here.

	public static final String GRADEBOOK_DEFINITION_TYPE = "sakai-gradebook";

	public boolean canHandleType(String typeName) {
		return (typeName.equals(GRADEBOOK_DEFINITION_TYPE));
	}

	public void handle(Importable importable, String siteId) {
		if (importable.getTypeName().equals(GRADEBOOK_DEFINITION_TYPE)) {
			gradebookService.mergeGradebookDefinitionXml(siteId, ((XmlImportable)importable).getXmlData());
		}
	}
	
	public List<Importable> getAllImportables(String contextId) {
		List<Importable> importables = new ArrayList<Importable>();
		importables.add(new XmlImportable(GRADEBOOK_DEFINITION_TYPE, gradebookService.getGradebookDefinitionXml(contextId)));
		return importables;
	}
	
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup) {
		
			if(cleanup == true) {				
				// remove the gradebook assignments
				String toSiteId = toContext;
				List gbItems = gradebookService.getAssignments(toSiteId);
				
				if (gbItems != null && !gbItems.isEmpty()) 
				{
					for (Iterator iter = gbItems.iterator(); iter.hasNext();)
					{
						Assignment assign = (Assignment) iter.next(); 				
						gradebookService.removeAssignment(assign.getId());
					}
				}
				// remove the gradebook categories
				Gradebook gradebook =(Gradebook) gradebookService.getGradebook(toSiteId);				
				Long gradebookId = gradebook.getId();				
				List categoryList = gradebookService.getCategories(gradebookId);
				
				if (categoryList != null) 
				{
					for (Iterator iter = categoryList.iterator(); iter.hasNext();)
					{
						Category category = (Category) iter.next(); 
						gradebookService.removeCategory(category.getId());
					}
				}
				 
				transferCopyEntitiesWithSettings(fromContext, toContext, ids);
				
			}
			else {
				transferCopyEntities(fromContext, toContext, ids);
			}
	
	}
}
