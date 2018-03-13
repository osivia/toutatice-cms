package fr.toutatice.portail.cms.nuxeo.portlets.statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentService;
import org.nuxeo.ecm.automation.client.model.DocRef;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PropertyMap;
import org.osivia.portal.api.statistics.SpaceStatistics;
import org.osivia.portal.api.statistics.SpaceVisits;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Update space statistics Nuxeo command test class.
 * 
 * @author Cédric Krommenhoek
 * @see UpdateSpaceStatisticsCommand
 */
public class UpdateSpaceStatisticsCommandTest {

    /** Document path. */
    private static final String PATH = "/path/under/test";


    /** Object under test. */
    private UpdateSpaceStatisticsCommand command;
    /** Nuxeo session. */
    private Session nuxeoSession;

    /** Space statistics. */
    private SpaceStatistics spaceStatistics;
    /** Captured properties. */
    private Capture<PropertyMap> capturedProperties;
    /** Captured added properties. */
    private Capture<Object> capturedAddedProperties;


    /** Session identifier. */
    private final String sessionId;


    /**
     * Constructor.
     */
    public UpdateSpaceStatisticsCommandTest() {
        super();
        this.sessionId = UUID.randomUUID().toString();
    }


    /**
     * Set-up.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Captured properties
        this.capturedProperties = new Capture<>(CaptureType.ALL);
        // Captured added month
        this.capturedAddedProperties = new Capture<>(CaptureType.ALL);

        // Document
        Document document = EasyMock.createNiceMock(Document.class);
        // Document service
        DocumentService documentService = EasyMock.createMock(DocumentService.class);
        EasyMock.expect(documentService.update(EasyMock.anyObject(DocRef.class), EasyMock.capture(this.capturedProperties))).andStubReturn(document);
        // Operation request
        OperationRequest operationRequest = EasyMock.createMock(OperationRequest.class);
        EasyMock.expect(operationRequest.setInput(EasyMock.anyObject(DocRef.class))).andStubReturn(operationRequest);
        EasyMock.expect(operationRequest.set(EasyMock.anyObject(String.class), EasyMock.capture(this.capturedAddedProperties))).andStubReturn(operationRequest);
        EasyMock.expect(operationRequest.execute()).andStubReturn(document);

        // Nuxeo session
        this.nuxeoSession = EasyMock.createMock(Session.class);
        EasyMock.expect(this.nuxeoSession.getAdapter(DocumentService.class)).andStubReturn(documentService);
        EasyMock.expect(this.nuxeoSession.newRequest("Document.AddComplexProperty")).andStubReturn(operationRequest);

        // Replay all
        EasyMock.replay(document, documentService, operationRequest, this.nuxeoSession);

        
        // Space statistics
        this.spaceStatistics = new SpaceStatistics(PATH);

        // Statistics
        List<SpaceStatistics> statistics = new ArrayList<>();
        statistics.add(this.spaceStatistics);

        // Nuxeo command
        this.command = new UpdateSpaceStatisticsCommand(this.sessionId, statistics);
    }


    @Test
    public final void testExecute1() throws Exception {
        // Blank execution
        this.command.execute(this.nuxeoSession);
    }


    @Test
    public final void testExecute2() throws Exception {
        // Deprecated data, without purge

        // Last date
        Date lastUpdate = new Date();
        this.spaceStatistics.setLastUpdate(lastUpdate);

        // Previous update, 3 days ago
        Date previousUpdate = DateUtils.addDays(lastUpdate, -3);
        this.spaceStatistics.setPreviousUpdate(previousUpdate);

        // Current day visits
        SpaceVisits currentDayVisits = new SpaceVisits();
        currentDayVisits.setAnonymousVisitors(10);
        currentDayVisits.setHits(50);
        currentDayVisits.getVisitors().addAll(Arrays.asList(new String[]{"Un", "Deux", "Trois"}));
        this.spaceStatistics.setCurrentDayVisits(currentDayVisits);

        // Historized days
        Map<Date, SpaceVisits> historizedDays = this.spaceStatistics.getHistorizedDaysVisits();
        SpaceVisits visits;

        // 20 days ago
        visits = new SpaceVisits();
        visits.setHits(75);
        visits.setUniqueVisitors(15);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -20), visits);

        // 10 days ago
        visits = new SpaceVisits();
        visits.setHits(125);
        visits.setUniqueVisitors(25);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -10), visits);

        // 3 days ago
        visits = new SpaceVisits();
        visits.setHits(15);
        visits.setUniqueVisitors(2);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -3), visits);


        this.command.execute(this.nuxeoSession);


        // Captured properties
        List<PropertyMap> values = this.capturedProperties.getValues();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());

        // Current properties
        PropertyMap properties = values.get(0);
        Assert.assertEquals(3, properties.size());

        // Last update
        Assert.assertTrue(DateUtils.truncatedEquals(lastUpdate, properties.getDate("stats:lastUpdate"), Calendar.SECOND));

        // Current day visits
        JSONObject day = JSONObject.fromObject(properties.get("stats:dayVisits"));
        Assert.assertNotNull(day);
        Assert.assertFalse(day.isEmpty());
        // Current day hits
        Long hits = day.getLong("hits");
        Assert.assertNotNull(hits);
        Assert.assertEquals(50, hits.longValue());
        // Current day anonymous visits
        Long anonymous = day.getLong("anonymous");
        Assert.assertNotNull(anonymous);
        Assert.assertEquals(10, anonymous.longValue());
        // Current day identified visits
        JSONArray identifiers = day.getJSONArray("identifiers");
        Assert.assertNotNull(identifiers);
        Assert.assertEquals(3, identifiers.size());

        // Current month visits
        JSONObject month = JSONObject.fromObject(properties.get("stats:monthVisits"));
        Assert.assertNotNull(month);
        Assert.assertTrue(month.isEmpty());


        // Added properties
        List<Object> capturedAddedValues = this.capturedAddedProperties.getValues();
        Assert.assertNotNull(capturedAddedValues);
        Assert.assertEquals(2, capturedAddedValues.size());
        Assert.assertEquals("stats:days", capturedAddedValues.get(0));
        
        // Added day
        Object addedDayValue = capturedAddedValues.get(1);
        Assert.assertTrue(addedDayValue instanceof PropertyMap);
        PropertyMap addedDay = (PropertyMap) addedDayValue;
        Assert.assertEquals(3, addedDay.size());
        Assert.assertEquals(new SimpleDateFormat("dd-MM-yyyy").format(previousUpdate), addedDay.getString("date"));
        Assert.assertEquals(15, addedDay.getLong("hits").longValue());
        Assert.assertEquals(2, addedDay.getLong("uniqueVisitors").longValue());
    }


    @Test
    public final void testExecute3() throws Exception {
        // Deprecated data, with purge

        // Last date
        Date lastUpdate = new Date();
        this.spaceStatistics.setLastUpdate(lastUpdate);

        // Previous update, 3 days ago
        Date previousUpdate = DateUtils.addDays(lastUpdate, -3);
        this.spaceStatistics.setPreviousUpdate(previousUpdate);

        // Current day visits
        SpaceVisits currentDayVisits = new SpaceVisits();
        currentDayVisits.setAnonymousVisitors(10);
        currentDayVisits.setHits(50);
        currentDayVisits.getVisitors().addAll(Arrays.asList(new String[]{"Un", "Deux", "Trois"}));
        this.spaceStatistics.setCurrentDayVisits(currentDayVisits);

        // Historized days
        Map<Date, SpaceVisits> historizedDays = this.spaceStatistics.getHistorizedDaysVisits();
        SpaceVisits visits;

        // 50 days ago
        visits = new SpaceVisits();
        visits.setHits(75);
        visits.setUniqueVisitors(15);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -50), visits);

        // 10 days ago
        visits = new SpaceVisits();
        visits.setHits(125);
        visits.setUniqueVisitors(25);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -10), visits);

        // 3 days ago
        visits = new SpaceVisits();
        visits.setHits(15);
        visits.setUniqueVisitors(2);
        historizedDays.put(DateUtils.addDays(DateUtils.truncate(lastUpdate, Calendar.DAY_OF_MONTH), -3), visits);


        this.command.execute(this.nuxeoSession);


        // Captured properties
        List<PropertyMap> capturedPropertiesValues = this.capturedProperties.getValues();
        Assert.assertNotNull(capturedPropertiesValues);
        Assert.assertEquals(2, capturedPropertiesValues.size());


        // Current properties
        PropertyMap currentProperties = capturedPropertiesValues.get(0);
        Assert.assertEquals(3, currentProperties.size());

        // Last update
        Assert.assertTrue(DateUtils.truncatedEquals(lastUpdate, currentProperties.getDate("stats:lastUpdate"), Calendar.SECOND));

        // Current day visits
        JSONObject currentDay = JSONObject.fromObject(currentProperties.get("stats:dayVisits"));
        Assert.assertNotNull(currentDay);
        Assert.assertFalse(currentDay.isEmpty());
        // Current day hits
        Long hits = currentDay.getLong("hits");
        Assert.assertNotNull(hits);
        Assert.assertEquals(50, hits.longValue());
        // Current day anonymous visits
        Long anonymous = currentDay.getLong("anonymous");
        Assert.assertNotNull(anonymous);
        Assert.assertEquals(10, anonymous.longValue());
        // Current day identified visits
        JSONArray identifiers = currentDay.getJSONArray("identifiers");
        Assert.assertNotNull(identifiers);
        Assert.assertEquals(3, identifiers.size());

        // Current month visits
        JSONObject month = JSONObject.fromObject(currentProperties.get("stats:monthVisits"));
        Assert.assertNotNull(month);
        Assert.assertTrue(month.isEmpty());


        // Day history properties
        PropertyMap dayHistoryProperties = capturedPropertiesValues.get(1);
        Assert.assertEquals(1, dayHistoryProperties.size());

        // Historized days
        JSONArray days = JSONArray.fromObject(dayHistoryProperties.getString("stats:days"));
        Assert.assertEquals(2, days.size());

        // Historized day
        for (int i = 0; i < 2; i++) {
            JSONObject day = days.getJSONObject(i);
            Assert.assertEquals(3, day.size());
            
            Date date = new SimpleDateFormat("dd-MM-yyyy").parse(day.getString("date"));
            if (DateUtils.truncatedEquals(date, DateUtils.addDays(lastUpdate, -10), Calendar.DAY_OF_MONTH)) {
                // 10 days ago
                Assert.assertEquals(125, day.getInt("hits"));
                Assert.assertEquals(25, day.getInt("uniqueVisitors"));
            } else if (DateUtils.truncatedEquals(date, DateUtils.addDays(lastUpdate, -3), Calendar.DAY_OF_MONTH)) {
                // 3 days ago
                Assert.assertEquals(15, day.getInt("hits"));
                Assert.assertEquals(2, day.getInt("uniqueVisitors"));
            } else {
                Assert.fail("Unknown date");
            }
        }

        // Captured added properties
        List<Object> capturedAddedValues = this.capturedAddedProperties.getValues();
        Assert.assertNotNull(capturedPropertiesValues);
        Assert.assertTrue(capturedAddedValues.isEmpty());
    }


    @Test
    public final void testExecute4() throws Exception {
        // Up to date data

        // Last date
        Date lastUpdate = new Date();
        this.spaceStatistics.setLastUpdate(lastUpdate);

        // Previous update, today
        Date previousUpdate = DateUtils.addMinutes(lastUpdate, -1);
        this.spaceStatistics.setPreviousUpdate(previousUpdate);

        // Current day visits
        SpaceVisits currentDayVisits = new SpaceVisits();
        currentDayVisits.setHits(50);
        currentDayVisits.setAnonymousVisitors(10);
        currentDayVisits.getVisitors().addAll(Arrays.asList(new String[]{"Un", "Deux", "Trois"}));
        this.spaceStatistics.setCurrentDayVisits(currentDayVisits);


        this.command.execute(this.nuxeoSession);


        // Captured properties
        List<PropertyMap> values = this.capturedProperties.getValues();
        Assert.assertNotNull(values);
        Assert.assertEquals(1, values.size());

        PropertyMap properties = values.get(0);
        Assert.assertEquals(3, properties.size());

        // Last update
        Assert.assertTrue(DateUtils.truncatedEquals(lastUpdate, properties.getDate("stats:lastUpdate"), Calendar.SECOND));

        // Current day visits
        JSONObject day = JSONObject.fromObject(properties.get("stats:dayVisits"));
        Assert.assertNotNull(day);
        Assert.assertFalse(day.isEmpty());
        // Current day hits
        Long hits = day.getLong("hits");
        Assert.assertNotNull(hits);
        Assert.assertEquals(50, hits.longValue());
        // Current day anonymous visits
        Long anonymous = day.getLong("anonymous");
        Assert.assertNotNull(anonymous);
        Assert.assertEquals(10, anonymous.longValue());
        // Current day identified visits
        JSONArray identifiers = day.getJSONArray("identifiers");
        Assert.assertNotNull(identifiers);
        Assert.assertEquals(3, identifiers.size());

        // Current month visits
        JSONObject month = JSONObject.fromObject(properties.get("stats:monthVisits"));
        Assert.assertNotNull(month);
        Assert.assertTrue(month.isEmpty());
    }

}
