/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.api.services;

import java.util.List;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.ThreadPostDTO;

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
    List<CommentDTO> getDocumentComments(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Get forum thread posts list.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @return thread posts list
     * @throws CMSException
     */
    List<ThreadPostDTO> getForumThreadPosts(CMSServiceCtx cmsContext, Document document) throws CMSException;


    /**
     * Add Nuxeo document comment.
     *
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @param comment comment to add
     * @param parentId parent comment identifier, may be null
     * @throws CMSException
     */
    void addDocumentComment(CMSServiceCtx cmsContext, Document document, CommentDTO comment, String parentId) throws CMSException;


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
