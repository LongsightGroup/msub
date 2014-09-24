package com.rsmart.userdataservice.persistence.model;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: Jan 18, 2011
 * Time: 2:09:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class RsnUserProperty {

    private RsnUserPropertyId rsnUserPropertyId;
    private String propertyValue;


    public RsnUserPropertyId getRsnUserPropertyId() {
        return rsnUserPropertyId;
    }

    public void setRsnUserPropertyId(RsnUserPropertyId rsnUserPropertyId) {
        this.rsnUserPropertyId = rsnUserPropertyId;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
