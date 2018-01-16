package com.rsmart.sakai.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Apr 2, 2010
 * Time: 10:01:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class JdbcUserProvider implements UserDirectoryProvider {
    private static Log logger = LogFactory.getLog(JdbcUserProvider.class);    
    private String authenticateUserSql;
    private String findUserByEmailSql;
    private String userSql;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private String defaultUserType = "registered";
    private MemoryService memoryService;
    private Cache cache;

    public void init() {
        cache = memoryService.newCache(JdbcUserProvider.class.getName());
    }

    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public boolean authenticateUser(String eid, UserEdit edit, String password) {
        Map namedParameters = new HashMap();
        namedParameters.put("eid", eid);
        namedParameters.put("password", password);
        List<User> foundUsers = namedParameterJdbcTemplate.query(authenticateUserSql, namedParameters, getUserMapper());
        if (foundUsers == null || foundUsers.size() == 0) {
            return false;
        }
        populateUserEdit(edit, foundUsers.get(0));
        logger.info("authenticated user with eid:" + eid);
        return true;
    }

    public boolean authenticateWithProviderFirst(String eid) {
        return true;
    }

    public boolean findUserByEmail(UserEdit edit, String email) {
        User user = null;

        if (cache.containsKey(edit.getEid())){
            user = (User) cache.get(edit.getEid());
        } else {
            Map namedParameters = new HashMap();
            namedParameters.put("email", email);

            List<User> foundUsers = namedParameterJdbcTemplate.query(findUserByEmailSql, namedParameters, getUserMapper());
             if (foundUsers == null || foundUsers.size() == 0) {
                logger.info("can't find an existing user with email:" + email);
                return false;
            }
            user = foundUsers.get(0);
        }
        populateUserEdit(edit, user);


        return true;
    }

    private void populateUserEdit(UserEdit edit, User result) {
        // TODO figured out how to deal with this
        edit.setType(defaultUserType);

        // TODO support user properties ?

        BeanUtils.copyProperties(result, edit);

        cache.put(edit.getEid(), result);
    }

    private RowMapper getUserMapper() {
        return new RowMapper(){
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setEid(rs.getString("eid"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        };
    }

    public boolean getUser(UserEdit edit) {
        if (edit.getEid() == null) return false;

        User user = null;

        if (cache.containsKey(edit.getEid())){
            user = (User) cache.get(edit.getEid());
        } else {
            Map namedParameters = new HashMap();
            namedParameters.put("eid", edit.getEid());
            List<User> foundUsers = namedParameterJdbcTemplate.query(userSql, namedParameters, getUserMapper());
            if (foundUsers == null || foundUsers.size() == 0) {
                logger.info("can't find an existing user with eid:" + edit.getEid());
                return false;
            }
            user = foundUsers.get(0);
        }

        populateUserEdit(edit, user);

        return true;
    }

    public void getUsers(Collection<UserEdit> users) {
        for (Iterator i = users.iterator(); i.hasNext();) {
           UserEdit user = (UserEdit) i.next();
           if (!getUser(user)) {
              i.remove();
           }
        }
    }

    public void setAuthenticateUserSql(String authenticateUserSql) {
        this.authenticateUserSql = authenticateUserSql;
    }

    public void setFindUserByEmailSql(String findUserByEmailSql) {
        this.findUserByEmailSql = findUserByEmailSql;
    }

    public void setUserSql(String userSql) {
        this.userSql = userSql;
    }

    public void setDefaultUserType(String defaultUserType) {
        this.defaultUserType = defaultUserType;
    }

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    class User {
        private String email;
        private String eid;
        private String firstName;
        private String lastName;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getEid() {
            return eid;
        }

        public void setEid(String eid) {
            this.eid = eid;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
