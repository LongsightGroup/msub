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
 * Time: 10:04:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class CleSection {
    private String courseEid;
    private String category;
    private String name;
    private String sectionEid;
    private Integer maxEnrollments = new Integer(0);
    private String siteReference;
    private String property1;
    private String property2;
    private String property3;
    private String property4;
    private String property5;


    public String getCourseEid() {
        return courseEid;
    }

    public void setCourseEid(String courseEid) {
        this.courseEid = courseEid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSectionEid() {
        return sectionEid;
    }

    public void setSectionEid(String sectionEid) {
        this.sectionEid = sectionEid;
    }

    public Integer getMaxEnrollments() {
        return maxEnrollments;
    }

    public void setMaxEnrollments(Integer maxEnrollments) {
        this.maxEnrollments = maxEnrollments;
    }

    public String getSiteReference() {
        return siteReference;
    }

    public void setSiteReference(String siteReference) {
        this.siteReference = siteReference;
    }

    public int hashCode() {
        return (sectionEid).hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof CleSection)) {
            return false;
        }

        CleSection that = (CleSection) o;
        if (that.sectionEid != null && that.sectionEid != null &&
            that.sectionEid.equals(this.sectionEid)){
            return true;
        }
       return false;
    }

    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
    }

    public String getProperty2() {
        return property2;
    }

    public void setProperty2(String property2) {
        this.property2 = property2;
    }

    public String getProperty3() {
        return property3;
    }

    public void setProperty3(String property3) {
        this.property3 = property3;
    }

    public String getProperty4() {
        return property4;
    }

    public void setProperty4(String property4) {
        this.property4 = property4;
    }

    public String getProperty5() {
        return property5;
    }

    public void setProperty5(String property5) {
        this.property5 = property5;
    }

    @Override
    public String toString() {
        return "CleSection{" +
                "courseEid='" + courseEid + '\'' +
                ", category='" + category + '\'' +
                ", name='" + name + '\'' +
                ", sectionEid='" + sectionEid + '\'' +
                ", maxEnrollments=" + maxEnrollments +
                ", siteReference='" + siteReference + '\'' +
                ", property1='" + property1 + '\'' +
                ", property2='" + property2 + '\'' +
                ", property3='" + property3 + '\'' +
                ", property4='" + property4 + '\'' +
                ", property5='" + property5 + '\'' +
                '}';
    }
}
