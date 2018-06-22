package fr.toutatice.portail.cms.nuxeo.portlets.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSPublicationInfos;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.Satellite;

/**
 * Documents discovery service implementation.
 *
 * @author ckrommenhoek
 */
public class DocumentsDiscoveryService {

    /** Singleton instance. */
    private static DocumentsDiscoveryService instance;


    /** Satellites. */
    private Map<String, Satellite> satellites;


    /** CMS service. */
    private final CMSService cmsService;

    /** Cache. */
    private final Map<String, Satellite> cache;


    /**
     * Constructor.
     *
     * @param cmsService CMS service
     */
    private DocumentsDiscoveryService(CMSService cmsService) {
        super();
        this.cmsService = cmsService;
        this.cache = new ConcurrentHashMap<>();
    }


    /**
     * Get singleton instance.
     * 
     * @param cmsService CMS service
     * @return singleton instance
     */
    public static DocumentsDiscoveryService getInstance(CMSService cmsService) {
        if (instance == null) {
            initInstance(cmsService);
        }
        return instance;
    }


    /**
     * Singleton instance initialization.
     * 
     * @param cmsService CMS service
     */
    private synchronized static void initInstance(CMSService cmsService) {
        if (instance == null) {
            instance = new DocumentsDiscoveryService(cmsService);
        }
    }


    /**
     * Satellites initialization.
     *
     * @throws CMSException
     */
    private synchronized void initSatellites() throws CMSException {
        if (this.satellites == null) {
            Set<Satellite> satellites = this.cmsService.getSatellites();

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
     * Discover document location.
     * 
     * @param cmsContext CMS context
     * @param path document path
     * @return location
     * @throws CMSException
     */
    public Satellite discoverLocation(CMSServiceCtx cmsContext, String path) throws CMSException {
        Satellite result;

        if (StringUtils.isEmpty(path)) {
            result = null;
        } else {
            result = this.cache.get(path);
            
            if (result == null) {
                if (this.satellites == null) {
                    this.initSatellites();
                }

                // Path regex matching
                Iterator<Satellite> satelliteIterator = this.satellites.values().iterator();
                while ((result == null) && satelliteIterator.hasNext()) {
                    Satellite satellite = satelliteIterator.next();
                    if (CollectionUtils.isNotEmpty(satellite.getPaths())) {
                        Iterator<Pattern> pathsIterator = satellite.getPaths().iterator();
                        while ((result == null) && pathsIterator.hasNext()) {
                            Pattern pattern = pathsIterator.next();
                            Matcher matcher = pattern.matcher(path);
                            if (matcher.matches()) {
                                result = satellite;
                            }
                        }
                    }
                }

                DiscoveryResult discoveryResult;
                if (result == null) {
                    // Search on main satellite
                    List<Satellite> main = Arrays.asList(new Satellite[]{Satellite.MAIN});

                    // Invocation
                    List<DiscoveryResult> discoveryResults = this.invoke(cmsContext, path, main);

                    if (discoveryResults.size() == 1) {
                        discoveryResult = discoveryResults.get(0);
                    } else {
                        // Search on all satellites, but main
                        List<Satellite> others = new ArrayList<>(this.satellites.values());
                        others.remove(Satellite.MAIN);

                        CMSServiceCtx discoveryCtx = new CMSServiceCtx();
                        discoveryCtx.setScope("superuser_context");
                        discoveryCtx.setForcePublicationInfosScope("superuser_context");
                        
                        discoveryResults = this.invoke(discoveryCtx, path, others);

                        if (discoveryResults.size() == 0) {
                            throw new CMSException(CMSException.ERROR_NOTFOUND);
                        } else if (discoveryResults.size() == 1) {
                            discoveryResult = discoveryResults.get(0);
                        } else {
                            throw new CMSException(CMSException.ERROR_UNAVAILAIBLE);
                        }
                    }

                    if (discoveryResult != null) {
                        result = discoveryResult.satellite;
                    }
                } else {
                    discoveryResult = null;
                }

                // Update cache
                this.cache.put(path, result);
                if ((discoveryResult != null) && StringUtils.isNotEmpty(discoveryResult.path) && !StringUtils.equals(path, discoveryResult.path)) {
                    this.cache.put(discoveryResult.path, result);
                }
            }
        }

        return result;
    }


    /**
     * Invoke discovery.
     * 
     * @param cmsContext CMS context
     * @param path path
     * @param satellites satellites
     * @return discovery results
     * @throws CMSException
     */
    private List<DiscoveryResult> invoke(CMSServiceCtx cmsContext, String path, List<Satellite> satellites) throws CMSException {
        int threadPoolSize = satellites.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // Results
        List<DiscoveryResult> discoveryResults = new ArrayList<>(threadPoolSize);

        // Tasks
        List<DiscoveryCallable> tasks = new ArrayList<>(threadPoolSize);
        for (Satellite satellite : satellites) {
            DiscoveryCallable task = new DiscoveryCallable(this.cmsService, cmsContext, satellite, path);
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

                if ((discoveryResult.error == 0) && StringUtils.isNotEmpty(discoveryResult.path)) {
                    discoveryResults.add(discoveryResult);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new CMSException(e);
            }
        }

        return discoveryResults;
    }


    /**
     * Discovery callable.
     *
     * @author ckrommenhoek
     * @see Callable
     * @see DiscoveryResult
     */
    private class DiscoveryCallable implements Callable<DiscoveryResult> {

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

            // Saved satellite
            Satellite savedSatellite = this.cmsContext.getSatellite();
            try {
                this.cmsContext.setSatellite(this.satellite);

                PublishInfosCommand command = new PublishInfosCommand(this.satellite, this.path);
                CMSPublicationInfos publicationInfos = (CMSPublicationInfos) DocumentsDiscoveryService.this.cmsService.executeNuxeoCommand(this.cmsContext,
                        command);

                if (publicationInfos != null) {
                    result.path = publicationInfos.getDocumentPath();
                }
            } catch (CMSException e) {
                result.error = e.getErrorCode();
            } finally {
                this.cmsContext.setSatellite(savedSatellite);
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
