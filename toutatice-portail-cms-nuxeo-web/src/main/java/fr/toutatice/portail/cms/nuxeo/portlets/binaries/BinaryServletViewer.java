package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.liveedit.OnlyofficeLiveEditHelper;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/* 
 * Classe prototype de visionneuse 
 * 
 * Un problème important à traiter : 
 * - la mise en cluster des jetons Web
 * 
 * Penser à packager la liste des js, css, ..
 * 
 */

public class BinaryServletViewer {

    /** Log. */
    public static Log log;
    
    private static final String ONLYOFFICE_PORTAL_URL = System.getProperty("osivia.onlyoffice.portal.url", StringUtils.EMPTY);
    
    public static void generateViewer(PortletContext portletContext, HttpServletRequest request, HttpServletResponse response) throws IOException {

        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setServletRequest(request);
        
        //TODO :  utiliser les web tokens, ...
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setForcePublicationInfosScope("superuser_context");

        String id = request.getParameter("id");

        Document doc = nuxeoController.getDocumentContext(IWebIdService.FETCH_PATH_PREFIX + id).getDocument();
        PropertyMap properties = doc.getProperties();
        PropertyMap fileContent = properties.getMap("file:content");
        String filename = fileContent.getString("name");
        String docExtension = StringUtils.substringAfterLast(filename, ".");
        String mimeType = fileContent.getString("mime-type");
        String fileType= OnlyofficeLiveEditHelper.getFileType(mimeType).name();        

        OutputStream output = response.getOutputStream();

        String url = ONLYOFFICE_PORTAL_URL+"/"+NuxeoController.getCMSNuxeoWebContextName()+"/binary";
        String params = "";

        if (request.getParameter("linkId") != null)
            params += "linkId=" + request.getParameter("linkId");

        if (request.getParameter("webToken") != null) {
            if (params.length() > 0)
                params += "&";
            params += "webToken=" + request.getParameter("webToken");
        }

        if (params.length() > 0)
            params += "&";
        params += "type=FILE";


        if (params.length() > 0)
            params += "&";
        params += "path=" + IWebIdService.FETCH_PATH_PREFIX + id;


        if (params.length() > 0)
            params += "&";
        params += "fieldName=file:content";


        url += "?" + params;

        String html = "" + "<html><head>"
                + "<script type='text/javascript' src='/osivia-portal-custom-web-assets/components/jquery/jquery-1.12.4.min.js'>"
                + "</script><script type='text/javascript' src='/osivia-portal-custom-web-assets/js/jquery-integration.min.js'></script>"
                + "<link rel='stylesheet' href='/osivia-portal-custom-web-assets/css/bootstrap.min.css' title='Bootstrap'>"
                + "<link rel='stylesheet' href='/osivia-portal-custom-web-assets/css/osivia.min.css'>"
                + "<script src='/osivia-portal-custom-web-assets/components/bootstrap/js/bootstrap.min.js'></script>"
                + "<link rel=\"stylesheet\" href=\"/osivia-services-onlyoffice/css/onlyoffice-integration.css\"/>" + "" + ""
                + "</head><body class=\"fixed-layout\">"
                + "<script type=\"text/javascript\" src=\"/osivia-services-onlyoffice/js/onlyoffice-integration.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\"/onlyoffice/web-apps/apps/api/documents/api.js\"></script>\n" + "\n" + "\n" + "<main>"
                + "<div class=\"portlet-filler flexbox\">" +
                "<div id=\"onlyoffice-placeholder\" data-onlyoffice-config='{\"document\":{\"title\":\""+filename+"\",\"fileType\":\""+docExtension+"\",\"url\":\"" + url
                + "\"},\"height\":\"\",\"documentType\":\""+fileType+"\",\"width\":\"\",\"type\":\"\",\"editorConfig\":{\"customization\":{\"chat\":false},\"lang\":\"fr-FR\",\"mode\":\"view\"}}' class=\"flexbox\"></div>"
                + "</div>" + "</main>" + "<body>";
        output.write(html.getBytes());

    }

}