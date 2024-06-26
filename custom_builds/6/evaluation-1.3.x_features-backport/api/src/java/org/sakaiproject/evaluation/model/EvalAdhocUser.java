/**
 * $Id: EvalAdhocUser.java 59127 2009-03-20 12:08:27Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/branches/1.3.x/api/src/java/org/sakaiproject/evaluation/model/EvalAdhocUser.java $
 * EvalAdhocGroup.java - evaluation - Mar 4, 2008 1:53:13 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * This represents an adhoc user (internal user) in the evaluation system
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAdhocUser implements java.io.Serializable {

    public final static String TYPE_EVALUATOR = "evaluator";
    public final static String TYPE_EVALUATEE = "evaluatee";

    public final static String ADHOC_ID_PREFIX = "adhoc-user:";

    /**
     * Get the id from the adhoc userId, return null if the id cannot be found
     * 
     * @param userId
     *           the internal userId for this adhoc user
     * @return the persistent id
     */
    public static Long getIdFromAdhocUserId(String userId) {
        Long id = null;
        int location = userId.indexOf(ADHOC_ID_PREFIX);
        if (location != -1) {
            String idString = userId.substring(location + ADHOC_ID_PREFIX.length());
            id = new Long(idString);
        }
        return id;
    }

    /**
     * @return the internal userId for this adhoc user (will be null if the id is null), this is
     *         generated by adding the {@link #ADHOC_ID_PREFIX} ({@value #ADHOC_ID_PREFIX}) in
     *         front of the persistent id
     */
    public String getUserId() {
        if (id != null) {
            return ADHOC_ID_PREFIX + id;
        } else {
            return null;
        }
    }

    // Fields

    protected Long id;

    protected Date lastModified;

    /**
     * The creator of this adhoc user
     */
    protected String owner;
    /**
     * The type of this user, use {@link #TYPE_EVALUATEE} (instructor) or {@link #TYPE_EVALUATOR}
     * (student/participant)
     */
    protected String type;
    /**
     * The email address for this user if they have one, this MUST be set for all adhoc users
     */
    protected String email;
    /**
     * The string which is the username (eid) for this user, null if none is set
     */
    protected String username;
    /**
     * The displayable name of this user, null if no display name is set
     */
    protected String displayName;

    // Constructors

    /** default constructor */
    public EvalAdhocUser() {
    }

    /**
     * Minimal constructor, sets the type automatically to {@link #TYPE_EVALUATOR}
     * 
     * @param owner
     * @param email
     */
    public EvalAdhocUser(String owner, String email) {
        this(owner, email, email, owner, null);
    }

    /**
     * Full constructor
     * 
     * @param owner
     * @param email
     * @param username
     * @param displayName
     * @param type
     */
    public EvalAdhocUser(String owner, String email, String username, String displayName, String type) {
        this.lastModified = new Date();
        this.owner = owner;
        if (this.type == null) {
            this.type = TYPE_EVALUATOR;
        }
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // this should stop this from being set more than once
        if (this.email == null) {
            this.email = email;
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
