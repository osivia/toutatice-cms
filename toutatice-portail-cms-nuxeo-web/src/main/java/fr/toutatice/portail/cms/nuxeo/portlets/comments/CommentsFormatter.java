package fr.toutatice.portail.cms.nuxeo.portlets.comments;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.osivia.portal.api.HTMLConstants;

import fr.toutatice.portail.cms.nuxeo.api.domain.Comment;

/**
 * Nuxeo document comments formatter.
 *
 * @author Cédric Krommenhoek
 */
public class CommentsFormatter {

    /** Document comments. */
    private final List<Comment> comments;


    /**
     * Constructor.
     *
     * @param comments document comments
     */
    public CommentsFormatter(List<Comment> comments) {
        super();
        this.comments = comments;
    }


    /**
     * HTML content generator entry point.
     *
     * @return HTML content
     */
    public String generateHTMLContent() {
        Element root = this.generateRootNode();

        for (Comment comment : this.comments) {
            Element commentNode = this.generateCommentNode(comment);
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
     * @return comment node
     */
    protected Element generateCommentNode(Comment comment) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "comment");

        // Author
        Element author = this.generateCommentAttribute("author", comment.getAuthor());
        node.add(author);

        // Creation date
        Element creationDate = this.generateCommentAttribute("creation-date", comment.getCreationDate());
        node.add(creationDate);

        // Content
        Element content = this.generateCommentAttribute("content", comment.getCreationDate());
        node.add(content);

        // Action
        Element actions = this.generateCommentActions(comment);
        node.add(actions);

        // Children
        if (CollectionUtils.isNotEmpty(comment.getChildren())) {
            Element children = this.generateChildrenNode(comment);
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
    protected Element generateCommentActions(Comment comment) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "actions");

        // TODO

        return node;
    }


    /**
     * Generate comment children node.
     *
     * @param comment comment
     * @return comment children node
     */
    protected Element generateChildrenNode(Comment comment) {
        Element node = new DOMElement(QName.get(HTMLConstants.DIV));
        node.addAttribute(QName.get(HTMLConstants.CLASS), "children");

        for (Comment child : comment.getChildren()) {
            Element childNode = this.generateCommentNode(child);
            node.add(childNode);
        }

        return node;
    }

}
