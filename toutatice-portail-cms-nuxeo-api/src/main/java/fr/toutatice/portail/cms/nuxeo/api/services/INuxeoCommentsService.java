package fr.toutatice.portail.cms.nuxeo.api.services;

import java.util.List;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.domain.Comment;
import fr.toutatice.portail.cms.nuxeo.api.domain.ThreadPost;

/**
 * Nuxeo comments service interface.
 *
 * @author Cédric Krommenhoek
 */
public interface INuxeoCommentsService {

    /**
     * Get Nuxeo document comments list.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @return comments list
     * @throws CMSException
     */
    List<Comment> getDocumentComments(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Get forum thread posts list.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @return thread posts list
     * @throws CMSException
     */
    List<ThreadPost> getForumThreadPosts(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Add Nuxeo document comment.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @param comment comment to add
     * @param parentId parent comment identifier, may be null
     * @throws CMSException
     */
    void addDocumentComment(CMSServiceCtx cmsContext, Document document, Comment comment, String parentId) throws CMSException;


    /**
     * Delete Nuxeo document comment.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @param id comment identifier
     * @throws CMSException
     */
    void deleteDocumentComment(CMSServiceCtx cmsContext, Document document, String id) throws CMSException;

}
