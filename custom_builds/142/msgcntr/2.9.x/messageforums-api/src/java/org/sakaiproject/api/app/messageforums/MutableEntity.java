/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/MutableEntity.java $
 * $Id: MutableEntity.java 13692 2009-04-24 16:14:32Z jbush $
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

public interface MutableEntity {

    public Date getCreated();

    public void setCreated(Date created);

    public String getCreatedBy();

    public void setCreatedBy(String createdBy);

    public Long getId();

    public void setId(Long id);

    public String getModifiedBy();

    public void setModifiedBy(String modifiedBy);

    public Date getModified();

    public void setModified(Date modified);

    public String getUuid();

    public void setUuid(String uuid);

    public Integer getVersion();

    public void setVersion(Integer version);

}