package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshaller;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshalling;
import org.nuxeo.ecm.automation.client.model.Documents;

public class EsMarshaller implements JsonMarshaller<Documents> {

	@Override
	public String getType() {
		return "esresponse";
	}

	@Override
	public Class<Documents> getJavaType() {
        return Documents.class;
	}
	
	@Override
	public Documents read(JsonParser jp) throws Exception {
        jp.nextToken();
        String key = jp.getCurrentName();
        if ("value".equals(key)) {
            jp.nextToken(); // '{'
            jp.nextToken(); // hopefully "entity-type"
            jp.nextToken(); // its value            
            String etype = jp.getText();
            JsonMarshaller<?> jm = JsonMarshalling.getMarshaller(etype);
            if (null != jm) {
                return (Documents) jm.read(jp);            	
            }
        } else {
        	throw new Exception("missing 'value' filed");
        }
		
		return null;
	}

    @Override
    public void write(JsonGenerator jg, Documents value) throws Exception {
        // Nothing to do.
    }

}
