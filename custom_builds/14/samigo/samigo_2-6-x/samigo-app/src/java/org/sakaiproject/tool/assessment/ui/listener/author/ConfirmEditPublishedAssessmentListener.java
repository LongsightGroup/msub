/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ConfirmEditPublishedAssessmentListener  implements ActionListener {
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		author.setIsEditPendingAssessmentFlow(false);
		String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        	"assessmentBean");
		assessmentBean.setAssessmentId(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.confirm_edit", "publishedAssessmentId=" + publishedAssessmentId, true));
	}
}
