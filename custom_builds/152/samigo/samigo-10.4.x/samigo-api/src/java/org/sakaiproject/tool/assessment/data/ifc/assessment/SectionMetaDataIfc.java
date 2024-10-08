/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.data.ifc.assessment;

public interface SectionMetaDataIfc
    extends java.io.Serializable
{
  public static String KEYWORDS = "SECTION_KEYWORDS";
  public static String OBJECTIVES = "SECTION_OBJECTIVES";
  public static String RUBRICS = "SECTION_RUBRICS";
  public static String ATTACHMENTS = "ATTACHMENTS";
  public static String QUESTIONS_ORDERING = "QUESTIONS_ORDERING";
  public static String AUTHOR_TYPE = "AUTHOR_TYPE";
  public static String POINT_VALUE_FOR_QUESTION = "POINT_VALUE_FOR_QUESTION";
  public static String NUM_QUESTIONS_DRAWN = "NUM_QUESTIONS_DRAWN";
  public static String POOLNAME_FOR_RANDOM_DRAW = "POOLNAME_FOR_RANDOM_DRAW";
  public static String POOLID_FOR_RANDOM_DRAW = "POOLID_FOR_RANDOM_DRAW";
    
  Long getId();

  void setId(Long id);

  SectionDataIfc getSection();

  void setSection(SectionDataIfc section);

  String getLabel();

  void setLabel(String label);

  String getEntry();

  void setEntry(String entry);

}
