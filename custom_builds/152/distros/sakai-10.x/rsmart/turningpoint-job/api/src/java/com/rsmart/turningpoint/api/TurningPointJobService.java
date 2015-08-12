package com.rsmart.turningpoint.api;

import java.util.List;


public interface TurningPointJobService
{
	public void registerUserDevices(final List userIdList);
	
	public void updateUserDevices();
	
	public void deleteExistingDeviceIdMappings();
	
	public List getAllUserEid();
}
