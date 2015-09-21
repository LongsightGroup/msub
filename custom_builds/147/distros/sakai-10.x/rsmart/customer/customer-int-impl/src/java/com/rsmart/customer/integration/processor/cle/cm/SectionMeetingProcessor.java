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
import org.sakaiproject.coursemanagement.api.Meeting;
import org.sakaiproject.coursemanagement.api.Section;
import java.util.*;



/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 29, 2008
 * Time: 4:55:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SectionMeetingProcessor extends AbstractCMProcessor {
    private static final Log log = LogFactory.getLog(SectionMeetingProcessor.class);
    private SectionHibernateHelperImpl sectionHibernateHelper;

    private final static Map getMeetings(ProcessorState state)
    {
        return (Map)state.getConfiguration().get("meetings");
    }

    public ProcessorState init(Map config)
    {
        ProcessorState
            state = super.init(config);

        state.getConfiguration().put("meetings", new HashMap());

        return state;
    }

    public void processRow(String[] data, ProcessorState state) throws Exception {
       CleMeeting meeting = new CleMeeting();
       meeting.setLocation(data[1]);
       meeting.setSectionEid(data[0]);
       meeting.setNotes(data[2]);
       addMeeting(meeting, state);
    }

    public void setSectionHibernateHelper(SectionHibernateHelperImpl sectionHibernateHelper) {
        this.sectionHibernateHelper = sectionHibernateHelper;
    }

    protected void addMeeting(CleMeeting meeting, ProcessorState state) {
        Map meetings = getMeetings(state);

       String sectionEid = meeting.getSectionEid();
        List meetingList = (List) meetings.get(sectionEid);
        if (meetingList == null) {
            meetingList = new ArrayList();
            meetings.put(sectionEid, meetingList);
        }
        meetingList.add(meeting);
    }

    public void preProcess(ProcessorState state) throws Exception
    {
        Map meetings = getMeetings(state);

        meetings.clear();
    }

    public void postProcess(ProcessorState state) throws Exception {
        Map meetings = getMeetings(state);

       log.debug("postProcess() " + meetings.size() + " sections to inspect");
      
       for (Iterator i= meetings.keySet().iterator(); i.hasNext();) {
          String sectionEid = (String)i.next();
          Section section = null;
          if(cmService.isSectionDefined(sectionEid)) {
              section = cmService.getSection(sectionEid);
          } else {
              log.error("can't add meeting no section with eid of " + sectionEid + " found");
             continue;
          }

          // there no way to remove meetings, so is brute force add
          // we are orphaning meeting records everytime we run this job, this is bad!          
         sectionHibernateHelper.deleteSectionMeetings(section);

         List currentMeetings = (List)meetings.get(sectionEid);
         Set meetingTimes = new HashSet();
         section.setMeetings(meetingTimes);

          for (Iterator j=currentMeetings.iterator();j.hasNext();) {             
             CleMeeting cleMeeting = (CleMeeting) j.next();
             Meeting meeting = cmAdmin.newSectionMeeting(sectionEid, cleMeeting.getLocation(), null, null, cleMeeting.getNotes());
             section.getMeetings().add(meeting);

          }
          cmAdmin.updateSection(section);

      }
    }

    public String getProcessorTitle() {
        return "Section Meeting Processor";
    }

    class CleMeeting {
      private String notes;
      private String location;
      private String sectionEid;


      public String getNotes() {
         return notes;
      }

      public void setNotes(String notes) {
         this.notes = notes;
      }

      public String getLocation() {
         return location;
      }

      public void setLocation(String location) {
         this.location = location;
      }

      public String getSectionEid() {
         return sectionEid;
      }

      public void setSectionEid(String sectionEid) {
         this.sectionEid = sectionEid;
      }


   }
}