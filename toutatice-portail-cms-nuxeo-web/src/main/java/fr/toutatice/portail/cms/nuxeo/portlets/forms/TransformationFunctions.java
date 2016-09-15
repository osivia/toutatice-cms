package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

/**
 * Transformation functions.
 * 
 * @author Cédric Krommenhoek
 */
public class TransformationFunctions {

    /** Singleton instance. */
    private static TransformationFunctions instance;


    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;


    /**
     * Constructor.
     */
    private TransformationFunctions() {
        super();

        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
    }


    /**
     * Get singleton instance.
     * 
     * @return instance
     */
    private static TransformationFunctions getInstance() {
        if (instance == null) {
            instance = new TransformationFunctions();
        }
        return instance;
    }


    /**
     * Get portal URL factory.
     * 
     * @return portal URL factory
     */
    private static IPortalUrlFactory getPortalUrlFactory() {
        TransformationFunctions instance = getInstance();
        return instance.portalUrlFactory;
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

        // DOM4J element
        Element element;

        if (person == null) {
            element = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, user);
        } else {
            // Display name
            String displayName = StringUtils.defaultIfBlank(person.getDisplayName(), user);

            element = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, displayName);
        }

        return DOM4JUtils.write(element);
    }


    /**
     * Get user:name method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getUserNameMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getUserDisplayName", String.class);
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
        String url = portalUrlFactory.getCMSUrl(portalControllerContext, null, path, null, null, null, null, null, null, null);
        
        
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

}
