package com.anisakai.entitybroker.providers;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * AbstractAsahiEntityProvider
 * 
 * All Asahi Providers should extend this
 *
 * @author Earle Nietzel
 * Created on Sep 10, 2013
 * 
 */
public abstract class AbstractAsahiEntityProvider extends AbstractEntityProvider {

	public final static String ASAHI_ENTITY_PREFIX = "asahi-";
	
	private DeveloperHelperService developerHelperService;
	
	public AbstractAsahiEntityProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	@Override
	public final String getEntityPrefix() {
		return ASAHI_ENTITY_PREFIX + getLocalEntityPrefix();
	}
	
	/**
	 * Declare a entity tool prefix, for example:<br><br>
	 * public final static String ENTITY_PREFIX = "tool";<br><br>
	 * where getLocalEntityPrefix() should return that string.
	 * 
	 * All asahi entity providers will then be registerd as<br>
	 * "asahi-" +  getLocalEntityPrefix()
	 * 
	 * @return local tool prefix i.e. "tool"
	 */
	public abstract String getLocalEntityPrefix();

	
	/**
	 * Helper method to determine if the current request is from an admin user.
	 * 
	 * @return true if the user has admin privileges
	 */
	public final boolean isSuperUser() {
        String currentUserRef = developerHelperService.getCurrentUserReference();
    
        if (currentUserRef != null) {
            String currentUserId = developerHelperService.getUserIdFromRef(currentUserRef);
            if (developerHelperService.isUserAdmin(currentUserId)) { 
                return true;
            }
        }
        
        return false;
	}
	
	
	/**
	 * Find out if a request is internal or external
	 * 
	 * @param ref Entity reference
	 * @return true if internal false otherwise
	 */
	public final boolean isEntityInternalRequest(EntityReference ref) {
		if (ref == null) {
			return false;
		}
		
		return developerHelperService.isEntityRequestInternal(ref.toString());
	}
	
	public DeveloperHelperService getDeveloperHelperService() {
		return developerHelperService;
	}

	public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}

}
