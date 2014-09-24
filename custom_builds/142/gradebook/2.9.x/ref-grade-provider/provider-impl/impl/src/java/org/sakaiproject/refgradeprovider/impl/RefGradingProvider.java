package org.sakaiproject.refgradeprovider.impl;

import org.sakaiproject.service.gradebook.shared.ExternalGradeProvider;
import org.sakaiproject.service.gradebook.shared.GradingPeriod;
import org.sakaiproject.service.gradebook.shared.ExternalProviderException;
import org.sakaiproject.refgradeprovider.model.RefGradingPeriod;
import org.sakaiproject.refgradeprovider.model.ExternalGrade;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.event.cover.UsageSessionService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 4, 2008
 * Time: 11:00:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefGradingProvider extends HibernateDaoSupport implements ExternalGradeProvider {

   private List<GradingPeriod> gradingPeriods;
   
   private boolean openNext = true;
   
   /**
    * returns the id that the external provider uses for the student.
    * this can return the user eid, if it returns null, the eid will
    * be used in subsequent calls
    *
    * @param userId user id.  this can be used to look up the student
    *               in the user directory provider
    * @return the student id according to the provider
    */
   public String getStudentId(String userId) {
      try {
         User user = UserDirectoryService.getUserByEid(userId);
         return user.getId();
      } catch (UserNotDefinedException e) {
         logger.error("failed to find user", e);
      }
      return userId;
   }

   /**
    * get the list of grading periods for this course.
    * this could be "mid-term" and "final" could be quarters,
    * could be weekly, etc.
    *
    * @param courseId
    * @return List of grading periods to allow a grader to export grades for
    */
   public List<GradingPeriod> getGradingPeriods(String courseId) {
      try {
      List<GradingPeriod> localalGradingPeriods = 
         getHibernateTemplate().findByNamedQuery("gradingPeriodForCourse", 
            new Object[] {courseId});
      
      if (localalGradingPeriods.size() == 0) {
         localalGradingPeriods = new ArrayList<GradingPeriod>();
         
         for (GradingPeriod period : getGradingPeriods()) {
            RefGradingPeriod refPeriod = new RefGradingPeriod(period, courseId);
            localalGradingPeriods.add(refPeriod);
         }
         getHibernateTemplate().saveOrUpdateAll(localalGradingPeriods);
      }
      
      return localalGradingPeriods;
      }
      catch (Throwable e) {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * saves the changed grades to the external provider.  note that this is
    * just saving a draft and different from submit grades below
    *
    * @param courseId
    * @param period   grading period for these grades
    * @param grades   map of student id from above and the letter grade
    */
   public void savePeriodGrades(String courseId, String period, Map<String, String> grades) 
      throws ExternalProviderException {
      String graderId = UsageSessionService.getSession().getUserEid();
      
      RefGradingPeriod coursePeriod = getGradingPeriod(courseId, period);
      Set<String> leftovers = grades.keySet();
      
      List<ExternalGrade> localGrades = getGrades(coursePeriod);
      
      for (ExternalGrade grade : localGrades) {
         if (grades.containsKey(grade.getStudentId())) {
            grade.setGrade(grades.get(grade.getStudentId()));
            leftovers.remove(grade.getStudentId());
         }
      }
      
      for (String student : leftovers) {
         ExternalGrade grade = new ExternalGrade();
         grade.setGradingPeriod(coursePeriod);
         grade.setGrade(grades.get(student));
         grade.setStudentId(student);
         localGrades.add(grade);
      }
      
      for (ExternalGrade grade : localGrades) {
         grade.setModifiedOn(new Date());
         grade.setGraderId(graderId);
      }

      getHibernateTemplate().saveOrUpdateAll(localGrades);
      getHibernateTemplate().flush();
   }

   /**
    * submits the grades to the external system.  this method allows the external system to "lock"
    * the grading period, open the next grading period or whatever workflow is associated
    * with their grading periods
    *
    * @param courseId
    * @param period   grading period for these grades
    * @param grades   map of student id from above and the letter grade
    */
   public void submitPeriodGrades(String courseId, String period, Map<String, String> grades) 
      throws ExternalProviderException {
      submitPeriodGrades(courseId, period, grades, true);
   }

   protected void submitPeriodGrades(String courseId, String period, Map<String, String> grades, boolean save) 
      throws ExternalProviderException {
      
      Map<String, String> entryErrors = new HashMap<String, String>();
      for (Map.Entry<String, String> grade : grades.entrySet()) {
         if (grade.getValue() == null || grade.getValue().length() == 0) {
            entryErrors.put(grade.getKey(), "Required");
         }
      }
      
      if (!entryErrors.isEmpty()) {
         throw new ExternalProviderException(entryErrors);
      }
      
      if (save) {
         savePeriodGrades(courseId, period, grades);
      }
      
      RefGradingPeriod coursePeriod = getGradingPeriod(courseId, period);
      coursePeriod.setWritable(false);
      getHibernateTemplate().saveOrUpdate(coursePeriod);
      
      List<GradingPeriod> coursePeriods = getGradingPeriods(courseId);
      
      if (openNext) {
         for (Iterator<GradingPeriod> i=coursePeriods.iterator();i.hasNext();) {
            GradingPeriod currentP = i.next();
            
            if (currentP.getColumnKey().equals(coursePeriod.getColumnKey())) {
               if (i.hasNext()) {
                  currentP = i.next();
                  currentP.setWritable(true);
                  getHibernateTemplate().saveOrUpdate(currentP);
               }
               break;
            }
         }
      }
   }

   /**
    * gets the grades for the grading period.  this can come from the gradebook for this course,
    * or could come from the external provider or from some combination (ie, grades were saved or
    * submitted above.)
    *
    * @param courseId
    * @param period   grading period for these grades
    * @return map of student id from above and the letter grade
    */
   public Map<String, String> getPeriodGrades(String courseId, String period) {
      Map<String, String> grades = new HashMap<String, String>();
      RefGradingPeriod coursePeriod = getGradingPeriod(courseId, period);
      
      List<ExternalGrade> localGrades = getGrades(coursePeriod);
      
      for (ExternalGrade grade : localGrades) {
         grades.put(grade.getStudentId(), grade.getGrade());
      }
      
      return grades;
   }

   protected RefGradingPeriod getGradingPeriod(String courseId, String gradingPeriodKey) {
      List<RefGradingPeriod> period = getHibernateTemplate().findByNamedQuery("courseGradingPeriod",
         new Object[]{courseId, gradingPeriodKey});
      
      if (period.size() > 0) {
         return period.get(0);
      }
      
      return null;  
   }
   

   protected List<ExternalGrade> getGrades(RefGradingPeriod coursePeriod) {
      if (coursePeriod == null) {
         return new ArrayList();
      }
      return getHibernateTemplate().findByNamedQuery("gradesForPeriod", coursePeriod);
   }
   
   public List<GradingPeriod> getGradingPeriods() {
      return gradingPeriods;
   }

   public void setGradingPeriods(List<GradingPeriod> gradingPeriods) {
      this.gradingPeriods = gradingPeriods;
   }

   public boolean isOpenNext() {
      return openNext;
   }

   public void setOpenNext(boolean openNext) {
      this.openNext = openNext;
   }
}
