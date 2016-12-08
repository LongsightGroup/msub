package com.rsmart.userdataservice.persistence.model;

import org.sakaiproject.user.api.UserEdit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 18, 2011
 * Time: 1:59:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RsnUser {

    private String eid;
    private String email;
    private String emailLc;
    private String firstName;
    private String lastName;
    private String type;
    private String pw;
    private String createdBy;
    private String modifiedBy;
    private String createdOn;
    private String modifiedOn;
    private Map properties;


    public RsnUser ( UserEdit edit){
      this.eid = edit.getId();
      this.email = edit.getEmail();
      this.firstName = edit.getFirstName();
      this.lastName = edit.getLastName();
      this.type = edit.getType();  
     }




    public RsnUser(){}


    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailLc() {
        return emailLc;
    }

    public void setEmailLc(String emailLc) {
        this.emailLc = emailLc;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(String modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Map getProperties() {
        if ( properties == null){
            return new HashMap(); 
        }
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }
}
