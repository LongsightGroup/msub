/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.rsf.bean;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.rsf.params.BeginAssessmentViewParameters;
import org.sakaiproject.tool.assessment.rsf.producers.BeginAssessmentProducer;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Entity View Paramater Inferrer for samigo PublishedAssessments
 * 
 * @author Joshua Ryan  josh@asu.edu  alt^I
 *
 */
public class PublishedAssessmentVPInferrer implements EntityViewParamsInferrer {

  public String[] getHandledPrefixes() {
    return new String[] {PublishedAssessmentEntityProvider.ENTITY_PREFIX};
  }

  public ViewParameters inferDefaultViewParameters(String reference) {
    BeginAssessmentViewParameters params = new BeginAssessmentViewParameters();
    IdEntityReference ep = new IdEntityReference(reference);
    params.pubReference = ep.id;
    params.viewID = BeginAssessmentProducer.VIEW_ID;
    return params;
  }
}
