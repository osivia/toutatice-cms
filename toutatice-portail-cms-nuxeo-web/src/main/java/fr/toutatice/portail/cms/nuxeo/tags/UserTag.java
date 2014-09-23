package fr.toutatice.portail.cms.nuxeo.tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
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

        if (nuxeoController != null) {
            // User LDAP person
            DirectoryPerson person = DIRECTORY_SERVICE_LOCATOR.getDirectoryService().getPerson(this.name);

            // User avatar image source
            String avatarSource;
            try {
                Link link = nuxeoController.getUserAvatar(this.name);
                avatarSource = link.getUrl();
            } catch (CMSException e) {
                avatarSource = StringUtils.EMPTY;
            }

            // User display name
            String displayName = person.getDisplayName();

            // User profile page URL
            String profileURL = this.getUserProfilePageURL(nuxeoController, person);



            // User display container
            Element container = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, null);

            // Avatar
            Element avatar = DOM4JUtils.generateElement(HTMLConstants.IMG, "avatar", null);
            DOM4JUtils.addAttribute(avatar, HTMLConstants.SRC, avatarSource);
            DOM4JUtils.addAttribute(avatar, HTMLConstants.ALT, StringUtils.EMPTY);
            container.add(avatar);

            // Link
            Element link = DOM4JUtils.generateLinkElement(profileURL, null, null, null, displayName);
            container.add(link);


            JspWriter out = pageContext.getOut();
            HTMLWriter htmlWriter = new HTMLWriter(out);
            // try {
                htmlWriter.write(container);
            // } finally {
            // htmlWriter.close();
            // }
        }
    }


    /**
     * Get user profile page URL.
     *
     * @param nuxeoController Nuxeo controller
     * @param person user LDAP person
     * @return user profile page URL
     */
    private String getUserProfilePageURL(NuxeoController nuxeoController, DirectoryPerson person) {
        // Portal controller context
        PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();

        // Page properties
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("osivia.ajaxLink", "1");
        properties.put("theme.dyna.partial_refresh_enabled", "true");

        // Page parameters
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("uidFichePersonne", person.getUid());

        String url;
        try {
            url = PORTAL_URL_FACTORY.getStartPageUrl(portalControllerContext, "userprofile", "/default/templates/userprofile", properties, parameters);
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

}
