/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.tool.spring.form_bean;

import com.rsmart.virtual_classroom.model.VirtualClassroomSessionGroup;




/**
 * Spring mvc framework binds the values from an html form on a jsp page into the data members of this bean class.
 */
public class VirtualClassroomSessionGroupForm extends VirtualClassroomSessionGroup {
   // html form input fields
   private String   instructorName;            // name of instructor
   private boolean  selected;                  // from the list_virtual_classroom_sessions.jsp page
   private boolean  updating;                  // whether this session is being created or updated.




   /**
    * default constructor.
    */
   public VirtualClassroomSessionGroupForm() {
      this(new VirtualClassroomSessionGroup());
   }

   /**
    * constructor.
    * sets the form bean fields from the model's values.
    */
   public VirtualClassroomSessionGroupForm(VirtualClassroomSessionGroup virtualClassroomSessionGroup) {
      super(virtualClassroomSessionGroup);
      setSchedule(new ScheduleForm(getSchedule()));
      setUpdating(getId() != null);
   }

   /**
    * returns the model.
    */
   public VirtualClassroomSessionGroup getModel() {
      return new VirtualClassroomSessionGroup(this);
   }

   public String getInstructorName() {
      return instructorName;
   }

   public void setInstructorName(String instructorName) {
      this.instructorName = instructorName;
   }

    public boolean isUpdating() {
      return updating;
   }

   public void setUpdating(boolean updating) {
      this.updating = updating;
   }

   /**
    * returns whether the virtual classroom session is selected by the checkbox.
    */
   public boolean isSelected() {
      return selected;
   }

   /**
    * sets whether the virtual classroom session is selected by the checkbox.
    */
   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   /**
    * returns a string representation of this instance.
    */
   public String toString() {
      StringBuffer buffer = new StringBuffer();

      buffer.append(super.toString());
      buffer.append("instructor.............: " + instructorName + "\n");
      buffer.append("selected...............: " + selected       + "\n");
      buffer.append("updating...............: " + updating       + "\n");

      return buffer.toString();
   }
}
