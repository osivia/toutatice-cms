package fr.toutatice.portail.cms.nuxeo.portlets.selectors;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.VocabularyHelper;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * Vocabulary Select2 component portlet.
 * 
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class VocabularySelect2Portlet extends CMSPortlet {

    /** Selector label window property name. */
    private static final String LABEL_WINDOW_PROPERTY = "osivia.selector.label";
    /** Selector identifier window property name. */
    private static final String ID_WINDOW_PROPERTY = "osivia.selector.id";
    /** Vocabulary name window property name. */
    private static final String VOCABULARY_WINDOW_PROPERTY = "osivia.selector.vocabulary";
    /** Mono-valued selector window property name. */
    private static final String MONO_VALUED_WINDOW_PROPERTY = "osivia.selector.monoValued";

    /** View path. */
    private static final String VIEW_PATH = "/WEB-INF/jsp/selectors/select2/view.jsp";
    /** Admin path. */
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
     * @param request render request
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

                // Current selector
                List<String> selector = selectors.get(configuration.getId());
                if (selector == null) {
                    selector = new ArrayList<String>();
                    selectors.put(configuration.getId(), selector);
                } else {
                    selector.clear();
                }

                if (request.getParameter("clear") == null) {
                    String[] values = request.getParameterValues("vocabulary");
                    if (values != null) {
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
                window.setProperty(ID_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("id")));
                window.setProperty(VOCABULARY_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("vocabulary")));
                window.setProperty(MONO_VALUED_WINDOW_PROPERTY, String.valueOf(BooleanUtils.toBoolean(request.getParameter("monoValued"))));
            }
            
            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        if ("load".equals(request.getResourceID())) {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
            nuxeoController.setCacheTimeOut(TimeUnit.HOURS.toMillis(1));
            nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
            nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Configuration
            Configuration configuration = getConfiguration(window);

            // Filter
            String filter = request.getParameter("filter");

            // Result
            JSONArray results = null;
            if (configuration.getVocabulary() != null) {
                INuxeoCommand command = new LoadVocabularyCommand(configuration.getVocabulary());
                Object object = nuxeoController.executeNuxeoCommand(command);
                if (object instanceof Blob) {
                    Blob blob = (Blob) object;
                    String content = IOUtils.toString(blob.getStream(), "UTF-8");
                    JSONArray array = JSONArray.fromObject(content);
                    results = this.parse(array, filter);
                }

            }
            if (results == null) {
                results = new JSONArray();
            }

            // Content type
            response.setContentType("application/json");

            // Content
            PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
            printWriter.write(results.toString());
            printWriter.close();
        } else {
            super.serveResource(request, response);
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
        configuration.setId(window.getProperty(ID_WINDOW_PROPERTY));
        configuration.setVocabulary(window.getProperty(VOCABULARY_WINDOW_PROPERTY));
        configuration.setMonoValued(BooleanUtils.isNotFalse(BooleanUtils.toBooleanObject(window.getProperty(MONO_VALUED_WINDOW_PROPERTY))));
        return configuration;
    }


    /**
     * Parse JSON array with filter.
     * 
     * @param array JSON array
     * @param filter filter, may be null
     * @return results
     * @throws IOException
     */
    private JSONArray parse(JSONArray array, String filter) throws IOException {
        Map<String, Item> items = new HashMap<String, Item>(array.size());
        Set<String> rootItems = new LinkedHashSet<String>();

        boolean multilevel = false;

        Iterator<?> iterator = array.iterator();
        while (iterator.hasNext()) {
            JSONObject object = (JSONObject) iterator.next();
            String key = object.getString("key");
            String value = object.getString("value");
            String parent = null;
            if (object.containsKey("parent")) {
                parent = object.getString("parent");
            }
            boolean matches = this.matches(value, filter);
            
            Item item = items.get(key);
            if (item == null) {
                item = new Item(key);
                items.put(key, item);
            }
            item.value = value;
            item.parent = parent;
            if (matches) {
                item.matches = true;
                item.displayed = true;
            }

            if (StringUtils.isEmpty(parent)) {
                rootItems.add(key);
            } else {
                multilevel = true;

                Item parentItem = items.get(parent);
                if (parentItem == null) {
                    parentItem = new Item(parent);
                    items.put(parent, parentItem);
                }
                parentItem.children.add(key);

                if (item.displayed) {
                    while (parentItem != null) {
                        parentItem.displayed = true;

                        if (StringUtils.isEmpty(parentItem.parent)) {
                            parentItem = null;
                        } else {
                            parentItem = items.get(parentItem.parent);
                        }
                    }
                }
            }
        }


        JSONArray results = new JSONArray();
        this.generateChildren(items, results, rootItems, multilevel, 1, null);

        return results;
    }


    /**
     * Check if value matches filter.
     * 
     * @param value value
     * @param filter filter
     * @return true if value matches filter
     * @throws UnsupportedEncodingException
     */
    private boolean matches(String value, String filter) throws UnsupportedEncodingException {
        boolean matches = true;

        if (filter != null) {
            // Decoded value
            String decodedValue = URLDecoder.decode(value, "UTF-8");
            // Diacritical value
            String diacriticalValue = Normalizer.normalize(decodedValue, Normalizer.Form.NFD).replaceAll("\\p{IsM}+", StringUtils.EMPTY);
            
            // Filter
            String[] splittedFilters = StringUtils.split(filter, "*");
            for (String splittedFilter : splittedFilters) {
                // Diacritical filter
                String diacriticalFilter = Normalizer.normalize(splittedFilter, Normalizer.Form.NFD).replaceAll("\\p{IsM}+", StringUtils.EMPTY);

                if (!StringUtils.containsIgnoreCase(diacriticalValue, diacriticalFilter)) {
                    matches = false;
                    break;
                }
            }
        }
        
        return matches;
    }


    /**
     * Generate children.
     * 
     * @param items vocabulary items
     * @param array results JSON array
     * @param children children
     * @param optgroup options group presentation indicator
     * @param level depth level
     * @param parentId parent identifier
     * @throws UnsupportedEncodingException
     */
    private void generateChildren(Map<String, Item> items, JSONArray array, Set<String> children, boolean optgroup, int level, String parentId)
            throws UnsupportedEncodingException {
        for (String child : children) {
            Item item = items.get(child);
            if ((item != null) && item.displayed) {
                // Identifier
                String id;
                if (parentId == null) {
                    id = item.key;
                } else {
                    id = parentId + "/" + item.key;
                }

                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("text", URLDecoder.decode(item.value, "UTF-8"));
                object.put("optgroup", optgroup);
                object.put("level", level);

                if (!item.matches) {
                    object.put("disabled", true);
                }

                array.add(object);

                if (!item.children.isEmpty()) {
                    this.generateChildren(items, array, item.children, false, level + 1, id);
                }
            }
        }
    }


    /**
     * Vocabulary configuration java-bean.
     * 
     * @author Cédric Krommenhoek
     */
    public class Configuration {

        /** Selector label. */
        private String label;
        /** Selector identifier. */
        private String id;
        /** Vocabulary name. */
        private String vocabulary;
        /** Mono-valued selector indicator. */
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


    /**
     * Vocabulary item java-bean.
     * 
     * @author Cédric Krommenhoek
     */
    private class Item {

        /** Vocabulary key. */
        private final String key;
        /** Vocabulary children. */
        private final Set<String> children;

        /** Vocabulary value. */
        private String value;
        /** Vocabulary parent. */
        private String parent;
        /** Displayed item indicator. */
        private boolean displayed;
        /** Filter matches indicator. */
        private boolean matches;


        /**
         * Constructor.
         * 
         * @param key vocabulary key
         */
        public Item(String key) {
            super();
            this.key = key;
            this.children = new HashSet<String>();
        }

    }


    /**
     * Load vocabulary command.
     * 
     * @author Cédric Krommenhoek
     * @see INuxeoCommand
     */
    private class LoadVocabularyCommand implements INuxeoCommand {

        /** Vocabulary name. */
        private final String name;
        
        
        /**
         * Constructor.
         */
        public LoadVocabularyCommand(String name) {
            super();
            this.name = name;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Object execute(Session nuxeoSession) throws Exception {
            OperationRequest request = nuxeoSession.newRequest("Document.GetVocabularies");
            request.setHeader(org.nuxeo.ecm.automation.client.Constants.HEADER_NX_SCHEMAS, "*");
            request.set("vocabularies", name);
            return request.execute();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public String getId() {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getClass().getName());
            builder.append("/");
            builder.append(this.name);
            return builder.toString();
        }

    }

}
