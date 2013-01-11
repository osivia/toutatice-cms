package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.profils.ProfilBean;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.core.WysiwygParser;
import fr.toutatice.portail.cms.nuxeo.core.XSLFunctions;


/**
 * Portlet de selection de liste par mot cle
 */

public class KeywordsSelectorPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(KeywordsSelectorPortlet.class);

	public static String DELETE_PREFIX = "delete_";

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		PortalWindow window = WindowFactory.getWindow(req);

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			if( req.getParameter("selectorId").length() > 0)
				window.setProperty("osivia.selectorId", req.getParameter("selectorId"));
			else if (window.getProperty("osivia.selectorId") != null)
				window.setProperty("osivia.selectorId", null);	

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Pour supporter le mode Ajax, il faut également test le add sans l'extension '.x'
		if ("view".equals(req.getPortletMode().toString())  && (req.getParameter("add.x") != null || req.getParameter("add") != null)) {

			// Set public parameter
			String selectorId = window.getProperty("osivia.selectorId");
			if (selectorId != null) {

				Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));

				if (req.getParameter("keyword") != null && req.getParameter("keyword").length() > 0) {

					List<String> keywords = selectors.get(selectorId);
					if (keywords == null) {
						keywords = new ArrayList<String>();
						selectors.put(selectorId, keywords);
					}
					keywords.add(req.getParameter("keyword"));

				}

				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				
				//Réinitialisation des fenetres en mode NORMAL
				req.setAttribute("osivia.unsetMaxMode", "true");
				
			}

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Delete
		if ("view".equals(req.getPortletMode().toString()) && "delete".equals(req.getParameter("action")) ) {
			int occ = new Integer(req.getParameter("occ"));

			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			String selectorId = window.getProperty("osivia.selectorId");

			List<String> keywords = selectors.get(selectorId);
			if (keywords != null && keywords.size() > occ) {

				keywords.remove(occ);
				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				
				//Réinitialisation des fenetres en mode NORMAL
				req.setAttribute("osivia.initPageState", "true");
			}
		}

	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);

		String selectorId = window.getProperty("osivia.selectorId");
		if (selectorId == null)
			selectorId = "";
		req.setAttribute("selectorId", selectorId);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			String selectorId = window.getProperty("osivia.selectorId");
			String keyword = request.getParameter("keyword");

			if (selectorId != null) {
				// Get public parameter

				Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

				if (selectors.get(selectorId) != null)
					request.setAttribute("keywords", selectors.get(selectorId));
				else
					request.setAttribute("keywords", new ArrayList<String>());

				request.setAttribute("keyword", keyword);

	
				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/view.jsp").include(request,
						response);

			} else {
				response.getWriter().print("<h2>Identifiant non défini</h2>");
				response.getWriter().close();
			}

		}
			catch (NuxeoException e) {
				PortletErrorHandler.handleGenericErrors(response, e);
			}

		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");
	}

}
