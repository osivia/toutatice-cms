package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class PropertyFragmentModule implements IFragmentModule {

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String nuxeoPath = null;
		boolean emptyContent = true;

		nuxeoPath = window.getProperty("osivia.nuxeoPath");

		if (StringUtils.isNotEmpty(nuxeoPath)) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			Document doc = ctx.fetchDocument(nuxeoPath);

			if (doc.getTitle() != null)
				response.setTitle(doc.getTitle());

			String propertyName = window.getProperty("osivia.propertyName");

			if (StringUtils.isNotEmpty(propertyName)) {

				String content = (String) doc.getProperties().get(propertyName);

				if (content != null && content.length() > 0) {
					
					ctx.setCurrentDoc(doc);
					request.setAttribute("doc", doc);
					request.setAttribute("ctx", ctx);
					request.setAttribute("propertyName", window.getProperty("osivia.propertyName"));

					emptyContent = false;
				}
			}
		}

		if (emptyContent)
			request.setAttribute("osivia.emptyResponse", "1");

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		request.setAttribute("nuxeoPath", nuxeoPath);

		String propertyName = window.getProperty("osivia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);


		String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		request.setAttribute("displayLiveVersion", displayLiveVersion);

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res)
			throws Exception {

		if (request.getParameter("nuxeoPath") != null)
			window.setProperty("osivia.nuxeoPath", request.getParameter("nuxeoPath"));

		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("osivia.propertyName", request.getParameter("propertyName"));
			else if (window.getProperty("osivia.propertyName") != null)
				window.setProperty("osivia.propertyName", null);
		}

		if (request.getParameter("displayLiveVersion") != null) {

			if ("1".equals(request.getParameter("displayLiveVersion")))
				window.setProperty("osivia.cms.displayLiveVersion", "1");
			else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
				window.setProperty("osivia.cms.displayLiveVersion", null);
		}

	}

}
