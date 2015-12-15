package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.DocumentsMetadata;

/**
 * Documents metadata implementation test class.
 *
 * @author Cédric Krommenhoek
 * @see DocumentsMetadataImpl
 */
public class DocumentsMetadataImplTest {

    /** Base path test value. */
    private static final String BASE_PATH = "/domain/site";


    /** Metadata object under test. */
    private DocumentsMetadataImpl metadata;


    /**
     * Constructor.
     */
    public DocumentsMetadataImplTest() {
        super();
    }


    /**
     * Set-up.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        List<Document> documents = new ArrayList<Document>();

        // Portal site
        Document portalSite = EasyMock.createMock(Document.class);
        EasyMock.expect(portalSite.getPath()).andReturn(BASE_PATH).anyTimes();
        EasyMock.expect(portalSite.getString("ottcweb:segment")).andReturn(StringUtils.EMPTY).anyTimes();
        EasyMock.expect(portalSite.getString("ttc:webid")).andReturn("site").anyTimes();
        EasyMock.replay(portalSite);
        documents.add(portalSite);


        for (int i = 0; i < 4; i++) {
            Document pageLevel1 = EasyMock.createMock(Document.class);
            EasyMock.expect(pageLevel1.getPath()).andReturn(BASE_PATH + "/page" + i).anyTimes();
            if (i < 2) {
                EasyMock.expect(pageLevel1.getString("ottcweb:segment")).andReturn("page-" + i).anyTimes();
            } else {
                EasyMock.expect(pageLevel1.getString("ottcweb:segment")).andReturn(null).anyTimes();
            }
            if ((i % 2) == 0) {
                EasyMock.expect(pageLevel1.getString("ttc:webid")).andReturn("page" + i).anyTimes();
            } else {
                EasyMock.expect(pageLevel1.getString("ttc:webid")).andReturn(null).anyTimes();
            }
            EasyMock.replay(pageLevel1);
            documents.add(pageLevel1);

            for (int j = 0; j < 4; j++) {
                Document pageLevel2 = EasyMock.createMock(Document.class);
                EasyMock.expect(pageLevel2.getPath()).andReturn(BASE_PATH + "/page" + i + "/page" + i + j).anyTimes();
                if (j < 2) {
                    EasyMock.expect(pageLevel2.getString("ottcweb:segment")).andReturn("page-" + i + "-" + j).anyTimes();
                } else {
                    EasyMock.expect(pageLevel2.getString("ottcweb:segment")).andReturn(null).anyTimes();
                }
                if ((j % 2) == 0) {
                    EasyMock.expect(pageLevel2.getString("ttc:webid")).andReturn("page" + i + j).anyTimes();
                } else {
                    EasyMock.expect(pageLevel2.getString("ttc:webid")).andReturn(null).anyTimes();
                }
                EasyMock.replay(pageLevel2);
                documents.add(pageLevel2);

                for (int k = 0; k < 4; k++) {
                    Document pageLevel3 = EasyMock.createMock(Document.class);
                    EasyMock.expect(pageLevel3.getPath()).andReturn(BASE_PATH + "/page" + i + "/page" + i + j + "/page" + i + j + k).anyTimes();
                    if (k < 2) {
                        EasyMock.expect(pageLevel3.getString("ottcweb:segment")).andReturn("page-" + i + "-" + j + "-" + k).anyTimes();
                    } else {
                        EasyMock.expect(pageLevel3.getString("ottcweb:segment")).andReturn(null).anyTimes();
                    }
                    if ((k % 2) == 0) {
                        EasyMock.expect(pageLevel3.getString("ttc:webid")).andReturn("page" + i + j + k).anyTimes();
                    } else {
                        EasyMock.expect(pageLevel3.getString("ttc:webid")).andReturn(null).anyTimes();
                    }
                    EasyMock.replay(pageLevel3);
                    documents.add(pageLevel3);

                }
            }
        }


        this.metadata = new DocumentsMetadataImpl(BASE_PATH, documents);
    }


    /**
     * Test getWebPath function.
     */
    @Test
    public final void testGetWebPath() {
        String webPath;

        // Portal site
        webPath = this.metadata.getWebPath("site");
        Assert.assertEquals(StringUtils.EMPTY, webPath);


        // Pages
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    webPath = this.metadata.getWebPath("page" + i + j + k);

                    if ((k % 2) == 1) {
                        // Missing webId
                        Assert.assertNull(webPath);
                    } else if ((i < 2) && (j < 2) && (k < 2)) {
                        // All items have segment
                        Assert.assertEquals("/page-" + i + "/page-" + i + "-" + j + "/page-" + i + "-" + j + "-" + k, webPath);
                    } else if (i > 1) {
                        // First item doesn't have segment
                        Assert.assertEquals("/id_page" + i + j + k, webPath);
                    } else if (j > 1) {
                        // Second item doesn't have segment
                        Assert.assertEquals("/page-" + i + "/id_page" + i + j + k, webPath);
                    } else if (k > 1) {
                        // Third item doesn't have segment
                        Assert.assertEquals("/page-" + i + "/page-" + i + "-" + j + "/id_page" + i + j + k, webPath);
                    } else {
                        // Unknown test case
                        Assert.fail(webPath);
                    }
                }
            }
        }
    }


    /**
     * Test getWebId function.
     */
    @Test
    public final void testGetWebId() {
        String webId;

        // Portal site
        webId = this.metadata.getWebId(StringUtils.EMPTY);
        Assert.assertEquals("site", webId);


        // Pages
        String webPath;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                webPath = "/page-" + i;
            } else if ((i % 2) == 0) {
                webPath = "/id_page" + i;
            } else {
                webPath = null;
            }
            webId = this.metadata.getWebId(webPath);
            if (webPath != null) {
                Assert.assertEquals("page" + i, webId);
            } else {
                Assert.assertNull(webId);
            }

            for (int j = 0; j < 4; j++) {
                if (i < 2) {
                    if (j == 0) {
                        webPath = "/page-" + i + "/page-" + i + "-" + j;
                    } else if ((j % 2) == 0) {
                        webPath = "/page-" + i + "/id_page" + i + j;
                    } else {
                        webPath = null;
                    }
                } else if ((j % 2) == 0) {
                    webPath = "/id_page" + i + j;
                } else {
                    webPath = null;
                }
                webId = this.metadata.getWebId(webPath);
                if (webPath != null) {
                    Assert.assertEquals("page" + i + j, webId);
                } else {
                    Assert.assertNull(webId);
                }

                for (int k = 0; k < 4; k++) {
                    if (i < 2) {
                        if (j < 2) {
                            if (k == 0) {
                                webPath = "/page-" + i + "/page-" + i + "-" + j + "/page-" + i + "-" + j + "-" + k;
                            } else if ((k % 2) == 0) {
                                webPath = "/page-" + i + "/page-" + i + "-" + j + "/id_page" + i + j + k;
                            } else {
                                webPath = null;
                            }
                        } else if ((k % 2) == 0) {
                            webPath = "/page-" + i + "/id_page" + i + j + k;
                        } else {
                            webPath = null;
                        }
                    } else if ((k % 2) == 0) {
                        webPath = "/id_page" + i + j + k;
                    } else {
                        webPath = null;
                    }
                    webId = this.metadata.getWebId(webPath);
                    if (webPath != null) {
                        Assert.assertEquals("page" + i + j + k, webId);
                    } else {
                        Assert.assertNull(webId);
                    }
                }
            }
        }
    }


    /**
     * Test update function.
     */
    @Test
    public final void testUpdate() {
        // Load cache
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    this.metadata.getWebId("/page-" + i + "/page-" + i + "-" + j + "/page-" + i + "-" + j + "-" + k);
                }
            }
        }


        // Documents
        List<Document> documents = new ArrayList<Document>();

        // Page 8
        Document page8 = EasyMock.createMock(Document.class);
        EasyMock.expect(page8.getPath()).andReturn(BASE_PATH + "/page8").anyTimes();
        EasyMock.expect(page8.getString("ottcweb:segment")).andReturn(null).anyTimes();
        EasyMock.expect(page8.getString("ttc:webid")).andReturn("page8").anyTimes();
        EasyMock.replay(page8);
        documents.add(page8);

        // Page 2-0 -> 8-0
        Document page80 = EasyMock.createMock(Document.class);
        EasyMock.expect(page80.getPath()).andReturn(BASE_PATH + "/page8/page80").anyTimes();
        EasyMock.expect(page80.getString("ottcweb:segment")).andReturn("page-8-0").anyTimes();
        EasyMock.expect(page80.getString("ttc:webid")).andReturn("page20").anyTimes();
        EasyMock.replay(page80);
        documents.add(page80);

        // Page 9
        Document page9 = EasyMock.createMock(Document.class);
        EasyMock.expect(page9.getPath()).andReturn(BASE_PATH + "/page9").anyTimes();
        EasyMock.expect(page9.getString("ottcweb:segment")).andReturn("page-9").anyTimes();
        EasyMock.expect(page9.getString("ttc:webid")).andReturn("page9").anyTimes();
        EasyMock.replay(page9);
        documents.add(page9);

        // Page 0-0 -> 9-0
        Document page00 = EasyMock.createMock(Document.class);
        EasyMock.expect(page00.getPath()).andReturn(BASE_PATH + "/page9/page90").anyTimes();
        EasyMock.expect(page00.getString("ottcweb:segment")).andReturn("page-9-0").anyTimes();
        EasyMock.expect(page00.getString("ttc:webid")).andReturn("page00").anyTimes();
        EasyMock.replay(page00);
        documents.add(page00);

        // New page 0-8
        Document page08 = EasyMock.createMock(Document.class);
        EasyMock.expect(page08.getPath()).andReturn(BASE_PATH + "/page0/page08").anyTimes();
        EasyMock.expect(page08.getString("ottcweb:segment")).andReturn(null).anyTimes();
        EasyMock.expect(page08.getString("ttc:webid")).andReturn("page08").anyTimes();
        EasyMock.replay(page08);
        documents.add(page08);

        // New page 0-9
        Document page09 = EasyMock.createMock(Document.class);
        EasyMock.expect(page09.getPath()).andReturn(BASE_PATH + "/page0/page09").anyTimes();
        EasyMock.expect(page09.getString("ottcweb:segment")).andReturn("page-0-9").anyTimes();
        EasyMock.expect(page09.getString("ttc:webid")).andReturn("page09").anyTimes();
        EasyMock.replay(page09);
        documents.add(page09);

        // New page 0-1-1 bis
        Document page011 = EasyMock.createMock(Document.class);
        EasyMock.expect(page011.getPath()).andReturn(BASE_PATH + "/page0/page01/page011bis").anyTimes();
        EasyMock.expect(page011.getString("ottcweb:segment")).andReturn("page-0-1-1").anyTimes();
        EasyMock.expect(page011.getString("ttc:webid")).andReturn("page011bis").anyTimes();
        EasyMock.replay(page011);
        documents.add(page011);


        // Updates
        DocumentsMetadata updates = new DocumentsMetadataImpl(BASE_PATH, documents);

        this.metadata.update(updates);


        String webPath;
        String webId;

        // Page 2-0
        // Get new web path
        webPath = this.metadata.getWebPath("page20");
        Assert.assertEquals("/id_page20", webPath);
        // Resolve web path with id
        webId = this.metadata.getWebId("/id_page20");
        Assert.assertEquals("page20", webId);

        // Page 0-0
        // Get new web path
        webPath = this.metadata.getWebPath("page00");
        Assert.assertEquals("/page-9/page-9-0", webPath);
        // Resolve old web path
        webId = this.metadata.getWebId("/page-0/page-0-0");
        Assert.assertEquals("page00", webId);
        // Resolve old web path with id
        webId = this.metadata.getWebId("/page-0/id_page00");
        Assert.assertEquals("page00", webId);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-9/page-9-0");
        Assert.assertEquals("page00", webId);
        // Resolve new web path with id
        webId = this.metadata.getWebId("/page-9/id_page00");
        Assert.assertEquals("page00", webId);

        // Page 0-0 child
        // Get new web path
        webPath = this.metadata.getWebPath("page000");
        Assert.assertEquals("/page-9/page-9-0/page-0-0-0", webPath);
        // Resolve old web path
        webId = this.metadata.getWebId("/page-0/page-0-0/page-0-0-0");
        Assert.assertEquals("page000", webId);
        // Resolve old web path with id
        webId = this.metadata.getWebId("/page-0/page-0-0/id_page000");
        Assert.assertEquals("page000", webId);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-9/page-9-0/page-0-0-0");
        Assert.assertEquals("page000", webId);
        // Resolve new web path with id
        webId = this.metadata.getWebId("/page-9/page-9-0/id_page000");
        Assert.assertEquals("page000", webId);


        // Page 0-8
        // Get new web path
        webPath = this.metadata.getWebPath("page08");
        Assert.assertEquals("/page-0/id_page08", webPath);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-0/id_page08");
        Assert.assertEquals("page08", webId);

        // Page 0-9
        // Get new web path
        webPath = this.metadata.getWebPath("page09");
        Assert.assertEquals("/page-0/page-0-9", webPath);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-0/page-0-9");
        Assert.assertEquals("page09", webId);
        // Resolve new web path with id
        webId = this.metadata.getWebId("/page-0/id_page09");
        Assert.assertEquals("page09", webId);

        // Page 0-1-1 bis
        // Get web path
        webPath = this.metadata.getWebPath("page011bis");
        Assert.assertEquals("/page-0/page-0-1/page-0-1-1", webPath);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-0/page-0-1/page-0-1-1");
        Assert.assertEquals("page011bis", webId);
        // Resolve new web path with id
        webId = this.metadata.getWebId("/page-0/page-0-1/id_page011bis");
        Assert.assertEquals("page011bis", webId);
    }

}
