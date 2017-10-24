package com.rsmart.decryption.impl;

import com.rsmart.generate.tokens.api.TokenGeneratedService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Created by IntelliJ IDEA.
 * User: lmaxey
 * Date: 3/2/12
 * Time: 9:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class RsmartDecyptionDeleteJob implements Job {
    private static final Log logger = LogFactory.getLog(RsmartDecyptionDeleteJob.class);
    private TokenGeneratedService tokenGeneratedService;
    private AuthzGroupService authzGroupService;
    private SessionManager sessionManager;


    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getKey().getName();
        if (jobName != null) {
            this.execute();
        }
    }


    public void setTokenGeneratedService(TokenGeneratedService tokenGeneratedService) {
        this.tokenGeneratedService = tokenGeneratedService;
    }

    /**
     * Executes Delete For generated_tokens table
     */
    public void execute() {
        actAsAdmin();
        tokenGeneratedService.deleteTokens();
    }

    /**
     * Convenience routine to support the frequent testing need to switch authn/authz identities.
     *
     * static.
     */
    private void actAsAdmin() {
		String userId = "admin";
		Session session = sessionManager.getCurrentSession();
		session.setUserEid(userId);
		session.setUserId(userId);
		authzGroupService.refreshUser(userId);
    }

    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
