/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package fr.toutatice.portail.cms.nuxeo.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.path.IBrowserService;
import org.osivia.portal.api.portlet.PortalGenericPortlet;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.cms.ICMSServiceLocator;

import fr.toutatice.portail.cms.nuxeo.api.services.INuxeoService;


/**
 * Superclass for CMS Portlet.
 * 
 * @see PortalGenericPortlet
 */
public abstract class CMSPortlet extends PortalGenericPortlet {

    /** Logger. */
    protected final Log logger;

    /** The nuxeo navigation service. */
    private final INuxeoService nuxeoService;
    /** CMS service locator. */
    private final ICMSServiceLocator cmsServiceLocator;
    /** Documents browser service. */
    private final IBrowserService browserService;


    /**
     * Constructor.
     */
    public CMSPortlet() {
        super();
        this.logger = LogFactory.getLog(CMSPortlet.class);
        this.cmsServiceLocator = Locator.findMBean(ICMSServiceLocator.class, ICMSServiceLocator.MBEAN_NAME);
        this.nuxeoService = Locator.findMBean(INuxeoService.class, INuxeoService.MBEAN_NAME);
        this.browserService = Locator.findMBean(IBrowserService.class, IBrowserService.MBEAN_NAME);
    }


    /**
     * Get CMS service.
     * 
     * @return CMS service
     */
    public ICMSService getCMSService() {
        return this.cmsServiceLocator.getCMSService();
    }


    /**
     * Get Nuxeo service.
     * 
     * @return Nuxeo service
     */
    public INuxeoService getNuxeoService() {
        return this.nuxeoService;
    }


    /**
     * Gets the nuxeo navigation service.
     *
     * @return the nuxeo navigation service
     * @deprecated see getNuxeoService
     * @throws Exception
     *             the exception
     */
    @Deprecated
    public INuxeoService getNuxeoNavigationService() throws Exception {
        return this.nuxeoService;
    }


    /**
     * Performs nuxeo service initialization.
     *
     * @param config the config
     * @throws PortletException the portlet exception
     * @see javax.portlet.GenericPortlet#init(javax.portlet.PortletConfig)
     */
    @Override
    public void init(PortletConfig config) throws PortletException {
        super.init(config);

        try {
            new NuxeoController(this.getPortletContext()).startNuxeoService();
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    /**
     * Performs nuxeo service .
     *
     * @see javax.portlet.GenericPortlet#destroy()
     */
    @Override
    public void destroy() {
        try {
            // Destruction des threads éventuels
            new NuxeoController(this.getPortletContext()).stopNuxeoService();
        } catch (Exception e) {
            logger.error(e);
        }

        super.destroy();
    }


    /**
     * Format resource last modified.
     *
     * @return the string
     */
    public String formatResourceLastModified() {
        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }


    /**
     * Checks if resource has expired.
     *
     * @param sOriginalDate the original date
     * @param resourceResponse the resource response
     * @return true, if is resource expired
     */
    public boolean isResourceExpired(String sOriginalDate, ResourceResponse resourceResponse, String refreshMs) {
        boolean isExpired = true;

        if (sOriginalDate != null) {
            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, yyyy-MM-dd'T'HH:mm:ss.SS'Z'", Locale.ENGLISH);

            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < (originalDate.getTime() + (resourceResponse.getCacheControl().getExpirationTime() * 1000))) {
                    if ((refreshMs == null) || (Long.parseLong(refreshMs) < originalDate.getTime())) {
                        isExpired = false;
                    }
                }
            } catch (Exception e) {
                // Do nothing
            }
        }

        return isExpired;
    }


    /**
     * Serve resource by cache.
     *
     * @param resourceRequest the resource request
     * @param resourceResponse the resource response
     * @return true, if successful
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public boolean serveResourceByCache(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
        String sOriginalDate = resourceRequest.getProperty("if-modified-since");
        if (sOriginalDate == null) {
            sOriginalDate = resourceRequest.getProperty("If-Modified-Since");
        }

        if (!this.isResourceExpired(sOriginalDate, resourceResponse, resourceRequest.getParameter("refresh"))) {
            // validation

            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_NOT_MODIFIED));
            resourceResponse.setProperty("Last-Modified", sOriginalDate);

            resourceResponse.getPortletOutputStream().close();

            return true;
        }

        return false;
    }


    /**
     * Serve ressource exception.
     *
     * @param resourceRequest resource request
     * @param resourceResponse resource response
     * @param e Nuxeo exception
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void serveResourceException(ResourceRequest resourceRequest, ResourceResponse resourceResponse, NuxeoException e) throws PortletException,
            IOException {
        int httpErrorCode = 0;
        if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
            httpErrorCode = HttpServletResponse.SC_NOT_FOUND;
            String message = "Resource CMSPortlet " + resourceRequest.getParameterMap() + " not found (error 404).";
            logger.error(message);
        } else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {
            httpErrorCode = HttpServletResponse.SC_FORBIDDEN;
        }

        if (httpErrorCode != 0) {
            NuxeoController nuxeoController = this.createNuxeoController(resourceRequest, resourceResponse);
            PortalControllerContext portalCtx = new PortalControllerContext(this.getPortletContext(), resourceRequest, resourceResponse);

            String errorUrl = nuxeoController.getPortalUrlFactory().getHttpErrorUrl(portalCtx, httpErrorCode);

            resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
            resourceResponse.setProperty("Location", errorUrl);
            resourceResponse.getPortletOutputStream().close();
        } else {
            throw e;
        }
    }


    /**
     * Serve CMS Resource.
     *
     * IMPORTANT !!!
     *
     * For web page mode, live mode MUST BE computed by the portlet when generating resource URL (displayLiveVersion=1)
     *
     * @param resourceRequest resource request
     * @param resourceResponse resource response
     * @throws PortletException the portlet exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
        try {
            if (this.serveResourceByCache(resourceRequest, resourceResponse)) {
                return;
            }

            // Redirection
            if ("link".equals(resourceRequest.getParameter("type"))) {
                NuxeoController nuxeoController = new NuxeoController(resourceRequest, null, this.getPortletContext());

                // Document identifier
                String id = resourceRequest.getResourceID();

                // Fetch document
                Document document = nuxeoController.fetchDocument(id);

                // Response
                resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
                resourceResponse.setProperty("Location", document.getString("clink:link"));
                resourceResponse.getPortletOutputStream().close();
            } else if ("fancytreeLazyLoading".equals(resourceRequest.getResourceID())) {
                // Fancytree lazy-loading
                serveResourceFancytreeLazyLoading(resourceRequest, resourceResponse);
            } else {
                // Tous les autres cas sont dépréciés
                resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, String.valueOf(HttpServletResponse.SC_NOT_FOUND));
            }
        } catch (NuxeoException e) {
            this.serveResourceException(resourceRequest, resourceResponse, e);
        } catch (PortletException e) {
            throw e;
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }


    protected void serveResourceFancytreeLazyLoading(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(getPortletContext(), request, response);

        try {
            String data = this.browserService.browse(portalControllerContext);

            // Content type
            response.setContentType("application/json");

            // Content
            PrintWriter printWriter = new PrintWriter(response.getPortletOutputStream());
            printWriter.write(data);
            printWriter.close();
        } catch (PortalException e) {
            throw new PortletException(e);
        }
    }


    /**
     * Creates Nuxeo controller.
     *
     * @param portletRequest portlet request
     * @param portletResponse portlet response
     * @return Nuxeo controller
     */
    protected NuxeoController createNuxeoController(PortletRequest portletRequest, PortletResponse portletResponse) {
        return new NuxeoController(portletRequest, portletResponse, this.getPortletContext());
    }

}
