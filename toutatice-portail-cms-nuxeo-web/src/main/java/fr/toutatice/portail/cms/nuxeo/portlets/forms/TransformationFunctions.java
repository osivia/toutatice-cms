package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.tasks.ITasksService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

/**
 * Transformation functions.
 * 
 * @author Cédric Krommenhoek
 */
public class TransformationFunctions {

    /** Portal URL factory. */
    private static IPortalUrlFactory portalUrlFactory;
    /** CMS service locator. */
    private static ICMSServiceLocator cmsServiceLocator;
    /** Tasks service. */
    private static ITasksService tasksService;


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
     * Get CMS service.
     * 
     * @return CMS service
     */
    private static ICMSService getCmsService() {
        if (cmsServiceLocator == null) {
            cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        }
        return cmsServiceLocator.getCMSService();
    }


    /**
     * Get tasks service.
     * 
     * @return tasks service
     */
    private static ITasksService getTasksService() {
        if (tasksService == null) {
            tasksService = Locator.findMBean(ITasksService.class, ITasksService.MBEAN_NAME);
        }
        return tasksService;
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
     * Get user link.
     * 
     * @param user user identifier
     * @return link
     */
    public static String getUserLink(String user) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();
        // Person service
        PersonService personService = DirServiceFactory.getService(PersonService.class);

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // Person
        Person person = personService.getPerson(user);


        // DOM4J element
        Element element;

        if (person == null) {
            element = DOM4JUtils.generateElement(HTMLConstants.SPAN, null, user);
        } else {
            // Display name
            String displayName = StringUtils.defaultIfBlank(person.getDisplayName(), user);

            // Page properties
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("osivia.hideTitle", "1");
            properties.put("osivia.ajaxLink", "1");
            properties.put("theme.dyna.partial_refresh_enabled", "true");
            properties.put("uidFichePersonne", displayName);

            // Page parameters
            Map<String, String> parameters = new HashMap<String, String>(0);

            // URL
            String url;
            try {
                url = portalUrlFactory.getStartPortletInNewPage(portalControllerContext, "myprofile", displayName, "directory-person-card-instance", properties,
                        parameters);
            } catch (PortalException e) {
                url = "#";
            }

            element = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", displayName);
        }

        return DOM4JUtils.write(element);
    }


    /**
     * Get user:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getUserLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getUserLink", String.class);
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
        // CMS service
        ICMSService cmsService = getCmsService();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);
        cmsContext.setForcePublicationInfosScope("superuser_context");


        // Document title
        String title;

        try {
            // Nuxeo document
            CMSItem cmsItem = cmsService.getContent(cmsContext, path);
            Document document = (Document) cmsItem.getNativeItem();

            title = document.getTitle();
        } catch (CMSException e) {
            title = null;
        }

        return title;
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

        // Document title
        String title = getDocumentTitle(path);

        // URL
        String url = portalUrlFactory.getCMSUrl(portalControllerContext, null, path, null, null, null, null, null, null, null);
        
        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", title);

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
     * Get update task command link.
     * 
     * @param title link title
     * @param actionId action identifier
     * @param redirectionPath redirection path
     * @return link
     */
    public static String getCommandLink(String title, String actionId, String redirectionPath) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();
        // Tasks service
        ITasksService tasksService = getTasksService();
        
        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // UUID
        UUID uuid = FormsServiceImpl.getUuid();

        // Redirection URL
        String redirectionUrl;
        if (StringUtils.isEmpty(redirectionPath)) {
            redirectionUrl = null;
        } else {
            redirectionUrl = portalUrlFactory.getCMSUrl(portalControllerContext, null, redirectionPath, null, null, null, null, null, null, null);
        }

        // URL
        String url;
        try {
            url = tasksService.getCommandUrl(portalControllerContext, uuid, actionId, redirectionUrl);
        } catch (PortalException e) {
            url = "#";
        }

        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, null, title);

        return DOM4JUtils.write(link);
    }


    /**
     * Get command:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getCommandLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getCommandLink", String.class, String.class, String.class);
    }

}
