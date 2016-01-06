package com.rsmart.admin.customizer.tool;

import org.sakaiproject.messagebundle.api.MessageBundleService;
import org.sakaiproject.messagebundle.api.MessageBundleProperty;
import com.rsmart.sakai.common.web.listfilter.PagedViewListController;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static java.util.Collections.sort;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 17, 2010
 * Time: 9:54:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class MessageBundleListController extends PagedViewListController {

    private MessageBundleService messageBundleService;

    public Map referenceData(HttpServletRequest httpServletRequest) {
        Map map = super.referenceData(httpServletRequest);
        if (map == null) {
            map = new HashMap();
        }
        if (httpServletRequest.getParameter("msg") != null) {
            map.put("msg", httpServletRequest.getParameter("msg") );
        }
        map.put("load.bundles.from.db", ServerConfigurationService.getBoolean("load.bundles.from.db", false));
        map.put("modules",messageBundleService.getAllModuleNames());
        map.put("baseNames",messageBundleService.getAllBaseNames());
        map.put("locales",messageBundleService.getLocales());

        SearchBean searchBean = (SearchBean) httpServletRequest.getSession().getAttribute("searchBean");
        if (searchBean == null) {
            searchBean = new SearchBean();
            httpServletRequest.getSession().setAttribute("searchBean", searchBean);
        }
        map.put("searchBean", searchBean);

        return map;
    }

    public void setMessageBundleService(MessageBundleService messageBundleService) {
        this.messageBundleService = messageBundleService;
    }

     public List getFilterNames() {
        return messageBundleService.getAllModuleNames();
    }


    protected int getListSize(HttpServletRequest request) {
        SearchBean searchBean = (SearchBean) request.getSession().getAttribute("searchBean");
        if (searchBean != null && searchBean.notEmpty()) {
            return messageBundleService.getSearchCount(searchBean.getSearch(), searchBean.getModule(),
                    searchBean.getBaseName(), searchBean.getLocale());
        }
        return messageBundleService.getModifiedPropertiesCount();
    }

    public ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        bindParameters(httpServletRequest);
        return super.handleRequestInternal(httpServletRequest, httpServletResponse);
    }

    protected List getList(HttpServletRequest httpServletRequest, int startingIndex, int pageSize) {

        SearchBean searchBean = bindParameters(httpServletRequest);

        int    sortOrder = isAscendingOrder() ? MessageBundleService.SORT_ORDER_ASCENDING : MessageBundleService.SORT_ORDER_DESCENDING;
        int    sortField  = MessageBundleService.SORT_FIELD_ID;
        if ("module".equals(getCurrentSortCol())) {
                sortField = MessageBundleService.SORT_FIELD_MODULE;
        }
        if ("property".equals(getCurrentSortCol())) {
                sortField = MessageBundleService.SORT_FIELD_PROPERTY;
        }
        if ("locale".equals(getCurrentSortCol())) {
                sortField = MessageBundleService.SORT_FIELD_LOCALE;
        }
        if ("baseName".equals(getCurrentSortCol())) {
                sortField = MessageBundleService.SORT_FIELD_BASENAME;
        }

        if (searchBean.notEmpty()) {
            List list = messageBundleService.search(searchBean.getSearch(), searchBean.getModule(), searchBean.getBaseName(), searchBean.getLocale());
            sort(list, new MessageBundleComparator(sortOrder, sortField));
            List subList = indexList(list, startingIndex, pageSize, list.size());
            return subList;
        }

        return messageBundleService.getModifiedProperties(sortOrder, sortField, startingIndex, pageSize);
    }
    
    protected SearchBean bindParameters(HttpServletRequest httpServletRequest) {
        SearchBean searchBean = (SearchBean) httpServletRequest.getSession().getAttribute("searchBean");
        if (searchBean == null) {
            searchBean = new SearchBean();
        }

        if (httpServletRequest.getParameter("searchLocale") != null && httpServletRequest.getParameter("searchLocale").length() > 0)
            searchBean.setLocale(httpServletRequest.getParameter("searchLocale"));

        String filterName = getFilterName(httpServletRequest);
        searchBean.setModule("-1".equals(filterName) ? null : filterName);

        if (httpServletRequest.getParameter("search") != null && httpServletRequest.getParameter("search").length() > 0)
            searchBean.setSearch(httpServletRequest.getParameter("search"));

        if (httpServletRequest.getParameter("_clear") != null){
            searchBean = new SearchBean();
        }

        httpServletRequest.getSession().setAttribute("searchBean", searchBean);
        return searchBean;
    }

    class MessageBundleComparator implements Comparator {
        int sortField;
        int sortOrder;

        public MessageBundleComparator(int sortOrder, int sortField) {
            this.sortField = sortField;
            this.sortOrder = sortOrder;
        }

        public int compare(Object o1, Object o2) {
            int result = 0;

            MessageBundleProperty mbp1 = (MessageBundleProperty) o1;
            MessageBundleProperty mbp2 = (MessageBundleProperty) o2;
            if (mbp1 == null && mbp2 == null) result = 0;
            else if (mbp1 == null && mbp2 != null) result = 1;
            else if (mbp1 != null && mbp2 == null) result = -1;
            else {
                switch (sortField) {
                    case MessageBundleService.SORT_FIELD_BASENAME:
                        result = mbp1.getBaseName().compareTo(mbp2.getBaseName());
                        break;
                    case MessageBundleService.SORT_FIELD_LOCALE:
                        result = mbp1.getLocale().compareTo(mbp2.getLocale());
                        break;
                    case MessageBundleService.SORT_FIELD_MODULE:
                        result = mbp1.getModuleName().compareTo(mbp2.getModuleName());
                        break;
                    case MessageBundleService.SORT_FIELD_PROPERTY:
                        result = mbp1.getPropertyName().compareTo(mbp2.getPropertyName());
                        break;
                    case MessageBundleService.SORT_FIELD_ID:
                        result = mbp1.getId().compareTo(mbp2.getId());
                        break;

                }
            }
            return (sortOrder == MessageBundleService.SORT_ORDER_ASCENDING ? result : -result);
        }
    }
}
