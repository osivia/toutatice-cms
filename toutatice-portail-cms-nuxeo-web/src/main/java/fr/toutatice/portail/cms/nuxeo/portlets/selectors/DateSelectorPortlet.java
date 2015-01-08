/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;


/**
 * Portlet de sélection par dates.
 * 
 * @see CMSPortlet
 */
public class DateSelectorPortlet extends CMSPortlet {

    /** Dates separator. */
    public static String DATES_SEPARATOR = "%";

    /** Date from attribute name suffix. */
    private static final String DATE_FROM_SUFFIX = "-date-from";
    /** Date to attribute name suffix. */
    private static final String DATE_TO_SUFFIX = "-date-to";


    /** Logger. */
    private static Log logger = LogFactory.getLog(DateSelectorPortlet.class);


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        logger.debug("processAction ");

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);


        if ("admin".equals(request.getPortletMode().toString()) && (request.getParameter("modifierPrefs") != null)) {
            if (request.getParameter("selectorId").length() > 0) {
                window.setProperty("osivia.selectorId", request.getParameter("selectorId"));
            } else if (window.getProperty("osivia.selectorId") != null) {
                window.setProperty("osivia.selectorId", null);
            }

            if (request.getParameter("libelle").length() > 0) {
                window.setProperty("osivia.libelle", request.getParameter("libelle"));
            } else if (window.getProperty("osivia.libelle") != null) {
                window.setProperty("osivia.libelle", null);
            }

            if ("1".equals(request.getParameter("datesMonoValued"))) {
                window.setProperty("osivia.datesMonoValued", "1");
            } else if (window.getProperty("osivia.datesMonoValued") != null) {
                window.setProperty("osivia.datesMonoValued", null);
            }

            /* Initialisation des dates suite à configuration. */
            Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
            if (selectors != null) {
                List<String> dates = selectors.get(request.getParameter("selectorId"));
                if ((dates != null) && (dates.size() > 0)) {
                    dates.clear();
                    if( request.getParameter("selectors")!= null)
                        response.setRenderParameter("lastSelectors", request.getParameter("selectors"));                     
                    
                    response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
                }
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }


        if ("admin".equals(request.getPortletMode().toString()) && (request.getParameter("annuler") != null)) {
            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }


        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View mode

            // Action parameter
            String action = request.getParameter("action");
            // Selector identifier
            String selectorId = window.getProperty("osivia.selectorId");
            // Mono valued selector indicator
            boolean monoValued = "1".equals(window.getProperty("osivia.datesMonoValued"));

            if ("add".equalsIgnoreCase(action)) {
                // Add action
                this.add(request, response, selectorId, monoValued);
            } else if ("delete".equalsIgnoreCase(action)) {
                // Delete action
                this.delete(request, response, selectorId);
            }

        }
    }


    /**
     * Utility method used to add dates.
     * 
     * @param request action request
     * @param response action response
     * @param selectorId selector identifier
     * @param monoValued mono valued selector indicator
     */
    private void add(ActionRequest request, ActionResponse response, String selectorId, boolean monoValued) {
        // Selectors
        Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
        List<String> datesSelector = selectors.get(selectorId);
        if (datesSelector == null) {
            datesSelector = new ArrayList<String>();
            selectors.put(selectorId, datesSelector);
        }
        if (monoValued) {
            datesSelector.clear();
        }

        // Dates
        String dateFrom = request.getParameter(response.getNamespace() + DATE_FROM_SUFFIX);
        String dateTo = request.getParameter(response.getNamespace() + DATE_TO_SUFFIX);
        boolean validation = true;
        if (StringUtils.isBlank(dateFrom)) {
            if (StringUtils.isBlank(dateTo)) {
                validation = false;
            } else {
                dateFrom = dateTo;
            }
        } else if (StringUtils.isBlank(dateTo)) {
            dateTo = dateFrom;
        }

        if (validation) {
            datesSelector.add(dateFrom + DATES_SEPARATOR + dateTo);
        }
        if( request.getParameter("selectors")!= null)
            response.setRenderParameter("lastSelectors", request.getParameter("selectors"));              
        response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

        // Reset window mode to normal
        request.setAttribute(Constants.PORTLET_ATTR_UNSET_MAX_MODE, Constants.PORTLET_VALUE_ACTIVATE);
    }


    /**
     * Utility method used to delete dates.
     * 
     * @param request action request
     * @param response action response
     * @param selectorId selector identifier
     */
    private void delete(ActionRequest request, ActionResponse response, String selectorId) {
        // Deletion index
        int occ = Integer.valueOf(request.getParameter("occ")) - 1;

        // Selectors
        Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
        List<String> dateSelectors = selectors.get(selectorId);
        if (dateSelectors != null) {
            dateSelectors.remove(occ);
        }

        if( request.getParameter("selectors")!= null)
            response.setRenderParameter("lastSelectors", request.getParameter("selectors"));      
        response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

        // Reset window mode to normal
        request.setAttribute(Constants.PORTLET_ATTR_UNSET_MAX_MODE, Constants.PORTLET_VALUE_ACTIVATE);

    }


    /**
     * Admin view display.
     * 
     * @param req request
     * @param res response
     * @throws PortletException
     * @throws IOException
     */
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
        if (datesMonoValued == null) {
            datesMonoValued = "0";
        }
        req.setAttribute("datesMonoValued", datesMonoValued);

        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/date/admin.jsp");
        rd.include(req, res);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        logger.debug("doView");

        try {
            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);

            String selectorId = window.getProperty("osivia.selectorId");
            String libelle = window.getProperty("osivia.libelle");
            request.setAttribute("libelle", libelle);
            String datesMonoValued = window.getProperty("osivia.datesMonoValued");
            request.setAttribute("datesMonoValued", datesMonoValued);

            String idDateFrom = response.getNamespace() + DATE_FROM_SUFFIX;
            String dateFrom = request.getParameter(idDateFrom);
            String idDateTo = response.getNamespace() + DATE_TO_SUFFIX;
            String dateTo = request.getParameter(idDateTo);

            if (selectorId != null) {
                // Get public parameter
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                List<String> curSelect = selectors.get(selectorId);
                if (curSelect != null) {
                    request.setAttribute("dates", curSelect);

                    if ("1".equals(datesMonoValued)) {


                        if (curSelect.size() > 0) {
                            String[] tokens = curSelect.get(0).split("\\" + DATES_SEPARATOR);
                            if (tokens.length > 0) {
                                dateFrom = tokens[0];
                            }
                            if (tokens.length > 1) {
                                dateTo = tokens[1];
                            }

                        }
                    }
                } else {
                    request.setAttribute("dates", new ArrayList<String>());
                }

                request.setAttribute(idDateFrom, dateFrom);
                request.setAttribute(idDateTo, dateTo);

                this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/date/view.jsp").include(request, response);
            } else {
                response.getWriter().print("<h2>Identifiant non défini</h2>");
                response.getWriter().close();
            }
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }

        logger.debug("doView end");
    }
}
