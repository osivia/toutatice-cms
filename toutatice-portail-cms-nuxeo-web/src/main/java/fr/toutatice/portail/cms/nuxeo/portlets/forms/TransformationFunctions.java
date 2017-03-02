package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Transformation functions.
 *
 * @author Cédric Krommenhoek
 */
public class TransformationFunctions {

    /** Portal URL factory. */
    private static IPortalUrlFactory portalUrlFactory;


    /**
     * Constructor.
     */
    private TransformationFunctions() {
        super();
    }


    /**
     * Get portal URL factory.
     *
     * @return portal URL factory
     */
    private static IPortalUrlFactory getPortalUrlFactory() {
        if (portalUrlFactory == null) {
            portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        }
        return portalUrlFactory;
    }


    /**
     * Get user display name.
     *
     * @param user user identifier
     * @return display name
     */
    public static String getUserDisplayName(String user) {
        // Person service
        PersonService personService = DirServiceFactory.getService(PersonService.class);

        // Person
        Person person = personService.getPerson(user);

        // Display name
        String displayName;
        if (person == null) {
            displayName = user;
        } else {
            displayName = StringUtils.defaultIfBlank(person.getDisplayName(), user);
        }

        return displayName;
    }


    /**
     * Get user:name method.
     *
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getUserDisplayNameMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getUserDisplayName", String.class);
    }

    /**
     * Get user email.
     *
     * @param user user identifier
     * @return email
     */
    public static String getUserEmail(String user) {
        // Person service
        PersonService personService = DirServiceFactory.getService(PersonService.class);

        // Person
        Person person = personService.getPerson(user);

        // Email
        String email;

        if (person == null) {
            email = null;
        } else {
            email = person.getMail();
        }

        return email;
    }


    /**
     * Get user:email method.
     *
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getUserEmailMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getUserEmail", String.class);
    }


    /**
     * Get document title.
     *
     * @param path document path
     * @return title
     */
    public static String getDocumentTitle(String path) {
        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Nuxeo document
        Document document = nuxeoController.fetchDocument(path);

        return document.getTitle();
    }


    /**
     * Get document:title method.
     *
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getDocumentTitleMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getDocumentTitle", String.class);
    }


    /**
     * Get document link.
     * @param path document path
     * @return link
     */
    public static String getDocumentLink(String path) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);

        // Nuxeo document context
        Document document = nuxeoController.fetchDocument(path);


        // URL
        String url = null;
        try {
            url = portalUrlFactory.getPermaLink(portalControllerContext, null, null, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        } catch (PortalException e) {
        }


        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", document.getTitle());

        return DOM4JUtils.write(link);
    }


    /**
     * Get document:link method.
     *
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getDocumentLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getDocumentLink", String.class);
    }


    /**
     * Get document link.
     * @param path document path
     * @return link
     */
    public static String getDocumentLinkWithText(String path, String text) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        String url = null;
        try {
            url = portalUrlFactory.getPermaLink(portalControllerContext, null, null, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        } catch (PortalException e) {
        }


        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", text);

        return DOM4JUtils.write(link);
    }

    /**
     * Get document:link method.
     *
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getDocumentLinkWithTextMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getDocumentLinkWithText", String.class, String.class);
    }

}
