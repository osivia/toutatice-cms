package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.portlet.ActionRequest;
import javax.portlet.MimeResponse;
import javax.portlet.PortletURL;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.html.AccessibilityRoles;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.CommentDTO;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * Comments tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class CommentsTag extends ToutaticeSimpleTag {

    /** Internationalization service. */
    private final IBundleFactory bundleFactory;


    /**
     * Constructor.
     */
    public CommentsTag() {
        super();

        // Internationalization bundle factory
        IInternationalizationService internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        this.bundleFactory = internationalizationService.getBundleFactory(this.getClass().getClassLoader());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        if (document.isCommentable()) {
            // Internationalization bundle
            Bundle bundle = this.bundleFactory.getBundle(nuxeoController.getRequest().getLocale());

            // Container
            Element container = this.generateContainer(nuxeoController, document, bundle);

            // Delete confirmation fancybox
            Element fancybox = this.generateDeleteConfirmationFancybox(nuxeoController, document, bundle);

            // HTML writer
            HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
            htmlWriter.setEscapeText(false);
            htmlWriter.write(container);
            htmlWriter.write(fancybox);
        }
    }


    /**
     * Generate comments container DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param document Nuxeo document
     * @param bundle internationalization bundle
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateContainer(NuxeoController nuxeoController, DocumentDTO document, Bundle bundle) throws JspException {
        // Response
        MimeResponse response = (MimeResponse) nuxeoController.getResponse();
        // Namespace
        String namespace = nuxeoController.getResponse().getNamespace();
        // Add comment URL
        PortletURL addActionUrl = response.createActionURL();
        addActionUrl.setParameter(ActionRequest.ACTION_NAME, "addComment");
        String addUrl = addActionUrl.toString();


        // Container
        Element container = DOM4JUtils.generateDivElement("hidden-print");

        // Horizontal row
        Element hr = DOM4JUtils.generateElement(HTMLConstants.HR, null, null);
        container.add(hr);

        // Comments container
        Element commentsContainer = DOM4JUtils.generateDivElement("comments");
        container.add(commentsContainer);

        // Panel
        Element panel = DOM4JUtils.generateDivElement("panel panel-default");
        commentsContainer.add(panel);

        // Panel heading
        Element panelHeading = DOM4JUtils.generateDivElement("panel-heading");
        panel.add(panelHeading);

        // Panel title
        Element panelTitle = DOM4JUtils.generateElement(HTMLConstants.H3, "panel-title", bundle.getString("COMMENTS"), "glyphicons glyphicons-conversation",
                null);
        panelHeading.add(panelTitle);

        // Panel list group
        Element listGroup = DOM4JUtils.generateDivElement("list-group");
        panel.add(listGroup);

        for (CommentDTO comment : document.getComments()) {
            // List group item
            Element listGroupItem = DOM4JUtils.generateDivElement("list-group-item");
            listGroupItem.add(this.generateComment(nuxeoController, bundle, comment));
            listGroup.add(listGroupItem);
        }

        // Panel body
        Element panelBody = DOM4JUtils.generateDivElement("panel-body");
        panel.add(panelBody);

        // Add comment toggle button
        Element addToggle = DOM4JUtils.generateLinkElement("#" + namespace + "-add-comment", null, null, "btn btn-default no-ajax-link",
                bundle.getString("COMMENT_ADD"), "glyphicons glyphicons-chat");
        DOM4JUtils.addDataAttribute(addToggle, "toggle", "collapse");
        panelBody.add(addToggle);

        // Add comment collapsed container
        Element addCollapsed = DOM4JUtils.generateDivElement("collapse");
        DOM4JUtils.addAttribute(addCollapsed, HTMLConstants.ID, namespace + "-add-comment");
        panelBody.add(addCollapsed);

        // Add comment horizontal row
        Element addHr = DOM4JUtils.generateElement(HTMLConstants.HR, null, null);
        addCollapsed.add(addHr);

        // Add comment form
        Element addForm = DOM4JUtils.generateElement(HTMLConstants.FORM, "no-ajax-link", null, null, AccessibilityRoles.FORM);
        DOM4JUtils.addAttribute(addForm, HTMLConstants.ACTION, addUrl);
        DOM4JUtils.addAttribute(addForm, HTMLConstants.METHOD, HTMLConstants.FORM_METHOD_POST);
        addCollapsed.add(addForm);

        // Add comment content form group
        Element addContentFormGroup = DOM4JUtils.generateDivElement("form-group");
        addForm.add(addContentFormGroup);

        // Add comment content label
        Element addContentLabel = DOM4JUtils.generateElement(HTMLConstants.LABEL, "control-label", bundle.getString("COMMENT_CONTENT"));
        DOM4JUtils.addAttribute(addContentLabel, HTMLConstants.FOR, namespace + "-comment-content");
        addContentFormGroup.add(addContentLabel);

        // Add comment content textarea
        Element addContentTextarea = DOM4JUtils.generateElement(HTMLConstants.TEXTAREA, "form-control", StringUtils.EMPTY);
        DOM4JUtils.addAttribute(addContentTextarea, HTMLConstants.ID, namespace + "-comment-content");
        DOM4JUtils.addAttribute(addContentTextarea, HTMLConstants.NAME, "content");
        addContentFormGroup.add(addContentTextarea);

        // Add comment buttons form group
        Element addButtonsFormGroup = DOM4JUtils.generateDivElement("form-group");
        addForm.add(addButtonsFormGroup);

        // Add comment submit button
        Element addSubmit = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-primary", bundle.getString("SAVE"));
        DOM4JUtils.addAttribute(addSubmit, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        addButtonsFormGroup.add(addSubmit);

        // Add comment cancel button
        Element addCancel = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("CANCEL"));
        DOM4JUtils.addAttribute(addCancel, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(addCancel, HTMLConstants.ONCLICK, "$JQry('#" + namespace + "-add-comment').collapse('hide')");
        addButtonsFormGroup.add(addCancel);

        return container;
    }


    /**
     * Generate delete confirmation fancybox DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param document Nuxeo document
     * @param bundle internationalization bundle
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateDeleteConfirmationFancybox(NuxeoController nuxeoController, DocumentDTO document, Bundle bundle) throws JspException {
        // Response
        MimeResponse response = (MimeResponse) nuxeoController.getResponse();
        // Namespace
        String namespace = response.getNamespace();
        // URL
        PortletURL actionUrl = response.createActionURL();
        actionUrl.setParameter(ActionRequest.ACTION_NAME, "deleteComment");
        String url = actionUrl.toString();


        // Container
        Element container = DOM4JUtils.generateDivElement("hidden");

        // Fancybox container
        Element fancyboxContainer = DOM4JUtils.generateDivElement("container-fluid");
        DOM4JUtils.addAttribute(fancyboxContainer, HTMLConstants.ID, namespace + "-delete-comment");
        container.add(fancyboxContainer);

        // Form
        Element form = DOM4JUtils.generateElement(HTMLConstants.FORM, "no-ajax-link", null, null, AccessibilityRoles.FORM);
        DOM4JUtils.addAttribute(form, HTMLConstants.ACTION, url);
        DOM4JUtils.addAttribute(form, HTMLConstants.METHOD, HTMLConstants.FORM_METHOD_POST);
        fancyboxContainer.add(form);

        // Identifier hidden input
        Element hidden = DOM4JUtils.generateElement(HTMLConstants.INPUT, null, StringUtils.EMPTY);
        DOM4JUtils.addAttribute(hidden, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_HIDDEN);
        DOM4JUtils.addAttribute(hidden, HTMLConstants.NAME, "id");
        form.add(hidden);

        // Form group
        Element formGroup = DOM4JUtils.generateDivElement("form-group");
        form.add(formGroup);

        // Message
        Element message = DOM4JUtils.generateElement(HTMLConstants.P, null, bundle.getString("COMMENT_SUPPRESSION_CONFIRM_MESSAGE"));
        formGroup.add(message);

        // Buttons container
        Element buttonsContainer = DOM4JUtils.generateDivElement("text-center");
        formGroup.add(buttonsContainer);

        // Submit
        Element submit = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-warning", bundle.getString("YES"), "halflings halflings-alert", null);
        DOM4JUtils.addAttribute(submit, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        buttonsContainer.add(submit);

        // Cancel
        Element cancel = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("NO"));
        DOM4JUtils.addAttribute(cancel, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(cancel, HTMLConstants.ONCLICK, "closeFancybox()");
        buttonsContainer.add(cancel);

        return container;
    }


    /**
     * Generate comment DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateComment(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) throws JspException {
        // Container
        Element container = DOM4JUtils.generateDivElement("comment");

        // Header
        Element header = this.generateHeader(nuxeoController, bundle, comment);
        container.add(header);

        // Content
        Element content = DOM4JUtils.generateElement(HTMLConstants.DIV, null, comment.getContent());
        container.add(content);

        // Children
        Element children = this.generateChildren(nuxeoController, bundle, comment);
        container.add(children);

        return container;
    }


    /**
     * Generate comment header DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateHeader(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) throws JspException {
        // Header
        Element header = DOM4JUtils.generateDivElement("clearfix");

        // Informations
        Element informations = this.generateInformations(nuxeoController, bundle, comment);
        header.add(informations);

        // Actions
        Element actions = this.generateActions(nuxeoController, bundle, comment);
        header.add(actions);

        return header;
    }


    /**
     * Generate comment informations DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateInformations(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) throws JspException {
        // Informations
        Element container = DOM4JUtils.generateDivElement("pull-left");

        // paragraph
        Element paragraph = DOM4JUtils.generateElement(HTMLConstants.P, "small", null);
        container.add(paragraph);

        // Author
        Element author = this.generateAuthor(nuxeoController, comment.getAuthor());
        paragraph.add(author);

        // Separator
        Element separator = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, "&ndash;");
        paragraph.add(separator);

        // Date
        Element date = this.generateDate(bundle.getLocale(), comment.getCreationDate());
        paragraph.add(date);

        return container;
    }


    /**
     * Generate comment author DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param author comment author name
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateAuthor(NuxeoController nuxeoController, String author) throws JspException {
        // Container
        Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

        try {
            // Avatar link
            Link avatarLink = nuxeoController.getUserAvatar(author);
            // User name & link
            DirectoryPerson person = this.getTagService().getDirectoryPerson(nuxeoController, author);
            String displayName;
            if ((person != null) && StringUtils.isNotBlank(person.getDisplayName())) {
                displayName = person.getDisplayName();
            } else {
                displayName = author;
            }
            Link profileLink = null;
            if (person != null) {
                profileLink = this.getTagService().getUserProfileLink(nuxeoController, author, displayName);
            }


            // Avatar
            if (avatarLink != null) {
                Element avatar = DOM4JUtils.generateElement(HTMLConstants.IMG, "avatar", null);
                DOM4JUtils.addAttribute(avatar, HTMLConstants.SRC, avatarLink.getUrl());
                DOM4JUtils.addAttribute(avatar, HTMLConstants.ALT, StringUtils.EMPTY);
                container.add(avatar);
            }

            // Name
            Element name;
            if (profileLink != null) {
                name = DOM4JUtils.generateLinkElement(profileLink.getUrl(), null, null, null, displayName);
            } else {
                name = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, displayName);
            }
            container.add(name);
        } catch (CMSException e) {
            throw new JspException(e);
        }

        return container;
    }


    /**
     * Generate comment creation date DOM4J element.
     *
     * @param locale user locale
     * @param date comment creation date
     * @return DOM4J element
     */
    private Element generateDate(Locale locale, Date date) {
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT, locale);
        return DOM4JUtils.generateElement(HTMLConstants.SPAN, null, dateFormat.format(date));
    }


    /**
     * Generate comment actions DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     */
    private Element generateActions(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) {
        // Namespace
        String namespace = nuxeoController.getResponse().getNamespace();

        // Container
        Element container = DOM4JUtils.generateDivElement("pull-right");

        // Button group
        Element group = DOM4JUtils.generateDivElement("btn-group btn-group-sm");
        container.add(group);

        // Reply
        StringBuilder replyUrlBuilder = new StringBuilder();
        replyUrlBuilder.append("#");
        replyUrlBuilder.append(namespace);
        replyUrlBuilder.append("-reply-comment-");
        replyUrlBuilder.append(comment.getId());
        Element reply = DOM4JUtils.generateLinkElement(replyUrlBuilder.toString(), null, null, "btn btn-default no-ajax-link", bundle.getString("REPLY"),
                "glyphicons glyphicons-chat");
        DOM4JUtils.addDataAttribute(reply, "toggle", "collapse");
        group.add(reply);

        // Delete
        if (comment.isDeletable()) {
            StringBuilder deleteUrlBuilder = new StringBuilder();
            deleteUrlBuilder.append("#");
            deleteUrlBuilder.append(namespace);
            deleteUrlBuilder.append("-delete-comment");
            Element delete = DOM4JUtils.generateLinkElement(deleteUrlBuilder.toString(), null, null, "btn btn-default fancybox_inline no-ajax-link",
                    bundle.getString("DELETE"), "halflings halflings-trash");
            DOM4JUtils.addDataAttribute(delete, "input-name", "id");
            DOM4JUtils.addDataAttribute(delete, "input-value", comment.getId());
            group.add(delete);
        }

        return container;
    }


    /**
     * Generate comment children DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     * @throws JspException
     */
    private Element generateChildren(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) throws JspException {
        // Container
        Element container = DOM4JUtils.generateDivElement("children");

        // UL
        Element ul = DOM4JUtils.generateElement(HTMLConstants.UL, null, null);
        container.add(ul);

        for (CommentDTO child : comment.getChildren()) {
            // LI
            Element li = DOM4JUtils.generateElement(HTMLConstants.LI, null, null);
            ul.add(li);

            // Horizontal row
            Element hr = DOM4JUtils.generateElement(HTMLConstants.HR, null, null);
            li.add(hr);

            Element element = this.generateComment(nuxeoController, bundle, child);
            li.add(element);
        }

        // Reply form
        Element replyForm = this.generateReplyForm(nuxeoController, bundle, comment);
        ul.add(replyForm);

        return container;
    }


    /**
     * Generate comment reply form DOM4J element.
     *
     * @param nuxeoController Nuxeo controller
     * @param bundle internationalization bundle
     * @param comment comment DTO
     * @return DOM4J element
     */
    private Element generateReplyForm(NuxeoController nuxeoController, Bundle bundle, CommentDTO comment) {
        // Portlet response
        MimeResponse response = (MimeResponse) nuxeoController.getResponse();
        // Namespace
        String namespace = response.getNamespace();

        // Container
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(namespace);
        idBuilder.append("-reply-comment-");
        idBuilder.append(comment.getId());
        Element container = DOM4JUtils.generateElement(HTMLConstants.LI, "collapse", null);
        DOM4JUtils.addAttribute(container, HTMLConstants.ID, idBuilder.toString());

        // Horizontal row
        Element hr = DOM4JUtils.generateElement(HTMLConstants.HR, null, null);
        container.add(hr);

        // Form
        PortletURL actionUrl = response.createActionURL();
        actionUrl.setParameter(ActionRequest.ACTION_NAME, "replyComment");
        actionUrl.setParameter("id", comment.getId());
        Element form = DOM4JUtils.generateElement(HTMLConstants.FORM, "no-ajax-link", null, null, AccessibilityRoles.FORM);
        DOM4JUtils.addAttribute(form, HTMLConstants.ACTION, actionUrl.toString());
        DOM4JUtils.addAttribute(form, HTMLConstants.METHOD, HTMLConstants.FORM_METHOD_POST);
        container.add(form);

        // Form group #1
        Element group1 = DOM4JUtils.generateDivElement("form-group");
        form.add(group1);

        // Label
        String labelTarget = namespace + "-comment-content";
        Element label = DOM4JUtils.generateElement(HTMLConstants.LABEL, null, bundle.getString("COMMENT_CONTENT"));
        DOM4JUtils.addAttribute(label, HTMLConstants.FOR, labelTarget);
        group1.add(label);

        // Textarea
        Element textarea = DOM4JUtils.generateElement(HTMLConstants.TEXTAREA, "form-control", StringUtils.EMPTY);
        DOM4JUtils.addAttribute(textarea, HTMLConstants.ID, labelTarget);
        DOM4JUtils.addAttribute(textarea, HTMLConstants.NAME, "content");
        group1.add(textarea);

        // Form group #2
        Element group2 = DOM4JUtils.generateDivElement("form-group");
        form.add(group2);

        // Submit button
        Element submitButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-primary", bundle.getString("SAVE"));
        DOM4JUtils.addAttribute(submitButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_SUBMIT);
        group2.add(submitButton);

        Element cancelButton = DOM4JUtils.generateElement(HTMLConstants.BUTTON, "btn btn-default", bundle.getString("CANCEL"));
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.TYPE, HTMLConstants.INPUT_TYPE_BUTTON);
        DOM4JUtils.addAttribute(cancelButton, HTMLConstants.ONCLICK, "$JQry(this).closest('.collapse').collapse('hide')");
        group2.add(cancelButton);

        return container;
    }

}
