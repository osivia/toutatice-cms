/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * bstefanescu
 */
package org.nuxeo.ecm.automation.client.jaxrs.impl;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.nuxeo.ecm.automation.client.LoginInfo;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.adapters.DocumentSecurityServiceFactory;
import org.nuxeo.ecm.automation.client.adapters.DocumentServiceFactory;
import org.nuxeo.ecm.automation.client.jaxrs.spi.AbstractAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.Connector;
import org.nuxeo.ecm.automation.client.jaxrs.spi.ConnectorHandler;
import org.nuxeo.ecm.automation.client.jaxrs.spi.StreamedSession;
import org.nuxeo.ecm.automation.client.model.OperationRegistry;

/**
 * TOUTATICE update
 * 
 * This class directly extends AbstractAutomationClient instead of
 * AsyncAutomationClient (performance issue during shutdown ( delay of 2 s. with
 * java synchronization)
 * 
 * redefines getSession() in order to share the registry between
 * clients (perf. issues)
 * 
 * introduces a StreamedSession for large files
 * 
 * 
 * @author jssteux@cap2j.org
 */
public class HttpAutomationClient extends AbstractAutomationClient {

    protected DefaultHttpClient http;


    public static OperationRegistry sharedRegistry = null;
    public static Object sharedRegistrySynchronizer = new Object();
    public static long sharedRegistryUpdateTimestamp = 0L;
    private static long SHARED_REGISTRY_EXPIRATION_DELAY = 60000L;

    public HttpAutomationClient(String url) {
        super(url);
        http = new DefaultHttpClient();
        // http.setCookieSpecs(null);
        // http.setCookieStore(null);
        registerAdapter(new DocumentServiceFactory());
        registerAdapter(new DocumentSecurityServiceFactory());
    }

    public void setProxy(String host, int port) {
        // httpclient.getCredentialsProvider().setCredentials(
        // new AuthScope(PROXY, PROXY_PORT),
        // new UsernamePasswordCredentials("username", "password"));

        http.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(host, port));
    }

    public HttpClient http() {
        return http;
    }

    @Override
    public Session getSession() {
        Connector connector = newConnector();
        if (requestInterceptor != null) {
            connector = new ConnectorHandler(connector, requestInterceptor);
        }
        if (registry == null) {
            if (System.currentTimeMillis() - sharedRegistryUpdateTimestamp < SHARED_REGISTRY_EXPIRATION_DELAY) {
                registry = sharedRegistry;
            } else {
                synchronized (sharedRegistrySynchronizer) {
                    // Duplicate the test to avoid reentrance
                    if (System.currentTimeMillis() - sharedRegistryUpdateTimestamp < SHARED_REGISTRY_EXPIRATION_DELAY) {
                        registry = sharedRegistry;
                    } else {
                        // Retrieve the registry
                        registry = connect(connector);
                        sharedRegistry = registry;
                        sharedRegistryUpdateTimestamp = System.currentTimeMillis();
                    }
                }
            }
        }
        return login(connector);
    }

    @Override
    protected Session createSession(final Connector connector, final LoginInfo login) {
        return new StreamedSession(this, connector, login == null ? LoginInfo.ANONYNMOUS : login);
    }


    @Override
    public synchronized void shutdown() {
        super.shutdown();
        if (http != null) {
            if (http.getConnectionManager() != null)
                http.getConnectionManager().shutdown();
        }
        http = null;
    }

    @Override
    protected Connector newConnector() {
        return new HttpConnector(http);
    }
}
