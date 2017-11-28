package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshaller;
import net.sf.json.JSONObject;

public class RawEsMarshaller implements JsonMarshaller<JSONObject> {

	@Override
	public String getType() {
		return "rawesresponse";
	}

	@Override
	public Class<JSONObject> getJavaType() {
		return JSONObject.class;
	}

	@Override
	public JSONObject read(JsonParser jp) throws Exception {
		jp.nextToken();
		final String key = jp.getCurrentName();
		if ("value".equals(key)) {
			jp.nextToken();
			final JsonNode node = jp.readValueAsTree();
			final String rawEsVal = node.toString();

			return JSONObject.fromObject(rawEsVal);
		} else {
			throw new IllegalArgumentException("missing 'value' filed");
		}
	}

	@Override
	public void write(JsonGenerator jg, JSONObject value) throws Exception {
		// Nothing to do.
	}

}
