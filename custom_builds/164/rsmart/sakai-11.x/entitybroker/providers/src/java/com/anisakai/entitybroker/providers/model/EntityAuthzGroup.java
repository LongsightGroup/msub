package com.anisakai.entitybroker.providers.model;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.Set;
import java.util.Stack;

/**
 * Created with IntelliJ IDEA.
 * User: kevin
 * Date: 9/9/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntityAuthzGroup implements AuthzGroup {

    private transient AuthzGroup authzGroup;

    @EntityId
    private String id;
    private String url;
    private String reference;
    private ResourceProperties resourceProperties;
    private String maintainRole;
    private Set<Member> members;
    private String providerGroupId;
    private User createdBy;
    private Date createdDate;
    private Time createdTime;
    private User modifiedBy;
    private Date modifiedDate;
    private Time modifiedTime;
    private String description;
    private Set<Role> roles;
    private Set<String> users;
    private boolean active;

    public EntityAuthzGroup(AuthzGroup authzGroup){
        this.id = authzGroup.getId();
        this.url = authzGroup.getUrl();
        this.reference = authzGroup.getReference();
        this.resourceProperties = authzGroup.getProperties();
        this.maintainRole = authzGroup.getMaintainRole();
        this.members = authzGroup.getMembers();
        this.providerGroupId = authzGroup.getProviderGroupId();
        this.createdBy = authzGroup.getCreatedBy();
        this.createdDate = authzGroup.getCreatedDate();
        this.createdTime = authzGroup.getCreatedTime();
        this.modifiedBy = authzGroup.getModifiedBy();
        this.modifiedDate = authzGroup.getModifiedDate();
        this.modifiedTime = authzGroup.getModifiedTime();
        this.description = authzGroup.getDescription();
        this.roles = authzGroup.getRoles();
        this.users = authzGroup.getUsers();
        this.active = authzGroup.isActiveEdit();
    }

    public EntityAuthzGroup(){
    }

    @EntityId
    public String getId() {
        return id;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getReference() {
        return reference;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getUrl(String rootProperty) {
        if(authzGroup != null){
            return authzGroup.getUrl(rootProperty);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String getReference(String rootProperty) {
        if(authzGroup != null){
            return authzGroup.getReference(rootProperty);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceProperties getProperties() {
        return resourceProperties;
    }

    @Override
    public Element toXml(Document doc, Stack<Element> stack) {
        if(authzGroup != null){
            return authzGroup.toXml(doc, stack);
        }
        throw new UnsupportedOperationException();
    }

    public String getMaintainRole() {
        return maintainRole;
    }

    @Override
    public Member getMember(String userId) {
        if(authzGroup != null){
            return authzGroup.getMember(userId);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Member> getMembers() {
        return members;
    }

    @Override
    public String getProviderGroupId() {
        return providerGroupId;
    }

    @Override
    public void setProviderGroupId(String id) {
        this.providerGroupId = id;
    }

    @Override
    public boolean keepIntersection(AuthzGroup other) {
        if(authzGroup != null){
            return authzGroup.keepIntersection(other);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMember(String userId, String roleId, boolean active, boolean provided) {
        if(authzGroup != null){
            authzGroup.addMember(userId, roleId, active, provided);
        }
    }

    @Override
    public Role addRole(String id) throws RoleAlreadyDefinedException {
        if(authzGroup != null){
            return authzGroup.addRole(id);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Role addRole(String id, Role other) throws RoleAlreadyDefinedException {
        if(authzGroup != null){
            return authzGroup.addRole(id, other);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public User getCreatedBy() {
        if (authzGroup != null){
            return createdBy;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getCreatedTime() {
        return createdTime;
    }

    public User getModifiedBy() {
        if (authzGroup != null){
            return modifiedBy;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getModifiedTime() {
        return modifiedTime;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public Role getRole(String id) {
        if (authzGroup != null){
            authzGroup.getRole(id);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public Set<String> getRolesIsAllowed(String function) {
        if (authzGroup != null){
            authzGroup.getRolesIsAllowed(function);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Role getUserRole(String userId) {
        if (authzGroup != null){
            authzGroup.getUserRole(userId);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getUsers() {
        return users;
    }

    @Override
    public Set<String> getUsersHasRole(String role) {
        if (authzGroup != null){
            authzGroup.getUsersHasRole(role);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getUsersIsAllowed(String function) {
        if (authzGroup != null){
            authzGroup.getUsersIsAllowed(function);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasRole(String userId, String role) {
        if (authzGroup != null){
            authzGroup.hasRole(userId, role);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAllowed(String userId, String function) {
        if (authzGroup != null){
            authzGroup.isAllowed(userId, function);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        if (authzGroup != null){
            authzGroup.isEmpty();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMember(String userId) {
        if (authzGroup != null){
            authzGroup.removeMember(userId);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMembers() {
        if (authzGroup != null){
            authzGroup.removeMembers();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRole(String role) {
        if (authzGroup != null){
            authzGroup.removeRole(role);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRoles() {
        if (authzGroup != null){
            authzGroup.removeRoles();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaintainRole(String role) {
        this.maintainRole = role;
    }

    @Override
    public int compareTo(Object o) {
        if (authzGroup != null){
            return authzGroup.compareTo(o);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActiveEdit() {
        return active;
    }

    @Override
    public ResourcePropertiesEdit getPropertiesEdit() {
        return (ResourcePropertiesEdit) resourceProperties;
    }
}
