/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/OpenForum.java $
 * $Id: OpenForum.java 23942 2010-12-09 16:48:03Z jbush $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.Date;


public interface OpenForum extends BaseForum {

    public Boolean getLocked();

    public void setLocked(Boolean locked);

    public Boolean getDraft();
    
    public void setDraft(Boolean draft);
    
    public String getDefaultAssignName();

    public void setDefaultAssignName(String defaultAssignName);
    
    public Boolean getAvailabilityRestricted();
    
    public void setAvailabilityRestricted(Boolean restricted);
      
    public Date getOpenDate();

	public void setOpenDate(Date openDate);
	
    public Date getCloseDate();
    
	public void setCloseDate(Date closeDate);
	
	public Boolean getAvailability();
    
    public void setAvailability(Boolean restricted);

}