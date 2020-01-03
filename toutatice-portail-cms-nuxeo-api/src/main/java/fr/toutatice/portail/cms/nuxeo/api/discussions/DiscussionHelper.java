package fr.toutatice.portail.cms.nuxeo.api.discussions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.constants.InternalConstants;

/**
 * The Class DiscussionHelper.
 */
public class DiscussionHelper {


    /** The portal url factory. */
    private static IPortalUrlFactory portalUrlFactory;

    /** The bundle factory. */
    private static IBundleFactory bundleFactory;


    
    /**
     * Gets the url factory.
     *
     * @return the url factory
     */
    private static IPortalUrlFactory getUrlFactory() {
        if (portalUrlFactory == null)
            portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        return portalUrlFactory;
    }


    /**
     * Get bundle factory.
     *
     * @return bundle factory
     */

    private static IBundleFactory getBundleFactory() {
        if (bundleFactory == null) {
            IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                    IInternationalizationService.MBEAN_NAME);
            bundleFactory = internationalizationService.getBundleFactory(DiscussionHelper.class.getClassLoader());
        }

        return bundleFactory;
    }


    public static String getDiscussionUrlByParticipant(PortalControllerContext portalControllerContext, String participant)  {


        // Internationalization bundle
        Bundle bundle = getBundleFactory().getBundle(portalControllerContext.getRequest().getLocale());


        Map<String, String> properties = new HashMap<>();

        // TODO : bundle
        properties.put(InternalConstants.PROP_WINDOW_TITLE, "Discussion");
        properties.put("osivia.ajaxLink", "1");
        properties.put("osivia.hideTitle", "1");
        properties.put(DynaRenderOptions.PARTIAL_REFRESH_ENABLED, String.valueOf(true));
        Map<String, String> params = new HashMap<>();
        params.put("view", "detail");
        params.put("participant", participant);
        params.put("anchor", "newMessage");


        // URL
        String url;
        try {
            url = getUrlFactory().getStartPortletInNewPage(portalControllerContext, "discussion", "Discussion", "index-cloud-ens-discussion-instance",
                    properties, params);
        } catch (PortalException e) {
            url = "#";
        }

        return url;
    }

}
