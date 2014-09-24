/*
 * Copyright 2008 The rSmart Group
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.rsmart.course_site_publish.intf;





/**
 * sakai service for publishing course sites a set number of days before a term begins.
 * <p>
 * example: <br/>
 * <code>
 *     fall term, 2008: starts August 20th, 2008
 *
 *     let's say that some course sites, including bio 201, are created for the fall term 2008.
 *     assume that we want the course sites to be published two weeks before the fall term starts.
 *     thus, we want the sakai site for bio 201 to be published on August 20th - 14 days = August 6th, 2008.
 * </code>
 * </p>
 */
public interface CourseSitePublishService {
   // permissions
   public final static String PERMISSION_COURSE_SITE_PUBLISH     = "rsmart.course_site.publish";

   // site property
   public final static String SITE_PROPRTY_COURSE_SITE_PUBLISHED = "rsmart.course_site.publish.set";





   /**
    * publishes course sites whose terms are about to begin.
    * Before a term begins, existing, unpublished course sites are published so that they are then available to the students enrolled in the courses.
    * The courses will be published a number of days before the start of the term, whose value is specified by the <i>rsmart.course_site_publish.num_days_before_term</i> sakai property.
    * </br></br>
    * @param numDaysBeforeTermStarts   number of days before a term starts that course sites should be published.
    * </br></br>
    * @return the number of course sites that were published.
    */
   public int publishCourseSites(int numDaysBeforeTermStarts);
}
