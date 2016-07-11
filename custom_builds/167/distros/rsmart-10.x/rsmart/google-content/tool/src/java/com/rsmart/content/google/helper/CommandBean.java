/*
 * Copyright 2011 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Contributor(s): duffy
 */

package com.rsmart.content.google.helper;

import com.rsmart.content.google.api.GoogleDocDescriptor;
import org.sakaiproject.metaobj.shared.model.DateBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.text.DateFormat;

/**
 * Created by IntelliJ IDEA.
* User: duffy
* Date: Apr 5, 2010
* Time: 9:18:05 AM
* To change this template use File | Settings | File Templates.
*/
public class CommandBean
{
    private static Log
        LOG         = LogFactory.getLog(CommandBean.class);

    private GoogleDocDescriptor
        docDescriptor;
    private boolean
        releaseSelected = false,
        retractSelected = false,
        showSelected = true,
        copyrightAlertSelected = false;
    private String
        description,
        copyright;
    private Date
        startDate,
        endDate;
    private DateBean
        startDateBean = new DateBean(new Date()),
        endDateBean = new DateBean(new Date());


    public CommandBean()
    {
        docDescriptor = new GoogleDocDescriptor();
    }

    public void setDocDescriptor (GoogleDocDescriptor gdd)
    {
        docDescriptor = gdd;
    }

    public GoogleDocDescriptor getDocDescriptor()
    {
        return docDescriptor;
    }

    public void setDocId (String id)
    {
        docDescriptor.setDocId(id);
    }

    public String getDocId ()
    {
        return docDescriptor.getDocId();
    }

    public void setExportType (String exType)
    {
        docDescriptor.setExportType(exType);
    }

    public String getExportType ()
    {
        return docDescriptor.getExportType();
    }

    public void setLink (String l)
    {
        docDescriptor.setLink(l);
    }

    public String getLink ()
    {
        return docDescriptor.getLink();
    }

    public void setOwnerId (String oId)
    {
        docDescriptor.setOwnerId (oId);
    }

    public String getOwnerId ()
    {
        return docDescriptor.getOwnerId();
    }

    public String getResourceId ()
    {
        return docDescriptor.getResourceId();
    }

    public void setTitle (String t)
    {
        docDescriptor.setTitle (t);
    }

    public String getTitle ()
    {
        return docDescriptor.getTitle();
    }

    public void setType (String t)
    {
        docDescriptor.setType (t);
    }

    public String getType ()
    {
        return docDescriptor.getType();
    }

    public void setVersionId (String vId)
    {
        docDescriptor.setVersionId (vId);
    }

    public String getVersionId()
    {
        return docDescriptor.getVersionId();
    }

    public void setWorksheet (int ws)
    {
        docDescriptor.setWorksheet (ws);
    }

    public int getWorksheet()
    {
        return docDescriptor.getWorksheet();
    }

    public int getWorksheetCount()
    {
        return docDescriptor.getWorksheetCount();
    }

    public void setWorksheetCount (int c)
    {
        docDescriptor.setWorksheetCount(c);
    }
    
    public boolean isReleaseSelected()
    {
        return releaseSelected;
    }

    public void setReleaseSelected(boolean releaseSelected)
    {
        this.releaseSelected = releaseSelected;
    }

    public boolean isRetractSelected()
    {
        return retractSelected;
    }

    public void setRetractSelected(boolean retractSelected)
    {
        this.retractSelected = retractSelected;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        LOG.debug("setDescription()");
        this.description = description;
    }

    public boolean isShowSelected() {
        return showSelected;
    }

    public void setShowSelected(boolean showSelected) {
        LOG.debug("setShowSelected()");

        this.showSelected = showSelected;
    }

    public boolean isCopyrightAlertSelected() {
        return copyrightAlertSelected;
    }

    public void setCopyrightAlertSelected(boolean copyrightAlertSelected) {
        LOG.debug("setCopyrightAlertSelected()");

        this.copyrightAlertSelected = copyrightAlertSelected;
    }

    /**
    * returns the date bean corresponding to the model's start date.
    */
    public DateBean getStartDateBean() {
        LOG.debug ("getStartDateBean()");
        return startDateBean;
    }

    /**
    * sets the date bean corresponding to the model's start date.
    */
    public void setStartDate(Date meetingStartDate)
    {
        if (LOG.isDebugEnabled())
        {
            DateFormat
                df = DateFormat.getDateInstance();

            LOG.debug ("setting start date: " + df.format(meetingStartDate));
        }
        this.startDateBean = new DateBean(meetingStartDate);
        this.startDate = meetingStartDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    /**
    * sets the date bean corresponding to the model's start date.
    */
    public void setStartDateBean(DateBean meetingStartDateBean) {
        LOG.debug ("setStartDateBean()");
        this.startDateBean = meetingStartDateBean;
    }

    /**
    * sets the date bean corresponding to the model's end date.
    */
    public void setEndDate(Date meetingEndDate)
    {
        LOG.debug("setEndDate()");
        if (LOG.isDebugEnabled())
        {
            DateFormat
                df = DateFormat.getDateInstance();

            LOG.debug ("setting end date: " + df.format(meetingEndDate));
        }
        this.endDate = meetingEndDate;
        this.endDateBean = new DateBean(meetingEndDate);
    }

    public Date getEndDate()
    {
        return endDate;
    }

    /**
    * sets the date bean corresponding to the model's end date.
    */
    public void setEndDateBean(DateBean meetingEndDateBean) {
        LOG.debug ("setStartDateBean()");
        this.endDateBean = meetingEndDateBean;
    }

    /**
    * returns the date bean corresponding to the model's end date.
    */
    public DateBean getEndDateBean() {
        return endDateBean;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }


}
