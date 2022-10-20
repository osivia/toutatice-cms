package fr.toutatice.portail.cms.nuxeo.service.tag;

import java.io.IOException;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.cms.DocumentType;
import org.osivia.portal.api.cms.FileMimeType;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.portalobject.bridge.PortalObjectUtils;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.web.IWebIdService;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.bridge.Formater;
import fr.toutatice.portail.cms.nuxeo.portlets.cms.ExtendedDocumentInfos;
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

    /** WebId service. */
    private final IWebIdService webIdService;
    /** Person service */
    private PersonService personService;
    /** Internationalization bundle factory. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public NuxeoTagService() {
        super();

        // WebId service
        this.webIdService = Locator.findMBean(IWebIdService.class, IWebIdService.MBEAN_NAME);
        // Person service

        this.personService = DirServiceFactory.getService(PersonService.class);
        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
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
                    String url = nuxeoController.getPortalUrlFactory().getViewContentUrl(nuxeoController.getPortalCtx(), nuxeoController.getUniversalIDFromPath(path));
                    link = new Link(url, false);
                } catch (Exception e) {
                    link = new Link("#", false);
                }
            } else if (StringUtils.isEmpty(property)) {
            	
                // Document Contextual Link: LBI #1609
            	String url = nuxeoDocument.getString("clink:link");
				if(url != null && "contextualLink".equals(displayContext)) {
										
            		link = new Link(url, true);
            	}
            	else {
            		link = nuxeoController.getLink(nuxeoDocument, StringUtils.trimToNull(displayContext));	
            	}
            	
                
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
     * {@Override}
     */
    public Link getPreviewFileLink(NuxeoController nuxeoController, DocumentDTO document) {
        // Preview link
        Link previewLink = null;

        // Extended document informations.
        ExtendedDocumentInfos infos = null;

        try {
            ICMSService cmsService = NuxeoController.getCMSService();
            if (cmsService.getClass().isAssignableFrom(CMSService.class)) {
                CMSService cmsServiceImpl = (CMSService) cmsService;
                infos = cmsServiceImpl.getExtendedDocumentInfos(nuxeoController.getCMSCtx(), document.getPath());
            }
        } catch (CMSException e) {
            // Nothing to do
        }

        if (infos != null) {
            // Is convertible to pdf
            if (BooleanUtils.isTrue(infos.isPdfConvertible())) {
                // File link
                String createFileLink = nuxeoController.createFileLink(document.getDocument(), "pdf:content");

                StringBuffer previewLinkStr;
                previewLinkStr = new StringBuffer();
                previewLinkStr.append(createFileLink);
                previewLink = new Link(previewLinkStr.toString(), false);
            }
        }

        return previewLink;
    }


    
    /**
     * {@Override}
     */
    @Override
    public String getResourceContext() {
        return NuxeoController.getCMSNuxeoWebContextName();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Link getUserProfileLink(NuxeoController nuxeoController, String name, String displayName) {
    	Person person = personService.getPerson(name);
    	Link link = null;
    	if(person != null) {
    		try {
				link = personService.getCardUrl(nuxeoController.getPortalCtx(), person);
			} catch (PortalException e) {
				// Do nohing
			}
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
        CustomizationPluginMgr pluginManager = cmsCustomizer.getPluginManager();

        return pluginManager.customizeJSP(name, nuxeoController.getPortletCtx(), nuxeoController.getRequest());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Element getDocumentIcon(NuxeoController nuxeoController, DocumentDTO documentDto, String style) throws IOException {
        // CMS customizer
        INuxeoCustomizer customizer = nuxeoController.getNuxeoCMSService().getCMSCustomizer();
        
        // Nuxeo document
        Document nuxeoDocument = documentDto.getDocument();
        // Document type
        DocumentType documentType = documentDto.getType();

        // Nuxeo document file content;
        PropertyMap fileContent;
        if (nuxeoDocument == null) {
            fileContent = null;
        } else {
            fileContent = nuxeoDocument.getProperties().getMap("file:content");
        }

        // File MIME type
        FileMimeType fileMimeType;
        if (fileContent == null) {
            fileMimeType = null;
        } else {
            fileMimeType = customizer.getFileMimeType(fileContent.getString("mime-type"));
        }

        return this.getIcon(nuxeoController, fileMimeType, documentType, style);
    }


    @Override
    public Element getMimeTypeIcon(NuxeoController nuxeoController, String mimeType, String style) throws IOException {
        // CMS customizer
        INuxeoCustomizer customizer = nuxeoController.getNuxeoCMSService().getCMSCustomizer();

        // File MIME type
        FileMimeType fileMimeType = customizer.getFileMimeType(mimeType);

        return this.getIcon(nuxeoController, fileMimeType, null, style);
    }


    /**
     * Get icon DOM element.
     * 
     * @param nuxeoController Nuxeo controller
     * @param fileMimeType file MIME type, may be null
     * @param documentType documen type, may be null
     * @param style icon style
     * @return DOM element
     */
    private Element getIcon(NuxeoController nuxeoController, FileMimeType fileMimeType, DocumentType documentType, String style) {
        // Portlet request
        PortletRequest request = nuxeoController.getRequest();
        // Internationalization bundle
        Bundle bundle = this.bundleFactory.getBundle(request.getLocale());


        // Title
        String title;
        if (fileMimeType != null) {
            title = fileMimeType.getDescription();
        } else if (documentType != null) {
            title = bundle.getString(StringUtils.upperCase(documentType.getName()), documentType.getCustomizedClassLoader());
        } else {
            title = null;
        }

        // Display
        String display;
        if (fileMimeType != null) {
            display = fileMimeType.getDisplay();
        } else {
            display = null;
        }

        // Icon
        String icon;
        if (fileMimeType != null) {
            icon = fileMimeType.getIcon();
        } else if (documentType != null) {
            icon = documentType.getIcon();
        } else {
            // Unknown
            icon = null;
        }

        // Size
        int size;
        if (StringUtils.isNotEmpty(display)) {
            size = display.length();
        } else {
            size = 1;
        }


        // Document type container DOM element
        String htmlClass = "document-type document-type-" + StringUtils.defaultIfBlank(style, "inline");
        Element container = DOM4JUtils.generateElement("span", htmlClass, StringUtils.EMPTY);
        if (StringUtils.isNotEmpty(title)) {
            DOM4JUtils.addAttribute(container, "title", title);
        }
        DOM4JUtils.addDataAttribute(container, "size", String.valueOf(size));
        if (fileMimeType != null) {
            if (fileMimeType.getMimeType() != null) {
                DOM4JUtils.addDataAttribute(container, "primary-type", fileMimeType.getMimeType().getPrimaryType());
                DOM4JUtils.addDataAttribute(container, "sub-type", fileMimeType.getMimeType().getSubType());
            }
            if (StringUtils.isNotEmpty(fileMimeType.getExtension())) {
                DOM4JUtils.addDataAttribute(container, "extension", fileMimeType.getExtension());
            }
        } else if (documentType == null) {
            // Unknown
            DOM4JUtils.addDataAttribute(container, "unknown", StringUtils.EMPTY);
        }
        if (StringUtils.isNotEmpty(display)) {
            DOM4JUtils.addDataAttribute(container, "display", display);
        } else {
            Element inner = DOM4JUtils.generateElement("i", icon, StringUtils.EMPTY);
            container.add(inner);
        }

        return container;
    }

}
