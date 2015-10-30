package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.domain.ICmsItemAdapterModule;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.WebConfigurationQueryCommand.WebConfigurationType;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Default CMS item adapter module.
 *
 * @author Cédric Krommenhoek
 * @see ICmsItemAdapterModule
 */
public class DefaultCmsItemAdapterModule implements ICmsItemAdapterModule {

    /** CMS service. */
    private final CMSService cmsService;


    /**
     * Constructor.
     *
     * @param cmsService CMS service
     */
    public DefaultCmsItemAdapterModule(CMSService cmsService) {
        super();

        // CMS service
        this.cmsService = cmsService;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void adaptNavigationProperties(CMSServiceCtx cmsContext, Document document, Map<String, String> properties) throws CMSException {
        try {
            // Compute domain path
            String domainPath = WebConfigurationHelper.getDomainPath(cmsContext);

            if (domainPath != null) {
                // Get configs installed in nuxeo
                WebConfigurationQueryCommand command = new WebConfigurationQueryCommand(domainPath, WebConfigurationType.CMS_NAVIGATION_ADAPTER);
                Documents configs = WebConfigurationHelper.executeWebConfigCmd(cmsContext, this.cmsService, command);

                if (configs.size() > 0) {
                    for (Document config : configs) {
                        String documentType = config.getProperties().getString(WebConfigurationHelper.CODE);
                        String urlAdapted = config.getProperties().getString(WebConfigurationHelper.ADDITIONAL_CODE);

                        if (document.getType().equals(documentType)) {
                            String path = CMSItemAdapter.computeNavPath(urlAdapted);
                            properties.put("navigationPath", path);
                            break;
                        }
                    }
                }
            }
        } catch (CMSException e) {
            throw e;
        } catch (NuxeoException e) {
            e.rethrowCMSException();
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }

}
