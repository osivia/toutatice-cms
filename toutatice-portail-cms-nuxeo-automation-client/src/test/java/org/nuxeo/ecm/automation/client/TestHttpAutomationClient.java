package org.nuxeo.ecm.automation.client;

import org.apache.commons.collections.MapUtils;
import org.junit.*;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.nuxeo.ecm.automation.client.model.Document;

import java.io.IOException;
import java.util.Map;

/**
 * @author Cédric Krommenhoek
 */
public class TestHttpAutomationClient {

    // Nuxeo URL
    private final String nuxeoUrl;
    // Secret key
    private final String secretKey;


    private HttpAutomationClient client;


    /**
     * Constructor.
     */
    public TestHttpAutomationClient() {
        super();
        Map<String, String> environnement = System.getenv();

        // Nuxeo URL
        this.nuxeoUrl = MapUtils.getString(environnement, "nuxeoUrl");
        // Secret key
        this.secretKey = MapUtils.getString(environnement, "secretKey", "secretKey");
    }


    @BeforeClass
    public static void beforeClass() throws Exception {
        // Don't run tests if Nuxeo URL is not defined
        Assume.assumeNotNull(System.getenv("nuxeoUrl"));
    }


    @Before
    public void setUp() {
        this.client = new HttpAutomationClient(this.nuxeoUrl + "/nuxeo/site/automation");
        this.client.setRequestInterceptor(new PortalSSOAuthInterceptor(this.secretKey, "admin"));
    }


    @After
    public void tearDown() {
        this.client.shutdown();
    }


    @Test
    public void testFetch() throws IOException {
        Session session = this.client.getSession();
        OperationRequest operation = session.newRequest("Document.Fetch");
        operation.set("value", "/default-domain/workspaces");
        Object result = operation.execute();
        Assert.assertTrue(result instanceof Document);

        Document document = (Document) result;
        Assert.assertEquals("Workspaces", document.getTitle());
    }

}
