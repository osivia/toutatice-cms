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
package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.portal.theme.ThemeConstants;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.IEcmCommandervice;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSExtendedDocumentInfos;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.ContextualizationHelper;
import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoCompatibility;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentAttachmentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.RemotePublishedDocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceInvocationHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.CommentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.RemotePublishedDocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.avatar.AvatarServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.binaries.BinaryServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.GetCommentsCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.helpers.DocumentConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.forms.FormsServiceImpl;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.site.SitePictureServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.thumbnail.ThumbnailServlet;
import fr.toutatice.portail.cms.nuxeo.service.tag.NuxeoTagService;

/**
 * View Nuxeo document portlet.
 *
 * @see CMSPortlet
 */
public class ViewDocumentPortlet extends CMSPortlet {

    /** Path window property name. */
    public static final String PATH_WINDOW_PROPERTY = Constants.WINDOW_PROP_URI;
    /** Display only description indicator window property name. */
    public static final String ONLY_DESCRIPTION_WINDOW_PROPERTY = "osivia.document.onlyDescription";
    /** Display only remote sections indicator window property name. */
    public static final String ONLY_REMOTE_SECTIONS_WINDOW_PROPERTY = "osivia.document.onlyRemoteSections";
    /** Indicates if portlet is in remote sections list page. */
    public static final String REMOTE_SECTIONS_PAGE_WINDOW_PROPERTY = "osivia.document.remoteSectionsPage";
    /** Hide metadata indicator window property name. */
    public static final String HIDE_METADATA_WINDOW_PROPERTY = InternalConstants.METADATA_WINDOW_PROPERTY;
    /** Hide attachment indicator window property name. */
    public static final String HIDE_ATTACHMENTS_WINDOW_PROPERTY = "osivia.document.hideAttachments";
    /** Admin path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/document/admin.jsp";
    /** View path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/document/view.jsp";


    /** Nuxeo service. */
    private INuxeoService nuxeoService;


    /** Document DAO. */
    private final DocumentDAO documentDao;
    /** Document comment DAO. */
    private final CommentDAO commentDao;
    /** Document published documents DAO. */
    private final RemotePublishedDocumentDAO publishedDocumentsDao;


    /**
     * Constructor.
     */
    public ViewDocumentPortlet() {
        super();

        // DAO
        this.documentDao = DocumentDAO.getInstance();
        this.commentDao = CommentDAO.getInstance();
        this.publishedDocumentsDao = RemotePublishedDocumentDAO.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Portlet context
        PortletContext portletContext = this.getPortletContext();

        try {
            // Nuxeo service
            this.nuxeoService = (INuxeoService) portletContext.getAttribute("NuxeoService");
            if (this.nuxeoService == null) {
                throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
            }

            // CMS service
            CMSService cmsService = new CMSService(portletContext);
            ICMSServiceLocator cmsLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
            cmsLocator.register(cmsService);

            // CMS customizer
            CMSCustomizer customizer = new CMSCustomizer(portletContext, cmsService);
            cmsService.setCustomizer(customizer);
            this.nuxeoService.registerCMSCustomizer(customizer);

            // Nuxeo tag service
            INuxeoTagService tagService = new NuxeoTagService();
            this.registerService(this.nuxeoService.getTagService(), tagService);

            // Forms service
            FormsServiceImpl formsService = new FormsServiceImpl(customizer);
            this.registerService(this.nuxeoService.getFormsService(), formsService);

            // ECM command services
            IEcmCommandervice ecmCmdService = Locator.findMBean(IEcmCommandervice.class, IEcmCommandervice.MBEAN_NAME);

            for (EcmCommand command : customizer.getEcmCommands().values()) {
                ecmCmdService.registerCommand(command.getCommandName(), command);
            }


            // v1.0.16
            ThumbnailServlet.setPortletContext(portletContext);
            SitePictureServlet.setPortletContext(portletContext);
            AvatarServlet.setPortletContext(portletContext);
            BinaryServlet.setPortletContext(portletContext);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Register service.
     *
     * @param proxy proxy
     * @param instance instance
     */
    private void registerService(Object proxy, Object instance) {
        NuxeoServiceInvocationHandler invocationHandler = (NuxeoServiceInvocationHandler) Proxy.getInvocationHandler(proxy);
        invocationHandler.setInstance(instance);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save action

                // Path
                String path = request.getParameter("path");
                window.setProperty(PATH_WINDOW_PROPERTY, path);

                // Display only description indicator
                String onlyDescription = request.getParameter("onlyDescription");
                window.setProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY, onlyDescription);

                // Hide metadata indicator
                boolean displayMetadata = BooleanUtils.toBoolean(request.getParameter("metadata"));
                window.setProperty(HIDE_METADATA_WINDOW_PROPERTY, BooleanUtils.toString(displayMetadata, null, "1"));

                // Hide attachments indicator
                boolean displayAttachments = BooleanUtils.toBoolean(request.getParameter("attachments"));
                window.setProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY, BooleanUtils.toString(displayAttachments, null, "1"));
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);
        } else if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            // Comment action
            this.processCommentAction(request, response);
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
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Path
        String path = window.getProperty(PATH_WINDOW_PROPERTY);
        request.setAttribute("path", path);

        // Display only description indicator
        boolean onlyDescription = BooleanUtils.toBoolean(window.getProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY));
        request.setAttribute("onlyDescription", onlyDescription);

        // Display metadata indicator
        boolean metadata = BooleanUtils.toBoolean(window.getProperty(HIDE_METADATA_WINDOW_PROPERTY), null, "1");
        request.setAttribute("metadata", metadata);

        // Display attachments indicator
        boolean attachments = BooleanUtils.toBoolean(window.getProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY), null, "1");
        request.setAttribute("attachments", attachments);

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher(PATH_ADMIN).include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
            // CMS service
            ICMSService cmsService = NuxeoController.getCMSService();
            // CMS context
            CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();

            // Current window
            PortalWindow window = WindowFactory.getWindow(request);

            // Theme
            String theme = window.getPageProperty(ThemeConstants.PORTAL_PROP_THEME);
            request.setAttribute("theme", theme);

            // Path
            String path = window.getProperty(PATH_WINDOW_PROPERTY);
            // Computed path
            path = nuxeoController.getComputedPath(path);

            if (StringUtils.isNotBlank(path)) {
                // Document context
                NuxeoDocumentContext documentContext = NuxeoController.getDocumentContext(request, response, this.getPortletContext(), path);

                // Maximized indicator
                boolean maximized = WindowState.MAXIMIZED.equals(request.getWindowState());

                // Display only remote sections indicator
                boolean onlyRemoteSections = BooleanUtils.toBoolean(window.getProperty(ONLY_REMOTE_SECTIONS_WINDOW_PROPERTY));
                request.setAttribute("onlyRemoteSections", onlyRemoteSections);

                // Remote sections page indicator
                boolean remoteSectionsPage = BooleanUtils.toBoolean(window.getProperty(REMOTE_SECTIONS_PAGE_WINDOW_PROPERTY));
                request.setAttribute("remoteSectionsPage", remoteSectionsPage);

                // Display only description indicator
                boolean onlyDescription = BooleanUtils.toBoolean(window.getProperty(ONLY_DESCRIPTION_WINDOW_PROPERTY));
                if (!maximized) {
                    request.setAttribute("onlyDescription", onlyDescription);
                }

                // Display metadata indicator
                boolean metadata = BooleanUtils.toBoolean(window.getProperty(HIDE_METADATA_WINDOW_PROPERTY), null, "1");
                request.setAttribute("metadata", metadata);

                // Display attachments indicator
                boolean attachments = BooleanUtils.toBoolean(window.getProperty(HIDE_ATTACHMENTS_WINDOW_PROPERTY), null, "1");
                request.setAttribute("attachments", attachments);

                // Document
                Document document = documentContext.getDoc();
                nuxeoController.setCurrentDoc(document);

                // View dispatched JSP
                String dispatchJsp = window.getProperty("osivia.document.dispatch.jsp");
                if (StringUtils.isEmpty(dispatchJsp)) {
                    dispatchJsp = this.getDispatchJspName(nuxeoController, document, false);
                }
                request.setAttribute("dispatchJsp", dispatchJsp);

                // View dispatched extra JSP
                String dispatchExtraJsp = window.getProperty("osivia.document.dispatch.extra.jsp");
                if (StringUtils.isEmpty(dispatchExtraJsp)) {
                    dispatchExtraJsp = this.getDispatchJspName(nuxeoController, document, true);
                }
                request.setAttribute("dispatchExtraJsp", dispatchExtraJsp);

                // DTO
                DocumentDTO documentDto = this.documentDao.toDTO(document);
                request.setAttribute("document", documentDto);

                // Title
                String title = document.getTitle();
                if (StringUtils.isNotBlank(title)) {
                    response.setTitle(title);
                }

                if (onlyRemoteSections && maximized) {
                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, document, documentDto, true);
                } else if (!onlyDescription || maximized) {
                    // Insert content menubar items
                    nuxeoController.insertContentMenuBarItems();

                    // Attachments
                    this.generateAttachments(nuxeoController, document, documentDto);

                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, document, documentDto, false);

                    if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                        // Publication informations
                        CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);
                        // Extended document informations
                        CMSExtendedDocumentInfos extendedDocumentInfos = cmsService.getExtendedDocumentInfos(cmsContext, document.getPath());

                        // Validation state
                        this.addValidationState(document, documentDto, extendedDocumentInfos);

                        // Comments
                        boolean commentsEnabled = this.areCommentsEnabled(cmsService, publicationInfos, cmsContext);
                        if (commentsEnabled && publicationInfos.isCommentableByUser()) {
                            documentDto.setCommentable(true);

                            // Comments
                            this.generateComments(nuxeoController, document, documentDto);
                        }

                        // Nuxeo Drive
                        this.handleDriveEdition(nuxeoController.getPortalCtx(), document, documentDto, publicationInfos);
                    }
                }
            }

            response.setContentType("text/html");
            this.getPortletContext().getRequestDispatcher(PATH_VIEW).include(request, response);
        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Get dispatch JSP name.
     *
     * @param nuxeoController Nuxeo controller
     * @param document Nuxeo document
     * @param extra dispatch extra JSP indicator
     * @return JSP name
     * @throws CMSException
     */
    private String getDispatchJspName(NuxeoController nuxeoController, Document document, boolean extra) throws CMSException {
        // CMS customizer
        INuxeoCustomizer customizer = nuxeoController.getNuxeoCMSService().getCMSCustomizer();
        // Portlet request
        PortletRequest request = nuxeoController.getRequest();

        // Document type
        String type = StringUtils.lowerCase(document.getType());

        // JSP path
        StringBuilder path = new StringBuilder();
        path.append("/WEB-INF/jsp/document/view-");
        path.append(type);
        if (extra) {
            path.append("-extra");
        }
        path.append(".jsp");
        // JSP name
        String name = customizer.getJSPName(path.toString(), this.getPortletContext(), request);
        // JSP real path
        String realPath = this.getPortletContext().getRealPath(name);
        // JSP file
        File file = new File(realPath);

        // Dispatch JSP name
        String dispatchJspName;

        if (file.exists()) {
            dispatchJspName = type;
        } else {
            dispatchJspName = "default";
        }

        return dispatchJspName;
    }


    /**
     * Generate document attachments.
     *
     * @param nuxeoController Nuxeo controller
     * @param document Nuxeo document
     * @param documentDTO document DTO
     */
    private void generateAttachments(NuxeoController nuxeoController, Document document, DocumentDTO documentDTO) {
        List<DocumentAttachmentDTO> attachments = documentDTO.getAttachments();
        PropertyList files = document.getProperties().getList("files:files");
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                PropertyMap map = files.getMap(i);

                DocumentAttachmentDTO attachment = new DocumentAttachmentDTO();

                // Attachment name
                String name = map.getString("filename");
                attachment.setName(name);

                // Attachement URL
                String url = nuxeoController.createAttachedFileLink(document.getPath(), String.valueOf(i));
                attachment.setUrl(url);

                attachments.add(attachment);
            }
        }
    }


    /**
     * Add document validation state.
     *
     * @param document Nuxeo document
     * @param documentDTO document DTO
     * @param extendedDocumentInfos extended document informations
     */
    private void addValidationState(Document document, DocumentDTO documentDTO, CMSExtendedDocumentInfos extendedDocumentInfos) {
        // Validation state internationalization key
        String key;
        // Validation state icon
        String icon;
        // Validation state color
        String color;

        if (extendedDocumentInfos.isValidationWorkflowRunning()) {
            // Validation in progress
            key = "DOCUMENT_STATE_VALIDATION_IN_PROGRESS";
            icon = "glyphicons glyphicons-hourglass";
            color = "info";
        } else if (DocumentConstants.APPROVED_DOC_STATE.equals(document.getState())) {
            // Valid
            key = "DOCUMENT_STATE_VALID";
            icon = "glyphicons glyphicons-ok";
            color = "success";
        } else {
            key = null;
            icon = null;
            color = null;
        }

        if (key != null) {
            // Validation state map
            Map<String, String> validationState = new HashMap<>();
            validationState.put("key", key);
            validationState.put("icon", icon);
            validationState.put("color", color);

            documentDTO.getProperties().put("validationState", validationState);
        }
    }


    /**
     * Generate document comments.
     *
     * @param nuxeoController Nuxeo controller
     * @param document Nuxeo document
     * @param documentDTO document DTO
     */
    private void generateComments(NuxeoController nuxeoController, Document document, DocumentDTO documentDTO) {
        INuxeoCommand getCommentsCommand = new GetCommentsCommand(document);
        JSONArray jsonComments = (JSONArray) nuxeoController.executeNuxeoCommand(getCommentsCommand);

        for (int i = 0; i < jsonComments.size(); i++) {
            JSONObject jsonComment = jsonComments.getJSONObject(i);
            CommentDTO commentDTO = this.commentDao.toDTO(jsonComment);
            documentDTO.getComments().add(commentDTO);
        }
    }

    /**
     * @param cmsService
     * @param publicationInfos
     * @param cmsContext
     * @return true if publish space of document
     *         allows comments
     * @throws CMSException
     */
    protected boolean areCommentsEnabled(ICMSService cmsService, CMSPublicationInfos publicationInfos, CMSServiceCtx cmsContext) throws CMSException {
        boolean enable = true;

        String publishSpacePath = publicationInfos.getPublishSpacePath();
        if (StringUtils.isBlank(publishSpacePath)) {
            /* Case where currentDoc is PublishSpace */
            publishSpacePath = publicationInfos.getDocumentPath();
        }

        if (StringUtils.isNotBlank(publishSpacePath)) {
            CMSItem spaceConfig = cmsService.getSpaceConfig(cmsContext, publishSpacePath);

            Document space = (Document) spaceConfig.getNativeItem();
            boolean isPublishSpace = (space.getFacets() != null) && (space.getFacets().list().contains(CommandConstants.PUBLISH_SPACE_CHARACTERISTIC));

            if (isPublishSpace) {
                enable = BooleanUtils.toBoolean(spaceConfig.getProperties().get(CommandConstants.COMMENTS_ENABLED_INDICATOR));
            }
        }

        return enable;
    }


    /**
     * Get remote published documents.
     *
     * @param readFilter filter published documents on user read permission
     * @param nuxeoController
     * @param document
     * @param documentDTO
     */
    protected void generatePublishedDocumentsInfos(NuxeoController nuxeoController, Document document, DocumentDTO documentDTO, Boolean readFilter) {
        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_61)) {

            int cacheType = nuxeoController.getCacheType();
            int authType = nuxeoController.getAuthType();

            try {

                nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

                GetPublishedDocumentsInfosCommand getPublishedCommand = new GetPublishedDocumentsInfosCommand(document, readFilter);
                JSONArray jsonPublishedDocumentsInfos = (JSONArray) nuxeoController.executeNuxeoCommand(getPublishedCommand);

                for (int index = 0; index < jsonPublishedDocumentsInfos.size(); index++) {
                    JSONObject publishedDocumentInfos = jsonPublishedDocumentsInfos.getJSONObject(index);
                    RemotePublishedDocumentDTO publishedDocumentDTO = this.publishedDocumentsDao.toDTO(publishedDocumentInfos);
                    documentDTO.getPublishedDocuments().add(publishedDocumentDTO);
                }
            } finally {
                nuxeoController.setCacheType(cacheType);
                nuxeoController.setAuthType(authType);
            }
        }
    }


    /**
     * Handle current document Nuxeo Drive edition.
     *
     * @param portalControllerContext portal controller context
     * @param document Nuxeo document
     * @param documentDto document DTO
     * @param publicationInfos publication informations
     */
    private void handleDriveEdition(PortalControllerContext portalControllerContext, Document document, DocumentDTO documentDto,
            CMSPublicationInfos publicationInfos) {
        // Portlet request
        PortletRequest request = portalControllerContext.getRequest();

        if (publicationInfos.isEditableByUser()) {
            DocumentType documentType = documentDto.getType();
            if ((documentType != null) && documentType.isLiveEditable()) {
                if (documentType.isFile()) {
                    // Drive edit URL
                    String driveEditUrl = (String) publicationInfos.getDriveEditURL();
                    // Drive enabled indicator
                    boolean driveEnabled = BooleanUtils.isTrue((Boolean) publicationInfos.isDriveEnabled());

                    if ((driveEditUrl != null) || driveEnabled) {
                        request.setAttribute("driveEditUrl", driveEditUrl);
                        request.setAttribute("driveEnabled", driveEnabled);
                        
                        
                        // MIME type icon
                        String mimeTypeIcon = "file";

                        // File content
                        PropertyMap fileContent = document.getProperties().getMap("file:content");

                        if (fileContent != null) {
                            try {
                                MimeType mimeType = new MimeType(fileContent.getString("mime-type"));
                                String primaryType = mimeType.getPrimaryType();
                                String subType = mimeType.getSubType();

                                if ("application".equals(primaryType)) {
                                    // Application

                                    if ("pdf".equals(subType)) {
                                        // PDF
                                        mimeTypeIcon = "pdf";
                                    } else if ("msword".equals(subType) || "vnd.openxmlformats-officedocument.wordprocessingml.document".equals(subType)) {
                                        // MS Word
                                        mimeTypeIcon = "word";
                                    } else if ("vnd.ms-excel".equals(subType) || "vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(subType)) {
                                        // MS Excel
                                        mimeTypeIcon = "excel";
                                    } else if ("vnd.ms-powerpoint".equals(subType)
                                            || "vnd.openxmlformats-officedocument.presentationml.presentation".equals(subType)) {
                                        // MS Powerpoint
                                        mimeTypeIcon = "powerpoint";
                                    } else if ("vnd.oasis.opendocument.text".equals(subType)) {
                                        // OpenDocument - Text
                                        mimeTypeIcon = "odt";
                                    } else if ("vnd.oasis.opendocument.spreadsheet".equals(subType)) {
                                        // OpenDocument - Spread sheet
                                        mimeTypeIcon = "ods";
                                    } else if ("vnd.oasis.opendocument.presentation".equals(subType)) {
                                        // OpenDocument - Presentation
                                        mimeTypeIcon = "odp";
                                    } else if ("zip".equals(subType) || "gzip".equals(subType)) {
                                        // Archive
                                        mimeTypeIcon = "archive";
                                    }
                                } else if ("text".equals(primaryType)) {
                                    // Text

                                    if ("html".equals(subType) || "xml".equals(subType)) {
                                        // HTML or XML
                                        mimeTypeIcon = "xml";
                                    } else {
                                        // Plain text
                                        mimeTypeIcon = "text";
                                    }
                                } else if ("image".equals(primaryType)) {
                                    // Image
                                    mimeTypeIcon = "image";
                                } else if ("video".equals(primaryType)) {
                                    // Video
                                    mimeTypeIcon = "video";
                                } else if ("audio".equals(primaryType)) {
                                    // Audio
                                    mimeTypeIcon = "audio";
                                }
                            } catch (MimeTypeParseException e) {
                                // Do nothing
                            }
                        }
                        
                        // Glyph
                        String glyph = "flaticon flaticon-" + mimeTypeIcon;
                        request.setAttribute("glyph", glyph);
                    }
                }
            }
        }
    }

}
