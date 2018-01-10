package fr.toutatice.portail.cms.nuxeo.api.portlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.portlet.PortletController;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.PortalUrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import net.sf.json.JSONObject;

/**
 * Spring CMS portlet controller abstract super-class.
 * 
 * @author Cédric Krommenhoek
 * @see PortletController
 */
public abstract class CmsPortletController extends PortletController {

    /** Portlet context. */
    @Autowired
    private PortletContext portletContext;

    /** Portal URL factory. */
    @Autowired
    private IPortalUrlFactory portalUrlFactory;

    /** Internationalization bundle factory. */
    @Autowired
    private IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public CmsPortletController() {
        super();
    }


    /**
     * Get editor properties resource mapping.
     *
     * @param request resource request
     * @param response resource response
     * @param editorId editor identifier required request parameter
     * @throws PortletException
     * @throws IOException
     */
    @ResourceMapping("editor")
    public void getEditorProperties(ResourceRequest request, ResourceResponse response, @RequestParam(name = "editorId") String editorId)
            throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.portletContext, request, response);
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

}
