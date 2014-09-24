package org.sakaiproject.refgradeprovider.model;

import org.sakaiproject.service.gradebook.shared.GradingPeriod;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Aug 4, 2008
 * Time: 10:50:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefGradingPeriod extends GradingPeriod {
   
   private Long id;
   private String courseId;

   public RefGradingPeriod() {
   }

   public RefGradingPeriod(GradingPeriod period, String courseId) {
      this.courseId = courseId;
      this.setColumnKey(period.getColumnKey());
      this.setColumnName(period.getColumnName());
      this.setWritable(period.isWritable());
      this.setSubmitButton(period.getSubmitButton());
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getCourseId() {
      return courseId;
   }

   public void setCourseId(String courseId) {
      this.courseId = courseId;
   }
}
