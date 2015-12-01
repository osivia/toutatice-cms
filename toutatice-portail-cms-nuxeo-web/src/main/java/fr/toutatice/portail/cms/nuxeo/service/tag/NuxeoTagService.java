package fr.toutatice.portail.cms.nuxeo.service.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.portalobjects.PortalObjectUtils;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.CustomizationPluginMgr;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Nuxeo tag service implementation.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoTagService
 */
public class NuxeoTagService implements INuxeoTagService {

    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;
    /** WebId service. */
    private final IWebIdService webIdService;
    /** Directory service locator. */
    private final IDirectoryServiceLocator directoryServiceLocator;


    /**
     * Constructor.
     */
    public NuxeoTagService() {
        super();

        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        // WebId service
        this.webIdService = Locator.findMBean(IWebIdService.class, IWebIdService.MBEAN_NAME);
        // Directory service locator
        this.directoryServiceLocator = Locator.findMBean(IDirectoryServiceLocator.class, IDirectoryServiceLocator.MBEAN_NAME);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getDocumentLink(NuxeoController nuxeoController, DocumentDTO document, String property, String displayContext, boolean picture,
            boolean permalink) {
        // Link
        Link link;

        // Nuxeo document
        Document nuxeoDocument;
        if (document != null) {
            nuxeoDocument = document.getDocument();
        } else if (nuxeoController != null) {
            nuxeoDocument = nuxeoController.getCurrentDoc();
        } else {
            nuxeoDocument = null;
        }

        if (nuxeoDocument != null) {
            if (picture) {
                // Picture
                String url;
                if (StringUtils.isEmpty(property)) {
                    url = nuxeoController.createPictureLink(nuxeoDocument.getPath(), StringUtils.defaultIfEmpty(displayContext, "Original"));
                } else if (nuxeoDocument.getProperties().getMap(property) != null) {
                    url = nuxeoController.createFileLink(nuxeoDocument, property);
                } else {
                    url = null;
                }

                if (url != null) {
                    link = new Link(url, false);
                } else {
                    link = null;
                }
            } else if (permalink) {
                // Permalink

                // Page
                Page page = null;
                Window window = (Window) nuxeoController.getRequest().getAttribute("osivia.window");
                if (window != null) {
                    page = window.getPage();
                }

                try {
                    String path = document.getPath();
                    if (PortalObjectUtils.isSpaceSite(page)) {
                        CMSService cmsService = (CMSService) NuxeoController.getCMSService();
                        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();
                        CMSItem cmsItem = cmsService.createItem(cmsContext, document.getPath(), null, nuxeoDocument);

                        if (StringUtils.isNotEmpty(cmsItem.getWebId())) {
                            path = this.webIdService.itemToPageUrl(cmsContext, cmsItem);
                        }
                    }

                    String url = nuxeoController.getPortalUrlFactory().getPermaLink(nuxeoController.getPortalCtx(), null, null, path,
                            IPortalUrlFactory.PERM_LINK_TYPE_CMS);
                    link = new Link(url, false);
                } catch (Exception e) {
                    link = new Link("#", false);
                }
            } else if (StringUtils.isEmpty(property)) {
                // Document link
                link = nuxeoController.getLink(nuxeoDocument, StringUtils.trimToNull(displayContext));
            } else {
                // Property value
                String value = String.valueOf(document.getProperties().get(property));
                link = nuxeoController.getLinkFromNuxeoURL(value);
            }
        } else {
            link = null;
        }

        return link;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryPerson getDirectoryPerson(NuxeoController nuxeoController, String name) {
        DirectoryPerson person = null;

        // Directory service
        IDirectoryService directoryService = this.directoryServiceLocator.getDirectoryService();
        if (directoryService != null) {
            // User LDAP person
            person = directoryService.getPerson(name);
        }

        return person;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getUserProfileLink(NuxeoController nuxeoController, String name, String displayName) {
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

        // Page properties
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("osivia.hideTitle", "1");
        properties.put("osivia.ajaxLink", "1");
        properties.put("theme.dyna.partial_refresh_enabled", "true");
        properties.put("uidFichePersonne", name);

        // Page parameters
        Map<String, String> parameters = new HashMap<String, String>(0);

        // Link
        Link link;
        try {
            String url = this.portalUrlFactory.getStartPortletInNewPage(portalControllerContext, "myprofile", displayName, "directory-person-card-instance",
                    properties, parameters);
            link = new Link(url, false);
        } catch (PortalException e) {
            link = null;
        }

        return link;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Link getNuxeoIconLink(NuxeoController nuxeoController, String contextPath, DocumentDTO document) {
        String url = contextPath + Formater.formatNuxeoIcon(document.getDocument());
        return new Link(url, false);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CustomizedJsp getCustomizedJsp(NuxeoController nuxeoController, String name) throws IOException {
        // CMS customizer
        DefaultCMSCustomizer cmsCustomizer = (DefaultCMSCustomizer) nuxeoController.getNuxeoCMSService().getCMSCustomizer();
        // Plugin manager
        CustomizationPluginMgr pluginManager = cmsCustomizer.getPluginMgr();

        return pluginManager.customizeJSP(name, nuxeoController.getPortletCtx(), nuxeoController.getRequest());
    }

}
