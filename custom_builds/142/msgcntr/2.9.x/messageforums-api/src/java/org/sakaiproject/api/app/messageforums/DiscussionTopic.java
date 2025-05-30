/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/DiscussionTopic.java $
 * $Id: DiscussionTopic.java 13692 2009-04-24 16:14:32Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.List;
 

public interface DiscussionTopic extends OpenTopic {

    public ActorPermissions getActorPermissions();

    public void setActorPermissions(ActorPermissions actorPermissions);

    public Boolean getConfidentialResponses();

    public void setConfidentialResponses(Boolean confidentialResponses);

    public DateRestrictions getDateRestrictions();

    public void setDateRestrictions(DateRestrictions dateRestrictions);

    public String getGradebook();

    public void setGradebook(String gradebook);

    public String getGradebookAssignment();

    public void setGradebookAssignment(String gradebookAssignment);

    public Integer getHourBeforeResponsesVisible();

    public void setHourBeforeResponsesVisible(Integer hourBeforeResponsesVisible);

    public List getLabels();

    public void setLabels(List labels);

    public Boolean getMustRespondBeforeReading();

    public void setMustRespondBeforeReading(Boolean mustRespondBeforeReading);
    
    public void addLabel(Label label);
    
    public void removeLabel(Label label);
}