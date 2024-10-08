/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.Date;

/**
 * This keeps track of the submission scheme, and the number allowed.
 *
 * @author Rachel Gollub
 */
public interface AssessmentAccessControlIfc
    extends java.io.Serializable
{
  // flag it when no editing on the property is desire
  public static Integer NO_EDIT = new Integer(-1);
  // timedAssessment
  public static Integer TIMED_ASSESSMENT = new Integer(1);
  public static Integer DO_NOT_TIMED_ASSESSMENT = new Integer(0);
  // autoSubmit
  public static Integer AUTO_SUBMIT = new Integer(1);
  public static Integer DO_NOT_AUTO_SUBMIT = new Integer(0);
  // autoSave
  public static Integer SAVE_ON_CLICK = new Integer(1);
  public static Integer AUTO_SAVE = new Integer(2);
  // itemNavigation
  public static Integer LINEAR_ACCESS = new Integer(1);
  public static Integer RANDOM_ACCESS = new Integer(2);
  // assessmentFormat
  public static Integer BY_QUESTION = new Integer(1);
  public static Integer BY_PART = new Integer(2);
  public static Integer BY_ASSESSMENT = new Integer(3);
  // itemNumbering
  public static Integer CONTINUOUS_NUMBERING = new Integer(1);
  public static Integer RESTART_NUMBERING_BY_PART = new Integer(2);
  //markForReview
  public static final Integer MARK_FOR_REVIEW = new Integer(1);
  public static final Integer NOT_MARK_FOR_REVIEW = new Integer(0);
  // submissionsAllowed
  public static Integer UNLIMITED_SUBMISSIONS_ALLOWED = new Integer(9999);
  public static Integer UNLIMITED_SUBMISSIONS = new Integer(1);
  public static Integer LIMITED_SUBMISSIONS = new Integer(0);
  // lateHandling
  public static Integer ACCEPT_LATE_SUBMISSION = new Integer(1);
  public static Integer NOT_ACCEPT_LATE_SUBMISSION = new Integer(2);
  
  // gopalrc - group release
  public static String RELEASE_TO_SELECTED_GROUPS = "Selected Groups";

  Long getId();

  void setId(Long id);

  void setAssessmentBase(AssessmentBaseIfc assessmentBase);

  AssessmentBaseIfc getAssessmentBase();

  Integer getSubmissionsAllowed();

  void setSubmissionsAllowed(Integer submissionsAllowed);

  Integer getSubmissionsSaved();

  void setSubmissionsSaved(Integer submissionsSaved);

  Integer getAssessmentFormat();

  void setAssessmentFormat(Integer assessmentFormat);

  Integer getBookMarkingItem();

  void setBookMarkingItem(Integer bookMarkingItem);

  Integer getTimeLimit();

  void setTimeLimit(Integer timeLimit);

  Integer getTimedAssessment();

  void setRetryAllowed(Integer retryAllowed);

  Integer getRetryAllowed();

  void setLateHandling(Integer lateHandling);

  Integer getLateHandling();

  void setTimedAssessment(Integer timedAssessment);

  Date getStartDate();

  void setStartDate(Date startDate);

  Date getDueDate();

  void setDueDate(Date dueDate);

  Date getScoreDate();

  void setScoreDate(Date scoreDate);

  Date getFeedbackDate();

  void setFeedbackDate(Date feedbackDate);

  Date getRetractDate();

  void setRetractDate(Date retractDate);

  void setAutoSubmit(Integer autoSubmit);

  Integer getAutoSubmit();

  void setItemNavigation(Integer itemNavigation);

  Integer getItemNavigation();

  void setItemNumbering(Integer itemNumbering);

  Integer getItemNumbering();

  void setSubmissionMessage(String submissionMessage);

  String getSubmissionMessage();

  String getReleaseTo();

  void setReleaseTo(String releaseTo);

  String getUsername();

  void setUsername(String username);

  String getPassword();

  void setPassword(String password);

  void setFinalPageUrl(String finalPageUrl);

  String getFinalPageUrl();

  Boolean getUnlimitedSubmissions();

  void setUnlimitedSubmissions(Boolean unlimitedSubmissions);

  Integer getMarkForReview();

  void setMarkForReview(Integer markForReview);
}
