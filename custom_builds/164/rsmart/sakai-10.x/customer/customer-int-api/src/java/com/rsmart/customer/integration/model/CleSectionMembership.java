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
 *
 * Contributor(s): jbush
 */

package com.rsmart.customer.integration.model;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 28, 2008
 * Time: 10:17:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleSectionMembership {
    private String userId;
    private String role;
    private String groupReference;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGroupReference() {
        return groupReference;
    }

    public void setGroupReference(String groupReference) {
        this.groupReference = groupReference;
    }


    public int hashCode() {
        return (userId + groupReference + role).hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof CleSectionMembership)) {
            return false;
        }

        CleSectionMembership that = (CleSectionMembership) o;
        if (that.groupReference != null && that.groupReference != null &&
            that.userId != null && that.userId != null &&
            that.role != null && that.role != null &&
            that.groupReference.equals(this.groupReference) &&
            that.userId.equals(this.userId) &&
            that.role.equals(this.role)) {
            return true;
        }
       return false;
    }

    public String toString() {
        return "CleSectionMembership{" +
                "userId='" + userId + '\'' +
                ", role='" + role + '\'' +
                ", groupReference='" + groupReference + '\'' +
                '}';
    }
}
