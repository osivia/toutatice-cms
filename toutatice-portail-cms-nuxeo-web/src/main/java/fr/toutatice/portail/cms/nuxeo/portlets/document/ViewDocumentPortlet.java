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
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
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
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.ecm.EcmCommand;
import org.osivia.portal.api.ecm.IEcmCommandervice;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.notifications.INotificationsService;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSException;
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
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentAttachmentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.RemotePublishedDocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.CommentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.RemotePublishedDocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.avatar.AvatarServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.binaries.BinaryServlet;
import fr.toutatice.portail.cms.nuxeo.portlets.commands.CommandConstants;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.AddCommentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.DeleteCommentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.comments.GetCommentsCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.CMSItemAdapter;
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
    /** Dispatch to JSP */
    public static final String DISPATCH_JSP = "osivia.document.dispatch.jsp";
    /** Admin path. */
    private static final String PATH_ADMIN = "/WEB-INF/jsp/document/admin.jsp";
    /** View path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/document/view.jsp";


    /** Nuxeo service. */
    private INuxeoService nuxeoService;
    /** Document DAO. */
    private DocumentDAO documentDAO;
    /** Document comment DAO. */
    private CommentDAO commentDAO;
    /** Document published documents DAO. */
    private RemotePublishedDocumentDAO publishedDocumentsDAO;


    /**
     * Default constructor.
     */
    public ViewDocumentPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        try {
            // Nuxeo service
            this.nuxeoService = (INuxeoService) this.getPortletContext().getAttribute("NuxeoService");
            if (this.nuxeoService == null) {
                throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
            }

            // CMS customizer
            CMSCustomizer customizer = new CMSCustomizer(this.getPortletContext());
            this.nuxeoService.registerCMSCustomizer(customizer);

            // Nuxeo tag service
            INuxeoTagService tagService = new NuxeoTagService();
            this.nuxeoService.registerTagService(tagService);

            // DAO
            this.documentDAO = DocumentDAO.getInstance();
            this.commentDAO = CommentDAO.getInstance();
            this.publishedDocumentsDAO = RemotePublishedDocumentDAO.getInstance();

            // CMS service
            CMSService cmsService = new CMSService(this.getPortletContext());
            ICMSServiceLocator cmsLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
            cmsLocator.register(cmsService);
            cmsService.setCustomizer(customizer);
            customizer.setCmsService(cmsService);



            // Directory service locator
            IDirectoryServiceLocator directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
            customizer.setDirectoryService(directoryServiceLocator.getDirectoryService());

            IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class, IInternationalizationService.MBEAN_NAME);
            customizer.setInternationalizationService(internationalizationService);
            INotificationsService notificationsService = Locator.findMBean(INotificationsService.class, INotificationsService.MBEAN_NAME);
            customizer.setNotificationsService(notificationsService);

            // ECM command services
            IEcmCommandervice ecmCmdService = Locator.findMBean(IEcmCommandervice.class, IEcmCommandervice.MBEAN_NAME);

            for(EcmCommand command : customizer.getEcmCommands().values()) {
            	ecmCmdService.registerCommand(command.getCommandName(), command);
            }


            // v1.0.16
            ThumbnailServlet.setPortletContext(this.getPortletContext());
            SitePictureServlet.setPortletContext(this.getPortletContext());
            AvatarServlet.setPortletContext(this.getPortletContext());
            BinaryServlet.setPortletContext(this.getPortletContext());
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

            // Comment identifier
            String id = request.getParameter("id");
            // Comment content
            String content = request.getParameter("content");
            // Comment DTO
            CommentDTO comment = new CommentDTO();
            comment.setContent(content);

            // Nuxeo controller
            NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
            // Document path
            String path = window.getProperty(PATH_WINDOW_PROPERTY);

            if (path != null) {
                // Fetch Nuxeo document
                Document document = nuxeoController.fetchDocument(path);

                if ("addComment".equals(action)) {
                    // Add comment

                    // Nuxeo command
                    INuxeoCommand command = new AddCommentCommand(document, comment, null);
                    nuxeoController.executeNuxeoCommand(command);
                } else if ("replyComment".equals(action)) {
                    // Reply comment

                    // Nuxeo command
                    INuxeoCommand command = new AddCommentCommand(document, comment, id);
                    nuxeoController.executeNuxeoCommand(command);
                } else if ("deleteComment".equals(action)) {
                    // Delete comment

                    // Nuxeo command
                    INuxeoCommand command = new DeleteCommentCommand(document, id);
                    nuxeoController.executeNuxeoCommand(command);
                }
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

                // Fetch document
                Document document = nuxeoController.fetchDocument(path);
                nuxeoController.setCurrentDoc(document);

                // View dispatched
                if(window.getProperty(DISPATCH_JSP) != null) {
                	request.setAttribute("dispatchJsp", window.getProperty(DISPATCH_JSP));
                }
                else {
                	String docType = StringUtils.lowerCase(document.getType());

                	String realPath = this.getPortletContext().getRealPath("/WEB-INF/jsp/document/view-" + docType + ".jsp" );
                	File file = new File(realPath);
                	if(file.exists()) {
                		request.setAttribute("dispatchJsp", docType);
                	}
                	else {
                		request.setAttribute("dispatchJsp", "default");
                	}


                }

                // DTO
                DocumentDTO documentDTO = this.documentDAO.toDTO(document);
                request.setAttribute("document", documentDTO);

                // Title
                String title = document.getTitle();
                if (StringUtils.isNotBlank(title)) {
                    response.setTitle(title);
                }

                if(onlyRemoteSections && maximized){

                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, document, documentDTO, onlyRemoteSections);

                } else if (!onlyDescription || maximized) {
                    // Insert content menubar items
                    nuxeoController.insertContentMenuBarItems();

                    // Attachments
                    this.generateAttachments(nuxeoController, document, documentDTO);

                    // Remote Published documents
                    this.generatePublishedDocumentsInfos(nuxeoController, document, documentDTO, Boolean.FALSE);

                    // Comments
                    if (ContextualizationHelper.isCurrentDocContextualized(cmsContext)) {
                        // CMS service
                        ICMSService cmsService = NuxeoController.getCMSService();

                        // Publication infos
                        CMSPublicationInfos publicationInfos = cmsService.getPublicationInfos(cmsContext, path);

                        boolean commentsEnabled = this.areCommentsEnabled(cmsService, publicationInfos, cmsContext);
                        if (commentsEnabled && publicationInfos.isCommentableByUser()) {
                            documentDTO.setCommentable(true);

                            // Comments
                            this.generateComments(nuxeoController, document, documentDTO);
                        }
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
            CommentDTO commentDTO = this.commentDAO.toDTO(jsonComment);
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
            boolean isPublishSpace = CMSItemAdapter.docHasFacet(space, CommandConstants.PUBLISH_SPACE_CHARACTERISTIC);

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
    protected void generatePublishedDocumentsInfos(NuxeoController nuxeoController, Document document, DocumentDTO documentDTO, Boolean readFilter){
        if (NuxeoCompatibility.isVersionGreaterOrEqualsThan(NuxeoCompatibility.VERSION_61)) {

            int cacheType = nuxeoController.getCacheType();
            int authType = nuxeoController.getAuthType();

            try {

                nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

                GetPublishedDocumentsInfosCommand getPublishedCommand = new GetPublishedDocumentsInfosCommand(document, readFilter);
                JSONArray jsonPublishedDocumentsInfos = (JSONArray) nuxeoController.executeNuxeoCommand(getPublishedCommand);

                for (int index = 0; index < jsonPublishedDocumentsInfos.size(); index++) {

                    JSONObject publishedDocumentInfos = jsonPublishedDocumentsInfos.getJSONObject(index);
                    RemotePublishedDocumentDTO publishedDocumentDTO = this.publishedDocumentsDAO.toDTO(publishedDocumentInfos);
                    documentDTO.getPublishedDocuments().add(publishedDocumentDTO);

                }
            } finally {
                nuxeoController.setCacheType(cacheType);
                nuxeoController.setAuthType(authType);
            }

        }

    }

}
