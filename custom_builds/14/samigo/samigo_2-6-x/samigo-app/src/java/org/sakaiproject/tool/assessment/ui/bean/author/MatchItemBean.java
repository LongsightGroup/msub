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


package org.sakaiproject.tool.assessment.ui.bean.author;


import java.io.Serializable;
public class MatchItemBean implements Serializable {

  private static final long serialVersionUID = 7526471155622776147L;

//  private String text;
  private Long sequence;
//  private String corrfeedback;
//  private String incorrfeedback;
  private Boolean isCorrect;
  private String choice;  //
  private String match;
  private String corrMatchFeedback;
  private String incorrMatchFeedback;
  private String sequenceStr;


  public MatchItemBean() {
	// sequence = -1 for new items
	sequence = new Long(-1);
	sequenceStr = "-1";
  }
/*
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
*/

  public Long getSequence() {
    return sequence;
  }

  public void setSequence(Long sequence) {
    this.sequence = sequence;
    this.sequenceStr = sequence.toString();
  }

// used by jsf to check if the current pair is for editing
  public String getSequenceStr() {
    return sequenceStr;
  }

  public void setSequenceStr(String param) {
    this.sequenceStr= param;
  }

/*
  public String getCorrfeedback() {
    return corrfeedback;
  }

  public void setCorrfeedback(String feedback) {
    this.corrfeedback= feedback;
  }

  public String getIncorrfeedback() {
    return incorrfeedback;
  }

  public void setIncorrfeedback(String feedback) {
    this.incorrfeedback= feedback;
  }

*/

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public String getChoice() {
    return choice;
  }
  public void setChoice(String param) {
    this.choice= param;
  }

  public String getMatch() {
    return match ;
  }
  public void setMatch(String param) {
    this.match= param;
  }

  public String getCorrMatchFeedback() {
    return corrMatchFeedback;
  }
  public void setCorrMatchFeedback(String param) {
    this.corrMatchFeedback= param;
  }

  public String getIncorrMatchFeedback() {
    return incorrMatchFeedback;
  }
  public void setIncorrMatchFeedback(String param) {
    this.incorrMatchFeedback= param;
  }



}
