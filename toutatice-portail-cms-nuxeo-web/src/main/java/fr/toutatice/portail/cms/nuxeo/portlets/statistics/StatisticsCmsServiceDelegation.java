package fr.toutatice.portail.cms.nuxeo.portlets.statistics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.client.model.PropertyList;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.statistics.SpaceStatistics;
import org.osivia.portal.api.statistics.SpaceVisits;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;

/**
 * Statistics CMS service delegation.
 * 
 * @author Cédric Krommenhoek
 */
public class StatisticsCmsServiceDelegation {

    /** Day date format. */
    public static final DateFormat DAY_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    /** Month date format. */
    public static final DateFormat MONTH_FORMAT = new SimpleDateFormat("MM-yyyy");


    /**
     * Constructor.
     */
    public StatisticsCmsServiceDelegation() {
        super();
    }


    /**
     * Get space statistics.
     * 
     * @param cmsContext CMS context
     * @param paths space paths
     * @return space statistics
     * @throws CMSException
     */
    public List<SpaceStatistics> getSpaceStatistics(CMSServiceCtx cmsContext, Set<String> paths) throws CMSException {
        // Portlet context
        PortletContext portletContext = cmsContext.getPortletCtx();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Space statistics
        List<SpaceStatistics> statistics;

        if (CollectionUtils.isEmpty(paths)) {
            statistics = new ArrayList<>(0);
        } else {
            // Nuxeo command
            INuxeoCommand command = new GetSpaceStatisticsCommand(paths);

            // Documents
            Documents documents = (Documents) nuxeoController.executeNuxeoCommand(command);

            statistics = new ArrayList<>(documents.size());

            for (Document document : documents) {
                statistics.add(this.convertSpaceStatistics(document));
            }
        }

        return statistics;
    }


    /**
     * Convert space statistics from Nuxeo document.
     * 
     * @param document space Nuxeo document
     * @return statistics
     */
    private SpaceStatistics convertSpaceStatistics(Document document) {
        // Statistics
        SpaceStatistics statistics = new SpaceStatistics(document.getPath());

        // Space properties
        PropertyMap properties = document.getProperties();

        // Last update
        Date lastUpdate = properties.getDate("stats:lastUpdate");
        statistics.setLastUpdate(lastUpdate);

        // Current day visits
        SpaceVisits currentDayVisits = this.getVisits(properties.getMap("stats:dayVisits"), false);
        statistics.setCurrentDayVisits(currentDayVisits);

        // Current month visits
        SpaceVisits currentMonthVisits = this.getVisits(properties.getMap("stats:monthVisits"), false);
        statistics.setCurrentMonthVisits(currentMonthVisits);

        // Last days visits
        Map<Date, SpaceVisits> lastDaysVisits = this.getHistorizedVisits(properties.getList("stats:days"), DAY_FORMAT);
        statistics.getHistorizedDaysVisits().putAll(lastDaysVisits);

        // Last months visits
        Map<Date, SpaceVisits> lastMonthVisits = this.getHistorizedVisits(properties.getList("stats:months"), MONTH_FORMAT);
        statistics.getHistorizedMonthsVisits().putAll(lastMonthVisits);

        return statistics;
    }


    /**
     * Get visits.
     * 
     * @param properties visits document properties
     * @param historized true for historized period visits, false for current period visits
     * @return visits
     */
    private SpaceVisits getVisits(PropertyMap properties, boolean historized) {
        // Visits
        SpaceVisits visits = new SpaceVisits();

        if (properties != null) {
            // Hits
            visits.setHits(NumberUtils.toInt(properties.getString("hits")));

            if (historized) {
                // Unique visitors count
                visits.setUniqueVisitors(NumberUtils.toInt(properties.getString("uniqueVisitors")));
            } else {
                // Anonymous visitors count
                visits.setAnonymousVisitors(NumberUtils.toInt(properties.getString("anonymous")));

                // Identified visitors
                PropertyList identifiers = properties.getList("identifiers");
                if ((identifiers != null) && !identifiers.isEmpty()) {
                    for (int i = 0; i < identifiers.size(); i++) {
                        String visitor = identifiers.getString(i);
                        visits.getVisitors().add(visitor);
                    }
                }
            }
        }

        return visits;
    }


    /**
     * Get historized visits.
     * 
     * @param list historized periods list
     * @param dateFormat period date format
     * @return historized visits
     */
    private Map<Date, SpaceVisits> getHistorizedVisits(PropertyList list, DateFormat dateFormat) {
        // Historized visits
        Map<Date, SpaceVisits> historizedVisits;

        if ((list == null) || list.isEmpty()) {
            historizedVisits = new HashMap<>(0);
        } else {
            historizedVisits = new HashMap<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                PropertyMap map = list.getMap(i);

                // Date
                Date date;
                try {
                    date = dateFormat.parse(map.getString("date"));
                } catch (ParseException e) {
                    date = null;
                }

                if (date != null) {
                    // Visits
                    SpaceVisits visit = this.getVisits(map, true);

                    historizedVisits.put(date, visit);
                }
            }
        }

        return historizedVisits;
    }


    /**
     * Update space statistics.
     * 
     * @param cmsContext CMS context
     * @param httpSession HTTP session
     * @param spaceStatistics space statistics
     * @throws CMSException
     */
    public void updateStatistics(CMSServiceCtx cmsContext, HttpSession httpSession, List<SpaceStatistics> spaceStatistics) throws CMSException {
        // Portlet context
        PortletContext portletContext = cmsContext.getPortletCtx();
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_NONE);

        // Nuxeo command
        INuxeoCommand command = new UpdateSpaceStatisticsCommand(httpSession.getId(), spaceStatistics);
        nuxeoController.executeNuxeoCommand(command);
    }

}
