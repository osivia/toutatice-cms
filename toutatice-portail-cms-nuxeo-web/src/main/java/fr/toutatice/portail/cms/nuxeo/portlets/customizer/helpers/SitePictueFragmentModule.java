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

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class SitePictueFragmentModule implements IFragmentModule {

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = null;


		nuxeoPath = window.getProperty("osivia.nuxeoPath");

		if (StringUtils.isNotEmpty(nuxeoPath)) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			request.setAttribute("nuxeoPath", nuxeoPath);
			String targetPath = window.getProperty("osivia.targetPath");

			if (targetPath != null) {
				targetPath = ctx.getComputedPath(targetPath);
				request.setAttribute("targetPath", targetPath);
			}
			
			request.setAttribute("ctx", ctx);

		}

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		request.setAttribute("nuxeoPath", nuxeoPath);

		String targetPath = window.getProperty("osivia.targetPath");
		if (targetPath == null)
			targetPath = "";
		request.setAttribute("targetPath", targetPath);

	}

	public void processAdminAttributes(NuxeoController ctx,
			PortalWindow window, ActionRequest request, ActionResponse res)
			throws Exception {

		if (request.getParameter("nuxeoPath") != null) {
			if (request.getParameter("nuxeoPath").length() > 0)
				window.setProperty("osivia.nuxeoPath",
						request.getParameter("nuxeoPath"));
			else if (window.getProperty("osivia.nuxeoPath") != null)
				window.setProperty("osivia.nuxeoPath", null);
		}

		if (request.getParameter("targetPath") != null) {
			if (request.getParameter("targetPath").length() > 0)
				window.setProperty("osivia.targetPath",
						request.getParameter("targetPath"));
			else if (window.getProperty("osivia.targetPath") != null)
				window.setProperty("osivia.targetPath", null);
		}

	}

}
