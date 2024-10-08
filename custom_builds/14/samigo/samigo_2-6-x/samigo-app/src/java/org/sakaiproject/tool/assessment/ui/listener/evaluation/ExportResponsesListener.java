/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/evaluation/TotalScoreListener.java $
 * $Id: TotalScoreListener.java 30029 2007-05-04 15:41:31Z ktsao@stanford.edu $
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

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ExportResponsesBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>
 * This handles the selection of the Total Score entry page.
 *  </p>
 * <p>Description: Action Listener for Evaluation Total Score front door</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: TotalScoreListener.java 30029 2007-05-04 15:41:31Z ktsao@stanford.edu $
 */

public class ExportResponsesListener
  implements ActionListener
{
  private static Log log = LogFactory.getLog(ExportResponsesListener.class);

  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws
    AbortProcessingException
  {
    log.debug("ExportResponsesListener Action Listener.");
    TotalScoresBean totalScoreBean = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
    ExportResponsesBean exportResponsesBean = (ExportResponsesBean) ContextUtil.lookupBean("exportResponses");
    exportResponsesBean.setAssessmentId(totalScoreBean.getPublishedId());
    exportResponsesBean.setAssessmentName(totalScoreBean.getAssessmentName());
    exportResponsesBean.setAnonymous(Boolean.valueOf(totalScoreBean.getAnonymous()).booleanValue());
  }
}

