/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.render.portlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerFactory;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.PortletURLProvider;
import org.sakaiproject.util.Web;
import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.render.api.RenderResult;
import org.sakaiproject.portal.render.api.ToolRenderException;
import org.sakaiproject.portal.render.api.ToolRenderService;
import org.sakaiproject.portal.render.portlet.services.SakaiOptionalPortletContainerServices;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalCallbackService;
import org.sakaiproject.portal.render.portlet.services.SakaiPortalContext;
import org.sakaiproject.portal.render.portlet.services.SakaiPortletContainerServices;
import org.sakaiproject.portal.render.portlet.services.state.PortletState;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateAccess;
import org.sakaiproject.portal.render.portlet.services.state.PortletStateEncoder;
import org.sakaiproject.portal.render.portlet.servlet.BufferedServletResponse;
import org.sakaiproject.portal.render.portlet.servlet.SakaiServletActionRequest;
import org.sakaiproject.portal.render.portlet.servlet.SakaiServletRequest;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;

/**
 * @author ddwolf
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
public class PortletToolRenderService implements ToolRenderService
{

	/**
	 * Log instance used for all instances of this service.
	 */
	private static final Log LOG = LogFactory.getLog(PortletToolRenderService.class);

	/**
	 * Portlet Container instance used by this service.
	 */
	private PortletContainer container;

	/**
	 * Portlet Registry used by this service.
	 */
	private PortletRegistry registry = new PortletRegistry();

	private PortletStateEncoder portletStateEncoder;

	private PortalService portalService;

	public PortletStateEncoder getPortletStateEncoder()
	{
		return portletStateEncoder;
	}

	public void setPortletStateEncoder(PortletStateEncoder portletStateEncoder)
	{
		this.portletStateEncoder = portletStateEncoder;
	}

	public boolean preprocess(Portal portal, HttpServletRequest request, HttpServletResponse response,
			ServletContext context) throws IOException
	{

		String stateParam = request
				.getParameter(SakaiPortalCallbackService.PORTLET_STATE_QUERY_PARAM);

		// If there is not state parameter, short circuit
		if (stateParam == null)
		{
			return true;
		}

		PortletState state = portletStateEncoder.decode(stateParam);
		PortletStateAccess.setPortletState(request, state);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("New Portlet State retrieved for Tool '" + state.getId() + ".");
		}

		if (state.isAction())
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Processing action for placement id " + state.getId());
			}

			PortletStateAccess.setPortletState(request, state);
			SakaiPortletWindow window = isIn168TestMode(request) ? createPortletWindow(state
					.getId())
					: registry.getPortletWindow(state.getId());
			window.setState(state);

			try
			{
				PortletContainer portletContainer = getPortletContainer(context);

				portletContainer.doAction(window, new SakaiServletActionRequest(request,
						state), response);

			}
			catch (PortletException e)
			{
				throw new ToolRenderException(e.getMessage(), e);
			}
			catch (PortletContainerException e)
			{
				throw new ToolRenderException(e.getMessage(), e);
			}
			finally
			{
				state.setAction(false);
			}
			return true;
		}
		return true;
	}

	// Note ToolConfiguration extends Placement
	public RenderResult render(Portal portal, ToolConfiguration toolConfiguration,
			final HttpServletRequest request, final HttpServletResponse response,
			ServletContext context) throws IOException
	{

		getPortletDD(toolConfiguration);
		final SakaiPortletWindow window = isIn168TestMode(request) ? createPortletWindow(toolConfiguration
				.getId())
				: registry.getOrCreatePortletWindow(toolConfiguration);

		PortletState state = PortletStateAccess.getPortletState(request, window.getId()
				.getStringId());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Retrieved PortletState from request cache.  Applying to window.");
		}

		if ("true".equals(request.getParameter(portalService.getResetStateParam()))
				|| "true".equals(portalService.getResetState()))
		{
			if (state != null)
			{
				String statePrefix = "javax.portlet.p." + state.getId();

				HttpSession session = request.getSession(true);
				for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();)
				{
					String key = (String) e.nextElement();
					if (key != null && key.startsWith(statePrefix))
					{
						session.removeAttribute(key);
					}
				}
				state = null; // Remove the remaining evidence of prior
				// existence
			}
		}

		if (state == null)
		{
			state = new PortletState(window.getId().getStringId());
			PortletStateAccess.setPortletState(request, state);
		}

		window.setState(state);

		try
		{
			final HttpServletRequest req = new SakaiServletRequest(request, state);
			final PortletContainer portletContainer = getPortletContainer(context);

			// Derive the Edit and Help URLs
			String editUrl = null;
			String helpUrl = null;
			RequiredContainerServices rs = portletContainer
					.getRequiredContainerServices();
			PortalCallbackService pcs = rs.getPortalCallbackService();
			PortletURLProvider pup = null;

			if (isPortletModeAllowed(toolConfiguration, "edit"))
			{
				pup = pcs.getPortletURLProvider(request, window);
				// System.out.println("pup = "+pup);
				pup.setPortletMode(new PortletMode("edit"));
				// System.out.println("pup edit="+pup.toString());
				editUrl = pup.toString();
			}

			if (isPortletModeAllowed(toolConfiguration, "help"))
			{
				pup = pcs.getPortletURLProvider(request, window);
				pup.setPortletMode(new PortletMode("help"));
				// System.out.println("pup help="+pup.toString());
				helpUrl = pup.toString();
			}

			return new Sakai168RenderResult(req, response, portletContainer, window,
					helpUrl, editUrl);
		}
		catch (PortletContainerException e)
		{
			throw new ToolRenderException(e.getMessage(), e);
		}
	}

	private class Sakai168RenderResult implements RenderResult
	{
		private HttpServletRequest req = null;

		private HttpServletResponse response = null;

		private PortletContainer portletContainer = null;

		private BufferedServletResponse bufferedResponse = null;

		private SakaiPortletWindow window = null;

		private String helpUrl = null;

		private String editUrl = null;

		public Sakai168RenderResult(HttpServletRequest req, HttpServletResponse response,
				PortletContainer pc, SakaiPortletWindow window, String helpUrl,
				String editUrl)
		{
			this.req = req;
			this.response = response;
			this.portletContainer = pc;
			this.window = window;
			this.helpUrl = helpUrl;
			this.editUrl = editUrl;
		}

		private void renderResponse() throws ToolRenderException
		{
			if (bufferedResponse == null)
			{
				bufferedResponse = new BufferedServletResponse(response);
				try
				{
					portletContainer.doRender(window, req, bufferedResponse);
				}
				catch (PortletException e)
				{
					throw new ToolRenderException(e.getMessage(), e);
				}
				catch (IOException e)
				{
					throw new ToolRenderException(e.getMessage(), e);
				}
				catch (PortletContainerException e)
				{
					throw new ToolRenderException(e.getMessage(), e);
				}
			}
		}

		public String getContent() throws ToolRenderException
		{
			renderResponse();
			return bufferedResponse.getInternalBuffer().getBuffer().toString();
		}

		public String getTitle() throws ToolRenderException
		{
			renderResponse();
			return Web.escapeHtml(PortletStateAccess.getPortletState(req, window.getId().getStringId())
					.getTitle());
		}

		public String getJSR168EditUrl()
		{
			return this.editUrl;
		}

		public String getJSR168HelpUrl()
		{
			return this.helpUrl;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.portal.render.api.RenderResult#getHead()
		 */
		public String getHead()
		{
			return "";
		}

	};

	// TODO: This must be test code and needs removing
	private SakaiPortletWindow createPortletWindow(String windowId)
	{
		String contextPath = "/rsf";
		String portletName = "numberguess";
		return new SakaiPortletWindow(windowId, contextPath, portletName);
	}

	private PortletContainer getPortletContainer(ServletContext context)
			throws PortletContainerException
	{
		if (container == null)
		{
			container = createPortletContainer();
			container.init(context);
		}

		return container;
	}

	private PortletContainer createPortletContainer() throws PortletContainerException
	{
		SakaiPortletContainerServices services = new SakaiPortletContainerServices();
		SakaiOptionalPortletContainerServices optServices = new SakaiOptionalPortletContainerServices();
		services.setPortalCallbackService(new SakaiPortalCallbackService());
		services.setPortalContext(new SakaiPortalContext());
		return PortletContainerFactory.getInstance().createContainer("sakai", services,
				optServices);
	}

	private static boolean isIn168TestMode(HttpServletRequest request)
	{
		HttpSession session = request.getSession(true);
		if (session.getAttribute("test168") != null
				|| request.getParameter("test168") != null)
		{
			request.getSession(true).setAttribute("test168", Boolean.TRUE.toString());
			return true;
		}
		return false;
	}

	private boolean isPortletApplication(ServletContext context,
			ToolConfiguration configuration) throws ToolRenderException,
			MalformedURLException
	{
		SakaiPortletWindow window = registry.getOrCreatePortletWindow(configuration);
		if (window == null)
		{
			return false;
		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Checking context for potential portlet ");
		}
		ServletContext crossContext = context.getContext(window.getContextPath());
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Got servlet context as  " + crossContext);
			LOG.debug("Getting Context for path " + window.getContextPath());
			LOG.debug("Base Path " + crossContext.getRealPath("/"));
			LOG.debug("Context Name " + crossContext.getServletContextName());
			LOG.debug("Server Info " + crossContext.getServerInfo());
			LOG.debug("      and it is a portlet ? :"
					+ (crossContext.getResource("/WEB-INF/portlet.xml") != null));
		}
		return crossContext.getResource("/WEB-INF/portlet.xml") != null;
	}

	public boolean accept(Portal portal, ToolConfiguration configuration, HttpServletRequest request,
			HttpServletResponse response, ServletContext context)
	{
		try
		{
			if (isIn168TestMode(request))
			{
				LOG.warn("In portlet test mode");
				return true;
			}
			if (isPortletApplication(context, configuration))
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("Tool " + configuration.getToolId() + " is a portlet");
				}
				return true;
			}
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Tool " + configuration.getToolId() + " is not a portlet");
			}
			return false;
		}
		catch (MalformedURLException e)
		{
			LOG.error("Failed to render ", e);
			return false;
		}
		catch (ToolRenderException e)
		{
			LOG.error("Failed to render ", e);
			return false;
		}
	}

	public void reset( ToolConfiguration configuration)
	{
		registry.reset(configuration);
	}

	public PortletDD getPortletDD(Placement placement)
	{
		Properties toolProperties = placement.getPlacementConfig();
		String portletName = null;
		String appName = null;
		String fred = null;

		if (toolProperties != null)
		{
			// System.out.println("tp = "+toolProperties);
			portletName = toolProperties.getProperty(PortalService.TOOL_PORTLET_NAME);
			appName = toolProperties.getProperty(PortalService.TOOL_PORTLET_APP_NAME);
			fred = toolProperties.getProperty("FRED");
		}
		// System.out.println("appName="+appName);
		// System.out.println("portletName="+portletName);
		// System.out.println("fred="+fred);
		Properties configProperties = placement.getConfig();
		if (configProperties != null)
		{
			if (portletName == null)
			{
				portletName = configProperties
						.getProperty(PortalService.TOOL_PORTLET_NAME);
			}
			if (appName == null)
			{
				appName = configProperties
						.getProperty(PortalService.TOOL_PORTLET_APP_NAME);
			}
		}
		// System.out.println("appName="+appName);
		// System.out.println("portletName="+portletName);
		PortletDD pdd = PortletContextManager.getManager().getPortletDescriptor(appName,
				portletName);
		// System.out.println("pdd="+pdd);
		return pdd;
	}

	public boolean isPortletModeAllowed(Placement placement, String mode)
	{
		if (placement == null || mode == null) return false;
		PortletDD pdd = getPortletDD(placement);
		if (pdd == null) return true;
		Iterator supports = pdd.getSupports().iterator();
		while (supports.hasNext())
		{
			SupportsDD sup = (SupportsDD) supports.next();
			Iterator modes = sup.getPortletModes().iterator();
			while (modes.hasNext())
			{
				if (modes.next().toString().equalsIgnoreCase(mode.toString()))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return the portalService
	 */
	public PortalService getPortalService()
	{
		return portalService;
	}

	/**
	 * @param portalService
	 *        the portalService to set
	 */
	public void setPortalService(PortalService portalService)
	{
		this.portalService = portalService;
	}
}
