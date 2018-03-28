package com.rsmart.userdataservice.impl;

import com.rsmart.userdataservice.UserDataService;
import com.rsmart.userdataservice.persistence.model.RsnUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.*;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 18, 2011
 * Time: 3:13:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserDataServiceImpl extends HibernateDaoSupport implements UserDataService {

    private static final Log logger = LogFactory.getLog(UserDataServiceImpl.class);


    public void init() {
        logger.info("Entering the UserDataZService API");
    }

    public void saveOrUpdateRsnUser(User user) {
        RsnUser rsnUser = getUser(user.getEid());
        if (rsnUser == null)   {
            rsnUser = new RsnUser();
            rsnUser.setCreatedBy("admin");
            rsnUser.setCreatedOn(getStartTimestamp());
        }
        rsnUser.setFirstName(user.getFirstName());
        rsnUser.setLastName(user.getLastName());
        rsnUser.setEid(user.getEid());
        rsnUser.setEmail(user.getEmail());
        rsnUser.setType(user.getType());
        rsnUser.setModifiedBy("admin");
        rsnUser.setModifiedOn(getStartTimestamp());
        rsnUser.setPw("");
        rsnUser.setEmailLc(user.getEmail());
        rsnUser = updateExtraPropertiesWithEdit(user, rsnUser);

        saveRsnUser(rsnUser);
    }

     private final String getStartTimestamp()
       {
           final Date
               now = new Date();
           final SimpleDateFormat
               dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

           return dateFormat.format(now);
       }

    protected RsnUser updateExtraPropertiesWithEdit(User user, RsnUser rsnUser)  {
        rsnUser.getProperties().clear();
        for (Iterator<String> i=  user.getProperties().getPropertyNames(); i.hasNext(); )  {
            String propertyName = i.next();
            String propertyValue = user.getProperties().getProperty(propertyName);
            rsnUser.getProperties().put(propertyName, propertyValue);
        }
        return rsnUser;

    }


    /**
     * This method saves all the user and user property info that is retrieved from the users.csv file from
     * SIS synchronization Job.
     *
     * @param rsnUser
     */

    public void saveRsnUser(RsnUser rsnUser) {

        try {
            //Gets all the properties to save to the database
            getHibernateTemplate().saveOrUpdate(rsnUser);

        } catch (Exception e) {
            logger.error("Exception in " + this.getClass().getName() + ":" + "saveRsnUser()", e);
        }

    }

    public RsnUser getUser(String userId) {
        RsnUser user = null;
        try {
            user = (RsnUser) getHibernateTemplate().load(RsnUser.class, userId);
            return user;
        } catch (Exception e) {
            logger.info("no user with eid [" + userId + "] exists in rsn_user tables yet, probably cle processor job needs to be run or RsnUserSyncJob depending on how you are syncing this table." );
        }

        return user;
    }
}
