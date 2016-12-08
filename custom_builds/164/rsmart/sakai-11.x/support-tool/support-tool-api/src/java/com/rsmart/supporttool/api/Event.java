package com.rsmart.supporttool.api;

public class Event {

    protected String id, date, type, ref, context, sessionId, code;

    public Event(){

    }

    public Event(String id, String date, String type, String ref, String context, String sessionId, String code){
        this.id = id;
        this.date = date;
        this.type = type;
        this.ref = ref;
        this.context = context;
        this.sessionId = sessionId;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}