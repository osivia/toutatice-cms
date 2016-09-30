package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.core.cms.DocumentsMetadata;

import fr.toutatice.portail.cms.nuxeo.api.domain.Symlink;

/**
 * Documents metadata implementation test class.
 *
 * @author Cédric Krommenhoek
 * @see DocumentsMetadataImpl
 */
public class DocumentsMetadataImplTest {

    /** Base path. */
    private static final String BASE_PATH = "/domain/site";
    /** Other path. */
    private static final String OTHER_PATH = "/domain/other";
    /** Base timestamp. */
    private static final long BASE_TIMESTAMP = 1000000;


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
        // Documents
        List<Document> documents = new ArrayList<Document>();
        // Symlinks
        List<Symlink> symlinks = new ArrayList<Symlink>();


        String path;
        String segment;
        String webId;
        Date modified;

        String parentPath;
        String targetPath;
        String targetWebId;


        // Portal site
        path = BASE_PATH;
        segment = null;
        webId = "site";
        modified = new Date(BASE_TIMESTAMP);

        Document portalSite = this.createDocumentMock(path, segment, webId, modified);
        documents.add(portalSite);


        for (int i = 0; i < 4; i++) {
            // Page level #1
            path = BASE_PATH + "/page" + i;
            if (i < 2) {
                segment = "page-" + i;
            } else {
                segment = null;
            }
            if ((i % 2) == 0) {
                webId = "page" + i;
            } else {
                webId = null;
            }
            modified = new Date(BASE_TIMESTAMP * (i + 1));

            Document pageLevel1 = this.createDocumentMock(path, segment, webId, modified);
            documents.add(pageLevel1);


            // Symlink
            parentPath = BASE_PATH + "/page" + i;
            segment = "link";
            targetPath = OTHER_PATH + "/folder" + i;
            targetWebId = "folder" + i;

            Symlink symlink = new Symlink(parentPath, segment, targetPath, targetWebId);
            symlinks.add(symlink);


            // Other path folder
            path = OTHER_PATH + "/folder" + i;
            segment = null;
            webId = "folder" + i;

            Document folder = this.createDocumentMock(path, segment, webId, modified);
            documents.add(folder);


            for (int j = 0; j < 4; j++) {
                // Page level #2
                path = BASE_PATH + "/page" + i + "/page" + i + j;
                if (j < 2) {
                    segment = "page-" + i + "-" + j;
                } else {
                    segment = null;
                }
                if ((j % 2) == 0) {
                    webId = "page" + i + j;
                } else {
                    webId = null;
                }
                modified = new Date(BASE_TIMESTAMP * (i + 1) * (j + 1));

                Document pageLevel2 = this.createDocumentMock(path, segment, webId, modified);
                documents.add(pageLevel2);


                // Other path file
                path = OTHER_PATH + "/folder" + i + "/file" + j;
                if (j < 2) {
                    segment = "file-" + j;
                } else {
                    segment = null;
                }
                if ((j % 2) == 0) {
                    webId = "file" + i + j;
                } else {
                    webId = null;
                }

                Document file = this.createDocumentMock(path, segment, webId, modified);
                documents.add(file);


                for (int k = 0; k < 4; k++) {
                    // Page level #3
                    path = BASE_PATH + "/page" + i + "/page" + i + j + "/page" + i + j + k;
                    if (k < 2) {
                        segment = "page-" + i + "-" + j + "-" + k;
                    } else {
                        segment = null;
                    }
                    if ((k % 2) == 0) {
                        webId = "page" + i + j + k;
                    } else {
                        webId = null;
                    }
                    modified = new Date(BASE_TIMESTAMP * (i + 1) * (j + 1) * (k + 1));

                    Document pageLevel3 = this.createDocumentMock(path, segment, webId, modified);
                    documents.add(pageLevel3);
                }
            }
        }

        // Shuffle
        Collections.shuffle(documents);

        this.metadata = new DocumentsMetadataImpl(BASE_PATH, documents, symlinks);
    }


    /**
     * Create document mock.
     *
     * @param path document path
     * @param segment document web URL segment
     * @param webId document webId
     * @param modified document modified date
     * @return document mock
     */
    private Document createDocumentMock(String path, String segment, String webId, Date modified) {
        Document document = EasyMock.createMock(Document.class);
        EasyMock.expect(document.getPath()).andReturn(path).anyTimes();
        EasyMock.expect(document.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn(segment).anyTimes();
        EasyMock.expect(document.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn(webId).anyTimes();
        EasyMock.expect(document.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(modified).anyTimes();
        EasyMock.replay(document);
        return document;
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


        // Symlink
        for (int i = 0; i < 4; i++) {
            webPath = this.metadata.getWebPath("folder" + i);

            if (i < 2) {
                Assert.assertEquals("/page-" + i + "/link", webPath);
            } else {
                // Missing page segment
                Assert.assertEquals("/id_folder" + i, webPath);
            }


            for (int j = 0; j < 4; j++) {
                webPath = this.metadata.getWebPath("file" + i + j);

                if ((j % 2) == 1) {
                    // Missing webId
                    Assert.assertNull(webPath);
                } else if ((i < 2) && (j < 2)) {
                    // Segment
                    Assert.assertEquals("/page-" + i + "/link/file-" + j, webPath);
                } else if (i < 2) {
                    // WebId
                    Assert.assertEquals("/page-" + i + "/link/id_file" + i + j, webPath);
                } else {
                    // Missing page segment
                    Assert.assertEquals("/id_file" + i + j, webPath);
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


        // Symlink
        for (int i = 0; i < 4; i++) {
            if (i < 2) {
                webPath = "/page-" + i + "/link";
            } else {
                webPath = "/id_folder" + i;
            }

            webId = this.metadata.getWebId(webPath);
            Assert.assertEquals("folder" + i, webId);


            for (int j = 0; j < 4; j++) {
                if ((j % 2) == 1) {
                    // Missing webId
                    webPath = null;
                } else if ((i < 2) && (j < 2)) {
                    // Segment
                    webPath = "/page-" + i + "/link/file-" + j;
                } else if (i < 2) {
                    // WebId
                    webPath = "/page-" + i + "/id_file" + i + j;
                } else {
                    // Missing page segment
                    webPath = "/id_file" + i + j;
                }

                webId = this.metadata.getWebId(webPath);
                if (webPath == null) {
                    Assert.assertNull(webId);
                } else {
                    Assert.assertEquals("file" + i + j, webId);
                }
            }
        }
    }


    /**
     * Test getTimestamp function.
     */
    @Test
    public final void testGetTimestamp() {
        long timestamp = this.metadata.getTimestamp();
        Assert.assertEquals((BASE_TIMESTAMP * Double.valueOf(Math.pow(4, 3)).longValue()) + 10, timestamp);
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
                    this.metadata.getWebPath("page" + i + j + k);
                    this.metadata.getWebId("/page-" + i + "/page-" + i + "-" + j + "/page-" + i + "-" + j + "-" + k);
                }
            }
        }


        // Check web path with missing parent segment
        String webPath120 = this.metadata.getWebPath("page120");
        Assert.assertEquals("/page-1/id_page120", webPath120);


        // Documents
        List<Document> documents = new ArrayList<Document>();

        // Remove segment to page 1-0
        Document page11 = EasyMock.createMock(Document.class);
        EasyMock.expect(page11.getPath()).andReturn(BASE_PATH + "/page1/page10").anyTimes();
        EasyMock.expect(page11.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn(null).anyTimes();
        EasyMock.expect(page11.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page10").anyTimes();
        EasyMock.expect(page11.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page11);
        documents.add(page11);

        // Add segment to page 1-2
        Document page12 = EasyMock.createMock(Document.class);
        EasyMock.expect(page12.getPath()).andReturn(BASE_PATH + "/page1/page12").anyTimes();
        EasyMock.expect(page12.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-1-2").anyTimes();
        EasyMock.expect(page12.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page12").anyTimes();
        EasyMock.expect(page12.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page12);
        documents.add(page12);

        // Page 8
        Document page8 = EasyMock.createMock(Document.class);
        EasyMock.expect(page8.getPath()).andReturn(BASE_PATH + "/page8").anyTimes();
        EasyMock.expect(page8.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn(null).anyTimes();
        EasyMock.expect(page8.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page8").anyTimes();
        EasyMock.expect(page8.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page8);
        documents.add(page8);

        // Page 2-0 -> 8-0
        Document page80 = EasyMock.createMock(Document.class);
        EasyMock.expect(page80.getPath()).andReturn(BASE_PATH + "/page8/page80").anyTimes();
        EasyMock.expect(page80.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-8-0").anyTimes();
        EasyMock.expect(page80.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page20").anyTimes();
        EasyMock.expect(page80.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page80);
        documents.add(page80);

        // Page 9
        Document page9 = EasyMock.createMock(Document.class);
        EasyMock.expect(page9.getPath()).andReturn(BASE_PATH + "/page9").anyTimes();
        EasyMock.expect(page9.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-9").anyTimes();
        EasyMock.expect(page9.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page9").anyTimes();
        EasyMock.expect(page9.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page9);
        documents.add(page9);

        // Page 0-0 -> 9-0
        Document page00 = EasyMock.createMock(Document.class);
        EasyMock.expect(page00.getPath()).andReturn(BASE_PATH + "/page9/page90").anyTimes();
        EasyMock.expect(page00.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-9-0").anyTimes();
        EasyMock.expect(page00.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page00").anyTimes();
        EasyMock.expect(page00.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page00);
        documents.add(page00);

        // New page 0-8
        Document page08 = EasyMock.createMock(Document.class);
        EasyMock.expect(page08.getPath()).andReturn(BASE_PATH + "/page0/page08").anyTimes();
        EasyMock.expect(page08.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn(null).anyTimes();
        EasyMock.expect(page08.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page08").anyTimes();
        EasyMock.expect(page08.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page08);
        documents.add(page08);

        // New page 0-9
        Document page09 = EasyMock.createMock(Document.class);
        EasyMock.expect(page09.getPath()).andReturn(BASE_PATH + "/page0/page09").anyTimes();
        EasyMock.expect(page09.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-0-9").anyTimes();
        EasyMock.expect(page09.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page09").anyTimes();
        EasyMock.expect(page09.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page09);
        documents.add(page09);

        // New page 0-1-1 bis
        Document page011 = EasyMock.createMock(Document.class);
        EasyMock.expect(page011.getPath()).andReturn(BASE_PATH + "/page0/page01/page011bis").anyTimes();
        EasyMock.expect(page011.getString(DocumentsMetadataImpl.WEB_URL_SEGMENT_PROPERTY)).andReturn("page-0-1-1").anyTimes();
        EasyMock.expect(page011.getString(DocumentsMetadataImpl.WEB_ID_PROPERTY)).andReturn("page011bis").anyTimes();
        EasyMock.expect(page011.getDate(DocumentsMetadataImpl.MODIFIED_PROPERTY)).andReturn(new Date(BASE_TIMESTAMP)).anyTimes();
        EasyMock.replay(page011);
        documents.add(page011);


        // Symlinks
        List<Symlink> symlinks = new ArrayList<Symlink>();


        // Updates
        DocumentsMetadata updates = new DocumentsMetadataImpl(BASE_PATH, documents, symlinks);

        this.metadata.update(updates);


        String webPath;
        String webId;

        // Page 1-0
        // Check web path with removed parent segment
        webPath = this.metadata.getWebPath("page100");
        Assert.assertEquals("/page-1/id_page100", webPath);
        // Resolve old web path
        webId = this.metadata.getWebId("/page-1/page-1-0/page-1-0-0");
        Assert.assertEquals("page100", webId);

        // Page 1-2
        // Check web path with new parent segment
        webPath = this.metadata.getWebPath("page120");
        Assert.assertEquals("/page-1/page-1-2/page-1-2-0", webPath);
        // Resolve new web path
        webId = this.metadata.getWebId("/page-1/page-1-2/page-1-2-0");
        Assert.assertEquals("page120", webId);

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
