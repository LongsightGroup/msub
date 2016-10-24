package org.sakaiproject.sitemanage.api;

import org.sakaiproject.user.api.User;

public interface GuestProvisioner {
	
	public User provisionNewGuestForSite(String userId, String siteId, String sponsorEid);
	
	public void provisionExistingGuestForSite(String userId, String siteId, String sponsorEid);

}
