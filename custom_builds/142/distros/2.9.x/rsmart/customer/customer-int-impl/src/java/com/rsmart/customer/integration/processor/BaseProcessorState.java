package com.rsmart.customer.integration.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Sep 28, 2010
 * Time: 9:34:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseProcessorState
    implements ProcessorState
{
    private HashMap
        configuration = new HashMap();

    private Date
        /** Start Date */
        startDate,
        /** End Date */
        endDate;

    private int
        /** Record Cnt */
        recordCnt = 0,
        /** Error Cnt */
        errorCnt = 0,
        /** Update Cnt */
        updateCnt = 0,
        /** Insert Cnt */
        insertCnt = 0,
        /** Insert Cnt */
        ignoreCnt = 0,
        /** Delete Cnt */
        deleteCnt = 0,
        /** Processed Cnt */
        processedCnt = 0;

    /** Error List */
    private List<String>
        errorList = new ArrayList<String>();

    public void reset()
    {
        startDate = null;
        endDate = null;
        recordCnt = 0;
        errorCnt = 0;
        updateCnt = 0;
        insertCnt = 0;
        ignoreCnt = 0;
        deleteCnt = 0;
        processedCnt = 0;

        errorList = new ArrayList<String>();

        configuration = new HashMap();
    }

    public void clearErrorList()
    {
        errorList.clear();
    }

    /**
     * Get End Date
     *
     * @return Date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set End Date
     *
     * @param endDate
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Get Start Date
     *
     * @return Date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set Start Date
     *
     * @param startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get Error Count
     *
     * @return int
     */
    public int getErrorCnt() {
        return errorCnt;
    }

    /**
     * Set Error Count
     *
     * @param errorCnt
     */
    public void setErrorCnt(int errorCnt) {
        this.errorCnt = errorCnt;
    }

    /**
     * Increment Error Cnt
     *
     */
    public void incrementErrorCnt() {
        this.errorCnt++;
    }

    /**
     * Get Processed Count
     *
     * @return int
     */
    public int getProcessedCnt() {
        return processedCnt;
    }

    /**
     * Set Processed Count
     *
     * @param processedCnt
     */
    public void setProcessedCnt(int processedCnt) {
        this.processedCnt = processedCnt;
    }

    /**
     * Increment Processed Count
     */
    public void incrementProcessedCnt() {
        this.processedCnt++;
    }

    /**
     * Get Record Coutn
     *
     * @return int
     */
    public int getRecordCnt() {
        return recordCnt;
    }

    /**
     * Set Record Count
     *
     * @param recordCnt
     */
    public void setRecordCnt(int recordCnt) {
        this.recordCnt = recordCnt;
    }

    /**
     * Increment Record Cnt
     *
     */
    public void incrementRecordCnt() {
        this.recordCnt++;
    }

    /**
     * Get Ignore Cnt
     *
     * @return int
     */
    public int getIgnoreCnt() {
        return ignoreCnt;
    }

    /**
     * Set Ignore Cnt
     *
     * @param ignoreCnt
     */
    public void setIgnoreCnt(int ignoreCnt) {
        this.ignoreCnt = ignoreCnt;
    }

    /**
     * Increment Ignore Cnt
     *
     */
    public void incrementIgnoreCnt() {
        this.ignoreCnt++;
    }

    /**
     * Get Insert Count
     *
     * @return int
     */
    public int getInsertCnt() {
        return insertCnt;
    }

    /**
     * Set Insert Cnt
     *
     * @param insertCnt
     */
    public void setInsertCnt(int insertCnt) {
        this.insertCnt = insertCnt;
    }

    /**
     * Increment Insert Cnt
     *
     */
    public void incrementInsertCnt() {
        this.insertCnt++;
    }

    /**
     * Get Update Cnt
     *
     * @return int
     */
    public int getUpdateCnt() {
        return updateCnt;
    }

    /**
     * Set Update Cnt
     *
     * @param updateCnt
     */
    public void setUpdateCnt(int updateCnt) {
        this.updateCnt = updateCnt;
    }

    /**
     * Set Update Cnt
     *
     */
    public void incrementUpdateCnt() {
        this.updateCnt++;
    }

    /**
     * Get Delete Cnt
     *
     * @return
     */
    public int getDeleteCnt() {
        return deleteCnt;
    }

    /**
     * Set Delete Cnt
     *
     * @param deleteCnt
     */
    public void setDeleteCnt(int deleteCnt) {
        this.deleteCnt = deleteCnt;
    }

    /**
     * Increment Delete Cnt
     *
     */
    public void incrementDeleteCnt() {
        this.deleteCnt++;
    }

    /**
     * Append Error to List
     *
     * @param txt
     */
    public void appendError(String txt) {
        errorList.add(txt);
    }

    /**
     *
     * @return
     */
    public List getErrorList() {
        return errorList;
    }

    /**
     *
     * @param errorList
     */
    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

    public void setConfiguration (Map c)
    {
        configuration.clear();

        if (c != null)
        {
            configuration.putAll(c);
        }
    }

    public Map getConfiguration()
    {
        return configuration;
    }
}
