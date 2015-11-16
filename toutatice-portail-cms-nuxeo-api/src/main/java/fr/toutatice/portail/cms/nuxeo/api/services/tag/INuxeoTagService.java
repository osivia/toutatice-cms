package fr.toutatice.portail.cms.nuxeo.api.services.tag;

import java.io.IOException;

import org.osivia.portal.api.urls.Link;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
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
     * Get user display name.
     *
     * @param nuxeoController Nuxeo controller
     * @param name user name
     * @return display name
     */
    String getUserDisplayName(NuxeoController nuxeoController, String name);


    /**
     * Get user profile link.
     *
     * @param nuxeoController Nuxeo controller
     * @param name user name
     * @return link
     */
    Link getUserProfileLink(NuxeoController nuxeoController, String name);


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
     * Get included JSP name.
     *
     * @param nuxeoController Nuxeo controller
     * @param path JSP name
     * @return included JSP name
     */
    String getIncludedJspName(NuxeoController nuxeoController, String name) throws IOException;

}