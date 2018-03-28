package com.rsmart.supporttool.tool;

import com.rsmart.sakai.common.web.listfilter.PagedViewListController;
import com.rsmart.supporttool.api.Event;
import com.rsmart.supporttool.api.EventService;
import com.rsmart.supporttool.api.SupportBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SupportToolHomeController extends PagedViewListController {
    protected final Log logger = LogFactory.getLog(getClass());
    private EventService eventService;
    private List eventList = Collections.EMPTY_LIST;
    private List pagedList = Collections.EMPTY_LIST;

    public Map referenceData(HttpServletRequest request) {

        Map map = super.referenceData(request);

        if (map == null) {
            map = new HashMap();
        }
        if (request.getParameter("eventTypes") == null) {
            map.put("eventTypes", eventService.getEventTypes());
        }

        SupportBean supportBean = (SupportBean) request.getSession().getAttribute("supportBean");
        if (supportBean == null) {
            supportBean = new SupportBean();
            request.getSession().setAttribute("supportBean", supportBean);
        }
        map.put("supportBean", supportBean);

        return map;
    }

    protected int getListSize(HttpServletRequest request) {

        SupportBean supportBean = (SupportBean) request.getSession().getAttribute("supportBean");
        int count = 0;

        if (request.getParameter("_clear") != null) {
            return 0;
        }
        if (supportBean != null) {
            count = eventService.countQuery(supportBean);
            supportBean.setCount(count);
            request.getSession().setAttribute("supportBean", supportBean);
            request.getSession().setAttribute("currentListSize", count);

            return count;
        }
        if(request.getSession().getAttribute("currentListSize") != null){

            count = (Integer) request.getSession().getAttribute("currentListSize");
            return count;
        }
        else {
            return 0;
        }
    }

    public List getFilterNames() {
        return eventService.getEventTypes();
    }

    protected List getList(HttpServletRequest request, int startIndex, int pageSize) {

        SupportBean supportBean = bindParameters(request);

        if (supportBean == null) {
            supportBean = new SupportBean();
        }

        supportBean.setStartIndex(startIndex);
        supportBean.setPageSize(pageSize);
        request.getSession().setAttribute("supportBean", supportBean);

        int sortOrder = isAscendingOrder() ? EventService.SORT_ORDER_ASCENDING : EventService.SORT_ORDER_DESCENDING;
        int sortField = EventService.SORT_FIELD_ID;

        if ("EVENT_ID".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_ID;
        }
        if ("EVENT_DATE".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_DATE;
        }
        if ("EVENT_TYPE".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_EVENT;
        }
        if ("REF".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_REF;
        }
        if ("CONTEXT".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_CONTEXT;
        }
        if ("SESSION_ID".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_SESSION_ID;
        }
        if ("EVENT_CODE".equals(getCurrentSortCol())) {
            sortField = EventService.SORT_FIELD_EVENT_CODE;
        }

        request.getSession().setAttribute("supportBean", supportBean);
        return eventService.queryWithFilters(supportBean, sortField, sortOrder);
    }

    protected SupportBean bindParameters(HttpServletRequest request) {

        SupportBean supportBean = (SupportBean) request.getSession().getAttribute("supportBean");

        if(supportBean == null){
            supportBean = new SupportBean();
        }

        if (request.getParameter("_clear") != null) {
            supportBean = new SupportBean();
            return null;
        }

        if (request.getParameter("searchText") != null && !"".equals(request.getParameter("searchText"))) {
            if ("context".equals(request.getParameter("searchSelect"))) {
                supportBean.setContext(request.getParameter("searchText"));
            }
            if ("ref".equals(request.getParameter("searchSelect"))) {
                supportBean.setRef(request.getParameter("searchText"));
            }
        }
        if (request.getParameter("filterEventType") != null && !"".equals(request.getParameter("filterEventType"))) {
            supportBean.setType(request.getParameter("filterEventType"));
        }

        if (request.getParameter("schedule.startDateBean.year") != null &&
                request.getParameter("schedule.endDateBean.year") != null) {
            String startDate = getStartDate(request);
            String endDate = getEndDate(request);

            supportBean.setStartDate(formatDate(startDate));
            supportBean.setEndDate(formatDate(endDate));
            
            if(startDate.compareTo(endDate) > 0){
               request.setAttribute("errorString", "The Start Date can not be later than the End Date!");
               return null;
            }
        }

        request.setAttribute("supportBean", supportBean);


        return supportBean;
    }

    private Date formatDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
           logger.error("bad date format:" + e.getMessage());
        }
        return null;
    }

    private String getStartDate(HttpServletRequest request) {

        StringBuffer dateAssembler = new StringBuffer();

        dateAssembler.append(request.getParameter("schedule.startDateBean.year"));
        dateAssembler.append("-");
        dateAssembler.append(request.getParameter("schedule.startDateBean.month"));
        dateAssembler.append("-");
        dateAssembler.append(request.getParameter("schedule.startDateBean.day"));
        dateAssembler.append(" ");
        dateAssembler.append(request.getParameter("schedule.startDateBean.hour"));
        dateAssembler.append(":");
        dateAssembler.append(request.getParameter("schedule.startDateBean.minute"));

        return dateAssembler.toString();
    }

    private String getEndDate(HttpServletRequest request) {
        StringBuffer dateAssembler = new StringBuffer();

        dateAssembler.append(request.getParameter("schedule.endDateBean.year"));
        dateAssembler.append("-");
        dateAssembler.append(request.getParameter("schedule.endDateBean.month"));
        dateAssembler.append("-");
        dateAssembler.append(request.getParameter("schedule.endDateBean.day"));
        dateAssembler.append(" ");
        dateAssembler.append(request.getParameter("schedule.endDateBean.hour"));
        dateAssembler.append(":");
        dateAssembler.append(request.getParameter("schedule.endDateBean.minute"));

        return dateAssembler.toString();
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        SupportBean supportBean = bindParameters(request);

        if(supportBean != null){
            supportBean.setPageSize(getPageSize(request, response));
            request.getSession().setAttribute("supportBean", supportBean);
        }

        return super.handleRequestInternal(request, response);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    private class EventServiceComparator implements Comparator {
        int sortOrder;
        int sortField;

        public EventServiceComparator(int sortOrder, int sortField) {
            this.sortOrder = sortOrder;
            this.sortField = sortField;
        }

        public int compare(Object o, Object o1) {

            int compareResult = 0;
            Integer e1, e2;

            Event event1 = (Event) o;
            Event event2 = (Event) o1;

            if (event1 == null && event2 == null) {
                compareResult = 0;
            } else if (event1 == null && event2 != null) {
                compareResult = 1;
            } else if (event1 != null && event2 == null) {
                compareResult = -1;
            } else {
                switch (sortField) {
                    case EventService.SORT_FIELD_ID:
                        e1 = Integer.parseInt(event1.getId());
                        e2 = Integer.parseInt(event2.getId());
                        compareResult = e1.compareTo(e2);
                        break;
                    case EventService.SORT_FIELD_DATE:
                        compareResult = event1.getDate().compareToIgnoreCase(event2.getDate());
                        break;
                    case EventService.SORT_FIELD_EVENT:
                        compareResult = event1.getType().compareToIgnoreCase(event2.getType());
                        break;
                    case EventService.SORT_FIELD_REF:
                        //If the ref field is null, sort by event id instead.
                        if (event1.getRef() == null || event2.getRef() == null){
                            e1 = Integer.parseInt(event1.getId());
                            e2 = Integer.parseInt(event2.getId());
                            compareResult = e1.compareTo(e2);
                        }
                        else{
                            compareResult = event1.getRef().compareToIgnoreCase(event2.getRef());
                        }
                        break;
                    case EventService.SORT_FIELD_CONTEXT:
                        //If the context field is null, sort by event id instead.
                        if (event1.getContext() == null || event2.getContext() == null){
                            e1 = Integer.parseInt(event1.getId());
                            e2 = Integer.parseInt(event2.getId());
                            compareResult = e1.compareTo(e2);
                        }
                        else{
                            compareResult = event1.getContext().compareToIgnoreCase(event2.getContext());
                        }
                        break;
                    case EventService.SORT_FIELD_SESSION_ID:
                        compareResult = event1.getSessionId().compareToIgnoreCase(event2.getSessionId());
                        break;
                    case EventService.SORT_FIELD_EVENT_CODE:
                        compareResult = event1.getCode().compareToIgnoreCase(event2.getCode());
                        break;
                }
            }

            return (sortOrder == EventService.SORT_ORDER_ASCENDING ? compareResult : -compareResult);
        }
    }
}