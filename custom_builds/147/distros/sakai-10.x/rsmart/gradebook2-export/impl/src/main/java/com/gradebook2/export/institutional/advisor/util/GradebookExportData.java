package com.gradebook2.export.institutional.advisor.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GradebookExportData {
	
	private String siteId;
	private String gradebookId;
	private String instructorEid;
	private String instructorUid;
	private String instructorEmail;
    private String serverUrl;
	private Map<String, Map<String, String>> instructorList = new HashMap();
	private String serverId;
	private Date submissionDate;
	private String campusId;
	private Map<String, String> siteProperties = new HashMap();
    private String gradebookToolPlacementId;
    private String siteTitle;
    private String siteDescription;
    private String siteShortDescription;
	
	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

    public String getGradebookToolPlacementId() {
        return gradebookToolPlacementId;
    }

    public void setGradebookToolPlacementId(String gradebookToolPlacementId) {
        this.gradebookToolPlacementId = gradebookToolPlacementId;
    }

    public String getGradebookId() {
		return gradebookId;
	}

	public void setGradebookId(String gradebookId) {
		this.gradebookId = gradebookId;
	}

	public String getInstructorEid() {
		return instructorEid;
	}

	public void setInstructorEid(String instructorEid) {
		this.instructorEid = instructorEid;
	}

	public String getInstructorUid() {
		return instructorUid;
	}

	public void setInstructorUid(String instructorUid) {
		this.instructorUid = instructorUid;
	}

	public String getInstructorEmail() {
		return instructorEmail;
	}

	public void setInstructorEmail(String instructorEmail) {
		this.instructorEmail = instructorEmail;
	}

	public Map<String, Map<String, String>> getInstructorList() {
		return instructorList;
	}

	public Map<String, String> getSiteProperties() {
		return siteProperties;
	}


	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	public String getCampusId() {
		return campusId;
	}

	public void setCampusId(String campusId) {
		this.campusId = campusId;
	}

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public GradebookExportData() {
		
	}

    public String getSiteTitle() {
        return siteTitle;
    }

    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    public String getSiteDescription() {
        return siteDescription;
    }

    public void setSiteDescription(String siteDescription) {
        this.siteDescription = siteDescription;
    }

    public String getSiteShortDescription() {
        return siteShortDescription;
    }

    public void setSiteShortDescription(String siteShortDescription) {
        this.siteShortDescription = siteShortDescription;
    }
}