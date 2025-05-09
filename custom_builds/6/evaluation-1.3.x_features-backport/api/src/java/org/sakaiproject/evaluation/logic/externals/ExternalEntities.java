/**
 * $Id: ExternalEntities.java 48892 2008-05-12 13:04:07Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/api/src/java/org/sakaiproject/evaluation/logic/externals/ExternalEntities.java $
 * ExternalEntities.java - evaluation - May 12, 2008 11:26:35 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import java.io.Serializable;

import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles entity interactions
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalEntities {

   // ENTITIES

   /**
    * @return the URL directly to the main server portal this tool is installed in
    */
   public String getServerUrl();

   /**
    * Get a full URL to a specific entity inside our system,
    * if this entity has no direct URL then just provide a URL to the sakai server
    * 
    * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
    * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
    */
   public String getEntityURL(Serializable evaluationEntity);

   /**
    * Get a full URL to a specific entity inside our system using just the class type and id,
    * if this entity has no direct URL then just provide a URL to the sakai server
    * 
    * @param entityPrefix an ENTITY_PREFIX constant from an entity provider
    * @param entityId the unique id of this entity (from getId() or similar) (e.g. 123)
    * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
    */
   public String getEntityURL(String entityPrefix, String entityId);

   /**
    * Creates a Sakai entity event for any internal entity which is registered with Sakai,
    * does nothing if the passed in entity type is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
    */
   public void registerEntityEvent(String eventName, Serializable evaluationEntity);

   /**
    * Creates a Sakai entity event for any internal entity which is registered with Sakai,
    * does nothing if the passed in entity class is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param entityClass class type of the entity which this event pertains to
    * @param entityId unique id for the entity which this event pertains to
    */
   public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId);

}
