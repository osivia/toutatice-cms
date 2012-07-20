package fr.toutatice.portail.cms.nuxeo.portlets.publish;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
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
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPath.Format;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.nuxeo.INuxeoCustomizer;
import fr.toutatice.portail.core.nuxeo.INuxeoService;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class MenuPortlet extends CMSPortlet {

	private static Log logger = LogFactory.getLog(MenuPortlet.class);

	private IPortalUrlFactory portalUrlFactory;

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		portalUrlFactory = (IPortalUrlFactory) getPortletContext().getAttribute("UrlService");
		if (portalUrlFactory == null) {
			throw new PortletException("Cannot start TestPortlet due to service unavailability");
		}
	}

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);

			// Taille de page
			int nbLevels = 0;
			if (req.getParameter("nbLevels") != null) {
				try {
					nbLevels = Integer.parseInt(req.getParameter("nbLevels"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (nbLevels > 0)
				window.setProperty("pia.cms.nbLevels", Integer.toString(nbLevels));
			else if (window.getProperty("pia.cms.nbLevels") != null)
				window.setProperty("pia.cms.nbLevels", null);

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

		String nbLevels = window.getProperty("pia.cms.nbLevels");
		req.setAttribute("nbLevels", nbLevels);

		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/publish/admin.jsp");
		rd.include(req, res);

	}

	private NavigationDisplayItem createServiceItem(NuxeoController ctx, CMSServiceCtx cmsReadNavContext, PortalControllerContext portalCtx,
			int curLevel, int maxLevel, String basePath, String nuxeoPath) throws Exception {
		
		
		//TODO : factoriser dans NuxeoController

		INuxeoService nuxeoService = (INuxeoService) ctx.getPortletCtx().getAttribute("NuxeoService");
		
		CMSItem navItem = nuxeoService.getPortalNavigationItem(cmsReadNavContext, basePath, nuxeoPath);
		Document doc = (Document) navItem.getNativeItem();
		
		
		
		// Méthode non optimisée
		//CategoryBean portalSite = (CategoryBean) ctx.executeNuxeoCommand(new CategoryFetchCommand(nuxeoPath));
		//Document doc = portalSite.getPortalDocument();

		
		// Get root publish page

		Window window = (Window) portalCtx.getRequest().getAttribute("pia.window");
		Page page = window.getPage();

		Map<String, String> pageParams = new HashMap<String, String>();

		/*
		 * 
		 * Map<String, List<String>> selectors =
		 * PageSelectors.decodeProperties(portalCtx
		 * .getRequest().getParameter("selectors"));
		 * 
		 * if( selectors != null && selectors.size() > 0)
		 * pageParams.put("selectors",
		 * PageSelectors.encodeProperties(selectors));
		 */

		//Link link = new Link(portalUrlFactory.getCMSUrl(portalCtx,
		//		page.getId().toString(PortalObjectPath.CANONICAL_FORMAT), doc.getPath(), pageParams, null, IPortalUrlFactory.TEMPLATE_NAVIGATION, null, null), false);
		
		Link link = ctx.getLink(doc,"menu");

		boolean selected = false;

		
		
		String categoryPath = portalCtx.getRequest().getParameter("pia.cms.path");
		
		String itemRelativePath = portalCtx.getRequest().getParameter("pia.cms.itemRelPath");
		if( itemRelativePath != null)
			categoryPath += itemRelativePath;

		if (categoryPath != null && categoryPath.startsWith(doc.getPath()))
			selected = true;

		NavigationDisplayItem displayItem = new NavigationDisplayItem(doc.getTitle(), link.getUrl(), link.isExternal(),
				selected);

		if (curLevel + 1 <= maxLevel) {
			List<CMSItem> navItems = nuxeoService.getPortalNavigationSubitems(cmsReadNavContext, basePath, nuxeoPath);
			
			for(CMSItem child : navItems){
				
				if (link != null)
					displayItem.getChildrens().add(
							createServiceItem(ctx, cmsReadNavContext, portalCtx, curLevel + 1, maxLevel, basePath, child.getPath()));
				
				
			}

			/*
			for (Document child : portalSite.getChildren()) {

				if (link != null)
					displayItem.getChildrens().add(
							createServiceItem(ctx, cmsReadNavContext, portalCtx, curLevel + 1, maxLevel, basePath, child.getPath()));
			}
			*/
		}

		return displayItem;

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
			nuxeoPath = window.getPageProperty("pia.cms.basePath");

			// logger.debug("doView "+ uid);


			if (nuxeoPath != null) {

				NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

				// rafraichir en asynchrone
				ctx.setAsynchronousUpdates(true);

				int maxLevels = 1;

				String sNbLevels = window.getProperty("pia.cms.nbLevels");
				if (sNbLevels != null && sNbLevels.length() > 0)
					maxLevels = Integer.parseInt(sNbLevels);
				
				
				// Navigation context
				CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
				cmsReadNavContext.setCtx(ctx.getPortalCtx().getControllerCtx());
				cmsReadNavContext.setScope(ctx.getScope());				

				

				NavigationDisplayItem displayItem = createServiceItem(ctx, cmsReadNavContext, new PortalControllerContext(
						getPortletContext(), request, response), 0, maxLevels, nuxeoPath, nuxeoPath);

				if (displayItem.getTitle() != null)
					response.setTitle(displayItem.getTitle());

				request.setAttribute("itemToDisplay", displayItem);

				request.setAttribute("ctx", ctx);

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/publish/view.jsp").include(request, response);
			} else {
				response.setContentType("text/html");
				response.getWriter().print("<h2>Path de la page non défini</h2>");
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
