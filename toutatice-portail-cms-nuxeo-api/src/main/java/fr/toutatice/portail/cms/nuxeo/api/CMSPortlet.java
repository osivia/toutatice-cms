package fr.toutatice.portail.cms.nuxeo.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


/**
 * Portlet d'affichage d'un document Nuxeo
 */

public class CMSPortlet extends GenericPortlet {

    protected static Log logger = LogFactory.getLog(CMSPortlet.class);

    INuxeoService nuxeoNavigationService;

    public INuxeoService getNuxeoNavigationService() throws Exception {

        if (this.nuxeoNavigationService == null) {
            this.nuxeoNavigationService = (INuxeoService) this.getPortletContext().getAttribute("NuxeoService");
        }

        return this.nuxeoNavigationService;

    }


    @Override
    public void init(PortletConfig config) throws PortletException {

        super.init(config);


        try	{
            new NuxeoController(this.getPortletContext()).startNuxeoService();

        } catch( Exception e)	{
            throw new PortletException( e);
        }


    }

    @Override
    public void destroy() {

        try {
            // Destruction des threads éventuels
            new NuxeoController(this.getPortletContext()).stopNuxeoService();

        } catch (Exception e) {
            logger.error(e);
        }

        super.destroy();
    }

    public String formatResourceLastModified() {

        // Modif-MIGRATION-begin
        /* SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH); */
        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);
        // Modif-MIGRATION-end
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }

    public boolean isResourceExpired(String sOriginalDate, ResourceResponse resourceResponse) {

        boolean isExpired = true;

        if (sOriginalDate != null) {

            // Modif-MIGRATION-begin
            /* SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH); */
            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);
            // Modif-MIGRATION-begin
            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < (originalDate.getTime()
                        + (resourceResponse.getCacheControl().getExpirationTime() * 1000))) {
                    isExpired = false;
                }
            } catch (Exception e) {

            }
        }

        return isExpired;
    }

    public boolean serveResourceByCache(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws PortletException, IOException {

        String sOriginalDate = resourceRequest.getProperty("if-modified-since");
        if (sOriginalDate == null) {
            sOriginalDate = resourceRequest.getProperty("If-Modified-Since");
        }

        if (!this.isResourceExpired(sOriginalDate, resourceResponse)) { // validation
            // request

            // resourceResponse.setContentLength(0);
            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
                    String.valueOf(HttpServletResponse.SC_NOT_MODIFIED));
            resourceResponse.setProperty("Last-Modified", sOriginalDate);

            // au moins un caractère
            // resourceResponse.getPortletOutputStream().write(' ');
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
     * @throws PortletException
     * @throws IOException
     */
    protected void serveResourceException(ResourceRequest resourceRequest, ResourceResponse resourceResponse,
            NuxeoException e) throws PortletException, IOException {
        int httpErrorCode = 0;
        if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
            httpErrorCode = HttpServletResponse.SC_NOT_FOUND;
            String message = "Resource CMSPortlet " + resourceRequest.getParameterMap() + " not found (error 404).";
            logger.error(message);
        } else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {
            httpErrorCode = HttpServletResponse.SC_FORBIDDEN;
        }

        if (httpErrorCode != 0) {
            NuxeoController nuxeoController = this.createNuxeoController(resourceRequest, resourceResponse);
            PortalControllerContext portalCtx = new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse);

            String errorUrl = nuxeoController.getPortalUrlFactory().getHttpErrorUrl(portalCtx, httpErrorCode);

            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
                    String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
            resourceResponse.setProperty("Location", errorUrl);
            resourceResponse.getPortletOutputStream().close();
        } else {
            throw e;
        }
    }

    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws PortletException, IOException {



        try {

            if (this.serveResourceByCache(resourceRequest, resourceResponse)) {
                return;
            }





            // Redirection
            if ("link".equals(resourceRequest.getParameter("type"))) {

                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());


                String id = resourceRequest.getResourceID();

                Document doc = ctx.fetchDocument(id);

                resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE,
                        String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
                resourceResponse.setProperty("Location", doc.getString("clink:link"));
                resourceResponse.getPortletOutputStream().close();
            }


            // Téléchargement d'un fichier présent dans un document externe
            /* Fichier, image "contenue dans un document - propriété spécifique du document (ex: annonce:image) */
            if ("file".equals(resourceRequest.getParameter("type"))) {

                String docPath = resourceRequest.getParameter("docPath");
                String fieldName = resourceRequest.getParameter("fieldName");
                String filename = resourceRequest.getParameter("filename");


                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());

                // V 1.0.19
                // V2 suppression a valider
                /*
				if( !"1".equals( resourceRequest.getParameter("displayLiveVersion")))	{
					Document doc = fetchLinkedDocument(ctx, docPath);
					docPath = doc.getPath();
				}
                 */

                CMSBinaryContent content = ctx.fetchFileContent(docPath, fieldName);

                if (StringUtils.isEmpty(filename)) {
                    filename = content.getName();
                }

                // Les headers doivent être positionnées avant la réponse
                resourceResponse.setContentType(content.getMimeType());
                resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + filename + "\"");

                ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
                        4096);

                resourceResponse.setProperty("Cache-Control", "max-age="
                        + resourceResponse.getCacheControl().getExpirationTime());

                resourceResponse.setProperty("Last-Modified", this.formatResourceLastModified());
            }

            if ("attachedFile".equals(resourceRequest.getParameter("type"))) {

                // Gestion d'un cache global

                String docPath = resourceRequest.getParameter("docPath");
                String fileIndex = resourceRequest.getParameter("fileIndex");

                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());


                CMSBinaryContent content = ResourceUtil.getCMSBinaryContent(ctx, docPath, fileIndex);

                // Les headers doivent être positionnées avant la réponse
                resourceResponse.setContentType(content.getMimeType());
                resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");

                ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
                        4096);


                resourceResponse.setProperty("Cache-Control", "max-age="
                        + resourceResponse.getCacheControl().getExpirationTime());
                resourceResponse.setProperty("Last-Modified", this.formatResourceLastModified());
            }


            // v.0.27 : ajout blob
            if ("blob".equals(resourceRequest.getParameter("type"))) {

                // Gestion d'un cache global

                String docPath = resourceRequest.getParameter("docPath");
                String blobIndex = resourceRequest.getParameter("blobIndex");



                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());

                // v2.1 : fetch direct (pour l'instant, on ne remonte pas en 2.0)
                // On traite comme s'il s'agissait d'un hyper-lien

                //				Document doc = fetchLinkedDocument(ctx, docPath);
                //				docPath = doc.getPath();
                //


                CMSBinaryContent content = ResourceUtil.getBlobHolderContent(ctx, docPath, blobIndex);



                // Les headers doivent être positionnées avant la réponse
                resourceResponse.setContentType(content.getMimeType());
                resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");

                ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
                        4096);


                resourceResponse.setProperty("Cache-Control", "max-age="
                        + resourceResponse.getCacheControl().getExpirationTime());
                resourceResponse.setProperty("Last-Modified", this.formatResourceLastModified());
            }

            /* Image "contenue" dans le document - propriété ttc:images */
            if ("attachedPicture".equals(resourceRequest.getParameter("type"))) {


                String docPath = resourceRequest.getParameter("docPath");
                String pictureIndex = resourceRequest.getParameter("pictureIndex");

                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());

                CMSBinaryContent content = ctx.fetchAttachedPicture(docPath, pictureIndex);

                // Les headers doivent être positionnées avant la réponse
                resourceResponse.setContentType(content.getMimeType());
                resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");

                ResourceUtil.copy(new FileInputStream(content.getFile()), resourceResponse.getPortletOutputStream(),
                        4096);


                resourceResponse.setProperty("Cache-Control", "max-age="
                        + resourceResponse.getCacheControl().getExpirationTime());
                resourceResponse.setProperty("Last-Modified", this.formatResourceLastModified());
            }

            /* Image externe au document - "objet nuxeo */
            if ("picture".equals(resourceRequest.getParameter("type"))) {


                // Gestion d'un cache global

                String docPath = resourceRequest.getParameter("docPath");
                String content = resourceRequest.getParameter("content");

                NuxeoController ctx = new NuxeoController(resourceRequest, null, this.getPortletContext());



                // V 1.0.19
                /* TOCHECK si on peut mettre en commentaire */
                // V2 suppression a valider
                /*
				if( !"1".equals( resourceRequest.getParameter("displayLiveVersion")))	{
					Document doc = fetchLinkedDocument(ctx, docPath);
					docPath = doc.getPath();
				}
                 */
                CMSBinaryContent picture = ctx.fetchPicture(docPath, content);

                // Les headers doivent être positionnées avant la réponse
                resourceResponse.setContentType(picture.getMimeType());
                resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + picture.getName() + "\"");

                ResourceUtil.copy(new FileInputStream(picture.getFile()), resourceResponse.getPortletOutputStream(),
                        4096);


                resourceResponse.setProperty("Cache-Control", "max-age="
                        + resourceResponse.getCacheControl().getExpirationTime());
                resourceResponse.setProperty("Last-Modified", this.formatResourceLastModified());
            }


        } catch( NuxeoException e){
            this.serveResourceException(  resourceRequest,  resourceResponse, e);

        }

        catch (Exception e) {
            throw new PortletException(e);

        }
    }


    /**
     * Create Nuxeo controller.
     *
     * @param portletRequest portlet request
     * @param portletResponse portlet response
     * @return Nuxeo controller
     */
    protected NuxeoController createNuxeoController(PortletRequest portletRequest, PortletResponse portletResponse) {
        return new NuxeoController(portletRequest, portletResponse, this.getPortletContext());
    }

}
