package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSObjectPath;
import fr.toutatice.portail.core.cms.CMSServiceCtx;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewFragmentPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(ViewFragmentPortlet.class);

	public static Map<String, FragmentType> getFragments() {

		Map<String, FragmentType> fragments = new LinkedHashMap<String, FragmentType>();

		fragments.put("text_property",
				new FragmentType("text_property", "Propriété texte", "property-text", "property"));
		fragments
				.put("html_property", new FragmentType("html_property", "Propriété html", "property-html", "property"));
		fragments.put("navigation_picture", new FragmentType("navigation_picture", "Visuel navigation",
				"navigation-picture", "navigation"));

		return fragments;
	}

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("changeFragmentType") != null) {
			res.setRenderParameter("fragmentTypeId", req.getParameter("fragmentTypeId"));
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);

			if (req.getParameter("fragmentTypeId") != null && req.getParameter("fragmentTypeId").length() > 0)
				window.setProperty("pia.fragmentTypeId", req.getParameter("fragmentTypeId"));
			else if (window.getProperty("pia.fragmentTypeId") != null)
				window.setProperty("pia.fragmentTypeId", null);

			if (req.getParameter("nuxeoPath") != null)
				window.setProperty("pia.nuxeoPath", req.getParameter("nuxeoPath"));

			if (req.getParameter("scope") != null) {
				if (req.getParameter("scope").length() > 0)
					window.setProperty("pia.cms.scope", req.getParameter("scope"));
				else if (window.getProperty("pia.cms.scope") != null)
					window.setProperty("pia.cms.scope", null);
			}

			if (req.getParameter("propertyName") != null) {
				if (req.getParameter("propertyName").length() > 0)
					window.setProperty("pia.propertyName", req.getParameter("propertyName"));
				else if (window.getProperty("pia.propertyName") != null)
					window.setProperty("pia.propertyName", null);
			}

			if (req.getParameter("displayLiveVersion") != null) {

				if ("1".equals(req.getParameter("displayLiveVersion")))
					window.setProperty("pia.cms.displayLiveVersion", "1");
				else if (window.getProperty("pia.cms.displayLiveVersion") != null)
					window.setProperty("pia.cms.displayLiveVersion", null);
			}

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
		String nuxeoPath = window.getProperty("pia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);

		String propertyName = window.getProperty("pia.propertyName");
		if (propertyName == null)
			propertyName = "";
		req.setAttribute("propertyName", propertyName);

		String scope = window.getProperty("pia.cms.scope");
		req.setAttribute("scope", scope);

		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		req.setAttribute("displayLiveVersion", displayLiveVersion);

		String fragmentTypeId = req.getParameter("fragmentTypeId");
		if (fragmentTypeId == null)
			fragmentTypeId = window.getProperty("pia.fragmentTypeId");

		req.setAttribute("fragmentTypeId", fragmentTypeId);

		req.setAttribute("fragmentTypes", getFragments());

		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/fragment/admin.jsp");
		rd.include(req, res);

	}

	private String computePageTemplate(NuxeoController ctx, CMSServiceCtx navCtx) throws CMSException {

		String pathToCheck = ctx.getNavigationPath();
		String pageTemplate = null;

		do {

			CMSItem cmsItemNav = ctx.getNuxeoCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(),
					pathToCheck);

			if (cmsItemNav != null) {

				if (cmsItemNav.getProperties().get("pageTemplate") != null) {
					pageTemplate = cmsItemNav.getProperties().get("pageTemplate");
				}

			}

			// One level up
			CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
			pathToCheck = parentPath.toString();

		} while (pageTemplate == null && pathToCheck.contains(ctx.getSpacePath()));

		return pageTemplate;
	}
	
	
	

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			String fragmentTypeId = (String) window.getProperty("pia.fragmentTypeId");

			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

			if (fragmentTypeId != null) {
				FragmentType fragmentType = getFragments().get(fragmentTypeId);

				if (fragmentType.getKey().equals("text_property") || fragmentType.getKey().equals("html_property")) {

					/* On détermine l'uid et le scope */

					String nuxeoPath = null;

					// portal window parameter (appels dynamiques depuis le
					// portail)
					nuxeoPath = window.getProperty("pia.cms.uri");

					// logger.debug("doView "+ uid);

					if (nuxeoPath == null) {
						// WIndow parameter (back-office)
						nuxeoPath = window.getProperty("pia.nuxeoPath");
					}

					if (nuxeoPath != null) {

						nuxeoPath = ctx.getComputedPath(nuxeoPath);

						Document doc = ctx.fetchDocument(nuxeoPath);

						if (doc.getTitle() != null)
							response.setTitle(doc.getTitle());

						ctx.setCurrentDoc(doc);
						request.setAttribute("doc", doc);
						request.setAttribute("ctx", ctx);
						request.setAttribute("propertyName", window.getProperty("pia.propertyName"));

					}
				}

				if (fragmentType.getKey().equals("navigation_picture")) {

					// Navigation context
					CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
					cmsReadNavContext.setControllerContext(ctx.getPortalCtx().getControllerCtx());
					cmsReadNavContext.setScope(ctx.getNavigationScope());

					request.setAttribute("ctx", ctx);
					
					if( ctx.getNavigationPath() != null)
						request.setAttribute("navigationPageTemplate", computePageTemplate(ctx, cmsReadNavContext));
				}

				request.setAttribute("fragmentType", fragmentType);

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/fragment/view.jsp").include(request, response);
			} else {
				response.setContentType("text/html");
				response.getWriter().print("<h2>Fragment non défini</h2>");
				response.getWriter().close();
				return;
			}

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		} catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}

}
