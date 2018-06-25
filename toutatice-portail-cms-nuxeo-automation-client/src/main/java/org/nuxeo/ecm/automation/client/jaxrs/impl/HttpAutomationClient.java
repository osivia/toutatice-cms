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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
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

    /** Shared registry. */
    private static final Map<String, OperationRegistry> SHARED_REGISTRY = new ConcurrentHashMap<>();
    /** Shared registry cache timestamps. */
    private static final Map<String, Long> SHARED_REGISTRY_TIMESTAMPS = new ConcurrentHashMap<>();
    /** Shared registry expiration delay. */
    private static final long SHARED_REGISTRY_EXPIRATION_DELAY = 60000L;


    /** Shared registry synchronizer. */
    public static Object sharedRegistrySynchronizer = new Object();


    /** Default HTTP client. */
    private DefaultHttpClient http;
    /** Satellite name. */
    private String satelliteName;


    /**
     * Constructor.
     *
     * @param url URL
     * @param satelliteName satellite name
     */
    public HttpAutomationClient(String url, String satelliteName) {
        super(url);
        this.http = new DefaultHttpClient();
        this.satelliteName = StringUtils.trimToEmpty(satelliteName);
        // http.setCookieSpecs(null);
        // http.setCookieStore(null);
        this.registerAdapter(new DocumentServiceFactory());
        this.registerAdapter(new DocumentSecurityServiceFactory());
    }


    public void setProxy(String host, int port) {
        // httpclient.getCredentialsProvider().setCredentials(
        // new AuthScope(PROXY, PROXY_PORT),
        // new UsernamePasswordCredentials("username", "password"));

        this.http.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(host, port));
    }


    public HttpClient http() {
        return this.http;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Session getSession() {
        Connector connector = this.newConnector();
        if (this.requestInterceptor != null) {
            connector = new ConnectorHandler(connector, this.requestInterceptor);
        }
        if (this.registry == null) {
            // Cache timestamp
            Long cacheTimestamp = SHARED_REGISTRY_TIMESTAMPS.get(this.satelliteName);
            if (cacheTimestamp == null) {
                cacheTimestamp = 0L;
            }
            // Current timestamp
            long currentTimestamp = System.currentTimeMillis();

            if ((currentTimestamp - cacheTimestamp) < SHARED_REGISTRY_EXPIRATION_DELAY) {
                this.registry = SHARED_REGISTRY.get(this.satelliteName);
            } else {
                synchronized (sharedRegistrySynchronizer) {
                    // Duplicate the test to avoid reentrance
                    if ((currentTimestamp - cacheTimestamp) < SHARED_REGISTRY_EXPIRATION_DELAY) {
                        this.registry = SHARED_REGISTRY.get(this.satelliteName);
                    } else {
                        // Retrieve the registry
                        this.registry = this.connect(connector);
                        SHARED_REGISTRY.put(this.satelliteName, this.registry);
                        SHARED_REGISTRY_TIMESTAMPS.put(this.satelliteName, currentTimestamp);
                    }
                }
            }
        }
        return this.login(connector);
    }


    @Override
    protected Session createSession(final Connector connector, final LoginInfo login) {
        return new StreamedSession(this, connector, login == null ? LoginInfo.ANONYNMOUS : login);
    }


    @Override
    public synchronized void shutdown() {
        super.shutdown();
        if (this.http != null) {
            if (this.http.getConnectionManager() != null) {
                this.http.getConnectionManager().shutdown();
            }
        }
        this.http = null;
    }


    @Override
    protected Connector newConnector() {
        return new HttpConnector(this.http);
    }

}
