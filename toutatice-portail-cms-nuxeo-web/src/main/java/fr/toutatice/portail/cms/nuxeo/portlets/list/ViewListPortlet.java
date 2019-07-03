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
package fr.toutatice.portail.cms.nuxeo.portlets.list;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PaginableDocuments;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.sequencing.IPortletSequencingService;
import org.osivia.portal.api.taskbar.ITaskbarService;
import org.osivia.portal.api.taskbar.TaskbarTask;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.*;
import org.osivia.portal.core.context.ControllerContextAdapter;

import bsh.EvalError;
import bsh.Interpreter;
import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PageSelectors;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.ListTemplate;
import fr.toutatice.portail.cms.nuxeo.api.portlet.IPortletModule;
import fr.toutatice.portail.cms.nuxeo.api.portlet.IPrivilegedModule;
import fr.toutatice.portail.cms.nuxeo.api.portlet.ViewList;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;

/**
 * List portlet.
 *
 * @see CMSPortlet
 */
public class ViewListPortlet extends ViewList {

    /** Default request page size. */
    private static final int DEFAULT_REQUEST_PAGE_SIZE = 100;
    /** Default RSS results limit. */
    private static final int DEFAULT_RSS_RESULTS_LIMIT = 10;

    /** Admin JSP path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/list/admin.jsp";

    /** View JSP path. */
    protected static final String PATH_VIEW = "/WEB-INF/jsp/list/view.jsp";

    /** INFINITE_VIEW */
    protected static final String INFINITE_VIEW = "/WEB-INF/jsp/list/view-infinite-scroll.jsp";

    
	private static final String SETS_PROPERTY = "sets:sets";
	private static final String LIST_WEBID_PROPERTY= "webids";
	private static final String NAME_PROPERTY = "name";
	private static final String WEBID_PROPERTY = "ttc:webid";

    /** Bundle factory. */
    private IBundleFactory bundleFactory;
    /** Document DAO. */
    private DocumentDAO documentDAO;
    /** Portlet sequencing service. */
    private IPortletSequencingService portletSequencingService;


    /** Nuxeo service. */
    private final INuxeoService nuxeoService;
    /** Taskbar service. */
    private final ITaskbarService taskbarService;


    /**
     * Constructor.
     */
    public ViewListPortlet() {
        super();

        // Nuxeo service
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        // Taskbar service
        this.taskbarService = Locator.findMBean(ITaskbarService.class, ITaskbarService.MBEAN_NAME);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());

        // Document DAO
        this.documentDAO = DocumentDAO.getInstance();

        // Portlet sequencing service
        this.portletSequencingService = Locator.findMBean(IPortletSequencingService.class, IPortletSequencingService.MBEAN_NAME);
    }


    /**
     * Get current template.
     *
     * @param locale user locale
     * @param configuration configuration
     * @return current template
     */
    public ListTemplate getCurrentTemplate(Locale locale, ListConfiguration configuration) {
        // Customizer
        INuxeoCustomizer customizer = this.nuxeoService.getCMSCustomizer();

        String currentTemplateName = configuration.getTemplate();
        if (currentTemplateName == null) {
            currentTemplateName = LIST_TEMPLATE_NORMAL;
        }

        // Search template
        ListTemplate currentTemplate = null;
        ListTemplate defaultTemplate = null;
        List<ListTemplate> templates = customizer.getListTemplates(locale);
        for (ListTemplate template : templates) {
            if (currentTemplateName.equals(template.getKey())) {
                // Current template
                currentTemplate = template;
                break;
            } else if (LIST_TEMPLATE_NORMAL.equals(template.getKey())) {
                // Default template
                defaultTemplate = template;
            }
        }

        if (currentTemplate == null) {
            currentTemplate = defaultTemplate;
        }

        return currentTemplate;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        try {
            // Portal controller context
            PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);
            // Configuration
            ListConfiguration configuration = this.getConfiguration(window);


            // Template
            ListTemplate template = this.getCurrentTemplate(request.getLocale(), configuration);

            // Module
            IPortletModule module = template.getModule();

            // Request filter
            String filter = null;
            if (module != null && module instanceof IPrivilegedModule) {
                IPrivilegedModule privilegedModule = (IPrivilegedModule) module;
                nuxeoController.setAuthType(privilegedModule.getAuthType());
                nuxeoController.setCacheType(privilegedModule.getCacheType());
                filter = privilegedModule.getFilter(portalControllerContext);
            }

            boolean requestExecution = false;

            PaginableDocuments documents = null;
            int resultsLimit = 100;

            if ("rss".equals(request.getParameter("type"))) {
                resultsLimit = DEFAULT_RSS_RESULTS_LIMIT;
                requestExecution = true;
            }

            if ("true".equals(request.getParameter("injectdocs"))) {
                requestExecution = true;
                String limit = request.getParameter("limit");
                if (limit != null) {
                    resultsLimit = Integer.parseInt(limit);
                } else {
                    if (WindowState.MAXIMIZED.equals(request.getWindowState()) && configuration.getMaximizedPagination() != null) {
                        resultsLimit = configuration.getMaximizedPagination();
                    } else if (configuration.getNormalPagination() != null) {
                        resultsLimit = configuration.getNormalPagination();
                    }
                }
            }


            if (requestExecution) {
                // RSS generation

                // Nuxeo request
                String nuxeoRequest = configuration.getNuxeoRequest();

                if (configuration.isBeanShell()) {
                    // BeanShell interpretation
                    Interpreter interpreter = new Interpreter();
                    interpreter.set("params", PageSelectors.decodeProperties(request.getParameter("selectors")));
                    interpreter.set("request", request);
                    interpreter.set("NXQLFormater", new NXQLFormater(nuxeoController));

                    interpreter.set("basePath", nuxeoController.getBasePath());
                    interpreter.set("spacePath", nuxeoController.getSpacePath());
                    interpreter.set("navigationPath", nuxeoController.getNavigationPath());
                    interpreter.set("contentPath", nuxeoController.getContentPath());
                    interpreter.set("spaceId", null);
                    if (nuxeoController.getNavigationPath() != null) {
                        CMSPublicationInfos navigationPubInfos = NuxeoController.getCMSService().getPublicationInfos(nuxeoController.getCMSCtx(),
                                nuxeoController.getNavigationPath());
                        interpreter.set("navigationPubInfos", navigationPubInfos);
                        interpreter.set("spaceId", navigationPubInfos.getSpaceID());
                    }

                    nuxeoRequest = (String) interpreter.eval(nuxeoRequest);
                }


                if (nuxeoRequest != null) {
                    int currentPage = NumberUtils.toInt(request.getParameter("currentPage"));
                    request.setAttribute("currentPage", currentPage);

                    String schemas = template.getSchemas();

                    // Apply request filter
                    nuxeoRequest = this.applyFilter(nuxeoRequest, filter);

                    // Nuxeo command
                    INuxeoCommand command = new ListCommand(nuxeoRequest, nuxeoController.getDisplayLiveVersion(), currentPage, resultsLimit, schemas,
                            configuration.getContentFilter());
                    ((ListCommand) command).setForceVCS(configuration.isForceVCS());

                    // Nuxeo documents
                    documents = (PaginableDocuments) nuxeoController.executeNuxeoCommand(command);

                    // Result list
                    List<DocumentDTO> documentsDTO = new ArrayList<DocumentDTO>(documents.size());
                    for (Document document : documents) {
                        DocumentDTO documentDTO = this.documentDAO.toDTO(portalControllerContext, document);
                        documentsDTO.add(documentDTO);
                    }
                    request.setAttribute("documents", documentsDTO);
                }
            }

            if ("rss".equals(request.getParameter("type"))) {
                if (documents != null) {
                    // RSS document
                    org.w3c.dom.Document document = RssGenerator.createDocument(nuxeoController, portalControllerContext, configuration.getRssTitle(),
                            documents, configuration.getRssReference());


                    // Send RSS content
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(document);
                    StringWriter stringWriter = new StringWriter();
                    StreamResult streamResult = new StreamResult(stringWriter);
                    transformer.transform(source, streamResult);
                    String xmlString = stringWriter.toString();

                    InputStream in = new ByteArrayInputStream(xmlString.getBytes());

                    ResourceUtil.copy(in, response.getPortletOutputStream(), 4096);

                    response.setContentType("application/rss+xml");
                    response.setProperty("Cache-Control", "max-age=" + response.getCacheControl().getExpirationTime());
                    response.setProperty("Last-Modified", this.formatResourceLastModified());
                } else {
                    throw new IllegalArgumentException("No request defined for RSS");
                }
            } else {
                if (module != null) {
                    // Saved class loader
                    ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(module.getClassLoader());
                    try {
                        module.serveResource(portalControllerContext);
                    } finally {
                        Thread.currentThread().setContextClassLoader(savedClassLoader);
                    }
                }
                if ("loadMore".equals(request.getResourceID())) {
                    includeJsp(request, response, template, module);
                } else if (!response.isCommitted()) {
                    super.serveResource(request, response);
                }
            }
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Génère le html renvoyé dans la resource à l'aide de la jsp du style
     * 
     * @param request
     * @param response
     * @param template
     * @param module
     * @throws PortletException
     * @throws IOException
     */
    private void includeJsp(ResourceRequest request, ResourceResponse response, ListTemplate template, IPortletModule module)
            throws PortletException, IOException {
        String customS = module != null ? "custom/" : StringUtils.EMPTY;
        PortletContext portletContext = module != null ? module.getPortletContext() : this.getPortletContext();
        response.setContentType("text/html");
        String style = StringUtils.lowerCase(template.getKey());
        String viewPath = "/WEB-INF/" + customS + "jsp/list/view-" + style + ".jsp";
        PortletRequestDispatcher dispatcher = portletContext.getRequestDispatcher(viewPath);
        dispatcher.include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
        // Action
        String action = request.getParameter(ActionRequest.ACTION_NAME);
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        if ("admin".equals(request.getPortletMode().toString())) {
            if ("save".equals(action)) {
                // Save action

                // Nuxeo request
                window.setProperty(NUXEO_REQUEST_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("nuxeoRequest")));

                // BeanShell
                window.setProperty(BEAN_SHELL_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("beanShell")));

                // Force request on VCS
                window.setProperty(FORCE_VCS_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("forceVCS")));

                // Version
                window.setProperty(VERSION_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("displayLiveVersion")));

                // Content filter
                window.setProperty(CONTENT_FILTER_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("requestFilteringPolicy")));

                // Scope
                window.setProperty(SCOPE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("scope")));

                // Hide metadata indicator
                boolean displayMetadata = BooleanUtils.toBoolean(request.getParameter("metadataDisplay"));
                window.setProperty(METADATA_WINDOW_PROPERTY, BooleanUtils.toString(displayMetadata, null, "1"));

                // Nuxeo request display
                window.setProperty(NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("nuxeoRequestDisplay")));

                // Results limit
                window.setProperty(RESULTS_LIMIT_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("resultsLimit")));

                // Normal view pagination
                window.setProperty(NORMAL_PAGINATION_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("normalPagination")));

                // Maximized view pagination
                window.setProperty(MAXIMIZED_PAGINATION_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("maximizedPagination")));

                // Template
                window.setProperty(TEMPLATE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("template")));

                // Permalink reference
                window.setProperty(PERMALINK_REFERENCE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("permalinkReference")));

                // RSS reference
                window.setProperty(RSS_REFERENCE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("rssReference")));

                // RSS title
                window.setProperty(RSS_TITLE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("rssTitle")));


                // Parent container path
                window.setProperty(CREATION_PARENT_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("creationParentPath")));

                // Content type
                window.setProperty(CREATION_CONTENT_TYPE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("creationContentType")));
                
                window.setProperty(SETTYPE_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("setType")));
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        }


        // Configuration
        ListConfiguration configuration = this.getConfiguration(window);


        // v2.0.8 : ajout custom
        ListTemplate template = this.getCurrentTemplate(request.getLocale(), configuration);


        // Module
        IPortletModule module = template.getModule();
        if (module != null) {
            // Saved class loader
            ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(module.getClassLoader());
            try {
                module.processAction(nuxeoController.getPortalCtx());
            } finally {
                Thread.currentThread().setContextClassLoader(savedClassLoader);
            }
        }
    }


    /**
     * Admin view display.
     *
     * @param request request
     * @param response response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Customizer
            INuxeoCustomizer customizer = this.nuxeoService.getCMSCustomizer();


            // Configuration
            ListConfiguration configuration = this.getConfiguration(window);
            request.setAttribute("configuration", configuration);

            // Versions
            request.setAttribute("versions", nuxeoController.formatDisplayLiveVersionList(configuration.getVersion()));

            // Content filters
            request.setAttribute("contentFilters", nuxeoController.formatRequestFilteringPolicyList(configuration.getContentFilter()));

            // Scopes
            request.setAttribute("scopes", nuxeoController.formatScopeList(configuration.getScope()));

            // Templates
            request.setAttribute("templates", customizer.getListTemplates(request.getLocale()));
            
            //Set types
            request.setAttribute("setTypes", customizer.getSetTypes());


            response.setContentType("text/html");

            PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(getPathAdmin());
            dispatcher.include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
        String pathView = PATH_VIEW;
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
            // Portal controller context
            PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);
            // Configuration
            ListConfiguration configuration = this.getConfiguration(window);
            // Bundle
            Bundle bundle = this.bundleFactory.getBundle(request.getLocale());

            // Customizer
            INuxeoCustomizer customizer = this.nuxeoService.getCMSCustomizer();

            // Template
            ListTemplate template = this.getCurrentTemplate(request.getLocale(), configuration);

            // Module
            IPortletModule module = template.getModule();

            // Request filter
            String filter = null;

            if (module instanceof IPrivilegedModule) {
                IPrivilegedModule privilegedModule = (IPrivilegedModule) module;
                nuxeoController.setAuthType(privilegedModule.getAuthType());
                nuxeoController.setCacheType(privilegedModule.getCacheType());
                filter = privilegedModule.getFilter(portalControllerContext);
            }

            // Feed mode
            boolean feed = BooleanUtils.toBoolean(window.getProperty("osivia.cms.feed"));
            Map<String, Integer> feedDocumentsOrder = null;

            // webid ordering mode
            boolean webidOrdering = BooleanUtils.toBoolean(window.getProperty(ViewList.WEBID_ORDERING_WINDOW_PROPERTY));
            Map<String, Integer> webidOrder = null;
            if (webidOrdering) {
                int size = NumberUtils.toInt(window.getProperty(ViewList.WEBID_ORDERING_SIZE_WINDOW_PROPERTY));
                webidOrder = new HashMap<String, Integer>(size);
                for (int i = 0; i < size; i++) {
                    webidOrder.put(window.getProperty(ViewList.WEBID_ORDERING_WINDOW_PROPERTY + "." + i), i);
                }

            }

            // Nuxeo request
            String nuxeoRequest;
            if (feed) {
                int size = NumberUtils.toInt(window.getProperty("osivia.cms.news.size"));
                feedDocumentsOrder = new HashMap<String, Integer>(size);

                StringBuilder builder = new StringBuilder();
                builder.append("return \"");

                for (int i = 0; i < size; i++) {
                    if (i > 0) {
                        builder.append(" OR ");
                    }

                    // Path
                    String path = window.getProperty("osivia.cms.news." + i + ".docURI");
                    feedDocumentsOrder.put(path, NumberUtils.toInt(window.getProperty("osivia.cms.news." + i + ".order")));
                    builder.append("ecm:path = '").append(path).append("'");
                }

                builder.append("\";");

                nuxeoRequest = builder.toString();
            } else {
                nuxeoRequest = configuration.getNuxeoRequest();
            }


            // BeanShell
            if (configuration.isBeanShell()) {
                String orginalRequest = nuxeoRequest;

                nuxeoRequest = this.beanShellInterpretation(nuxeoController, nuxeoRequest);

                /*
                 * many request (almost templates) generate null values which are not accepted bu Nuxeo
                 * It's due to the fact that they expect to be run in contextualized mode
                 * Instead of generatig an exception, it's better to return a null value
                 */
                if ((nuxeoRequest != null) && nuxeoRequest.contains("'null")) {
                    // Is it a contextualization error
                    if (nuxeoController.getBasePath() == null) {
                        if (orginalRequest.contains("basePath") || orginalRequest.contains("domainPath") || orginalRequest.contains("spacePath")
                                || orginalRequest.contains("navigationPath")) {
                            request.setAttribute("error", "La requête ne peut pas être interprétée en mode template : valeur(s) à 'null' ");
                        }
                    }
                }
            }

            //Sets
            boolean sets = StringUtils.isNotEmpty(configuration.getSetType());
            Document workspace = null;
            List<Object> setsWebidList = null;
            if (sets)
    		{
            	if (nuxeoRequest == null) nuxeoRequest = "";
                // CMS base path
                String basePath = nuxeoController.getBasePath();
                if (basePath == null) {
                    workspace =  null;
                } else
                {
    	            // Nuxeo document context
    	            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(basePath);
    	
    	            // get workspace document
    	            workspace = documentContext.getDocument();
                }

                if (workspace != null)
                {
                	PropertyList list = (PropertyList) workspace.getProperties().get(SETS_PROPERTY);
                	if (list != null && list.list().size() >0)
                	{
                		for (Object map : list.list())
                		{
                			if (StringUtils.equals(configuration.getSetType(), ((PropertyMap) map).getString(NAME_PROPERTY)))
                			{
                				PropertyList propertyListWebId = ((PropertyMap) map).getList(LIST_WEBID_PROPERTY);
                				setsWebidList = propertyListWebId.list();
                				break;
                			}
                		}
                	}
                	String workspacePath = workspace.getPath();
                	nuxeoRequest = this.addSetClause(nuxeoRequest, setsWebidList, workspacePath);
                } else
                {
                	//Add a clause to get no document
                	nuxeoRequest += "ecm:currentLifeCycleState <> 'deleted' and ecm:currentLifeCycleState = 'deleted' ";
                }
    		}

            // Apply request filter
            nuxeoRequest = this.applyFilter(nuxeoRequest, filter);


            if ("EMPTY_REQUEST".equals(nuxeoRequest)) {
                request.setAttribute("osivia.emptyResponse", "1");
                request.setAttribute("error", bundle.getString("LIST_MESSAGE_EMPTY_REQUEST"));
            } else if (nuxeoRequest != null) {
                // Nuxeo request
                if (configuration.isNuxeoRequestDisplay()) {
                    request.setAttribute("nuxeoRequest", nuxeoRequest);
                }


                // CMS path
                String cmsPath = request.getParameter("osivia.cms.path");


                // Selectors
                String selectors = request.getParameter("selectors");
                String lastSelectors = request.getParameter("lastSelectors");

                if ("".equals(selectors)) {
                    selectors = null;
                }


                // On render url, it is not possible to clear a parameter value (old value keeps unchanged)
                // So put __NONE__
                if ("__NONE__".equals(lastSelectors)) {
                    lastSelectors = null;
                }

                request.setAttribute("lastSelectors", StringUtils.isEmpty(selectors) ? "__NONE__" : selectors);

                // Results limit
                int resultsLimit;
                if (configuration.getResultsLimit() == null) {
                    resultsLimit = -1;
                } else {
                    resultsLimit = configuration.getResultsLimit();
                }


                // Pagination
                int pageSize = -1;
                int currentPage = 0;
                Integer pageSizeProperty;
                if (WindowState.MAXIMIZED.equals(request.getWindowState())) {
                    pageSizeProperty = configuration.getMaximizedPagination();
                } else {
                    pageSizeProperty = configuration.getNormalPagination();
                }
                if (pageSizeProperty != null) {
                    pageSize = pageSizeProperty;

                    String currentStateParameter = request.getParameter("currentState");
                    String currentPageParameter = request.getParameter("currentPage");

                    // Current page is reset on mode change
                    boolean modeChanged = !request.getWindowState().toString().equals(currentStateParameter);
                    // Current page is reset on selectors change
                    boolean selectorsChanged = (((selectors != null) && !selectors.equals(lastSelectors)) || ((selectors == null) && (lastSelectors != null)));

                    if (!((currentPageParameter == null) || modeChanged || selectorsChanged)) {
                        currentPage = NumberUtils.toInt(currentPageParameter);
                    }
                }
                request.setAttribute("currentPage", currentPage);


                // Templates
                request.setAttribute("templates", customizer.getListTemplates(request.getLocale()));

                request.setAttribute("style", StringUtils.lowerCase(template.getKey()));
                String schemas = template.getSchemas();

                // Request page size
                int requestPageSize = DEFAULT_REQUEST_PAGE_SIZE;
                if (pageSize > 0) {
                    requestPageSize = pageSize;
                }
                if ((resultsLimit > 0) && (currentPage == 0)) {
                    // Limit size on first page
                    requestPageSize = Math.min(requestPageSize, resultsLimit);
                }


                // Nuxeo command
                INuxeoCommand command = new ListCommand(nuxeoRequest, nuxeoController.getDisplayLiveVersion(), currentPage, requestPageSize, schemas,
                        configuration.getContentFilter());
                ((ListCommand) command).setForceVCS(configuration.isForceVCS());

                // Nuxeo documents
                PaginableDocuments documents = (PaginableDocuments) nuxeoController.executeNuxeoCommand(command);

                // Nuxeo documents sorted list
                List<Document> documentsList = documents.list();
                if (feed) {
                    DocumentPathComparator comparator = new DocumentPathComparator(feedDocumentsOrder);
                    Collections.sort(documentsList, comparator);
                } else if (webidOrdering) {
                    DocumentWebidComparator comparator = new DocumentWebidComparator(webidOrder);
                    Collections.sort(documentsList, comparator);
                }
                
                // Result list
                List<DocumentDTO> documentsDTO = new ArrayList<DocumentDTO>(documentsList.size());
                for (Document document : documentsList) {
                    DocumentDTO documentDTO = this.documentDAO.toDTO(portalControllerContext, document);
                    documentsDTO.add(documentDTO);
                }

                //If sets : order the documentsDTO list
                if (sets)
                {
                	Map<Object, DocumentDTO> mapSets = new HashMap<Object, DocumentDTO>();

                	//The result list is not sorted because of the 'in' clause
                	//First put dto in a hashmap
                	if (documentsDTO != null)
                	{
                		for (DocumentDTO dto: documentsDTO)
                		{
                			mapSets.put(dto.getProperties().get(WEBID_PROPERTY), dto);
                		}
                	}
                	//Then add dto in a list in the order of the listwebid
                	if (workspace != null && setsWebidList != null)
                	{
                		documentsDTO = new ArrayList<DocumentDTO>(documentsDTO == null? 0 : documentsDTO.size());
                		for (Object webid: setsWebidList)
                		{
                			if (mapSets.get(webid)!=null) documentsDTO.add(mapSets.get(webid));
                		}
                	}
                }
                
                request.setAttribute("documents", documentsDTO);


                // Pages count
                int pagesCount = 0;
                if (pageSize > 0) {
                    int limit;
                    if (resultsLimit > 0) {
                        limit = Math.min(documents.getTotalSize(), resultsLimit);

                        // Remove excess results
                        if (documents.size() < ((currentPage + 1) * requestPageSize)) {
                            int lastPageLimit = Math.max(0, resultsLimit - (currentPage * requestPageSize));
                            while (documents.size() > lastPageLimit) {
                                // Remove last item
                                documentsList.remove(documents.size() - 1);
                            }
                        }
                    } else {
                        limit = documents.getTotalSize();
                    }
                    pagesCount = ((limit - 1) / pageSize) + 1;
                }
                request.setAttribute("nbPages", pagesCount);


                // Total size
                request.setAttribute("totalSize", documents.getTotalSize());


                // Permalink
                if (StringUtils.isNotBlank(configuration.getPermalinkReference())) {
                    String reference = configuration.getPermalinkReference();
                    Map<String, String> parameters = new HashMap<String, String>();

                    if (selectors != null) {
                        Map<String, List<String>> decodedSelectors = PageSelectors.decodeProperties(selectors);
                        decodedSelectors.remove("selectorChanged");
                        parameters.put("selectors", PageSelectors.encodeProperties(decodedSelectors));
                    }

                    String type;
                    if (cmsPath == null) {
                        type = IPortalUrlFactory.PERM_LINK_TYPE_PAGE;
                    } else {
                        type = IPortalUrlFactory.PERM_LINK_TYPE_CMS;
                        reference = null;
                    }

                    String url = nuxeoController.getPortalUrlFactory().getPermaLink(portalControllerContext, reference, parameters, cmsPath, type);
                    request.setAttribute("permaLinkURL", url);
                }


                // RSS
                if (StringUtils.isNotBlank(configuration.getRssReference())) {
                    boolean anonymousAccess = true;

                    if (cmsPath != null) {
                        // Check if navigation folder is accessible in anonymous mode
                        CMSServiceCtx cmsContext = new CMSServiceCtx();
                        cmsContext.setControllerContext(ControllerContextAdapter.getControllerContext(portalControllerContext));
                        cmsContext.setScope(nuxeoController.getScope());

                        anonymousAccess = NuxeoController.getCMSService().checkContentAnonymousAccess(cmsContext, cmsPath);
                    }

                    if (anonymousAccess) {
                        Map<String, String> publicParams = new HashMap<String, String>();
                        if (selectors != null) {
                            // Selectors
                            Map<String, List<String>> selectorsMap = PageSelectors.decodeProperties(selectors);

                            selectorsMap.remove("selectorChanged");
                            publicParams.put("selectors", PageSelectors.encodeProperties(selectorsMap));
                        }

                        String url = nuxeoController.getPortalUrlFactory().getPermaLink(portalControllerContext, configuration.getRssReference(), publicParams,
                                cmsPath, IPortalUrlFactory.PERM_LINK_TYPE_RSS);
                        request.setAttribute("rssLinkURL", url);
                    }
                }


                // Creation item, if parameters are given
                String dynamicPath = window.getProperty(CREATION_PARENT_PATH_WINDOW_PROPERTY);
                if (dynamicPath == null) {
                    dynamicPath = window.getProperty(Constants.WINDOW_PROP_URI);
                }
                if (dynamicPath != null) {
                    dynamicPath = NuxeoController.getLivePath(nuxeoController.getComputedPath(dynamicPath));
                    NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(dynamicPath);
                    Document folder = documentContext.getDocument();
                    nuxeoController.setCurrentDoc(folder);
                    response.setTitle(folder.getTitle());
                }

                nuxeoController.insertContentMenuBarItems();


                // Empty response indicator
                if ((currentPage == 0) && (documents.size() == 0)) {
                    request.setAttribute("osivia.emptyResponse", "1");
                }


                if (module != null) {
                    // Saved class loader
                    ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(module.getClassLoader());
                    try {
                        module.doView(portalControllerContext);
                    } finally {
                        Thread.currentThread().setContextClassLoader(savedClassLoader);
                    }
                }
            } else {
                String bshTitle = (String) request.getAttribute("bsh.title");
                String bshHTML = (String) request.getAttribute("bsh.html");
                if (StringUtils.isNotEmpty(bshTitle) || StringUtils.isNotEmpty(bshHTML)) {
                    if (StringUtils.isNotEmpty(bshTitle)) {
                        response.setTitle(bshTitle);
                    }
                    if (StringUtils.isNotEmpty(bshHTML)) {
                        response.setContentType("text/html");

                        response.getWriter().print(bshHTML);
                        response.getWriter().close();
                    }
                    return;
                } else {
                    request.setAttribute("error", "Requête non définie");
                }
            }
            if (configuration.isInfiniteScroll()) {
                pathView = INFINITE_VIEW;
            }
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (EvalError e) {
            request.setAttribute("error", "LIST_MESSAGE_INVALID_REQUEST");
            request.setAttribute("errorMessage", e.getMessage());
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }

        response.setContentType("text/html");


        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(pathView);
        dispatcher.include(request, response);
    }


    /**
     * BeanShell interpretation.
     *
     * @param nuxeoController Nuxeo controller
     * @param nuxeoRequest Nuxeo request
     * @return interpreted request
     * @throws EvalError
     * @throws CMSException
     */
    private String beanShellInterpretation(NuxeoController nuxeoController, String nuxeoRequest) throws EvalError, CMSException {
        // Request
        PortletRequest request = nuxeoController.getRequest();
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

        // User workspace
        CMSItem userWorkspace = cmsService.getUserWorkspace(cmsContext);

        // BeanShell
        Interpreter interpreter = new Interpreter();
        interpreter.set("params", PageSelectors.decodeProperties(request.getParameter("selectors")));
        interpreter.set("basePath", nuxeoController.getBasePath());
        interpreter.set("domainPath", nuxeoController.getDomainPath());
        interpreter.set("spacePath", nuxeoController.getSpacePath());
        interpreter.set("navigationPath", nuxeoController.getNavigationPath());
        if (userWorkspace != null) {
            interpreter.set("userWorkspacePath", userWorkspace.getCmsPath());
        }

        // Initialization to avoid undefined errors when request building with with var
        interpreter.set("navigationPubInfos", null);
        interpreter.set("spaceId", null);
        if (StringUtils.startsWith(nuxeoController.getContentPath(), nuxeoController.getNavigationPath()) && nuxeoController.getNavigationPath() != null) {
            CMSPublicationInfos navigationPubInfos = NuxeoController.getCMSService().getPublicationInfos(nuxeoController.getCMSCtx(),
                    nuxeoController.getNavigationPath());
            interpreter.set("navigationPubInfos", navigationPubInfos);
            interpreter.set("spaceId", navigationPubInfos.getSpaceID());
        }

        interpreter.set("contentPath", nuxeoController.getContentPath());
        interpreter.set("request", request);
        interpreter.set("NXQLFormater", new NXQLFormater(nuxeoController));
        interpreter.set("navItem", nuxeoController.getNavigationItem());

        // Storage attributes
        interpreter.set("storage", this.portletSequencingService.getAttributes(nuxeoController.getPortalCtx()));

        // Task path
        String taskPath;
        String taskId = window.getProperty(ITaskbarService.LINKED_TASK_ID_WINDOW_PROPERTY);
        if (StringUtils.isEmpty(taskId)) {
            taskPath = null;
        } else {
            // Linked task
            TaskbarTask linkedTask = null;

            try {
                // Tasks
                List<TaskbarTask> tasks = this.taskbarService.getTasks(nuxeoController.getPortalCtx(), nuxeoController.getSpacePath());
                for (TaskbarTask task : tasks) {
                    if (taskId.equals(task.getId())) {
                        linkedTask = task;
                        break;
                    }
                }
            } catch (PortalException e) {
                // Do nothing
            }

            if (linkedTask == null) {
                taskPath = null;
            } else {
                taskPath = linkedTask.getPath();
            }
        }
        interpreter.set("taskPath", taskPath);

        return (String) interpreter.eval(nuxeoRequest);
    }

    /**
     * Add set clause to get documents in which webids are in listWebid
     * @param request request
     * @param listWebid list of webid
     * @return 
     */
    private String addSetClause(String request, List<Object> listWebid, String workspacePath)
    {
    	StringBuilder clause = new StringBuilder();
    	clause.append(request);
    	clause.append("ecm:path startswith '").append(workspacePath).append("' ");
    	clause.append("and ecm:currentLifeCycleState <> 'deleted' ");
    	clause.append(" and ttc:webid in (");
        if (listWebid != null && listWebid.size() > 0)
        {
        	Iterator<Object> it = listWebid.iterator();
        	boolean first = true;
        	while (it.hasNext())
        	{
        		if (!first) clause.append(",");
        		clause.append("'").append(it.next()).append("'");
        		first = false;
        	}
        } else
        {
        	clause.append("''");
        }
        clause.append(") ");
        return clause.toString();
    }
    
    
    /**
     * Apply request filter.
     * 
     * @param request original request
     * @param filter request filter
     * @return filtered request
     */
    private String applyFilter(String request, String filter) {
        String result;

        if (StringUtils.isBlank(filter)) {
            result = request;
        } else {
            Pattern pattern = Pattern.compile("^(.*) ORDER *BY (.*)$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(request);

            StringBuilder builder = new StringBuilder();
            builder.append("(").append(filter).append(") AND (");

            if (matcher.matches()) {
                builder.append(matcher.group(1)).append(") ORDER BY ").append(matcher.group(2));
            } else {
                builder.append(request).append(")");
            }

            result = builder.toString();
        }

        return result;
    }


    /**
     * Get list configuration.
     *
     * @param window portal window
     * @return list configuration
     */
    protected ListConfiguration getConfiguration(PortalWindow window) {
        ListConfiguration configuration = new ListConfiguration();

        // Nuxeo request
        configuration.setNuxeoRequest(window.getProperty(NUXEO_REQUEST_WINDOW_PROPERTY));

        // Bean Shell interpretation
        configuration.setBeanShell(BooleanUtils.toBoolean(window.getProperty(BEAN_SHELL_WINDOW_PROPERTY)));

        // Fore VCS request (database request)
        configuration.setForceVCS(BooleanUtils.toBoolean(window.getProperty(FORCE_VCS_WINDOW_PROPERTY)));

        // Version
        configuration.setVersion(window.getProperty(VERSION_WINDOW_PROPERTY));

        // Content filter
        configuration.setContentFilter(window.getProperty(CONTENT_FILTER_WINDOW_PROPERTY));

        // Scope
        configuration.setScope(window.getProperty(SCOPE_WINDOW_PROPERTY));

        // Metadata display
        configuration.setMetadataDisplay(BooleanUtils.toBoolean(window.getProperty(METADATA_WINDOW_PROPERTY), null, "1"));

        // Nuxeo request display
        configuration.setNuxeoRequestDisplay(BooleanUtils.toBoolean(window.getProperty(NUXEO_REQUEST_DISPLAY_WINDOW_PROPERTY)));

        // Results limit
        configuration.setResultsLimit(NumberUtils.createInteger(StringUtils.trimToNull(window.getProperty(RESULTS_LIMIT_WINDOW_PROPERTY))));

        // Normal view pagination
        configuration.setNormalPagination(NumberUtils.createInteger(StringUtils.trimToNull(window.getProperty(NORMAL_PAGINATION_WINDOW_PROPERTY))));

        // Maximized view pagination
        configuration.setMaximizedPagination(NumberUtils.createInteger(StringUtils.trimToNull(window.getProperty(MAXIMIZED_PAGINATION_WINDOW_PROPERTY))));

        // Infinite scroll
        configuration.setInfiniteScroll(BooleanUtils.toBoolean(window.getProperty(INFINITE_SCROLL_WINDOW_PROPERTY)));

        // Template
        configuration.setTemplate(window.getProperty(TEMPLATE_WINDOW_PROPERTY));

        // Permalink reference
        configuration.setPermalinkReference(window.getProperty(PERMALINK_REFERENCE_WINDOW_PROPERTY));

        // RSS reference
        configuration.setRssReference(window.getProperty(RSS_REFERENCE_WINDOW_PROPERTY));

        // RSS title
        configuration.setRssTitle(window.getProperty(RSS_TITLE_WINDOW_PROPERTY));

        // Parent container path
        configuration.setCreationParentPath(window.getProperty(CREATION_PARENT_PATH_WINDOW_PROPERTY));

        // Content type
        configuration.setCreationContentType(window.getProperty(CREATION_CONTENT_TYPE_WINDOW_PROPERTY));
        
        //Set type
        configuration.setSetType(window.getProperty(SETTYPE_WINDOW_PROPERTY));


        return configuration;
    }

    /**
     * Getter for PATH_ADMIN.
     * 
     * @return the pathAdmin
     */
    public String getPathAdmin() {
        return PATH_ADMIN;
    }

}
