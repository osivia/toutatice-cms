package fr.toutatice.portail.cms.nuxeo.portlets.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.automation.client.model.Document;

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
     * Set-up
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
        webPath = this.metadata.getWebPath(BASE_PATH);
        assertEquals(StringUtils.EMPTY, webPath);


        // Pages
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    webPath = this.metadata.getWebPath(BASE_PATH + "/page" + i + "/page" + i + j + "/page" + i + j + k);

                    if (k == 3) {
                        // No segment or webid
                        assertNull(webPath);
                    } else if ((i < 2) && (j < 2) && (k < 2)) {
                        // All parents have segment
                        assertEquals("/page-" + i + "/page-" + i + "-" + j + "/page-" + i + "-" + j + "-" + k, webPath);
                    } else if (((i > 1) || (j > 1)) && ((k % 2) == 1)) {
                        // A parent doesn't have segment and page doesn't have webid
                        assertNull(webPath);
                    } else if ((i > 1) && ((k % 2) == 0)) {
                        // First parent doesn't have segment
                        assertEquals("/id_page" + i + j + k, webPath);
                    } else if ((j > 1) && ((k % 2) == 0)) {
                        // Second parent doesn't have segment
                        assertEquals("/page-" + i + "/id_page" + i + j + k, webPath);
                    }
                }
            }
        }
    }

}
