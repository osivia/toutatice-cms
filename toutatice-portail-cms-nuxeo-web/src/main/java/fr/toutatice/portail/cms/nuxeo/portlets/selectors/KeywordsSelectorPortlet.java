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
 *
 *
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.constants.InternalConstants;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Portlet de selection de liste par mot clé.
 *
 * @see CMSPortlet
 */
public class KeywordsSelectorPortlet extends CMSPortlet {

    /**
     * Delete prefix.
     */
    public static final String DELETE_PREFIX = "delete_";
    /**
     * Logger.
     */
    private static final Log logger = LogFactory.getLog(KeywordsSelectorPortlet.class);
    /**
     * Internationalization bundle factory.
     */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public KeywordsSelectorPortlet() {
        super();

        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class, IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        logger.debug("processAction ");

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Current action
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save

                // Selector identifier
                String selectorId = StringUtils.trimToNull(request.getParameter("selectorId"));
                window.setProperty("osivia.selectorId", selectorId);

                // Selector label
                String selectorLabel = StringUtils.trimToNull(request.getParameter("selectorLabel"));
                window.setProperty("osivia.libelle", selectorLabel);

                // Selector placeholder
                String selectorPlaceholder = StringUtils.trimToNull(request.getParameter("selectorPlaceholder"));
                window.setProperty("osivia.placeholder", selectorPlaceholder);

                // Selector type
                String selectorType = StringUtils.trimToNull(request.getParameter("selectorType"));
                window.setProperty("osivia.keywordMonoValued", selectorType);

                // Prevent Ajax refresh indicator
                boolean preventAjaxRefresh = "2".equals(selectorType);
                window.setProperty(InternalConstants.ATTR_WINDOW_PREVENT_AJAX_REFRESH, String.valueOf(preventAjaxRefresh));

                // Keywords initialization
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                if (selectors != null) {
                    List<String> keywords = selectors.get(request.getParameter("selectorId"));
                    if ((keywords != null) && (keywords.size() > 0)) {
                        keywords.clear();
                        if( request.getParameter("selectors")!= null) {
                            response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                        }                       
                        response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
                    }
                }
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        } else if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            boolean clear = (request.getParameter("clear") != null);
            if ((request.getParameter("add") != null) || (request.getParameter("monoAdd") != null) || clear) {
                // Add or clear

                String selectorId = window.getProperty("osivia.selectorId");
                if (selectorId != null) {
                    String keyword = null;
                    if (!clear) {
                    	keyword = request.getParameter("keyword");
                    }

                    Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

                    List<String> keywords = selectors.get(selectorId);
                    if (keywords == null) {
                        keywords = new ArrayList<String>();
                        selectors.put(selectorId, keywords);
                    }

                    if ("1".equals(window.getProperty("osivia.keywordMonoValued")) || "2".equals(window.getProperty("osivia.keywordMonoValued"))) {
                        // On ne conserve qu'une valeur dans le cas d'un sélecteur mono-valué.
                        keywords.clear();
                    }

                    if (StringUtils.isNotBlank(keyword)) {
                        keywords.add(keyword);
                    }
                    
                    if( request.getParameter("selectors")!= null) {
                        response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                    }  
                    response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                    // Réinitialisation des fenetres en mode NORMAL
                    request.setAttribute(Constants.PORTLET_ATTR_UNSET_MAX_MODE, Constants.PORTLET_VALUE_ACTIVATE);
                }

                response.setPortletMode(PortletMode.VIEW);
                response.setWindowState(WindowState.NORMAL);
            } else if ("delete".equals(request.getParameter("action"))) {
                // Delete

                int occ = new Integer(request.getParameter("occ")) - 1;
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                String selectorId = window.getProperty("osivia.selectorId");
                List<String> keywords = selectors.get(selectorId);
                if ((keywords != null) && (keywords.size() > occ)) {
                    keywords.remove(occ);
                    if( request.getParameter("selectors")!= null) {
                        response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                    }  
                    
                    response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                    // Réinitialisation des fenetres en mode NORMAL
                    request.setAttribute("osivia.unsetMaxMode", "true");
                }
            }
        }
    }


    /**
     * Admin view display.
     *
     * @param request render request
     * @param response render response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        // Selector identifier
        String selectorId = StringUtils.trimToEmpty(window.getProperty("osivia.selectorId"));
        request.setAttribute("selectorId", selectorId);

        // Selector label
        String selectorLabel = StringUtils.trimToEmpty(window.getProperty("osivia.libelle"));
        request.setAttribute("selectorLabel", selectorLabel);

        // Selector placeholder
        String selectorPlaceholder = StringUtils.trimToEmpty(window.getProperty("osivia.placeholder"));
        request.setAttribute("selectorPlaceholder", selectorPlaceholder);

        // Selector type
        String selectorType = StringUtils.defaultIfBlank(window.getProperty("osivia.keywordMonoValued"), "0");
        request.setAttribute("selectorType", selectorType);


        // Request dispatcher
        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/admin.jsp");
        response.setContentType("text/html");
        dispatcher.include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        logger.debug("doView");

        try {
            response.setContentType("text/html");

            // Window
            PortalWindow window = WindowFactory.getWindow(request);

            // Selector identifier
            String selectorId = window.getProperty("osivia.selectorId");

            // Selector label
            String selectorLabel = window.getProperty("osivia.libelle");
            request.setAttribute("selectorLabel", selectorLabel);

            // Selector type
            String selectorType = StringUtils.defaultIfBlank(window.getProperty("osivia.keywordMonoValued"), "0");
            request.setAttribute("selectorType", selectorType);

            // Selector placeholder
            String selectorPlaceholder = window.getProperty("osivia.placeholder");
            if (StringUtils.isEmpty(selectorLabel) && StringUtils.isEmpty(selectorPlaceholder)) {
                // Internationalization bundle
                Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

                if (StringUtils.equals("0", selectorType)) {
                    selectorPlaceholder = bundle.getString("SELECTOR_KEYWORD_PLACEHOLDER");
                } else {
                    selectorPlaceholder = bundle.getString("SELECTOR_KEYWORDS_PLACEHOLDER");
                }
            }
            request.setAttribute("selectorPlaceholder", selectorPlaceholder);

            if (selectorId != null) {
                // Get public parameter
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                List<String> selector = selectors.get(selectorId);
                if (selector != null) {
                    String[] keywords = new String[selector.size()];

                    for (int i = 0; i < selector.size(); i++) {
                        keywords[i] = StringEscapeUtils.escapeHtml(selector.get(i));
                    }
                    
                    request.setAttribute("keywords", keywords);
                } else {
                    request.setAttribute("keywords", ArrayUtils.EMPTY_STRING_ARRAY);
                }

                // Keyword
                String keyword = StringEscapeUtils.escapeHtml(request.getParameter("keyword"));
                request.setAttribute("keyword", keyword);

                this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/view.jsp").include(request, response);
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
