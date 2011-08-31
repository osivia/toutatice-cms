package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyIdentifier;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyLoaderCommand;

/**
 * Portlet de selection de liste par vocabulaire
 */

public class VocabSelectorPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(VocabSelectorPortlet.class);

	public static String DELETE_PREFIX = "delete_";

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		PortalWindow window = WindowFactory.getWindow(req);

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			window.setProperty("pia.selectorId", req.getParameter("selectorId"));
			window.setProperty("pia.vocabName1", req.getParameter("vocabName1"));

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("view".equals(req.getPortletMode().toString()) && req.getParameter("add") != null) {

			// Set public parameter
			String selectorId = window.getProperty("pia.selectorId");
			if (selectorId != null) {

				Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));

				if (req.getParameter("vocab1Id") != null && req.getParameter("vocab1Id").length() > 0) {

					List<String> vocabIds = selectors.get(selectorId);
					if (vocabIds == null) {
						vocabIds = new ArrayList<String>();
						selectors.put(selectorId, vocabIds);
					}
					vocabIds.add(req.getParameter("vocab1Id"));

				}

				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

				// SYnchronisation des parametres public en mode Ajax
				req.setAttribute("pia.ajaxSynchronisation", "true");

				// Réinitialisation des fenetres en mode NORMAL
				// Permet de s'assurer qu'une fenetre MAX ne masque pas les
				// listes de résultats
				req.setAttribute("pia.initPageState", "true");

			}

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Delete
		if ("view".equals(req.getPortletMode().toString()) && "delete".equals(req.getParameter("action"))) {
			int occ = new Integer(req.getParameter("occ"));

			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			String selectorId = window.getProperty("pia.selectorId");

			List<String> vocabIds = selectors.get(selectorId);
			if (vocabIds != null && vocabIds.size() > occ) {

				vocabIds.remove(occ);
				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

				// SYnchronisation des parametres public en mode Ajax
				req.setAttribute("pia.ajaxSynchronisation", "true");

				req.setAttribute("pia.initPageState", "true");

			}
		}

	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);

		String selectorId = window.getProperty("pia.selectorId");
		if (selectorId == null)
			selectorId = "";
		req.setAttribute("selectorId", selectorId);

		String vocabName1 = window.getProperty("pia.vocabName1");
		if (vocabName1 == null)
			vocabName1 = "";
		req.setAttribute("vocabName1", vocabName1);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/vocab/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			String selectorId = window.getProperty("pia.selectorId");
			if (selectorId == null) {
				response.getWriter().print("<h2>Identifiant non défini</h2>");
				response.getWriter().close();
				return;

			}

			String vocabName1 = window.getProperty("pia.vocabName1");
			if (vocabName1 == null) {
				response.getWriter().print("<h2>Vocabulaire non défini</h2>");
				response.getWriter().close();
				return;

			}

			String vocab1Id = request.getParameter("vocab1Id");

			// Get public parameter

			Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

			if (selectors.get(selectorId) != null)
				request.setAttribute("vocabsId", selectors.get(selectorId));
			else
				request.setAttribute("vocabsId", new ArrayList<String>());

			request.setAttribute("vocab1Id", vocab1Id);

			// TODO : droits d'acces en admin
			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

			// rafraichir en asynchrone
			ctx.setAsynchronousUpdates(true);

			VocabularyIdentifier vocabIdentifier = new VocabularyIdentifier(vocabName1, vocabName1);

			VocabularyEntry vocab = (VocabularyEntry) ctx.executeNuxeoCommand(new VocabularyLoaderCommand(
					vocabIdentifier));
			request.setAttribute("vocab1", vocab);

			getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/vocab/view.jsp")
					.include(request, response);

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		}

		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");
	}

}
