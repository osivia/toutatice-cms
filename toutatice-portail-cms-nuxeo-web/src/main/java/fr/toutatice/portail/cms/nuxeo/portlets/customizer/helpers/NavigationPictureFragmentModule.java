package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.jaxrs.model.Document;

import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;
import fr.toutatice.portail.core.cms.CMSException;
import fr.toutatice.portail.core.cms.CMSItem;
import fr.toutatice.portail.core.cms.CMSObjectPath;
import fr.toutatice.portail.core.cms.CMSServiceCtx;

public class NavigationPictureFragmentModule implements IFragmentModule {

	private String computePageTemplate(NuxeoController ctx, CMSServiceCtx navCtx) throws CMSException {

		String pathToCheck = ctx.getNavigationPath();
		String pageTemplate = null;

		do {

			CMSItem cmsItemNav = ctx.getNuxeoCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(), pathToCheck);

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

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		// Navigation context
		CMSServiceCtx cmsReadNavContext = new CMSServiceCtx();
		cmsReadNavContext.setControllerContext(ctx.getPortalCtx().getControllerCtx());
		cmsReadNavContext.setScope(ctx.getNavigationScope());

		request.setAttribute("ctx", ctx);

		if (ctx.getNavigationPath() != null)
			request.setAttribute("navigationPageTemplate", computePageTemplate(ctx, cmsReadNavContext));

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response)
			throws Exception {
	}

}
