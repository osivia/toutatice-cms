package fr.toutatice.portail.cms.nuxeo.api.services;

import org.osivia.portal.api.locator.Locator;

import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;

/**
 * Nuxeo service factory.
 * 
 * @author Cédric Krommenhoek
 */
public class NuxeoServiceFactory {

    /**
     * Private constructor, prevent instanciation.
     */
    private NuxeoServiceFactory() {
        // Do nothing
    }


    /**
     * Get tag service.
     * 
     * @return tag service
     */
    public static INuxeoTagService getTagService() {
        INuxeoService nuxeoService = getNuxeoService();
        return nuxeoService.getTagService();
    }


    /**
     * Get forms service.
     * 
     * @return forms service
     */
    public static IFormsService getFormsService() {
        INuxeoService nuxeoService = getNuxeoService();
        return nuxeoService.getFormsService();
    }


    /**
     * Get Nuxeo service.
     * 
     * @return Nuxeo service
     */
    private static INuxeoService getNuxeoService() {
        return Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
    }

}
