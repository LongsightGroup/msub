package com.rsmart.supporttool.api;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SupportBean {

    protected String type, ref, context;
    protected Date startDate = new Date();
    protected Date endDate = new Date();
    protected int startIndex, pageSize, count;

    public SupportBean(){
        // initialize calendar
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE,0);
        calendar.add(Calendar.HOUR,1);

        setEndDate(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH,-1);
        setStartDate(calendar.getTime());

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

 
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isNotEmpty(){
        return notEmpty();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean notEmpty(){
        if (type != null && type.length() > 0){
            return true;
        }
        if (context != null && context.length() > 0){
            return true;
        }
        if (ref != null && ref.length() > 0){
            return true;
        }
        if (startDate != null){
            return true;
        }
        if (endDate != null){
            return true;
        }
        return false;
    }
}
