package fr.toutatice.portail.cms.test.common.service;

import javax.portlet.PortletException;

import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;

import fr.toutatice.portail.cms.test.common.model.Configuration;
import fr.toutatice.portail.cms.test.common.model.Tab;


/**
 * Test repository implementation.
 *
 * @author Cédric Krommenhoek
 * @see ITestRepository
 */
public class TestRepositoryImpl implements ITestRepository {

    /** Default tab window property name. */
    private static final String DEFAULT_TAB_WINDOW_PROPERTY = "toutatice.test.defaultTab";
    /** Document path window property name. */
    private static final String PATH_WINDOW_PROPERTY = "toutatice.test.path";
    /** User name window property name. */
    private static final String USER_WINDOW_PROPERTY = "toutatice.test.user";
    /** Selection identifier window property name. */
    private static final String SELECTION_ID_WINDOW_PROPERTY = "toutatice.test.selectionId";


    /** Singleton instance. */
    private static ITestRepository instance;


    /**
     * Constructor.
     */
    protected TestRepositoryImpl() {
        super();
    }


    /**
     * Get singleton instance.
     *
     * @return singleton instance
     */
    public static ITestRepository getInstance() {
        if (instance == null) {
            instance = new TestRepositoryImpl();
        }
        return instance;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration(PortalControllerContext portalControllerContext) throws PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(portalControllerContext.getRequest());

        // Configuration
        Configuration configuration = new Configuration();
        configuration.setDefaultTab(Tab.fromId(window.getProperty(DEFAULT_TAB_WINDOW_PROPERTY)));
        configuration.setPath(window.getProperty(PATH_WINDOW_PROPERTY));
        configuration.setUser(window.getProperty(USER_WINDOW_PROPERTY));
        configuration.setSelectionId(window.getProperty(SELECTION_ID_WINDOW_PROPERTY));

        return configuration;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(PortalControllerContext portalControllerContext, Configuration configuration) throws PortletException {
        // Current window
        PortalWindow window = WindowFactory.getWindow(portalControllerContext.getRequest());
        window.setProperty(DEFAULT_TAB_WINDOW_PROPERTY, configuration.getDefaultTab().getId());
        window.setProperty(PATH_WINDOW_PROPERTY, StringUtils.trimToNull(configuration.getPath()));
        window.setProperty(USER_WINDOW_PROPERTY, StringUtils.trimToNull(configuration.getUser()));
        window.setProperty(SELECTION_ID_WINDOW_PROPERTY, StringUtils.trimToNull(configuration.getSelectionId()));
    }

}
