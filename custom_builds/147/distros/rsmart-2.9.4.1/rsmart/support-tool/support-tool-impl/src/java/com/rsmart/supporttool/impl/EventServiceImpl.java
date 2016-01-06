package com.rsmart.supporttool.impl;

import com.rsmart.supporttool.api.Event;
import com.rsmart.supporttool.api.EventService;
import com.rsmart.supporttool.api.SupportBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventServiceImpl extends NamedParameterJdbcTemplate implements EventService {
 protected final Log logger = LogFactory.getLog(getClass());
    private SqlService sqlService;

    public List eventList;

    public EventServiceImpl(DataSource dataSource, SqlService sqlService) {
        super(dataSource);
        setSqlService(sqlService);
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public List getEventTypes() {

        List<String> eventTypes = new ArrayList<String>();
        List tempList = query("SELECT DISTINCT EVENT FROM sakai_event ORDER BY EVENT ASC", new HashMap(), new EventTypeMapper());

        for (int i = 0; i < tempList.size(); i++) {
            Event event = (Event) tempList.get(i);
            eventTypes.add(event.getType());
        }
        tempList.clear();
        return eventTypes;
    }

    public int countQuery(SupportBean supportBean) {

        Map params = new HashMap();
        String query = createQuery("count", supportBean, params);

        int count = queryForInt(query, params);

        return count;
    }

    protected String createQuery(String query, SupportBean supportBean, Map params) {
        return createQuery(query, supportBean, EventService.SORT_FIELD_ID, EventService.SORT_ORDER_ASCENDING, params);
    }


    public List queryWithFilters(final SupportBean supportBean, int sortField, int sortOrder) {
        Map params = new HashMap();
        String query = createQuery("query", supportBean, sortField, sortOrder, params);

        eventList = (List) query(query, new MapSqlParameterSource(params),  new ResultSetExtractor() {
            public Object extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                List<Event> events = new ArrayList<Event>();

                while (resultSet.next()) {

                    Event newEvent = new Event();

                    newEvent.setId(resultSet.getString("EVENT_ID"));
                    newEvent.setDate(resultSet.getString("EVENT_DATE"));
                    newEvent.setType(resultSet.getString("EVENT"));
                    newEvent.setRef(resultSet.getString("REF"));
                    newEvent.setContext(resultSet.getString("CONTEXT"));
                    newEvent.setSessionId(resultSet.getString("SESSION_ID"));
                    newEvent.setCode(resultSet.getString("EVENT_CODE"));

                    events.add(newEvent);
                }

                return events;
            }
        });

        return eventList;
    }

    protected String createQuery(String queryType, SupportBean supportBean, int sortField, int sortOrder, Map params){
        StringBuffer queryBuffer = new StringBuffer();

        
        if ("count".equalsIgnoreCase(queryType)) {
            queryBuffer.append("SELECT COUNT(*) FROM sakai_event ");
        }
        if ("query".equalsIgnoreCase(queryType)) {
            if (sqlService.getVendor().equals("oracle")) {
                queryBuffer.append("SELECT EVENT_ID,EVENT_DATE,EVENT,REF,CONTEXT,SESSION_ID,EVENT_CODE FROM (");
                queryBuffer.append("SELECT EVENT_ID,EVENT_DATE,EVENT,REF,CONTEXT,SESSION_ID,EVENT_CODE, rownum seq_num FROM sakai_event ");
            } else {
                queryBuffer.append("SELECT EVENT_ID,EVENT_DATE,EVENT,REF,CONTEXT,SESSION_ID,EVENT_CODE FROM sakai_event ");
            }
        }

        if (supportBean.notEmpty()) {
            queryBuffer.append(" WHERE ");
        }

        Boolean firstCondition = Boolean.valueOf(true);

        if (supportBean.getType() != null) {
            firstCondition = appendAnd(queryBuffer, firstCondition);
            queryBuffer.append("EVENT=:type");
            params.put("type" , supportBean.getType());
        }
        if (supportBean.getContext() != null) {
            firstCondition = appendAnd(queryBuffer, firstCondition);

            queryBuffer.append("CONTEXT LIKE :context");
            params.put("context" , "%" + supportBean.getContext() + "%");
        }
        if (supportBean.getRef() != null) {
            firstCondition = appendAnd(queryBuffer, firstCondition);

            queryBuffer.append("REF LIKE :ref ");
            params.put("ref", "%"+supportBean.getRef()+"%");
        }
        if (supportBean.getStartDate() != null && supportBean.getEndDate() != null) {
            firstCondition = appendAnd(queryBuffer, firstCondition);

            // select * from sakai_event where event_date between to_date('2001-12-12 12:12','YYYY-MM-DD HH24:MI') and to_date('2017-12-12 12:12','YYYY-MM-DD HH24:MI')
            if (sqlService.getVendor().equals("oracle")) {
                queryBuffer.append("EVENT_DATE BETWEEN to_date(:startDate, 'YYYY-MM-DD HH24:MI') AND to_date(:endDate, 'YYYY-MM-DD HH24:MI')");
                params.put("startDate", format.format(supportBean.getStartDate()));
                params.put("endDate", format.format(supportBean.getEndDate()));
            // select * from sakai_event where event_date between '2001-12-12 12:12' and '2017-12-12 12:12';
            } else {
                queryBuffer.append("EVENT_DATE BETWEEN :startDate AND :endDate");
                params.put("startDate", format.format(supportBean.getStartDate()));
                params.put("endDate", format.format(supportBean.getEndDate()));
            }
        }

        if ("query".equalsIgnoreCase(queryType)) {
            if ("EVENT_DATE".equals(sortField)) {
                queryBuffer.append(" ORDER BY EVENT_DATE");
            } else if ("EVENT_TYPE".equals(sortField)) {
                queryBuffer.append(" ORDER BY EVENT_TYPE");
            } else if ("REF".equals(sortField)) {
                queryBuffer.append(" ORDER BY REF");
            } else if ("CONTEXT".equals(sortField)) {
                queryBuffer.append(" ORDER BY CONTEXT");
            } else if ("SESSION_ID".equals(sortField)) {
                queryBuffer.append(" ORDER BY SESSION_ID");
            } else if ("EVENT_CODE".equals(sortField)) {
                queryBuffer.append(" ORDER BY EVENT_CODE");
            } else {
                queryBuffer.append(" ORDER BY EVENT_ID");
            }

            if (sortOrder == EventService.SORT_ORDER_ASCENDING) {
                queryBuffer.append(" ASC ");
            } else {
                queryBuffer.append(" DESC");
            }

            int pageSize = supportBean.getPageSize();
            int startRow = supportBean.getStartIndex();

            if (sqlService.getVendor().equals("oracle")) {
                int endRow = startRow + pageSize - 1;
                queryBuffer.append(" ) WHERE seq_num BETWEEN " + startRow + " AND " + endRow );
            } else {
                queryBuffer.append(" limit " + startRow + "," + pageSize);
            }
        }
        logger.debug(queryBuffer.toString());
        return queryBuffer.toString();
    }

    private Boolean appendAnd(StringBuffer queryBuffer, Boolean firstCondition) {


        if (!firstCondition.booleanValue()) {
            queryBuffer.append(" AND ");
        }
        
        firstCondition = Boolean.valueOf(false);
        return firstCondition;
    }

    private class EventTypeMapper implements ParameterizedRowMapper<Event> {

        public Event mapRow(ResultSet resultSet, int i) throws SQLException {

            Event event = new Event();

            event.setType(resultSet.getString("EVENT"));

            return event;
        }
    }

    public SqlService getSqlService() {
        return sqlService;
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
}