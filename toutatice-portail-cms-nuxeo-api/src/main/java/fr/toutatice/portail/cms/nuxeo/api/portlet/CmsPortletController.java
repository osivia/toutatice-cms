package fr.toutatice.portail.cms.nuxeo.api.portlet;

import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.editor.EditorService;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.portlet.PortletController;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;

/**
 * Spring CMS portlet controller abstract super-class.
 *
 * @author Cédric Krommenhoek
 * @see PortletController
 */
public abstract class CmsPortletController extends PortletController {

    /**
     * Portlet context.
     */
    @Autowired
    private PortletContext portletContext;

    /**
     * Editor service.
     */
    @Autowired
    private EditorService editorService;


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

        this.editorService.serveResource(portalControllerContext, editorId);
    }

}
