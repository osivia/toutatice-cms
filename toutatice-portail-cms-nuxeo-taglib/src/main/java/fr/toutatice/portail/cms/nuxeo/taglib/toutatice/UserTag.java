package fr.toutatice.portail.cms.nuxeo.taglib.toutatice;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
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
    /** Linkable indicator Default = true. */
    private boolean linkable;
    /** Hide avatar indicator. Default = false. */
    private boolean hideAvatar;
    /** Hide display name indicator. Default = false. */
    private boolean hideDisplayName;


    /**
     * Constructor.
     */
    public UserTag() {
        super();
        this.linkable = true;
        this.hideAvatar = false;
        this.hideDisplayName = false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTag(NuxeoController nuxeoController, DocumentDTO document) throws JspException, IOException {
        if (StringUtils.isNotBlank(this.name)) {
            
 /*           
            if( true)   {
                

                // HTML writer
                HTMLWriter htmlWriter = new HTMLWriter(this.getJspContext().getOut());
                htmlWriter.setEscapeText(false);
                htmlWriter.write("user "+name);
                return;
            }
*/            
            // Container
            Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);


            // Display name
            String displayName;
            // URL
            String url;

            if (this.linkable || !this.hideDisplayName) {
                // Directory person
                PersonService personService = DirServiceFactory.getService(PersonService.class);
                Person person = personService.getPerson(name);

                if (person != null) {
                    displayName = StringUtils.defaultIfBlank(person.getDisplayName(), this.name);

                    if (this.linkable) {
                        Link link = this.getTagService().getUserProfileLink(nuxeoController, this.name, displayName);
                        if (link == null) {
                            url = null;
                        } else {
                            url = link.getUrl();
                        }
                    } else {
                        url = null;
                    }
                } else {
                    displayName = this.name;
                    url = null;
                }
            } else {
                displayName = null;
                url = null;
            }


            if (!this.hideAvatar) {
                // Avatar container
                Element avatarContainer;
                // Avatar HTML class
                String avatarHtmlClass;

                if (!this.hideDisplayName) {
                    avatarContainer = container;
                    avatarHtmlClass = "avatar";
                } else {
                    avatarContainer = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", null);
                    container.add(avatarContainer);
                    avatarHtmlClass = null;
                }

                // Avatar
                Element avatar;
                try {
                    Link link = nuxeoController.getUserAvatar(this.name);
                    if (link == null) {
                        avatar = null;
                    } else {
                        avatar = DOM4JUtils.generateElement(HTMLConstants.IMG, avatarHtmlClass, null);
                        DOM4JUtils.addAttribute(avatar, HTMLConstants.SRC, link.getUrl());
                        DOM4JUtils.addAttribute(avatar, HTMLConstants.ALT, StringUtils.EMPTY);
                    }
                } catch (CMSException e) {
                    avatar = null;
                }
                if (avatar == null) {
                    avatar = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null, "glyphicons glyphicons-user", null);
                }
                avatarContainer.add(avatar);
            }

            if (!this.hideDisplayName) {
                // Content
                Element content;
                if (url == null) {
                    content = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, displayName);
                } else {
                    content = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", displayName);
                }
                container.add(content);
            }

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

    /**
     * Setter for hideAvatar.
     *
     * @param hideAvatar the hideAvatar to set
     */
    public void setHideAvatar(boolean hideAvatar) {
        this.hideAvatar = hideAvatar;
    }

    /**
     * Setter for hideDisplayName.
     *
     * @param hideDisplayName the hideDisplayName to set
     */
    public void setHideDisplayName(boolean hideDisplayName) {
        this.hideDisplayName = hideDisplayName;
    }

}
