package fr.toutatice.portail.cms.nuxeo.portlets.document;

import java.io.File;
import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.PortletErrorHandler;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.document.comments.AddCommentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.comments.CreateChildCommentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.comments.DeleteCommentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.comments.GetCommentsCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.comments.HTMLCommentsTreeBuilder;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;
import fr.toutatice.portail.cms.nuxeo.portlets.thumbnail.ThumbnailServlet;

/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class ViewDocumentPortlet extends CMSPortlet {

    private static Log logger = LogFactory.getLog(ViewDocumentPortlet.class);

    private INuxeoService nuxeoService;


    @Override
    public void init(PortletConfig config) throws PortletException {

        super.init(config);


        try {
            // Enregistremennt des gestionnaires de liens et de template

            this.nuxeoService = (INuxeoService) this.getPortletContext().getAttribute("NuxeoService");
            if (this.nuxeoService == null) {
                throw new PortletException("Cannot start ViewDocumentPortlet portlet due to service unavailability");
            }

            CMSCustomizer customizer = new CMSCustomizer(this.getPortletContext());
            this.nuxeoService.registerCMSCustomizer(customizer);

            CMSService CMSservice = new CMSService(this.getPortletContext());
            ICMSServiceLocator cmsLocator = Locator.findMBean(ICMSServiceLocator.class, "osivia:service=CmsServiceLocator");
            cmsLocator.register(CMSservice);


            customizer.setCmsService(CMSservice);
            CMSservice.setCustomizer(customizer);


            // v1.0.16
            ThumbnailServlet.setPortletContext(this.getPortletContext());


        } catch (Exception e) {
            throw new PortletException(e);
        }


    }


    @Override
    public void processAction(ActionRequest req, ActionResponse res) throws IOException, PortletException {

        logger.debug("processAction ");

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("modifierPrefs") != null) {

            PortalWindow window = WindowFactory.getWindow(req);
            window.setProperty("osivia.cms.uri", req.getParameter("nuxeoPath"));


            if ("1".equals(req.getParameter("onlyDescription"))) {
                window.setProperty("osivia.document.onlyDescription", "1");
            } else if (window.getProperty("osivia.document.onlyDescription") != null) {
                window.setProperty("osivia.document.onlyDescription", null);
            }

            if (!"1".equals(req.getParameter("showMetadatas"))) {
                window.setProperty("osivia.cms.hideMetaDatas", "1");
            } else if (window.getProperty("osivia.cms.hideMetaDatas") != null) {
                window.setProperty("osivia.cms.hideMetaDatas", null);
            }


            if (req.getParameter("displayLiveVersion") != null && req.getParameter("displayLiveVersion").length() > 0) {
                window.setProperty("osivia.cms.displayLiveVersion", req.getParameter("displayLiveVersion"));
            } else if (window.getProperty("osivia.cms.displayLiveVersion") != null) {
                window.setProperty("osivia.cms.displayLiveVersion", null);
            }


            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        if ("admin".equals(req.getPortletMode().toString()) && req.getParameter("annuler") != null) {

            res.setPortletMode(PortletMode.VIEW);
            res.setWindowState(WindowState.NORMAL);
        }

        String commentAction = req.getParameter("comments");
        if (commentAction != null) {

            NuxeoController ctrl = new NuxeoController(req, res, this.getPortletContext());
            PortalWindow window = WindowFactory.getWindow(req);
            String nuxeoPath = window.getProperty("osivia.cms.uri");
            if (nuxeoPath == null) {
                // WIndow parameter (back-office)
                nuxeoPath = window.getProperty("osivia.nuxeoPath");
            }

            if (nuxeoPath != null) {
                nuxeoPath = ctrl.getComputedPath(nuxeoPath);
                try {
                    Document commentableDoc = ctrl.fetchDocument(nuxeoPath);
                    if ("toAdd".equals(commentAction)) {
                        String commentContent = req.getParameter("content");                   
                        ctrl.executeNuxeoCommand(new AddCommentCommand(commentableDoc, commentContent, null, null));
                    }

                    if ("addChild".equals(req.getParameter("comments"))) {
                        String commentId = req.getParameter("commentId");
                        String childCommentContent = req.getParameter("childCommentContent");
                        ctrl.executeNuxeoCommand(new CreateChildCommentCommand(commentableDoc, commentId, childCommentContent, null, null));
                    }

                    if ("delete".equals(commentAction)) {
                        String commentId = req.getParameter("commentId");
                        ctrl.executeNuxeoCommand(new DeleteCommentCommand(commentableDoc, commentId));
                    }
                } catch (Exception e) {
                    if (!(e instanceof PortletException)) {
                        throw new PortletException(e);
                    }
                }
            }
        }
        // Modif-COMMENTS-end

    }


    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest req, RenderResponse res) throws IOException, PortletException {

        res.setContentType("text/html");
        NuxeoController ctx = new NuxeoController(req, res, this.getPortletContext());

        PortletRequestDispatcher rd = null;

        PortalWindow window = WindowFactory.getWindow(req);
        String nuxeoPath = window.getProperty("osivia.cms.uri");
        if (nuxeoPath == null) {
            nuxeoPath = "";
        }
        req.setAttribute("nuxeoPath", nuxeoPath);

        String onlyDescription = window.getProperty("osivia.document.onlyDescription");
        req.setAttribute("onlyDescription", onlyDescription);

        String showMetadatas = "1";
        if ("1".equals(window.getProperty("osivia.cms.hideMetaDatas"))) {
            showMetadatas = "0";
        }
        req.setAttribute("showMetadatas", showMetadatas);


        String displayLiveVersion = window.getProperty("osivia.cms.displayLiveVersion");
        req.setAttribute("displayLiveVersion", displayLiveVersion);

        req.setAttribute("ctx", ctx);

        rd = this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/document/admin.jsp");
        rd.include(req, res);


    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {

        logger.debug("doView");

        try {

            response.setContentType("text/html");

            PortalWindow window = WindowFactory.getWindow(request);

            /* On détermine l'uid et le scope */

            String nuxeoPath = null;


            // path parameter
            nuxeoPath = window.getProperty("osivia.cms.uri");


            if (nuxeoPath != null) {

                NuxeoController ctx = new NuxeoController(request, response, this.getPortletContext());

                nuxeoPath = ctx.getComputedPath(nuxeoPath);

                Document doc = ctx.fetchDocument(nuxeoPath);


                if (doc.getTitle() != null) {
                    response.setTitle(doc.getTitle());
                }


                request.setAttribute("doc", doc);


                if (!"1".equals(window.getProperty("osivia.document.onlyDescription")) || request.getWindowState().equals(WindowState.MAXIMIZED)) {

                    /* transformation de la partie wysiwyg */

                    String note = doc.getString("note:note", "");


                    ctx.setCurrentDoc(doc);

                    // Insert standard menu bar for content item
                    ctx.insertContentMenuBarItems();


                    String noteTransformee = ctx.transformHTMLContent(note);
                    request.setAttribute("note", noteTransformee);



                    ICMSService cmsService = NuxeoController.getCMSService();
                    CMSPublicationInfos publiInfos = cmsService.getPublicationInfos(ctx.getCMSCtx(), nuxeoPath);
                    
                    boolean docCanBeCommentedByUser = publiInfos.isCommentableByUser();

                    if (docCanBeCommentedByUser) {// TODO: ajouter cdt de contextualisation
                        String user = request.getRemoteUser();
                        int authType = ctx.getAuthType();
                        JSONArray jsonComments = (JSONArray) ctx.executeNuxeoCommand(new GetCommentsCommand(doc));
                        CMSServiceCtx cmsCtx = ctx.getCMSCtx();
                        String comments = HTMLCommentsTreeBuilder.buildHtmlTree(cmsCtx, new StringBuffer(), jsonComments, 0, authType, user);
                        request.setAttribute("comments", comments);
                    }


                }

                request.setAttribute("ctx", ctx);

                String description = doc.getString("dc:description", "");
                request.setAttribute("description", description);

                request.setAttribute("onlyDescription", window.getProperty("osivia.document.onlyDescription"));


                String showMetadatas = "1";
                if ("1".equals(window.getProperty("osivia.cms.hideMetaDatas"))) {
                    showMetadatas = "0";
                }
                request.setAttribute("showMetadatas", showMetadatas);


                this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/document/view.jsp").include(request, response);
            } else {
                response.setContentType("text/html");
                response.getWriter().print("<h2>Document non défini</h2>");
                response.getWriter().close();
            }
        }

        catch (NuxeoException e) {
            PortletErrorHandler.handleGenericErrors(response, e);
        } catch (Exception e) {
            if (!(e instanceof PortletException)) {
                throw new PortletException(e);
            }
        }

        logger.debug("doView end");

    }


}
