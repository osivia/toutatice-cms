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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.ArrayUtils;
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
 * Portlet de selection de liste par mot clé.
 *
 * @see CMSPortlet
 */
public class KeywordsSelectorPortlet extends CMSPortlet {

    /** Logger. */
    private static final Log logger = LogFactory.getLog(KeywordsSelectorPortlet.class);
    /** Delete prefix. */
    public static final String DELETE_PREFIX = "delete_";


    /**
     * Default constructor.
     */
    public KeywordsSelectorPortlet() {
        super();
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
                String identifier = request.getParameter("selectorId");
                if (StringUtils.isNotBlank(identifier)) {
                    window.setProperty("osivia.selectorId", identifier);
                } else {
                    window.setProperty("osivia.selectorId", null);
                }

                // Selector label
                String label = request.getParameter("libelle");
                if (StringUtils.isNotBlank(label)) {
                    window.setProperty("osivia.libelle", label);
                } else {
                    window.setProperty("osivia.libelle", null);
                }

                // Mono-valued indicator
                if ("1".equals(request.getParameter("keywordMonoValued"))) {
                    window.setProperty("osivia.keywordMonoValued", "1");
                } else {
                    window.setProperty("osivia.keywordMonoValued", null);
                }

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

                    if ("1".equals(window.getProperty("osivia.keywordMonoValued"))) {
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

        String keywordMonoValued = window.getProperty("osivia.keywordMonoValued");
        if (keywordMonoValued == null) {
            keywordMonoValued = "0";
        }
        req.setAttribute("keywordMonoValued", keywordMonoValued);

        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/keywords/admin.jsp");
        rd.include(req, res);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        logger.debug("doView");

        try {
            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);

            String selectorId = window.getProperty("osivia.selectorId");

            String libelle = window.getProperty("osivia.libelle");
            request.setAttribute("libelle", libelle);

            String keywordMonoValued = window.getProperty("osivia.keywordMonoValued");
            request.setAttribute("keywordMonoValued", keywordMonoValued);

            String keyword = request.getParameter("keyword");

            if (selectorId != null) {
                // Get public parameter

                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

                List<String> selector = selectors.get(selectorId);
                if (selector != null) {
                    String[] keywords = new String[selector.size()];
                    request.setAttribute("keywords", selector.toArray(keywords));
                } else {
                    request.setAttribute("keywords", ArrayUtils.EMPTY_STRING_ARRAY);
                }

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
