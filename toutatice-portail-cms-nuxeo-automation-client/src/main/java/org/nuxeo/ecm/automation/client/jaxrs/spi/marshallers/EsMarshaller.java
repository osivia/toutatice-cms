package org.nuxeo.ecm.automation.client.jaxrs.spi.marshallers;


import java.io.IOException;

import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshaller;
import org.nuxeo.ecm.automation.client.jaxrs.spi.JsonMarshalling;
import org.nuxeo.ecm.automation.client.model.Documents;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

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
	public Documents read(JsonParser jp) throws IOException {
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
        	throw new IOException("missing 'value' filed");
        }
		
		return null;
	}

	@Override
	public void write(JsonGenerator jg, Object value) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
