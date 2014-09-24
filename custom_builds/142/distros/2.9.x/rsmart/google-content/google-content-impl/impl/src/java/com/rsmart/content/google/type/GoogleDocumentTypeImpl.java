/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.content.google.type;

import com.rsmart.content.google.entity.GoogleContentEntity;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.rsmart.content.google.entity.GoogleContentResource;
import com.rsmart.content.google.api.GoogleDocumentType;

/**
 * The GoogleDocumentType defines the actions surrounding the creation and maintenance of Google Document links
 * within the Resources tool.
 * 
 * User: duffy
 * Date: Feb 1, 2010
 * Time: 8:25:03 AM
 */
public class GoogleDocumentTypeImpl
    extends BaseResourceType
    implements GoogleDocumentType
{
    private static final Log
        LOG = LogFactory.getLog(GoogleDocumentTypeImpl.class);

    private static final String
        PATH_COLL_ICON                  = "icons/collection.gif",
        PATH_RESOURCE_ICON              = "icons/resource.gif",
        GOOGLE_DOCS_MESSAGEBUNDLE       = "com.rsmart.content.google.type.googledocs",
        GOOGLE_DOCS_HELPER_ID           = "com.rsmart.content.google.helper",
        GOOGLE_DOCS_EDIT_HELPER_ID      = "com.rsmart.content.google.editingoogle";

    public static final String
        GOOGLE_EDIT_REDIRECT_URL        = "google.edit.redirect.url";

    private ResourceLoader
        rb = new ResourceLoader(GOOGLE_DOCS_MESSAGEBUNDLE);
    
    protected EnumMap<ResourceToolAction.ActionType,
                      List<ResourceToolAction>>
        actionMap = new EnumMap<ResourceToolAction.ActionType,
                                List<ResourceToolAction>>
                        (ResourceToolAction.ActionType.class);

    protected Map<String, ResourceToolAction>
        actions = new HashMap<String, ResourceToolAction>();

    private ResourceTypeRegistry
        typeRegistry = null;

    private SessionManager
        sessionManager = null;

    public void init()
    {
        LOG.debug ("Initializing GoogleDocumentTypeImpl");

        LOG.debug ("Registring actions");
        actions.put(ResourceToolAction.CREATE,
                    new BaseInteractionAction(ResourceToolAction.CREATE,                    //id
                                              ResourceToolAction.ActionType.NEW_UPLOAD,     //ActionType,
                                              GoogleDocumentType.TYPE_ID,                                      //type id
                                              GOOGLE_DOCS_HELPER_ID,                        //helperId
                                              null)                                         //required property keys
                            {
                                public String getLabel()
                                {
                                    return rb.getString("create.link");
                                }
                            }
                   );
        actions.put(ResourceToolAction.DELETE,
                    new DeleteLinkAction());
        actions.put(ResourceToolAction.COPY,
                    new CopyLinkAction());
        actions.put(ResourceToolAction.DUPLICATE,
                    new DuplicateLinkAction());
        actions.put(ResourceToolAction.REVISE_METADATA,
                    new LinkPropertiesAction());
        actions.put(ResourceToolAction.ACCESS_PROPERTIES,
                    new ViewLinkPropertiesAction());
        actions.put(ResourceToolAction.REVISE_CONTENT,
                    new BaseInteractionAction (ResourceToolAction.REVISE_CONTENT,
                                               ResourceToolAction.ActionType.REVISE_CONTENT,
                                               GoogleDocumentType.TYPE_ID,
                                               GOOGLE_DOCS_EDIT_HELPER_ID,
                                               null)
                            {
                                public String getLabel()
                                {
                                    return rb.getString ("edit.document");
                                }
                            }
                    );

        // initialize actionMap with an empty List for each ActionType
        for(ResourceToolAction.ActionType type : ResourceToolAction.ActionType.values())
        {
            actionMap.put(type, new ArrayList<ResourceToolAction>());
        }

        for (ResourceToolAction action : actions.values())
        {
            List<ResourceToolAction>
                list = actionMap.get(action.getActionType());

            if(list == null)
            {
                actionMap.put(action.getActionType(), new ArrayList<ResourceToolAction> ());
            }
            list.add(action);
        }

        if (ServerConfigurationService.getBoolean("google-content.enabled", false))
        {
            LOG.debug("Google Document support enabled");

            final ResourceTypeRegistry
                rtr = getResourceTypeRegistry();

            if (rtr == null)
                LOG.fatal ("ResourceTypeRegistry not provided to GoogleDocumentTypeImpl. Google Document support diabled.");
            else
                rtr.register(this);
        }
        else
        {
            LOG.debug("Google Document support disabled - include 'google-content.enabled=true' in Sakai properties to enable");
        }
    }

    public void setSessionManager (SessionManager sm)
    {
        sessionManager = sm;
    }

    public SessionManager getSessionManager()
    {
        return sessionManager;
    }
    
    public void setResourceTypeRegistry (ResourceTypeRegistry registry)
    {
        typeRegistry = registry;
    }

    public ResourceTypeRegistry getResourceTypeRegistry ()
    {
        return typeRegistry;
    }

    /**
     * Retrieve the registered ResourceToolActions for the provided Acti
     *
     * @param type
     * @return
     */
    public List<ResourceToolAction> getActions(ResourceToolAction.ActionType type)
    {
        List<ResourceToolAction>
            list = actionMap.get(type);

        if(list == null)
        {
            return new ArrayList<ResourceToolAction>();
        }

        return new ArrayList<ResourceToolAction>(list);
    }

    public List<ResourceToolAction> getActions(List<ResourceToolAction.ActionType> types)
    {
        List<ResourceToolAction>
            list = new ArrayList<ResourceToolAction>();

        if (types != null)
            for (ResourceToolAction.ActionType type : types)
            {
                List<ResourceToolAction> sublist = actionMap.get(type);
                if(sublist == null)
                {
                    sublist = new ArrayList<ResourceToolAction>();
                    actionMap.put(type, sublist);
                }
                list.addAll(sublist);
            }

        return list;
    }

    public ResourceToolAction getAction(String actionId)
    {
        return (ResourceToolAction) actions.get(actionId);
    }

    public String getIconLocation(ContentEntity entity)
    {
        if (entity != null && GoogleContentResource.class.isAssignableFrom (entity.getClass()))
        {
            return PATH_RESOURCE_ICON;
        }

        return null;
    }

    public String getId()
    {
        return GoogleDocumentType.TYPE_ID;
    }

    public String getLabel()
    {
        return rb.getString("type.googledoc");
    }

    public String getLocalizedHoverText(ContentEntity entity)
    {
        return rb.getString("type.googledoc");
    }

    public class GoogleDocumentCreateAction
        extends BaseInteractionAction
    {

        public GoogleDocumentCreateAction(String id, ActionType actionType, String typeId,
                                String helperId, List requiredPropertyKeys)
        {
            super(id, actionType, typeId, helperId, requiredPropertyKeys);
        }
    }

    class DeleteLinkAction implements ServiceLevelAction
    {

        public void initializeAction(Reference reference) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void finalizeAction(Reference reference) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void cancelAction(Reference reference) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean isMultipleItemAction() {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getId() {
            return ResourceToolAction.DELETE;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getTypeId() {
            return GoogleDocumentType.TYPE_ID;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public ActionType getActionType() {
            return ResourceToolAction.ActionType.DELETE;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getLabel() {
            return rb.getString("delete");  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean available(ContentEntity entity) {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public class CopyLinkAction implements ServiceLevelAction
    {
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
			return rb.getString("copy");
		}

		public boolean isMultipleItemAction()
		{
			return true;
		}

		public String getTypeId()
		{
			return GoogleDocumentType.TYPE_ID;
		}

		public void cancelAction(Reference reference)
		{
		}

		public void finalizeAction(Reference reference)
		{
		}

		public void initializeAction(Reference reference)
		{
		}

        public boolean available(ContentEntity entity)
        {
	        return true;
        }
	}

    public class DuplicateLinkAction implements ServiceLevelAction
    {
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
            return rb.getString("duplicate");
        }

        public boolean isMultipleItemAction()
        {
            return false;
        }

        public String getTypeId()
        {
            return GoogleDocumentType.TYPE_ID;
        }

        public void cancelAction(Reference reference)
        {

        }

        public void finalizeAction(Reference reference)
        {

        }

        public void initializeAction(Reference reference)
        {

        }

        public boolean available(ContentEntity entity)
        {
            return true;
        }
    }

    public class LinkPropertiesAction implements ServiceLevelAction
    {

        public void cancelAction(Reference reference)
        {

        }

        public void finalizeAction(Reference reference)
        {

        }

        public void initializeAction(Reference reference)
        {

        }

        public boolean isMultipleItemAction()
        {
            return false;
        }

        public ActionType getActionType()
        {
            return ResourceToolAction.ActionType.REVISE_METADATA;
        }

        public String getId()
        {
            return ResourceToolAction.REVISE_METADATA;
        }

        public String getLabel()
        {
            return rb.getString("props");
        }

        public String getTypeId()
        {
            return GoogleDocumentType.TYPE_ID;
        }

        public boolean available(ContentEntity entity)
        {
            return true;
        }

    }

    public class ViewLinkPropertiesAction implements ServiceLevelAction
    {

        public void cancelAction(Reference reference)
        {

        }

        public void finalizeAction(Reference reference)
        {

        }

        public void initializeAction(Reference reference)
        {

        }

        public boolean isMultipleItemAction()
        {
            return false;
        }

        public ActionType getActionType()
        {
            return ResourceToolAction.ActionType.VIEW_METADATA;
        }

        public String getId()
        {
            return ResourceToolAction.ACCESS_PROPERTIES;
        }

        public String getLabel()
        {
            return rb.getString("access");
        }

        public String getTypeId()
        {
            return GoogleDocumentType.TYPE_ID;
        }

        public boolean available(ContentEntity entity)
        {
            return true;
        }

    }


}
