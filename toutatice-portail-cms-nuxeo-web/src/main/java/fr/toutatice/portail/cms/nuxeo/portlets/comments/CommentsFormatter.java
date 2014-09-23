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
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.osivia.portal.api.html.HTMLConstants;

import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;

/**
 * Nuxeo document comments formatter.
 *
 * @author Cédric Krommenhoek
 */
public class CommentsFormatter {

    /** Document comments. */
    private final List<CommentDTO> comments;


    /**
     * Constructor.
     *
     * @param comments document comments
     */
    public CommentsFormatter(List<CommentDTO> comments) {
        super();
        this.comments = comments;
    }


    /**
     * HTML content generator entry point.
     *
     * @param locale locale
     * @return HTML content
     */
    public String generateHTMLContent(Locale locale) {
        Element root = this.generateRootNode();

        for (CommentDTO comment : this.comments) {
            Element commentNode = this.generateCommentNode(comment, locale);
            root.add(commentNode);
        }

        return root.asXML();
    }


    /**
     * Generate root node.
     *
     * @return root node
     */
    protected Element generateRootNode() {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "comments");
        return node;
    }


    /**
     * Generate comment node.
     * 
     * @param comment comment
     * @param locale locale
     * @return comment node
     */
    protected Element generateCommentNode(CommentDTO comment, Locale locale) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "comment");

        // Avatar
        if (comment.getPerson() != null) {
            Element avatarImg = new DOMElement(QName.get(HTMLConstants.IMG));
            avatarImg.addAttribute(QName.get(HTMLConstants.SRC), comment.getPerson().getAvatar().getUrl());
            avatarImg.addAttribute(QName.get(HTMLConstants.ALT), "avatar");
            avatarImg.addAttribute(QName.get(HTMLConstants.CLASS), "avatar");

            node.add(avatarImg);

            // Author
            Element author = this.generateCommentAttribute("author", comment.getPerson().getDisplayName());
            node.add(author);
        } else {
            // Author
            Element author = this.generateCommentAttribute("author", comment.getAuthor());
            node.add(author);
        }


        // Creation date
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
        Element creationDate = this.generateCommentAttribute("creation-date", dateFormat.format(comment.getCreationDate()));
        node.add(creationDate);

        // Content
        Element content = this.generateCommentAttribute("content", comment.getContent());
        node.add(content);

        // Action
        Element actions = this.generateCommentActions(comment);
        node.add(actions);

        // Children
        if (CollectionUtils.isNotEmpty(comment.getChildren())) {
            Element children = this.generateChildrenNode(comment, locale);
            node.add(children);
        }

        return node;
    }


    /**
     * Generate comment attribute node.
     *
     * @param name attribute name, used for HTML class
     * @param value attribute value
     * @return comment attribute node
     */
    protected Element generateCommentAttribute(String name, String value) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), name);
        node.setText(value);
        return node;
    }


    /**
     * Generate comment actions node.
     *
     * @param comment comment
     * @return comment actions node
     */
    protected Element generateCommentActions(CommentDTO comment) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "actions");

        // TODO

        return node;
    }


    /**
     * Generate comment children node.
     *
     * @param comment comment
     * @param locale locale
     * @return comment children node
     */
    protected Element generateChildrenNode(CommentDTO comment, Locale locale) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "children");

        for (CommentDTO child : comment.getChildren()) {
            Element childNode = this.generateCommentNode(child, locale);
            node.add(childNode);
        }

        return node;
    }

}
