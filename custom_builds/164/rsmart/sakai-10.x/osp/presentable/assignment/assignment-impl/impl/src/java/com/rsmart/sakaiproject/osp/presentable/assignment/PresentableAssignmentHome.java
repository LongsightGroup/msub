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

import org.jdom.Element;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.metaobj.security.AllowMapSecurityAdvisor;
import org.sakaiproject.metaobj.shared.ArtifactFinder;
import org.sakaiproject.metaobj.shared.mgt.IdManager;
import org.sakaiproject.metaobj.shared.mgt.PresentableObjectHome;
import org.sakaiproject.metaobj.shared.mgt.ReadableObjectHome;
import org.sakaiproject.metaobj.shared.model.*;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Dec 18, 2007
 * Time: 10:56:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class PresentableAssignmentHome implements ReadableObjectHome, ArtifactFinder,
            PresentableObjectHome {

   private PresentableObjectHome xmlRenderer;
   private AssignmentService assignmentService;
   private SiteService siteService;
   private UserDirectoryService userDirectoryService;
   private TaggingManager taggingManager;
   private IdManager idManager;
   private SecurityService securityService;


   public Type getType() {
      return new org.sakaiproject.metaobj.shared.model.Type(getIdManager().getId("baseAssignment"), "assignment");
   }

   /**
    * Used to get an externally unique type to identify this home
    * across running osp instances
    *
    * @return an externally unique type suitable for storage and later import
    */
   public String getExternalType() {
      return null;  //not needed
   }

   /**
    * Load the object from the
    * backing store.
    *
    * @param id Uniquely identifies the object.
    * @return The loaded object
    */
   public Artifact load(Id id) throws PersistenceException {
      getSecurityService().pushAdvisor(
         new AllowMapSecurityAdvisor(AssignmentService.SECURE_ACCESS_ASSIGNMENT, id.getValue()));
      PresentableSubmittedAssignment sa = getSubmittedAssignment(id);
      sa.setHome(this);
      return sa;
   }

   /**
    * Creates an empty instance of this home's object
    *
    * @return An empty object instance
    */
   public Artifact createInstance() {
      return null;  //not needed
   }

   public void prepareInstance(Artifact object) {
      //not needed
   }

   /**
    * Creates a sample instance of the
    * object with each field filled in with some
    * representative data.
    *
    * @return An object instance with sample data filled in.
    */
   public Artifact createSample() {
      return null;  //not needed
   }

   /**
    * Find all the instances of this home's
    * objects that are owned by the supplied owner.
    * How do we handle permissions here?
    *
    * @param owner The owner in question.
    * @return A list of objects.
    */
   public Collection findByOwner(Agent owner) throws FinderException {
      return null;  //not needed
   }

   /**
    * Determines if the supplied object is handled by this home.
    *
    * @param testObject the object to be tested.
    * @return true if the supplied object is handled by this home
    */
   public boolean isInstance(Artifact testObject) {
      return testObject instanceof PresentableSubmittedAssignment;
   }

   /**
    * re-initialize any configuration
    */
   public void refresh() {
      //not needed
   }

   public String getExternalUri(Id artifactId, String name) {
      return null;  //not needed
   }

   public InputStream getStream(Id artifactId) {
      return null;  //not needed
   }

   public boolean isSystemOnly() {
      return false;  //always false
   }

   public Class getInterface() {
      return null;  //not needed
   }

   /**
    * search for a list of artifacts in the system owner by owner and matching the given type
    *
    * @param owner
    * @param type
    * @return
    */
   public Collection findByOwnerAndType(Id owner, String type) {
      return findByOwner(owner);
   }

   public Collection findByOwnerAndType(Id owner, String type, MimeType mimeType) {
      return findByOwner(owner);
   }

   public Collection findByOwner(Id owner) {
     return getAllSubmissionsForUser(owner.getValue());
   }

   protected Collection getAllSubmissionsForUser(String owner) {
      List submissions = new ArrayList();
      User user = null;
      try {
         user = getUserDirectoryService().getUser(owner);
      } catch (UserNotDefinedException e) {
         throw new RuntimeException(e);
      }

      List<Site> sites = getSiteService().getSites(SiteService.SelectionType.ACCESS, null,
         null, null, SiteService.SortType.TITLE_ASC,
         null);
      
      for (Site site : sites) {
         Iterator assignments = getAssignmentService().getAssignmentsForContext(site.getId());
         while (assignments.hasNext()) {
            Assignment assignment = (Assignment) assignments.next();
            AssignmentSubmission submission = getAssignmentService().getSubmission(assignment.getId(), user);
            if (submission != null) {
               submissions.add(new PresentableSubmittedAssignment(submission, getTaggingManager()));
            }
         }
      }
      
      return submissions;
   }

   public Collection findByWorksiteAndType(Id worksiteId, String type) {
      return null;
   }

   public Collection findByWorksite(Id worksiteId) {
      return null;
   }

   public Collection findByType(String type) {
      return null;
   }

   /**
    * @return true if calls to find should actually load the artifacts
    */
   public boolean getLoadArtifacts() {
      return false;
   }

   public void setLoadArtifacts(boolean loadArtifacts) {
    
   }

   public Element getArtifactAsXml(Artifact art) {
      return getXmlRenderer().getArtifactAsXml(art);
   }

   /**
    * Add some additional information to the artifact to construct a reference like so:
    * /<container>/<site>/<context>/...
    * @param art
    * @param site
    * @param context
    * @param container
    * @return
    */
   public Element getArtifactAsXml(Artifact art, String container, String site, String context){
      return getXmlRenderer().getArtifactAsXml(art, container, site, context);
   }


   public PresentableObjectHome getXmlRenderer() {
      return xmlRenderer;
   }

   public void setXmlRenderer(PresentableObjectHome xmlRenderer) {
      this.xmlRenderer = xmlRenderer;
   }

   public AssignmentService getAssignmentService() {
      return assignmentService;
   }

   public void setAssignmentService(AssignmentService assignmentService) {
      this.assignmentService = assignmentService;
   }

   public SiteService getSiteService() {
      return siteService;
   }

   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   public UserDirectoryService getUserDirectoryService() {
      return userDirectoryService;
   }

   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }

   public TaggingManager getTaggingManager() {
      return taggingManager;
   }

   public void setTaggingManager(TaggingManager taggingManager) {
      this.taggingManager = taggingManager;
   }

   public IdManager getIdManager() {
      return idManager;
   }

   public void setIdManager(IdManager idManager) {
      this.idManager = idManager;
   }

   protected PresentableSubmittedAssignment getSubmittedAssignment(Id id){
      try {
         return new PresentableSubmittedAssignment(getAssignmentService().getSubmission(id.getValue()), getTaggingManager());
      } catch (IdUnusedException e) {
         throw new RuntimeException(e);
      } catch (PermissionException e) {
         throw new RuntimeException(e);
      }

   }

   public SecurityService getSecurityService() {
      return securityService;
   }

   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }
}
