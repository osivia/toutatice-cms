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
package fr.toutatice.portail.cms.nuxeo.api;

import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.path.IBrowserService;
import org.osivia.portal.api.portlet.PortalGenericPortlet;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.PortalUrlType;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import javax.portlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Superclass for CMS Portlet.
 *
 * @see PortalGenericPortlet
 */
public abstract class CMSPortlet extends PortalGenericPortlet {

    /** Log. */
    private final Log logger;

    /** The nuxeo navigation service. */
    private final INuxeoService nuxeoService;
    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;
    /** Documents browser service. */
    private final IBrowserService browserService;
    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;
    /** Internationalization bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public CMSPortlet() {
        super();

        // Log
        this.logger = LogFactory.getLog(CMSPortlet.class);

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        // Browser service
        this.browserService = Locator.findMBean(IBrowserService.class, IBrowserService.MBEAN_NAME);
        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class, IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * Get CMS service.
     *
     * @return CMS service
     */
    public ICMSService getCMSService() {
        return this.cmsServiceLocator.getCMSService();
    }


    /**
     * Get Nuxeo service.
     *
     * @return Nuxeo service
     */
    public INuxeoService getNuxeoService() {
        return this.nuxeoService;
    }


    /**
     * Gets the nuxeo navigation service.
     *
     * @return the nuxeo navigation service
     * @deprecated see getNuxeoService
     * @throws Exception
     *             the exception
     */
    @Deprecated
    public INuxeoService getNuxeoNavigationService() throws Exception {
        return this.nuxeoService;
    }


    /**
     * Performs nuxeo service initialization.
     *
     * @param config the config
     * @throws PortletException the portlet exception
     * @see javax.portlet.GenericPortlet#init(javax.portlet.PortletConfig)
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        try {
            new NuxeoController(this.getPortletContext()).startNuxeoService();
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Performs nuxeo service .
     *
     * @see javax.portlet.GenericPortlet#destroy()
     */
    @Override
    public void destroy() {
        try {
            // Destruction des threads éventuels
            new NuxeoController(this.getPortletContext()).stopNuxeoService();
        } catch (Exception e) {
            this.logger.error(e);
        }

        super.destroy();
    }


    /**
     * Format resource last modified.
     *
     * @return the string
     */
    public String formatResourceLastModified() {
        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }


    /**
     * Checks if resource has expired.
     *
     * @param sOriginalDate the original date
     * @param resourceResponse the resource response
     * @return true, if is resource expired
     */
    public boolean isResourceExpired(String sOriginalDate, ResourceResponse resourceResponse, String refreshMs) {
        boolean isExpired = true;

        if (sOriginalDate != null) {
            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);

            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < (originalDate.getTime() + (resourceResponse.getCacheControl().getExpirationTime() * 1000))) {
                    if ((refreshMs == null) || (Long.parseLong(refreshMs) < originalDate.getTime())) {
                        isExpired = false;
                    }
                }
            } catch (Exception e) {
                // Do nothing
            }
        }

        return isExpired;
    }


    /**
     * Process comment action.
     *
     * @param request action request
     * @param response action response
     * @throws PortletException
     * @throws IOException
     */
    protected void processCommentAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        // Comment identifier
        String id = request.getParameter("id");
        // Comment content
        String content = request.getParameter("content");


        if (PortletMode.VIEW.equals(request.getPortletMode())) {
            if ("addComment".equals(action)) {
                // Add comment action
                this.addCommentAction(request, response, content, null);
            } else if ("replyComment".equals(action)) {
                // Reply comment action
                this.addCommentAction(request, response, content, id);
            } else if ("deleteComment".equals(action)) {
                // Delete comment
                this.deleteCommentAction(request, response, id);
            }
        }
    }


    /**
     * Add or reply comment action.
     *
     * @param request action request
     * @param response action response
     * @param content comment content
     * @param parentId parent comment identifier, may be null
     * @throws PortletException
     */
    protected void addCommentAction(ActionRequest request, ActionResponse response, String content, String parentId) throws PortletException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Document context
        NuxeoDocumentContext documentContext = nuxeoController.getCurrentDocumentContext();
        // Comments service
        INuxeoCommentsService commentsService = nuxeoController.getNuxeoCommentsService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();
        // Document
        Document document = documentContext.getDocument();

        // Comment DTO
        CommentDTO comment = new CommentDTO();
        comment.setContent(content);

        try {
            commentsService.addDocumentComment(cmsContext, document, comment, parentId);
        } catch (CMSException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Delete comment action.
     *
     * @param request action request
     * @param response action response
     * @param id comment identifier
     * @throws PortletException
     */
    protected void deleteCommentAction(ActionRequest request, ActionResponse response, String id) throws PortletException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Document context
        NuxeoDocumentContext documentContext = nuxeoController.getCurrentDocumentContext();
        // Comments service
        INuxeoCommentsService commentsService = nuxeoController.getNuxeoCommentsService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();
        // Document
        Document document = documentContext.getDocument();

        try {
            commentsService.deleteDocumentComment(cmsContext, document, id);
        } catch (CMSException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Serve resource by cache.
     *
     * @param resourceRequest the resource request
     * @param resourceResponse the resource response
     * @return true, if successful
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean serveResourceByCache(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
        String sOriginalDate = resourceRequest.getProperty("if-modified-since");
        if (sOriginalDate == null) {
            sOriginalDate = resourceRequest.getProperty("If-Modified-Since");
        }

        if (!this.isResourceExpired(sOriginalDate, resourceResponse, resourceRequest.getParameter("refresh"))) {
            // validation

            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_NOT_MODIFIED));
            resourceResponse.setProperty("Last-Modified", sOriginalDate);

            resourceResponse.getPortletOutputStream().close();

            return true;
        }

        return false;
    }


    /**
     * Serve ressource exception.
     *
     * @param resourceRequest resource request
     * @param resourceResponse resource response
     * @param e Nuxeo exception
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void serveResourceException(ResourceRequest resourceRequest, ResourceResponse resourceResponse, NuxeoException e) throws PortletException,
            IOException {
        int httpErrorCode = 0;
        if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
            httpErrorCode = HttpServletResponse.SC_NOT_FOUND;
            String message = "Resource CMSPortlet " + resourceRequest.getParameterMap() + " not found (error 404).";
            this.logger.error(message);
        } else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {
            httpErrorCode = HttpServletResponse.SC_FORBIDDEN;
        }

        if (httpErrorCode != 0) {
            NuxeoController nuxeoController = this.createNuxeoController(resourceRequest, resourceResponse);
            PortalControllerContext portalCtx = new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse);

            String errorUrl = nuxeoController.getPortalUrlFactory().getHttpErrorUrl(portalCtx, httpErrorCode);

            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
            resourceResponse.setProperty("Location", errorUrl);
            resourceResponse.getPortletOutputStream().close();
        } else {
            throw e;
        }
    }


    /**
     * Serve CMS Resource.
     *
     * IMPORTANT !!!
     *
     * For web page mode, live mode MUST BE computed by the portlet when generating resource URL (displayLiveVersion=1)
     *
     * @param request resource request
     * @param response resource response
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        try {
            if (this.serveResourceByCache(request, response)) {
                return;
            }

            // Redirection
            if ("link".equals(request.getParameter("type"))) {
                NuxeoController nuxeoController = new NuxeoController(request, null, this.getPortletContext());

                // Document identifier
                String id = request.getResourceID();

                // Fetch document
                Document document = nuxeoController.fetchDocument(id);

                // URL
                String url = document.getString("clink:link");
                if (!StringUtils.startsWith(url, "http")) {
                    url = "http://" + url;
                }

                // Response
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
                response.setProperty("Location", url);
                response.getPortletOutputStream().close();
            } else if ("fancytreeLazyLoading".equals(request.getResourceID())) {
                // Fancytree lazy-loading
                this.serveResourceFancytreeLazyLoading(request, response);
            } else if ("editor".equals(request.getResourceID())) {
                // Get editor properties
                String editorId = request.getParameter("editorId");
                this.serveResourceEditor(request, response, editorId);
            } else if ("select2-vocabulary".equals(request.getResourceID())) {
                // Select2 vocabulary
                this.serveResourceSelect2Vocabulary(request, response);
            } else {
                // Tous les autres cas sont dépréciés
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_NOT_FOUND));
            }
        } catch (NuxeoException e) {
            this.serveResourceException(request, response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Serve resource for fancytree lazy loading.
     * @param request resource request
     * @param response resource response
     * @throws PortletException
     * @throws IOException
     */
    protected void serveResourceFancytreeLazyLoading(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);

        try {
            String data = this.browserService.browse(portalControllerContext);

            // Content type
            response.setContentType("application/json");

            // Content
            PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
            printWriter.write(data);
            printWriter.close();
        } catch (PortalException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Serve resource for editor properties.
     * @param request resource request
     * @param response resource response
     * @param editorId editor identifier
     * @throws PortletException
     * @throws IOException
     */
    protected void serveResourceEditor(ResourceRequest request, ResourceResponse response, String editorId) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

        // Editor title
        String title;
        if ("link".equals(editorId)) {
            title = bundle.getString("EDITOR_LINK_TITLE");
        } else {
            title = null;
        }

        // Editor instance
        String instance;
        if ("link".equals(editorId)) {
            instance = "osivia-services-editor-link-instance";
        } else {
            instance = null;
        }

        // Editor properties
        Map<String, String> properties = new HashMap<>();
        if ("link".equals(editorId)) {
            properties.put("osivia.editor.url", request.getParameter("url"));
            properties.put("osivia.editor.text", request.getParameter("text"));
            properties.put("osivia.editor.title", request.getParameter("title"));
            properties.put("osivia.editor.onlyText", request.getParameter("onlyText"));
            properties.put("osivia.editor.basePath", nuxeoController.getBasePath());
        }

        // URL
        String url;
        try {
            url = this.portalUrlFactory.getStartPortletUrl(portalControllerContext, instance, properties, PortalUrlType.MODAL);
        } catch (PortalException e) {
            throw new PortletException(e);
        }


        // JSON
        JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("url", url);


        // Content type
        response.setContentType("application/json");

        // Content
        PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
        printWriter.write(object.toString());
        printWriter.close();
    }


    /**
     * Serve resource for Select2 vocabulary.
     * 
     * @param request resource request
     * @param response resource response
     * @throws PortletException
     * @throws IOException
     */
    protected void serveResourceSelect2Vocabulary(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
        nuxeoController.setCacheTimeOut(TimeUnit.HOURS.toMillis(1));
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        // Vocabulary
        String vocabulary = request.getParameter("vocabulary");
        // Vocabulary tree indicator
        boolean tree = BooleanUtils.toBoolean(request.getParameter("tree"));
        // Vocabulary optgroup disabled indicator
        boolean optgroupDisabled = BooleanUtils.toBoolean(request.getParameter("optgroupDisabled"));
        // Filter
        String filter = request.getParameter("filter");

        // Result
        JSONArray results = null;
        if (StringUtils.isNotEmpty(vocabulary)) {
            INuxeoCommand command = new LoadVocabularyCommand(vocabulary);
            Object object = nuxeoController.executeNuxeoCommand(command);
            if (object instanceof Blob) {
                Blob blob = (Blob) object;
                String content = IOUtils.toString(blob.getStream(), CharEncoding.UTF_8);
                JSONArray array = JSONArray.fromObject(content);
                results = this.parseVocabulary(array, tree, optgroupDisabled, filter);
            }
        }
        if (results == null) {
            results = new JSONArray();
        }

        // All
        String allLabel = request.getParameter("allLabel");
        if (StringUtils.isNotEmpty(allLabel)) {
            JSONObject object = new JSONObject();
            object.put("id", StringUtils.EMPTY);
            object.put("text", allLabel);
            object.put("optgroup", false);
            object.put("level", 1);
            results.add(0, object);
        }


        // Content type
        response.setContentType("application/json");

        // Content
        PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
        printWriter.write(results.toString());
        printWriter.close();
    }


    /**
     * Parse vocabulary JSON array with filter.
     *
     * @param array JSON array
     * @param tree vocabulary tree indicator
     * @param optgroupDisabled vocabulary option group disabled indicator
     * @param filter filter, may be null
     * @return results
     * @throws IOException
     */
    private JSONArray parseVocabulary(JSONArray array, boolean tree, boolean optgroupDisabled, String filter) throws IOException {
        Map<String, VocabularyItem> items = new HashMap<String, VocabularyItem>(array.size());
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
            boolean matches = this.matchesVocabularyItem(value, filter);

            VocabularyItem item = items.get(key);
            if (item == null) {
                item = new VocabularyItem(key);
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

                VocabularyItem parentItem = items.get(parent);
                if (parentItem == null) {
                    parentItem = new VocabularyItem(parent);
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
        this.generateVocabularyChildren(items, results, rootItems, multilevel, 1, null, tree, optgroupDisabled);

        return results;
    }


    /**
     * Check if value matches filter.
     *
     * @param value vocabulary item value
     * @param filter filter
     * @return true if value matches filter
     * @throws UnsupportedEncodingException
     */
    private boolean matchesVocabularyItem(String value, String filter) throws UnsupportedEncodingException {
        boolean matches = true;

        if (filter != null) {
            // Decoded value
            String decodedValue = URLDecoder.decode(value, CharEncoding.UTF_8);
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
     * Generate vocabulary children.
     *
     * @param items vocabulary items
     * @param array results JSON array
     * @param children children
     * @param optgroup options group presentation indicator
     * @param level depth level
     * @param parentId parent identifier
     * @param tree vocabulary tree indicator
     * @param optgroupDisabled vocabulary option group disabled indicator
     * @throws UnsupportedEncodingException
     */
    private void generateVocabularyChildren(Map<String, VocabularyItem> items, JSONArray array, Set<String> children, boolean optgroup, int level,
            String parentId, boolean tree, boolean optgroupDisabled) throws UnsupportedEncodingException {
        for (String child : children) {
            VocabularyItem item = items.get(child);
            if ((item != null) && item.displayed) {
                // Identifier
                String id;
                if (!tree || StringUtils.isEmpty(parentId)) {
                    id = item.key;
                } else {
                    id = parentId + "/" + item.key;
                }

                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("text", URLDecoder.decode(item.value, "UTF-8"));
                object.put("optgroup", optgroup);
                object.put("level", level);

                if (!item.matches || (optgroup && optgroupDisabled)) {
                    object.put("disabled", true);
                }

                array.add(object);

                if (!item.children.isEmpty()) {
                    this.generateVocabularyChildren(items, array, item.children, false, level + 1, id, tree, optgroupDisabled);
                }
            }
        }
    }


    /**
     * Creates Nuxeo controller.
     *
     * @param portletRequest portlet request
     * @param portletResponse portlet response
     * @return Nuxeo controller
     */
    protected NuxeoController createNuxeoController(PortletRequest portletRequest, PortletResponse portletResponse) {
        return new NuxeoController(portletRequest, portletResponse, this.getPortletContext());
    }


    /**
     * Vocabulary item java-bean.
     *
     * @author Cédric Krommenhoek
     */
    private class VocabularyItem {

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
        public VocabularyItem(String key) {
            super();
            this.key = key;
            this.children = new LinkedHashSet<String>();
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
