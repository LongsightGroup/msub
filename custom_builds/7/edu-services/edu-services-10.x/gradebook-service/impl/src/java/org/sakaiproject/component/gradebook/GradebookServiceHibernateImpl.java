/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.component.gradebook;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

/**
 * A Hibernate implementation of GradebookService.
 */
public class GradebookServiceHibernateImpl extends BaseHibernateManager implements GradebookService {
    private static final Log log = LogFactory.getLog(GradebookServiceHibernateImpl.class);

    private GradebookFrameworkService frameworkService;
    private GradebookExternalAssessmentService externalAssessmentService;
    private Authz authz;
    private GradebookPermissionService gradebookPermissionService;
    private EventTrackingService eventTrackingService;
	
    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }
    
	public boolean isAssignmentDefined(final String gradebookUid, final String assignmentName)
        throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to check for assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
        Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
        return (assignment != null);
    }

	private boolean isUserAbleToViewAssignments(String gradebookUid) {
		Authz authz = getAuthz();
		return (authz.isUserAbleToEditAssessments(gradebookUid) || authz.isUserAbleToGrade(gradebookUid));
	}

	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToGradeItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	public boolean isUserAbleToGradeItemForStudent(String gradebookUid, String itemName, String studentUid) {
		
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in GradebookServiceHibernateImpl.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid);
		}
		
		return false;

	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().isUserAbleToViewItemForStudent(gradebookUid, itemId, studentUid);
	}
	
	public boolean isUserAbleToViewItemForStudent(String gradebookUid, String itemName, String studentUid) {
		
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in GradebookServiceHibernateImpl.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid);
		}
		
		return false;

	}
	
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, Long itemId, String studentUid) {
		return getAuthz().getGradeViewFunctionForUserForStudentForItem(gradebookUid, itemId, studentUid);
	}
	
	public String getGradeViewFunctionForUserForStudentForItem(String gradebookUid, String itemName, String studentUid) {
		if (itemName == null || studentUid == null) {
			throw new IllegalArgumentException("Null parameter(s) in G.isUserAbleToGradeItemForStudent");
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assignment = getAssignment(gradebookUid, itemName);
		if (assignment != null) {
			return getGradeViewFunctionForUserForStudentForItem(gradebookUid, assignment.getId(), studentUid);
		}
		
		return null;
	}

	public List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid)
		throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignments list");
			throw new SecurityException("You do not have permission to perform this operation");
		}

		final Long gradebookId = getGradebook(gradebookUid).getId();

        List internalAssignments = (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignments(gradebookId, session);
            }
        });

		List<org.sakaiproject.service.gradebook.shared.Assignment> assignments = new ArrayList<org.sakaiproject.service.gradebook.shared.Assignment>();
		for (Iterator iter = internalAssignments.iterator(); iter.hasNext(); ) {
			Assignment assignment = (Assignment)iter.next();
			assignments.add(getAssignmentDefinition(assignment));
		}
		return assignments;
	}

	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final String assignmentName) throws GradebookNotFoundException {
		if (!isUserAbleToViewAssignments(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, assignmentName, session);
			}
		});
		if (assignment != null) {
			return getAssignmentDefinition(assignment);
		} else {
			return null;
		}
	}
	
	public org.sakaiproject.service.gradebook.shared.Assignment getAssignment(final String gradebookUid, final Long gbItemId) throws AssessmentNotFoundException {
		if (gbItemId == null || gradebookUid == null) {
			throw new IllegalArgumentException("null gbItemId passed to getAssignment");
		}
		if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
			log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to get gb item with id " + gbItemId);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		
		if (assignment == null) {
			throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gbItemId);
		}
		
		org.sakaiproject.service.gradebook.shared.Assignment assnDef;
		if (assignment != null) {
			assnDef =  getAssignmentDefinition(assignment);
		} else {
			assnDef = null;
		}
		
		return assnDef;
	}
		
	private org.sakaiproject.service.gradebook.shared.Assignment getAssignmentDefinition(Assignment internalAssignment) {
		org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition = new org.sakaiproject.service.gradebook.shared.Assignment();
    	assignmentDefinition.setName(internalAssignment.getName());
    	assignmentDefinition.setPoints(internalAssignment.getPointsPossible());
    	assignmentDefinition.setDueDate(internalAssignment.getDueDate());
    	assignmentDefinition.setAutoReleaseDate(internalAssignment.getAutoReleaseDate());
    	assignmentDefinition.setCounted(internalAssignment.isCounted());
    	assignmentDefinition.setExternallyMaintained(internalAssignment.isExternallyMaintained());
    	assignmentDefinition.setExternalAppName(internalAssignment.getExternalAppName());
    	assignmentDefinition.setExternalId(internalAssignment.getExternalId());
    	assignmentDefinition.setReleased(internalAssignment.isReleased());
    	assignmentDefinition.setId(internalAssignment.getId());
    	assignmentDefinition.setExtraCredit(internalAssignment.isExtraCredit());
    	if(internalAssignment.getCategory() != null) {
    		assignmentDefinition.setCategoryName(internalAssignment.getCategory().getName());
    		assignmentDefinition.setWeight(internalAssignment.getCategory().getWeight());
    		assignmentDefinition.setCategoryExtraCredit(internalAssignment.getCategory().isExtraCredit());
    	}
    	
    	assignmentDefinition.setUngraded(internalAssignment.getUngraded());
    	return assignmentDefinition;
    }   

	public Double getAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

		Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				
				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				
				// If this is the student, then the assignment needs to have
				// been released.
				if (studentRequestingOwnScore && !assignment.isReleased()) {
					log.error("AUTHORIZATION FAILURE: Student " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve score for unreleased assignment " + assignment.getName());
					throw new SecurityException("You do not have permission to perform this operation");					
				}
				
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
				if (gradeRecord == null) {
					return null;
				} else {
					return gradeRecord.getPointsEarned();
				}
			}
		});
		if (log.isDebugEnabled()) log.debug("returning " + assignmentScore);
		return assignmentScore;
	}
	
	public Double getAssignmentScore(final String gradebookUid, final Long gbItemId, final String studentUid) {
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScore");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with the gbItemId " + gbItemId);
		}
		
		return getAssignmentScore(gradebookUid, assignment.getName(), studentUid);
	}
	
	public GradeDefinition getGradeDefinitionForStudentForItem(final String gradebookUid,
			final Long gbItemId, final String studentUid) {
		
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("Null gradebookUid or gbItemId or studentUid" +
					" passed to getGradeDefinitionForStudentForItem");	
		}
		
		final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

		GradeDefinition gradeDef = (GradeDefinition)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, gbItemId, session);
	
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment with the gbItemId " 
							+ gbItemId + " in gradebook " + gradebookUid);
				}
				
				if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + gbItemId);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				
				Gradebook gradebook = assignment.getGradebook();
				
				GradeDefinition gradeDef = new GradeDefinition();
				gradeDef.setStudentUid(studentUid);
				gradeDef.setGradeEntryType(gradebook.getGrade_type());
				gradeDef.setGradeReleased(assignment.isReleased());
				
				// If this is the student, then the assignment needs to have
				// been released. Return null score information if not released
				if (studentRequestingOwnScore && !assignment.isReleased()) {
					gradeDef.setDateRecorded(null);
					gradeDef.setGrade(null);
					gradeDef.setGraderUid(null);
					gradeDef.setGradeComment(null);
					log.debug("Student " + getUserUid() + " in gradebook " + gradebookUid + " retrieving score for unreleased assignment " + assignment.getName());		
				
				} else {
				
					AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
					CommentDefinition gradeComment = getAssignmentScoreComment(gradebookUid, gbItemId, studentUid);
					String commentText = gradeComment != null ? gradeComment.getCommentText() : null;
					if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
					
					if (gradeRecord == null) {
						gradeDef.setDateRecorded(null);
						gradeDef.setGrade(null);
						gradeDef.setGraderUid(null);
						gradeDef.setGradeComment(commentText);
					} else {
						gradeDef.setDateRecorded(gradeRecord.getDateRecorded());
						gradeDef.setGraderUid(gradeRecord.getGraderId());
						gradeDef.setGradeComment(commentText);
						
						if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
							List<AssignmentGradeRecord> gradeList = new ArrayList<AssignmentGradeRecord>();
							gradeList.add(gradeRecord);
							convertPointsToLetterGrade(gradebook, gradeList);
							AssignmentGradeRecord gradeRec = (AssignmentGradeRecord)gradeList.get(0);
							if (gradeRec != null) {
								gradeDef.setGrade(gradeRec.getLetterEarned());
							}
						} else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
							Double percent = calculateEquivalentPercent(assignment.getPointsPossible(), gradeRecord.getPointsEarned());
							if (percent != null) {
								gradeDef.setGrade(percent.toString());
							}
						} else {
							if (gradeRecord.getPointsEarned() != null) {
								gradeDef.setGrade(gradeRecord.getPointsEarned().toString());
							}
						}
					}
				}
				
				return gradeDef;
			}
		});
		if (log.isDebugEnabled()) log.debug("returning grade def for " + studentUid);
		return gradeDef;
	}

	public void setAssignmentScore(final String gradebookUid, final String assignmentName, final String studentUid, final Double score, final String clientServiceDescription)
		throws GradebookNotFoundException, AssessmentNotFoundException {


		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentName + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription + " for item " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, score);
				} else {
					gradeRecord.setPointsEarned(score);
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);
				
				session.save(new GradingEvent(assignment, graderId, studentUid, score));
				
				// Sync database.
				session.flush();
				session.clear();
				return null;
			}
		});

		if (log.isDebugEnabled()) log.debug("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}
	
	public List getPastAutoReleaseDate() {
		return getHibernateTemplate().find("from org.sakaiproject.tool.gradebook.GradableObject where autoReleaseDate < CURRENT_TIMESTAMP()");
	}

	public String getGradebookDefinitionXml(String gradebookUid) {		
		Long gradebookId = getGradebook(gradebookUid).getId();
		Gradebook gradebook = getGradebook(gradebookUid);
		
		GradebookDefinition gradebookDefinition = new GradebookDefinition();
		GradeMapping selectedGradeMapping = gradebook.getSelectedGradeMapping();
		gradebookDefinition.setSelectedGradingScaleUid(selectedGradeMapping.getGradingScale().getUid());
		gradebookDefinition.setSelectedGradingScaleBottomPercents(new HashMap<String,Double>(selectedGradeMapping.getGradeMap()));
		gradebookDefinition.setAssignments(getAssignments(gradebookUid));
		
		gradebookDefinition.setGradeType(gradebook.getGrade_type());
		gradebookDefinition.setCategoryType(gradebook.getCategory_type());	
		gradebookDefinition.setCategory(getCategories(gradebookId));
		
		return VersionedExternalizable.toXml(gradebookDefinition);
	}
	
	public void transferGradebookDefinitionXml(String fromGradebookUid, String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		final Gradebook fromGradebook = getGradebook(fromGradebookUid);
		
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);
		
		gradebook.setCategory_type(gradebookDefinition.getCategoryType());
		gradebook.setGrade_type(gradebookDefinition.getGradeType());
		
		updateGradebook(gradebook);
		
		List category = getCategories(fromGradebook.getId());
	
		int assignmentsAddedCount = 0;
		Long catId = null;
		int undefined_nb = 0;
		
		List catList = gradebookDefinition.getCategory();
		List<Category> catList_tempt = new ArrayList<Category>();
				
		if(category.size() !=0) {
			//deal with category with assignments
			for(Iterator iter = category.iterator(); iter.hasNext();) {
			
				int categoryCount = 0;
				String catName = ((Category)iter.next()).getName();
		
				for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
					org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
					boolean newCategory = false;
					// Externally managed assessments should not be included.
					if (assignmentDef.isExternallyMaintained()) {
						continue;
					}
					
					if(catName.equals(assignmentDef.getCategoryName())) {
						newCategory = true;
						categoryCount++;
					}
		
					if (assignmentDef.getCategoryName() != null) {
						if(!newCategory) {}
						else if(newCategory && categoryCount == 1) {
							catId = createCategory(gradebook.getId(), assignmentDef.getCategoryName(), assignmentDef.getWeight(), 0, 0, 0, assignmentDef.isCategoryExtraCredit());
							Category catTempt = getCategory(catId);
							
							catList_tempt.add(catTempt);
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							
							assignmentsAddedCount++;
						}
						else{
							createAssignmentForCategory(gradebook.getId(), catId, assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							assignmentsAddedCount++;
						}
					
					}
					//deal with assignments in undefined.
					else {
						if (undefined_nb == 0) {
							createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
							assignmentsAddedCount++;
							
						}
					}
				}
				undefined_nb++;
			}
			
			//deal with Category without assignments inside
			Iterator it_tempt = catList_tempt.iterator();
			Iterator it = catList.iterator();
			
			Category cat_cat ;
			Category cat_tempt;
		
			while(it_tempt.hasNext()) {
				cat_tempt = (Category) it_tempt.next();
				while(it.hasNext()) {
					cat_cat = (Category) it.next();
					if(cat_tempt.getName().equals(cat_cat.getName())) {
						it.remove();
						it = catList.iterator();
					}	
				}
				it = catList.iterator();
			}
						
			Iterator itUpdate = catList.iterator();
			while(itUpdate.hasNext()){
				Category catObj = (Category)itUpdate.next();
				createCategory(gradebook.getId(), catObj.getName(), catObj.getWeight(), catObj.getDrop_lowest(), catObj.getDropHighest(), catObj.getKeepHighest(), catObj.isExtraCredit());				
			}
		}
		//deal with no categories
		else {
			for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
				org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
				
				// Externally managed assessments should not be included.
				if (assignmentDef.isExternallyMaintained()) {
					continue;
				}
				
				// All assignments should be unreleased even if they were released in the original.
			
				createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
				assignmentsAddedCount++;
			}	
			
		}
				
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
			// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
		for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	public void mergeGradebookDefinitionXml(String toGradebookUid, String fromGradebookXml) {
		final Gradebook gradebook = getGradebook(toGradebookUid);
		GradebookDefinition gradebookDefinition = (GradebookDefinition)VersionedExternalizable.fromXml(fromGradebookXml);

		List<String> assignmentNames = (List<String>)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(final Session session) throws HibernateException {
            	return session.createQuery(
            		"select asn.name from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
            		setLong(0, gradebook.getId().longValue()).
            		list();
            }
        });
		
		// Add any non-externally-managed assignments with non-duplicate names.
		int assignmentsAddedCount = 0;
		for (org.sakaiproject.service.gradebook.shared.Assignment obj : gradebookDefinition.getAssignments()) {
			org.sakaiproject.service.gradebook.shared.Assignment assignmentDef = (org.sakaiproject.service.gradebook.shared.Assignment)obj;
			
			// Externally managed assessments should not be included.
			if (assignmentDef.isExternallyMaintained()) {
				continue;
			}

			// Skip any input assignments with duplicate names.
			if (assignmentNames.contains(assignmentDef.getName())) {
				if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped duplicate assignment named " + assignmentDef.getName());
				continue;				
			}
			
			// All assignments should be unreleased even if they were released in the original.
			createAssignment(gradebook.getId(), assignmentDef.getName(), assignmentDef.getPoints(), assignmentDef.getDueDate(), true, false, assignmentDef.isExtraCredit());
			assignmentsAddedCount++;
		}
		if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " added " + assignmentsAddedCount + " assignments");
		
		// Carry over the old gradebook's selected grading scheme if possible.
		String fromGradingScaleUid = gradebookDefinition.getSelectedGradingScaleUid();
		MERGE_GRADE_MAPPING: if (!StringUtils.isEmpty(fromGradingScaleUid)) {
			for (GradeMapping gradeMapping : gradebook.getGradeMappings()) {
				if (gradeMapping.getGradingScale().getUid().equals(fromGradingScaleUid)) {
					// We have a match. Now make sure that the grades are as expected.
					Map<String, Double> inputGradePercents = gradebookDefinition.getSelectedGradingScaleBottomPercents();
					Set<String> gradeCodes = (Set<String>)inputGradePercents.keySet();
					if (gradeCodes.containsAll(gradeMapping.getGradeMap().keySet())) {
						// Modify the existing grade-to-percentage map.
						for (String gradeCode : gradeCodes) {
							gradeMapping.getGradeMap().put(gradeCode, inputGradePercents.get(gradeCode));							
						}
						gradebook.setSelectedGradeMapping(gradeMapping);
						updateGradebook(gradebook);
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " updated grade mapping");
					} else {
						if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because the " + fromGradingScaleUid + " grade codes did not match");
					}
					break MERGE_GRADE_MAPPING;
				}			
			}
			// Did not find a matching grading scale.
			if (log.isInfoEnabled()) log.info("Merge to gradebook " + toGradebookUid + " skipped grade mapping change because grading scale " + fromGradingScaleUid + " is not defined");
		}
	}
	
	public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
    	
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);
                Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);
                
                if(log.isInfoEnabled()) log.info("Assignment " + asn.getName() + " has been removed from " + gradebook);
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
        
    }
	
	public void addAssignment(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to add an assignment");
			throw new SecurityException("You do not have permission to perform this operation");
		}

        // Ensure that points is > zero.
		Double points = assignmentDefinition.getPoints();
        if ((points == null) || (points.doubleValue() <= 0)) {
            throw new AssignmentHasIllegalPointsException("Points must be > 0");
        }

		Gradebook gradebook = getGradebook(gradebookUid);
		createAssignment(gradebook.getId(), assignmentDefinition.getName(), points, assignmentDefinition.getDueDate(), !assignmentDefinition.isCounted(), assignmentDefinition.isReleased(), assignmentDefinition.isExtraCredit());
	}

	public void updateAssignment(final String gradebookUid, final String assignmentName, final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition) {		
		if (!getAuthz().isUserAbleToEditAssessments(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the definition of assignment " + assignmentName);
			throw new SecurityException("You do not have permission to perform this operation");
		}
		
		// This method is for Gradebook-managed assignments only.
		if (assignmentDefinition.isExternallyMaintained()) {
			log.error("User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to set assignment " + assignmentName + " to be externally maintained");
			throw new SecurityException("You do not have permission to perform this operation");
		}

		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to change the definition of externally maintained assignment " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}
				assignment.setCounted(assignmentDefinition.isCounted());
				assignment.setDueDate(assignmentDefinition.getDueDate());
				assignment.setAutoReleaseDate(assignmentDefinition.getAutoReleaseDate());
				assignment.setName(assignmentDefinition.getName().trim());
				assignment.setPointsPossible(assignmentDefinition.getPoints());
				assignment.setReleased(assignmentDefinition.isReleased());
				updateAssignment(assignment, session);
				return null;
			}
		});
	}

    public Authz getAuthz() {
        return authz;
    }
    public void setAuthz(Authz authz) {
        this.authz = authz;
    }
    
    public GradebookPermissionService getGradebookPermissionService() {
    	return gradebookPermissionService;
    }
    public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
    	this.gradebookPermissionService = gradebookPermissionService;
    }

    // Deprecated calls to new framework-specific interface.

	public void addGradebook(String uid, String name) {
		frameworkService.addGradebook(uid, name);
	}
	public void setAvailableGradingScales(Collection gradingScaleDefinitions) {
		frameworkService.setAvailableGradingScales(gradingScaleDefinitions);
	}
	public void setDefaultGradingScale(String uid) {
		frameworkService.setDefaultGradingScale(uid);
	}
	public void deleteGradebook( String uid)
		throws GradebookNotFoundException {
		frameworkService.deleteGradebook(uid);
	}
    public boolean isGradebookDefined(String gradebookUid) {
        return frameworkService.isGradebookDefined(gradebookUid);
    }

	public GradebookFrameworkService getFrameworkService() {
		return frameworkService;
	}
	public void setFrameworkService(GradebookFrameworkService frameworkService) {
		this.frameworkService = frameworkService;
	}

	// Deprecated calls to new interface for external assessment engines.

	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, double points, Date dueDate, String externalServiceDescription)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {
		externalAssessmentService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription);
	}
	public void addExternalAssessment(String gradebookUid, String externalId, String externalUrl,
			String title, Double points, Date dueDate, String externalServiceDescription, Boolean ungraded)
            throws ConflictingAssignmentNameException, ConflictingExternalIdException, GradebookNotFoundException {
		externalAssessmentService.addExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, externalServiceDescription, ungraded);
	}
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
                                         String title, double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
    	externalAssessmentService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, Double.valueOf(points), dueDate, Boolean.valueOf(false));
	}
    public void updateExternalAssessment(String gradebookUid, String externalId, String externalUrl,
        String title, Double points, Date dueDate) throws GradebookNotFoundException, AssessmentNotFoundException,AssignmentHasIllegalPointsException {
    	externalAssessmentService.updateExternalAssessment(gradebookUid, externalId, externalUrl, title, points, dueDate, Boolean.valueOf(false));
    }
	public void removeExternalAssessment(String gradebookUid,
            String externalId) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.removeExternalAssessment(gradebookUid, externalId);
	}
	public void updateExternalAssessmentScore(String gradebookUid, String externalId,
			String studentUid, Double points) throws GradebookNotFoundException, AssessmentNotFoundException {
	    String strPoints = (points == null ? null : points.toString());
		externalAssessmentService.updateExternalAssessmentScore(gradebookUid, externalId, studentUid, strPoints);
	}
	public void updateExternalAssessmentScores(String gradebookUid, String externalId, Map studentUidsToScores)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentScores(gradebookUid, externalId, studentUidsToScores);
	}
	
	public void updateExternalAssessmentComment(String gradebookUid, String externalId,
			String studentUid, String comment) throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentComment(gradebookUid, externalId, studentUid, comment);
	}
	public void updateExternalAssessmentComments(String gradebookUid, String externalId, Map studentUidsToComments)
		throws GradebookNotFoundException, AssessmentNotFoundException {
		externalAssessmentService.updateExternalAssessmentComments(gradebookUid, externalId, studentUidsToComments);
	}	

	public boolean isExternalAssignmentDefined(String gradebookUid, String externalId) throws GradebookNotFoundException {
		return externalAssessmentService.isExternalAssignmentDefined(gradebookUid, externalId);
	}

	public GradebookExternalAssessmentService getExternalAssessmentService() {
		return externalAssessmentService;
	}
	public void setExternalAssessmentService(
			GradebookExternalAssessmentService externalAssessmentService) {
		this.externalAssessmentService = externalAssessmentService;
	}


	/**
	 * @param gradebookUid
	 * @return A mapping from user display IDs to grades.  If no grade is available for a user, default to zero.
	 */
	public Map<String, String> getImportCourseGrade(String gradebookUid)
	{
		return getImportCourseGrade(gradebookUid, true, true);
	}

	/**
	 * @param gradebookUid
	 * @param useDefault If true, assume zero for missing grades.  Otherwise, null.
	 * @return A mapping from user display IDs to grades.
	 */
	public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault)
	{
		return getImportCourseGrade(gradebookUid, useDefault, true);
	}

	/**
	 * @param gradebookUid
	 * @param useDefault If true, assume zero for missing grades.  Otherwise, null.
	 * @param mapTheGrades If true, map the numerical grade to letter grade. If false, return a string of the numerical grade.
	 * @return A mapping from user display IDs to grades.
	 */
	public Map<String, String> getImportCourseGrade(String gradebookUid, boolean useDefault, boolean mapTheGrades)
	{
		HashMap<String, String> returnMap = new HashMap<String, String> ();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			Map<String, EnrollmentRecord> enrollmentMap = new HashMap<String, EnrollmentRecord>();

			Map<String, EnrollmentRecord> enrollmentMapUid = new HashMap<String, EnrollmentRecord>();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					// SAK-29243: if we are not mapping grades, we don't want letter grade here
					if(mapTheGrades && StringUtils.isNotBlank(gradeRecord.getEnteredGrade()))
					{
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getEnteredGrade());
					}
					else
					{
						if(!nonAssignment) {
							Double grade = null;

							if(useDefault) 
							{
								grade = gradeRecord.getNonNullAutoCalculatedGrade();
							}
							else
							{
								grade = gradeRecord.getAutoCalculatedGrade();
							}

							if(mapTheGrades)
							{
								returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(grade));
							}
							else
							{
								returnMap.put(enr.getUser().getDisplayId(), grade.toString());
							}

						}
					}
				}
			}
		}
		catch(Exception e)
		{
			log.error("Error in getImportCourseGrade", e);
		}
		return returnMap;
	}

	public CourseGrade getCourseGrade(Long gradebookId) {
		return (CourseGrade)getHibernateTemplate().find(
				"from CourseGrade as cg where cg.gradebook.id=?",
				gradebookId).get(0);
	}

	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				if(studentUids == null || studentUids.size() == 0) {
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs");
					return new ArrayList();
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Long gradebookId = courseGrade.getGradebook().getId();
				Gradebook gradebook = getGradebook(gradebookId);
				List cates = getCategories(gradebookId);
				
				// get all of the AssignmentGradeRecords here to avoid repeated db calls
    			Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(session, gradebookId, studentUids);
    			
    			// get all of the counted assignments
    			List<Assignment> assignments = getCountedAssignments(session, gradebookId);
    			List<Assignment> countedAssigns = new ArrayList<Assignment>();
    			if (assignments != null) {
    	                    for (Assignment assign : assignments) {
    	                        // extra check to account for new features like extra credit
    	                        if (assign.isIncludedInCalculations()) {
    	                            countedAssigns.add(assign);
    	                        }
    	                    }
    	                }
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) {
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
					List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());
    				
    				applyDropScores(studentGradeRecs);
					List totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs, countedAssigns);
					double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
					double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
					double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs, countedAssigns, false);
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}


	private List filterAndPopulateCourseGradeRecordsByStudents(CourseGrade courseGrade, Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		Set missingStudents = new HashSet(studentUids);
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
			if (studentUids.contains(cgr.getStudentId())) {
				filteredRecords.add(cgr);
				missingStudents.remove(cgr.getStudentId());
			}
		}
		for (Iterator iter = missingStudents.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			CourseGradeRecord cgr = new CourseGradeRecord(courseGrade, studentUid);
			filteredRecords.add(cgr);
		}
		return filteredRecords;
	}
	
	private double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<Assignment> countedAssigns, boolean literalTotal)
        {
            int gbGradeType = gradebook.getGrade_type();
            if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
            {
                if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
                return -1;
            }

            if (studentGradeRecs == null || countedAssigns == null) {
                if (log.isDebugEnabled()) log.debug("Returning 0 from getTotalPointsInternal " +
                        "since studentGradeRecs or countedAssigns was null");
                return 0;
            }

            double totalPointsPossible = 0;

            HashSet<Assignment> countedSet = new HashSet<Assignment>(countedAssigns);

            // we need to filter this list to identify only "counted" grade recs
            List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<AssignmentGradeRecord>();
            for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
                Assignment assign = gradeRec.getAssignment();
                boolean extraCredit = assign.isExtraCredit();
                if(gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY && assign.getCategory() != null && assign.getCategory().isExtraCredit())
                    extraCredit = true;

                if (assign.isCounted() && !assign.getUngraded() && !assign.isRemoved() && countedSet.contains(assign) &&
                        assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit) {
                    countedGradeRecs.add(gradeRec);
                }
            }

            Set assignmentsTaken = new HashSet();
            Set categoryTaken = new HashSet();
            for (AssignmentGradeRecord gradeRec : countedGradeRecs)
            {
                if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("")) 
                {
                    Double pointsEarned = new Double(gradeRec.getPointsEarned());
                    Assignment go = gradeRec.getAssignment();
                    if (pointsEarned != null) 
                    {
                        if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
                        {
                            assignmentsTaken.add(go.getId());
                        }
                        else if ((gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY || gradebook
                                .getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                                && go != null && categories != null)
                        {
                            //                              assignmentsTaken.add(go.getId());
                            //                          }
                            //                          else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
                            //                          {
                            for(int i=0; i<categories.size(); i++)
                            {
                                Category cate = (Category) categories.get(i);
                                if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()) && ((cate.isExtraCredit()!=null && !cate.isExtraCredit()) || cate.isExtraCredit()==null))
                                {
                                    assignmentsTaken.add(go.getId());
                                    categoryTaken.add(cate.getId());
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if(!assignmentsTaken.isEmpty())
            {
                if(!literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
                {
                    for(int i=0; i<categories.size(); i++)
                    {
                        Category cate = (Category) categories.get(i);
                        if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
                        {
                            totalPointsPossible += cate.getWeight().doubleValue();
                        }
                    }
                    return totalPointsPossible;
                }
                Iterator assignmentIter = countedAssigns.iterator();
                while (assignmentIter.hasNext()) 
                {
                    Assignment asn = (Assignment) assignmentIter.next();
                    if(asn != null)
                    {
                        Double pointsPossible = asn.getPointsPossible();

                        if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }
                        else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }else if(literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && assignmentsTaken.contains(asn.getId()))
                        {
                            totalPointsPossible += pointsPossible.doubleValue();
                        }
                    }
                }
            }
            else
                totalPointsPossible = -1;

            return totalPointsPossible;
        }

	       
	private List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories,
	        final List<AssignmentGradeRecord> gradeRecs, List<Assignment> countedAssigns) 
	{
	    int gbGradeType = gradebook.getGrade_type();
	    if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
	    {
	        if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
	        return new ArrayList();
	    }

	    if (gradeRecs == null || countedAssigns == null) {
	        if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for " +
	                "studentId=" + studentId + " returning 0 because null gradeRecs or countedAssigns");
	        List returnList = new ArrayList();
	        returnList.add(new Double(0));
	        returnList.add(new Double(0));
	        returnList.add(new Double(0)); // 3rd one is for the pre-adjusted course grade
	        return returnList;
	    }


	    double totalPointsEarned = 0;
	    BigDecimal literalTotalPointsEarned = new BigDecimal(0d);

	    Map cateScoreMap = new HashMap();
	    Map cateTotalScoreMap = new HashMap();

	    Set assignmentsTaken = new HashSet();
	    for (AssignmentGradeRecord gradeRec : gradeRecs)
	    {
	        if(gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade())
	        {
	            Assignment go = gradeRec.getAssignment();
	            if (go.isIncludedInCalculations() && countedAssigns.contains(go))
	            {
	                Double pointsEarned = new Double(gradeRec.getPointsEarned());
	                //if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
	                //{
	                if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
	                {
	                    totalPointsEarned += pointsEarned.doubleValue();
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
	                {
	                    totalPointsEarned += pointsEarned.doubleValue();
	                    literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                    assignmentsTaken.add(go.getId());
	                }
	                else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
	                {
	                    for(int i=0; i<categories.size(); i++)
	                    {
	                        Category cate = (Category) categories.get(i);
	                        if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
	                        {
	                            assignmentsTaken.add(go.getId());
	                            literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
	                            if(cateScoreMap.get(cate.getId()) != null)
	                            {
	                                cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
	                            }
	                            else
	                            {
	                                cateScoreMap.put(cate.getId(), new Double(pointsEarned));
	                            }
	                            break;
	                        }
	                    }
	                }
	            }
	        }                       
	    }

	    if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
	    {
	        Iterator assgnsIter = countedAssigns.iterator();
	        while (assgnsIter.hasNext()) 
	        {
	            Assignment asgn = (Assignment)assgnsIter.next();
	            if(assignmentsTaken.contains(asgn.getId()))
	            {
	                for(int i=0; i<categories.size(); i++)
	                {
	                    Category cate = (Category) categories.get(i);
	                    if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()) && !asgn.isExtraCredit())
	                    {

	                        if(cateTotalScoreMap.get(cate.getId()) == null)
	                        {                                                               
	                            cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
	                        }
	                        else
	                        {                                                               
	                            cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));                                                  
	                        }

	                    }
	                }
	            }
	        }
	    }

	    if(assignmentsTaken.isEmpty())
	        totalPointsEarned = -1;

	    if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
	    {
	        for(int i=0; i<categories.size(); i++)
	        {
	            Category cate = (Category) categories.get(i);
	            if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
	            {
	                totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
	            }
	        }
	    }

	    if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
	    List returnList = new ArrayList();
	    returnList.add(new Double(totalPointsEarned));
	    returnList.add(new Double((new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue()));

	    return returnList;
	}

	public Gradebook getGradebook(Long id) {
		return (Gradebook)getHibernateTemplate().load(Gradebook.class, id);
	}

	protected List getAssignmentsCounted(final Long gradebookId) throws HibernateException 
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List assignments = session.createQuery(
						"from Assignment as asn where asn.gradebook.id=? and asn.removed=false and asn.notCounted=false").
						setLong(0, gradebookId.longValue()).
						list();
				return assignments;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}
	
  public boolean checkStudentsNotSubmitted(String gradebookUid)
  {
  	Gradebook gradebook = getGradebook(gradebookUid);
  	Set studentUids = getAllStudentUids(getGradebookUid(gradebook.getId()));
  	if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY || gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY)
  	{
  		List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
  		List assigns = getAssignments(gradebook.getId(), Assignment.DEFAULT_SORT, true);
  		List filteredAssigns = new ArrayList();
  		for(Iterator iter = assigns.iterator(); iter.hasNext();)
  		{
  			Assignment assignment = (Assignment)iter.next();
  			if(assignment.isCounted() && !assignment.getUngraded())
  				filteredAssigns.add(assignment);
  		}
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(!agr.isCourseGradeRecord() && agr.getAssignment().isCounted() && !agr.getAssignment().getUngraded())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < (filteredAssigns.size() * studentUids.size()))
  			return true;
  		
  		return false;
  	}
  	else
  	{
    	List assigns = getAssignments(gradebook.getId(), Assignment.DEFAULT_SORT, true);
    	List records = getAllAssignmentGradeRecords(gradebook.getId(), studentUids);
    	Set filteredAssigns = new HashSet();
    	for (Iterator iter = assigns.iterator(); iter.hasNext(); )
    	{
    		Assignment assign = (Assignment) iter.next();
    		if(assign != null && assign.isCounted() && !assign.getUngraded())
    		{
    			if(assign.getCategory() != null && !assign.getCategory().isRemoved())
    			{
    				filteredAssigns.add(assign.getId());
    			}
    		}
    	}
    	
  		List filteredRecords = new ArrayList();
  		for(Iterator iter = records.iterator(); iter.hasNext();)
  		{
  			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
  			if(filteredAssigns.contains(agr.getAssignment().getId()) && !agr.isCourseGradeRecord())
  			{
  				if(agr.getPointsEarned() == null)
  					return true;
  				filteredRecords.add(agr);
  			}
  		}

  		if(filteredRecords.size() < filteredAssigns.size() * studentUids.size())
  			return true;

  		return false;
  	}
  }

  public List getAllAssignmentGradeRecords(final Long gradebookId, final Collection studentUids) {
  	HibernateCallback hc = new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			if(studentUids.size() == 0) {
  				// If there are no enrollments, no need to execute the query.
  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
  				return new ArrayList();
  			} else {
  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
  				"agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
  				q.setLong("gradebookId", gradebookId.longValue());
  				return filterGradeRecordsByStudents(q.list(), studentUids);
  			}
  		}
  	};
  	return (List)getHibernateTemplate().execute(hc);
  }
  
  private List getAllAssignmentGradeRecordsForGbItem(final Long gradableObjectId, 
		  final Collection studentUids) {
	  	HibernateCallback hc = new HibernateCallback() {
	  		public Object doInHibernate(Session session) throws HibernateException {
	  			if(studentUids.size() == 0) {
	  				// If there are no enrollments, no need to execute the query.
	  				if(log.isInfoEnabled()) log.info("No enrollments were specified.  Returning an empty List of grade records");
	  				return new ArrayList();
	  			} else {
	  				Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.removed=false and " +
	  				"agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
	  				q.setLong("gradableObjectId", gradableObjectId.longValue());
	  				return filterGradeRecordsByStudents(q.list(), studentUids);
	  			}
	  		}
	  	};
	  	return (List)getHibernateTemplate().execute(hc);
	  }

  public List getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
  	return (List)getHibernateTemplate().execute(new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			List assignments = getAssignments(gradebookId, session);

  			sortAssignments(assignments, sortBy, ascending);
  			return assignments;
  		}
  	});
  }
  private void sortAssignments(List assignments, String sortBy, boolean ascending) {
    // WARNING: AZ - this method is duplicated in GradebookManagerHibernateImpl
  	Comparator comp;
    if (Assignment.SORT_BY_NAME.equals(sortBy)) {
        comp = GradableObject.nameComparator;
    } else if(Assignment.SORT_BY_DATE.equals(sortBy)){
        comp = GradableObject.dateComparator;
    } else if(Assignment.SORT_BY_MEAN.equals(sortBy)) {
        comp = GradableObject.meanComparator;
    } else if(Assignment.SORT_BY_POINTS.equals(sortBy)) {
        comp = Assignment.pointsComparator;
    } else if(Assignment.SORT_BY_RELEASED.equals(sortBy)){
        comp = Assignment.releasedComparator;
    } else if(Assignment.SORT_BY_COUNTED.equals(sortBy)){
        comp = Assignment.countedComparator;
    } else if(Assignment.SORT_BY_EDITOR.equals(sortBy)){
        comp = Assignment.gradeEditorComparator;
    } else if (Assignment.SORT_BY_SORTING.equals(sortBy)) {
        comp = GradableObject.sortingComparator;
    } else {
        comp = GradableObject.defaultComparator;
    }
  	Collections.sort(assignments, comp);
  	if(!ascending) {
  		Collections.reverse(assignments);
  	}
  	if (log.isDebugEnabled()) {
  	    log.debug("sortAssignments: ordering by "+sortBy+" ("+comp+"), ascending="+ascending);
  	}
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.service.gradebook.shared.GradebookService#getViewableAssignmentsForCurrentUser(java.lang.String)
   */
  public List<org.sakaiproject.service.gradebook.shared.Assignment> getViewableAssignmentsForCurrentUser(String gradebookUid)
  throws GradebookNotFoundException {

	  List<Assignment> viewableAssignments = new ArrayList();
	  List<org.sakaiproject.service.gradebook.shared.Assignment> assignmentsToReturn = new ArrayList();

	  Gradebook gradebook = getGradebook(gradebookUid);

	  // will send back all assignments if user can grade all
	  if (getAuthz().isUserAbleToGradeAll(gradebookUid)) {
		  viewableAssignments = getAssignments(gradebook.getId(), null, true);
	  } else if (getAuthz().isUserAbleToGrade(gradebookUid)) {
		  // if user can grade and doesn't have grader perm restrictions, they
		  // may view all assigns
		  if (!getAuthz().isUserHasGraderPermissions(gradebookUid)) {
			  viewableAssignments = getAssignments(gradebook.getId(), null, true);
		  } else {
			  // this user has grader perms, so we need to filter the items returned
			  // if this gradebook has categories enabled, we need to check for category-specific restrictions

			  if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY) {
				  assignmentsToReturn = getAssignments(gradebookUid);
			  } else {

				  String userUid = getUserUid();
				  if (getGradebookPermissionService().getPermissionForUserForAllAssignment(gradebook.getId(), userUid)) {
					  assignmentsToReturn = getAssignments(gradebookUid);
				  }

				  // categories are enabled, so we need to check the category restrictions
				  List allCategories = getCategoriesWithAssignments(gradebook.getId());
				  if (allCategories != null && !allCategories.isEmpty()) {
					  List<Long> catIds = new ArrayList<Long>();
					  for (Category category : (List<Category>) allCategories) {
						  catIds.add(category.getId());
					  }
					  List<Long> viewableCategorieIds = getGradebookPermissionService().getCategoriesForUser(gradebook.getId(), userUid, catIds, gradebook.getCategory_type());
					  List<Category> viewableCategories = new ArrayList<Category>();
					  for (Category category : (List<Category>) allCategories) {
						  if(viewableCategorieIds.contains(category.getId())){
							  viewableCategories.add(category);
						  }
					  }
					  
					  for (Iterator catIter = viewableCategories.iterator(); catIter.hasNext();) {
						  Category cat = (Category) catIter.next();
						  if (cat != null) {
							  List assignments = cat.getAssignmentList();
							  if (assignments != null && !assignments.isEmpty()) {
								  viewableAssignments.addAll(assignments);
							  }
						  }
					  }
				  }
			  }
		  }
	  } else if (getAuthz().isUserAbleToViewOwnGrades(gradebookUid)) {
		  // if user is just a student, we need to filter out unreleased items
		  List allAssigns = getAssignments(gradebook.getId(), null, true);
		  if (allAssigns != null) {
			  for (Iterator aIter = allAssigns.iterator(); aIter.hasNext();) {
				  Assignment assign = (Assignment) aIter.next();
				  if (assign != null && assign.isReleased()) {
					  viewableAssignments.add(assign);
				  }
			  }
		  }
	  }

	  // Now we need to convert these to the assignment template objects
	  if (viewableAssignments != null && !viewableAssignments.isEmpty()) {
		  for (Iterator assignIter = viewableAssignments.iterator(); assignIter.hasNext();) {
			  Assignment assignment = (Assignment) assignIter.next();
			  assignmentsToReturn.add(getAssignmentDefinition(assignment));
		  }
	  }

	  return assignmentsToReturn;

  }
  
  public Map<String, String> getViewableStudentsForItemForCurrentUser(final String gradebookUid, final Long gradableObjectId) {
	  String userUid = authn.getUserUid();

	  return getViewableStudentsForItemForUser(userUid, gradebookUid, gradableObjectId);
  }
  
  public Map<String, String> getViewableStudentsForItemForUser(final String userUid, final String gradebookUid, final Long gradableObjectId) {

      if (gradebookUid == null || gradableObjectId == null || userUid == null) {
          throw new IllegalArgumentException("null gradebookUid or gradableObjectId or " +
                  "userId passed to getViewableStudentsForUserForItem." +
                  " gradebookUid: " + gradebookUid + " gradableObjectId:" + 
                  gradableObjectId + " userId: " + userUid);
      }
      
      if (!authz.isUserAbleToGrade(gradebookUid, userUid)) {
          return new HashMap();
      }

      Assignment gradebookItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException {
              return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
          }
      });

      if (gradebookItem == null) {
          log.debug("The gradebook item does not exist, so returning empty set");
          return new HashMap();
      }

      Long categoryId = gradebookItem.getCategory() == null ? null : gradebookItem.getCategory().getId();

      Map<EnrollmentRecord, String> enrRecFunctionMap  = authz.findMatchingEnrollmentsForItemForUser(userUid, gradebookUid, categoryId, getGradebook(gradebookUid).getCategory_type(), null, null);
      if (enrRecFunctionMap == null) {
          return new HashMap();
      }

      Map<String, String> studentIdFunctionMap = new HashMap();
      for (Iterator<Entry<EnrollmentRecord, String>> enrIter = enrRecFunctionMap.entrySet().iterator(); enrIter.hasNext();) {
    	  Entry<EnrollmentRecord, String> entry = enrIter.next();
          EnrollmentRecord enr = entry.getKey();
          if (enr != null && enrRecFunctionMap.get(enr) != null) {
              studentIdFunctionMap.put(enr.getUser().getUserUid(), entry.getValue());
          }
      }
      return studentIdFunctionMap;
  }

  public boolean isGradableObjectDefined(Long gradableObjectId) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to isGradableObjectDefined");
	  }
	  
	  return isAssignmentDefined(gradableObjectId);
  }
  
  public Map getViewableSectionUuidToNameMap(String gradebookUid) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid passed to getViewableSectionIdToNameMap");
	  }
	  
	  Map<String, String> sectionIdNameMap = new HashMap();
	  
	  List viewableCourseSections = getAuthz().getViewableSections(gradebookUid); 
	  if (viewableCourseSections == null || viewableCourseSections.isEmpty()) {
		  return sectionIdNameMap;
	  }
	  
	  for (Iterator sectionIter = viewableCourseSections.iterator(); sectionIter.hasNext();) {
		  CourseSection section = (CourseSection) sectionIter.next();
		  if (section != null) {
			  sectionIdNameMap.put(section.getUuid(), section.getTitle());
		  }
	  }
	  
	  return sectionIdNameMap;
  }
  
  public boolean currentUserHasGradeAllPerm(String gradebookUid) {
	  return authz.isUserAbleToGradeAll(gradebookUid);
  }
  
  public boolean isUserAllowedToGradeAll(String gradebookUid, String userUid) {
      return authz.isUserAbleToGradeAll(gradebookUid, userUid);
  }

  public boolean currentUserHasGradingPerm(String gradebookUid) {
	  return authz.isUserAbleToGrade(gradebookUid);
  }
  
  public boolean isUserAllowedToGrade(String gradebookUid, String userUid) {
      return authz.isUserAbleToGrade(gradebookUid, userUid);
  }

  public boolean currentUserHasEditPerm(String gradebookUid) {
	  return authz.isUserAbleToEditAssessments(gradebookUid);
  }

  public boolean currentUserHasViewOwnGradesPerm(String gradebookUid) {
	  return authz.isUserAbleToViewOwnGrades(gradebookUid);
  }
  
  public List<GradeDefinition> getGradesForStudentsForItem(final String gradebookUid, final Long gradableObjectId, List<String> studentIds) {
	  if (gradableObjectId == null) {
		  throw new IllegalArgumentException("null gradableObjectId passed to getGradesForStudentsForItem");
	  }
	  
	  List<org.sakaiproject.service.gradebook.shared.GradeDefinition> studentGrades = new ArrayList();
	  
	  if (studentIds != null && !studentIds.isEmpty()) {
		  // first, we need to make sure the current user is authorized to view the
		  // grades for all of the requested students
		  Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });
		  
		  if (gbItem != null) {
			  Gradebook gradebook = gbItem.getGradebook();
			  
			  if (!authz.isUserAbleToGrade(gradebook.getUid())) {
				  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information without permission in gb " + 
						  gradebook.getUid() + " using gradebookService.getGradesForStudentsForItem");
			  }
			  
			  Long categoryId = gbItem.getCategory() != null ? gbItem.getCategory().getId() : null;
			  Map enrRecFunctionMap = authz.findMatchingEnrollmentsForItem(gradebook.getUid(), categoryId, gradebook.getCategory_type(), null, null);
			  Set enrRecs = enrRecFunctionMap.keySet();
			  Map studentIdEnrRecMap = new HashMap();
			  if (enrRecs != null) {
				  for (Iterator enrIter = enrRecs.iterator(); enrIter.hasNext();) {
					  EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
					  if (enr != null) {
						  studentIdEnrRecMap.put(enr.getUser().getUserUid(), enr);
					  }
				  }
			  }
			  
			  for (Iterator stIter = studentIds.iterator(); stIter.hasNext();) {
				  String studentId = (String) stIter.next();
				  if (studentId != null) {
					  if (!studentIdEnrRecMap.containsKey(studentId)) {
						  throw new SecurityException("User " + authn.getUserUid() + 
						  " attempted to access grade information for student " + studentId + 
						  " without permission in gb " + gradebook.getUid() + 
						  " using gradebookService.getGradesForStudentsForItem");
					  }
				  }
			  }
			  
			  // retrieve the grading comments for all of the students
			  List<Comment> commentRecs = getComments(gbItem, studentIds);
			  Map<String, String> studentIdCommentTextMap = new HashMap();
			  if (commentRecs != null) {
				  for (Iterator<Comment> cIter = commentRecs.iterator(); cIter.hasNext();) {
					  Comment comment = cIter.next();
					  if (comment != null) {
						  studentIdCommentTextMap.put(comment.getStudentId(), comment.getCommentText());
					  }
				  }
			  }
			  
			  // now, we can populate the grade information
			  List<AssignmentGradeRecord> gradeRecs = getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIds);
			  if (gradeRecs != null) {
				  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					  convertPointsToLetterGrade(gradebook, gradeRecs);
				  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
					  convertPointsToPercentage(gradebook, gradeRecs);
				  }
				  
				  boolean gradeReleased = gradebook.isAssignmentsDisplayed() && gbItem.isReleased();
				  
				  for (Iterator gradeIter = gradeRecs.iterator(); gradeIter.hasNext();) {
					  AssignmentGradeRecord agr = (AssignmentGradeRecord) gradeIter.next();
					  if (agr != null) {
						  GradeDefinition gradeDef = new GradeDefinition();
						  gradeDef.setStudentUid(agr.getStudentId());
						  gradeDef.setGradeEntryType(gradebook.getGrade_type());
						  gradeDef.setGradeReleased(gradeReleased);
						  gradeDef.setGraderUid(agr.getGraderId());
						  gradeDef.setDateRecorded(agr.getDateRecorded());
						  
						  String commentText = studentIdCommentTextMap.get(agr.getStudentId());
						  if (commentText != null) {
							  gradeDef.setGradeComment(commentText);
						  }
						  
						  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
							  gradeDef.setGrade(agr.getLetterEarned());
						  } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
						      String grade = agr.getPercentEarned() != null ? agr.getPercentEarned().toString() : null;
							  gradeDef.setGrade(grade);
						  } else {
						      String grade = agr.getPointsEarned() != null ? agr.getPointsEarned().toString() : null;
							  gradeDef.setGrade(grade);
						  }
					  	  	
						  studentGrades.add(gradeDef);
					  }
				  }
			  }
		  }
	  }
	  
	  return studentGrades;
  }
  
  public boolean isGradeValid(String gradebookUuid, String grade) {
	  if (gradebookUuid == null) {
		  throw new IllegalArgumentException("Null gradebookUuid passed to isGradeValid");
	  }
	  Gradebook gradebook;
	  try {
		  gradebook = getGradebook(gradebookUuid);
	  } catch (GradebookNotFoundException gnfe) {
		  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
				  gradebookUuid + "Error: " + gnfe.getMessage());
	  }
	  
	  int gradeEntryType = gradebook.getGrade_type();
	  LetterGradePercentMapping mapping = null;
	  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
		  mapping = getLetterGradePercentMapping(gradebook);
	  }
	  
	  return isGradeValid(grade, gradeEntryType, mapping);
  }
  
  private boolean isGradeValid(String grade, int gradeEntryType, LetterGradePercentMapping gradeMapping) {

	  boolean gradeIsValid = false;

	  if (grade == null || "".equals(grade)) {

		  gradeIsValid = true;

	  } else {

		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS ||
				  gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			  try {
				  Double gradeAsDouble = Double.parseDouble(grade);
				  // grade must be greater than or equal to 0
				  if (gradeAsDouble.doubleValue() >= 0) {
					  // check that there are no more than 2 decimal places
					  String[] splitOnDecimal = grade.split("\\.");
					  if (splitOnDecimal == null || splitOnDecimal.length < 2) {
						  gradeIsValid = true;
					  } else if (splitOnDecimal.length == 2) {
						  String decimal = splitOnDecimal[1];
						  if (decimal.length() <= 2) {
							  gradeIsValid = true;
						  }
					  }
				  }
			  } catch (NumberFormatException nfe) {
				  if (log.isDebugEnabled()) log.debug("Passed grade is not a numeric value");
			  }

		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
			  if (gradeMapping == null) {
				  throw new IllegalArgumentException("Null mapping passed to isGradeValid for a letter grade-based gradeook");
			  }

			  String standardizedGrade = gradeMapping.standardizeInputGrade(grade);
			  if (standardizedGrade != null) {
				  gradeIsValid = true;
			  }
		  } else {
			  throw new IllegalArgumentException("Invalid gradeEntryType passed to isGradeValid");
		  }
	  }

	  return gradeIsValid;
  }

  public List<String> identifyStudentsWithInvalidGrades(String gradebookUid, Map<String, String> studentIdToGradeMap) {
	  if (gradebookUid == null) {
		  throw new IllegalArgumentException("null gradebookUid passed to identifyStudentsWithInvalidGrades");
	  }

	  List<String> studentsWithInvalidGrade = new ArrayList<String>();

	  if (studentIdToGradeMap != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid);
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  LetterGradePercentMapping gradeMapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  gradeMapping = getLetterGradePercentMapping(gradebook);
		  }

		  for (String studentId : studentIdToGradeMap.keySet()) {
			  String grade = studentIdToGradeMap.get(studentId);
			  if (!isGradeValid(grade, gradebook.getGrade_type(), gradeMapping)) {
				  studentsWithInvalidGrade.add(studentId);
			  }
		  }
	  }  
	  return studentsWithInvalidGrade;
  }

  public void saveGradeAndCommentForStudent(String gradebookUid, Long gradableObjectId, String studentUid, String grade, String comment) {
	  if (gradebookUid == null || gradableObjectId == null || studentUid == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId or studentUid passed to saveGradeAndCommentForStudent");
	  }

	  GradeDefinition gradeDef = new GradeDefinition();
	  gradeDef.setStudentUid(studentUid);
	  gradeDef.setGrade(grade);
	  gradeDef.setGradeComment(comment);

	  List<GradeDefinition> gradeDefList = new ArrayList<GradeDefinition>();
	  gradeDefList.add(gradeDef);

	  saveGradesAndComments(gradebookUid, gradableObjectId, gradeDefList);
  }

  public void saveGradesAndComments(final String gradebookUid, final Long gradableObjectId, List<GradeDefinition> gradeDefList) {
	  if (gradebookUid == null || gradableObjectId == null) {
		  throw new IllegalArgumentException("Null gradebookUid or gradableObjectId passed to saveGradesAndComments");
	  }

	  if (gradeDefList != null) {
		  Gradebook gradebook;

		  try {
			  gradebook = getGradebook(gradebookUid); 
		  } catch (GradebookNotFoundException gnfe) {
			  throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + 
					  gradebookUid + "Error: " + gnfe.getMessage());
		  }

		  Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			  public Object doInHibernate(Session session) throws HibernateException {
				  return getAssignmentWithoutStats(gradebookUid, gradableObjectId, session);
			  }
		  });

		  if (assignment == null) {
			  throw new AssessmentNotFoundException("No gradebook item exists with gradable object id = " + gradableObjectId);
		  }

		  if (!currentUserHasGradingPerm(gradebookUid)) {
			  log.warn("User attempted to save grades and comments without authorization");
			  throw new SecurityException("Current user is not authorized to save grades or comments in gradebook " + gradebookUid);
		  }

		  // let's identify all of the students being updated first
		  Map<String, GradeDefinition> studentIdGradeDefMap = new HashMap<String, GradeDefinition>();
		  Map<String, String> studentIdToGradeMap = new HashMap<String, String>();

		  for (GradeDefinition gradeDef: gradeDefList) {
			  studentIdGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
			  studentIdToGradeMap.put(gradeDef.getStudentUid(), gradeDef.getGrade());
		  }

		  // check for invalid grades
		  List invalidStudents = identifyStudentsWithInvalidGrades(gradebookUid, studentIdToGradeMap);
		  if (invalidStudents != null && !invalidStudents.isEmpty()) {
			  throw new InvalidGradeException ("At least one grade passed to be updated is " +
			  "invalid. No grades or comments were updated.");
		  }

		  boolean userHasGradeAllPerm = currentUserHasGradeAllPerm(gradebookUid);

		  // let's retrieve all of the existing grade recs for the given students
		  // and assignments
		  List<AssignmentGradeRecord> allGradeRecs = 
			  getAllAssignmentGradeRecordsForGbItem(gradableObjectId, studentIdGradeDefMap.keySet());


		  // put in map for easier accessibility
		  Map<String, AssignmentGradeRecord> studentIdToAgrMap = new HashMap<String, AssignmentGradeRecord>();
		  if (allGradeRecs != null) {
			  for (AssignmentGradeRecord rec : allGradeRecs) {
				  studentIdToAgrMap.put(rec.getStudentId(), rec);
			  }
		  }

		  // set up the grader and grade time
		  String graderId = getAuthn().getUserUid();
		  Date now = new Date();

		  // get grade mapping, if nec, to convert grades to points
		  LetterGradePercentMapping mapping = null;
		  if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
			  mapping = getLetterGradePercentMapping(gradebook);
		  }

		  // get all of the comments, as well
		  List<Comment> allComments = getComments(assignment, studentIdGradeDefMap.keySet());
		  // put in a map for easier accessibility
		  Map<String, Comment> studentIdCommentMap = new HashMap<String, Comment>();
		  if (allComments != null) {
			  for (Comment comment : allComments) {
				  studentIdCommentMap.put(comment.getStudentId(), comment);
			  }
		  }

		  // these are the records that will need to be updated. iterate through
		  // everything and then we'll save it all at once
		  Set<AssignmentGradeRecord> agrToUpdate = new HashSet<AssignmentGradeRecord>();
		  // do not use a HashSet b/c you may have multiple Comments with null id and the same comment at this point.
		  // the Comment object defines objects as equal if they have the same id, comment text, and gb item. the
		  // only difference may be the student ids
		  List<Comment> commentsToUpdate = new ArrayList<Comment>();
		  Set<GradingEvent> eventsToAdd = new HashSet<GradingEvent>();

		  for (GradeDefinition gradeDef : gradeDefList) {

			  String studentId = gradeDef.getStudentUid();

			  // check specific grading privileges if user does not have
			  // grade all perm
			  if (!userHasGradeAllPerm) {
				  if (!isUserAbleToGradeItemForStudent(gradebookUid, gradableObjectId, studentId)) {
					  log.warn("User " + graderId + " attempted to save a grade for " + studentId + 
					  " without authorization");

					  throw new SecurityException("User " + graderId + " attempted to save a grade for " + 
							  studentId + " without authorization");
				  }
			  }

			  Double convertedGrade = convertInputGradeToPoints(gradebook.getGrade_type(), mapping, assignment.getPointsPossible(), gradeDef.getGrade());

			  // let's see if this agr needs to be updated
			  AssignmentGradeRecord gradeRec = studentIdToAgrMap.get(studentId);
			  if (gradeRec != null) {
				  if ((convertedGrade == null && gradeRec.getPointsEarned() != null) || 
						  (convertedGrade != null && gradeRec.getPointsEarned() == null) ||
						  (convertedGrade != null && gradeRec.getPointsEarned() != null && 
								  !convertedGrade.equals(gradeRec.getPointsEarned()))) {
					  
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  } else {
				  // if the grade is something other than null, add a new AGR
				  if (gradeDef.getGrade() != null && !gradeDef.getGrade().trim().equals("")) {
					  gradeRec =  new AssignmentGradeRecord(assignment, studentId, convertedGrade);
					  gradeRec.setPointsEarned(convertedGrade);
					  gradeRec.setGraderId(graderId);
					  gradeRec.setDateRecorded(now);

					  agrToUpdate.add(gradeRec);

					  // we also need to add a GradingEvent
					  // the event stores the actual input grade, not the converted one
					  GradingEvent event = new GradingEvent(assignment, graderId, studentId, gradeDef.getGrade());
					  eventsToAdd.add(event);
				  }
			  }

			  // let's see if the comment needs to be updated
			  Comment comment = studentIdCommentMap.get(studentId);
			  if (comment != null) {
				  boolean oldCommentIsNull = comment.getCommentText() == null || comment.getCommentText().equals("");
				  boolean newCommentIsNull = gradeDef.getGradeComment() == null || gradeDef.getGradeComment().equals("");
				  
				  if ((oldCommentIsNull && !newCommentIsNull) || 
						  (!oldCommentIsNull && newCommentIsNull) ||
						  (!oldCommentIsNull && !newCommentIsNull && 
								  !gradeDef.getGradeComment().equals(comment.getCommentText()))) {
					  // update this comment
					  comment.setCommentText(gradeDef.getGradeComment());
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  } else {
				  // if there is a comment, add it
				  if (gradeDef.getGradeComment() != null && !gradeDef.getGradeComment().trim().equals("")) {
					  comment = new Comment(studentId, gradeDef.getGradeComment(), assignment);
					  comment.setGraderId(graderId);
					  comment.setDateRecorded(now);

					  commentsToUpdate.add(comment);
				  }
			  }
		  }

		  // now let's save them
		  try {
			  getHibernateTemplate().saveOrUpdateAll(agrToUpdate);
			  getHibernateTemplate().saveOrUpdateAll(commentsToUpdate);
			  getHibernateTemplate().saveOrUpdateAll(eventsToAdd);
		  }	catch (HibernateOptimisticLockingFailureException holfe) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(holfe);
		  } catch (StaleObjectStateException sose) {
			  if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to save scores and comments for gb Item " + gradableObjectId);
			  throw new StaleObjectModificationException(sose);
		  }
	  }
  }

  /**
   * 
   * @param gradeEntryType
   * @param mapping
   * @param gbItemPointsPossible
   * @param grade
   * @return given a generic String grade, converts it to the equivalent Double
   * point value that will be stored in the db based upon the gradebook's grade entry type
   */
  private Double convertInputGradeToPoints(int gradeEntryType, LetterGradePercentMapping mapping, 
		  Double gbItemPointsPossible, String grade) throws InvalidGradeException {
	  Double convertedValue = null;

	  if (grade != null && !"".equals(grade)) {
		  if (gradeEntryType == GradebookService.GRADE_TYPE_POINTS) {
			  try {
				  Double pointValue = Double.parseDouble(grade);
				  convertedValue = pointValue;
			  } catch (NumberFormatException nfe) {
				  throw new InvalidGradeException("Invalid grade passed to convertInputGradeToPoints");
			  }
		  } else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE ||
				  gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {

			  // for letter or %-based grading, we need to calculate the equivalent point value
			  if (gbItemPointsPossible == null) {
				  throw new IllegalArgumentException("Null points possible passed" +
				  " to convertInputGradeToPoints for letter or % based grading");
			  }

			  Double percentage = null;
			  if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
				  if (mapping == null) {
					  throw new IllegalArgumentException("No mapping passed to convertInputGradeToPoints for a letter-based gb");
				  }

				  if(mapping.getGradeMap() != null)
				  {
				      // standardize the grade mapping
				      String standardizedGrade = mapping.standardizeInputGrade(grade);
					  percentage = mapping.getValue(standardizedGrade);
					  if(percentage == null)
					  {
						  throw new IllegalArgumentException("Invalid grade passed to convertInputGradeToPoints");
					  }
				  }
			  } else {
				  try {
					  percentage = Double.parseDouble(grade);
				  } catch (NumberFormatException nfe) {
					  throw new IllegalArgumentException("Invalid % grade passed to convertInputGradeToPoints");
				  }
			  }

			  convertedValue = calculateEquivalentPointValueForPercent(gbItemPointsPossible, percentage);

		  } else {
			  throw new InvalidGradeException("invalid grade entry type passed to convertInputGradeToPoints");
		  }
	  }

	  return convertedValue;
  }
	
	public int getGradeEntryType(String gradebookUid) {
		if (gradebookUid == null) {
			throw new IllegalArgumentException("null gradebookUid passed to getGradeEntryType");
		}
		
		try {
			Gradebook gradebook = getGradebook(gradebookUid);
			return gradebook.getGrade_type();
		} catch (GradebookNotFoundException gnfe) {
			throw new GradebookNotFoundException("No gradebook exists with the given gradebookUid: " + gradebookUid);
		}
	}

	
	public Map getEnteredCourseGrade(final String gradebookUid)
	{
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Gradebook thisGradebook = getGradebook(gradebookUid);

				Long gradebookId = thisGradebook.getId();
				CourseGrade courseGrade = getCourseGrade(gradebookId);

				Map enrollmentMap;

				Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
				enrollmentMap = new HashMap();

				Map enrollmentMapUid = new HashMap();
				for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
				{
					EnrollmentRecord enr = (EnrollmentRecord)iter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
					enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), enrollmentMap.keySet());

				Map returnMap = new HashMap();

				for(int i=0; i<records.size(); i++)
				{
					CourseGradeRecord cgr = (CourseGradeRecord) records.get(i);
					if(cgr.getEnteredGrade() != null && !cgr.getEnteredGrade().equalsIgnoreCase(""))
					{		
						EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(cgr.getStudentId());
						if(enr != null)
						{
							returnMap.put(enr.getUser().getDisplayId(), cgr.getEnteredGrade());
						}
					}
				}

				return returnMap;
			}
		};
		return (Map)getHibernateTemplate().execute(hc);		
	}
	
	public Map getCalculatedCourseGrade(String gradebookUid) {
		return getCalculatedCourseGrade (gradebookUid, true);
	}

	public Map getCalculatedCourseGrade(String gradebookUid, boolean mapTheGrades) {
		HashMap returnMap = new HashMap();

		try
		{
			Gradebook thisGradebook = getGradebook(gradebookUid);
			
			List assignList = getAssignmentsCounted(thisGradebook.getId());
			boolean nonAssignment = false;
			if(assignList == null || assignList.size() < 1)
			{
				nonAssignment = true;
			}
			
			Long gradebookId = thisGradebook.getId();
			CourseGrade courseGrade = getCourseGrade(gradebookId);

			Map enrollmentMap;
			
			Map viewableEnrollmentsMap = authz.findMatchingEnrollmentsForViewableCourseGrade(gradebookUid, thisGradebook.getCategory_type(), null, null);
			enrollmentMap = new HashMap();

			Map enrollmentMapUid = new HashMap();
			for (Iterator iter = viewableEnrollmentsMap.keySet().iterator(); iter.hasNext(); ) 
			{
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				enrollmentMap.put(enr.getUser().getUserUid(), enr);
				enrollmentMapUid.put(enr.getUser().getUserUid(), enr);
			}
			List gradeRecords = getPointsEarnedCourseGradeRecords(courseGrade, enrollmentMap.keySet());
			for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) 
			{
				CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();

				GradeMapping gradeMap= thisGradebook.getSelectedGradeMapping();

				EnrollmentRecord enr = (EnrollmentRecord)enrollmentMapUid.get(gradeRecord.getStudentId());
				if(enr != null)
				{
					if(!nonAssignment) {
					  if (mapTheGrades) {
						returnMap.put(enr.getUser().getDisplayId(), (String)gradeMap.getGrade(gradeRecord.getNonNullAutoCalculatedGrade()));
					  } 
					  else {
						returnMap.put(enr.getUser().getDisplayId(), gradeRecord.getNonNullAutoCalculatedGrade().toString());
					  }
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return returnMap;
	}

  public String getAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid) 
  throws GradebookNotFoundException, AssessmentNotFoundException
  {
  	final boolean studentRequestingOwnScore = authn.getUserUid().equals(studentUid);

  	Double assignmentScore = (Double)getHibernateTemplate().execute(new HibernateCallback() {
  		public Object doInHibernate(Session session) throws HibernateException {
  			Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
  			if (assignment == null) {
  				throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
  			}

  			if (!studentRequestingOwnScore && !isUserAbleToViewItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
  				log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve grade for student " + studentUid + " for assignment " + assignmentName);
  				throw new SecurityException("You do not have permission to perform this operation");
  			}

  			// If this is the student, then the assignment needs to have
  			// been released.
  			if (studentRequestingOwnScore && !assignment.isReleased()) {
  				log.error("AUTHORIZATION FAILURE: Student " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve score for unreleased assignment " + assignment.getName());
  				throw new SecurityException("You do not have permission to perform this operation");					
  			}

  			AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
  			if (log.isDebugEnabled()) log.debug("gradeRecord=" + gradeRecord);
  			if (gradeRecord == null) {
  				return null;
  			} else {
  				return gradeRecord.getPointsEarned();
  			}
  		}
  	});
  	if (log.isDebugEnabled()) log.debug("returning " + assignmentScore);
  	
  	//TODO: when ungraded items is considered, change column to ungraded-grade 
  	//its possible that the assignment score is null
  	if (assignmentScore == null)
  		return null;
  	
  	return Double.valueOf(assignmentScore).toString();
  }

  public String getAssignmentScoreString(final String gradebookUid, final Long gbItemId, String studentUid) 
  throws GradebookNotFoundException, AssessmentNotFoundException
  {
		if (gradebookUid == null || gbItemId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScore");
		}
		
		Assignment assignment = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getAssignmentWithoutStats(gradebookUid, gbItemId, session);
			}
		});
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignment with the gbItemId " + gbItemId);
		}
		
		return getAssignmentScoreString(gradebookUid, assignment.getName(), studentUid);
  }

	public void setAssignmentScoreString(final String gradebookUid, final String assignmentName, final String studentUid, final String score, final String clientServiceDescription) 
	throws GradebookNotFoundException, AssessmentNotFoundException 
	{
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Assignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentName, session);
				if (assignment == null) {
					throw new AssessmentNotFoundException("There is no assignment named " + assignmentName + " in gradebook " + gradebookUid);
				}
				if (assignment.isExternallyMaintained()) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade externally maintained assignment " + assignmentName + " from " + clientServiceDescription);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				if (!isUserAbleToGradeItemForStudent(gradebookUid, assignment.getId(), studentUid)) {
					log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to grade student " + studentUid + " from " + clientServiceDescription + " for item " + assignmentName);
					throw new SecurityException("You do not have permission to perform this operation");
				}

				Date now = new Date();
				String graderId = getAuthn().getUserUid();
				AssignmentGradeRecord gradeRecord = getAssignmentGradeRecord(assignment, studentUid, session);
				if (gradeRecord == null) {
					// Creating a new grade record.
					gradeRecord = new AssignmentGradeRecord(assignment, studentUid, convertStringToDouble(score));
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
				} else {
					//TODO: test if it's ungraded item or not. if yes, set ungraded grade for this record. if not, need validation??
					gradeRecord.setPointsEarned(convertStringToDouble(score));
				}
				gradeRecord.setGraderId(graderId);
				gradeRecord.setDateRecorded(now);
				session.saveOrUpdate(gradeRecord);
				
				session.save(new GradingEvent(assignment, graderId, studentUid, score));
				
				// Sync database.
				session.flush();
				session.clear();

           		// Post an event in SAKAI_EVENT table
           		postUpdateGradeEvent(gradebookUid, assignmentName, studentUid, convertStringToDouble(score));
				return null;
			}
		});

		if (log.isInfoEnabled()) log.info("Score updated in gradebookUid=" + gradebookUid + ", assignmentName=" + assignmentName + " by userUid=" + getUserUid() + " from client=" + clientServiceDescription + ", new score=" + score);
	}

	public void finalizeGrades(String gradebookUid)
			throws GradebookNotFoundException {
		if (!getAuthz().isUserAbleToGradeAll(gradebookUid)) {
			log.error("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to finalize grades");
			throw new SecurityException("You do not have permission to perform this operation");
		}
		finalizeNullGradeRecords(getGradebook(gradebookUid));
	}
	
	public String getLowestPossibleGradeForGbItem(final String gradebookUid, final Long gradebookItemId) {
	    if (gradebookUid == null || gradebookItemId == null) {
	        throw new IllegalArgumentException("Null gradebookUid and/or gradebookItemId " +
	        		"passed to getLowestPossibleGradeForGbItem. gradebookUid:" + 
	        		gradebookUid + " gradebookItemId:" + gradebookItemId);
	    }
	    
	    Assignment gbItem = (Assignment)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                return getAssignmentWithoutStats(gradebookUid, gradebookItemId, session);
            }
        });
	    
	    if (gbItem == null) {
	        throw new AssessmentNotFoundException("No gradebook item found with id " + gradebookItemId);
	    }
	    
	    Gradebook gradebook = gbItem.getGradebook();
	    
	    // double check that user has some permission to access gb items in this site
	    if (!isUserAbleToViewAssignments(gradebookUid) && !currentUserHasViewOwnGradesPerm(gradebookUid)) {
	        throw new SecurityException("User attempted to access gradebookItem: " + 
	                gradebookItemId + " in gradebook:" + gradebookUid + " without permission!");
	    }
	    
	    String lowestPossibleGrade = null;
	    
	    if (gbItem.getUngraded()) {
	        lowestPossibleGrade = null;
	    } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE || 
	            gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
	        lowestPossibleGrade = "0";
	    } else if (gbItem.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
	        LetterGradePercentMapping mapping = getLetterGradePercentMapping(gradebook);
	        lowestPossibleGrade = mapping.getGrade(0d);
	    }
	    
	    return lowestPossibleGrade;
	}
	
	public List<CategoryDefinition> getCategoryDefinitions(String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getCategoryDefinitions");
	    }

	    if (!isUserAbleToViewAssignments(gradebookUid)) {
	        log.warn("AUTHORIZATION FAILURE: User " + getUserUid() + " in gradebook " + gradebookUid + " attempted to retrieve all categories without permission");
	        throw new SecurityException("You do not have permission to perform this operation");
	    }

	    List<CategoryDefinition> categoryDefList = new ArrayList<CategoryDefinition>();

	    List<Category> gbCategories = getCategories(getGradebook(gradebookUid).getId());
	    if (gbCategories != null) {
	        for (Category category : gbCategories) {
	            categoryDefList.add(getCategoryDefinition(category));
	        }
	    }

	    return categoryDefList;
	}

	private CategoryDefinition getCategoryDefinition(Category category) {
	    CategoryDefinition categoryDef = new CategoryDefinition();
	    if (category != null) {
	        categoryDef.setId(category.getId());
	        categoryDef.setName(category.getName());
	        categoryDef.setWeight(category.getWeight());
	        categoryDef.setAssignmentList(getAssignments(category.getGradebook().getUid(), category.getName()));
	    }

	    return categoryDef;
	}

	public boolean checkStuendsNotSubmitted(String gradebookUid) {
		return checkStudentsNotSubmitted(gradebookUid);
	}


	/**
	 * 
	 * @param session
	 * @param gradebookId
	 * @param studentUids
	 * @return a map of studentUid to a list of that student's AssignmentGradeRecords for the given studentUids list
	 * in the given gradebook.  the grade records are all recs for assignments that are not removed and
	 * have a points possible > 0
	 */
	protected Map<String,List<AssignmentGradeRecord>> getGradeRecordMapForStudents(Session session, Long gradebookId, Collection<String> studentUids) {
	    Map<String,List<AssignmentGradeRecord>> filteredGradeRecs = new HashMap<String,List<AssignmentGradeRecord>>();
	    if (studentUids != null) {
	        List<AssignmentGradeRecord> allGradeRecs = new ArrayList<AssignmentGradeRecord>();

	        if (studentUids.size() >= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
	            allGradeRecs = session.createQuery(
	                    "from AssignmentGradeRecord agr where agr.gradableObject.gradebook.id=:gbid " +
	                    "and agr.gradableObject.removed=false").
	                    setParameter("gbid", gradebookId).
	                    list();
	        } else {
	            String query = "from AssignmentGradeRecord agr where agr.gradableObject.gradebook.id=:gbid and " +
	            "agr.gradableObject.removed=false and " +
	            "agr.studentId in (:studentUids)";
	            
	            allGradeRecs = session.createQuery(
	                    query).
	                    setParameter("gbid", gradebookId).
	                    setParameterList("studentUids", studentUids).
	                    list();
	        }

	        if (allGradeRecs != null) {
	            for (AssignmentGradeRecord gradeRec : allGradeRecs) {
	                if (studentUids.contains(gradeRec.getStudentId())) {
	                    String studentId = gradeRec.getStudentId();
	                    List<AssignmentGradeRecord> gradeRecList = filteredGradeRecs.get(studentId);
	                    if (gradeRecList == null) {
	                        gradeRecList = new ArrayList<AssignmentGradeRecord>();
	                        gradeRecList.add(gradeRec);
	                        filteredGradeRecs.put(studentId, gradeRecList);
	                    } else {
	                        gradeRecList.add(gradeRec);
	                        filteredGradeRecs.put(studentId, gradeRecList);
	                    }
	                }
	            }
	        }
	    }

	    return filteredGradeRecs;
	}
	
	/**
	 * 
	 * @param session
	 * @param gradebookId
	 * @return a list of Assignments that have not been removed, are "counted", graded,
	 * and have a points possible > 0
	 */
	protected List<Assignment> getCountedAssignments(Session session, Long gradebookId) {
	    List<Assignment> assignList = new ArrayList<Assignment>();
	    
	    List <Assignment>results = session.createQuery(
        "from Assignment as asn where asn.gradebook.id=:gbid and asn.removed=false and " +
        "asn.notCounted=false and asn.ungraded=false").
        setParameter("gbid", gradebookId).
        list();
	    
	    if (results != null) {
	    	// making sure there's no invalid points possible for normal assignments
	    	for (Assignment a : results)
	    	{
	    		
	    		if (a.getPointsPossible()!=null && a.getPointsPossible()>0)
	    		{
	    			assignList.add(a);
	    		}
	    	}
	    }
	    
	    return assignList;
	}
	
	/**
     * set the droppedFromGrade attribute of each 
     * of the n highest and the n lowest scores of a 
     * student based on the assignment's category
     * @param gradeRecords
     * @return void
     */
    public void applyDropScores(Collection<AssignmentGradeRecord> gradeRecords) {
        if(gradeRecords == null || gradeRecords.size() < 1) {
            return;
        }
        long start = System.currentTimeMillis();
        
        List<String> studentIds = new ArrayList<String>();
        List<Category> categories = new ArrayList<Category>();
        Map<String, List<AssignmentGradeRecord>> gradeRecordMap = new HashMap<String, List<AssignmentGradeRecord>>();
        for(AssignmentGradeRecord gradeRecord : gradeRecords) {
            
            if(gradeRecord == null 
                    || gradeRecord.getPointsEarned() == null) { // don't consider grades that have null pointsEarned (this occurs when a previously entered score for an assignment is removed; record stays in database) 
                continue;
            }
            
            // reset
            gradeRecord.setDroppedFromGrade(false);
            
            Assignment assignment = gradeRecord.getAssignment();
            if(assignment.getUngraded()  // GradebookService.GRADE_TYPE_LETTER
                    || assignment.isNotCounted() // don't consider grades that are not counted toward course grade
                    || assignment.getItemType().equals(Assignment.item_type_adjustment)
                    || assignment.isRemoved()) {
                continue;
            }
            // get all the students represented
            String studentId = gradeRecord.getStudentId();
            if(!studentIds.contains(studentId)) {
                studentIds.add(studentId);
            }
            // get all the categories represented
            Category cat = gradeRecord.getAssignment().getCategory();
            if(cat != null) {
                if(!categories.contains(cat)) {
                    categories.add(cat);
                }
                List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                if(gradeRecordsByCatAndStudent == null) {
                    gradeRecordsByCatAndStudent = new ArrayList<AssignmentGradeRecord>();
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                    gradeRecordMap.put(studentId + cat.getId(), gradeRecordsByCatAndStudent);
                } else {
                    gradeRecordsByCatAndStudent.add(gradeRecord);
                }
            }            
        }
        
        if(categories == null || categories.size() < 1) {
            return;
        }
        for(Category cat : categories) {
            Integer dropHighest = cat.getDropHighest();
            Integer dropLowest = cat.getDrop_lowest();
            Integer keepHighest = cat.getKeepHighest();
            Long catId = cat.getId();
            
            if((dropHighest != null && dropHighest > 0) || (dropLowest != null && dropLowest > 0) || (keepHighest != null && keepHighest > 0)) {
                
                for(String studentId : studentIds) {
                    // get the student's gradeRecords for this category
                    List<AssignmentGradeRecord> gradesByCategory = new ArrayList<AssignmentGradeRecord>();
                    List<AssignmentGradeRecord> gradeRecordsByCatAndStudent = gradeRecordMap.get(studentId + cat.getId());
                    if(gradeRecordsByCatAndStudent != null) {
                        gradesByCategory.addAll(gradeRecordsByCatAndStudent);
                    
                        int numGrades = gradesByCategory.size();
                        
                        if(dropHighest > 0 && numGrades > dropHighest + dropLowest) {
                            for(int i=0; i<dropHighest; i++) {
                                AssignmentGradeRecord highest = Collections.max(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                highest.setDroppedFromGrade(true);
                                gradesByCategory.remove(highest);
                                if(log.isDebugEnabled()) log.debug("dropHighest applied to " + highest);
                            }
                        }
                        
                        if(keepHighest > 0 && numGrades > (gradesByCategory.size() - keepHighest)) {
                            dropLowest = gradesByCategory.size() - keepHighest;
                        }
                        
                        if(dropLowest > 0 &&  numGrades > dropLowest + dropHighest) {
                            for(int i=0; i<dropLowest; i++) {
                                AssignmentGradeRecord lowest = Collections.min(gradesByCategory, AssignmentGradeRecord.numericComparator);
                                lowest.setDroppedFromGrade(true);
                                gradesByCategory.remove(lowest);
                                if(log.isDebugEnabled()) log.debug("dropLowest applied to " + lowest);
                            }
                        }
                    }
                }
                if(log.isDebugEnabled()) log.debug("processed " + studentIds.size() + "students in category " + cat.getId());
            }
        }
        
        if(log.isDebugEnabled()) log.debug("GradebookManager.applyDropScores took " + (System.currentTimeMillis() - start) + " millis to execute");
    }
    
    public PointsPossibleValidation isPointsPossibleValid(String gradebookUid, org.sakaiproject.service.gradebook.shared.Assignment gradebookItem, 
            Double pointsPossible) {
        if (gradebookUid == null) {
            throw new IllegalArgumentException("Null gradebookUid passed to isPointsPossibleValid");
        }
        if (gradebookItem == null) {
            throw new IllegalArgumentException("Null gradebookItem passed to isPointsPossibleValid");
        }

        // At this time, all gradebook items follow the same business rules for
        // points possible (aka relative weight in % gradebooks) so special logic 
        // using the properties of the gradebook item is unnecessary. 
        // In the future, we will have the flexibility to change
        // that behavior without changing the method signature

        // the points possible must be a non-null value greater than 0 with
        // no more than 2 decimal places
        
        if (pointsPossible == null) {
            return PointsPossibleValidation.INVALID_NULL_VALUE;
        }
        
        if (pointsPossible.doubleValue() <= 0) {
            return PointsPossibleValidation.INVALID_NUMERIC_VALUE;
        }
        // ensure there are no more than 2 decimal places
        BigDecimal bd = new BigDecimal(pointsPossible.doubleValue());
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
        double roundedVal = bd.doubleValue();
        double diff = pointsPossible - roundedVal;
        if (diff != 0) {
            return PointsPossibleValidation.INVALID_DECIMAL;
        }

        return PointsPossibleValidation.VALID;
    }

    /**
	 *
	 * @param doubleAsString
	 * @return a locale-aware Double value representation of the given String
	 * @throws ParseException
	 */
	private Double convertStringToDouble(String doubleAsString) {
	    Double scoreAsDouble = null;
	    if (doubleAsString != null && !"".equals(doubleAsString)) {
	        try {
	        	NumberFormat numberFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
				Number numericScore = numberFormat.parse(doubleAsString.trim());
				scoreAsDouble = numericScore.doubleValue();
			} catch (ParseException e) {
				log.error(e);
			}
	    }

	    return scoreAsDouble;
	}
	
	/**
	 * Get a list of assignments in the gradebook attached to the given category.
	 * Note that each assignment only knows the category by name.
	 * 
	 * <p>Note also that this is different to {@link BaseHibernateManager#getAssignmentsForCategory(Long)} because this method returns the shared Assignment object.
	 * 
	 * @param gradebookUid
	 * @param categoryName
	 * @return
	 */
	private List<org.sakaiproject.service.gradebook.shared.Assignment> getAssignments(String gradebookUid, String categoryName) {
		
		List<org.sakaiproject.service.gradebook.shared.Assignment> allAssignments = getAssignments(gradebookUid);
		List<org.sakaiproject.service.gradebook.shared.Assignment> matchingAssignments = new ArrayList<org.sakaiproject.service.gradebook.shared.Assignment>();
		
		for(org.sakaiproject.service.gradebook.shared.Assignment assignment: allAssignments) {
			if(StringUtils.equals(assignment.getCategoryName(), categoryName)) {
				matchingAssignments.add(assignment);
			}
		}
		return matchingAssignments;
	}
	
	/**
	 * Post an event to Sakai's event table
	 * 
	 * @param gradebookUid
	 * @param assignmentName
	 * @param studentUid
	 * @param pointsEarned
	 * @return
	 */
	private void postUpdateGradeEvent(String gradebookUid, String assignmentName, String studentUid, Double pointsEarned) {
	    if (eventTrackingService != null) {
            eventTrackingService.postEvent("gradebook.updateItemScore","/gradebook/"+gradebookUid+"/"+assignmentName+"/"+studentUid+"/"+pointsEarned+"/student");
        }
    }
	
	/**
	 * Retrieves the calculated average course grade.
	 */
	public String getAverageCourseGrade(String gradebookUid) {
	    if (gradebookUid == null) {
	        throw new IllegalArgumentException("Null gradebookUid passed to getAverageCourseGrade");
	    }
	    // Check user has permission to invoke method.
	    if (!currentUserHasGradeAllPerm(gradebookUid)) {
	    	StringBuilder sb = new StringBuilder()
	    	.append("User ")
	    	.append(authn.getUserUid())
	    	.append(" attempted to access the average course grade without permission in gb ")
	    	.append(gradebookUid)
	    	.append(" using gradebookService.getAverageCourseGrade");
	        throw new SecurityException(sb.toString());
	    }
	    
	    String courseGradeLetter = null;
	    Gradebook gradebook = getGradebook(gradebookUid);
	    if (gradebook != null) {
		    CourseGrade courseGrade = getCourseGrade(gradebook.getId());
		    Set<String> studentUids = getAllStudentUids(gradebookUid);
		    // This call handles the complex rules of which assignments and grades to include in the calculation
		    List<CourseGradeRecord> courseGradeRecs = getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
		    if (courseGrade != null) {
		    	// Calculate the course mean grade whether the student grade was manually entered or auto-calculated.
		    	courseGrade.calculateStatistics(courseGradeRecs, studentUids.size());
			    if (courseGrade.getMean() != null) {
			        courseGradeLetter = gradebook.getSelectedGradeMapping().getGrade(courseGrade.getMean());
			    }
		    }
		    
	    }
	    return courseGradeLetter;
	}
	
}
