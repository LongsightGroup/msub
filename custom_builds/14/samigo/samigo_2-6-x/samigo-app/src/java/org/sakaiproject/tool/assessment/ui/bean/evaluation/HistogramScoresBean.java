/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.bean.evaluation;
import org.sakaiproject.tool.assessment.ui.bean.util.Validator;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>$Id$</p>
 * Copyright: Copyright (c) 2003-2004
 * </p>
 *
 * Used to be org.navigoproject.ui.web.form.evaluation.HistogramScoresForm.java
 * </p>
 *
 * @author Huong Nguyen
 * @version 1.0
 */
public class HistogramScoresBean
  implements Serializable
{
  private String assessmentName;

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = 3465442692662950563L;
  private String assessmentId;
  private String publishedId;
  private String itemId;
  private String agent;
  private String hasNav;
  private String groupName;
  private String maxScore; // heighest score
  private String totalScore;
  private String totalPossibleScore; //total possible score
  private String adjustedScore;
  private String questionNumber;
  private String allSubmissions;
  private String partNumber;
  private Integer parts;
  private String mean;
  private String median;
  private String mode;
  private String highestRange;
  private String standDev;
  private String lowerQuartile; //medidan of lowest-median
  private String upperQuartile; //median of median-highest
  private int interval; // number interval breaks down
  private Collection info;
  private int[] numStudentCollection = {  };
  private String[] rangeCollection = {  };
  private int[] columnHeight = {  };
  private int arrayLength; //length of array
  private String range; // range of student Score lowest-highest
  private int numResponses;
  private String q1;
  private String q2;
  private String q3;
  private String q4;
  private HistogramBarBean[] histogramBars;
  private HistogramQuestionScoresBean[] histogramQuestions;
  private boolean randomType;   // true = has at least one random draw part

  private static Log log = LogFactory.getLog(HistogramScoresBean.class);


  /**
   * Creates a new HistogramScoresBean object.
   */
  public HistogramScoresBean()
  {
  }

  /**
   * number of responses
   *
   * @param pagent number of responses
   */
  public void setAgent(String pagent)
  {
    agent = pagent;
  }

  /**
   * get the agent
   *
   * @return agent
   */
  public String getAgent()
  {
    return agent;
  }

  /**
   * assessment name
   *
   * @return assessment name
   */
  public String getAssessmentName()
  {
    return assessmentName;
  }

  /**
   * assessment name
   *
   * @param passessmentName assessment name
   */
  public void setAssessmentName(String passessmentName)
  {
    assessmentName = passessmentName;
  }

  /**
   * get histogram quesions
   *
   * @return the histogram quesions beans in an array
   */
  public HistogramQuestionScoresBean[] getHistogramQuestions()
  {
    return histogramQuestions;
  }

  /**
   * set histogram questions name
   *
   *
   * @param phistogramQuestions HistogramQuestionScoresBean[]
   */
  public void setHistogramQuestions(
    HistogramQuestionScoresBean[] phistogramQuestions)
  {
    histogramQuestions = phistogramQuestions;
  }

  /**
   * assessment id
   *
   * @return assessment id
   */
  public String getAssessmentId()
  {
    return assessmentId;
  }

  /**
   * assessment id
   *
   * @param passessmentId assessment id
   */
  public void setAssessmentId(String passessmentId)
  {
    assessmentId = passessmentId;
  }

  /**
* get published id
*
* @return the published id
*/
public String getPublishedId()
{
return publishedId;
}

/**
* set published id
*
* @param passessmentId the id
*/
public void setPublishedId(String ppublishedId)
{
publishedId = ppublishedId;
}


  /**
   * Get item id for QuestionScores.
   */
  public String getItemId()
  {
    return itemId;
  }

  /**
   * Set item id for QuestionScores.
   */
  public void setItemId(String newId)
  {
    itemId = newId;
  }

  /**
   * group name
   *
   * @return group name
   */
  public String getGroupName()
  {
    return groupName;
  }

  /**
   * group name
   *
   * @param pgroupName group name
   */
  public void setGroupName(String pgroupName)
  {
    groupName = pgroupName;
  }

 /**
   * hasNav
   *
   * @return hasNav
   */
  public String getHasNav()
  {
    return hasNav;
  }

  /**
   * hasNav
   *
   * @param hasNav
   */
  public void setHasNav(String phasNav)
  {
    hasNav = phasNav;
  }


  public boolean getRandomType()
  {
    return randomType;
  }

  public void setRandomType(boolean param)
  {
    randomType= param;
  }

  /**
   * all submissions
   *
   * @return all submissions
   */
  public String getAllSubmissions()
  {
    return allSubmissions;
  }

  /**
   * all submissions
   *
   * @param pallSubmissions all submissions
   */
  public void setAllSubmissions(String pallSubmissions)
  {
    allSubmissions = pallSubmissions;
  }

  /**
   * range  info for bars
   *
   * @return range  info for bars
   */
  public String[] getRangeCollection()
  {
    return rangeCollection;
  }

  /**
   * range  info for bars
   *
   * @param prange range  info for bars
   */
  public void setRangeCollection(String[] prange)
  {
    rangeCollection = prange;
  }

  /**
   * number of students for bars
   *
   * @return number of students for bars
   */
  public int[] getNumStudentCollection()
  {
    return numStudentCollection;
  }

  /**
   * number of students for bars
   *
   * @param pnumStudent number of students for bars
   */
  public void setNumStudentCollection(int[] pnumStudent)
  {
    numStudentCollection = pnumStudent;
  }

  /**
   * height of bars, if running vertically, width if horizonatl
   *
   * @return height of bars
   */
  public int[] getColumnHeight()
  {
    return columnHeight;
  }

  /**
   * height of bars
   *
   * @param pcolumnHeight height of bars
   */
  public void setColumnHeight(int[] pcolumnHeight)
  {
    columnHeight = pcolumnHeight;
  }

  /**
   * set the agent
   *
   * @return agent
   */
  public int getArrayLength()
  {
    return arrayLength;
  }

  /**
   * set the array lenght
   *
   * @param parrayLength the lenght
   */
  public void setArrayLength(int parrayLength)
  {
    arrayLength = parrayLength;
  }

  /**
   * get the interval
   *
   * @return the interval
   */
  public int getInterval()
  {
    return interval;
  }

  /**
   * set the interval
   *
   * @param pinterval the interval
   */
  public void setInterval(int pinterval)
  {
    interval = pinterval;
  }

  /**
   * the a collection of information
   *
   * @return the info collection
   */
  public Collection getInfo()
  {
    return info;
  }

  /**
   * set the info
   *
   * @param pinfo the info
   */
  public void setInfo(Collection pinfo)
  {
    info = pinfo;
  }

  /**
   * get the maximum score
   *
   * @return the score
   */
  public String getMaxScore()
  {
    return maxScore;
  }

  /**
   * set the maximum score
   *
   * @param pmaxScore the max score
   */
  public void setMaxScore(String pmaxScore)
  {
    maxScore = pmaxScore;
  }

  /**
   * get the total score
   *
   * @return the total score
   */
  public String getTotalScore()
  {
    return totalScore;
  }

  /**
   * set the total score
   *
   * @param ptotalScore the total score
   */
  public void setTotalScore(String ptotalScore)
  {
    totalScore = ptotalScore;
  }

  /**
   * get the total possible score
   *
   * @return the total possible score
   */
  public String getTotalPossibleScore()
  {
    return totalPossibleScore;
  }


  public String getRoundedTotalPossibleScore() {
   try {

      String newscore = ContextUtil.getRoundedValue(totalPossibleScore, 2);
      return Validator.check(newscore, "N/A");
    }
    catch (Exception e) {
      // encountered some weird number format/locale
      return Validator.check(totalPossibleScore, "0");
    }

  }


  /**
   * set the total possible score
   *
   * @param ptotalScore the total possible score
   */
  public void setTotalPossibleScore(String ptotalScore)
  {
    totalPossibleScore = ptotalScore;
  }

  /**
   * ge the adjusted score
   *
   * @return the adjusted socre
   */
  public String getAdjustedScore()
  {
    return adjustedScore;
  }

  /**
   * set the adjusted score
   *
   * @param padjustedScore the adjusted score
   */
  public void setAdjustedScore(String padjustedScore)
  {
    adjustedScore = padjustedScore;
  }

  /**
   * get the question number
   *
   * @return the question number
   */
  public String getQuestionNumber()
  {
    return questionNumber;
  }

  /**
   * set the question number
   *
   * @param pquestionNumber the question number
   */
  public void setQuestionNumber(String pquestionNumber)
  {
    questionNumber = pquestionNumber;
  }

  /**
   * get the part number
   *
   * @return the part number
   */
  public String getPartNumber()
  {
    return partNumber;
  }

  /**
   * set the part number
   *
   * @param ppartNumber the part number
   */
  public void setPartNumber(String ppartNumber)
  {
    partNumber = ppartNumber;
  }

  /**
   * get the mean
   *
   * @return the mean
   */
  public String getMean()
  {
    return mean;
  }

  /**
   * set the mean
   *
   * @param pmean the mean
   */
  public void setMean(String pmean)
  {
    mean = pmean;
  }

  /**
   * get the median
   *
   * @return the median
   */
  public String getMedian()
  {
    return median;
  }

  /**
   * set the median
   *
   * @param pmedian the median
   */
  public void setMedian(String pmedian)
  {
    median = pmedian;
  }

  /**
   * get the mode
   *
   * @return the mode
   */
  public String getMode()
  {
    return mode;
  }

  /**
   * set the mode
   *
   * @param pmode the mode
   */
  public void setMode(String pmode)
  {
    mode = pmode;
  }

  /**
   * get the standard deviation
   *
   * @return the standard deviation
   */
  public String getStandDev()
  {
    return standDev;
  }

  /**
   * the standard deviation
   *
   * @param pstandDev the standard deviation
   */
  public void setStandDev(String pstandDev)
  {
    standDev = pstandDev;
  }

  /**
   * get the lower quartile
   *
   * @return lower quartile
   */
  public String getLowerQuartile()
  {
    return lowerQuartile;
  }

  /**
   * lower quartile
   *
   * @param plowerQuartile lower quartile
   */
  public void setLowerQuartile(String plowerQuartile)
  {
    lowerQuartile = plowerQuartile;
  }

  /**
   * get the upper quartile
   *
   * @return upper quartile
   */
  public String getUpperQuartile()
  {
    return upperQuartile;
  }

  /**
   * set the upper quartile
   *
   * @param pupperQuartile the upper quartile
   */
  public void setUpperQuartile(String pupperQuartile)
  {
    upperQuartile = pupperQuartile;
  }

  /**
   * get the first quartile
   *
   * @return the first quartile
   */
  public String getQ1()
  {
    return q1;
  }

  /**
   * set the first quartile
   *
   * @param pq1 the first quartile
   */
  public void setQ1(String pq1)
  {
    q1 = pq1;
  }

  /**
   * get the second quartile
   *
   * @return the second quartile
   */
  public String getQ2()
  {
    return q2;
  }

  /**
   * set the second quartile
   *
   * @param pq2 the second quartile
   */
  public void setQ2(String pq2)
  {
    q2 = pq2;
  }

  /**
   * get teh third quartile
   *
   * @return the third quartile
   */
  public String getQ3()
  {
    return q3;
  }

  /**
   * set the third quartile
   *
   * @param pq3 the third quartile
   */
  public void setQ3(String pq3)
  {
    q3 = pq3;
  }

  /**
   * get the fourth quartile
   *
   * @return the fourth quartile
   */
  public String getQ4()
  {
    return q4;
  }

  /**
   * set the fourth quartile
   *
   * @param pq4 the fourth quartile
   */
  public void setQ4(String pq4)
  {
    q4 = pq4;
  }

  /**
   * get the range
   *
   * @return range
   */
  public String getRange()
  {
    return range;
  }

  /**
   * set the range
   *
   * @param prange range
   */
  public void setRange(String prange)
  {
    range = prange;
  }

  /**
   * number of responses
   *
   * @return number of responses
   */
  public int getNumResponses()
  {
    return numResponses;
  }

  /**
   * number of responses
   *
   * @param pnumResponses number of responses
   */
  public void setNumResponses(int pnumResponses)
  {
    numResponses = pnumResponses;
  }

  /**
   * HistogramBar arrray
   *
   * @return HistogramBar arrray
   */
  public HistogramBarBean[] getHistogramBars()
  {
    if (histogramBars != null)
    {
      return histogramBars;
    }

    // note we cache this value, calc only once.
    int length = getArrayLength();
    int[] height = getColumnHeight();
    int[] nums = getNumStudentCollection();
    String[] range = getRangeCollection();

    histogramBars = new HistogramBarBean[length];

    for (int i = 0; i < length; i++) {
      histogramBars[i] = new HistogramBarBean();
      histogramBars[i].setColumnHeight(Integer.toString(height[i]));
      histogramBars[i].setNumStudents(nums[i]);
      histogramBars[i].setRangeInfo(range[i]);
    }

    return histogramBars;
  }

  /**
   * HistogramBar arrray
   *
   * @param bars HistogramBar arrray
   */
  public void setHistogramBars(HistogramBarBean[] bars)
  {
    histogramBars = bars;
  }

  
  // Below Added by gopalrc - Nov 2007
  /**
   * added by gopalrc Nov 2007
   * Students in the upper 25%
   */
  private Map upperQuartileStudents;
  
  /**
   * added by gopalrc Nov 2007
   * Students in the lower 25%
   */
  private Map lowerQuartileStudents;

  /**
   * gopalrc - Nov 2007
   * The maximum number of answers per question
   * for this assessment (for detailed stats layout)
   */
  private int maxNumberOfAnswers = 0;

  
  /**
   * gopalrc Dec 2007
   * The HistogramQuestionScores for detailed Statistics for 
   */
  private Collection detailedStatistics;

  
  
  public void addToUpperQuartileStudents(String agentId) {
	  if (upperQuartileStudents == null) {
		  upperQuartileStudents = new HashMap();
	  }
	  upperQuartileStudents.put(agentId, agentId);
  }
  
  public boolean isUpperQuartileStudent(String agentId) {
	  if (upperQuartileStudents == null) {
		  return false;
	  }
	  else {
		  if (upperQuartileStudents.get(agentId) == null) {
			  return false;
		  }
		  else {
			  return true;
		  }
	  }
  }
  
  public int getNumberOfUpperQuartileStudents() {
	  if (upperQuartileStudents == null) {
		  return 0;
	  }
	  else {
		  return upperQuartileStudents.size();
	  }
  }
  
  public void addToLowerQuartileStudents(String agentId) {
	  if (lowerQuartileStudents == null) {
		  lowerQuartileStudents = new HashMap();
	  }
	  lowerQuartileStudents.put(agentId, agentId);
  }
  
  
  public boolean isLowerQuartileStudent(String agentId) {
	  if (lowerQuartileStudents == null) {
		  return false;
	  }
	  else {
		  if (lowerQuartileStudents.get(agentId) == null) {
			  return false;
		  }
		  else {
			  return true;
		  }
	  }
  }

  public int getNumberOfLowerQuartileStudents() {
	  if (lowerQuartileStudents == null) {
		  return 0;
	  }
	  else {
		  return lowerQuartileStudents.size();
	  }
  }
  
  public void clearUpperQuartileStudents() {
	  upperQuartileStudents = null;
  }
  
  public void clearLowerQuartileStudents() {
	  lowerQuartileStudents = null;
  }
  
  
  public int getMaxNumberOfAnswers() {
	  return maxNumberOfAnswers;  
  }

  public void setMaxNumberOfAnswers(int maxNumberOfAnswers) {
	  this.maxNumberOfAnswers = maxNumberOfAnswers;
  }

  public Collection getDetailedStatistics() {
	return detailedStatistics;
  }

  public void setDetailedStatistics(Collection detailedStatistics) {
	this.detailedStatistics = detailedStatistics;
  }
	
  public boolean getShowDiscriminationColumn() {
	  try {
		  return getTotalScore() == null ? false : Float.parseFloat(getTotalScore())!=0.0f;
	  }
	  catch (NumberFormatException ex) {
		  return false;
	  }
  }
  
  public boolean getShowPartAndTotalScoreSpreadsheetColumns() {
	  try {
		  return getTotalScore() == null ? false : Float.parseFloat(getTotalScore())!=0.0f;
	  }
	  catch (NumberFormatException ex) {
		  return false;
	  }
  }
  
  
  public String getUndisplayedStudentResponseInItemAnalysisColumnHeader() {
	  
	  if (getMaxNumberOfAnswers()<13) {
		  return "";
	  }
	  else {
		  int first = 65+12;
		  char firstUndisplayed = (char)first;
		  int last = 65+getMaxNumberOfAnswers()-1;
		  char lastUndisplayed = (char)last;
		  if (first==last) {
			  return String.valueOf(firstUndisplayed);
		  }
		  else {
			  return String.valueOf(firstUndisplayed) + " - " + String.valueOf(lastUndisplayed);
		  }
	  }
  }

  
}
