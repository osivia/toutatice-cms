package org.nuxeo.ecm.automation.client.jaxrs.spi;

import java.io.InputStream;

import org.nuxeo.ecm.automation.client.model.StreamBlob;


/**
 * Requete de streaming
 * 
 * @author Jean-Sébastien Steux
 *
 */
public class StreamedRequest extends Request {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StreamedRequest(int method, String url) {
        super(method, url);

    }

    public Object handleResult(int status, String ctype, String disp,
            InputStream stream) throws Exception {
        if (status == 204) { // no content
            return null;
        } else if (status >= 400) {
            handleException(status, ctype, stream);
        }
        return new StreamBlob(stream, getFileName(disp), ctype);
    }
    
}
