package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vocabulary Select2 component portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class VocabularySelect2Portlet extends CMSPortlet {

    /**
     * Selector label window property name.
     */
    private static final String LABEL_WINDOW_PROPERTY = "osivia.selector.label";
    /**
     * Selector "all" label window property name.
     */
    private static final String ALL_LABEL_WINDOW_PROPERTY = "osivia.selector.allLabel";
    /**
     * Selector identifier window property name.
     */
    private static final String ID_WINDOW_PROPERTY = "osivia.selector.id";
    /**
     * Vocabulary name window property name.
     */
    private static final String VOCABULARY_WINDOW_PROPERTY = "osivia.selector.vocabulary";
    /**
     * Mono-valued selector window property name.
     */
    private static final String MONO_VALUED_WINDOW_PROPERTY = "osivia.selector.monoValued";

    /**
     * View path.
     */
    private static final String VIEW_PATH = "/WEB-INF/jsp/selectors/select2/view.jsp";
    /**
     * Admin path.
     */
    private static final String ADMIN_PATH = "/WEB-INF/jsp/selectors/select2/admin.jsp";


    /**
     * Constructor.
     */
    public VocabularySelect2Portlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Configuration
        Configuration configuration = getConfiguration(window);
        request.setAttribute("configuration", configuration);

        if ((configuration.getId() != null) && (configuration.getVocabulary() != null)) {
            // Selectors
            Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));

            // Current selector
            List<String> selector = selectors.get(configuration.getId());
            if (selector != null) {
                // Selected items
                Map<String, String> selectedItems = new LinkedHashMap<String, String>(selector.size());
                request.setAttribute("selectedItems", selectedItems);

                for (String key : selector) {
                    String vocabularyKey;
                    if (key.contains("/")) {
                        vocabularyKey = StringUtils.substringAfterLast(key, "/");
                    } else {
                        vocabularyKey = key;
                    }

                    String value = VocabularyHelper.getVocabularyLabel(nuxeoController, configuration.getVocabulary(), vocabularyKey);
                    selectedItems.put(key, value);
                }
            }
        }

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(VIEW_PATH);
        dispatcher.include(request, response);
    }


    /**
     * Admin render mapping.
     *
     * @param request  render request
     * @param response render response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Configuration
        Configuration configuration = getConfiguration(window);
        request.setAttribute("configuration", configuration);

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(ADMIN_PATH);
        dispatcher.include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Current action
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        // Configuration
        Configuration configuration = getConfiguration(window);


        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            if ("save".equals(action) && (configuration.getId() != null) && (configuration.getVocabulary() != null)) {
                // Save

                // Selectors
                Map<String, List<String>> selectors = PageSelectors.decodeProperties(request.getParameter("selectors"));
                if (request.getParameter("selectors") != null) {
                    response.setRenderParameter("lastSelectors", request.getParameter("selectors"));
                }
                // Current selector
                List<String> selector = selectors.get(configuration.getId());
                if (selector == null) {
                    selector = new ArrayList<>();
                    selectors.put(configuration.getId(), selector);
                } else {
                    selector.clear();
                }

                if (request.getParameter("clear") == null) {
                    String[] values = request.getParameterValues("vocabulary");
                    if (ArrayUtils.isEmpty(values) || ((values.length == 1) && StringUtils.isEmpty(values[0]))) {
                        selectors.remove(configuration.getId());
                    } else {
                        for (String value : values) {
                            selector.add(value);
                        }
                    }
                }

                response.setRenderParameter("selectors", PageSelectors.encodeProperties(selectors));


                request.setAttribute(Constants.PORTLET_ATTR_UNSET_MAX_MODE, String.valueOf(true));
            }


        } else if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save
                window.setProperty(LABEL_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("label")));
                window.setProperty(ALL_LABEL_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("allLabel")));
                window.setProperty(ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("id")));
                window.setProperty(VOCABULARY_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("vocabulary")));
                window.setProperty(MONO_VALUED_WINDOW_PROPERTY, String.valueOf(BooleanUtils.toBoolean(request.getParameter("monoValued"))));
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }
    }


    /**
     * Get vocabulary configuration.
     *
     * @param window portal window
     * @return configuration
     */
    private Configuration getConfiguration(PortalWindow window) {
        Configuration configuration = new Configuration();
        configuration.setLabel(window.getProperty(LABEL_WINDOW_PROPERTY));
        configuration.setAllLabel(window.getProperty(ALL_LABEL_WINDOW_PROPERTY));
        configuration.setId(window.getProperty(ID_WINDOW_PROPERTY));
        configuration.setVocabulary(window.getProperty(VOCABULARY_WINDOW_PROPERTY));
        configuration.setMonoValued(BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(window.getProperty(MONO_VALUED_WINDOW_PROPERTY))));
        return configuration;
    }


    /**
     * Vocabulary configuration java-bean.
     *
     * @author Cédric Krommenhoek
     */
    public class Configuration {

        /**
         * Selector label.
         */
        private String label;
        /**
         * Selector "all" label.
         */
        private String allLabel;
        /**
         * Selector identifier.
         */
        private String id;
        /**
         * Vocabulary name.
         */
        private String vocabulary;
        /**
         * Mono-valued selector indicator.
         */
        private boolean monoValued;


        /**
         * Constructor.
         */
        public Configuration() {
            super();
        }


        /**
         * Getter for label.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Setter for label.
         *
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        public String getAllLabel() {
            return allLabel;
        }

        public void setAllLabel(String allLabel) {
            this.allLabel = allLabel;
        }

        /**
         * Getter for id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Setter for id.
         *
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Getter for vocabulary.
         *
         * @return the vocabulary
         */
        public String getVocabulary() {
            return vocabulary;
        }

        /**
         * Setter for vocabulary.
         *
         * @param vocabulary the vocabulary to set
         */
        public void setVocabulary(String vocabulary) {
            this.vocabulary = vocabulary;
        }

        /**
         * Getter for monoValued.
         *
         * @return the monoValued
         */
        public boolean isMonoValued() {
            return monoValued;
        }

        /**
         * Setter for monoValued.
         *
         * @param monoValued the monoValued to set
         */
        public void setMonoValued(boolean monoValued) {
            this.monoValued = monoValued;
        }

    }

}
