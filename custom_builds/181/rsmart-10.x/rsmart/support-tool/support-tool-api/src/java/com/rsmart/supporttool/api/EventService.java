package com.rsmart.supporttool.api;

import java.util.List;

public interface EventService{

    public int SORT_ORDER_ASCENDING = 1;
    public int SORT_ORDER_DESCENDING = 2;
    public int SORT_FIELD_ID = 1;
    public int SORT_FIELD_DATE = 2;
    public int SORT_FIELD_EVENT = 3;
    public int SORT_FIELD_REF = 4;
    public int SORT_FIELD_CONTEXT = 5;
    public int SORT_FIELD_SESSION_ID = 6;
    public int SORT_FIELD_EVENT_CODE = 7;

    public List getEventTypes();

    public int countQuery(SupportBean supportBean);

    public List queryWithFilters(SupportBean supportBean, int sortField, int sortOrder);

}