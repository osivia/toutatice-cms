package fr.toutatice.portail.cms.nuxeo.api.services.tag;

import java.io.IOException;

import org.dom4j.Element;
import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CustomizedJsp;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;

/**
 * Nuxeo tag service interface.
 *
 * @author Cédric Krommenhoek
 */
public interface INuxeoTagService {

    /**
     * Get document link.
     *
     * @param nuxeoController Nuxeo controller
     * @param document document DTO
     * @param property document link property name
     * @param displayContext display context
     * @param picture picture indicator
     * @param permalink permalink indicator
     * @return link
     */
    Link getDocumentLink(NuxeoController nuxeoController, DocumentDTO document, String property, String displayContext, boolean picture, boolean permalink);
    
    /**
     * Get file preview link.
     * 
     * @param nuxeoController Nuxeo controller
     * @param document
     * @return link
     */
    Link getPreviewFileLink(NuxeoController nuxeoController, DocumentDTO document);

    /**
     * Get user profile link.
     *
     * @param nuxeoController Nuxeo controller
     * @param name user name document DTO
     * @param displayName user display name
     * @return link
     */
    Link getUserProfileLink(NuxeoController nuxeoController, String name, String displayName);


    /**
     * Get Nuxeo document icon link.
     *
     * @param nuxeoController Nuxeo controller
     * @param contextPath context path
     * @param document document DTO
     * @return link
     */
    Link getNuxeoIconLink(NuxeoController nuxeoController, String contextPath, DocumentDTO document);


    /**
     * Get customized JavaServer page.
     *
     * @param nuxeoController Nuxeo controller
     * @param name JSP name
     * @return customized JSP
     */
    CustomizedJsp getCustomizedJsp(NuxeoController nuxeoController, String name) throws IOException;


    /**
     * Get document icon DOM element.
     * 
     * @param nuxeoController Nuxeo controller
     * @param document document DTO
     * @param style icon style
     * @return DOM element
     * @throws IOException
     */
    Element getDocumentIcon(NuxeoController nuxeoController, DocumentDTO document, String style) throws IOException;


    /**
     * Get MIME type icon.
     * 
     * @param nuxeoController Nuxeo controller
     * @param mimeType MIME type
     * @param style icon style
     * @return DOM element
     * @throws IOException
     */
    Element getMimeTypeIcon(NuxeoController nuxeoController, String mimeType, String style) throws IOException;

    /**
     * Gets the cms resource context .
     *
     * @return the resource context 
     */
    String getResourceContext();

}
