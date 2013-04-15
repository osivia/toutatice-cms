package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyList;
import org.nuxeo.ecm.automation.client.jaxrs.model.PropertyMap;
import org.osivia.portal.api.windows.PortalWindow;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.IFragmentModule;

public class PropertyFragmentModule implements IFragmentModule {

	private static final String REF_URI = "refURI";

	public void injectViewAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = null;
		boolean emptyContent = true;

		nuxeoPath = window.getProperty("osivia.nuxeoPath");

		if (StringUtils.isNotEmpty(nuxeoPath)) {

			nuxeoPath = ctx.getComputedPath(nuxeoPath);

			Document doc = ctx.fetchDocument(nuxeoPath);

			if (doc.getTitle() != null)
				response.setTitle(doc.getTitle());

			String propertyName = window.getProperty("osivia.propertyName");
			String refURI = window.getProperty("osivia.refURI");

			if (StringUtils.isNotEmpty(propertyName)) {

				Object content = doc.getProperties().get(propertyName);
				
				// Si paramétrage de l'URI, propriétés du fragment attendues dans propertyName
				if (StringUtils.isNotEmpty(refURI)) {

					if (content instanceof PropertyList) {

						PropertyList dataContents = (PropertyList) content;

						if (dataContents != null && dataContents.size() > 0) {
							
							for(int index = 0; index < dataContents.size(); index++) {
								PropertyMap mProperty = dataContents.getMap(index);
								String refURIValue = (String) mProperty
										.get(REF_URI);
								
								if(refURI.equalsIgnoreCase(refURIValue)) {
									content = ((PropertyMap) dataContents
											.getMap(index)).getString("data");
									break;
								}
								content = "";
							}
							
//							 index = 0;
//							PropertyMap mProperty = dataContents.getMap(0);
//							String refURIValue = (String) mProperty
//									.get(REF_URI);
//							//index += 1;
//
//							while (index < dataContents.size()
//									&& !refURI.equalsIgnoreCase(refURIValue)) {
//								refURIValue = ((PropertyMap) dataContents
//										.getMap(index++)).getString(REF_URI);
//								//index++;
//							}
//							
//							// Si propriété trouvée, renseignement du contenu
//							if (index != dataContents.size()) {
//								content = ((PropertyMap) dataContents
//										.getMap(index -1)).getString("data");
//							// sinon, affichage d'un champ vide (ou "Propriété inexistante")
//							} else {
//								content = "";
//							}
						}
					}
					// Erreur si refUri renseigné et que la valeur n'est pas une propriété complexe
					else 
						content = "Paramétrage fragment incorrect";

				}
				String dataContent = (String) content;

				if (dataContent != null && dataContent.length() > 0) {

					ctx.setCurrentDoc(doc);
					request.setAttribute("doc", doc);
					request.setAttribute("ctx", ctx);
					request.setAttribute("dataContent", dataContent);

					emptyContent = false;
				}
			}
		}

		if (emptyContent)
			request.setAttribute("osivia.emptyResponse", "1");

	}

	public void injectAdminAttributes(NuxeoController ctx, PortalWindow window,
			PortletRequest request, RenderResponse response) throws Exception {

		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		request.setAttribute("nuxeoPath", nuxeoPath);

		String propertyName = window.getProperty("osivia.propertyName");
		if (propertyName == null)
			propertyName = "";
		request.setAttribute("propertyName", propertyName);

		String refURI = window.getProperty("osivia.refURI");
		if (refURI == null)
			refURI = "";
		request.setAttribute(REF_URI, refURI);

		String displayLiveVersion = window
				.getProperty("osivia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		request.setAttribute("displayLiveVersion", displayLiveVersion);

	}

	public void processAdminAttributes(NuxeoController ctx,
			PortalWindow window, ActionRequest request, ActionResponse res)
			throws Exception {

		if (request.getParameter("nuxeoPath") != null)
			window.setProperty("osivia.nuxeoPath",
					request.getParameter("nuxeoPath"));

		if (request.getParameter("propertyName") != null) {
			if (request.getParameter("propertyName").length() > 0)
				window.setProperty("osivia.propertyName",
						request.getParameter("propertyName"));
			else if (window.getProperty("osivia.propertyName") != null)
				window.setProperty("osivia.propertyName", null);

			if (request.getParameter(REF_URI) != null) {
				if (request.getParameter(REF_URI).length() > 0)
					window.setProperty("osivia.refURI",
							request.getParameter(REF_URI));
				else if (window.getProperty("osivia.refURI") != null)
					window.setProperty("osivia.refURI", null);
			}
		}

		if (request.getParameter("displayLiveVersion") != null) {

			if ("1".equals(request.getParameter("displayLiveVersion")))
				window.setProperty("osivia.cms.displayLiveVersion", "1");
			else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
				window.setProperty("osivia.cms.displayLiveVersion", null);
		}

	}

}
