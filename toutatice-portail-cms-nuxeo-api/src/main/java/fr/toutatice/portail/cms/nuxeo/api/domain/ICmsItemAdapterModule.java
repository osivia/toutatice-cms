package fr.toutatice.portail.cms.nuxeo.api.domain;

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

/**
 * CMS item adapter module interface.
 *
 * @author Cédric Krommenhoek
 */
public interface ICmsItemAdapterModule {

    /**
     * Adapt navigation properties.
     *
     * @param cmsContext CMS context
     * @param document document
     * @param properties document properties
     * @throws CMSException
     */
    void adaptNavigationProperties(CMSServiceCtx cmsContext, Document document, Map<String, String> properties) throws CMSException;

}
