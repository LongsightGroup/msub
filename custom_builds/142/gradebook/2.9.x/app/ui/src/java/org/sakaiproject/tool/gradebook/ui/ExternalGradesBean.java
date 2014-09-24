package org.sakaiproject.tool.gradebook.ui;

import org.sakaiproject.service.gradebook.shared.ExternalGradeProvider;
import org.sakaiproject.service.gradebook.shared.GradingPeriod;
import org.sakaiproject.service.gradebook.shared.ExternalProviderException;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseListener;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.FactoryFinder;
import javax.faces.component.UIData;

import java.util.*;
import java.io.Serializable;


/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 4, 2008
 * Time: 3:44:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalGradesBean extends GradebookDependentBean implements Serializable, PhaseListener {
   
	private static final Log logger = LogFactory.getLog(ExternalGradesBean.class);

   private List<GradingPeriod> gradingPeriods;
   private Map<String, Map<String, String>> gradingPeriodGrades;
   private CourseGradeDetailsBean courseGradeDetailsBean;
   private Map<String, String> userIds;
   private transient UIData tableData;

   public ExternalGradesBean() {
      LifecycleFactory factory = (LifecycleFactory)
         FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
      
      Lifecycle lc = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
      lc.addPhaseListener(this);
   }

   public boolean isExternalGrades() {
      return getExternalGradeProvider() != null;
   }

   public ExternalGradeProvider getExternalGradeProvider() {
      return (ExternalGradeProvider)getGradebookBean().getConfigurationBean()
         .getPlugin(ExternalGradeProvider.EXTERNAL_GRADE_PLUGIN_KEY);
   }

   public List<GradingPeriod> getGradingPeriods() {
      if (gradingPeriods == null && getExternalGradeProvider() != null) {
         List<GradingPeriod> temp = getExternalGradeProvider().getGradingPeriods(getGradebookUid());
         gradingPeriods = new ArrayList<GradingPeriod>();
         for (GradingPeriod period : temp) {
            gradingPeriods.add(new GradingPeriodBean(period, this));
         }
      }
      return gradingPeriods;
   }
   
   public int getGradingPeriodsSize() {
      return getGradingPeriods().size();
   }

   public Map<String, Map<String, String>> getPeriodGrades() {
      if (gradingPeriodGrades == null) {
         gradingPeriodGrades = new Hashtable();
         for (GradingPeriod period : getGradingPeriods()) {
            Map<String, String> periodGrades = getExternalGradeProvider().getPeriodGrades(
               getGradebookUid(), period.getColumnKey());
            getCourseGradeDetailsBean().addSortingColumn(period.getColumnKey(), 
               new GradingPeriodComparator(periodGrades));
            gradingPeriodGrades.put(period.getColumnKey(),
               periodGrades);
         }
      }      
      return gradingPeriodGrades;
   }

   protected void processMessages(ExternalProviderException exp) {
      if (exp.getLocalizedMessage() != null) {
         FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, exp.getLocalizedMessage(), null));
      }

      Object tableState = getTableData().saveState(FacesContext.getCurrentInstance());
      getCourseGradeDetailsBean().startRenderResponse(); // should reload the score rows
      List<CourseGradeDetailsBean.ScoreRow> scoreRows = getCourseGradeDetailsBean().getScoreRows();
      getTableData().setValue(scoreRows);
      getTableData().restoreState(FacesContext.getCurrentInstance(), tableState);
      int index = 0;
      if (exp.getGradeErrors() != null) {
         for (CourseGradeDetailsBean.ScoreRow row : scoreRows) {
            User user = row.getEnrollment().getUser();
            String error = exp.getGradeErrors().get(user.getUserUid());
            if (error != null) {
               String clientId = "gbForm:gradingTable:" + index + ":Grade";
               FacesContext.getCurrentInstance().addMessage(clientId, 
                  new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            }
            index++;
         }
      }
   }
   
   public void saveGrades() {
      saveGradesInternal();
   }
   
   public boolean saveGradesInternal() {
      Object tableState = getTableData().saveState(FacesContext.getCurrentInstance());
      try {
         for (Map.Entry<String, Map<String, String>> period : getPeriodGrades().entrySet()) {
            getExternalGradeProvider().savePeriodGrades(getGradebookUid(), 
               period.getKey(), new HashMap<String, String>(period.getValue()));
         }
         //gradingPeriodGrades = null; // cause it to reload
         getTableData().restoreState(FacesContext.getCurrentInstance(), tableState);
         return true;
      }
      catch (ExternalProviderException exp) {
         processMessages(exp);
         return false;
      }
   }
   
   public String submit(String columnKey) {
      if (!saveGradesInternal()) {
         return null;
      }
      
      Map<String, String> periodGrades = getPeriodGrades().get(columnKey);
      
      try {
         getExternalGradeProvider().submitPeriodGrades(getGradebookUid(), 
            columnKey, periodGrades);
         gradingPeriodGrades = null; // cause it to reload
         gradingPeriods = null;
         FacesUtil.addMessage(getLocalizedString("external_grades_saved_submitted"));
      }
      catch (ExternalProviderException exp) {
         logger.warn("", exp);
         gradingPeriodGrades = null; // cause it to reload
         gradingPeriods = null;
         processMessages(exp);
      }
      return null;
   }
   
   public class GradingPeriodBean extends GradingPeriod {
      
      private ExternalGradesBean parent;

      public GradingPeriodBean(GradingPeriod copy, ExternalGradesBean parent) {
         this.parent = parent;
         setColumnKey(copy.getColumnKey());
         setColumnName(copy.getColumnName());
         setSubmitButton(copy.getSubmitButton());
         setWritable(copy.isWritable());
      }
      
      public String submit() {
         return parent.submit(getColumnKey());
      }
      
      public String getSubmitLabel() {
         if (getSubmitButton()!= null) {
            return getSubmitButton();
         }
         return FacesUtil.getLocalizedString("external_submit_label", 
            new String[]{getColumnName()});
      }

      public String getConfirmMessage() {
         return FacesUtil.getLocalizedString("external_grades_confirm_message", 
            new String[]{getColumnName()});
      }
   }
   
   public class GradingPeriodComparator implements Comparator<EnrollmentRecord> {
      Map<String, String> grades;

      public GradingPeriodComparator(Map<String, String> grades) {
         this.grades = grades;
      }

      public int compare(EnrollmentRecord enrollmentRecord1, EnrollmentRecord enrollmentRecord2) {
         String userId1 = enrollmentRecord1.getUser().getUserUid();
         String userId2 = enrollmentRecord2.getUser().getUserUid();
         
         String grade1 = grades.get(userId1);
         String grade2 = grades.get(userId2);
         
         grade1 = (grade1==null)?"":grade1;
         grade2 = (grade2==null)?"":grade2;
         
         return grade1.compareToIgnoreCase(grade2); 
      }
   }

   public class ExternalIdComparator implements Comparator<EnrollmentRecord> {
      
      private ExternalGradesBean parent;

      public ExternalIdComparator(ExternalGradesBean parent) {
         this.parent = parent;
      }

      public int compare(EnrollmentRecord enrollmentRecord1, EnrollmentRecord enrollmentRecord2) {
         String uid1 = parent.getUserIds().get(enrollmentRecord1.getUser().getUserUid());
         String uid2 = parent.getUserIds().get(enrollmentRecord2.getUser().getUserUid());
         
         return uid1.compareTo(uid2);
      }
   }
  
   
   public class UserIdMap extends Hashtable<String, String> {
      private ExternalGradesBean parent;

      public UserIdMap(ExternalGradesBean parent) {
         this.parent = parent;
      }

      public String get(Object o) {
         if (super.get(o) != null) {
            return super.get(o);
         }
         else {
            String returned = parent.getExternalGradeProvider().getStudentId((String)o);
            super.put((String)o, returned);
            return returned;
         }
      }
   }
   
   public CourseGradeDetailsBean getCourseGradeDetailsBean() {
      if (courseGradeDetailsBean == null) {
         courseGradeDetailsBean = (CourseGradeDetailsBean)FacesUtil.resolveVariable("courseGradeDetailsBean");
      }
      return courseGradeDetailsBean;
   }

   public Map<String, String> getUserIds() {
      if (userIds == null) {
         userIds = new UserIdMap(this);
         getCourseGradeDetailsBean().addSortingColumn("externalUserId", new ExternalIdComparator(this));
      }
      return userIds;
   }

   public void afterPhase(PhaseEvent event) {
   }

   public void beforePhase(PhaseEvent event) {
      // convert any grade row errors here
      for (Iterator<String> i = FacesContext.getCurrentInstance().getClientIdsWithMessages(); i.hasNext();) {
         String clientId = i.next();
         
         if (clientId != null) {
            int index = clientId.indexOf("externalGrades");
            if (index != -1) {
               String newClientId = clientId.substring(0, index) + "Grade";
               for (Iterator messages = FacesContext.getCurrentInstance().getMessages(clientId);messages.hasNext();) {
                  FacesContext.getCurrentInstance().addMessage(newClientId, (FacesMessage) messages.next());
               }
            }
         }
      }
   }

   public PhaseId getPhaseId() {
      return PhaseId.RENDER_RESPONSE;
   }

   public UIData getTableData() {
      return tableData;
   }

   public void setTableData(UIData tableData) {
      this.tableData = tableData;
   }
}                                             
