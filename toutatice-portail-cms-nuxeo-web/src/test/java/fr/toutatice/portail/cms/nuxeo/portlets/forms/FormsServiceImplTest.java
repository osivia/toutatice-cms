package fr.toutatice.portail.cms.nuxeo.portlets.forms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.omg.IOP.TransactionService;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.GroupService;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.tasks.ITasksService;
import org.osivia.portal.api.transaction.ITransactionService;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.api.urls.Link;
import org.osivia.portal.core.cms.CMSItem;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.extension.listener.AnnotationEnabler;
import org.powermock.core.classloader.annotations.PowerMockListener;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.domain.DocumentDTO;
import fr.toutatice.portail.cms.nuxeo.api.forms.IFormsService;
import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoServiceFactory;
import fr.toutatice.portail.cms.nuxeo.api.services.dao.DocumentDAO;
import fr.toutatice.portail.cms.nuxeo.api.services.tag.INuxeoTagService;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

/**
 * Forms service implementation test.
 * 
 * @author Cédric Krommenhoek
 */
@RunWith(PowerMockRunner.class)
@PowerMockListener(AnnotationEnabler.class)
@PrepareForTest({DocumentDAO.class, TransformationFunctions.class, Locator.class, DirServiceFactory.class, NuxeoServiceFactory.class})
public class FormsServiceImplTest {

    /** User identifier. */
    private static final String USER_ID = "gpoitoux";
    /** User display name. */
    private static final String USER_DISPLAY_NAME = "Gilles Poitoux";
    /** User avatar URL. */
    private static final String USER_AVATAR_URL = "http://host/portal/avatar";
    /** User profile URL. */
    private static final String USER_PROFILE_URL = "http://host/portal/profile";

    /** Document path. */
    private static final String DOCUMENT_PATH = "/default/article";
    /** Document title. */
    private static final String DOCUMENT_TITLE = "Article";
    /** Document URL. */
    private static final String DOCUMENT_URL = "http://host/portal/cms/article";

    /** Workspace identifier. */
    private static final String WORKSPACE_ID = "workspace-id";


    /** Forms service. */
    private IFormsService formsService;

    /** Portal controller context mock. */
    @Mock
    private PortalControllerContext portalControllerContext;


    /**
     * Constructor.
     */
    public FormsServiceImplTest() {
        super();
    }


    /**
     * Set-up.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        // Document
        Document document = EasyMock.createMock(Document.class);
        EasyMock.expect(document.getTitle()).andReturn(DOCUMENT_TITLE).anyTimes();
        EasyMock.expect(document.getPath()).andReturn(DOCUMENT_PATH).anyTimes();
        EasyMock.expect(document.getId()).andReturn(UUID.randomUUID().toString()).anyTimes();

        // Document DTO
        DocumentDTO dto = EasyMock.createMock(DocumentDTO.class);

        // Document DAO
        DocumentDAO dao = EasyMock.createMock(DocumentDAO.class);
        EasyMock.expect(dao.toDTO(document)).andStubReturn(dto);
        PowerMock.mockStatic(DocumentDAO.class);
        EasyMock.expect(DocumentDAO.getInstance()).andStubReturn(dao);

        // Documents
        Documents documents = EasyMock.createMock(Documents.class);
        EasyMock.expect(documents.size()).andReturn(1).anyTimes();
        EasyMock.expect(documents.get(0)).andStubReturn(document);

        // Document context
        NuxeoDocumentContext documentContext = EasyMock.createMock(NuxeoDocumentContext.class);
        EasyMock.expect(documentContext.getDocument()).andStubReturn(document);

        // Transformation functions
        PowerMock.mockStaticPartial(TransformationFunctions.class, "getDocument");
        PowerMock.expectPrivate(TransformationFunctions.class, "getDocument", EasyMock.anyObject(PortalControllerContext.class),
                EasyMock.anyObject(String.class)).andStubReturn(document);

        // Nuxeo controller
        NuxeoController nuxeoController = EasyMock.createMock(NuxeoController.class);
        EasyMock.expect(nuxeoController.getDocumentContext(DOCUMENT_PATH)).andStubReturn(documentContext);
        PowerMock.expectNew(NuxeoController.class, this.portalControllerContext).andStubReturn(nuxeoController);

        // CMS item
        CMSItem cmsItem = EasyMock.createMock(CMSItem.class);
        EasyMock.expect(cmsItem.getNativeItem()).andStubReturn(document);

        // CMS customizer
        DefaultCMSCustomizer cmsCustomizer = EasyMock.createMock(DefaultCMSCustomizer.class);
        EasyMock.expect(cmsCustomizer.executeNuxeoCommand(EasyMock.anyObject(CMSServiceCtx.class), EasyMock.anyObject(GetDocumentCommand.class)))
                .andStubReturn(documents);
        EasyMock.expect(cmsCustomizer.getTarget(dto)).andReturn(WORKSPACE_ID).anyTimes();

        // CMS context
        CMSServiceCtx cmsContext = EasyMock.createMock(CMSServiceCtx.class);
        cmsContext.setPortalControllerContext(portalControllerContext);
        EasyMock.expectLastCall();
        cmsContext.setForcePublicationInfosScope(EasyMock.anyObject(String.class));
        EasyMock.expectLastCall();
        PowerMock.expectNew(CMSServiceCtx.class).andStubReturn(cmsContext);

        // CMS service
        ICMSService cmsService = EasyMock.createMock(ICMSService.class);
        EasyMock.expect(cmsService.getContent(cmsContext, DOCUMENT_PATH)).andStubReturn(cmsItem);

        // Nuxeo service
        INuxeoService nuxeoService = EasyMock.createMock(INuxeoService.class);
        EasyMock.expect(nuxeoService.getCMSCustomizer()).andStubReturn(cmsCustomizer);

        // Empty person
        Person emptyPerson = EasyMock.createNiceMock(Person.class);

        // Person
        Person person = EasyMock.createMock(Person.class);
        EasyMock.expect(person.getAvatar()).andReturn(new Link(USER_AVATAR_URL, false)).anyTimes();
        EasyMock.expect(person.getDisplayName()).andReturn(USER_DISPLAY_NAME).anyTimes();

        // Person service
        PersonService personService = EasyMock.createMock(PersonService.class);
        EasyMock.expect(personService.getEmptyPerson()).andStubReturn(emptyPerson);
        EasyMock.expect(personService.findByCriteria(emptyPerson)).andStubReturn(Arrays.asList(new Person[]{person}));
        EasyMock.expect(personService.getPerson(USER_ID)).andStubReturn(person);

        // Group service
        GroupService groupService = EasyMock.createMock(GroupService.class);

        // Directory service factory
        PowerMock.mockStatic(DirServiceFactory.class);
        EasyMock.expect(DirServiceFactory.getService(PersonService.class)).andStubReturn(personService);
        EasyMock.expect(DirServiceFactory.getService(GroupService.class)).andStubReturn(groupService);

        // Tag service
        INuxeoTagService tagService = EasyMock.createMock(INuxeoTagService.class);
        EasyMock.expect(tagService.getUserProfileLink(nuxeoController, USER_ID, USER_DISPLAY_NAME)).andReturn(new Link(USER_PROFILE_URL, false)).anyTimes();

        // Nuxeo service factory
        PowerMock.mockStatic(NuxeoServiceFactory.class);
        EasyMock.expect(NuxeoServiceFactory.getNuxeoService()).andStubReturn(nuxeoService);
        EasyMock.expect(NuxeoServiceFactory.getTagService()).andStubReturn(tagService);

        // Portal URL factory
        IPortalUrlFactory portalUrlFactory = EasyMock.createMock(IPortalUrlFactory.class);
        EasyMock.expect(portalUrlFactory.getPermaLink(this.portalControllerContext, null, null, DOCUMENT_PATH, IPortalUrlFactory.PERM_LINK_TYPE_CMS))
                .andReturn(DOCUMENT_URL).anyTimes();
        EasyMock.expect(portalUrlFactory.getStartPortletInNewPage(EasyMock.anyObject(PortalControllerContext.class), EasyMock.anyObject(String.class),
                EasyMock.anyObject(String.class), EasyMock.anyObject(String.class), EasyMock.anyObject(Map.class), EasyMock.anyObject(Map.class)))
                .andReturn(USER_PROFILE_URL).anyTimes();

        // CMS service locator
        ICMSServiceLocator cmsServiceLocator = EasyMock.createMock(ICMSServiceLocator.class);
        EasyMock.expect(cmsServiceLocator.getCMSService()).andStubReturn(cmsService);

        // Tasks service
        ITasksService tasksService = EasyMock.createMock(ITasksService.class);

        // Bundle factory
        IBundleFactory bundleFactory = EasyMock.createMock(IBundleFactory.class);

        // Internationalization service
        IInternationalizationService internationalizationService = EasyMock.createMock(IInternationalizationService.class);
        EasyMock.expect(internationalizationService.getBundleFactory(EasyMock.anyObject(ClassLoader.class))).andStubReturn(bundleFactory);


        ITransactionService transactionService = EasyMock.createMock(ITransactionService.class);
        // Locator
        PowerMock.mockStatic(Locator.class);
        EasyMock.expect(Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME)).andStubReturn(nuxeoService);
        EasyMock.expect(Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME)).andStubReturn(portalUrlFactory);
        EasyMock.expect(Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME)).andStubReturn(cmsServiceLocator);
        EasyMock.expect(Locator.findMBean(ITasksService.class, ITasksService.MBEAN_NAME)).andStubReturn(tasksService);
        EasyMock.expect(Locator.findMBean(IInternationalizationService.class, IInternationalizationService.MBEAN_NAME))
                .andStubReturn(internationalizationService);
        EasyMock.expect(Locator.findMBean(ITransactionService.class, ITransactionService.MBEAN_NAME))
        .andStubReturn(transactionService);

        // Replay
        PowerMock.replayAll(document, dto, dao, documents, documentContext, nuxeoController, cmsItem, cmsCustomizer, cmsContext, cmsService, nuxeoService,
                emptyPerson, person, personService, groupService, tagService, portalUrlFactory, cmsServiceLocator, tasksService, bundleFactory,
                internationalizationService, transactionService);


        // Forms service
        this.formsService = new FormsServiceImpl(cmsCustomizer);
    }


    @Test
    public void testTransform() throws PortalException {
        String expression;
        Map<String, String> variables;
        String transformedExpression;

        // #1 : nominal test without variable or function
        expression = "Test #1.";
        variables = null;
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals(expression, transformedExpression);

        // #2 : null expression
        expression = null;
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals(StringUtils.EMPTY, transformedExpression);

        // #3 : empty expression
        expression = StringUtils.EMPTY;
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals(StringUtils.EMPTY, transformedExpression);

        // #4 : blank expression
        expression = "  ";
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals(StringUtils.EMPTY, transformedExpression);

        // #5 : variables
        expression = "value = ${foo}";
        variables = new HashMap<String, String>();
        variables.put("foo", "bar");
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals("value = bar", transformedExpression);

        // #6 : user:name function
        expression = "user = ${user:name(user)}";
        variables.put("user", USER_ID);
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals(transformedExpression, "user = " + USER_DISPLAY_NAME);

        // #7 : user:link function
        expression = "user = ${user:link(user)}";
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertTrue(
                StringUtils.contains(transformedExpression, "<a class=\"no-ajax-link\" href=\"" + USER_PROFILE_URL + "\">" + USER_DISPLAY_NAME + "</a>"));

        // #8 : document:name function
        variables.put("documentPath", DOCUMENT_PATH);

        // #9 : document:link function
        expression = "document = ${document:link(documentPath)}";
        transformedExpression = this.formsService.transform(portalControllerContext, expression, variables);
        Assert.assertEquals("document = <a class=\"no-ajax-link\" target=\"" + WORKSPACE_ID + "\" href=\"" + DOCUMENT_URL + "\">" + DOCUMENT_TITLE + "</a>",
                StringUtils.remove(transformedExpression, System.lineSeparator()));
    }

}
