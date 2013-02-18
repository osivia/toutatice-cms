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
import org.osivia.portal.api.contexte.PortalControllerContext;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
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
			
			

			// Nombre de niveaux ouverts
			int openLevels = 0;
			if (req.getParameter("openLevels") != null) {
				try {
					openLevels = Integer.parseInt(req.getParameter("openLevels"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (openLevels > 0)
				window.setProperty("osivia.cms.openLevels", Integer.toString(openLevels));
			else if (window.getProperty("osivia.cms.openLevels") != null)
				window.setProperty("osivia.cms.openLevels", null);
			
			// Nombre de niveaux maximum
			int maxLevels = 0;
			if (req.getParameter("maxLevels") != null) {
				try {
					maxLevels = Integer.parseInt(req.getParameter("maxLevels"));
				} catch (Exception e) {
					// Mal formatté
				}
			}

			if (maxLevels > 0)
				window.setProperty("osivia.cms.maxLevels", Integer.toString(maxLevels));
			else if (window.getProperty("osivia.cms.maxLevels") != null)
				window.setProperty("osivia.cms.maxLevels", null);
			
			
			

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

		String openLevels = window.getProperty("osivia.cms.openLevels");
		req.setAttribute("openLevels", openLevels);
		

		String maxLevels = window.getProperty("osivia.cms.maxLevels");
		req.setAttribute("maxLevels", maxLevels);

		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/publish/admin.jsp");
		rd.include(req, res);

	}

	private NavigationDisplayItem createServiceItem(NuxeoController ctx, CMSServiceCtx cmsReadNavContext, PortalControllerContext portalCtx,
			int curLevel, int maxLevel, String spacePath, String basePath, String nuxeoPath, boolean isParentNavigable) throws Exception {
		
		
		//TODO : factoriser dans NuxeoController

		INuxeoService nuxeoService = (INuxeoService) ctx.getPortletCtx().getAttribute("NuxeoService");
		
		CMSItem navItem = ctx.getCMSService().getPortalNavigationItem(cmsReadNavContext, spacePath, nuxeoPath);
		
		if( navItem == null)
			return null;
		
		Document doc = (Document) navItem.getNativeItem();
		
		String navPath = (String) navItem.getPath();
		
		// Get root publish page

		
		Link link = ctx.getLink(doc,"menu");

		boolean selected = false;

		
		String itemPath = ctx.getContentPath();


		if (itemPath != null)	{ 
			itemPath += "/";
			
			if( itemPath.startsWith(navPath + "/") && isParentNavigable)	
			// non navigational items are not selected
			// because children elements are managed at portlet level and not CMS levels
			// So selection can not be sure
			// See FAQ sample : links between questions don't interact with CMS
				selected = true;
		}

		NavigationDisplayItem displayItem = new NavigationDisplayItem(doc.getTitle(), link.getUrl(), link.isExternal(),
				selected);

		if (curLevel + 1 <= maxLevel) {
			List<CMSItem> navItems = ctx.getCMSService().getPortalNavigationSubitems(cmsReadNavContext, basePath, nuxeoPath);
			
			for(CMSItem child : navItems){
				
				if ( "1".equals(child.getProperties().get("menuItem")) )	{
					
					NavigationDisplayItem newItem = createServiceItem(ctx, cmsReadNavContext, portalCtx, curLevel + 1, maxLevel, spacePath, basePath, child.getPath(), "1".equals(navItem.getProperties().get("navigationElement")));
					if( newItem != null)
						displayItem.getChildrens().add(	newItem);
				}
				
			}

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


			// logger.debug("doView "+ uid);
			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

			if (ctx.getBasePath() != null) {



				// rafraichir en asynchrone
				ctx.setAsynchronousUpdates(true);

				int maxLevels = 3;

				String sMaxLevels = window.getProperty("osivia.cms.maxLevels");
				if (sMaxLevels != null && sMaxLevels.length() > 0)
					maxLevels = Integer.parseInt(sMaxLevels);
				
				int openLevels = 1;
				
				String sOpenLevels = window.getProperty("osivia.cms.openLevels");
				if (sOpenLevels != null && sOpenLevels.length() > 0)
					openLevels = Integer.parseInt(sOpenLevels);

				
				
				// Navigation context
				CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
				cmsReadNavContext.setControllerContext(ctx.getPortalCtx().getControllerCtx());
				cmsReadNavContext.setScope(ctx.getNavigationScope());				

				

				NavigationDisplayItem displayItem = createServiceItem(ctx, cmsReadNavContext, new PortalControllerContext(
						getPortletContext(), request, response), 0, maxLevels, ctx.getSpacePath(), ctx.getBasePath(),  ctx.getBasePath(), true);
				
				if( displayItem != null)	{

					if (displayItem.getTitle() != null)
						response.setTitle(displayItem.getTitle());

					request.setAttribute("itemToDisplay", displayItem);
				}
				request.setAttribute("openLevels", openLevels);

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
