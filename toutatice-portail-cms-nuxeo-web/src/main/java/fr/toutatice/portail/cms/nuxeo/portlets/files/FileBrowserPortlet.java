package fr.toutatice.portail.cms.nuxeo.portlets.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.menubar.MenubarItem;
import org.osivia.portal.api.path.PortletPathItem;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.DocTypeDefinition;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoConnectionProperties;

import fr.toutatice.portail.cms.nuxeo.portlets.bridge.PortletHelper;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DeleteDocumentCommand;


/**
 * Portlet d'affichage d'un explorateur de fichiers
 */

public class FileBrowserPortlet extends CMSPortlet {


    public static boolean isNavigable(Document doc) {

        return "Folder".equals(doc.getType()) || "OrderedFolder".equals(doc.getType()) || "Workspace".equals(doc.getType())
                || "WorkspaceRoot".equals(doc.getType()) || "PortalSite".equals(doc.getType()) || "Section".equals(doc.getType())
                || "SectionRoot".equals(doc.getType());

    }

    public static boolean isOrdered(Document doc) {

        return "Workspace".equals(doc.getType()) || "WorkspaceRoot".equals(doc.getType()) || "OrderedFolder".equals(doc.getType())
                || "PortalSite".equals(doc.getType()) || "PortalPage".equals(doc.getType()) || "WikiBook".equals(doc.getType())
                || "WikiSection".equals(doc.getType());

    }

    public static boolean isFolderish(Document doc) {
        return "PictureBook".equals(doc.getType()) || "DocumentUrlContainer".equals(doc.getType()) || isNavigable(doc);

    }


    // tri sur folder / libelle
    // Doit être mis à jour en parallele avec le MenuPortlet.createComparator

    public static Comparator<Document> createComparator(final Document parentDoc) {

        Comparator<Document> comparator = new Comparator<Document>() {

            public int compare(Document e1, Document e2) {


                if (isFolderish(e1)) {
                    if (isFolderish(e2)) {
                        return e1.getTitle().toUpperCase().compareTo(e2.getTitle().toUpperCase());
                    } else
                        return -1;

                } else {
                    if (isFolderish(e2)) {
                        return 1;
                    } else {
                        return e1.getTitle().toUpperCase().compareTo(e2.getTitle().toUpperCase());
                    }

                }

            }
        };

        return comparator;
    };

    // v2.1 WORKSPACE
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {

        try {
            // Redirection sur lien contextuel

            if ("fileActions".equals(resourceRequest.getParameter("type"))) {

                NuxeoController ctx = new NuxeoController(resourceRequest, null, getPortletContext());

                String id = resourceRequest.getResourceID();

                CMSPublicationInfos pubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), id);
                StringBuffer sb = new StringBuffer();


                sb.append("<div>");
                int nbItems = 0;
                if (pubInfos.isEditableByUser()) {

                    Document doc = ctx.fetchDocument(id);

                    Map<String, DocTypeDefinition> managedTypes = ctx.getDocTypeDefinitions();
                    DocTypeDefinition docTypeDef = managedTypes.get(doc.getType());

                    if (docTypeDef != null && docTypeDef.isSupportingPortalForm()) {


                        String refreshUrl =  ctx.getPortalUrlFactory().getRefreshPageUrl(new PortalControllerContext(getPortletContext(), resourceRequest,
                                resourceResponse));

                        sb.append("<a class=\"fancyframe_refresh\" onClick=\"setCallbackParams(null, '" + refreshUrl + "')\" href=\""
                                + ctx.getNuxeoPublicBaseUri() + "/nxpath/default" + pubInfos.getDocumentPath() + "@toutatice_edit\">Modifier</a>");
                        nbItems++;

                    }
                    if (nbItems > 0)
                        sb.append("<br/>");

                    sb.append("<a target=\"nuxeo\" href=\"" + ctx.getNuxeoPublicBaseUri() + "/nxdoc/default/" + pubInfos.getLiveId()
                            + "/view_documents\">Editer dans Nuxeo</a>");
                    nbItems++;

                }


                 if (pubInfos.isDeletableByUser()) {
                     String deleteDivId = resourceResponse.getNamespace() + "delete-file-item";
                     String deleteFormId = resourceResponse.getNamespace() + "delete-file-form";
                     
                     String deleteURL =  ctx.getPortalUrlFactory().getPutDocumentInTrashUrl(new PortalControllerContext(getPortletContext(), resourceRequest,
                             resourceResponse), pubInfos.getLiveId(), pubInfos.getDocumentPath());

                     
                    sb.append("<br/>");
                    sb.append("<a class=\"fancybox_inline\" href=\"#"+deleteDivId+"\"");
                    sb.append("onclick=\"document.getElementById('"+deleteFormId+"').action ='"+deleteURL+"';\">");
                    sb.append("Supprimer</a>");
                    nbItems++;
                 }


                if (nbItems == 0)
                    sb.append("<b>Aucune action<br/>disponible</b>");


                sb.append("<div>");
                resourceResponse.getPortletOutputStream().write(sb.toString().getBytes());
                resourceResponse.getPortletOutputStream().close();

            } else
                super.serveResource(resourceRequest, resourceResponse);

        } catch (NuxeoException e) {
            serveResourceException(resourceRequest, resourceResponse, e);
        } catch (Exception e) {
            throw new PortletException(e);

        }
    }

   
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


        if (req.getParameter("deleteFileItem") != null) {
            String itemId = (String) req.getParameter("fileItemId");
            NuxeoController ctrl = new NuxeoController(req, res, getPortletContext());
            try {
                ctrl.executeNuxeoCommand(new DeleteDocumentCommand(itemId));
            } catch (Exception e) {
                if (!(e instanceof PortletException))
                    throw new PortletException(e);
            }
        }

    }

    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

        res.setContentType("text/html");

        NuxeoController ctx = new NuxeoController(req, res, getPortletContext());

        PortletRequestDispatcher rd = null;

        PortalWindow window = WindowFactory.getWindow(req);

        String nuxeoPath = window.getProperty("osivia.nuxeoPath");
        if (nuxeoPath == null)
            nuxeoPath = "";
        req.setAttribute("nuxeoPath", nuxeoPath);



        req.setAttribute("ctx", ctx);


        rd = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/admin.jsp");
        rd.include(req, res);

    }




    @SuppressWarnings("unchecked")
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {

        logger.debug("doView");

        try {

            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);


            /* On détermine l'uid et le scope */

            String nuxeoPath = null;

            // portal window parameter (appels dynamiques depuis le portail)
            nuxeoPath = window.getProperty("osivia.cms.uri");

            if (nuxeoPath == null) {
                nuxeoPath = window.getProperty("osivia.nuxeoPath");
            }


            if (nuxeoPath != null) {
                NuxeoController ctx = new NuxeoController(request, response, getPortletContext());


                nuxeoPath = ctx.getComputedPath(nuxeoPath);
                
  
                Document doc = ctx.fetchDocument(nuxeoPath);
                


                /* Récupération des fils */


                CMSPublicationInfos pubInfos = ctx.getCMSService().getPublicationInfos(ctx.getCMSCtx(), nuxeoPath);


                Documents docs = (Documents) ctx.executeNuxeoCommand(new FolderGetFilesCommand(pubInfos.getDocumentPath(), pubInfos.getLiveId()));


                // Tri pour affichage

                List<Document> sortedDocs = (ArrayList<Document>) docs.list();

                if (!isOrdered(doc))
                    Collections.sort(sortedDocs, createComparator(doc));
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

                getPortletContext().getRequestDispatcher("/WEB-INF/jsp/files/view.jsp").include(request, response);

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
            if (!(e instanceof PortletException))
                throw new PortletException(e);
        }

        logger.debug("doView end");

    }

}
