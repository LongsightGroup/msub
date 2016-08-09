/*
 * Copyright 2008 The rSmart Group
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
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.processor.cle.cm;

import com.rsmart.customer.integration.processor.ProcessorState;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class AcademicSessionProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(AcademicSessionProcessor.class);


    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if(cmService.isAcademicSessionDefined(eid)) {
            updateAcademicSession(cmService.getAcademicSession(eid), data);
        } else {
            addAcademicSession(data);
        }
    }

    public String getProcessorTitle() {
        return "AcademicSession processor";
    }

    public void addAcademicSession(String[] data) {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Adding AcademicSession + " + eid);
        String title = data[1];
        String description = data[2];
        Date startDate = getDate(data[3]);
        Date endDate = getDate(data[4]);
        AcademicSession session = cmAdmin.createAcademicSession(eid, title, description, startDate, endDate);
        setCurrentStatus(session);
    }

    public void updateAcademicSession(AcademicSession session, String[] data) {
        if(log.isDebugEnabled()) log.debug("Updating AcademicSession + " + session.getEid());
        session.setTitle(data[1]);
        session.setDescription(data[2]);
        session.setStartDate(getDate(data[3]));
        session.setEndDate(getDate(data[4]));
        cmAdmin.updateAcademicSession(session);
        setCurrentStatus(session);        
    }

    private void setCurrentStatus(AcademicSession session) {
        List<AcademicSession> currentSessions = cmService.getCurrentAcademicSessions();
        List<String> currentTerms = new ArrayList<String>();

       // initialize the array with the current sessions
       for (AcademicSession s: currentSessions) {
                currentTerms.add(s.getEid());
        }

           // add this session if its end date is after today
        if (session.getEndDate().after(new Date())) {
               if (!currentTerms.contains(session.getEid().toString())) {
                       currentTerms.add(session.getEid());
               }
        // otherwise remove this session
        } else {
            if (currentTerms.contains(session.getEid().toString())) {
                    currentTerms.remove(session.getEid().toString());
            }
        }
        cmAdmin.setCurrentAcademicSessions(currentTerms);
    }
}