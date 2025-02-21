/* $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/ItemFacade.java $
 * $Id: ItemFacade.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osid.assessment.AssessmentException;
import org.osid.assessment.Item;
import org.osid.shared.Type;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.osid.assessment.impl.ItemImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;

/**
 *
 * ItemFacade implements ItemDataIfc that encapsulates our out of bound (OOB)
 * agreement.
 */
public class ItemFacade implements Serializable, ItemDataIfc, Comparable {
  private static Log log = LogFactory.getLog(ItemFacade.class);

  private static final long serialVersionUID = 7526471155622776147L;
  protected org.osid.assessment.Item item;
  // We have 2 sets of properties:
  // #1) properties according to org.osid.assessment.Item. However, we will
  // not have property "displayName" because I am not sure what it is.
  // Properties "description" will be persisted through data - daisyf 07/28/04
  protected org.osid.shared.Id id;
  protected String description;
  protected ItemDataIfc data;
  protected org.osid.shared.Type itemType;
  // #2) properties according to ItemDataIfc
  protected Long itemId;
  protected String itemIdString;
  private SectionFacade section;
  protected Integer sequence;
  protected Integer duration;
  protected Integer triesAllowed;
  protected String instruction;
  protected Long typeId;
  protected String grade;
  protected Float score;
  protected Float minScore;
  protected Float discount;
  protected String hint;
  protected Boolean partialCreditFlag;
  protected Boolean hasRationale;
  protected Integer status;
  protected String createdBy;
  protected Date createdDate;
  protected String lastModifiedBy;
  protected Date lastModifiedDate;
  protected Set itemTextSet;
  protected Set itemMetaDataSet;
  protected Set itemFeedbackSet;
  protected HashMap itemMetaDataMap = new HashMap();
  protected HashMap itemFeedbackMap = new HashMap();
  protected TypeFacade itemTypeFacade;
  protected Set itemAttachmentSet;
  protected String itemAttachmentMetaData;
  
  /** ItemFacade is the class that is exposed to developer
   *  It contains some of the useful methods specified in
   *  org.osid.assessment.Item and it implements
   *  org.sakaiproject.tool.assessment.ifc.
   *  When new methods is added to osid api, this code is still workable.
   *  If signature in any of the osid methods that we mirrored changes,
   *  we only need to modify those particular methods.
   *  - daisyf
   */

  public ItemFacade(){
  // need to hook ItemFacade.data to ItemData, our POJO for Hibernate
  // persistence
   this.data = new ItemData();
   ItemImpl itemImpl = new ItemImpl(); //<-- place holder
   item = (Item)itemImpl;
   try {
     item.updateData(this.data);
   }
   catch (AssessmentException ex) {
     throw new DataFacadeException(ex.getMessage());
   }
  }

  /**
   * This is a very important constructor. Please make sure that you have
   * set all the properties (declared above as private) of ItemFacade using
   * the "data" supplied. "data" is a org.osid.assessment.Item properties
   * and I use it to store info about an item.
   * @param data
   */
  public ItemFacade(ItemDataIfc data){
    this.data = data;
    ItemImpl itemImpl = new ItemImpl(); // place holder
    item = (Item)itemImpl;
    try {
      item.updateData(this.data);
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    this.id = getId();
    this.description = getDescription();
    this.itemType = getItemType();
    this.itemTextSet = getItemTextSet();
    this.itemMetaDataSet = getItemMetaDataSet();
    this.itemMetaDataMap = getItemMetaDataMap(this.itemMetaDataSet);
    this.itemFeedbackSet = getItemFeedbackSet();
    this.itemFeedbackMap = getItemFeedbackMap(this.itemFeedbackSet);
    this.hasRationale= data.getHasRationale();//rshastri :SAK-1824
    this.itemAttachmentSet = getItemAttachmentSet();
  }

    /*
  public Object clone() throws CloneNotSupportedException{
        ItemData itemdataOrig = (ItemData) this.data;
  ItemData cloneditemdata = (ItemData) itemdataOrig.clone();
  // set itemId and itemIdString = 0
        cloneditemdata.setItemId(new Long(0));
        cloneditemdata.setItemIdString("0");
        Object cloned = new ItemFacade(cloneditemdata);
        return cloned;
    }
    */

  // the following method's signature has a one to one relationship to
  // org.sakaiproject.tool.assessment.osid.item.ItemImpl
  // which implements org.osid.assessment.Item

  /**
   * Get the Id for this ItemFacade.
   * @return org.osid.shared.Id
   */
  org.osid.shared.Id getId(){
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    ItemFacadeQueriesAPI itemFacadeQueries = new ItemFacadeQueries();
    return itemFacadeQueries.getItemId(this.data.getItemId());
  }

  /**
   * Get the Type for this ItemFacade.
   * @return org.osid.shared.Type
   */
  Type getItemType() {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    return typeFacadeQueries.getTypeById(this.data.getTypeId());
  }

  public TypeIfc getType() {
    return getItemTypeFacade();
  }

  public TypeFacade getItemTypeFacade() {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    return typeFacadeQueries.getTypeFacadeById(this.data.getTypeId());
  }

  /**
   * Get the data for this ItemFacade.
   * @return ItemDataIfc
   */
  public ItemDataIfc getData(){
    return this.data;
  }

  /**
   * Call setDate() to update data in ItemFacade
   * @param data
   */
  public void updateData(ItemDataIfc data) {
      setData(data);
  }

  /**
   * Set data for ItemFacade
   * @param data
   */
  public void setData(ItemDataIfc data) {
      this.data = data;
  }

  // the following methods implements
  // org.sakaiproject.tool.assessment.ifc.ItemDataIfc
  public Long getItemId() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemId();
  }

  /**
   * Set itemId for ItemFacade
   * @param itemId
   */
  public void setItemId(Long itemId) {
    this.itemId = itemId;
    this.data.setItemId(itemId);
    setItemIdString(itemId.toString());
  }

  // the following methods implements
  // org.sakaiproject.tool.assessment.ifc.ItemDataIfc
  public String getItemIdString() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemIdString();
  }

  /**
   * Set itemId for ItemFacade
   * @param itemId
   */
  public void setItemIdString(String itemIdString) {
    this.itemIdString = itemIdString;
    this.data.setItemIdString(itemIdString);
  }

  // expect a return of SectionFacade from this method
  public SectionDataIfc getSection() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    if (this.data.getSection()!= null) {
      return new SectionFacade(this.data.getSection());
    }
    else {
      return null;
    }
  }

  // section is SectionFacade not SectionData
  public void setSection(SectionDataIfc section) {
    this.section = (SectionFacade) section;
    if (this.section != null) {
      this.data.setSection(this.section.getData());
    }
    else {
      this.data.setSection(null);
    }
  }

  public Integer getSequence() throws DataFacadeException {
  try {
    this.data = (ItemDataIfc) item.getData();
  }
  catch (AssessmentException ex) {
    throw new DataFacadeException(ex.getMessage());
  }
   return this.data.getSequence();
  }


  public void setSequence(Integer sequence) {
    this.sequence = sequence;
    this.data.setSequence(sequence);
  }

  public Integer getDuration() throws DataFacadeException {
  try {
    this.data = (ItemDataIfc) item.getData();
  }
  catch (AssessmentException ex) {
    throw new DataFacadeException(ex.getMessage());
  }
   return this.data.getDuration();
  }

  /**
   * Set duration for ItemFacade
   * @param duration
   */
  public void setDuration(Integer duration) {
    this.duration = duration;
    this.data.setDuration(duration);
  }

  public String getInstruction() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getInstruction();
  }

  /**
   * Set instruction for ItemFacade
   * e.g. "Match the following sentences", "In the score between 1-5, specify
   * your preference"
   * @param instruction
   */
  public void setInstruction(String instruction) {
    this.instruction = instruction;
    this.data.setInstruction(instruction);
  }

  public String getDescription() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getDescription();
  }

  /**
   * Set description for ItemFacade
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
    this.data.setDescription(description);
  }

  public Long getTypeId() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getTypeId();
  }

  /**
   * Set TypeId for ItemType. This property is used to indicate question type.
   * e.g. 1 = Multiple Choice, 2 = Multiple Correct. Please check out
   * ddl/02_TypeData.sql and table "type".
   * @param typeId
   */
  public void setTypeId(Long typeId) {
    this.typeId = typeId;
    this.data.setTypeId(typeId);
  }

  /**
   * Get Grade for ItemFacade
   * @return
   * @throws DataFacadeException
   */
  public String getGrade() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getGrade();
  }

  /**
   * Set Grade for ItemFacade
   * @param grade
   */
  public void setGrade(String grade) {
    this.grade = grade;
    this.data.setGrade(grade);
  }

  /**
   * Get Score for ItemFacade
   * @return
   * @throws DataFacadeException
   */
  public Float getScore() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getScore();
  }

  /**
   * Set Score for ItemFacade
   * @param score
   */
  public void setScore(Float score) {
    this.score = score;
    this.data.setScore(score);
  }

  /**
   * Get Discount for ItemFacade
   * @return
   * @throws DataFacadeException
   */
  public Float getDiscount() throws DataFacadeException {
	  try {
		  this.data = (ItemDataIfc) item.getData();
	  }
	  catch (AssessmentException ex) {
		  throw new DataFacadeException(ex.getMessage());
	  }
	  return this.data.getDiscount();
  }

  /**
   * Set Discount for ItemFacade
   * @param discount
   */
  public void setDiscount(Float discount) {
	  this.discount = discount;
	  this.data.setDiscount(discount);
  }
  
  /**
   * Get Hint for ItemFacade
   * @return
   * @throws DataFacadeException
   */
  public String getHint() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getHint();
  }

  /**
   * Set Hint for ItemFacade
   * @param hint
   */
  public void setHint(String hint) {
    this.hint = hint;
    this.data.setHint(hint);
  }

  /**
   * Check if item (question) require rationale in answer
   * @return
   * @throws DataFacadeException
   */
  public Boolean getHasRationale() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getHasRationale();
  }

  /**
   * Set preference if rationale should be collected in answer
   * @param hasRationale
   */
  public void setHasRationale(Boolean hasRationale) {
    this.hasRationale = hasRationale;
    this.data.setHasRationale(hasRationale);
  }

  /**
   * Get status of ItemFacade. 1 = active, 0 = inactive
   * @return
   * @throws DataFacadeException
   */
  public Integer getStatus() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getStatus();
  }

  /**
   * Set status for ItemFacade. 1 = active, 0 = inactive
   * @param status
   */
  public void setStatus(Integer status) {
    this.status = status;
    this.data.setStatus(status);
  }

  /**
   * Get createdBy for ItemFacade. This represents the agentId of the person
   * who created the record
   * @return
   * @throws DataFacadeException
   */
  public String getCreatedBy() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getCreatedBy();
  }

  /**
   * Set createdBy for ItemFacade. This represents the agentId of the person
   * who created the record
   * @param createdBy
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    this.data.setCreatedBy(createdBy);
  }


  /**
   * Get the creation date of ItemFacade.
   * @return
   * @throws DataFacadeException
   */
  public Date getCreatedDate() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getCreatedDate();
  }

  /**
   * Set the creation date of ItemFacade
   * @param createdDate
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
    this.data.setCreatedDate(createdDate);
  }

  /**
   * Get the agentId of the person who last modified ItemFacade
   * @return
   * @throws DataFacadeException
   */
  public String getLastModifiedBy() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedBy();
  }

  /**
   * set the agentId of the person who last modified itemFacade
   * @param lastModifiedBy
   */
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
    this.data.setLastModifiedBy(lastModifiedBy);
  }

  /**
   * Get the date when ItemFacade where last modified By
   * @return
   * @throws DataFacadeException
   */
  public Date getLastModifiedDate() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getLastModifiedDate();
  }

  /**
   * Set the last modified date
   * @param lastModifiedBy
   */
  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
    this.data.setLastModifiedDate(lastModifiedDate);
  }

  /**
   * Get item text set (question text set) from ItemFacade.data
   * @return
   * @throws DataFacadeException
   */
  public Set getItemTextSet() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemTextSet();
  }

  /**
   * Set item text (question text) in ItemFacade.data
   * @param itemTextSet
   */
  public void setItemTextSet(Set itemTextSet) {
    this.itemTextSet = itemTextSet;
    this.data.setItemTextSet(itemTextSet);
  }

  public Set getItemMetaDataSet() throws DataFacadeException {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemMetaDataSet();
  }

  /**
   * Set item metadata set in ItemFacade and ItemFacade.data
   * @param itemMetaDataSet
   */
  public void setItemMetaDataSet(Set itemMetaDataSet) {
    this.itemMetaDataSet = itemMetaDataSet;
    this.data.setItemMetaDataSet(itemMetaDataSet);
    this.itemMetaDataMap = getItemMetaDataMap(itemMetaDataSet);
  }

  /**
   * Get item metadata in HashMap (String Label, ItemMetaData itemMetaData) of
   * ItemFacade
   * @param itemMetaDataSet
   * @return
   */
  public HashMap getItemMetaDataMap(Set itemMetaDataSet) {
    HashMap itemMetaDataMap = new HashMap();
    if (itemMetaDataSet !=null){
      for (Iterator i = itemMetaDataSet.iterator(); i.hasNext(); ) {
        ItemMetaDataIfc itemMetaData = (ItemMetaDataIfc) i.next();
        itemMetaDataMap.put(itemMetaData.getLabel(), itemMetaData.getEntry());
      }
    }
    return itemMetaDataMap;
  }

  /**
   * Set the item feedback set for ItemFacade using the "data"
   * @return
   */
  public Set getItemFeedbackSet() {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemFeedbackSet();
  }

  /**
   * Set the item feedback set for ItemFacade and ItemFacade.data
   * @param itemFeedbackSet
   */
   public void setItemFeedbackSet(Set itemFeedbackSet) {
     this.itemFeedbackSet = itemFeedbackSet;
     this.data.setItemFeedbackSet(itemFeedbackSet);
     this.itemFeedbackMap = getItemFeedbackMap(itemFeedbackSet);
   }

  /**
   * Get a HashMap (Long feedbackTypeId, ItemFeedback itemFeedback) of item
   * feedback for ItemFacade
   * @param itemFeedbackSet
   * @return
   */
  public HashMap getItemFeedbackMap(Set itemFeedbackSet) {
    HashMap itemFeedbackMap = new HashMap();
    if (itemFeedbackSet != null){
      for (Iterator i = itemFeedbackSet.iterator(); i.hasNext(); ) {
        ItemFeedbackIfc itemFeedback = (ItemFeedbackIfc) i.next();
        itemFeedbackMap.put(itemFeedback.getTypeId(), itemFeedback.getText());
      }
    }
    return itemFeedbackMap;
  }

  /**
   * implements Serializable method
   * @param out
   * @throws IOException
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  /**
   * implements Serializable method
   * @param in
   * @throws IOException
   * @throws java.lang.ClassNotFoundException
   */
  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
  }

  /**
   * Add item text (question text) to ItemFacade (question). For multiple
   * choice, multiple correct, survey, matching & fill in the blank, you can
   * specify a set of acceptable answers. Usually, the purpose for this is
   * to facilitate auto-grading.
   * @param text
   * @param answerSet
   */
  public void addItemText(String text, Set answerSet) {
    if (this.data.getItemTextSet() == null) {
      this.data.setItemTextSet(new HashSet());
    }
    Long sequence = Long.valueOf(this.data.getItemTextSet().size()+1);
    ItemText itemText = new ItemText((ItemData)this.data, sequence,
                                     text, answerSet);
    this.data.getItemTextSet().add(itemText);
    this.itemTextSet = this.data.getItemTextSet();
  }

  /**
   * Get meta data by label
   * @param label
   * @return
   */
  public String getItemMetaDataByLabel(String label) {
    return (String)this.itemMetaDataMap.get(label);
  }

  /**
   * Add a Meta Data to ItemFacade
   * @param label
   * @param entry
   */
  public void addItemMetaData(String label, String entry) {
    if (this.itemMetaDataSet == null) {
      setItemMetaDataSet(new HashSet());
      this.itemMetaDataMap = new HashMap();
    }
    this.itemMetaDataMap.put(label, entry);
    this.data.getItemMetaDataSet().add(new ItemMetaData((ItemData)this.data, label, entry));
    this.itemMetaDataSet = this.data.getItemMetaDataSet();
  }

 /**
  * Get General Feedback
  * @return
  */
  public String getGeneralItemFeedback() {
    return getItemFeedback(ItemFeedbackIfc.GENERAL_FEEDBACK);
  }

  /**
   * Set General Feedback
   * @param text
   */
  public void setGeneralItemFeedback(String text) {
    removeFeedbackByType(ItemFeedbackIfc.GENERAL_FEEDBACK);
    addItemFeedback(ItemFeedbackIfc.GENERAL_FEEDBACK, text);
  }


 /**
  * Get Correct Feedback
  * @return
  */
  public String getCorrectItemFeedback() {
    return getItemFeedback(ItemFeedbackIfc.CORRECT_FEEDBACK);
  }

  /**
   * Set Correct Feedback
   * @param text
   */
  public void setCorrectItemFeedback(String text) {
    removeFeedbackByType(ItemFeedbackIfc.CORRECT_FEEDBACK);
    addItemFeedback(ItemFeedbackIfc.CORRECT_FEEDBACK, text);
  }

  /**
   * Get Incorrect Feedback
   * @return
   */
  public String getInCorrectItemFeedback() {
    return getItemFeedback(ItemFeedbackIfc.INCORRECT_FEEDBACK);
  }

  /**
   * Set InCorrect Feedback
   * @param text
   */
  public void setInCorrectItemFeedback(String text) {
    removeFeedbackByType(ItemFeedbackIfc.INCORRECT_FEEDBACK);
    addItemFeedback(ItemFeedbackIfc.INCORRECT_FEEDBACK, text);
  }

  /**
   * Get feedback based on feedback type (e.g. CORRECT, INCORRECT)
   * @param feedbackTypeId
   * @return
   */
  public String getItemFeedback(String feedbackTypeId) {
    return (String)this.itemFeedbackMap.get(feedbackTypeId);
  }

  /**
   * Add feedback of a specified feedback type (e.g. CORRECT, INCORRECT)
   * to ItemFacade
   * @param feedbackTypeId
   * @param text
   */
  public void addItemFeedback(String feedbackTypeId, String text) {
    if (this.itemFeedbackSet == null) {
      setItemFeedbackSet(new HashSet());
      this.itemFeedbackMap = new HashMap();
    }
    this.itemFeedbackMap.put(feedbackTypeId, text);
    this.data.getItemFeedbackSet().add(new ItemFeedback((ItemData)this.data, feedbackTypeId, text));
    this.itemFeedbackSet = this.data.getItemFeedbackSet();
  }

  /**
   * Remove Feedback by feedback typeId (e.g. CORRECT, INCORRECT)
   * @param feedbackTypeId
   */
  public void removeFeedbackByType(String feedbackTypeId) {
    this.itemFeedbackSet = this.data.getItemFeedbackSet();
    if (this.itemFeedbackSet != null) {
      HashSet toBeRemovedSet = new HashSet();
      for (Iterator i = this.itemFeedbackSet.iterator(); i.hasNext(); ) {
        ItemFeedbackIfc itemFeedback = (ItemFeedbackIfc) i.next();
        if (itemFeedback.getTypeId().equals(feedbackTypeId)) {
        	toBeRemovedSet.add(itemFeedback);
          //this.itemFeedbackSet.remove(itemFeedback);
        }
      }
      this.itemFeedbackSet.removeAll(toBeRemovedSet);
    }
  }


  /**
   * If this is a true-false question return true if it is true, else false.
   * If it is not a true-false question return false.
   * @return true if this is a true true-false question
   */
  public Boolean getIsTrue()  throws DataFacadeException
  {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getIsTrue();
  }

  /**
   * Utility method.
   * In the case of an ordinary question, this will obtain the a set of text with
   * one element and return it; in FIB or FIN return multiple elements separated by
   * underscores.
   * @return text of question
   */

  public String getText() throws DataFacadeException
  {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getText();
  }

  /**
   * Utility method.
   * Obeys the semantics and rules of the getText() but removes
   * HTML tags.
   *
   * @see getText()
   * @return text of question, removes
   * HTML tags.
   * @throws DataFacadeException
   */
  public String getTextHtmlStripped() throws DataFacadeException
  {
    String regex = "\\<.*?\\>";
    return getText().replaceAll(regex," ");
  }

  /**
   * Utility method.
   * Obeys the semantics and rules of the getText() but removes
   * HTML tags. The differency of getTextHtmlStripped() is this
   * API doesn't replace HTMLtags by white space.
   *
   * @see getText()
   * @return text of question, removes
   * HTML tags.
   * @throws DataFacadeException
   */
  public String getTextHtmlStrippedAll() throws DataFacadeException
  {
    String regex = "\\<.*?\\>|\\n|\\r\\n";
    return getText().replaceAll(regex,"");
  }
  
  public ArrayList getItemTextArray() {
    ArrayList list = new ArrayList();
    Iterator iter = itemTextSet.iterator();
    while (iter.hasNext()){
      list.add(iter.next());
    }
    return list;
  }

  public ArrayList getItemTextArraySorted() {
    ArrayList list = getItemTextArray();
    Collections.sort(list);
    return list;
  }

  public Integer getTriesAllowed() throws DataFacadeException {
  try {
    this.data = (ItemDataIfc) item.getData();
  }
  catch (AssessmentException ex) {
    throw new DataFacadeException(ex.getMessage());
  }
   return this.data.getTriesAllowed();
  }

  /**
   * Set duration for ItemFacade
   * @param duration
   */
  public void setTriesAllowed(Integer triesAllowed) {
    this.triesAllowed = triesAllowed;
    this.data.setTriesAllowed(triesAllowed);
  }

    public void removeMetaDataByType(String label) {
	try {
	    if (itemMetaDataSet!= null) {
		for (Iterator i = this.itemMetaDataSet.iterator(); i.hasNext(); ) {
		    ItemMetaDataIfc itemMetaData= (ItemMetaDataIfc) i.next();
		    if (itemMetaData.getLabel().equals(label)) {
			//this.itemMetaDataSet.remove(itemMetaData);
			i.remove();
		    }
		}
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }


  /**
   * This method return the answerKey for a matching question
   * e.g. A:2, B:3, C:1, D:4 (where A, B & C is the answer label and 1,2 &3
   * are the itemText sequence
   */
  public String getAnswerKey(){
   return ((ItemData)this.data).getAnswerKey();
  }

  public int compareTo(Object o) {
      ItemFacade a = (ItemFacade)o;
      return sequence.compareTo(a.sequence);
  }

  public Set getItemAttachmentSet() {
    try {
      this.data = (ItemDataIfc) item.getData();
    }
    catch (AssessmentException ex) {
      throw new DataFacadeException(ex.getMessage());
    }
    return this.data.getItemAttachmentSet();
  }

  public void setItemAttachmentSet(Set itemAttachmentSet) {
    this.itemAttachmentSet = itemAttachmentSet;
    this.data.setItemAttachmentSet(itemAttachmentSet);
  }

  public List getItemAttachmentList() {
    ArrayList list = new ArrayList();
    Set set = getItemAttachmentSet(); 
    if (set !=null ){
      Iterator iter = set.iterator();
      while (iter.hasNext()){
        ItemAttachmentIfc a = (ItemAttachmentIfc)iter.next();
        list.add(a);
      }
    }
    return list;
  }

  public void addItemAttachmentMetaData(String entry) {
	  itemAttachmentMetaData = entry;
  }
  
  public String getItemAttachmentMetaData() {
	  return itemAttachmentMetaData;
  }
  
  public void setPartialCreditFlag(Boolean partialCreditFlag){
	  this.partialCreditFlag=partialCreditFlag;
	  this.data.setPartialCreditFlag(partialCreditFlag);

  }

  public Boolean getPartialCreditFlag(){
	  try {
		  this.data = (ItemDataIfc) item.getData();
	  }
	  catch (AssessmentException ex) {
		  throw new DataFacadeException(ex.getMessage());
	  }
	  return this.data.getPartialCreditFlag();
  }

  public Float getMinScore() {
	  try {
		  this.data = (ItemDataIfc) item.getData();
	  }
	  catch (AssessmentException ex) {
		  throw new DataFacadeException(ex.getMessage());
	  }
	  return this.data.getMinScore();
  }

  public void setMinScore(Float minScore) {
	  this.minScore = minScore;
	  this.data.setMinScore(minScore);
  }
}
