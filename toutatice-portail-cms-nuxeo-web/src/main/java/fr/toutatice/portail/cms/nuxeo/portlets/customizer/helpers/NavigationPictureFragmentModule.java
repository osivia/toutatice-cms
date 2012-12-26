package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class NavigationPictureFragmentModule implements IFragmentModule {

	private Document computePicture(NuxeoController ctx, CMSServiceCtx navCtx, String propertyName) throws CMSException {

		Document pictureContainer = null;
		boolean hasPicture = false;

		String pathToCheck = ctx.getNavigationPath();

		// On regarde dans le document courant
		CMSItem currentItem = ctx.getNuxeoCMSService().getContent(navCtx, ctx.getContentPath());
		Document currentDoc = (Document) currentItem.getNativeItem();

		if (docHasPicture(currentDoc, propertyName)) {
			return currentDoc;

		} else {
			// Puis dans l'arbre de navigation
			do {

				CMSItem cmsItemNav = ctx.getNuxeoCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(), pathToCheck);
				pictureContainer = (Document) cmsItemNav.getNativeItem();
				if (pictureContainer != null) {
					hasPicture = docHasPicture(pictureContainer, propertyName);
				}

				// One level up
				CMSObjectPath parentPath = CMSObjectPath.parse(pathToCheck).getParent();
				pathToCheck = parentPath.toString();

			} while (!hasPicture && pathToCheck.contains(ctx.getSpacePath()));
		}
		if (hasPicture)
			return pictureContainer;
		else
			return null;
	}

	private boolean docHasPicture(Document currentDoc, String propertyName) {
		boolean hasPicture;
		PropertyMap picture = (PropertyMap) currentDoc.getProperties().get(propertyName);
		hasPicture = picture != null && picture.get("data") != null;
		return hasPicture;
	}

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		boolean emptyResponse = true;

		// Navigation context
		CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
		cmsReadNavContext.setControllerContext(ctx.getPortalCtx().getControllerCtx());
		cmsReadNavContext.setScope(ctx.getNavigationScope());

		request.setAttribute("ctx", ctx);

		String propertyName = window.getProperty("pia.propertyName");

		if ((ctx.getNavigationPath() != null) && (propertyName != null)) {
			Document navigationPictureContainer = computePicture(ctx, cmsReadNavContext, propertyName);
			if (navigationPictureContainer != null) {
				request.setAttribute("propertyName", propertyName);
				request.setAttribute("navigationPictureContainer", computePicture(ctx, cmsReadNavContext, propertyName));
				emptyResponse = false;
			}

		}
		if (emptyResponse) {
			request.setAttribute("pia.emptyResponse", "1");
		}

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String propertyName = window.getProperty("pia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response)
			throws Exception {
		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("pia.propertyName", request.getParameter("propertyName"));
			else if (window.getProperty("pia.propertyName") != null)
				window.setProperty("pia.propertyName", null);
		}

	}

}
