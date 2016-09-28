package org.sakaiproject.login.filter;

import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Performs a logout of Sakai invalidating the {@link Session}
 * if {@link #isInvalidateSakaiSession()} is {@code true} and the session is not {@code null}.
 */
public class SakaiLogoutSamlFilter extends SecurityContextLogoutHandler {

    private UsageSessionService usageSessionService;
    private SessionManager sessionManager;

    public void setUsageSessionService(UsageSessionService usageSessionService) {
        this.usageSessionService = usageSessionService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private boolean invalidateSakaiSession = true;

    /**
     * Requires the request to be passed in.
     *
     * @param request        from which to obtain the HTTP session (cannot be null)
     * @param response       not used (can be <code>null</code>)
     * @param authentication not used (can be <code>null</code>)
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        super.logout(request, response, authentication);

        if (invalidateSakaiSession) {
            Session session = sessionManager.getCurrentSession();

            if (session != null) {
                //log.debug("SAML logout invalidating sakai session: {}", session.getId());
                usageSessionService.logout();
            }
        }
    }
}
