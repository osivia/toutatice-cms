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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;




/**
 * Portlet de selection de liste par mot cle
 */

public class DateSelectorPortlet extends CMSPortlet {

	private static Log logger = LogFactory.getLog(DateSelectorPortlet.class);

	public static String DATES_SEPARATOR = "%";

	//public static String DELETE_PREFIX = "delete_";

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		logger.debug("processAction ");

		PortalWindow window = WindowFactory.getWindow(req);

		if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("modifierPrefs") != null)) {

			if( req.getParameter("selectorId").length() > 0) {
                window.setProperty("osivia.selectorId", req.getParameter("selectorId"));
            } else if (window.getProperty("osivia.selectorId") != null) {
                window.setProperty("osivia.selectorId", null);
            }

			if( req.getParameter("libelle").length() > 0) {
                window.setProperty("osivia.libelle", req.getParameter("libelle"));
            } else if (window.getProperty("osivia.libelle") != null) {
                window.setProperty("osivia.libelle", null);
            }

			if("1".equals(req.getParameter("datesMonoValued"))) {
                window.setProperty("osivia.datesMonoValued", "1");
            } else if (window.getProperty("osivia.datesMonoValued") != null) {
                window.setProperty("osivia.datesMonoValued", null);
            }

			/* Initialisation des dates suite à configuration. */
			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			if(selectors != null){
				List<String> dates = selectors.get(req.getParameter("selectorId"));
				if((dates != null) && (dates.size() > 0)){
					dates.clear();
					res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
				}
			}


			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("annuler") != null)) {

			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Pour supporter le mode Ajax, il faut également test le add sans l'extension '.x'
		boolean isAddAction = (req.getParameter("add.x") != null) || (req.getParameter("add") != null);
		boolean isMonoValuedAddAction = (req.getParameter("monoAdd.x") != null) || (req.getParameter("monoAdd") != null);

		if ("view".equals(req.getPortletMode().toString()) && (isAddAction || isMonoValuedAddAction))
		{
			String selectorId = window.getProperty("osivia.selectorId");
			if (selectorId != null)
			{
                if (StringUtils.isNotEmpty(req.getParameter("datefrom")) && StringUtils.isNotEmpty(req.getParameter("dateto")))
				{
					Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));

					if ((req.getParameter("datefrom") != null) && (req.getParameter("datefrom").length() > 0)) {/* TO ASK: utilité du test? */

						List<String> dates = selectors.get(selectorId);
						if (dates == null) {
							dates = new ArrayList<String>();
							selectors.put(selectorId, dates);
						}
						if("1".equals(window.getProperty("osivia.datesMonoValued"))) {
                            dates.clear();
                        }

						dates.add(req.getParameter("datefrom") + DATES_SEPARATOR + req.getParameter("dateto"));

					}

					res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

					//Réinitialisation des fenetres en mode NORMAL
					req.setAttribute("osivia.unsetMaxMode", "true");
				}
			}
			res.setPortletMode(PortletMode.VIEW);
			res.setWindowState(WindowState.NORMAL);
		}

		// Delete
		if ("view".equals(req.getPortletMode().toString()) && "delete".equals(req.getParameter("action")) )
		{
			int occ = new Integer(req.getParameter("occ"));

			Map<String, List<String>> selectors = PageSelectors.decodeProperties(req.getParameter("selectors"));
			String selectorId = window.getProperty("osivia.selectorId");

			List<String> dates = selectors.get(selectorId);
			if ((dates != null) && (dates.size() > 0))
			{
				dates.remove(occ);
				res.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                // Réinitialisation des fenetres en mode NORMAL
                req.setAttribute("osivia.unsetMaxMode", "true");
			}
		}

	}

	@RenderMode(name = "admin")
	public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

		res.setContentType("text/html");
		PortletRequestDispatcher rd = null;

		PortalWindow window = WindowFactory.getWindow(req);

		String selectorId = window.getProperty("osivia.selectorId");
		if (selectorId == null) {
            selectorId = "";
        }
		req.setAttribute("selectorId", selectorId);


		String libelle = window.getProperty("osivia.libelle");
		if (libelle == null) {
            libelle = "";
        }
		req.setAttribute("libelle", libelle);

		String datesMonoValued = window.getProperty("osivia.datesMonoValued");
		if(datesMonoValued == null) {
            datesMonoValued = "0";
        }
		req.setAttribute("datesMonoValued", datesMonoValued);

		rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/date/admin.jsp");
		rd.include(req, res);

	}


	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			String selectorId = window.getProperty("osivia.selectorId");
			String libelle = window.getProperty("osivia.libelle");
			request.setAttribute("libelle", libelle);
			String datesMonoValued = window.getProperty("osivia.datesMonoValued");
			request.setAttribute("datesMonoValued", datesMonoValued);
			String dateFrom = request.getParameter("datefrom");
			String dateTo = request.getParameter("dateto");

			if (selectorId != null)
			{
				// Get public parameter

				Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

				if (selectors.get(selectorId) != null) {
                    request.setAttribute("dates", selectors.get(selectorId));
                } else {
                    request.setAttribute("dates", new ArrayList<String>());
                }

				request.setAttribute("datefrom", dateFrom);
				request.setAttribute("dateto", dateTo);


				this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/date/view.jsp").include(request,
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
			if (!(e instanceof PortletException)) {
                throw new PortletException(e);
            }
		}

		logger.debug("doView end");
	}

}
