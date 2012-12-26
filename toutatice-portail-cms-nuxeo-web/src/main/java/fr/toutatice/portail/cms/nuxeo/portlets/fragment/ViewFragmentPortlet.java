package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import java.io.IOException;
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
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.core.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.FragmentType;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewFragmentPortlet extends fr.toutatice.portail.cms.nuxeo.core.CMSPortlet {

	private static Log logger = LogFactory.getLog(ViewFragmentPortlet.class);

	public static Map<String, FragmentType> getFragments() {

		List<FragmentType> fragmentTypes = CMSCustomizer.getFragmentTypes();

		Map<String, FragmentType> fragmentsMap = new LinkedHashMap<String, FragmentType>();

		for (FragmentType fragmentType : fragmentTypes)
			fragmentsMap.put(fragmentType.getKey(), fragmentType);

		return fragmentsMap;
	}

	public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

		NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("changeFragmentType") != null) {
			res.setRenderParameter("fragmentTypeId", req.getParameter("fragmentTypeId"));
		}

		if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

			PortalWindow window = WindowFactory.getWindow(req);

			if (req.getParameter("fragmentTypeId") != null && req.getParameter("fragmentTypeId").length() > 0)
				window.setProperty("pia.fragmentTypeId", req.getParameter("fragmentTypeId"));
			else if (window.getProperty("pia.fragmentTypeId") != null)
				window.setProperty("pia.fragmentTypeId", null);

		
			String fragmentTypeId = req.getParameter("fragmentTypeId");
			if (fragmentTypeId != null) {
				FragmentType fragmentType = getFragments().get(fragmentTypeId);
				if (fragmentType != null) {

					try {
						fragmentType.getModule().processAdminAttributes(ctx, window, req, res);
					} catch (Exception e) {
						throw new PortletException(e);
					}
				}
			}

			

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

		String fragmentTypeId = req.getParameter("fragmentTypeId");
		if (fragmentTypeId == null)
			fragmentTypeId = window.getProperty("pia.fragmentTypeId");

		if (fragmentTypeId != null) {
			FragmentType fragmentType = getFragments().get(fragmentTypeId);
			if (fragmentType != null) {

				try {
					fragmentType.getModule().injectAdminAttributes(ctx, window, req, res);
				} catch (Exception e) {
					throw new PortletException(e);
				}
			}
		}

		req.setAttribute("fragmentTypeId", fragmentTypeId);

		req.setAttribute("fragmentTypes", getFragments());

		req.setAttribute("ctx", ctx);

		rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/fragment/admin.jsp");
		rd.include(req, res);

	}

	

	@SuppressWarnings("unchecked")
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException,
			PortletSecurityException, IOException {

		logger.debug("doView");

		try {

			response.setContentType("text/html");

			PortalWindow window = WindowFactory.getWindow(request);

			String fragmentTypeId = (String) window.getProperty("pia.fragmentTypeId");

			NuxeoController ctx = new NuxeoController(request, response, getPortletContext());

			if (fragmentTypeId != null) {
				FragmentType fragmentType = getFragments().get(fragmentTypeId);
				
				if (fragmentType != null) {

					try {
						fragmentType.getModule().injectViewAttributes(ctx, window, request, response);
					} catch (Exception e) {
						throw new PortletException(e);
					}
				}
				
				request.setAttribute("fragmentType", fragmentType);

				getPortletContext().getRequestDispatcher("/WEB-INF/jsp/fragment/view.jsp").include(request, response);
			} else {
				response.setContentType("text/html");
				response.getWriter().print("<h2>Fragment non défini</h2>");
				response.getWriter().close();
				return;
			}

		} catch (NuxeoException e) {
			PortletErrorHandler.handleGenericErrors(response, e);
		} catch (Exception e) {
			if (!(e instanceof PortletException))
				throw new PortletException(e);
		}

		logger.debug("doView end");

	}
}
