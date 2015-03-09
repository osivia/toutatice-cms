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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.core.cms.BinaryDelegation;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.BinaryDescription.Type;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.page.PageProperties;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;



/**
 * Portlet unifié de gestion de ressources :
 * 
 * suppose que les droits ait élé calculés en amont. Un mécanisme de délégation permet d'éviter un nouveau contrôle de droits.
 *
 * @author Jean-Sébastien Steux
 */

public class BinaryServlet extends HttpServlet

{    /** The logger. */
    protected static Log logger = LogFactory.getLog(BinaryServlet.class);

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8877737456357488020L;


    /** The portlet ctx. */
    private static PortletContext portletCtx;

    /**
     * Stream big file.
     *
     * @param inputStream the input stream
     * @param outputStream the output stream
     * @param bufSize the buf size
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void streamBigFile(InputStream inputStream, OutputStream outputStream, int bufSize) throws IOException {


        try {
            byte[] b = new byte[bufSize];
            int bytesread = 0;
            int bytesBuffered = 0;
            while ((bytesread = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, bytesread);
                bytesBuffered += bytesread;
                if (bytesBuffered > 1024 * 1024) { // flush after 1MB
                    bytesBuffered = 0;
                }
            }
            outputStream.flush();
        } finally {
            inputStream.close();

        }
    }


    /**
     * Sets the portlet context.
     *
     * @param documentPortletCtx the new portlet context
     */
    public static void setPortletContext(PortletContext documentPortletCtx) {
        portletCtx = documentPortletCtx;
    }

    /** The binary timeout. */
    private static long BINARY_TIMEOUT = 600;

    /**
     * Format resource last modified.
     *
     * @return the string
     */
    public String formatResourceLastModified() {

        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Checks if is resource expired.
     *
     * @param sOriginalDate the s original date
     * @return true, if is resource expired
     */
    public boolean isResourceExpired(String sOriginalDate) {

        boolean isExpired = true;

        if (sOriginalDate != null) {
            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < originalDate.getTime() + BINARY_TIMEOUT * 1000)
                    isExpired = false;
            } catch (Exception e) {

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

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException, ServletException {
        
        
        // réinitialisation des propriétes des windows
        PageProperties.getProperties().init();
        
        String portalName = theRequest.getParameter("portalName");
        if( portalName == null)
            portalName = "default";
        PageProperties.getProperties().getPagePropertiesMap().put(Constants.PORTAL_NAME, portalName);
        

        OutputStream output = theResponse.getOutputStream();
        try {

            if (serveResourceByCache(theRequest, theResponse))
                return;


            String docPath = theRequest.getParameter("path");
            docPath = URLDecoder.decode(docPath, "UTF-8");

            NuxeoController ctx = new NuxeoController(portletCtx);

            ctx.setServletRequest(theRequest);

            BinaryDelegation delegation = ctx.getCMSService().validateBinaryDelegation(ctx.getCMSCtx(), docPath);


            String index = theRequest.getParameter("index");
            String type = theRequest.getParameter("type");
            String pictureContent = theRequest.getParameter("content");
            String fieldName = theRequest.getParameter("fieldName");
            String scope = theRequest.getParameter("scope");
            String forcedScope = theRequest.getParameter("forcedScope");


            String sLiveState = theRequest.getParameter("liveState");
            if (BooleanUtils.toBoolean(sLiveState))
                ctx.setDisplayLiveVersion("1");

            ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);

            if (delegation != null) {
                theRequest.setAttribute("osivia.delegation.userName", delegation.getUserName());

                if (delegation.isGrantedAccess()) {
                    ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
                    ctx.setForcePublicationInfosScope("superuser_context");
                }
                
                theRequest.setAttribute("osivia.isAdmin", delegation.isAdmin());       
                
                PageProperties.getProperties().setBinarySubject(delegation.getSubject());       
             }
            
            

            if( scope != null)  {
                ctx.setScope(scope);
            }
            
            if( forcedScope != null)  {
                ctx.setForcePublicationInfosScope(forcedScope);
            }
            

            ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

            CMSBinaryContent content = null;

            Type binaryType = BinaryDescription.Type.valueOf(type);

            if (BinaryDescription.Type.ATTACHED_PICTURE.equals(binaryType)) {
                content = ctx.fetchAttachedPicture(docPath, index);
            } else if (BinaryDescription.Type.PICTURE.equals(binaryType)) {
                content = ctx.fetchPicture(docPath, pictureContent);
            } else if (BinaryDescription.Type.ATTACHED_FILE.equals(binaryType)) {
                content = ResourceUtil.getCMSBinaryContent(ctx, docPath, index);
            } else if (BinaryDescription.Type.BLOB.equals(binaryType)) {
                content = ResourceUtil.getBlobHolderContent(ctx, docPath, index);
            } else if (BinaryDescription.Type.FILE.equals(binaryType)) {
                ctx.setStreamingSupport(true);

                content = ctx.fetchFileContent(docPath, fieldName);

                // Redirection vers portlet de streaming
                if (content.getStream() != null) {

                    theResponse.setContentType(content.getMimeType());
                    theResponse.setHeader("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");
                    theResponse.setBufferSize(8192);

                    streamBigFile(content.getStream(), output, 8192);
                    return;
                }

            }

            // Les headers doivent être positionnées avant la réponse
            theResponse.setContentType(content.getMimeType());
            theResponse.setHeader("Content-Disposition", "attachment; filename=\"" + content.getName() + "\"");

            theResponse.setHeader("Cache-Control", "max-age=" + BINARY_TIMEOUT);

            theResponse.setHeader("Last-Modified", formatResourceLastModified());


            ResourceUtil.copy(new FileInputStream(content.getFile()), theResponse.getOutputStream(), 4096);

        } catch (NuxeoException e) {
            if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
                String message = "Resource BinaryServlet " + theRequest.getParameterMap() + " not found (error 404).";
                logger.error(message);
                theResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                theRequest.setAttribute("osivia.no_redirection", "1");
            } else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {
                theResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                theRequest.setAttribute("osivia.no_redirection", "1");
            }

       } catch(Exception e) {
           throw new ServletException(e);
       }    
           finally {
       
            output.close();
        }

    }

}
