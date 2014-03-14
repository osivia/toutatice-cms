/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSObjectPath;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.context.ControllerContextAdapter;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class NavigationPictureFragmentModule implements IFragmentModule {

	private Document computePicture(NuxeoController ctx, CMSServiceCtx navCtx, String propertyName) throws CMSException {

		Document pictureContainer = null;
		boolean hasPicture = false;

		String pathToCheck = ctx.getNavigationPath();

		// On regarde dans le document courant
		CMSItem currentItem = ctx.getCMSService().getContent(navCtx, ctx.getContentPath());
		Document currentDoc = (Document) currentItem.getNativeItem();

		if (docHasPicture(currentDoc, propertyName)) {
			return currentDoc;

		} else {
			// Puis dans l'arbre de navigation
			do {

				CMSItem cmsItemNav = ctx.getCMSService().getPortalNavigationItem(navCtx, ctx.getSpacePath(), pathToCheck);
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
		cmsReadNavContext.setControllerContext(ControllerContextAdapter.getControllerContext(ctx.getPortalCtx()));
		cmsReadNavContext.setScope(ctx.getNavigationScope());

		request.setAttribute("ctx", ctx);

		String propertyName = window.getProperty("osivia.propertyName");

		if ((ctx.getNavigationPath() != null) && (propertyName != null)) {
			Document navigationPictureContainer = computePicture(ctx, cmsReadNavContext, propertyName);
			if (navigationPictureContainer != null) {
				request.setAttribute("propertyName", propertyName);
				request.setAttribute("navigationPictureContainer", computePicture(ctx, cmsReadNavContext, propertyName));
				emptyResponse = false;
			}

		}
		if (emptyResponse) {
			request.setAttribute("osivia.emptyResponse", "1");
		}

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String propertyName = window.getProperty("osivia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse response)
			throws Exception {
		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("osivia.propertyName", request.getParameter("propertyName"));
			else if (window.getProperty("osivia.propertyName") != null)
				window.setProperty("osivia.propertyName", null);
		}

	}

}
