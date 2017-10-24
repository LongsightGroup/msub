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
package com.rsmart.course_site_removal.intf;





/**
 * sakai service for removing\\unpublishing course sites whose terms have ended.
 * <p>
 * example: <br/>
 * <code>
 *     fall term, 2008: starts August 22nd, 2008
 *                      ends   December 15th, 2008
 *
 *     let's say that a site is created for bio 201 for the fall term 2008.
 *     assume that we want the course site to be removed, since it will no longer be needed, two weeks after the term ends.
 *     thus, we want the sakai site for bio 201 to be removed on December 15th + 14 days = December 29th, 2008.
 *
 *     if this service is invoked on any day after December 29th, 2008, the course will be removed or unpublished, depending
 *     on how the service is configured in sakai.properties.
 * </code>
 * </p>
 */
public interface CourseSiteRemovalService {
   // permissions
   public final static String PERMISSION_COURSE_SITE_REMOVAL = "rsmart.course_site.removal";

    // site property
   public final static String SITE_PROPRTY_COURSE_SITE_REMOVAL = "rsmart.course_site.removal.set";

   // enum
   public enum Action {remove, unpublish}    // whether to remove the expired course site altogether or to simply unpublish it.




   /**
    * removes\\unpublishes course sites whose terms have ended and a specified number of days have passed.
    * Once a term has ended, the course sites for that term remain available for a specified number of days, whose duration is specified in sakai.properties
    * via the <i>rsmart.course_site_removal.num_days_after_term_ends</i> property.  After the specified period has elapsed, this invoking this service will either
    * remove or unpublish the course site, depending on the value of the <i>rsmart.course_site_removal.action</i> sakai property.
    * </br></br>
    * @param action                 whether to delete the course site or to simply unpublish it.
    * @param numDaysAfterTermEnds   number of days after a term ends when course sites expire.
    * </br></br>
    * @return the number of course sites that were removed\\unpublished.
    */
   public int removeCourseSites(Action action, int numDaysAfterTermEnds);
}
