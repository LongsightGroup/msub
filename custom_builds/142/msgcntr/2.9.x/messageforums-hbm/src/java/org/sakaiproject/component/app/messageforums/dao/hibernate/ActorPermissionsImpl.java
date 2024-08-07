/**********************************************************************************
 * $URL: https://svn.rsmart.com/svn/vendor/branches/sakai/rsmart-cle/msgcntr/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/ActorPermissionsImpl.java $
 * $Id: ActorPermissionsImpl.java 13692 2009-04-24 16:14:32Z jbush $
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

public class ActorPermissionsImpl implements ActorPermissions {

    private static final Log LOG = LogFactory.getLog(ActorPermissionsImpl.class);
    
    private List contributors = new UniqueArrayList();
    private List accessors = new UniqueArrayList();
    private List moderators = new UniqueArrayList();

    private Long id;
    private Integer version; 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
                
    public List getAccessors() {
        return accessors;
    }

    public void setAccessors(List accessors) {
        this.accessors = accessors;
    }

    public List getContributors() {
        return contributors;
    }

    public void setContributors(List contributors) {
        this.contributors = contributors;
    }

    public List getModerators() {
        return moderators;
    }

    public void setModerators(List moderators) {
        this.moderators = moderators;
    }

    
    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addContributor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addContributor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApContributors(this);
        contributors.add(user);
    }

    public void removeContributor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeContributor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApContributors(null);
        contributors.remove(user);
    }
    
    public void addAccesssor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAccesssor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApAccessors(this);
        accessors.add(user);
    }

    public void removeAccessor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAccessor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApAccessors(null);
        accessors.remove(user);
    }    
    
    public void addModerator(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addModerator(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApModerators(this);
        moderators.add(user);
    }

    public void removeModerator(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeModerator(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApModerators(null);
        moderators.remove(user);
    }
    
}
