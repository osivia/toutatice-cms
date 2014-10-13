/**
 *
 */
package fr.toutatice.portail.cms.nuxeo.portlets.fragment;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.portal.core.cms.IFragmentModule;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Link fragment module.
 *
 * @see IFragmentModule
 */
public class LinkFragmentModule implements IFragmentModule {

    /** Link fragment identifier. */
    public static final String ID = "doc_link";

    /** Link name window property name. */
    public static final String NAME_WINDOW_PROPERTY = "osivia.linkName";
    /** Link target path window property name. */
    public static final String TARGET_PATH_WINDOW_PROPERTY = "osivia.docPathForLink";
    /** Nuxeo link indicator window property name. */
    public static final String NUXEO_LINK_WINDOW_PROPERTY = "osivia.isNuxeoLink";
    /** CSS classes window property name. */
    public static final String CSS_CLASSES_WINDOW_PROPERTY = "osivia.cssLinkClass";

    /** JSP name. */
    private static final String JSP_NAME = "link";

    /** Singleton instance. */
    private static IFragmentModule instance;


    /**
     * Private constructor.
     */
    private LinkFragmentModule() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static IFragmentModule getInstance() {
        if (instance == null) {
            instance = new LinkFragmentModule();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();
        // Response
        PortletResponse response = portalControllerContext.getResponse();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, portalControllerContext.getPortletCtx());

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);
        // Link name
        String name = window.getProperty(NAME_WINDOW_PROPERTY);
        // Link target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);
        // Nuxeo link indicator
        boolean nuxeoLink = BooleanUtils.toBoolean(window.getProperty(NUXEO_LINK_WINDOW_PROPERTY));
        // CSS classes
        String cssClasses = window.getProperty(CSS_CLASSES_WINDOW_PROPERTY);

        if (StringUtils.isNotEmpty(targetPath)) {
            // Computed path
            targetPath = nuxeoController.getComputedPath(targetPath);

            if (nuxeoLink) {
                nuxeoController.setDisplayLiveVersion("1");
            }

            // Fetch Nuxeo document
            Document document = nuxeoController.fetchDocument(targetPath);

            // Update link name with property value
            String property = document.getProperties().getString(name);
            if (StringUtils.isNotEmpty(property)) {
                name = property;
            }
            request.setAttribute("name", name);

            // Link target
            Link link;
            if (nuxeoLink) {
                link = nuxeoController.getLink(document, "nuxeo-link");
            } else {
                link = nuxeoController.getLink(document);
            }
            request.setAttribute("link", link);

            request.setAttribute("cssClasses", cssClasses);
        } else {
            request.setAttribute("messageKey", "MESSAGE_PATH_UNDEFINED");
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Link name
        String name = window.getProperty(NAME_WINDOW_PROPERTY);
        request.setAttribute("name", name);

        // Link target path
        String targetPath = window.getProperty(TARGET_PATH_WINDOW_PROPERTY);
        request.setAttribute("targetPath", targetPath);

        // Nuxeo link indicator
        boolean nuxeoLink = BooleanUtils.toBoolean(window.getProperty(NUXEO_LINK_WINDOW_PROPERTY));
        request.setAttribute("nuxeoLink", nuxeoLink);

        // CSS classes
        String cssClasses = window.getProperty(CSS_CLASSES_WINDOW_PROPERTY);
        request.setAttribute("cssClasses", cssClasses);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAdminAction(PortalControllerContext portalControllerContext) throws PortletException {
        // Request
        PortletRequest request = portalControllerContext.getRequest();

        // Current window
        PortalWindow window = WindowFactory.getWindow(request);

        // Link name
        window.setProperty(NAME_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("name")));

        // Link target path
        window.setProperty(TARGET_PATH_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("targetPath")));

        // Nuxeo link indicator
        window.setProperty(NUXEO_LINK_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("nuxeoLink")));

        // CSS classes
        window.setProperty(CSS_CLASSES_WINDOW_PROPERTY, StringUtils.trimToNull(request.getParameter("cssClasses")));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplayedInAdmin() {
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewJSPName() {
        return JSP_NAME;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getAdminJSPName() {
        return JSP_NAME;
    }

}
