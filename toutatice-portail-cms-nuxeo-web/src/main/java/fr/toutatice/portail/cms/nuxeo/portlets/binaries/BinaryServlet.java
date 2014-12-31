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
package fr.toutatice.portail.cms.nuxeo.portlets.binaries;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * Portlet unifié de gestion de ressources :
 * 
 * suppose que les droits ait élé calculés en amont, avec un délégation
 * 
 * 
 * @author Jean-Sébastien Steux
 * 
 */
public class BinaryServlet extends HttpServlet

{

    private static final long serialVersionUID = -8877737456357488020L;


    private static PortletContext portletCtx;


    public void checkDelegation(String path) {

    }


    public static void setPortletContext(PortletContext documentPortletCtx) {
        portletCtx = documentPortletCtx;
    }

    private static long PICTURE_TIMEOUT = 600;

    public String formatResourceLastModified() {

        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }

    public boolean isResourceExpired(String sOriginalDate) {

        boolean isExpired = true;

        if (sOriginalDate != null) {

            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < originalDate.getTime() + PICTURE_TIMEOUT * 1000)
                    isExpired = false;
            } catch (Exception e) {

            }
        }

        return isExpired;
    }

    public boolean serveResourceByCache(HttpServletRequest resourceRequest, HttpServletResponse resourceResponse) throws PortletException, IOException {

        String sOriginalDate = resourceRequest.getHeader("if-modified-since");
        if (sOriginalDate == null)
            sOriginalDate = resourceRequest.getHeader("If-Modified-Since");

        if (!isResourceExpired(sOriginalDate)) { // validation
                                                 // request

            // resourceResponse.setContentLength(0);
            resourceResponse.sendError(HttpServletResponse.SC_NOT_MODIFIED);

            resourceResponse.setHeader("Last-Modified", sOriginalDate);

            return true;
        }

        return false;
    }

    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException, ServletException {

        OutputStream output = theResponse.getOutputStream();
        try {

            if (serveResourceByCache(theRequest, theResponse))
                return;


            String docPath = theRequest.getParameter("path");
            docPath = URLDecoder.decode(docPath, "UTF-8");

            String id = theRequest.getSession().getId();
            boolean delegation = NuxeoController.delegations.contains(id + docPath);


            String index = theRequest.getParameter("index");

            NuxeoController ctx = new NuxeoController(portletCtx);

            ctx.setServletRequest(theRequest);
            ctx.setScope("anonymous");


            if (delegation) {
                ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
                ctx.setForcePublicationInfosScope("superuser_context");
            } else
                ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);

            ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

            CMSBinaryContent content = ctx.fetchAttachedPicture(docPath, index);


            // Les headers doivent être positionnées avant la réponse
            theResponse.setContentType(content.getMimeType());

            theResponse.setHeader("Cache-Control", "max-age=" + PICTURE_TIMEOUT);

            theResponse.setHeader("Last-Modified", formatResourceLastModified());

            ResourceUtil.copy(new FileInputStream(content.getFile()), theResponse.getOutputStream(), 4096);

        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            output.close();
        }

    }

}
