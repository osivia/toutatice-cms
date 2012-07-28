package fr.toutatice.portail.cms.nuxeo.portlets.customizer;

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
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.DocumentResolvePublishSpaceCommand;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSServiceCtx;
import fr.toutatice.portail.core.cms.ICMSService;

/**
 * Menu bar associé aux contenus
 * 
 * Techniquement cette classe est intéressante car elle montre comment on peut déployer
 * à chaud des fonctionnalités partagées entre les portlets
 * 
 * Les fonctions du NuxeoController pourront donc etre basculées petit à petit dans le CMSCustomizer
 * 
 * A PACKAGER pour la suite
 * 
 * @author jeanseb
 *
 */
public class MenuBarFormater {

	CMSServiceCtx cmsCtx;
	CMSService CMSService;
	IPortalUrlFactory urlFactory;	
	DefaultCMSCustomizer customizer;

	public static String ID_PERMALINK = "PERMALINK";
	public static String ID_CONTEXTUALIZE = "CONTEXTUALIZE";
	public static String ID_EDIT_IN_NUXEO = "EDIT_IN_NUXEO";

	
	public IPortalUrlFactory getPortalUrlFactory( ) throws Exception{
		if (urlFactory == null)
			urlFactory = (IPortalUrlFactory) cmsCtx.getPortletCtx().getAttribute("UrlService");
		
		return urlFactory;
	}
	public MenuBarFormater( CMSServiceCtx cmsCtx, DefaultCMSCustomizer customizer) throws Exception {
		super();
		CMSService = (CMSService) cmsCtx.getPortletCtx().getAttribute("CMSService");
		
		
		this.cmsCtx = cmsCtx;
		this.customizer = customizer;
		
	};

	public void formatContentMenuBar() throws Exception {

		if (cmsCtx.getDoc() == null)
			return;

		PortletRequest request = cmsCtx.getRequest();

		List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("pia.menuBar");

		// Menu bar

		getPermaLinkLink(menuBar);

		getContextualizationLink(menuBar);

		getAdministrationLink(menuBar);
	}

	protected void getAdministrationLink(List<MenubarItem> menuBar) throws Exception {

		if (cmsCtx.getRequest().getRemoteUser() == null)
			return;

		String savedScope = cmsCtx.getScope();

		try {
			// Scope user
			cmsCtx.setScope(null);

			Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) CMSService
					.executeNuxeoCommand(cmsCtx, new DocumentFetchLiveCommand((((Document) (cmsCtx.getDoc())).getPath()), "Write"));

			if (doc != null) {
				String url = customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId()
						+ "/view_documents";

				MenubarItem item = new MenubarItem("Editer dans Nuxeo", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2,
						url, null, "portlet-menuitem-nuxeo-edit", "nuxeo");
				item.setIdentifier(ID_EDIT_IN_NUXEO);
				item.setAjaxDisabled(true);
				menuBar.add(item);
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

	protected void getContextualizationLink(List<MenubarItem> menuBar) throws Exception {

		if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
			return;

		//Link contextualLink = ctx.getLink((((Document) (cmsCtx.getDoc()), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL);
		
		
		String url = getPortalUrlFactory().getCMSUrl(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()),
				null, (((Document) (cmsCtx.getDoc())).getPath()), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL, null, null, null, null, null);
	
		

		if (url != null) {

			String savedScope = cmsCtx.getScope();

			try {
				// Scope user
				cmsCtx.setScope(null);

				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) CMSService
						.executeNuxeoCommand(cmsCtx, new DocumentResolvePublishSpaceCommand((((Document) (cmsCtx.getDoc())).getPath())));

				MenubarItem item = new MenubarItem("Espace "+doc.getTitle(), MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1,
						url, null, "portlet-menuitem-contextualize", null);
				item.setIdentifier(ID_CONTEXTUALIZE);
				item.setAjaxDisabled(true);
				menuBar.add(item);

			}

			finally {
				cmsCtx.setScope(savedScope);
			}
		}

		return;
	}


	protected void getPermaLinkLink(List<MenubarItem> menuBar) throws Exception {

		if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
			return;

		
		String permaLinkURL = getPortalUrlFactory().getPermaLink(new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null, null,
				((Document) (cmsCtx.getDoc())).getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

		if (permaLinkURL != null) {
			MenubarItem item = new MenubarItem("Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, permaLinkURL, null,
					"portlet-menuitem-permalink", null);
			item.setIdentifier(ID_PERMALINK);
			item.setAjaxDisabled(true);
			menuBar.add(item);
		}

	}
}
