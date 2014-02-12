/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/assignment2/tags/1.0/tool/src/java/org/sakaiproject/assignment2/tool/beans/Assignment2Validator.java $
 * $Id: Assignment2Validator.java 70412 2010-09-24 21:47:24Z swgithen@mtu.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment2.tool.beans;

import org.sakaiproject.assignment2.model.Assignment2;
import org.sakaiproject.assignment2.model.constants.AssignmentConstants;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This holds Validation utilities for the Assignments objects, so the checks
 * are in one place. This is mostly used for form verification in the GUI,
 * so an error message can be thrown up if the user forgets something like
 * the assignment title.
 * 
 * This uses the standard RSF error handling, so any errors it finds are added
 * to the targetted message list which includes the human readable message
 * keys to present to the user.
 * 
 * @author rjlowe
 * @author sgithens
 *
 */
public class Assignment2Validator  {

    /**
     * Validates the Assignment2 object. Currently checks to make sure there
     * is a title, some grading constraints, and some due date and accept date
     * constraints. Also checks Turnitin options, if enabled.
     * 
     * @param assignment The assignment to validate.
     * @param messages The message list to add any error/success messages to.
     * @return Whether or not this assignment object passed validation.
     */
    public boolean validate(Assignment2 assignment, TargettedMessageList messages) {

        boolean valid = true;
        String key = "";
        if (assignment.getId() == null){
            key = "new 1";
        } else {
            key = assignment.getId().toString();
        }


        //check for empty title
        if (assignment.getTitle() == null || assignment.getTitle().equals("")) {
            messages.addMessage(new TargettedMessage("assignment2.assignment_title_empty",
                    new Object[] { assignment.getTitle() }, "Assignment2." + key + ".title"));
            valid = false;
        }

        //check for graded but no gradebookItemId
        if (assignment.isGraded() && (assignment.getGradebookItemId() == null || 
                assignment.getGradebookItemId().longValue() < 1)) {
            messages.addMessage(new TargettedMessage("assignment2.assignment_graded_no_gb_item", 
                    new Object[] {}, "Assignment2."+ key + ".gradebookItemId"));
            valid = false;
        }

        // check for due date after open date
        if (assignment.getDueDate() != null && assignment.getOpenDate() != null
                && assignment.getDueDate().before(assignment.getOpenDate())) {
            messages.addMessage(new TargettedMessage("assignment2.assignment_due_before_open"));
            valid = false;
        }

        if (assignment.getAcceptUntilDate() != null && assignment.getOpenDate() != null
                && assignment.getAcceptUntilDate().before(assignment.getOpenDate())) {
            messages.addMessage(new TargettedMessage("assignment2.assignment_accept_before_open"));
            valid = false;
        }

        // check for due date before or equal to accept until
        if (assignment.getDueDate() != null && assignment.getAcceptUntilDate() != null
                && assignment.getAcceptUntilDate().before(assignment.getDueDate())) {
            messages.addMessage(new TargettedMessage("assignment2.assignment_accept_before_due"));
            valid = false;
        }
        
        // Validate the turnitin options ASNN-516
        if (assignment.isContentReviewEnabled()) {     
            // check to see if the user wants to generate reports related to due date
            // but there is no due date
            if (assignment.getProperties().containsKey("report_gen_speed") && 
                    ("2".equals(assignment.getProperties().get("report_gen_speed")) ||
                     "1".equals(assignment.getProperties().get("report_gen_speed")))) {
                if (assignment.getDueDate() == null) {
                    messages.addMessage(new TargettedMessage("assignment2.turnitin.asnnedit.error.due_date"));
                    valid = false;
                }
            }
            
            // make sure the user has selected at least one option to check against
            boolean checkOptionSelected = false;
            if ((assignment.getProperties().containsKey("s_paper_check") && (Boolean) assignment.getProperties().get("s_paper_check")) ||
                    (assignment.getProperties().containsKey("internet_check") && (Boolean) assignment.getProperties().get("internet_check")) ||
                    (assignment.getProperties().containsKey("journal_check") && (Boolean) assignment.getProperties().get("journal_check")) ||
                    (assignment.getProperties().containsKey("institution_check") && (Boolean) assignment.getProperties().get("institution_check"))) {
                checkOptionSelected = true;
            } 
            
            if (!checkOptionSelected) {
                messages.addMessage(new TargettedMessage("assignment2.turnitin.asnnedit.error.check_against"));
                valid = false;
            }
        }
        
        // model answer stuff
        if (assignment.isModelAnswerEnabled())
        {
            if ((assignment.getModelAnswerText()==null || "".equals(assignment.getModelAnswerText().trim())) && (assignment.getModelAnswerAttachmentSet()==null || assignment.getModelAnswerAttachmentSet().isEmpty()))
            {
                messages.addMessage(new TargettedMessage("assignment2.model_answer_empty"));
                valid = false;
            }
            int madr = assignment.getModelAnswerDisplayRule();
            if (!assignment.isRequiresSubmission())
            {
                if (madr!=AssignmentConstants.MODEL_NEVER && madr!=AssignmentConstants.MODEL_IMMEDIATELY)
                {
                    messages.addMessage(new TargettedMessage("assignment2.model_answer_invalid_submission"));
                    valid = false;
                }
            }
            if (assignment.getDueDate()==null && madr==AssignmentConstants.MODEL_AFTER_DUE_DATE)
            {
                messages.addMessage(new TargettedMessage("assignment2.model_answer_invalid_due_date"));
                valid = false;
            }
            if (assignment.getAcceptUntilDate()==null && madr==AssignmentConstants.MODEL_AFTER_ACCEPT_DATE)
            {
                messages.addMessage(new TargettedMessage("assignment2.model_answer_invalid_accept_date"));
                valid = false;
            }
        }

        return valid;
    }

}
