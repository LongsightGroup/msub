package com.anisakai.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.tool.api.Tool;

import com.anisakai.entitybroker.providers.model.EntityTool;
import com.rsmart.sakaiproject.tool.api.ReloadableToolManager;

public class ToolEntityProvider extends AbstractAsahiEntityProvider implements CoreEntityProvider, 
	Outputable, Resolvable, Describeable, ActionsExecutable, CollectionResolvable, Updateable, Inputable {

	private static Log LOG = LogFactory.getLog(ToolEntityProvider.class);
	public final static String ENTITY_PREFIX = "tool";
	
	private ReloadableToolManager reloadableToolManager;

	@Override
	public String getLocalEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Updateable#updateEntity(org.sakaiproject.entitybroker.EntityReference, java.lang.Object, java.util.Map)
	 */
	@Override
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        if (! isSuperUser()) {
        	throw new SecurityException("Current user cannot update information becuase they are not an admin.");
        }
		
		String toolId = ref.getId();
        if (toolId == null || "".equals(toolId)) {
            throw new IllegalArgumentException("Unable to perform tool update missing tool id for ref " + ref);
        }
        
        Tool orginalTool = reloadableToolManager.getTool(toolId);

        // ensure tool already exists
        if (orginalTool == null) {
        	LOG.warn("Unable to perform tool update cannot locate existing tool " + toolId);
        	return;
        }

    	// entity must be a tool
        if (entity instanceof Tool) {
        	Tool entityTool = (Tool) entity;

        	// only update if the toolids are the same
        	if (toolId.equals(entityTool.getId())) {
        		reloadableToolManager.changeTool((Tool) entity);
        	} else {
        		LOG.warn("Different tool id unable to perform tool update for tool " + toolId);
        	}
        } else {
        	LOG.warn("Entity was not Tool, unable to perform tool update for tool " + toolId);
        }
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
	 */
	@Override
	public Object getEntity(EntityReference ref) {
		if (ref == null || ref.getId() == null || getTool(ref.getId()) == null) {
			return new EntityTool();
		}
		
		return getToolEntity(getTool(ref.getId()));
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable#getSampleEntity()
	 */
	@Override
	public Object getSampleEntity() {
		return new EntityTool();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable#getHandledInputFormats()
	 */
	@Override
	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#getHandledOutputFormats()
	 */
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.HTML, Formats.JSON, Formats.FORM };
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	@Override
	public boolean entityExists(String id) {
		if (id == null) {
			return false;
		}
		
		Tool tool = getTool(id);
			
		if (tool != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public List<?> getEntities(EntityReference ref, Search search) {
        List<EntityTool> tools = new ArrayList<EntityTool>();
        if (search.getRestrictionByProperty("id") != null) {
        	String id = search.getRestrictionByProperty("id").getStringValue();

        	EntityTool entityTool = getToolEntity(getTool(id));
        	if (entityTool != null) {
        		tools.add(entityTool);
        	}
        } else if (search.getRestrictionByProperty("keywords") != null) {
        	String[] keywords = (String[]) search.getRestrictionByProperty("keywords").getArrayValue();
        	Set<Tool> found = reloadableToolManager.findTools(null, new HashSet<String>(Arrays.asList(keywords)));
        	
        	for (Tool tool : found) {
        		EntityTool entityTool = getToolEntity(tool);
            	if (entityTool != null) {
            		tools.add(entityTool);
            	}
			}
        } else if (search.getRestrictionByProperty("categories") != null) {
        	String[] categories = (String[]) search.getRestrictionByProperty("categories").getArrayValue();
        	Set<Tool> found = reloadableToolManager.findTools(new HashSet<String>(Arrays.asList(categories)), null);
        	
        	for (Tool tool : found) {
        		EntityTool entityTool = getToolEntity(tool);
            	if (entityTool != null) {
            		tools.add(entityTool);
            	}
        	}
        } else {
        	Set<Tool> found = reloadableToolManager.findTools(null, null);
        	
        	for (Tool tool : found) {
        		EntityTool entityTool = getToolEntity(tool);
            	if (entityTool != null) {
            		tools.add(entityTool);
            	}
        	}
        }
        Collections.sort(tools);
        return tools;
	}

    @EntityCustomAction(action="allToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getAllToolIds(EntityReference ref) {
    	Set<Tool> tools = reloadableToolManager.findTools(null, null);
    	List<String> toolIds = new ArrayList<String>(tools.size());
    	
    	for (Tool tool : tools) {
    		toolIds.add(tool.getId());
    	}
    	Collections.sort(toolIds);
        return new ActionReturn(toolIds);
    }
    
    @EntityCustomAction(action="hiddenToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getHiddenToolIds(EntityReference ref) {
    	Set<Tool> allTools = reloadableToolManager.findTools(null, null);
    	Set<Tool> publicTools = reloadableToolManager.findTools(Collections.<String> emptySet(), null);
    	
    	List<String> allToolIds = new ArrayList<String>(allTools.size());
    	List<String> publicToolIds = new ArrayList<String>(publicTools.size());
    	List<String> hiddenToolIds = new ArrayList<String>(allTools.size() - publicToolIds.size());
    	
    	for (Tool tool : allTools) {
    		allToolIds.add(tool.getId());
    	}
    	
    	for (Tool tool : publicTools) {
    		publicToolIds.add(tool.getId());
    	}
    	
    	hiddenToolIds.addAll(allToolIds);
    	hiddenToolIds.removeAll(publicToolIds);
    	
    	Collections.sort(hiddenToolIds);
        return new ActionReturn(hiddenToolIds);
    }

    @EntityCustomAction(action="publicToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getPublicToolIds(EntityReference ref) {
    	Set<Tool> publicTools = reloadableToolManager.findTools(Collections.<String> emptySet(), null);
    	
    	List<String> publicToolIds = new ArrayList<String>(publicTools.size());
    	
    	for (Tool tool : publicTools) {
    		publicToolIds.add(tool.getId());
    	}
    	
    	Collections.sort(publicToolIds);
        return new ActionReturn(publicToolIds);
    }
    
	private Tool getTool(String id) {
		if (! "".equals(id)) {
			return reloadableToolManager.getTool(id);
		}
		return null;
	}
	
	private EntityTool getToolEntity(Tool tool) {
		if (tool != null) {
			return new EntityTool(tool);
		}
		return null;
	}

	public void setReloadableToolManager(ReloadableToolManager reloadableToolManager) {
		this.reloadableToolManager = reloadableToolManager;
	}
}
