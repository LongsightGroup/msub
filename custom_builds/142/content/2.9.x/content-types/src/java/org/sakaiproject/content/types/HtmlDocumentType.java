/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/content/content-types/src/java/org/sakaiproject/content/types/HtmlDocumentType.java $
 * $Id: HtmlDocumentType.java 23942 2010-12-09 16:48:03Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.types;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;

public class HtmlDocumentType extends BaseResourceType 
{
	protected String typeId = ResourceType.TYPE_HTML;
	protected String helperId = "sakai.resource.type.helper";
	
	/** localized tool properties **/
	private static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.TypeProperties";
	private static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.type.types";
	private static final String RESOURCECLASS = "resource.class.type";
	private static final String RESOURCEBUNDLE = "resource.bundle.type";
	private String resourceClass = ServerConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
	private String resourceBundle = ServerConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
	private ResourceLoader rb = new Resource().getLoader(resourceClass, resourceBundle);
	// private static ResourceLoader rb = new ResourceLoader("types");
	
	protected EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>> actionMap = new EnumMap<ResourceToolAction.ActionType, List<ResourceToolAction>>(ResourceToolAction.ActionType.class);

	protected Map<String, ResourceToolAction> actions = new HashMap<String, ResourceToolAction>();	
	protected UserDirectoryService userDirectoryService;
	
	public class HtmlDocumentReplaceAction implements InteractionAction 
	{
		public boolean available(ContentEntity entity) 
		{
			return true;
		}

		public ActionType getActionType() 
		{
			return ResourceToolAction.ActionType.REPLACE_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.REPLACE_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.replace"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}
	}
	
	public class HtmlDocumentPropertiesAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
		 */
		public boolean isMultipleItemAction()
		{
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			// TODO Auto-generated method stub
			return ResourceToolAction.ActionType.REVISE_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
		 */
		public String getId()
		{
			// TODO Auto-generated method stub
			return ResourceToolAction.REVISE_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
		 */
		public String getLabel()
		{
			// TODO Auto-generated method stub
			return rb.getString("action.props");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
		 */
		public String getTypeId()
		{
			// TODO Auto-generated method stub
			return typeId;
		}
		
	}

	public class HtmlDocumentViewPropertiesAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#isMultipleItemAction()
		 */
		public boolean isMultipleItemAction()
		{
			// TODO Auto-generated method stub
			return false;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			// TODO Auto-generated method stub
			return ResourceToolAction.ActionType.VIEW_METADATA;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getId()
		 */
		public String getId()
		{
			// TODO Auto-generated method stub
			return ResourceToolAction.ACCESS_PROPERTIES;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getLabel()
		 */
		public String getLabel()
		{
			// TODO Auto-generated method stub
			return rb.getString("action.access");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getTypeId()
		 */
		public String getTypeId()
		{
			// TODO Auto-generated method stub
			return typeId;
		}
		
	}

	public class HtmlDocumentCopyAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
        
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.COPY;
		}

		public String getId() 
		{
			return ResourceToolAction.COPY;
		}

		public String getLabel() 
		{
			return rb.getString("action.copy");
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

	}

	public class HtmlDocumentCreateAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
        
		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.CREATE;
		}

		public String getId() 
		{
			return ResourceToolAction.CREATE;
		}

		public String getLabel() 
		{
			return rb.getString("create.html"); 
		}

		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

	}

	public class HtmlDocumentDeleteAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.DELETE;
		}

		public String getId() 
		{
			return ResourceToolAction.DELETE;
		}

		public String getLabel() 
		{
			return rb.getString("action.delete"); 
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

	}

	public class HtmlDocumentDuplicateAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.DUPLICATE;
		}

		public String getId() 
		{
			return ResourceToolAction.DUPLICATE;
		}

		public String getLabel() 
		{
			return rb.getString("action.duplicate"); 
		}

		public boolean isMultipleItemAction() 
		{
			// TODO Auto-generated method stub
			return false;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

	}

	public class HtmlDocumentMoveAction implements ServiceLevelAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.MOVE;
		}

		public String getId() 
		{
			return ResourceToolAction.MOVE;
		}

		public String getLabel() 
		{
			return rb.getString("action.move"); 
		}

		public boolean isMultipleItemAction() 
		{
			return true;
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#cancelAction(org.sakaiproject.entity.api.Reference)
		 */
		public void cancelAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#finalizeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void finalizeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ServiceLevelAction#initializeAction(org.sakaiproject.entity.api.Reference)
		 */
		public void initializeAction(Reference reference)
		{
			// TODO Auto-generated method stub
			
		}

	}

	public class HtmlDocumentReviseAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.REVISE_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.REVISE_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.revise"); 
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			List<String> rv = new ArrayList<String>();
			rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
			return rv;
		}

	}
	
	public class HtmlDocumentAccessAction implements InteractionAction
	{
		/* (non-Javadoc)
         * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
         */
        public boolean available(ContentEntity entity)
        {
	        return true;
        }

		public String initializeAction(Reference reference) 
		{
			return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
		}

		public void cancelAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		public void finalizeAction(Reference reference, String initializationId) 
		{
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ResourceToolAction#getActionType()
		 */
		public ActionType getActionType()
		{
			return ResourceToolAction.ActionType.VIEW_CONTENT;
		}

		public String getId() 
		{
			return ResourceToolAction.ACCESS_CONTENT;
		}

		public String getLabel() 
		{
			return rb.getString("action.access");
		}
		
		public String getTypeId() 
		{
			return typeId;
		}

		public String getHelperId() 
		{
			return helperId;
		}

		public List getRequiredPropertyKeys() 
		{
			return null;
		}

	}
	
	public HtmlDocumentType()
	{
		this.userDirectoryService = (UserDirectoryService) ComponentManager.get("org.sakaiproject.user.api.UserDirectoryService");
		
		actions.put(ResourceToolAction.CREATE, new HtmlDocumentCreateAction());
		//actions.put(ResourceToolAction.ACCESS_CONTENT, new HtmlDocumentAccessAction());
		actions.put(ResourceToolAction.REVISE_CONTENT, new HtmlDocumentReviseAction());
		actions.put(ResourceToolAction.REPLACE_CONTENT, new HtmlDocumentReplaceAction());
		actions.put(ResourceToolAction.ACCESS_PROPERTIES, new HtmlDocumentViewPropertiesAction());
		actions.put(ResourceToolAction.REVISE_METADATA, new HtmlDocumentPropertiesAction());
		actions.put(ResourceToolAction.DUPLICATE, new HtmlDocumentDuplicateAction());
		actions.put(ResourceToolAction.COPY, new HtmlDocumentCopyAction());
		actions.put(ResourceToolAction.MOVE, new HtmlDocumentMoveAction());
		actions.put(ResourceToolAction.DELETE, new HtmlDocumentDeleteAction());

		// initialize actionMap with an empty List for each ActionType
		for(ResourceToolAction.ActionType type : ResourceToolAction.ActionType.values())
		{
			actionMap.put(type, new ArrayList<ResourceToolAction>());
		}
		
		// for each action in actions, add a link in actionMap
		Iterator<String> it = actions.keySet().iterator();
		while(it.hasNext())
		{
			String id = it.next();
			ResourceToolAction action = actions.get(id);
			List<ResourceToolAction> list = actionMap.get(action.getActionType());
			if(list == null)
			{
				list = new ArrayList<ResourceToolAction>();
				actionMap.put(action.getActionType(), list);
			}
			list.add(action);
		}
		
	}

	public ResourceToolAction getAction(String actionId) 
	{
		return (ResourceToolAction) actions.get(actionId);
	}

	public String getIconLocation(ContentEntity entity) 
	{
		return null;
	}
	
	public String getId() 
	{
		return typeId;
	}

	public String getLabel() 
	{
		return rb.getString("type.html");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.entity.api.Reference)
	 */
	public String getLocalizedHoverText(ContentEntity member)
	{
		return rb.getString("type.html");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(org.sakaiproject.content.api.ResourceType.ActionType)
	 */
	public List<ResourceToolAction> getActions(ActionType type)
	{
		List<ResourceToolAction> list = actionMap.get(type);
		if(list == null)
		{
			list = new ArrayList<ResourceToolAction>();
			actionMap.put(type, list);
		}
		return new ArrayList<ResourceToolAction>(list);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(java.util.List)
	 */
	public List<ResourceToolAction> getActions(List<ActionType> types)
	{
		List<ResourceToolAction> list = new ArrayList<ResourceToolAction>();
		if(types != null)
		{
			Iterator<ActionType> it = types.iterator();
			while(it.hasNext())
			{
				ActionType type = it.next();
				List<ResourceToolAction> sublist = actionMap.get(type);
				if(sublist == null)
				{
					sublist = new ArrayList<ResourceToolAction>();
					actionMap.put(type, sublist);
				}
				list.addAll(sublist);
			}
		}
		return list;
	}

}
