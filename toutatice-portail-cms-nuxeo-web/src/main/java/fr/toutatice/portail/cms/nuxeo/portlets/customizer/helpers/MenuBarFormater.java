package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.List;
import java.util.Locale;

import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.contexte.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;


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

		try {

			getPermaLinkLink(cmsCtx, menuBar);

			getContextualizationLink(cmsCtx, menuBar);

			getAdministrationLink(cmsCtx, menuBar);
		} catch (CMSException e) {
			if (e.getErrorCode() == CMSException.ERROR_FORBIDDEN || e.getErrorCode() == CMSException.ERROR_NOTFOUND) {
				// On ne fait rien : le document n'existe pas ou je n'ai pas
				// les droits
			} else
				throw e;
		}
	}

	
	protected void addAdministrationLinkItem(List<MenubarItem> menuBar, String url) throws Exception {

		MenubarItem item = new MenubarItem("Editer dans Nuxeo", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2,
				url, null, "portlet-menuitem-nuxeo-edit", "nuxeo");
		item.setAjaxDisabled(true);
		menuBar.add(item);

	}

	
	protected void getAdministrationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

		if (cmsCtx.getRequest().getRemoteUser() == null)
			return;

		
		CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx, (((Document) (cmsCtx.getDoc())).getPath())  ) ;
		
		
		if( pubInfos.isEditableByUser())	{
			String url = customizer.getNuxeoConnectionProps().getPublicBaseUri().toString() + "/nxdoc/default/"
					+  pubInfos.getLiveId() + "/view_documents";
			
			addAdministrationLinkItem(menuBar,url);

		}
	}

	protected void addContextualizationLinkItem(List<MenubarItem> menuBar, String displayName, String url) throws Exception {
		
		

		MenubarItem item = new MenubarItem("Espace " + displayName, MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1, url,
				null, "portlet-menuitem-contextualize", null);

		item.setAjaxDisabled(true);
		menuBar.add(item);

	}
	
	
	
	

	/**
	 * Affiche un lien de recontextualisation explicite
	 * (dans une page existante ou une nouvelle page)
	 * 
	 * @param cmsCtx
	 * @param menuBar
	 * @throws Exception
	 */
	protected void getContextualizationLink(CMSServiceCtx cmsCtx, List<MenubarItem> menuBar) throws Exception {

		if (!WindowState.MAXIMIZED.equals(cmsCtx.getRequest().getWindowState()))
			return;

		PortalControllerContext portalCtx = new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(),
				cmsCtx.getResponse());

		Page currentPage = null;

		Window window = (Window) cmsCtx.getRequest().getAttribute("pia.window");
		if (window != null)
			currentPage = window.getPage();

		// On regarde dans quelle page le contenu ext contextualisé
		
		Page page = getPortalUrlFactory().getPortalCMSContextualizedPage(portalCtx,
				(((Document) (cmsCtx.getDoc())).getPath()));

		// Si la page correspond à la page courant on affiche pas le lien
		if (page == null || !page.getId().equals(currentPage.getId())) {

			// On détermine le nom de l'espace
			
			String spaceDisplayName = null;

			if (page != null) {
				// Soit le nom de la page
				Locale locale = Locale.FRENCH;
				spaceDisplayName = page.getDisplayName().getString(locale, true);
				if (spaceDisplayName == null)
					spaceDisplayName = page.getName();

			} else {
				// Soit le nom de l'espace de publication
				
				CMSPublicationInfos pubInfos = (CMSPublicationInfos) CMSService.getPublicationInfos(cmsCtx,
						(((Document) (cmsCtx.getDoc())).getPath()));

				if (pubInfos.getPublishSpacePath() != null) {
					if (pubInfos.isPublishSpaceInContextualization())
						spaceDisplayName = pubInfos.getPublishSpaceDisplayName();

				} else {
					if (pubInfos.getWorkspacePath() != null && pubInfos.isWorkspaceInContextualization()) {
						spaceDisplayName = pubInfos.getWorkspaceDisplayName();
					}
				}
			}

			if (spaceDisplayName != null) {
				

				String url = getPortalUrlFactory().getCMSUrl(
						new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()),
						currentPage.getId().toString(PortalObjectPath.CANONICAL_FORMAT), (((Document) (cmsCtx.getDoc())).getPath()), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL,
						null, null, null, null, null);
				
				addContextualizationLinkItem( menuBar, spaceDisplayName,  url) ;
				
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
		
//		if( "1".equals(cmsCtx.getDisplayLiveVersion()))
//			return;

		String permaLinkURL = getPortalUrlFactory().getPermaLink(
				new PortalControllerContext(cmsCtx.getPortletCtx(), cmsCtx.getRequest(), cmsCtx.getResponse()), null,
				null, ((Document) (cmsCtx.getDoc())).getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);

		if (permaLinkURL != null) {
			addPermaLinkItem(menuBar, permaLinkURL);
		}

	}
	
	
	
	
}
