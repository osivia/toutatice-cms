package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.InputStream;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nuxeo.ecm.automation.client.Constants;
import net.sf.json.JSONObject;

public class RawEsMapperTest {

	private static JsonFactory factory;

	@BeforeClass
	public static void setUp() {
		final ObjectMapper mapper = new ObjectMapper();
		factory = mapper.getJsonFactory();
	}

	@Test
	public void testRead() throws Exception {
		final RawEsMarshaller marshaller = new RawEsMarshaller();
		final InputStream input = this.getClass().getClassLoader().getResourceAsStream("esResponse.json");
		final JsonParser jp = factory.createJsonParser(input);
		jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
		jp.nextToken();
		if (!Constants.KEY_ENTITY_TYPE.equals(jp.getText())) {
			fail("unuspported respone type. No entity-type key found at top of the object");
		}
		jp.nextToken(); // etype
		final String etype = jp.getText();
		if (!marshaller.getType().equals(etype)) {
			fail("Invalid entity-type for this marshaller");
		}

		final JSONObject result = marshaller.read(jp);

		assertNotNull(result);
		assertNotNull(result.getJSONObject("aggregations"));
		assertNotNull(result.getJSONObject("aggregations").getJSONObject("tags"));
		assertEquals(8, CollectionUtils.size(result.getJSONObject("aggregations").getJSONObject("tags").getJSONArray("buckets")));
		assertEquals("blabla", result.getJSONObject("aggregations").getJSONObject("tags").getJSONArray("buckets").getJSONObject(0).getString("key"));
		assertEquals(12L, result.getJSONObject("aggregations").getJSONObject("tags").getJSONArray("buckets").getJSONObject(0).getLong("doc_count"));
		assertEquals("bart", result.getJSONObject("aggregations").getJSONObject("tags").getJSONArray("buckets").getJSONObject(1).getString("key"));
		assertEquals(3L, result.getJSONObject("aggregations").getJSONObject("tags").getJSONArray("buckets").getJSONObject(1).getLong("doc_count"));

	}

}
