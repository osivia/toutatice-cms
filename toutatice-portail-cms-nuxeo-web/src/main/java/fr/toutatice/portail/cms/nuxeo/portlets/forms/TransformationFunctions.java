package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.Name;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Group;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.GroupService;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.html.DOM4JUtils;
import org.osivia.portal.api.html.HTMLConstants;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.tasks.ITasksService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.constants.InternalConstants;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.discussions.DiscussionHelper;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoCustomizer;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;

/**
 * Transformation functions.
 * 
 * @author Cédric Krommenhoek
 */
public class TransformationFunctions {
	
	private static Log log = LogFactory.getLog(TransformationFunctions.class);

    /** Portal URL factory. */
    private static IPortalUrlFactory portalUrlFactory;
    /** CMS service locator. */
    private static ICMSServiceLocator cmsServiceLocator;
    /** Tasks service. */
    private static ITasksService tasksService;
    /** cms customizer */
	private static INuxeoCustomizer cmsCustomizer;
	
	private static DocumentDAO documentDao;
    
	/** The internationalization service. */
	private static  IInternationalizationService internationalizationService;

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

    private static DocumentDAO getDocumentDAO() {
    	if(documentDao == null) {
    		documentDao = DocumentDAO.getInstance();
    	}
    	return documentDao;
    }
    

    private static INuxeoCustomizer getNuxeoCustomizer() {
    	if(cmsCustomizer == null) {
    		cmsCustomizer = NuxeoServiceFactory.getNuxeoService().getCMSCustomizer();
    	}
    	return cmsCustomizer;
    }

    
    private static IInternationalizationService getInternationalizationService() {

        if( internationalizationService == null) {
            internationalizationService = Locator.findMBean(IInternationalizationService.class,
                IInternationalizationService.MBEAN_NAME);
        }
        return internationalizationService;

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
            String displayName = StringUtils.defaultIfEmpty(person.getDisplayName(), user);

            // Page properties
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(InternalConstants.PROP_WINDOW_TITLE, displayName);
            properties.put("osivia.hideTitle", "1");
            properties.put("osivia.ajaxLink", "1");
            properties.put(DynaRenderOptions.PARTIAL_REFRESH_ENABLED, String.valueOf(true));
            properties.put("uidFichePersonne", user);

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

        return DOM4JUtils.writeCompact(element);
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
     * Get group emails.
     * 
     * @param id group identifier
     * @return emails
     */
    public static String getGroupEmails(String id) {
        // LDAP group service
        GroupService groupService = DirServiceFactory.getService(GroupService.class);

        // Group
        Group group = groupService.get(id);

        // Emails
        String emails;

        if (group == null) {
            emails = null;
        } else {
            // Group DN
            Name dn = group.getDn();

            // Group members
            List<Person> members = groupService.getMembers(dn);

            if (CollectionUtils.isEmpty(members)) {
                emails = null;
            } else {
                // Email list
                Set<String> list = new HashSet<String>(members.size());
                for (Person member : members) {
                    String email = member.getMail();
                    if (StringUtils.isNotBlank(email)) {
                        list.add(email);
                    }
                }

                emails = StringUtils.join(list, ",");
            }
        }

        return emails;
    }


    /**
     * Get group:emails method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getGroupEmailsMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getGroupEmails", String.class);
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

        // Document
        Document document = getDocument(portalControllerContext, path);

        // Document title
        String title;
        if (document == null) {
            title = null;
        } else {
            title = StringUtils.defaultIfEmpty(StringUtils.trim(document.getTitle()), document.getId());
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
     * 
     * @param path document path
     * @return link
     */
    public static String getDocumentLink(String path) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // Document
        Document document = getDocument(portalControllerContext, path);

        // Result
        String result;
        if (document == null) {
            result = null;
        } else {
            // URL
            String url;
            try {
                url = portalUrlFactory.getPermaLink(portalControllerContext, null, null, document.getPath(), IPortalUrlFactory.PERM_LINK_TYPE_CMS);
            } catch (PortalException e) {
                url = "#";
            }
            // Title
            String title = StringUtils.defaultIfEmpty(StringUtils.trim(document.getTitle()), document.getId());
            
            // Target
            DocumentDTO dto = getDocumentDAO().toDTO(document);
            String target = getNuxeoCustomizer().getTarget(dto);
            
            // Link
            Element link = DOM4JUtils.generateLinkElement(url, target, null, "no-ajax-link", title);

            result = DOM4JUtils.writeCompact(link);
        }

        return result;
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
     * Get document.
     * 
     * @param portalControllerContext portal controller context
     * @param path document path
     * @return document
     */
    private static Document getDocument(PortalControllerContext portalControllerContext, String path) {
        // CMS service
//        ICMSService cmsService = getCmsService();
        // CMS context
        CMSServiceCtx cmsContext = new CMSServiceCtx();
        cmsContext.setPortalControllerContext(portalControllerContext);
        cmsContext.setForcePublicationInfosScope("superuser_context");

        INuxeoCommand command = new GetDocumentCommand(path);
        
        // Nuxeo document
        Document document = null;
        try {
            
        	Documents documents = (Documents) getNuxeoCustomizer().executeNuxeoCommand(cmsContext, command);
        	if(documents.size() == 1) {
        		document =  documents.get(0);
        	}

        } catch (CMSException e) {
        	log.error(e);
        }

        return document;
    }


    /**
     * Get update task command link.
     * 
     * @param title link title
     * @param actionId action identifier
     * @return link
     */
    public static String getCommandLink(String title, String actionId) {
        // Tasks service
        ITasksService tasksService = getTasksService();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();
        // Disabled links indicator
        boolean disabledLinks = FormsServiceImpl.areLinksDisabled();

        // Result
        String result;

        if (disabledLinks) {
            result = StringUtils.EMPTY;
        } else {
            // UUID
            UUID uuid = FormsServiceImpl.getUuid();

            // URL
            String url;
            try {
                url = tasksService.getCommandUrl(portalControllerContext, uuid, actionId);
            } catch (PortalException e) {
                url = "#";
            }

            // Link
            Element link = DOM4JUtils.generateLinkElement(url, null, null, null, title);

            result = DOM4JUtils.writeCompact(link);
        }

        return result;
    }


    /**
     * Get command:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getCommandLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getCommandLink", String.class, String.class);
    }
    
    
    
    
    
    /**
     * Get update task command link.
     * 
     * @param title link title
     * @param actionId action identifier
     * @return link
     */
    public static String getViewTaskLink(String title) {
        // Tasks service
        ITasksService tasksService = getTasksService();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();
        // Disabled links indicator
        boolean disabledLinks = FormsServiceImpl.areLinksDisabled();

        // Result
        String result;

        if (disabledLinks) {
            result = StringUtils.EMPTY;
        } else {
            // UUID
            UUID uuid = FormsServiceImpl.getUuid();

            // URL
            String url;
            try {
                url = tasksService.getViewTaskUrl(portalControllerContext, uuid);
            } catch (PortalException e) {
                url = "#";
            }

            // Link
            Element link = DOM4JUtils.generateLinkElement(url, null, null, null, title);

            result = DOM4JUtils.writeCompact(link);
        }

        return result;
    }


    /**
     * Get command:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getViewTaskLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getViewTaskLink", String.class);
    }
    
    
    
    
    

    /**
     * Get document link.
     * 
     * @param path document path
     * @param text link text
     * @return link
     */
    public static String getDocumentLinkWithText(String path, String text) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // URL
        String url;
        try {
            url = portalUrlFactory.getPermaLink(portalControllerContext, null, null, path, IPortalUrlFactory.PERM_LINK_TYPE_CMS);
        } catch (PortalException e) {
            url = "#";
        }

        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", text);

        return DOM4JUtils.writeCompact(link);
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


    /**
     * Get portal home page link.
     * 
     * @param text link text
     * @return link
     */
    public static String getPortalLink(String text) {
        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = getPortalUrlFactory();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

        // URL
        String url = null;
        try {
            url = portalUrlFactory.getHomePageUrl(portalControllerContext, false);
        } catch (PortalException e) {
        }

        // Link
        Element link = DOM4JUtils.generateLinkElement(url, null, null, "no-ajax-link", text);

        return DOM4JUtils.writeCompact(link);
    }


    /**
     * Get portal:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getPortalLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getPortalLink", String.class);
    }
    
    
    
    /**
     * Get portal translation.
     * 
     * @param text link text
     * @return link
     */
    public static String getPortalTranslate(String key) {
        // Portal URL factory
        IInternationalizationService internationalizationService = getInternationalizationService();

        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();

   
          IBundleFactory bundleFactory = internationalizationService.getBundleFactory(TransformationFunctions.class.getClassLoader());
        Bundle bundle = bundleFactory.getBundle(portalControllerContext.getHttpServletRequest().getLocale());
        
        return bundle.getString(key);
    }
    

    /**
     * Get portal:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getPortalTranslateMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getPortalTranslate", String.class);
    }

    
    
    /**
     * Get portal property.
     * 
     * @param text propertyName
     * @return property value
     */
    public static String getPortalProperty(String propertyName) {

        String result = System.getProperty(propertyName);
        if( result == null)
            result = "";
        return result;
    }
    

    /**
     * Get portal:link method.
     * 
     * @return method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public static Method getPortalPropertyMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getPortalProperty", String.class);
    }

    
     

    /**
     * Gets the view message link.
     *
     * @param discussionId the discussion id
     * @param messageId the message id
     * @return the view message link
     */
    public static String getViewMessageLink(String discussionId, String messageId) {
      
        // Portal controller context
        PortalControllerContext portalControllerContext = FormsServiceImpl.getPortalControllerContext();
        
        return DiscussionHelper.getDiscussionAdminUrl(portalControllerContext, discussionId, messageId);
    }
    

    /**
     * Gets the view message link method.
     *
     * @return the view message link method
     * @throws NoSuchMethodException the no such method exception
     * @throws SecurityException the security exception
     */
    public static Method getViewMessageLinkMethod() throws NoSuchMethodException, SecurityException {
        return TransformationFunctions.class.getMethod("getViewMessageLink", String.class, String.class);
    }
    
    
    
}
