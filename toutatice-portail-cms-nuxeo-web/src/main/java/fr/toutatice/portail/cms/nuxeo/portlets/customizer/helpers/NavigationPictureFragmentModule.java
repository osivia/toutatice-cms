package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;

import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSObjectPath;
import fr.toutatice.portail.core.cms.CMSServiceCtx;

public class NavigationPictureFragmentModule implements IFragmentModule {

	private Document computePicture(NuxeoController ctx, CMSServiceCtx navCtx) throws CMSException {

		Document pictureContainer = null;
		boolean hasPicture = false;
		
		String pathToCheck = ctx.getNavigationPath();
		
		CMSItem currentItem = ctx.getNuxeoCMSService().getContent(navCtx, ctx.getContentPath());
		Document currentDoc = (Document) currentItem.getNativeItem();
		CMSItem currentCmsItemNav = ctx.getNuxeoCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(),
				pathToCheck);
		Document currentNavDoc = (Document) currentCmsItemNav.getNativeItem();

		if (!currentDoc.getPath().equals(currentNavDoc.getPath())) {
			if (docHasPicture(currentDoc)) {
				return currentDoc;
			}
		} else {
			
			do {

				CMSItem cmsItemNav = ctx.getNuxeoCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(),
						pathToCheck);
				pictureContainer = (Document) cmsItemNav.getNativeItem();
				if (pictureContainer != null) {
					hasPicture = docHasPicture(pictureContainer);
				}

				// One level up
				CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
				pathToCheck = parentPath.toString();

			} while (!hasPicture && pathToCheck.contains(ctx.getSpacePath()));
		}

		return pictureContainer;
	}

	private boolean docHasPicture(Document currentDoc) {
		boolean hasPicture;
		PropertyMap picture = (PropertyMap) currentDoc.getProperties().get("wcmnvg:picture");
		hasPicture = picture != null && picture.get("data") != null;
		return hasPicture;
	}

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		// Navigation context
		CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
		cmsReadNavContext.setControllerContext(ctx.getPortalCtx().getControllerCtx());
		cmsReadNavContext.setScope(ctx.getNavigationScope());

		request.setAttribute("ctx", ctx);

		if (ctx.getNavigationPath() != null)
			request.setAttribute("navigationPictureContainer", computePicture(ctx, cmsReadNavContext));

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response)
			throws Exception {
	}

}
