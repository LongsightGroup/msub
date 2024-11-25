/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookPermissionService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.*;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Provide a UI handle to the selected gradebook.
 *
 * Since all application-specific backing beans (a group that doesn't include
 * authentication handlers) require a gradebook ID to use with any of the
 * business or facade services, this bean is also a reasonable place to centralize
 * configuration of and access to those services.
 */
public class GradebookBean extends InitializableBean {
    private static final Log logger = LogFactory.getLog(GradebookBean.class);

    private Long gradebookId;
    private String gradebookUid;


    // These interfaces are defined application-wide (through Spring, although the
    // UI classes don't know that).
    private GradebookManager gradebookManager;
    private SectionAwareness sectionAwareness;
    private UserDirectoryService userDirectoryService;
    private Authn authnService;
    private Authz authzService;
    private ContextManagement contextManagementService;
    private EventTrackingService eventTrackingService;
    private ConfigurationBean configurationBean;
    private GradebookPermissionService gradebookPermissionService;
    private GradebookExternalAssessmentService gradebookExternalAssessmentService;
    private SecurityService securityService;
    private SiteService siteService;
    /**
     * @return Returns the gradebookId.
     */
    public final Long getGradebookId() {
        refreshFromRequest();
        return gradebookId;
    }

    private final void setGradebookId(Long gradebookId) {
        this.gradebookId = gradebookId;
    }

    /**
     * @param newGradebookUid The gradebookId to set.
     * Since this is coming from the client, the application should NOT
     * trust that the current user actually has access to the gradebook
     * with this UID. This design assumes that authorization will come
     * into play on each request.
     */
    public final void setGradebookUid(String newGradebookUid) {
        Long newGradebookId = null;
        if (newGradebookUid != null) {
            Gradebook gradebook = null;
            try {
                gradebook = getGradebookManager().getGradebook(newGradebookUid);
            } catch (GradebookNotFoundException gnfe) {
                logger.error("Request made for inaccessible gradebookUid=" + newGradebookUid);
                newGradebookUid = null;
            }
            if(gradebook == null)
            	throw new IllegalStateException("Gradebook gradebook == null!");
            newGradebookId = gradebook.getId();
            if (logger.isDebugEnabled()) logger.debug("setGradebookUid gradebookUid=" + newGradebookUid + ", gradebookId=" + newGradebookId);
        }
        this.gradebookUid = newGradebookUid;
        setGradebookId(newGradebookId);
    }

    private final void refreshFromRequest() {
        String requestUid = contextManagementService.getGradebookUid(FacesContext.getCurrentInstance().getExternalContext().getRequest());
        if ((requestUid != null) && (!requestUid.equals(gradebookUid))) {
            if (logger.isDebugEnabled()) logger.debug("resetting gradebookUid from " + gradebookUid);
            setGradebookUid(requestUid);
        }
    }

    /**
     * Static method to pick up the gradebook UID, if any, held by the current GradebookBean, if any.
     * Meant to be called from a servlet filter.
     */
    public static String getGradebookUidFromRequest(ServletRequest request) {
        String gradebookUid = null;
        HttpSession session = ((HttpServletRequest)request).getSession();
        GradebookBean gradebookBean = (GradebookBean)session.getAttribute("gradebookBean");
        if (gradebookBean != null) {
            gradebookUid = gradebookBean.gradebookUid;
        }
        return gradebookUid;
    }


    // The following getters are used by other backing beans. The setters are used only by
    // the bean factory.

    /**
     * @return Returns the gradebookManager.
     */
    public GradebookManager getGradebookManager() {
        return gradebookManager;
    }

    /**
     * @param gradebookManager The gradebookManager to set.
     */
    public void setGradebookManager(GradebookManager gradebookManager) {
        this.gradebookManager = gradebookManager;
    }

    public SectionAwareness getSectionAwareness() {
        return sectionAwareness;
    }
    public void setSectionAwareness(SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }

    /**
     * @return Returns the userDirectoryService.
     */
    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }
    /**
     * @param userDirectoryService The userDirectoryService to set.
     */
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }
    /**
     * @return Returns the authnService.
     */
    public Authn getAuthnService() {
        return authnService;
    }
    /**
     * @param authnService The authnService to set.
     */
    public void setAuthnService(Authn authnService) {
        this.authnService = authnService;
    }

    public Authz getAuthzService() {
        return authzService;
    }
    public void setAuthzService(Authz authzService) {
        this.authzService = authzService;
    }

    /**
     * @return Returns the contextManagementService.
     */
    public ContextManagement getContextManagementService() {
        return contextManagementService;
    }
    /**
     * @param contextManagementService The contextManagementService to set.
     */
    public void setContextManagementService(ContextManagement contextManagementService) {
        this.contextManagementService = contextManagementService;
    }


    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

	public ConfigurationBean getConfigurationBean() {
		return configurationBean;
	}
	public void setConfigurationBean(ConfigurationBean configurationBean) {
		this.configurationBean = configurationBean;
	}
	
	public GradebookPermissionService getGradebookPermissionService() {
		return gradebookPermissionService;
	}
	public void setGradebookPermissionService(GradebookPermissionService gradebookPermissionService) {
		this.gradebookPermissionService = gradebookPermissionService;
	}
    
     public void setSecurityService(SecurityService securityService){
        this.securityService = securityService;
    }
    public SecurityService getSecurityService(){
        return securityService;
    }
    public void setSiteService(SiteService siteService){
        this.siteService = siteService;
    }
    public SiteService getSiteService(){
        return siteService;
    }
    public boolean canViewOwnGrades(String userId, String siteId){
        return securityService.unlock(userId, "gradebook.viewOwnGrades", siteService.siteReference(siteId));
    }   
	
	public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
		return gradebookExternalAssessmentService;
	}
	public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService gradebookExternalAssessmentService) {
		this.gradebookExternalAssessmentService = gradebookExternalAssessmentService;
	}
}



