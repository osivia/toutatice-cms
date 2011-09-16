package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.jaxrs.model.PaginableDocuments;

import bsh.EvalError;
import bsh.Interpreter;

import fr.toutatice.portail.api.contexte.PortalControllerContext;
import fr.toutatice.portail.api.statut.IStatutService;
import fr.toutatice.portail.api.windows.PortalWindow;
import fr.toutatice.portail.api.windows.WindowFactory;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.core.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.TransformationContext;
import fr.toutatice.portail.core.profils.ProfilBean;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewListPortlet extends CMSPortlet  {

	private static Log logger = LogFactory.getLog(ViewListPortlet.class);
	
	public static final String STYLE_MINI = "mini";
	public static final String STYLE_NORMAL = "normal";
	public static final String STYLE_DETAILED = "detailed";
	public static final String STYLE_NEWS = "news";
	public static final String STYLE_EDITORIAL = "editorial";

	
	
	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);
			window.setProperty("pia.nuxeoRequest", req.getParameter("nuxeoRequest"));
			
			
			if ("1".equals(req.getParameter("beanShell")))
				window.setProperty("pia.requestInterpretor", "beanShell");
			else if (window.getProperty("pia.requestInterpretor") != null)
				window.setProperty("pia.requestInterpretor", null);	

			
			if ("1".equals(req.getParameter("displayNuxeoRequest")))
				window.setProperty("pia.displayNuxeoRequest", "1");
			else if (window.getProperty("pia.displayNuxeoRequest") != null)
				window.setProperty("pia.displayNuxeoRequest", null);	


			if (req.getParameter("scope") != null && req.getParameter("scope").length() > 0)
				window.setProperty("pia.cms.scope", req.getParameter("scope"));
			else if (window.getProperty("pia.cms.scope") != null)
				window.setProperty("pia.cms.scope", null);
			
			if (req.getParameter("style") != null && req.getParameter("style").length() > 0)
				window.setProperty("pia.cms.style", req.getParameter("style"));
			else if (window.getProperty("pia.cms.style") != null)
				window.setProperty("pia.cms.style", null);
			
			// Taille de page
			int pageSize = 0;
			if (req.getParameter("pageSize") != null )	{
				try	{
					pageSize = Integer.parseInt(req.getParameter("pageSize"));
				} catch(Exception e){
					// Mal formatté
				}
			}
			
			if (pageSize > 0)
				window.setProperty("pia.cms.pageSize", Integer.toString(pageSize));
			else if (window.getProperty("pia.cms.pageSize") != null)
				window.setProperty("pia.cms.pageSize", null);
	
				
			//Limite
			int maxItems = 0;
			if (req.getParameter("maxItems") != null )	{
				try	{
					maxItems = Integer.parseInt(req.getParameter("maxItems"));
				} catch(Exception e){
					// Mal formatté
				}
			}
			
			if (maxItems > 0)
				window.setProperty("pia.cms.maxItems", Integer.toString(maxItems));
			else if (window.getProperty("pia.cms.maxItems") != null)
				window.setProperty("pia.cms.maxItems", null);
			
			if (req.getParameter("permaLinkRef") != null && req.getParameter("permaLinkRef").length() > 0)
				window.setProperty("pia.permaLinkRef", req.getParameter("permaLinkRef"));
			else if (window.getProperty("pia.permaLinkRef") != null)
				window.setProperty("pia.permaLinkRef", null);
			


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
		
		TransformationContext ctx = new TransformationContext(req, res, getPortletContext());
		
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);
		String nuxeoRequest = window.getProperty("pia.nuxeoRequest");
		if (nuxeoRequest == null)
			nuxeoRequest = "";
		req.setAttribute("nuxeoRequest", nuxeoRequest);

		String beanShell = "";
		String interpretor = window.getProperty("pia.requestInterpretor");
		if( "beanShell".equals( interpretor))
			beanShell = "1";
		req.setAttribute("beanShell", beanShell);

		
		req.setAttribute("displayNuxeoRequest", window.getProperty("pia.displayNuxeoRequest"));


		String scope = window.getProperty("pia.cms.scope");
		req.setAttribute("scope", scope);
		
		/* Styles d'affichage */
		
		Map<String, String> styles = new LinkedHashMap<String, String>();
		styles.put(STYLE_MINI, "Minimal [titre]");
		styles.put(STYLE_NORMAL, "Normal [titre, icône]");
		styles.put(STYLE_DETAILED, "Détaillé [description, date, ...]");
		styles.put(STYLE_NEWS, "Brève [date]");
		styles.put(STYLE_EDITORIAL, "Editorial [vignette, description]");
		
		req.setAttribute("styles", styles);
	
		String style = window.getProperty("pia.cms.style");
		if( style == null)
			style = STYLE_NORMAL;
		req.setAttribute("style", style);
		
		String pageSize = window.getProperty("pia.cms.pageSize");
		req.setAttribute("pageSize", pageSize);
		
		String maxItems = window.getProperty("pia.cms.maxItems");
		req.setAttribute("maxItems", maxItems);
		
		String permaLinkRef = window.getProperty("pia.permaLinkRef");
		if (permaLinkRef == null )
			permaLinkRef = "";
		req.setAttribute("permaLinkRef", permaLinkRef);
		
	
		req.setAttribute("ctx", ctx);
		
		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/admin.jsp");
		rd.include(req, res);

	}

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {
	

		logger.debug("doView");

		try {

			response.setContentType("text/html");

	
			/* On détermine l'uid et le scope */

			String nuxeoRequest = null;

			PortalWindow window = WindowFactory.getWindow(request);
			

				
			nuxeoRequest = window.getProperty("pia.nuxeoRequest");
			
			if ("beanShell".equals(window.getProperty("pia.requestInterpretor")))	{
				// Evaluation beanshell
				Interpreter i = new Interpreter();
				i.set("params", PageSelectors.decodeProperties(request.getParameter("selectors")));
				i.set("NXQLFormater", new NXQLFormater());
				

				nuxeoRequest = (String)  i.eval( nuxeoRequest);
			}
				
			int maxItems = -1;
			if (window.getProperty("pia.cms.maxItems") != null)
				maxItems = Integer.parseInt(window.getProperty("pia.cms.maxItems"));
			
			int pageSize = -1;
			int currentPage = 0;
			if (window.getProperty("pia.cms.pageSize") != null )		{
				pageSize = Integer.parseInt(window.getProperty("pia.cms.pageSize"));
				String sCurrentPage =  request.getParameter("currentPage");
				if( sCurrentPage != null)
					currentPage = Integer.parseInt(sCurrentPage);
			}
			
			/* Reinitialisation du numéro de page si changement de critères */
			
			String selectors = request.getParameter("selectors");
			String lastSelectors = request.getParameter("lastSelectors");
			
			if( lastSelectors != null && !lastSelectors.equals(selectors))
				currentPage = 0;
			


			if (nuxeoRequest != null) {

					// TODO : Gestion d'un cache global
						TransformationContext ctx = new TransformationContext(request, response, getPortletContext());
						ctx.setScope(window.getProperty("pia.cms.scope"));
						
						// Calcul de la taille de la page
						
						int requestPageSize = 100;
						if( pageSize != -1)	
							requestPageSize = pageSize;
						
						// Pas d'autre solution que de servir de la pagination pour limiter le nombre d'items ...
						// limite de la taille de la page par le nombre d'items (sur la première page)
						if( maxItems != -1 && currentPage == 0)	{
							requestPageSize = Math.min( requestPageSize, maxItems);
						}

						
						PaginableDocuments docs = (PaginableDocuments) ctx.executeNuxeoCommand(new ListCommand(nuxeoRequest,currentPage, requestPageSize));

	
						
						int nbPages = 0;
					
						if( pageSize != -1)	{
							if( maxItems == -1)	
								nbPages = ((docs.getTotalSize() - 1) / pageSize ) + 1;
							else	{
								nbPages = ((Math.min( docs.getTotalSize(), maxItems)  - 1) / pageSize ) + 1;
								
								// Attention à la dernière page ...
								// Elle peut contenir trop d'éléments ...
								
								if( docs.size() < (currentPage + 1) * requestPageSize )	{
									
									int pageLimit  = Math.max(0, maxItems - currentPage  * requestPageSize);
									
									while (docs.size() > pageLimit)
										docs.remove(docs.size() - 1);
								}
		
							}
						}
						
						request.setAttribute("docs", docs);
						request.setAttribute("ctx", ctx);

						request.setAttribute("currentPage", currentPage);						
						
						request.setAttribute("nbPages", nbPages );
						
						if( "1".equals(window.getProperty("pia.displayNuxeoRequest")))
								request.setAttribute("nuxeoRequest", nuxeoRequest );
						
						
						request.setAttribute("selectors", request.getParameter("selectors") );

						
						String style = window.getProperty("pia.cms.style");
						if( style == null)
							style = STYLE_NORMAL;
						request.setAttribute("style", style);
						
						
						String permaLinkRef = window.getProperty("pia.permaLinkRef");
						if( permaLinkRef != null)	{
							Map<String, String> publicParams = new HashMap<String, String>();
							if( selectors != null)
								publicParams.put("selectors", selectors);
								String permaLinkURL = ctx.getPortalUrlFactory().getPermaLink(new PortalControllerContext(getPortletContext(), request,
										response), permaLinkRef, publicParams);
								request.setAttribute("permaLinkURL", permaLinkURL);
						}
					
						

						getPortletContext().getRequestDispatcher("/WEB-INF/jsp/liste/view.jsp").include(request,
								response);


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
			if( ! (e instanceof PortletException))
			throw new PortletException(e);
		}

		logger.debug("doView end");
	}


}
