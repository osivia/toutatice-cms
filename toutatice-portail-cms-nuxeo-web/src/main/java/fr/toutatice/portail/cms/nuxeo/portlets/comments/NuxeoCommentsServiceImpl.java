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
package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.domain.Comment;
import fr.toutatice.portail.cms.nuxeo.api.domain.ThreadPost;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCommentsService;
import fr.toutatice.portail.cms.nuxeo.portlets.service.CMSService;

/**
 * Nuxeo comments service implementation.
 *
 * @author Cédric Krommenhoek
 * @see INuxeoCommentsService
 */
public class NuxeoCommentsServiceImpl implements INuxeoCommentsService {

    /** CMS service. */
    private final CMSService cmsService;


    /**
     * Default constructor.
     *
     * @param cmsService CMS service
     */
    public NuxeoCommentsServiceImpl(CMSService cmsService) {
        super();
        this.cmsService = cmsService;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Comment> getDocumentComments(CMSServiceCtx cmsContext, Document document) throws CMSException {
        return this.getGenericComments(cmsContext, document, Comment.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<ThreadPost> getForumThreadPosts(CMSServiceCtx cmsContext, Document document) throws CMSException {
        return this.getGenericComments(cmsContext, document, ThreadPost.class);
    }


    /**
     * Get generic comments.
     *
     * @param <T> comments parameterized type
     * @param cmsContext CMS context
     * @param document Nuxeo document
     * @param type comments type
     * @return generic comments
     * @throws CMSException
     */
    private <T extends Comment> List<T> getGenericComments(CMSServiceCtx cmsContext, Document document, Class<T> type) throws CMSException {
        try {
            // Locale
            Locale locale = cmsContext.getControllerContext().getServerInvocation().getRequest().getLocale();

            INuxeoCommand command = new GetCommentsCommand(document);
            JSONArray jsonArray = (JSONArray) this.cmsService.executeNuxeoCommand(cmsContext, command);

            return this.convertJSONArrayToComments(jsonArray, type, locale);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * Convert JSON array to comments list.
     *
     * @param <T> comments parameterized type
     * @param jsonArray JSON array
     * @param type comments type
     * @param locale current locale
     * @return comments list
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private <T extends Comment> List<T> convertJSONArrayToComments(JSONArray jsonArray, Class<T> type, Locale locale)
            throws InstantiationException, IllegalAccessException {
        List<T> comments = new ArrayList<T>(jsonArray.size());
        for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            T comment = type.newInstance();

            // Identifier
            if (jsonObject.containsKey("id")) {
                String id = jsonObject.getString("id");
                comment.setId(id);
            }

            // Path
            if (jsonObject.containsKey("path")) {
                String path = jsonObject.getString("path");
                comment.setPath(path);
            }

            // Author
            if (jsonObject.containsKey("author")) {
                String author = jsonObject.getString("author");
                comment.setAuthor(author);
            }

            // Creation date
            if (jsonObject.containsKey("creationDate")) {
                JSONObject jsonObjectDate = jsonObject.getJSONObject("creationDate");
                Long time = (Long) jsonObjectDate.get("timeInMillis");
                DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
                String formattedDate = dateFormat.format(time);
                comment.setCreationDate(formattedDate);
            }

            // Content
            if (jsonObject.containsKey("content")) {
                String content = jsonObject.getString("content");
                comment.setContent(content);
            }

            // Deletable indicator
            if (jsonObject.containsKey("canDelete")) {
                boolean deletable = jsonObject.getBoolean("canDelete");
                comment.setDeletable(deletable);
            }

            // Thead post specific attributes
            if (comment instanceof ThreadPost) {
                ThreadPost threadPost = (ThreadPost) comment;

                // Title
                if (jsonObject.containsKey("title")) {
                    String title = jsonObject.getString("title");
                    threadPost.setTitle(title);
                }

                // File name
                if (jsonObject.containsKey("filename")) {
                    String filename = jsonObject.getString("filename");
                    threadPost.setFilename(filename);
                }
            }

            // Children handling
            if (jsonObject.containsKey("children")) {
                JSONArray children = jsonObject.getJSONArray("children");
                List<T> commentChildren = this.convertJSONArrayToComments(children, type, locale);
                comment.setChildren(commentChildren);
            }

            comments.add(comment);
        }
        return comments;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addDocumentComment(CMSServiceCtx cmsContext, Document document, Comment comment, String parentId) throws CMSException {
        try {
            INuxeoCommand command = new AddCommentCommand(document, comment, parentId);
            this.cmsService.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDocumentComment(CMSServiceCtx cmsContext, Document document, String id) throws CMSException {
        try {
            INuxeoCommand command = new DeleteCommentCommand(document, id);
            this.cmsService.executeNuxeoCommand(cmsContext, command);
        } catch (CMSException e) {
            throw e;
        } catch (Exception e) {
            throw new CMSException(e);
        }
    }

}
