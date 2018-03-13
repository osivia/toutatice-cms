package fr.toutatice.portail.cms.nuxeo.api.portlet;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.plugin.PluginModule;

/**
 * Portlet module.
 *
 * @author Cédric Krommenhoek
 * @see PluginModule
 * @see IPortletModule
 */
public abstract class PortletModule extends PluginModule implements IPortletModule {

    /** Bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     *
     * @param portletContext portlet context
     */
    public PortletModule(PortletContext portletContext) {
        super(portletContext);

        // Bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Request
        RenderRequest request = (RenderRequest) portalControllerContext.getRequest();
        // Response
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Portlet context
        PortletContext portletContext = portalControllerContext.getPortletCtx();

        this.doView(request, response, portletContext);
    }


    /**
     * Render view.
     *
     * @param request render request
     * @param response render response
     * @param portletContext portlet context
     * @throws PortletException
     * @throws IOException
     */
    protected void doView(RenderRequest request, RenderResponse response, PortletContext portletContext) throws PortletException, IOException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doAdmin(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Request
        RenderRequest request = (RenderRequest) portalControllerContext.getRequest();
        // Response
        RenderResponse response = (RenderResponse) portalControllerContext.getResponse();
        // Portlet context
        PortletContext portletContext = portalControllerContext.getPortletCtx();

        this.doAdmin(request, response, portletContext);
    }


    /**
     * Render admin.
     *
     * @param request render request
     * @param response render response
     * @param portletContext portlet context
     * @throws PortletException
     * @throws IOException
     */
    protected void doAdmin(RenderRequest request, RenderResponse response, PortletContext portletContext) throws PortletException, IOException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Request
        ActionRequest request = (ActionRequest) portalControllerContext.getRequest();
        // Response
        ActionResponse response = (ActionResponse) portalControllerContext.getResponse();
        // Portlet context
        PortletContext portletContext = portalControllerContext.getPortletCtx();

        this.processAction(request, response, portletContext);
    }


    /**
     * Process action.
     *
     * @param request action request
     * @param response action response
     * @param portletContext portlet context
     * @throws PortletException
     * @throws IOException
     */
    protected void processAction(ActionRequest request, ActionResponse response, PortletContext portletContext) throws PortletException, IOException {
        // Do nothing
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void serveResource(PortalControllerContext portalControllerContext) throws PortletException, IOException {
        // Request
        ResourceRequest request = (ResourceRequest) portalControllerContext.getRequest();
        // Response
        ResourceResponse response = (ResourceResponse) portalControllerContext.getResponse();
        // Portlet context
        PortletContext portletContext = portalControllerContext.getPortletCtx();

        this.serveResource(request, response, portletContext);
    }


    /**
     * Serve resource.
     *
     * @param request resource request
     * @param response resource response
     * @param portletContext portlet context
     * @throws PortletException
     * @throws IOException
     */
    protected void serveResource(ResourceRequest request, ResourceResponse response, PortletContext portletContext) throws PortletException, IOException {
        // Do nothing
    }


    /**
     * Getter for bundleFactory.
     *
     * @return the bundleFactory
     */
    public IBundleFactory getBundleFactory() {
        return this.bundleFactory;
    }

}
