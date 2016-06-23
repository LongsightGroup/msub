package com.rsmart.userdataservice;

import com.rsmart.userdataservice.persistence.model.RsnUser;
import org.sakaiproject.user.api.User;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 18, 2011
 * Time: 3:14:28 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserDataService {
    public void saveOrUpdateRsnUser(User user);
    /**
     * This method saves all the user and user property info that is retrieved from the users.csv file from
     * SIS synchronization Job. rsn_user and rsn_user_property tables
     * @param rsnUser
     */
    public void saveRsnUser( RsnUser rsnUser);

    public RsnUser getUser(String userId);


}
