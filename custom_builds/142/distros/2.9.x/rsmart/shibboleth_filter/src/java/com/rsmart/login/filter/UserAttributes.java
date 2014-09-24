package com.rsmart.login.filter;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Oct 8, 2009
 * Time: 4:55:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserAttributes
{
    private String eid;
    private String firstName;
    private String lastName;
    private String email;
    private String type;

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

}
