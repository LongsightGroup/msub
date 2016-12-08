package com.rsmart.generate.util;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 2/9/12
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryUtility extends SimpleJdbcTemplate implements QueryUtilityService{

    private static final String USER_ID="USER_ID";
    public QueryUtility(DataSource source){
     super(source);
     }

    public String retrieveInternalId(String eid){
      String query= "SELECT USER_ID FROM sakai_user_id_map where EID = ? ";
      List internalId = queryForList(query, new Object[]{eid});
      Map maps = (Map)internalId.get(0);
      String value = (String)maps.get(USER_ID);
      return value;
    }

}
