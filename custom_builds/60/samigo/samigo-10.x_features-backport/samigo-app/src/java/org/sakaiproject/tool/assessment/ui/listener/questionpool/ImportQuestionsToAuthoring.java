/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */

public class ImportQuestionsToAuthoring implements ActionListener
{
  //private static Log log = LogFactory.getLog(ImportQuestionsToAuthoring.class);
  //private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    //log.info("ImportQuestionsToAuthoring:");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
    if (!importItems(qpoolbean))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }
    qpoolbean.setImportToAuthoring(false);

  }


  public boolean importItems(QuestionPoolBean qpoolbean){
    try {
      ArrayList destItems = ContextUtil.paramArrayValueLike("importCheckbox");
      if (destItems.size() > 0) {
      AssessmentService assessdelegate = new AssessmentService();
      ItemService delegate = new ItemService();
      SectionService sectiondelegate = new SectionService();
      AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
      ItemAuthorBean itemauthor = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
      int itempos= 0;
      SectionFacade section = null;
      ItemFacade itemfacade = null;
      boolean newSectionCreated = false;
      
        // SAM-2395 - sort based on question text
        TreeSet<ItemFacade> sortedQuestions = new TreeSet<ItemFacade>( new Comparator<ItemFacade>() {
          @Override
          public int compare(ItemFacade obj1, ItemFacade obj2) {
              return obj1.getText().compareTo(obj2.getText());
          }
      });

        // SAM-2395 - copy the questions into a sorted list
        for (Object itemID : destItems) {
          ItemFacade poolItemFacade = delegate.getItem(Long.valueOf((String) itemID), AgentFacade.getAgentString());
          ItemData clonedItem = delegate.cloneItem( poolItemFacade.getData() );
        clonedItem.setItemId(Long.valueOf(0));
        clonedItem.setItemIdString("0");
        itemfacade = new ItemFacade(clonedItem);
          sortedQuestions.add(itemfacade);
        }

        // SAM-2395 - iterate over the sorted list
        Iterator iter = sortedQuestions.iterator();
        while (iter.hasNext()) {
        // path instead. so we will fix it here
          itemfacade = (ItemFacade) iter.next();
        setRelativePathInAttachment(itemfacade.getItemAttachmentList());
          if ("-1".equals(qpoolbean.getSelectedSection())) {
        	  if (!newSectionCreated) {
        		  // add a new section
        		  section = assessdelegate.addSection(assessmentBean.getAssessmentId());
        		  newSectionCreated = true;
        	  }
          }
          else {
        	  section = sectiondelegate.getSection(Long.valueOf(qpoolbean.getSelectedSection()), AgentFacade.getAgentString());
          }

        if (section!=null) {
          itemfacade.setSection(section);
              if ((itemauthor.getInsertPosition() == null) || ("".equals(itemauthor.getInsertPosition()))) {
                if (newSectionCreated) {
                  itemfacade.setSequence(itempos + 1);
              } else {
                  // if adding to the end
                if (section.getItemSet() != null) {
                    itemfacade.setSequence(section.getItemSet().size() + 1);
                } else {
                    // this is a new part 
                    itemfacade.setSequence(1);
                }
              }
          } else {
                // if inserting or a question
              ItemAddListener itemAddListener = new ItemAddListener();
                int insertPosIntvalue = Integer.valueOf(itemauthor.getInsertPosition()) + itempos;
                itemAddListener.shiftSequences(delegate, section, insertPosIntvalue);
                itemfacade.setSequence(insertPosIntvalue + 1);
          }
          
              delegate.saveItem(itemfacade);
          // remove POOLID metadata if any,
              delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.POOLID, AgentFacade.getAgentString());
              delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, AgentFacade.getAgentString());
              delegate.addItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID,section.getSectionId().toString(), AgentFacade.getAgentString());
            }

         itempos++;   // for next item in the destItem.
      }
      
      // reset InsertPosition
      itemauthor.setInsertPosition("");
      itemauthor.setItemTypeString("");
      // reset checkbox, otherwise if the last question is checked, it stays checked. 
      String[] emptyArr= {};	
      qpoolbean.setDestItems(emptyArr);

   //TODO need to reset assessments.
      AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
      assessmentBean.setAssessment(assessment);

      qpoolbean.setOutcome("editAssessment");
      }
      else {
      // nothing is checked
      qpoolbean.setOutcome("editPool");
      }
    }
    catch (RuntimeException e) {
	e.printStackTrace();
	return false;
    }
    return true;
  }

  private void setRelativePathInAttachment(List attachmentList){
    for (int i=0; i<attachmentList.size();i++){
      AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
      String url = ContextUtil.getRelativePath(attach.getLocation());
      attach.setLocation(url);
    }
  }


}
