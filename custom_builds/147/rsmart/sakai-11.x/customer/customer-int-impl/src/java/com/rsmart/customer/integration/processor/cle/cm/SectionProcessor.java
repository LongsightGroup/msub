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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.coursemanagement.api.Section;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SectionProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(SectionProcessor.class);


    public void processRow(String[] data, ProcessorState state) throws Exception {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Reconciling section " + eid);

        Section section = null;
        if(cmService.isSectionDefined(eid)) {
            section = updateSection(cmService.getSection(eid), data);
        } else {
            section = addSection(data);
        }

    }

    public String getProcessorTitle() {
        return "Section Processor";
    }



    public Section updateSection(Section section, String[] data) {
        if(log.isDebugEnabled()) log.debug("Updating Section + " + section.getEid());
        section.setTitle(data[1]);
        section.setDescription(data[2]);
        section.setCategory(data[3]);
        String parentId = (data[4] == null || data[4].length() == 0) ? null : data[4];
        if(parentId != null && cmService.isSectionDefined(data[4])) {
            section.setParent(cmService.getSection(data[4]));
        }
        // Note: There's no way to change the course offering.  This makes sense, though.

        if(cmService.isEnrollmentSetDefined(data[5])) {
            section.setEnrollmentSet(cmService.getEnrollmentSet(data[5]));
        }
        cmAdmin.updateSection(section);
        return section;
    }

    public Section addSection(String[] data) {
        String eid = data[0];
        if(log.isDebugEnabled()) log.debug("Adding Section + " + eid);
        String title = data[1];
        String description = data[2];
        String category = data[3];
        String parentSectionEid = (data[4] == null || data[4].length() == 0) ? null : data[4];
        String enrollmentSetEid = data[5];
        String courseOfferingEid = data[6];

        return cmAdmin.createSection(eid, title, description, category, parentSectionEid, courseOfferingEid, enrollmentSetEid);
    }

}