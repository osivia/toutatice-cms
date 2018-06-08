package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;
import org.osivia.portal.core.cms.Satellite;

import fr.toutatice.portail.cms.nuxeo.api.services.IDocumentsDiscoveryService;

/**
 * Documents discovery service implementation.
 * 
 * @author ckrommenhoek
 * @see IDocumentsDiscoveryService
 */
public class DocumentsDiscoveryService implements IDocumentsDiscoveryService {

    /** Satellites. */
    private Map<String, Satellite> satellites;


    /** Cache. */
    private final Map<String, Satellite> cache;

    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;


    /**
     * Constructor.
     */
    public DocumentsDiscoveryService() {
        super();
        this.cache = new ConcurrentHashMap<>();

        // CMS service locator
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
    }


    /**
     * Satellites initialization.
     * 
     * @throws CMSException
     */
    private synchronized void initSatellites() throws CMSException {
        if (this.satellites == null) {
            // CMS service
            ICMSService cmsService = this.cmsServiceLocator.getCMSService();

            Set<Satellite> satellites = cmsService.getSatellites();

            if (CollectionUtils.isEmpty(satellites)) {
                this.satellites = new ConcurrentHashMap<>(1);
            } else {
                this.satellites = new ConcurrentHashMap<>(satellites.size() + 1);
                for (Satellite satellite : satellites) {
                    this.satellites.put(satellite.getId(), satellite);
                }
            }

            // Add main satellite
            Satellite main = Satellite.MAIN;
            this.satellites.put(main.getId(), main);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Satellite discoverLocation(CMSServiceCtx cmsContext, String path) throws CMSException {
        Satellite result = this.cache.get(path);

        if (result == null) {
            // CMS service
            ICMSService cmsService = this.cmsServiceLocator.getCMSService();

            // Saved publication infos scope
            String savedScope = cmsContext.getForcePublicationInfosScope();

            if (this.satellites == null) {
                this.initSatellites();
            }

            int threadPoolSize = this.satellites.size();
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

            // Results
            List<DiscoveryResult> discoveryResults = new ArrayList<>(threadPoolSize);

            try {
                cmsContext.setForcePublicationInfosScope("superuser_context");

                // Tasks
                List<DiscoveryCallable> tasks = new ArrayList<>(threadPoolSize);
                for (Satellite satellite : this.satellites.values()) {
                    DiscoveryCallable task = new DiscoveryCallable(cmsService, cmsContext, satellite, path);
                    tasks.add(task);
                }

                // Futures
                List<Future<DiscoveryResult>> futures;
                try {
                    futures = executor.invokeAll(tasks, 10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new CMSException(e);
                }

                for (Future<DiscoveryResult> future : futures) {
                    try {
                        DiscoveryResult discoveryResult = future.get();

                        if (discoveryResult.error == 0) {
                            discoveryResults.add(discoveryResult);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        throw new CMSException(e);
                    }
                }
            } finally {
                cmsContext.setForcePublicationInfosScope(savedScope);
            }

            if (discoveryResults.size() == 1) {
                DiscoveryResult discoveryResult = discoveryResults.get(0);
                result = discoveryResult.satellite;

                // Update cache
                this.cache.put(path, result);
                if (StringUtils.isNotEmpty(discoveryResult.path) && !StringUtils.equals(path, discoveryResult.path)) {
                    this.cache.put(discoveryResult.path, result);
                }
            } else {
                // TODO
            }

        }

        return result;
    }


    /**
     * Discovery callable.
     * 
     * @author ckrommenhoek
     * @see Callable
     * @see DiscoveryResult
     */
    private class DiscoveryCallable implements Callable<DiscoveryResult> {

        /** CMS service. */
        private final ICMSService cmsService;
        /** CMS context. */
        private final CMSServiceCtx cmsContext;
        /** Satellite. */
        private final Satellite satellite;
        /** Path. */
        private final String path;


        /**
         * Constructor.
         * 
         * @param cmsService CMS service
         * @param cmsContext CMS context
         * @param satellite satellite
         * @param path path
         */
        public DiscoveryCallable(ICMSService cmsService, CMSServiceCtx cmsContext, Satellite satellite, String path) {
            super();
            this.cmsService = cmsService;
            this.cmsContext = cmsContext;
            this.satellite = satellite;
            this.path = path;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public DiscoveryResult call() throws Exception {
            DiscoveryResult result = new DiscoveryResult(this.satellite);

            try {
                CMSPublicationInfos publicationInfos = this.cmsService.getPublicationInfos(this.cmsContext, this.path);
                result.path = publicationInfos.getDocumentPath();
            } catch (CMSException e) {
                result.error = e.getErrorCode();
            }

            return result;
        }

    }


    /**
     * Discovery result.
     * 
     * @author ckrommenhoek
     */
    private class DiscoveryResult {

        /** Result error code. */
        private int error;
        /** Result path. */
        private String path;


        /** Satellite. */
        private final Satellite satellite;


        /**
         * Constructor.
         * 
         * @param satellite satellite
         */
        public DiscoveryResult(Satellite satellite) {
            super();
            this.satellite = satellite;
        }

    }

}
