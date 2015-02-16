package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.IDirectoryService;
import org.osivia.portal.api.directory.IDirectoryServiceLocator;
import org.osivia.portal.api.directory.entity.DirectoryPerson;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSException;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * User tag.
 *
 * @author Cédric Krommenhoek
 * @see SimpleTagSupport
 */
public class UserTag extends SimpleTagSupport {

    /** Portal URL factory. */
    private static final IPortalUrlFactory PORTAL_URL_FACTORY = Locator.findMBean(IPortalUrlFactory.class, "osivia:service=UrlFactory");
    /** Directory service locator. */
    private static final IDirectoryServiceLocator DIRECTORY_SERVICE_LOCATOR = Locator.findMBean(IDirectoryServiceLocator.class,
            IDirectoryServiceLocator.MBEAN_NAME);


    /** User name. */
    private String name;
    /** Linkable indicator. */
    private Boolean linkable;


    /**
     * Default constructor.
     */
    public UserTag() {
        super();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doTag() throws JspException, IOException {
        // Context
        PageContext pageContext = (PageContext) this.getJspContext();
        // Request
        ServletRequest request = pageContext.getRequest();
        // Nuxeo controller
        NuxeoController nuxeoController = (NuxeoController) request.getAttribute("nuxeoController");
        // Directory service
        IDirectoryService directoryService = DIRECTORY_SERVICE_LOCATOR.getDirectoryService();

        JspWriter out = pageContext.getOut();

        if ((nuxeoController != null) && (directoryService != null)) {
            // User LDAP person
            DirectoryPerson person = directoryService.getPerson(this.name);

            // User avatar image source
            String avatarSource;
            try {
                Link link = nuxeoController.getUserAvatar(this.name);
                avatarSource = link.getUrl();
            } catch (CMSException e) {
                avatarSource = StringUtils.EMPTY;
            }

            // User display name
            String displayName;
            if ((person != null) && (StringUtils.isNotEmpty(person.getDisplayName()))) {
                displayName = person.getDisplayName();
            } else {
                displayName = this.name;
            }

            // User display container
            Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

            // Avatar
            Element avatar = DOM4JUtils.generateElement(HTMLConstants.IMG, "avatar", null);
            DOM4JUtils.addAttribute(avatar, HTMLConstants.SRC, avatarSource);
            DOM4JUtils.addAttribute(avatar, HTMLConstants.ALT, StringUtils.EMPTY);
            container.add(avatar);

            // Text
            Element text;
            if (BooleanUtils.isFalse(this.linkable)) {
                // Span text
                text = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, displayName);
            } else {
                // User profile page URL
                String profileURL = this.getUserProfilePageURL(nuxeoController, this.name, displayName);

                // Linkable text
                text = DOM4JUtils.generateLinkElement(profileURL, null, null, null, displayName);
            }
            container.add(text);


            HTMLWriter htmlWriter = new HTMLWriter(out);
            htmlWriter.write(container);
        } else {
            out.write(this.name);
        }
    }


    /**
     * Get user profile page URL.
     *
     * @param nuxeoController Nuxeo controller
     * @param name user name
     * @param displayName user display name
     * @return user profile page URL
     */
    private String getUserProfilePageURL(NuxeoController nuxeoController, String name, String displayName) {
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

        // Page properties
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("osivia.hideTitle", "1");
        properties.put("osivia.ajaxLink", "1");
        properties.put("theme.dyna.partial_refresh_enabled", "true");
        properties.put("uidFichePersonne", name);

        // Page parameters
        Map<String, String> parameters = new HashMap<String, String>(0);


        String url;
        try {
            url = PORTAL_URL_FACTORY.getStartPortletInNewPage(portalControllerContext, "myprofile", displayName,
                    "toutatice-identite-fichepersonne-portailPortletInstance", properties, parameters);
        } catch (PortalException e) {
            url = StringUtils.EMPTY;
        }

        return url;
    }


    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
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
     * Getter for linkable.
     *
     * @return the linkable
     */
    public Boolean getLinkable() {
        return this.linkable;
    }

    /**
     * Setter for linkable.
     *
     * @param linkable the linkable to set
     */
    public void setLinkable(Boolean linkable) {
        this.linkable = linkable;
    }

}
