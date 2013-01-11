package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
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

import org.nuxeo.ecm.automation.client.jaxrs.Constants;
import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.path.PortletPathItem;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.DocumentFetchCommand;
import fr.toutatice.portail.cms.nuxeo.core.NuxeoQueryFilter;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.jbossportal.NuxeoCommandContext;


import fr.toutatice.portail.cms.nuxeo.portlets.commands.FolderGetChildrenCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.FolderGetParentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.list.ListCommand;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class FileBrowserPortlet extends CMSPortlet {
	
	public static final String DISPLAY_MODE_NORMAL = "normal";
	public static final String DISPLAY_MODE_DETAILED = "detailed";	
	
	public static boolean isNavigable( Document doc)	{
		
		return "Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType()) || "Workspace".equals(doc.getType()) || "WorkspaceRoot".equals(doc.getType())|| "PortalSite".equals(doc.getType()) || "Section".equals(doc.getType()) || "SectionRoot".equals(doc.getType());

	}
	
	public static boolean isOrdered (Document doc)	{
		
		return "Workspace".equals(doc.getType()) || "WorkspaceRoot".equals(doc.getType()) || "OrderedFolder".equals(doc.getType()) || "PortalSite".equals(doc.getType()) ;

	}

	public static Comparator<Document> createComparator( final Document parentDoc){
	
	 Comparator<Document> comparator = new Comparator<Document>() {
		
		public int compare(Document e1, Document e2) {
			
			if( isOrdered(parentDoc))	{
				
				long pos1 = e1.getProperties().getLong("ecm:pos", new Long(1000));
				long pos2 = e1.getProperties().getLong("ecm:pos", new Long(1000));		
				
				return pos1 > pos2 ? 1 : -1;
				
			}
			else	{
				return e1.getTitle().toUpperCase().compareTo(e2.getTitle().toUpperCase());
			}
		}
	 };
		
		return comparator;
	};

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));
			
			if ("1".equals(req.getParameter("displayLiveVersion")))
				window.setProperty("osivia.cms.displayLiveVersion", "1");
			else if (window.getProperty("osivia.cms.displayLiveVersion") != null)
				window.setProperty("osivia.cms.displayLiveVersion", null);	

			

			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("osivia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("osivia.cms.scope") != null)
				window.setProperty("osivia.cms.scope", null);

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}
	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");

		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());
		
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);

		String nuxeoPath = window.getProperty("osivia.nuxeoPath");
		if (nuxeoPath == null)
			nuxeoPath = "";
		req.setAttribute("nuxeoPath", nuxeoPath);
		
		
		String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
		if (displayLiveVersion == null)
			displayLiveVersion = "";
		req.setAttribute("displayLiveVersion", displayLiveVersion);


	
		String scope = window.getProperty("osivia.cms.scope");
		req.setAttribute("scope", scope);
		
		req.setAttribute("ctx", ctx);


		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/admin.jsp");
		rd.include(req, res);

	}
	
	
	
	private void addPathItem(List<PortletPathItem> portletPath, Document curDoc, String displayMode)	{
		Map<String, String> renderParams = new Hashtable<String, String>();
		renderParams.put("folderPath", curDoc.getPath());
		
		if( displayMode != null)
			renderParams.put("displayMode", displayMode);
		
		PortletPathItem pathItem = new PortletPathItem(renderParams, curDoc.getTitle());
		
		portletPath.add(0, pathItem);
	}
	
	

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);
			

			/* On détermine l'uid et le scope */

			String nuxeoPath = null;

			// portal window parameter (appels dynamiques depuis le portail)
			nuxeoPath = window.getProperty("osivia.cms.uri");

			// logger.debug("doView "+ uid);
			if (nuxeoPath == null) {
				nuxeoPath = window.getProperty("osivia.nuxeoPath");
			}
			
			String displayMode = request.getParameter("displayMode");



			if (nuxeoPath != null) {
				NuxeoController ctx = new NuxeoController(request, response, getPortletContext());
				
				
				nuxeoPath = ctx.getComputedPath(nuxeoPath);
				
				
				String folderPath = nuxeoPath;

				if (request.getParameter("folderPath") != null) {
					folderPath = request.getParameter("folderPath");
				}
			
				

				
				/* Folder courant */
				
				Document doc = (org.nuxeo.ecm.automation.client.jaxrs.model.Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(folderPath));


				//Document doc = (Document) ctx.executeNuxeoCommand(new DocumentFetchCommand(folderPath));

				/* Récupération des fils */
				
				
				CMSPublicationInfos pubInfos = ctx.getNuxeoCMSService().getPublicationInfos(ctx.getCMSCtx(), folderPath);
				
				Documents docs = (Documents) ctx.executeNuxeoCommand(new FolderGetFilesCommand(pubInfos.getDocumentPath(), pubInfos.getLiveId(), ctx.isDisplayingLiveVersion()));
				
				// Tri pour affichage
				List<Document> sortedDocs = (ArrayList<Document>) docs.clone();
				Collections.sort(sortedDocs, createComparator( doc));
				request.setAttribute("docs", sortedDocs);

				/* Récupération des parents (pour le path) */

				NuxeoController ctxSession = new NuxeoController(request, response, getPortletContext());
				ctxSession.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
				ctxSession.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_SESSION);

				List<PortletPathItem> portletPath = new ArrayList<PortletPathItem>();

				Document curDoc = doc;
				
				while (! curDoc.getPath().equals(nuxeoPath) && curDoc.getPath().startsWith(nuxeoPath)) {
					addPathItem(portletPath,curDoc,  displayMode);
			
					curDoc = (Document) ctxSession.executeNuxeoCommand(new FolderGetParentCommand(curDoc));
				}
				
				if( curDoc.getPath().equals(nuxeoPath))
					addPathItem(portletPath,curDoc,  displayMode);
				
				
				//Injection du path vers le portail
				request.setAttribute("osivia.portletPath", portletPath);
				//response.setProperty("osivia.emptyResponse", "1");
				
				/* attributs de la JSP */
				
				request.setAttribute("portletPath", portletPath);
				
				request.setAttribute("basePath", nuxeoPath);
				request.setAttribute("folderPath", folderPath);
				
				// Pas d'affichage détaillé en mode normal
				if(DISPLAY_MODE_DETAILED.equals(displayMode) && !WindowState.MAXIMIZED.equals(request.getWindowState()))
						displayMode = DISPLAY_MODE_NORMAL;
				
				request.setAttribute("displayMode", displayMode);


				request.setAttribute("ctx", ctx);
				
				if(  DISPLAY_MODE_DETAILED.equals( displayMode) )	
					getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view-detailed.jsp").include(request, response);
				else
					getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view-normal.jsp").include(request, response);
					
			} else {
				response.setContentType("text/html");
				response.getWriter().print("<h2>Document non défini</h2>");
				response.getWriter().close();
				return;
			}

		} catch( NuxeoException e){
			PortletErrorHandler.handleGenericErrors(response, e);
		}

		catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}

}
