package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.List;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.WindowState;

import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentResolvePublishSpaceCommand;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;

/**
 * Menu bar associée aux contenus
 * 
 * Techniquement cette classe est intéressante car elle montre comment on peut
 * déployer à chaud des fonctionnalités partagées entre les portlets
 * 
 * Les fonctions du NuxeoController pourront donc etre basculées petit à petit
 * dans le CMSCustomizer
 * 
 * A PACKAGER pour la suite
 * 
 * @author jeanseb
 * 
 */
public class MenuBarFormater {

	CMSService CMSService;
	IPortalUrlFactory urlFactory;
	DefaultCMSCustomizer customizer;
	PortletContext portletCtx;


	public IPortalUrlFactory getPortalUrlFactory() throws Exception {
		if (urlFactory == null)
			urlFactory = (IPortalUrlFactory) portletCtx.getAttribute("UrlService");

		return urlFactory;
	}

	public MenuBarFormater(PortletContext portletCtx, DefaultCMSCustomizer customizer, CMSService cmsService) {
		super();
		CMSService = cmsService;
		this.portletCtx = portletCtx;
		this.customizer = customizer;

	};

	public void formatContentMenuBar(CMSServiceCtx cmsCtx) throws Exception {

		if (cmsCtx.getDoc() == null)
			return;

		PortletRequest request = cmsCtx.getRequest();

		List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("pia.menuBar");

		// Menu bar

		getPermaLinkLink(cmsCtx, menuBar);

		getContextualizationLink(cmsCtx, menuBar);

		getAdministrationLink(cmsCtx, menuBar);
	}

	
	protected void addAdministrationLinkItem(List<MenubarItem> menuBar, Document doc, String url) throws Exception {

		MenubarItem item = new MenubarItem("Editer dans Nuxeo", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2,
				url, null, "portlet-menuitem-nuxeo-edit", "nuxeo");
		item.setAjaxDisabled(true);
		menuBar.add(item);

	}

	
	protected void getAdministrationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

		if (cmsCtx.getRequest().getRemoteUser() == null)
			return;

		String savedScope = cmsCtx.getScope();

		try {
			// Scope user
			cmsCtx.setScope(null);

			Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) CMSService.executeNuxeoCommand(
					cmsCtx, new DocumentFetchLiveCommand((((Document) (cmsCtx.getDoc())).getPath()), "Write"));

			if (doc != null) {
				String url = customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/"
						+ doc.getId() + "/view_documents";
				
				addAdministrationLinkItem(menuBar, doc, url);

			}
		}

		catch (Exception e) {

			if (e instanceof CMSException) {
				CMSException ne = (CMSException) e;

				if (ne.getErrorCode() == CMSException.ERROR_FORBIDDEN
						|| ne.getErrorCode() == CMSException.ERROR_NOTFOUND) {
					// On ne fait rien : le document n'existe pas ou je n'ai pas
					// les droits
				} else
					throw e;
			}

		}

		finally {
			cmsCtx.setScope(savedScope);
		}
	}

	protected void addContextualizationLinkItem(List<MenubarItem> menuBar, Document doc, String url) throws Exception {

		MenubarItem item = new MenubarItem("Espace " + doc.getTitle(), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, url,
				null, "portlet-menuitem-contextualize", null);

		item.setAjaxDisabled(true);
		menuBar.add(item);

	}

	protected void getContextualizationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

		if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
			return;

		// Link contextualLink = ctx.getLink((((Document) (cmsCtx.getDoc()),
		// null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL);

		String url = getPortalUrlFactory().getCMSUrl(
				new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null,
				(((Document) (cmsCtx.getDoc())).getPath()), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null,
				null, null, null, null);

		if (url != null) {

			String savedScope = cmsCtx.getScope();

			try {
				// Scope user
				cmsCtx.setScope(null);

				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) CMSService.executeNuxeoCommand(
						cmsCtx, new DocumentResolvePublishSpaceCommand((((Document) (cmsCtx.getDoc())).getPath())));

				addContextualizationLinkItem(menuBar, doc, url);

			}

			finally {
				cmsCtx.setScope(savedScope);
			}
		}

		return;
	}

	
	protected void addPermaLinkItem(List<MenubarItem> menuBar,String url) throws Exception {
		MenubarItem item = new MenubarItem("Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, url, null,
				"portlet-menuitem-permalink", null);

		item.setAjaxDisabled(true);
		menuBar.add(item);

	}
	
	protected void getPermaLinkLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

		if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
			return;

		String permaLinkURL = getPortalUrlFactory().getPermaLink(
				new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null,
				null, ((Document) (cmsCtx.getDoc())).getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

		if (permaLinkURL != null) {
			addPermaLinkItem(menuBar, permaLinkURL);
		}

	}
}
