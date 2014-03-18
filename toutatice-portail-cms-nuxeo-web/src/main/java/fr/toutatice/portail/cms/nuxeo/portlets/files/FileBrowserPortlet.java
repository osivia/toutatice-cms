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
package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSItemType;
import org.osivia.portal.core.cms.CMSPublicationInfos;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;


/**
 * Portlet d'affichage d'un explorateur de fichiers.
 * 
 * @see CMSPortlet
 */
public class FileBrowserPortlet extends CMSPortlet {

    /**
     * Default constructor.
     */
    public FileBrowserPortlet() {
        super();
    }


    // v2.1 WORKSPACE
    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {

        try {
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

                    if (cmsItemType != null && cmsItemType.isSupportsPortalForms()) {

                        String refreshUrl = ctx.getPortalUrlFactory().getRefreshPageUrl(
                                new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse));

                        sb.append("<a class=\"fancyframe_refresh\" onClick=\"setCallbackParams(null, '" + refreshUrl + "')\" href=\""
                                + ctx.getNuxeoPublicBaseUri() + "/nxpath/default" + pubInfos.getDocumentPath() + "@toutatice_edit\">Modifier</a>");
                        nbItems++;

                    }
                    if (nbItems > 0) {
                        sb.append("<br/>");
                    }

                    sb.append("<a target=\"nuxeo\" href=\"" + ctx.getNuxeoPublicBaseUri() + "/nxdoc/default/" + pubInfos.getLiveId()
                            + "/view_documents\">Editer dans Nuxeo</a>");
                    nbItems++;

                }


                if (pubInfos.isDeletableByUser()) {
                    String deleteDivId = resourceResponse.getNamespace() + "delete-file-item";
                    String deleteFormId = resourceResponse.getNamespace() + "delete-file-form";

                    String deleteURL = ctx.getPortalUrlFactory().getPutDocumentInTrashUrl(
                            new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse), pubInfos.getLiveId(),
                            pubInfos.getDocumentPath());


                    sb.append("<br/>");
                    sb.append("<a class=\"fancybox_inline\" href=\"#" + deleteDivId + "\"");
                    sb.append("onclick=\"document.getElementById('" + deleteFormId + "').action ='" + deleteURL + "';\">");
                    sb.append("Supprimer</a>");
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

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

            PortalWindow window = WindowFactory.getWindow(req);
            window.setProperty("osivia.nuxeoPath", req.getParameter("nuxeoPath"));


            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

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
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {

        logger.debug("doView");

        try {

            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);


            /* On détermine l'uid et le scope */

            String nuxeoPath = null;

            // portal window parameter (appels dynamiques depuis le portail)
            nuxeoPath = window.getProperty(Constants.WINDOW_PROP_URI);

            if (nuxeoPath == null) {
                nuxeoPath = window.getProperty("osivia.nuxeoPath");
            }


            if (nuxeoPath != null) {
                NuxeoController ctx = new NuxeoController(request, response, this.getPortletContext());
                
                ctx.setDisplayLiveVersion("1");


                nuxeoPath = ctx.getComputedPath(nuxeoPath);


                Document doc = ctx.fetchDocument(nuxeoPath);


                /* Récupération des fils */


                CMSPublicationInfos pubInfos = NuxeoController.getCMSService().getPublicationInfos(ctx.getCMSCtx(), nuxeoPath);


                Documents docs = (Documents) ctx.executeNuxeoCommand(new FolderGetFilesCommand(pubInfos.getDocumentPath(), pubInfos.getLiveId()));


                // Tri pour affichage

                List<Document> sortedDocs = docs.list();


                CMSItemType cmsItemType = ctx.getCMSItemTypes().get(doc.getType());
                if ((cmsItemType == null) || !cmsItemType.isOrdered()) {
                    Collections.sort(sortedDocs, new FileBrowserComparator(ctx));
                }
                request.setAttribute("docs", sortedDocs);


                // Insert standard menu bar for content item
                ctx.setCurrentDoc(doc);
                ctx.insertContentMenuBarItems();


                /* attributs de la JSP */
                request.setAttribute("basePath", nuxeoPath);
                request.setAttribute("folderPath", nuxeoPath);


                request.setAttribute("doc", doc);
                request.setAttribute("ctx", ctx);


                response.setTitle(doc.getTitle());

                this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view.jsp").include(request, response);

            } else {
                response.setContentType("text/html");
                response.getWriter().print("<h2>Document non défini</h2>");
                response.getWriter().close();
                return;
            }

        } catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        }

        catch (Exception e) {
            if (!(e instanceof PortletException)) {
                throw new PortletException(e);
            }
        }

        logger.debug("doView end");

    }

}
