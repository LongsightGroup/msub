/**********************************************************************************
 * Copyright (c) 2006 the r-smart group, inc.
 **********************************************************************************/
package com.rsmart.virtual_classroom.model;

import com.rsmart.virtual_classroom.intf.VirtualClassroomService;
import java.util.Comparator;




/**
 * comparator which sorts virtual classroom session group objects according to the specified sort field and sort order.
 */
public class SortComparator implements Comparator {
   protected int sortField;
   protected int sortOrder;


   /**
    * constructor.
    * <br/><br/>
    * @param sortOrder  Specifies the whether the virtual classroom session groups are sorted in ascending or descending order.
    *                   It must have the value of either SORT_ORDER_ASCENDING or SORT_ORDER_ASCENDING.
    */
   public SortComparator(int sortField, int sortOrder) {
      if (sortField != VirtualClassroomService.SORT_FIELD_JOIN        &&
          sortField != VirtualClassroomService.SORT_FIELD_NAME        &&
          sortField != VirtualClassroomService.SORT_FIELD_START_DATE)
         throw new IllegalArgumentException("Invalid sort field specified: " + sortField);

      if (sortOrder != VirtualClassroomService.SORT_ORDER_ASCENDING && sortOrder != VirtualClassroomService.SORT_ORDER_DESCENDING)
         throw new IllegalArgumentException("Invalid sort order specified: " + sortOrder);

      this.sortField = sortField;
      this.sortOrder = sortOrder;
   }

   /**
    * Compares two virtual classroom session group objects for order based on the specified sort field and sort order.
    * <br/><br/>
    * @param object1  virtual classroom session group object
    * @param object2  virtual classroom session group object
    * <br/><br/>
    * @throws ClassCastException if either argument can not be cast to a VirtualClassroomSessionGroup.
    * <br/><br/>
    * @return  Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
    */
   public int compare(Object object1, Object object2) throws ClassCastException {
      if (object1 != null && !(object1 instanceof VirtualClassroomSessionGroup))
         throw new ClassCastException("object1 can not be cast to " + VirtualClassroomSessionGroup.class + ".");
      if (object2 != null && !(object2 instanceof VirtualClassroomSessionGroup))
         throw new ClassCastException("object2 can not be cast to " + VirtualClassroomSessionGroup.class + ".");

      int                          result   = 0;
      VirtualClassroomSessionGroup group1 = (VirtualClassroomSessionGroup)object1;
      VirtualClassroomSessionGroup group2 = (VirtualClassroomSessionGroup)object2;

           if (group1 == null && group2 == null) result =  0;
      else if (group1 == null && group2 != null) result =  1;
      else if (group1 != null && group2 == null) result = -1;
      else {
         switch (sortField) {
            case VirtualClassroomService.SORT_FIELD_JOIN:
                 result = compareJoin(group1, group2);
            break;
            case VirtualClassroomService.SORT_FIELD_NAME:
                 result = group1.getName().compareTo(group2.getName());
            break;
            case VirtualClassroomService.SORT_FIELD_START_DATE:
                 result = group1.getSchedule().getStartDate().compareTo(group2.getSchedule().getStartDate());
            break;
         }
      }
      return (sortOrder == VirtualClassroomService.SORT_ORDER_ASCENDING ? result : -result);
   }

   /**
    * compares two virtual classroom session group objects based on their join status.
    */
   private int compareJoin(VirtualClassroomSessionGroup group1, VirtualClassroomSessionGroup group2) {
      int result = 0;

           if ( group1.isJoinable() &&  group2.isJoinable()) result = group1.getSchedule().getStartDate().compareTo(group2.getSchedule().getStartDate());
      else if ( group1.isJoinable() && !group2.isJoinable()) result = -1;
      else if (!group1.isJoinable() &&  group2.isJoinable()) result =  1;
      else if ( group1.isRecorded() &&  group2.isRecorded()) result = group1.getSchedule().getStartDate().compareTo(group2.getSchedule().getStartDate());
      else if ( group1.isRecorded() && !group2.isRecorded()) result = -1;
      else if (!group1.isRecorded() &&  group2.isRecorded()) result =  1;
      else if ( group1.isStarted () &&  group2.isStarted ()) result = group1.getSchedule().getStartDate().compareTo(group2.getSchedule().getStartDate());
      else if ( group1.isStarted () && !group2.isStarted ()) result = -1;
      else if (!group1.isStarted () &&  group2.isStarted ()) result =  1;
      else                                                   result = group1.getSchedule().getStartDate().compareTo(group2.getSchedule().getStartDate());

      return result;
   }
}
