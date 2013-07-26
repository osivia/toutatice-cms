package fr.toutatice.portail.cms.nuxeo.portlets.portalsite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;

/**
 * Portlet d'affichage d'un menu de navigation portalSite sur 1 niveau
 */

public class NavigationPortlet extends CMSPortlet {

	private static Log logger = LogFactory.getLog(NavigationPortlet.class);

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));

			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("osivia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("osivia.cms.scope") != null)
				window.setProperty("osivia.cms.scope", null);
			
	
				

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}
	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		

		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);
		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);
		
		
		String scope = window.getProperty("osivia.cms.scope");
		req.setAttribute("scope", scope);
		

		
		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/portalsite/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			/* On détermine l'uid et le scope */
			
			PortalWindow window = WindowFactory.getWindow(request);

			String nuxeoPath = null;
	
			// portal window parameter (appels dynamiques depuis le portail)
			nuxeoPath = window.getProperty("osivia.cms.uri");

			// logger.debug("doView "+ uid);

			if (nuxeoPath == null) {
				// WIndow parameter (back-office)
				nuxeoPath = window.getProperty("osivia.nuxeoPath");
			}

			if (nuxeoPath != null) {


				NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
								
					// rafraichir en asynchrone
				ctx.setAsynchronousUpdates(true);
				

				PortalSiteBean portalSite = (PortalSiteBean) ctx.executeNuxeoCommand(new PortalSiteFetchCommand(
						ctx.getComputedPath(nuxeoPath) ));

				if (portalSite.getPortalDocument().getTitle() != null)
					response.setTitle(portalSite.getPortalDocument().getTitle());

				List<ServiceDisplayItem> listItems = new ArrayList<ServiceDisplayItem>();
				for (Document child : portalSite.getChildren()) {

					Link link = ctx.getLink(child, "menu");

					if (link != null)
						listItems.add(new ServiceDisplayItem(child.getTitle(), link.getUrl(), link.isExternal()));
				}

				request.setAttribute("serviceItems", listItems);

				request.setAttribute("ctx", ctx);

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/portalsite/view.jsp").include(request, response);
			} else {
				response.setContentType("text/html");
				response.getWriter().print("<h2>Document non défini</h2>");
				response.getWriter().close();
				return;
			}

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		}

		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}

}
