package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osivia.portal.api.cache.services.ICacheService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.Satellite;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.extension.listener.AnnotationEnabler;
import org.powermock.core.classloader.annotations.PowerMockListener;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PowerMockListener(AnnotationEnabler.class)
@PrepareForTest(Locator.class)
public class DocumentsDiscoveryServiceTest {

    /** CMS service. */
    private static CMSService cmsService;


    /** Documents discovery service. */
    private DocumentsDiscoveryService documentsDiscoveryService;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Satellite BND
        Satellite satellite = new Satellite("bnd");
        Pattern pattern = Pattern.compile("^/e-[0-9]{7}[a-z]/projets1d/");
        satellite.setPaths(Arrays.asList(new Pattern[]{pattern}));

        // Satellites
        Set<Satellite> satellites = new HashSet<>();
        satellites.add(satellite);

        // CMS service
        cmsService = EasyMock.createMock(CMSService.class);
        EasyMock.expect(cmsService.getSatellites()).andReturn(satellites);

        // Cache service
        ICacheService cacheService = EasyMock.createMock(ICacheService.class);

        // Locator
        PowerMock.mockStatic(Locator.class);
        EasyMock.expect(Locator.findMBean(ICacheService.class, ICacheService.MBEAN_NAME)).andStubReturn(cacheService);


        // Replay
        PowerMock.replayAll(cmsService, cacheService);
    }


    @Before
    public void setUp() throws Exception {
        this.documentsDiscoveryService = DocumentsDiscoveryService.getInstance(cmsService);
    }


    @Test
    public void testPathRegexSearch() {
        String path;
        Satellite result;

        // Empty path
        path = null;
        result = this.documentsDiscoveryService.pathRegexSearch(path);
        Assert.assertNull(result);

        // Path P1D
        path = "/e-0352762m/projets1d/0350267A/premier-dossier";
        result = this.documentsDiscoveryService.pathRegexSearch(path);
        Assert.assertNotNull(result);

        // Other path
        path = "/e-0352762m/foo/bar";
        result = this.documentsDiscoveryService.pathRegexSearch(path);
        Assert.assertNull(result);
    }

}
