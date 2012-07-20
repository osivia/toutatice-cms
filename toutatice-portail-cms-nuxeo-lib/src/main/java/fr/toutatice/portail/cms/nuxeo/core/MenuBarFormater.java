package fr.toutatice.portail.cms.nuxeo.core;

import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.WindowState;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.menubar.MenubarItem;
import fr.toutatice.portail.api.urls.IPortalUrlFactory;
import fr.toutatice.portail.api.urls.Link;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;

public class MenuBarFormater {

	NuxeoController ctx;
	
	public static String ID_PERMALINK = "PERMALINK";
	public static String ID_CONTEXTUALIZE = "CONTEXTUALIZE";
	public static String ID_EDIT_IN_NUXEO = "EDIT_IN_NUXEO";	

	
	public MenuBarFormater(NuxeoController ctx) {
		super();
		this.ctx = ctx;
	};

	public void formatContentMenuBar()	throws Exception {
		
		if (ctx.getCurrentDoc() == null)
			return;

		
		PortletRequest request = ctx.getRequest();
		
		List<MenubarItem> menuBar = (List<MenubarItem>) request.getAttribute("pia.menuBar");

		// Menu bar

		Link permaLinkURL = getPermaLinkLink();

		if( permaLinkURL != null)	{
				MenubarItem item = new MenubarItem("Permalink", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS, permaLinkURL.getUrl(), null, "portlet-menuitem-permalink", null);
			    item.setIdentifier(ID_PERMALINK);
			    item.setAjaxDisabled(true);
			    menuBar.add(item);
		}


		Link contextualLink = getContextualizationLink();
		if( contextualLink != null)	{

			    MenubarItem item = new MenubarItem("Contextualiser", MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 1,  contextualLink.getUrl(), null, "portlet-menuitem-contextualize", null);
			    item.setIdentifier(ID_CONTEXTUALIZE);
			    item.setAjaxDisabled(true);
			    menuBar.add(item);			    
				


		} 

		Link adminLink = getAdministrationLink();
		if( adminLink != null)	{
				MenubarItem item = new MenubarItem("Editer dans Nuxeo",  MenubarItem.ORDER_PORTLET_SPECIFIC_CMS + 2,  adminLink.getUrl(), null, "portlet-menuitem-nuxeo-edit", "nuxeo");
			    item.setIdentifier(ID_EDIT_IN_NUXEO);
			    item.setAjaxDisabled(true);
			    menuBar.add(item);				    
				
		} 


	}
	
	

	protected Link getAdministrationLink() throws Exception {

		if (ctx.getRequest().getRemoteUser() == null)
			return null;


		String savedScope = ctx.getScope();

		try {
			// Scope user
			ctx.setScope(null);

			Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx
					.executeNuxeoCommand(new DocumentFetchLiveCommand(ctx.getCurrentDoc().getPath(), "Write"));

			if (doc != null) {
				return new Link(ctx.getNuxeoPublicBaseUri().toString() + "/nxdoc/default/" + doc.getId()
						+ "/view_documents", true);
			}
		}

		catch (Exception e) {

			if (e instanceof NuxeoException) {
				NuxeoException ne = (NuxeoException) e;

				if (ne.getErrorCode() == NuxeoException.ERROR_FORBIDDEN
						|| ne.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
					// On ne fait rien : le document n'existe pas ou je n'ai pas
					// les droits
				} else
					throw e;
			}

		}

		finally {
			ctx.setScope(savedScope);
		}

		return null;

	}

	protected Link getContextualizationLink() throws Exception {

		if( !WindowState.MAXIMIZED.equals(ctx.getRequest().getWindowState()))
			return null;
		
		Link contextualLink = ctx.getLink(ctx.getCurrentDoc(), null, IPortalUrlFactory.CONTEXTUALIZATION_PORTAL);
		return contextualLink;
	}

	protected Link getPermaLinkLink() throws Exception {


		if( !WindowState.MAXIMIZED.equals(ctx.getRequest().getWindowState()))
			return null;
		
		
		String permaLinkURL = ctx.getPortalUrlFactory().getPermaLink(ctx.getPortalCtx(), null, null,
				ctx.getCurrentDoc().getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);
		
		if( permaLinkURL != null)
		
			return new Link(permaLinkURL, false);
		else return null;

	}
}
