package fr.toutatice.portail.cms.test.common;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.context.PortalControllerContext;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.test.common.model.Configuration;
import fr.toutatice.portail.cms.test.common.model.Tab;
import fr.toutatice.portail.cms.test.common.service.ITestRepository;
import fr.toutatice.portail.cms.test.common.service.ITestService;
import fr.toutatice.portail.cms.test.common.service.TestRepositoryImpl;
import fr.toutatice.portail.cms.test.common.service.TestServiceImpl;

/**
 * Test portlet.
 *
 * @author Cédric Krommenhoek
 * @see CMSPortlet
 */
public class TestPortlet extends CMSPortlet {

    /** Path prefix. */
    private static final String PATH_PREFIX = "/WEB-INF/jsp/view-";
    /** Path suffix. */
    private static final String PATH_SUFFIX = ".jsp";


    /** Test service. */
    private final ITestService service;
    /** Test repository. */
    private final ITestRepository repository;



    /**
     * Constructor.
     */
    public TestPortlet() {
        super();
        this.service = TestServiceImpl.getInstance();
        this.repository = TestRepositoryImpl.getInstance();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);

        // Configuration
        Configuration configuration = this.repository.getConfiguration(portalControllerContext);
        request.setAttribute("configuration", configuration);


        // Tabs
        Tab[] tabs = Tab.values();
        request.setAttribute("tabs", tabs);

        // Current tab
        Tab currentTab;
        String currentTabId = request.getParameter("currentTabId");
        if (currentTabId != null) {
            currentTab = Tab.fromId(currentTabId);
        } else {
            currentTab = configuration.getDefaultTab();
        }
        request.setAttribute("currentTabId", currentTab.getId());
        PortletRequestDispatcher dispatcher = this.getPortletContext().getRequestDispatcher(PATH_PREFIX + currentTab.getId() + PATH_SUFFIX);


        // Tags
        if (Tab.TAGS.equals(currentTab)) {
            this.service.injectTagsData(portalControllerContext, configuration);
        }


        // Attributes storage
        if (Tab.ATTRIBUTES_STORAGE.equals(currentTab)) {
            this.service.injectAttributesStorageData(portalControllerContext, configuration);
        }


        // Response
        response.setContentType("text/html");
        dispatcher.include(request, response);
    }


    /**
     * Admin view display.
     *
     * @param request request
     * @param response response
     * @throws PortletException
     * @throws IOException
     */
    @RenderMode(name = "admin")
    public void doAdmin(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);

        // Configuration
        Configuration configuration = this.repository.getConfiguration(portalControllerContext);
        request.setAttribute("configuration", configuration);

        // Tabs
        Tab[] tabs = Tab.values();
        request.setAttribute("tabs", tabs);

        response.setContentType("text/html");
        this.getPortletContext().getRequestDispatcher("/WEB-INF/jsp/admin.jsp").include(request, response);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Action name
        String action = request.getParameter(ActionRequest.ACTION_NAME);

        if ("admin".equals(request.getPortletMode().toString())) {
            // Admin

            if ("save".equals(action)) {
                // Save configuration
                Configuration configuration = new Configuration();
                configuration.setDefaultTab(Tab.fromId(request.getParameter("defaultTab")));
                configuration.setPath(request.getParameter("path"));
                configuration.setUser(request.getParameter("user"));
                configuration.setSelectionId(request.getParameter("selectionId"));

                this.repository.setConfiguration(portalControllerContext, configuration);
            }

            response.setPortletMode(PortletMode.VIEW);
            response.setWindowState(WindowState.NORMAL);

        } else if (PortletMode.VIEW.equals(request.getPortletMode())) {
            // View

            // Comment action
            this.processCommentAction(request, response);

            if ("addToSelection".equals(action)) {
                // Selection content
                String content = request.getParameter("content");

                this.service.addToSelection(portalControllerContext, content);

            } else if ("editStorage".equals(action)) {
                if (request.getParameter("add") != null) {
                    String name = request.getParameter("attributeName");
                    String value = request.getParameter("attributeValue");

                    this.service.addToStorage(portalControllerContext, name, value);
                } else if (request.getParameter("remove") != null) {
                    String name = request.getParameter("remove");

                    this.service.removeFromStorage(portalControllerContext, name);
                }
            }

            // Current tab
            response.setRenderParameter("tab", StringUtils.trimToEmpty(request.getParameter("tab")));
        }
    }

}
