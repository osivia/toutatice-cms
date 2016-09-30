package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.taglib.common.ToutaticeSimpleTag;

/**
 * User tag.
 *
 * @author Cédric Krommenhoek
 * @see ToutaticeSimpleTag
 */
public class UserTag extends ToutaticeSimpleTag {

    /** User name. */
    private String name;
    /** Linkable indicator. */
    private boolean linkable;


    /**
     * Constructor.
     */
    public UserTag() {
        super();
        this.linkable = true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        if (StringUtils.isNotBlank(this.name)) {
            // Container
            Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

            // Avatar
            Element avatar = null;
            try {
                Link avatarLink = nuxeoController.getUserAvatar(this.name);
                if (avatarLink != null) {
                    avatar = DOM4JUtils.generateElement(HTMLConstants.IMG, "avatar", null);
                    DOM4JUtils.addAttribute(avatar, HTMLConstants.SRC, avatarLink.getUrl());
                    DOM4JUtils.addAttribute(avatar, HTMLConstants.ALT, StringUtils.EMPTY);
                }
            } catch (CMSException e) {
                // Do nothing
            }
            if (avatar == null) {
                avatar = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null, "glyphicons glyphicons-user", null);
            }
            container.add(avatar);

            // Directory person
            PersonService personService = DirServiceFactory.getService(PersonService.class);
            Person person = personService.getPerson(name);

            // Display name
            String displayName;
            if ((person != null) && StringUtils.isNotBlank(person.getDisplayName())) {
                displayName = person.getDisplayName();
            } else {
                displayName = this.name;
            }

            // URL
            String url = null;
            if (this.linkable && (person != null)) {
                Link userLink = this.getTagService().getUserProfileLink(nuxeoController, this.name, displayName);
                if (userLink != null) {
                    url = userLink.getUrl();
                }
            }

            // Content
            Element content;
            if (StringUtils.isEmpty(url)) {
                content = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, displayName);
            } else {
                content = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", displayName);
            }
            container.add(content);

            // HTML writer
            HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
            htmlWriter.setEscapeText(false);
            htmlWriter.write(container);
        }
    }


    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter for linkable.
     *
     * @param linkable the linkable to set
     */
    public void setLinkable(boolean linkable) {
        this.linkable = linkable;
    }

}
