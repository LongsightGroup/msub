package org.sakaiproject.refgradeprovider.model;

import org.sakaiproject.service.gradebook.shared.GradingPeriod;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 4, 2008
 * Time: 10:30:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalGrade {
   
   private Long id;
   private String studentId;
   private String graderId;
   private Date modifiedOn;
   private GradingPeriod gradingPeriod;
   private String grade;

   public ExternalGrade() {
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getStudentId() {
      return studentId;
   }

   public void setStudentId(String studentId) {
      this.studentId = studentId;
   }

   public GradingPeriod getGradingPeriod() {
      return gradingPeriod;
   }

   public void setGradingPeriod(GradingPeriod gradingPeriod) {
      this.gradingPeriod = gradingPeriod;
   }

   public String getGrade() {
      return grade;
   }

   public void setGrade(String grade) {
      this.grade = grade;
   }

   public String getGraderId() {
      return graderId;
   }

   public void setGraderId(String graderId) {
      this.graderId = graderId;
   }

   public Date getModifiedOn() {
      return modifiedOn;
   }

   public void setModifiedOn(Date modifiedOn) {
      this.modifiedOn = modifiedOn;
   }
}
