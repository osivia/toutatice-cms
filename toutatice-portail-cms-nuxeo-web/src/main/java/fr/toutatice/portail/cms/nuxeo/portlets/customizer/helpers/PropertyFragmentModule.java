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

public class PropertyFragmentModule implements IFragmentModule {

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String nuxeoPath = null;
		boolean emptyContent = true;

		nuxeoPath = window.getProperty("pia.nuxeoPath");

		if (nuxeoPath != null) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			Document doc = ctx.fetchDocument(nuxeoPath);

			if (doc.getTitle() != null)
				response.setTitle(doc.getTitle());

			String propertyName = window.getProperty("pia.propertyName");

			if (propertyName != null) {

				String content = (String) doc.getProperties().get(propertyName);

				if (content != null && content.length() > 0) {
					
					ctx.setCurrentDoc(doc);
					request.setAttribute("doc", doc);
					request.setAttribute("ctx", ctx);
					request.setAttribute("propertyName", window.getProperty("pia.propertyName"));

					emptyContent = false;
				}
			}
		}

		if (emptyContent)
			request.setAttribute("pia.emptyResponse", "1");

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window, PortletRequest request, RenderResponse response)
			throws Exception {

		String nuxeoPath = window.getProperty("pia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		request.setAttribute("nuxeoPath", nuxeoPath);

		String propertyName = window.getProperty("pia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);

		String scope = window.getProperty("pia.cms.scope");
		request.setAttribute("scope", scope);

		String displayLiveVersion = window.getProperty("pia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		request.setAttribute("displayLiveVersion", displayLiveVersion);

	}

	public void processAdminAttributes(NuxeoController ctx, PortalWindow window, ActionRequest request, ActionResponse res)
			throws Exception {

		if (request.getParameter("nuxeoPath") != null)
			window.setProperty("pia.nuxeoPath", request.getParameter("nuxeoPath"));

		if (request.getParameter("scope") != null) {
			if (request.getParameter("scope").length() > 0)
				window.setProperty("pia.cms.scope", request.getParameter("scope"));
			else if (window.getProperty("pia.cms.scope") != null)
				window.setProperty("pia.cms.scope", null);
		}

		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("pia.propertyName", request.getParameter("propertyName"));
			else if (window.getProperty("pia.propertyName") != null)
				window.setProperty("pia.propertyName", null);
		}

		if (request.getParameter("displayLiveVersion") != null) {

			if ("1".equals(request.getParameter("displayLiveVersion")))
				window.setProperty("pia.cms.displayLiveVersion", "1");
			else if (window.getProperty("pia.cms.displayLiveVersion") != null)
				window.setProperty("pia.cms.displayLiveVersion", null);
		}

	}

}
