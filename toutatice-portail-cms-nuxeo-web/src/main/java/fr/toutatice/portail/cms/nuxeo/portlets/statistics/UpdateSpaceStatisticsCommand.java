package fr.toutatice.portail.cms.nuxeo.portlets.statistics;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.time.DateUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.statistics.SpaceStatistics;
import org.osivia.portal.api.statistics.SpaceVisits;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Update space statistics Nuxeo command
 * 
 * @author Cédric Krommenhoek
 * @see INuxeoCommand
 */
public class UpdateSpaceStatisticsCommand implements INuxeoCommand {

    /** HTTP session identifier. */
    private final String sessionId;
    /** Space statistics. */
    private final List<SpaceStatistics> spaceStatistics;


    /**
     * Constructor.
     * 
     * @param sessionId HTTP session identifier
     * @param spaceStatistics space statistics
     * @param dayFormat day date format
     * @param monthFormat month date format
     */
    public UpdateSpaceStatisticsCommand(String sessionId, List<SpaceStatistics> spaceStatistics) {
        super();
        this.sessionId = sessionId;
        this.spaceStatistics = spaceStatistics;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(Session nuxeoSession) throws Exception {
        for (SpaceStatistics statistics : this.spaceStatistics) {
            DocRef docRef = new DocRef(statistics.getPath());

            // Update properties
            this.updateProperties(nuxeoSession, docRef, statistics);

            // Update history
            this.updateHistory(nuxeoSession, docRef, statistics);
        }

        return null;
    }


    /**
     * Update document properties.
     * 
     * @param nuxeoSession Nuxeo session
     * @param docRef document reference
     * @param statistics space statistics
     * @throws Exception
     */
    private void updateProperties(Session nuxeoSession, DocRef docRef, SpaceStatistics statistics) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        // Updated properties
        PropertyMap properties = new PropertyMap();
        properties.set("stats:lastUpdate", statistics.getLastUpdate());
        properties.set("stats:dayVisits", this.convertCurrentVisitsJsonValue(statistics.getCurrentDayVisits()));
        properties.set("stats:monthVisits", this.convertCurrentVisitsJsonValue(statistics.getCurrentMonthVisits()));

        documentService.update(docRef, properties);
    }


    /**
     * Convert current visits to JSON.
     * 
     * @param visits current visits
     * @return JSON
     */
    private String convertCurrentVisitsJsonValue(SpaceVisits visits) {
        // Value JSON object
        JSONObject value = new JSONObject();

        if (visits != null) {
            // Identified visitors JSON array
            JSONArray identifiers = new JSONArray();
            identifiers.addAll(visits.getVisitors());

            value.put("hits", visits.getHits());
            value.put("anonymous", visits.getAnonymousVisitors());
            value.put("identifiers", identifiers);
        }

        return value.toString();
    }


    /**
     * Update statistics history.
     * 
     * @param nuxeoSession Nuxeo session
     * @param docRef document reference
     * @param statistics space statistics
     * @throws Exception
     */
    private void updateHistory(Session nuxeoSession, DocRef docRef, SpaceStatistics statistics) throws Exception {
        // Last update
        Date lastUpdate = statistics.getLastUpdate();
        // Previous update
        Date previousUpdate = statistics.getPreviousUpdate();

        if (previousUpdate != null) {
            if (!DateUtils.isSameDay(previousUpdate, lastUpdate)) {
                this.updateDaysHistory(nuxeoSession, docRef, statistics);

                if (!DateUtils.truncatedEquals(previousUpdate, lastUpdate, Calendar.MONTH)) {
                    this.updateMonthsHistory(nuxeoSession, docRef, statistics);
                }
            }
        }
    }


    /**
     * Update days history.
     * 
     * @param nuxeoSession Nuxeo session
     * @param docRef document reference
     * @param statistics space statistics
     * @throws Exception
     */
    private void updateDaysHistory(Session nuxeoSession, DocRef docRef, SpaceStatistics statistics) throws Exception {
        // Document service
        DocumentService documentService = nuxeoSession.getAdapter(DocumentService.class);

        // Previous day
        Date previousDay = DateUtils.truncate(statistics.getPreviousUpdate(), Calendar.DAY_OF_MONTH);

        // First history day
        Date firstHistoryDay = DateUtils.addDays(DateUtils.truncate(statistics.getLastUpdate(), Calendar.DAY_OF_MONTH), -30);

        // Historized days
        Map<Date, SpaceVisits> historizedDays = statistics.getHistorizedDaysVisits();

        // Historized days JSON array
        JSONArray array = new JSONArray();

        // Require update indicator
        boolean requireUpdate = false;

        for (Entry<Date, SpaceVisits> entry : historizedDays.entrySet()) {
            if (entry.getKey().before(firstHistoryDay)) {
                requireUpdate = true;
            } else {
                String date = StatisticsCmsServiceDelegation.DAY_FORMAT.format(entry.getKey());
                JSON json = this.convertHistorizedVisits(date, entry.getValue());
                array.add(json);
            }
        }

        if (requireUpdate) {
            // Updated properties
            PropertyMap properties = new PropertyMap();
            properties.set("stats:days", array.toString());
    
            documentService.update(docRef, properties);
        } else {
            if (!previousDay.before(firstHistoryDay)) {
                // Previous historized day
                SpaceVisits historizedDay = historizedDays.get(previousDay);

                if (historizedDay != null) {
                    String date = StatisticsCmsServiceDelegation.DAY_FORMAT.format(previousDay);
                    this.addToHistory(nuxeoSession, docRef, "stats:days", date, historizedDay);
                }
            }
        }
    }


    /**
     * Convert historized visits to JSON.
     * 
     * @param date date
     * @param visits historized visits
     * @return JSON
     */
    private JSON convertHistorizedVisits(String date, SpaceVisits visits) {
        JSONObject object = new JSONObject();
        object.put("date", date);
        object.put("hits", visits.getHits());
        object.put("uniqueVisitors", visits.getUniqueVisitors());
        return object;
    }


    /**
     * Update months history.
     * 
     * @param nuxeoSession Nuxeo session
     * @param docRef document reference
     * @param statistics space statistics
     * @throws Exception
     */
    private void updateMonthsHistory(Session nuxeoSession, DocRef docRef, SpaceStatistics statistics) throws Exception {
        // Previous month
        Date previousMonth = DateUtils.truncate(statistics.getPreviousUpdate(), Calendar.MONTH);

        // Historized months
        Map<Date, SpaceVisits> historizedMonths = statistics.getHistorizedMonthsVisits();
        // Previous historized month
        SpaceVisits historizedMonth = historizedMonths.get(previousMonth);

        if (historizedMonth != null) {
            String date = StatisticsCmsServiceDelegation.MONTH_FORMAT.format(previousMonth);
            this.addToHistory(nuxeoSession, docRef, "stats:months", date, historizedMonth);
        }
    }


    /**
     * Add to history.
     * 
     * @param nuxeoSession Nuxeo session
     * @param docRef document reference
     * @param xpath document xpath
     * @param date date
     * @param visits visits
     * @throws Exception
     */
    private void addToHistory(Session nuxeoSession, DocRef docRef, String xpath, String date, SpaceVisits visits) throws Exception {
        // Value
        PropertyMap value = new PropertyMap();
        value.set("date", date);
        value.set("hits", Long.valueOf(visits.getHits()));
        value.set("uniqueVisitors", Long.valueOf(visits.getUniqueVisitors()));

        // Operation request
        OperationRequest request = nuxeoSession.newRequest("Document.AddComplexProperty");
        request.setInput(docRef);
        request.set("xpath", xpath);
        request.set("value", value);

        request.execute();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getSimpleName());
        builder.append("|");
        builder.append(this.sessionId);
        return builder.toString();
    }

}
