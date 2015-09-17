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

package com.rsmart.sakaiproject.osp.presentable.assignment;

import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.taggable.api.*;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.gmt.api.GmtService;
import org.sakaiproject.gmt.api.Goal;
import org.sakaiproject.gmt.api.Link;
import org.sakaiproject.gmt.api.Rating;
import org.sakaiproject.metaobj.shared.mgt.ReadableObjectHome;
import org.sakaiproject.metaobj.shared.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 18, 2007
 * Time: 9:55:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class PresentableSubmittedAssignment extends IdentifiableObject implements Artifact {

    private AssignmentSubmission baseAssignmentSubmission;
    private ReadableObjectHome home;
    private TaggingManager taggingManager;
   private GmtService gmtService = null;
   
    public PresentableSubmittedAssignment(AssignmentSubmission submittedAssignment, 
                                          TaggingManager taggingManager) {
      setBaseAssignmentSubmission(submittedAssignment);
      setTaggingManager(taggingManager);
    }

   public TaggingManager getTaggingManager() {
      return taggingManager;
   }

   public void setTaggingManager(TaggingManager taggingManager) {
      this.taggingManager = taggingManager;
   }

    public Agent getOwner() {
        return null;
    }

    public ReadableObjectHome getHome() {
        return home;
    }

    public void setHome(ReadableObjectHome home) {
        this.home = home;
    }

    public String getDisplayName() {
        return this.baseAssignmentSubmission.getAssignment().getTitle();
    }

    public AssignmentSubmission getBaseAssignmentSubmission() {

        return baseAssignmentSubmission;
    }

    public void setBaseAssignmentSubmission(AssignmentSubmission submittedAssignment) {
        this.baseAssignmentSubmission = submittedAssignment;
    }

    public Id getId() {
        return new IdImpl(baseAssignmentSubmission.getId(), null);
    }

   public List getTags() throws PermissionException {
      List returned = new ArrayList();

      String ref = baseAssignmentSubmission.getAssignment().getReference();
      TaggableActivityProducer producer = getTaggingManager().findProducerByRef(
         ref);
      
      if (producer == null) {
         return returned;
      }
      
      for (TaggingProvider provider : getTaggingManager().getProviders()) {
         TaggableActivity activity = producer.getActivity(ref, provider);
         List<TaggableItem> items = producer.getItems(
            activity, baseAssignmentSubmission.getSubmitterIdString(), provider, true);
         
         TagList tags = provider.getTags(activity);   
         for (Tag tag : tags) {
            Map tagMap = new HashMap();
            returned.add(tagMap);
            for (TagColumn column : tags.getColumns()) {
               tagMap.put(column.getName(), tag.getField(column));
            }
            if (tag.getObject() instanceof Goal && getGmtService() != null) {
               List<Rating> ratings = new ArrayList();
               Goal goal = (Goal) tag.getObject();
               Link link = getGmtService().getLink(activity.getReference(), goal);
               for (TaggableItem item : items) {
                  ratings.add(getGmtService().getRating(item.getReference(), link));
               }
               tagMap.put("ratings", ratings);
            }
         }
      }
      
      return returned;
   }

   public GmtService getGmtService() {
      if (gmtService == null) {
         gmtService = (GmtService) ComponentManager.getInstance().get("org.sakaiproject.gmt.api.GmtService");
      }
      return gmtService;
   }

   public void setGmtService(GmtService gmtService) {
      this.gmtService = gmtService;
   }
}
