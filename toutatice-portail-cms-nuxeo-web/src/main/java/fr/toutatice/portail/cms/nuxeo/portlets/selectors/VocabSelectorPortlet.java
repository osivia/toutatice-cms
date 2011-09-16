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
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyIdentifier;
import fr.toutatice.portail.cms.nuxeo.vocabulary.VocabularyLoaderCommand;

/**
 * Portlet de selection de liste par vocabulaire
 */

public class VocabSelectorPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(VocabSelectorPortlet.class);

	public static String DELETE_PREFIX = "delete_";

	/**
	 * Permet d'exprimer le label d'un composant sur plusieurs niveaux : cle1/cle2/cle3
	 * 
	 * @param label
	 * @param id
	 * @param vocab
	 * @return
	 */
	public static String getLabel(String label, String id, VocabularyEntry vocab)  {	
		String[] tokens = id.split("/", 2);
		
		String res = "";

		if( tokens.length > 0)	{
			VocabularyEntry child = vocab.getChild(tokens[ 0]);
			res += child.getLabel();
		}
		
		if( tokens.length > 1)	{
			VocabularyEntry childVocab = vocab.getChild(tokens[ 0]);
			if( childVocab != null)
				res += "/" + getLabel( res, tokens[ 1], childVocab);
		}

		
		return res;
	}
	
	
	

	
	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		PortalWindow window = WindowFactory.getWindow(req);

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {
			
			if( req.getParameter("selectorId").length() > 0)
				window.setProperty("pia.selectorId", req.getParameter("selectorId"));
			else if (window.getProperty("pia.selectorId") != null)
				window.setProperty("pia.selectorId", null);	
			
			if( req.getParameter("vocabName1").length() > 0)
				window.setProperty("pia.vocabName1", req.getParameter("vocabName1"));
			else if (window.getProperty("pia.vocabName1") != null)
				window.setProperty("pia.vocabName1", null);				

			if( req.getParameter("vocabName2").length() > 0)
				window.setProperty("pia.vocabName2", req.getParameter("vocabName2"));
			else if (window.getProperty("pia.vocabName2") != null)
				window.setProperty("pia.vocabName2", null);				
			

			if( req.getParameter("vocabName3").length() > 0)
				window.setProperty("pia.vocabName3", req.getParameter("vocabName3"));
			else if (window.getProperty("pia.vocabName3") != null)
				window.setProperty("pia.vocabName3", null);				

			
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
					
					String selectedVocabId = req.getParameter("vocab1Id");
					
					if (req.getParameter("vocab2Id") != null && req.getParameter("vocab2Id").length() > 0) {
						selectedVocabId += "/" + req.getParameter("vocab2Id");
					}
					
					if (req.getParameter("vocab3Id") != null && req.getParameter("vocab3Id").length() > 0) {
						selectedVocabId += "/" + req.getParameter("vocab3Id");
					}
					
					

					List<String> vocabIds = selectors.get(selectorId);
					if (vocabIds == null) {
						vocabIds = new ArrayList<String>();
						selectors.put(selectorId, vocabIds);
					}
					
					vocabIds.add(selectedVocabId);
				}
				

				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				
				String vocab1Id = req.getParameter("vocab1Id");
				if( vocab1Id != null)
					res.setRenderParameter("vocab1Id", vocab1Id);
				
				String vocab2Id = req.getParameter("vocab2Id");
				if( vocab2Id != null)
					res.setRenderParameter("vocab2Id", vocab2Id);

				String vocab3Id = req.getParameter("vocab3Id");
				if( vocab3Id != null)
					res.setRenderParameter("vocab3Id", vocab3Id);

				

				// Réinitialisation des fenetres en mode NORMAL
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

				// Réinitialisation des fenetres en mode NORMAL
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
		

		String vocabName2 = window.getProperty("pia.vocabName2");
		if (vocabName2 == null)
			vocabName2 = "";
		req.setAttribute("vocabName2", vocabName2);
		
		String vocabName3 = window.getProperty("pia.vocabName3");
		if (vocabName3 == null)
			vocabName3 = "";
		req.setAttribute("vocabName3", vocabName3);
		
		

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
			
			String vocabsName = vocabName1;
			

			String vocabName2 = window.getProperty("pia.vocabName2");
			if( vocabName2 != null){
				vocabsName += ";" + vocabName2;
			}
			
			String vocabName3 = window.getProperty("pia.vocabName3");
			if( vocabName3 != null){
				vocabsName += ";" + vocabName3;
			}
			
			

			String vocab1Id = request.getParameter("vocab1Id");
			String vocab2Id = request.getParameter("vocab2Id");
			String vocab3Id = request.getParameter("vocab3Id");			
		
			

			// Get public parameter
			Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
			if (selectors.get(selectorId) != null)
				request.setAttribute("vocabsId", selectors.get(selectorId));
			else
				request.setAttribute("vocabsId", new ArrayList<String>());
			
			

			request.setAttribute("vocab1Id", vocab1Id);
			request.setAttribute("vocab2Id", vocab2Id);
			request.setAttribute("vocab3Id", vocab3Id);
			
			request.setAttribute("vocabName2", vocabName2);
			request.setAttribute("vocabName3", vocabName3);			


			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
			
			ctx.setScopeType(NuxeoCommandContext.SCOPE_TYPE_SUPERUSER);

			// rafraichir en asynchrone
			ctx.setAsynchronousUpdates(true);

			VocabularyIdentifier vocabIdentifier = new VocabularyIdentifier( vocabsName, vocabsName);

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
