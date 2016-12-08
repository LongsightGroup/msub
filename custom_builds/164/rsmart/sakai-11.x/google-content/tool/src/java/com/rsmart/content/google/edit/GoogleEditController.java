package com.rsmart.content.google.edit;

import com.rsmart.content.google.api.GoogleDocDescriptor;
import com.rsmart.content.google.api.GoogleDocsException;
import com.rsmart.content.google.api.GoogleDocsService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * User: duffy
 * Date: Jul 29, 2011
 * Time: 11:04:12 AM
 */
public class GoogleEditController
    extends AbstractController
{
    private ToolManager
        toolManager = null;
    private SessionManager
        sessionManager = null;
    private GoogleDocsService
        googleDocsService = null;

    public GoogleDocsService getGoogleDocsService()
    {
        return googleDocsService;
    }

    public void setGoogleDocsService(GoogleDocsService googleDocsService)
    {
        this.googleDocsService = googleDocsService;
    }

    public SessionManager getSessionManager()
    {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    public ToolManager getToolManager()
    {
        return toolManager;
    }

    public void setToolManager(ToolManager toolManager)
    {
        this.toolManager = toolManager;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse)
        throws Exception
    {

        final ToolSession
            toolSession = getSessionManager().getCurrentToolSession();
        final ResourceToolActionPipe
            pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

        if (httpServletRequest.getParameter("_cancel") != null)
        {
            pipe.setActionCanceled(false);
            pipe.setErrorEncountered(false);
            pipe.setActionCompleted(true);

            toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
            toolSession.removeAttribute(ResourceToolAction.STARTED);

            final Tool
                tool = getToolManager().getCurrentTool();
            final String
                url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

            toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

            httpServletResponse.sendRedirect(url);
            
            return null;
        }

        final byte[]
            content = pipe.getContent();
        final GoogleDocsService
            google = getGoogleDocsService();
        final HashMap<String, Object>
            model = new HashMap<String, Object>();


        try
        {
            GoogleDocDescriptor
                desc = google.xmlToDescriptor(new String(content));

            model.put("googleRedirectURL", desc.getLink());
            return new ModelAndView ("editInGoogle", model);
        }
        catch (GoogleDocsException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

            pipe.setActionCanceled(false);
            pipe.setErrorEncountered(false);
            pipe.setActionCompleted(true);

            toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
            toolSession.removeAttribute(ResourceToolAction.STARTED);

            final Tool
                tool = getToolManager().getCurrentTool();
            final String
                url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

            toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

            httpServletResponse.sendRedirect(url);
        }

        return null;
    }
}
