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
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyEntry;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;

/**
 * Vocabulary selector portlet.
 *
 * @see CMSPortlet
 */
public class VocabSelectorPortlet extends CMSPortlet {

    /** Other entries choice, used for NXQL request generation. */
    public static final String OTHER_ENTRIES_CHOICE = "othersVocabEntries";

    /** Selector identifier window property name. */
    private static final String SELECTOR_ID_WINDOW_PROPERTY = "osivia.selectorId";
    /** Selector label window property name. */
    private static final String SELECTOR_LABEL_WINDOW_PROPERTY = "osivia.libelle";
    /** Vocabulary name window property name prefix. */
    private static final String VOCABULARY_NAME_WINDOW_PROPERTY_PREFIX = "osivia.vocabName";
    /** Hierarchical vocabulary preselection level 1 window property name. */
    private static final String VOCABULARY_PRESELECTION_1_WINDOW_PROPERTY = "osivia.preselect1";
    /** Mono-valued selector window property name. */
    private static final String MONOVALUED_SELECTOR_WINDOW_PROPERTY = "osivia.selectorMonoValued";
    /** Multi level vocabulary */
	private static final String MULTILEVEL_SELECTOR_WINDOW_PROPERTY = "osivia.selectorMultiLevel";
    /** Others option indicator window property name. */
    private static final String OTHERS_OPTION_WINDOW_PROPERTY = "osivia.othersOption";
    /** Others label window property name. */
    private static final String OTHERS_LABEL_WINDOW_PROPERTY = "osivia.othersLabel";

    /** Hierarchical vocabulary max level. */
    private static final int MAX_LEVEL = 3;


    /**
     * Constructor.
     */
    public VocabSelectorPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Current action
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save

                // Selector identifier
                window.setProperty(SELECTOR_ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("selectorId")));

                // Selector label
                window.setProperty(SELECTOR_LABEL_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("libelle")));

                // Vocabulary names
                for (int level = 1; level < (MAX_LEVEL + 1); level++) {
                    // Request parameter name
                    String parameter = "vocabName" + String.valueOf(level);

                    // Window property name
                    String property = VOCABULARY_NAME_WINDOW_PROPERTY_PREFIX + String.valueOf(level);

                    window.setProperty(property, StringUtils.trimToNull(request.getParameter(parameter)));
                }

                // Preselection level 1
                window.setProperty(VOCABULARY_PRESELECTION_1_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("preselect1")));

                // Mono-valued selector indicator
                window.setProperty(MONOVALUED_SELECTOR_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("selectorMonoValued")));

                // Multi-level selector indicator
                window.setProperty(MULTILEVEL_SELECTOR_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("selectorMultiLevel")));
                
                // Others option indicator
                window.setProperty(OTHERS_OPTION_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("othersOption")));

                // Others label
                window.setProperty(OTHERS_LABEL_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("othersLabel")));


                // Initialisation du vocabulaire parent suite à éventuel changement de configuration.
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                if (selectors != null) {
                    List<String> vocabs = selectors.get(request.getParameter("selectorId"));
                    if ((vocabs != null) && (vocabs.size() > 0)) {
                        vocabs.clear();
                        if (request.getParameter("selectors") != null) {
                            response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                        }
                        response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
                    }
                }
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        } else if ("view".equals(request.getPortletMode().toString())
                && (((request.getParameter("add.x") != null) || (request.getParameter("add") != null)) || (request.getParameter("monovaluedSubmit") != null))) {
            // Pour supporter le mode Ajax, il faut également test le add sans l'extension '.x'

            // Set public parameter
            String selectorId = window.getProperty(SELECTOR_ID_WINDOW_PROPERTY);
            if (selectorId != null) {
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                List<String> vocabIds = selectors.get(selectorId);
                if (vocabIds == null) {
                    vocabIds = new ArrayList<String>();
                    selectors.put(selectorId, vocabIds);
                }

                String[] selectedVocabsEntries = {request.getParameter("vocab1Id"), request.getParameter("vocab2Id"), request.getParameter("vocab3Id")};

                String separator = "";
                int index = 0;
                String selectedEntries = "";

                String preselect = window.getProperty(VOCABULARY_PRESELECTION_1_WINDOW_PROPERTY);
                if (preselect != null) {
                    // If preselection is set, controls if 2nd item is selected
                    if (StringUtils.isNotEmpty(request.getParameter("vocab2Id"))) {
                        selectedVocabsEntries = new String[]{preselect, request.getParameter("vocab2Id"), request.getParameter("vocab3Id")};
                    } else {
                        // If no item selected, remove selection
                        selectedVocabsEntries = new String[0];
                    }
                }

                for (String selectedVocabEntry : selectedVocabsEntries) {
                    if (index > 0) {
                        separator = "/";
                    }

                    if (StringUtils.isNotEmpty(selectedVocabEntry)) {

                        selectedEntries += separator + selectedVocabEntry;

                    }

                    index++;
                }

                if (request.getParameter("monovaluedSubmit") != null) {
                    /*
                     * On ne conserve qu'une valeur dans le cas d'un
                     * sélecteur mono-valué.
                     */
                    vocabIds.clear();
                }
                if (StringUtils.isNotEmpty(selectedEntries)) {
                    vocabIds.add(selectedEntries);
                }


                String lastSelectors = request.getParameter("selectors");
                if (lastSelectors != null) {
                    response.setRenderParameter("lastSelectors", lastSelectors);
                }
                response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));

                String vocab1Id = request.getParameter("vocab1Id");
                if (StringUtils.isNotEmpty(vocab1Id)) {
                    response.setRenderParameter("vocab1Id", vocab1Id);
                }

                String vocab2Id = request.getParameter("vocab2Id");
                if (vocab2Id != null) {
                    response.setRenderParameter("vocab2Id", vocab2Id);
                }

                String vocab3Id = request.getParameter("vocab3Id");
                if (vocab3Id != null) {
                    response.setRenderParameter("vocab3Id", vocab3Id);
                }


                // Réinitialisation des fenetres en mode NORMAL
                request.setAttribute("osivia.unsetMaxMode", "true");
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }

        // Delete
        if ("view".equals(request.getPortletMode().toString()) && "delete".equals(request.getParameter("action"))) {
            int occ = new Integer(request.getParameter("occ"));

            Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
            String selectorId = window.getProperty(SELECTOR_ID_WINDOW_PROPERTY);

            List<String> vocabIds = selectors.get(selectorId);
            if ((vocabIds != null) && (vocabIds.size() > occ)) {
                vocabIds.remove(occ);

                response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));
                if (request.getParameter("selectors") != null) {
                    response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                }


                // Réinitialisation des fenetres en mode NORMAL
                request.setAttribute("osivia.unsetMaxMode", "true");
            }

            String vocab1Id = request.getParameter("vocab1Id");
            if (vocab1Id != null) {
                response.setRenderParameter("vocab1Id", vocab1Id);
            }

            String vocab2Id = request.getParameter("vocab2Id");
            if (vocab2Id != null) {
                response.setRenderParameter("vocab2Id", vocab2Id);
            }

            String vocab3Id = request.getParameter("vocab3Id");
            if (vocab3Id != null) {
                response.setRenderParameter("vocab3Id", vocab3Id);
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

        String libelle = window.getProperty(SELECTOR_LABEL_WINDOW_PROPERTY);
        if (libelle == null) {
            libelle = "";
        }
        req.setAttribute("libelle", libelle);

        String selectorId = window.getProperty(SELECTOR_ID_WINDOW_PROPERTY);
        if (selectorId == null) {
            selectorId = "";
        }
        req.setAttribute("selectorId", selectorId);

        for (int niveau = 1; niveau < (MAX_LEVEL + 1); niveau++) {
            String vocabName = window.getProperty(VOCABULARY_NAME_WINDOW_PROPERTY_PREFIX + String.valueOf(niveau));
            if (vocabName == null) {
                vocabName = "";
            }
            req.setAttribute("vocabName" + String.valueOf(niveau), vocabName);
        }

        String preselect = window.getProperty(VOCABULARY_PRESELECTION_1_WINDOW_PROPERTY);
        if (preselect == null) {
            preselect = "";
        }
        req.setAttribute("preselect1", preselect);

        String selectorMonoValued = window.getProperty(MONOVALUED_SELECTOR_WINDOW_PROPERTY);
        if (selectorMonoValued == null) {
            selectorMonoValued = "0";
        }
        req.setAttribute("selectorMonoValued", selectorMonoValued);
        
        String selectorMultiLevel = window.getProperty(MULTILEVEL_SELECTOR_WINDOW_PROPERTY);
        if (selectorMultiLevel == null) {
            selectorMultiLevel = "0";
        }
        req.setAttribute("selectorMultiLevel", selectorMultiLevel);        

        String othersOption = window.getProperty(OTHERS_OPTION_WINDOW_PROPERTY);
        if (othersOption == null) {
            othersOption = "0";
        }
        req.setAttribute("othersOption", othersOption);

        String othersLabel = window.getProperty("osivia.othersLabel");
        if (othersLabel == null) {
            othersLabel = "";
        }
        req.setAttribute("othersLabel", othersLabel);

        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/selectors/vocab/admin.jsp");
        rd.include(req, res);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        try {
            response.setContentType("text/html");

            // Window
            PortalWindow window = WindowFactory.getWindow(request);

            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());

            // Selector label
            request.setAttribute("libelle", window.getProperty(SELECTOR_LABEL_WINDOW_PROPERTY));

            // Selector identifier
            String selectorId = window.getProperty(SELECTOR_ID_WINDOW_PROPERTY);
            if (selectorId == null) {
                response.getWriter().print("<h2>Identifiant non défini</h2>");
                response.getWriter().close();
                return;
            }

            List<String> vocabs = new ArrayList<String>();

            // Mono-valued selector indicator
            String selectorMonoValued = window.getProperty(MONOVALUED_SELECTOR_WINDOW_PROPERTY);
            request.setAttribute("selectorMonoValued", selectorMonoValued);

            // Multi-level selector indicator
            String selectorMultiLevel = window.getProperty(MULTILEVEL_SELECTOR_WINDOW_PROPERTY);
            request.setAttribute("selectorMultiLevel", selectorMultiLevel);
            
            // Vocabulary level 1
            String vocabName1 = window.getProperty("osivia.vocabName1");
            if (vocabName1 == null) {
                response.getWriter().print("<h2>Vocabulaire non défini</h2>");
                response.getWriter().close();
                return;
            }
            vocabs.add(vocabName1);

            // Hierarchical vocabulary level 2
            String vocabName2 = window.getProperty("osivia.vocabName2");
            if (vocabName2 != null) {
                vocabs.add(vocabName2);
            }

            // Hierarchical vocabulary level 3
            String vocabName3 = window.getProperty("osivia.vocabName3");
            if (vocabName3 != null) {
                vocabs.add(vocabName3);
            }


            String vocab1Id = request.getParameter("vocab1Id");

            String preselect1 = window.getProperty(VOCABULARY_PRESELECTION_1_WINDOW_PROPERTY);
            if (StringUtils.isNotEmpty(preselect1)) {
                vocab1Id = preselect1;
            }

            String vocab2Id = request.getParameter("vocab2Id");
            String vocab3Id = request.getParameter("vocab3Id");


            // Get public parameter
            Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
            List<String> curSelect = selectors.get(selectorId);
            if (curSelect != null) {
                request.setAttribute("vocabsId", curSelect);
                if ("1".equals(selectorMonoValued)) {
                    if ((curSelect.size() > 0) && !("1".equals(selectorMultiLevel))) {
                        String[] tokens = curSelect.get(0).split("/");
                        if (tokens.length > 0) {
                            vocab1Id = tokens[0];
                        }
                        if (tokens.length > 1) {
                            vocab2Id = tokens[1];
                        }
                        if (tokens.length > 2) {
                            vocab3Id = tokens[2];
                        }
                    }
                }
            } else {
                if ("1".equals(selectorMonoValued)) {
                    if (preselect1 == null) {
                        vocab1Id = null;
                    }
                    vocab2Id = null;
                    vocab3Id = null;
                }
                request.setAttribute("vocabsId", new ArrayList<String>());
            }

            request.setAttribute("vocab1Id", vocab1Id);
            request.setAttribute("vocab2Id", vocab2Id);
            request.setAttribute("vocab3Id", vocab3Id);
            request.setAttribute("preselect1", preselect1);
            request.setAttribute("vocabName2", vocabName2);
            request.setAttribute("vocabName3", vocabName3);

            VocabularyEntry vocab;
            String path = "/WEB-INF/jsp/selectors/vocab/view.jsp";

            if("1".equals(selectorMultiLevel)) {
            	path = "/WEB-INF/jsp/selectors/vocab/view-multilevel.jsp";
            	vocab = VocabularyHelper.getVocabularyEntry(nuxeoController, vocabs, true);
            }
            else {
            	vocab = VocabularyHelper.getVocabularyEntry(nuxeoController, vocabs, false);
            }
            
            request.setAttribute("vocab1", vocab);
            
            // Others option
            if ("1".equals(window.getProperty(OTHERS_OPTION_WINDOW_PROPERTY))) {
                request.setAttribute("othersLabel", window.getProperty(OTHERS_LABEL_WINDOW_PROPERTY));
            }

			this.getPortletContext().getRequestDispatcher(path).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

}
