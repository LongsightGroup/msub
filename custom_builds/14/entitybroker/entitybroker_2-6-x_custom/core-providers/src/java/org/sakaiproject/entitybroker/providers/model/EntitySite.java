/**
 * $Id: EntitySite.java 51318 2008-08-24 05:28:47Z csev@umich.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/EntitySite.java $
 * EntitySite.java - entity-broker - Jun 29, 2008 9:31:10 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.azeckoski.reflectutils.annotations.ReflectTransient;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityLastModified;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntitySummary;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is needed to allow RESTful access to the site data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
@ReflectIgnoreClassFields({"createdBy","modifiedBy","properties","propertiesEdit","pages","members","orderedPages","roles","users","groups","url"})
public class EntitySite implements Site {

    private static final long serialVersionUID = 7526472295622776147L;

    @EntityId
    private String id;
    private String title;
    private String shortDescription;
    private String description;
    private String iconUrl;
    private String iconFullUrl;
    private String infoUrl;
    private String infoUrlFull;
    private boolean joinable;
    private String joinerRole;
    private String maintainRole;
    private String skin;
    private boolean published;
    private boolean pubView;
    private String type;
    private String providerGroupId;
    private boolean customPageOrdered;
    private String owner;
    private long lastModified;
    private String[] userRoles;

    public Map<String, String> props;

    public Map<String, String> getProps() {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public void setProperty(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
    }

    public String getProperty(String key) {
        if (props == null) {
            return null;
        }
        return props.get(key);
    }

    private transient Site site;

    public EntitySite() {
    }

    public EntitySite(String title, String shortDescription, String description, String iconUrl,
            String iconFullUrl, String infoUrl, String infoUrlFull, boolean joinable,
            String joinerRole, String maintainRole, String skin, boolean published, boolean pubView,
            String type, String providerGroupId, boolean customPageOrdered) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.iconUrl = iconUrl;
        this.iconFullUrl = iconFullUrl;
        this.infoUrl = infoUrl;
        this.infoUrlFull = infoUrlFull;
        this.joinable = joinable;
        this.joinerRole = joinerRole;
        this.maintainRole = maintainRole;
        this.skin = skin;
        this.published = published;
        this.pubView = pubView;
        this.type = type;
        this.providerGroupId = providerGroupId;
        this.customPageOrdered = customPageOrdered;
        this.lastModified = System.currentTimeMillis();
        getUserRoles(); // populate the user roles
    }

    public EntitySite(Site site) {
        this.site = site;
        this.id = site.getId();
        this.title = site.getTitle();
        this.shortDescription = site.getShortDescription();
        this.description = site.getDescription();
        this.iconUrl = site.getIconUrl();
        this.infoUrl = site.getInfoUrl();
        this.joinable = site.isJoinable();
        this.joinerRole = site.getJoinerRole();
        this.skin = site.getSkin();
        this.published = site.isPublished();
        this.pubView = site.isPubView();
        this.type = site.getType();
        this.customPageOrdered = site.isCustomPageOrdered();
        this.maintainRole = site.getMaintainRole();
        this.providerGroupId = site.getProviderGroupId();
        this.owner = site.getCreatedBy() == null ? null : site.getCreatedBy().getId();
        this.lastModified = site.getModifiedTime() == null ? System.currentTimeMillis() : site.getModifiedTime().getTime();
        getUserRoles(); // populate the user roles
        // properties
        ResourceProperties rp = site.getProperties();
        for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
            String name = iterator.next();
            String value = rp.getProperty(name);
            this.setProperty(name, value);
        }
    }


    @EntityId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the id of the owner of this site (will match the created by user id)
     */
    @EntityOwner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Special method
     * @return the owner data for the current site owner
     */
    public Owner getSiteOwner() {
        Owner owner = null;
        if (this.site != null) {
            // TODO handle the contact info?
            User user = site.getCreatedBy();
            owner = new Owner(user.getId(), user.getDisplayName());
        } else {
            owner = new Owner(this.owner, this.owner);
        }
        return owner;
    }

    @EntityLastModified
    public long getLastModified() {
        if (site != null) {
            this.lastModified = site.getModifiedTime() == null ? lastModified : site.getModifiedTime().getTime();
        }
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        throw new UnsupportedOperationException("Cannot set the last modified time manually");
    }

    @EntityTitle
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @EntitySummary
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

    public String getInfoUrlFull() {
        if (site != null) {
            return site.getInfoUrlFull();
        }
        return infoUrlFull;
    }

    public void setInfoUrlFull(String infoUrlFull) {
        this.infoUrlFull = infoUrlFull;
    }

    public boolean isJoinable() {
        return joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    public String getJoinerRole() {
        return joinerRole;
    }

    public void setJoinerRole(String joinerRole) {
        this.joinerRole = joinerRole;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIconFullUrl() {
        if (site != null) {
            return site.getIconUrlFull();
        }
        return iconFullUrl;
    }

    public void setIconFullUrl(String iconFullUrl) {
        this.iconFullUrl = iconFullUrl;
    }

    public String getMaintainRole() {
        return maintainRole;
    }

    public void setMaintainRole(String maintainRole) {
        this.maintainRole = maintainRole;
    }

    public String getProviderGroupId() {
        return providerGroupId;
    }

    public void setProviderGroupId(String providerGroupId) {
        this.providerGroupId = providerGroupId;
    }

    public boolean isCustomPageOrdered() {
        return customPageOrdered;
    }

    public void setCustomPageOrdered(boolean customPageOrdered) {
        this.customPageOrdered = customPageOrdered;
    }
    public boolean isPubView() {
        return pubView;
    }

    public void setPubView(boolean pubView) {
        this.pubView = pubView;
    }

    public String[] getUserRoles() {
        if (userRoles == null) {
            if (site == null) {
                userRoles = new String[] {maintainRole, joinerRole};
            } else {
                Set<Role> roles = (Set<Role>) site.getRoles();
                userRoles = new String[roles.size()];
                int i = 0;
                for (Role role : roles) {
                    userRoles[i] = role.getId();
                    i++;
                }
            }
        }
        return userRoles;
    }
    
    public void setUserRoles(String[] userRoles) {
        this.userRoles = userRoles;
    }
    
    
    // Site operations

    public Group addGroup() {
        if (site != null) {
            return site.addGroup();
        }
        throw new UnsupportedOperationException();
    }

    public SitePage addPage() {
        if (site != null) {
            return site.addPage();
        }
        throw new UnsupportedOperationException();
    }

    public User getCreatedBy() {
        if (site != null) {
            return site.getCreatedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getCreatedTime() {
        if (site != null) {
            return site.getCreatedTime();
        }
        throw new UnsupportedOperationException();
    }

    public Group getGroup(String arg0) {
        if (site != null) {
            return site.getGroup(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Collection getGroups() {
        if (site != null) {
            return site.getGroups();
        }
        throw new UnsupportedOperationException();
    }

    public Collection getGroupsWithMember(String arg0) {
        if (site != null) {
            return site.getGroupsWithMember(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Collection getGroupsWithMemberHasRole(String arg0, String arg1) {
        if (site != null) {
            return site.getGroupsWithMemberHasRole(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public String getIconUrlFull() {
        if (site != null) {
            return site.getIconUrlFull();
        }
        throw new UnsupportedOperationException();
    }

    public User getModifiedBy() {
        if (site != null) {
            return site.getModifiedBy();
        }
        throw new UnsupportedOperationException();
    }

    public Time getModifiedTime() {
        if (site != null) {
            return site.getModifiedTime();
        }
        throw new UnsupportedOperationException();
    }

    public List getOrderedPages() {
        if (site != null) {
            return site.getOrderedPages();
        }
        throw new UnsupportedOperationException();
    }

    public SitePage getPage(String arg0) {
        if (site != null) {
            return site.getPage(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public List getPages() {
        if (site != null) {
            return site.getPages();
        }
        throw new UnsupportedOperationException();
    }

    public ToolConfiguration getTool(String arg0) {
        if (site != null) {
            return site.getTool(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public ToolConfiguration getToolForCommonId(String arg0) {
        if (site != null) {
            return site.getToolForCommonId(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Collection getTools(String[] arg0) {
        if (site != null) {
            return site.getTools(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Collection getTools(String arg0) {
        if (site != null) {
            return site.getTools(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public boolean hasGroups() {
        if (site != null) {
            return site.hasGroups();
        }
        throw new UnsupportedOperationException();
    }

    public boolean isType(Object arg0) {
        if (site != null) {
            return site.isType(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void loadAll() {
        if (site != null) {
            site.loadAll();
        }
        throw new UnsupportedOperationException();
    }

    public void regenerateIds() {
        if (site != null) {
            site.regenerateIds();
        }
        throw new UnsupportedOperationException();
    }

    public void removeGroup(Group arg0) {
        if (site != null) {
            site.removeGroup(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void removePage(SitePage arg0) {
        if (site != null) {
            site.removePage(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public ResourcePropertiesEdit getPropertiesEdit() {
        if (site != null) {
            return site.getPropertiesEdit();
        }
        throw new UnsupportedOperationException();
    }

    public boolean isActiveEdit() {
        if (site != null) {
            return site.isActiveEdit();
        }
        throw new UnsupportedOperationException();
    }

    public ResourceProperties getProperties() {
        if (site != null) {
            return site.getProperties();
        }
        throw new UnsupportedOperationException();
    }

    public String getReference() {
        return "/site/" + id;
    }

    public String getReference(String arg0) {
        return this.getReference();
    }

    public String getUrl() {
        if (site != null) {
            return site.getUrl();
        }
        throw new UnsupportedOperationException();
    }

    public String getUrl(String arg0) {
        if (site != null) {
            return site.getUrl(arg0);
        }
        throw new UnsupportedOperationException();
    }

    @ReflectTransient
    public Element toXml(Document arg0, Stack arg1) {
        if (site != null) {
            return site.toXml(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public int compareTo(Object o) {
        if (site != null) {
            return site.compareTo(o);
        }
        throw new UnsupportedOperationException();
    }

    public void addMember(String arg0, String arg1, boolean arg2, boolean arg3) {
        if (site != null) {
            site.addMember(arg0, arg1, arg2, arg3);
        }
        throw new UnsupportedOperationException();
    }

    public Role addRole(String arg0) throws RoleAlreadyDefinedException {
        if (site != null) {
            return site.addRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Role addRole(String arg0, Role arg1) throws RoleAlreadyDefinedException {
        if (site != null) {
            return site.addRole(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public Member getMember(String arg0) {
        if (site != null) {
            return site.getMember(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getMembers() {
        if (site != null) {
            return site.getMembers();
        }
        throw new UnsupportedOperationException();
    }

    public Role getRole(String arg0) {
        if (site != null) {
            return site.getRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getRoles() {
        if (site != null) {
            return site.getRoles();
        }
        throw new UnsupportedOperationException();
    }

    public Set getRolesIsAllowed(String arg0) {
        if (site != null) {
            return site.getRolesIsAllowed(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Role getUserRole(String arg0) {
        if (site != null) {
            return site.getUserRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsers() {
        if (site != null) {
            return site.getUsers();
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsersHasRole(String arg0) {
        if (site != null) {
            return site.getUsersHasRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public Set getUsersIsAllowed(String arg0) {
        if (site != null) {
            return site.getUsersIsAllowed(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public boolean hasRole(String arg0, String arg1) {
        if (site != null) {
            return site.hasRole(arg0, arg1);
        }
        throw new UnsupportedOperationException();
    }

    public boolean isAllowed(String arg0, String arg1) {
        if (site != null) {
            return site.isAllowed(arg0, arg1);
        }
        return false;
    }

    public boolean isEmpty() {
        if (site != null) {
            return site.isEmpty();
        }
        return false;
    }

    public boolean keepIntersection(AuthzGroup arg0) {
        if (site != null) {
            return site.keepIntersection(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void removeMember(String arg0) {
        if (site != null) {
            site.removeMember(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void removeMembers() {
        if (site != null) {
            site.removeMembers();
        }
        throw new UnsupportedOperationException();
    }

    public void removeRole(String arg0) {
        if (site != null) {
            site.removeRole(arg0);
        }
        throw new UnsupportedOperationException();
    }

    public void removeRoles() {
        if (site != null) {
            site.removeRoles();
        }
        throw new UnsupportedOperationException();
    }

}
