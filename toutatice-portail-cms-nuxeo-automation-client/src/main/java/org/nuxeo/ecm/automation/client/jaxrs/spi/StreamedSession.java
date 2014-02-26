/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ecm.automation.client.jaxrs.spi;

import static org.nuxeo.ecm.automation.client.Constants.CTYPE_REQUEST_NOCHARSET;
import static org.nuxeo.ecm.automation.client.Constants.REQUEST_ACCEPT_HEADER;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.client.AsyncCallback;
import org.nuxeo.ecm.automation.client.AutomationClient;
import org.nuxeo.ecm.automation.client.LoginInfo;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.util.MultipartInput;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Blobs;
import org.nuxeo.ecm.automation.client.model.OperationDocumentation;
import org.nuxeo.ecm.automation.client.model.OperationInput;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class StreamedSession extends DefaultSession {

     public StreamedSession(AbstractAutomationClient client, Connector connector,
            LoginInfo login) {
        
        super(client, connector,login);
    }

    
    public Blob getStreamedFile(String path) throws Exception {
        Request req = new StreamedRequest(Request.GET, client.getBaseUrl() + path);
        return (Blob) connector.execute(req);
    }

    @Override
    public OperationRequest newRequest(String id, Map<String, Object> ctx)
            throws Exception {
        OperationDocumentation op = getOperation(id);
        if (op == null) {
            throw new IllegalArgumentException("No such operation: " + id);
        }
        return new DefaultOperationRequest(this, op, ctx);
    }
   
}
