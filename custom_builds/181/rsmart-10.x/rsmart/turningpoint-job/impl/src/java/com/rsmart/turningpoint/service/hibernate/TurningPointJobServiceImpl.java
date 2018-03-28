package com.rsmart.turningpoint.service.hibernate;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.rsmart.turningpoint.api.TurningPointJobService;
import com.rsmart.turningpoint.impl.hibernate.TPDeviceIdMapping;


public class TurningPointJobServiceImpl extends HibernateDaoSupport implements TurningPointJobService
{
	
	private UserDirectoryService userDirectoryService = null;
	public static final String DEVICE_ID = "turningPointDeviceId";

	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void init() {
		
		logger.info("TurningPointJobService init");
		
	    if (userDirectoryService == null)
	    {
	        throw new IllegalStateException ("UserDirectoryService not provided to TurningPointJobService");
	    }
	}

	public void updateUserDevices() {
		// TODO Auto-generated method stub
		
		//first clean up the device id mapping table so we don't have to worry about duplicate
		//entries when device id's are given to a new student.
		deleteExistingDeviceIdMappings();
		
		//Get All Users in SAKAI_USER_ID_MAP table
		
		//register device id mappings from user's properties into TT_DEVICE_ID_MAPPING
		registerUserDevices(getAllUserEid());
		
	}

	public void deleteExistingDeviceIdMappings()
	{
		getHibernateTemplate().execute
		(
			new HibernateCallback()
	        {
	            public Object doInHibernate(Session session)
	                throws HibernateException, SQLException
	            {
	            	Query
                    q = session.createQuery("DELETE FROM TPDeviceIdMapping");
	                q.executeUpdate();
	                return null;
	            }
	        }
		);
	}
	
	public List getAllUserEid()
	{


		return (List) getHibernateTemplate().execute
		(
				new HibernateCallback()
				{
					public Object doInHibernate(Session session)
						throws HibernateException, SQLException
					{
						return session.createSQLQuery("SELECT um.USER_ID FROM SAKAI_USER_ID_MAP um, RSN_USER ru WHERE ru.USER_EID = um.EID ORDER BY um.USER_ID").list();
					}
				}
		);
	}
	
	public void registerUserDevices(final List userIdList) {
		// TODO Auto-generated method stub
		getHibernateTemplate().execute
		(
			new HibernateCallback()
	        {
	            public Object doInHibernate(Session session)
	                throws HibernateException, SQLException
	            {
	            	try
            		{
	            		List<User> users = getUserDirectoryService().getUsers(userIdList);
		            	for(User user : users)
		            	{
		            		String deviceId = null;
	            			deviceId = user.getProperties().getProperty(DEVICE_ID);
		            		if(deviceId != null && deviceId.length() > 0 && user != null)
		            		{
		            			TPDeviceIdMapping deviceMapping = new TPDeviceIdMapping(deviceId);
		            			deviceMapping.setUserId(user.getId());
		            			session.save(deviceMapping);
		            		}
		            	}
            		}
            		catch (Exception ex)
            		{
            			logger.error("Exception registering turning point user device id's; " + ex.getMessage(), ex);
            		}
	            	return null;
	            }
	        }
		);
		
	}
	
}