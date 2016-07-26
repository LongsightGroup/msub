package com.rsmart.customer.integration.processor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 28, 2010
 * Time: 1:41:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProcessorState
{
    public void reset();
    
    public void appendError (String errorStr);

    public void setConfiguration(Map configuration);

    public Map getConfiguration();

    public void setDeleteCnt(int cnt);

    public int getDeleteCnt();

    public void setEndDate(Date end);

    public Date getEndDate();

    public void setErrorCnt(int cnt);

    public int getErrorCnt();

    public List getErrorList();

    public void clearErrorList();
    
    public void setIgnoreCnt(int cnt);

    public int getIgnoreCnt();

    public void setInsertCnt(int cnt);

    public int getInsertCnt();

    public void setProcessedCnt(int cnt);

    public int getProcessedCnt();

    public void setRecordCnt(int cnt);

    public int getRecordCnt();

    public void setStartDate(Date start);

    public Date getStartDate();

    public void setUpdateCnt(int cnt);

    public int getUpdateCnt();

    public void incrementDeleteCnt();

    public void incrementErrorCnt();

    public void incrementIgnoreCnt();

    public void incrementInsertCnt();

    public void incrementProcessedCnt();

    public void incrementRecordCnt();

    public void incrementUpdateCnt();
}
