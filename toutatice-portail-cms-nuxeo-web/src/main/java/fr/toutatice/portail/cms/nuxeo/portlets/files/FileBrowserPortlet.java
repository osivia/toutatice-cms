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
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.constants.InternationalizationConstants;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;


/**
 * File browser portlet.
 *
 * @see CMSPortlet
 */
public class FileBrowserPortlet extends CMSPortlet {

    /** View path. */
    private static final String PATH_VIEW = "/WEB-INF/jsp/files/view.jsp";

    /** Bundle factory. */
    private IBundleFactory bundleFactory;


    /**
     * Default constructor.
     */
    public FileBrowserPortlet() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        // Internationalization service initialization
        IInternationalizationService internationalizationService = (IInternationalizationService) this.getPortletContext().getAttribute(
                Constants.INTERNATIONALIZATION_SERVICE_NAME);
        if (internationalizationService == null) {
            throw new PortletException("Internationalization service initialization error.");
        }
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
        try {
            // Bundle
            Bundle bundle = this.bundleFactory.getBundle(resourceRequest.getLocale());

            // Redirection sur lien contextuel
            if ("fileActions".equals(resourceRequest.getParameter("type"))) {
                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());

                String id = resourceRequest.getResourceID();

                CMSPublicationInfos pubInfos = NuxeoController.getCMSService().getPublicationInfos(ctx.getCMSCtx(), id);
                StringBuffer sb = new StringBuffer();

                sb.append("<div>");
                int nbItems = 0;
                if (pubInfos.isEditableByUser()) {

                    Document doc = ctx.fetchDocument(id);

                    CMSItemType cmsItemType = ctx.getCMSItemTypes().get(doc.getType());

                    if ((cmsItemType != null) && cmsItemType.isSupportsPortalForms()) {
                        String refreshUrl = ctx.getPortalUrlFactory().getRefreshPageUrl(
                                new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse));

                        String editLabel = bundle.getString(InternationalizationConstants.KEY_EDIT);
                        sb.append("<a class=\"fancyframe_refresh\" onClick=\"setCallbackParams(null, '").append(refreshUrl).append("')\" href=\"")
                                .append(ctx.getNuxeoPublicBaseUri()).append("/nxpath/default").append(pubInfos.getDocumentPath()).append("@toutatice_edit\">")
                                .append(editLabel).append("</a>");
                        nbItems++;

                    }
                    if (nbItems > 0) {
                        sb.append("<br/>");
                    }

                    String manageLabel = bundle.getString(InternationalizationConstants.KEY_MANAGE);
                    sb.append("<a target=\"nuxeo\" href=\"").append(ctx.getNuxeoPublicBaseUri()).append("/nxdoc/default/").append(pubInfos.getLiveId())
                            .append("/view_documents\">").append(manageLabel).append("</a>");
                    nbItems++;

                }


                if (pubInfos.isDeletableByUser()) {
                    String deleteDivId = resourceResponse.getNamespace() + "delete-file-item";
                    String deleteFormId = resourceResponse.getNamespace() + "delete-file-form";

                    String deleteURL = ctx.getPortalUrlFactory().getPutDocumentInTrashUrl(
                            new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse), pubInfos.getLiveId(),
                            pubInfos.getDocumentPath());

                    String deleteLabel = bundle.getString(InternationalizationConstants.KEY_DELETE);
                    sb.append("<br/>");
                    sb.append("<a class=\"fancybox_inline\" href=\"#").append(deleteDivId).append("\" onclick=\"document.getElementById('")
                            .append(deleteFormId).append("').action ='").append(deleteURL).append("';\">").append(deleteLabel).append("</a>");
                    nbItems++;
                }


                if (nbItems == 0) {
                    sb.append("<b>Aucune action<br/>disponible</b>");
                }


                sb.append("<div>");
                resourceResponse.getPortletOutputStream().write(sb.toString().getBytes());
                resourceResponse.getPortletOutputStream().close();

            } else {
                super.serveResource(resourceRequest, resourceResponse);
            }

        } catch (NuxeoException e) {
            this.serveResourceException(resourceRequest, resourceResponse, e);
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

        logger.debug("processAction ");

        if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("modifierPrefs") != null)) {

            PortalWindow window = WindowFactory.getWindow(req);
            window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));


            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        if ("admin".equals(req.getPortletMode().toString()) && (req.getParameter("annuler") != null)) {

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
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

        NuxeoController ctx = new NuxeoController(req, res, this.getPortletContext());

        PortletRequestDispatcher rd = null;

        PortalWindow window = WindowFactory.getWindow(req);

        String nuxeoPath = window.getProperty("osivia.nuxeoPath");
        if (nuxeoPath == null) {
            nuxeoPath = "";
        }
        req.setAttribute("nuxeoPath", nuxeoPath);


        req.setAttribute("ctx", ctx);


        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/admin.jsp");
        rd.include(req, res);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        try {
            // Current window
            PortalWindow window = WindowFactory.getWindow(request);


            // Path
            String path = window.getProperty(Constants.WINDOW_PROP_URI);
            if (path == null) {
                path = window.getProperty("osivia.nuxeoPath");
            }

            if (StringUtils.isNotEmpty(path)) {
                // Nuxeo controller
                NuxeoController nuxeoController = new NuxeoController(request, response, this.getPortletContext());
                nuxeoController.setDisplayLiveVersion("1");

                // Computed path
                path = nuxeoController.getComputedPath(path);

                // Current Nuxeo document
                Document doc = nuxeoController.fetchDocument(path);

                // Publication informations
                CMSPublicationInfos pubInfos = NuxeoController.getCMSService().getPublicationInfos(nuxeoController.getCMSCtx(), path);

                // Nuxeo children documents
                Documents documents = (Documents) nuxeoController.executeNuxeoCommand(new FolderGetFilesCommand(pubInfos.getDocumentPath(), pubInfos.getLiveId()));

                // Sorted documents
                List<Document> sortedDocuments = documents.list();
                CMSItemType cmsItemType = nuxeoController.getCMSItemTypes().get(doc.getType());
                if ((cmsItemType == null) || !cmsItemType.isOrdered()) {
                    Collections.sort(sortedDocuments, new FileBrowserComparator(nuxeoController));
                }

                // Insert standard menu bar for content item
                nuxeoController.setCurrentDoc(doc);
                nuxeoController.insertContentMenuBarItems();

                // Description
                request.setAttribute("description", Formater.formatDescription(doc));
                // Documents
                request.setAttribute("documents", this.toViewObjects(nuxeoController, sortedDocuments));
                // Path
                request.setAttribute("path", path);

                // Title
                response.setTitle(doc.getTitle());
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
     * Convert Nuxeo documents to view objects.
     *
     * @param nuxeoController Nuxeo controller
     * @param documents Nuxeo documents
     * @return view objects
     * @throws ParseException
     */
    private Collection<FileBrowserDocumentVO> toViewObjects(NuxeoController nuxeoController, List<Document> documents) throws ParseException {
        Collection<FileBrowserDocumentVO> results = new ArrayList<FileBrowserDocumentVO>(documents.size());

        for (Document document : documents) {
            FileBrowserDocumentVO documentVO = new FileBrowserDocumentVO();
            // Title
            documentVO.setTitle(document.getTitle());
            // Link
            documentVO.setLink(nuxeoController.getLink(document, "fileExplorer"));
            // Document size
            documentVO.setSize(Formater.formatSize(document));
            // Icon source
            documentVO.setIconSource(Formater.formatNuxeoIcon(document));
            // Icon alt
            documentVO.setIconAlt(document.getType());
            // Download link
            if ("File".equals(document.getType())) {
                documentVO.setDownloadLink(nuxeoController.getLink(document, "download"));
            }
            // Date
            documentVO.setDate(Formater.formatDateAndTime(document));
            // Last contributor
            documentVO.setLastContributor(StringUtils.trimToEmpty(document.getString("dc:lastContributor")));

            results.add(documentVO);
        }
        return results;
    }

}
