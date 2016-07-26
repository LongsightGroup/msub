package com.rsmart.generate.persistence;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 1/6/12
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeneratedTokens {

    private String userId;
    private String token;
    private boolean sessionValid;
    private Date dateCreated;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSessionValid() {
        return sessionValid;
    }

    public void setSessionValid(boolean sessionValid) {
        this.sessionValid = sessionValid;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
