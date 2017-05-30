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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.portlet.PortletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.log.LoggerMessage;
import org.osivia.portal.core.cms.BinaryDelegation;
import org.osivia.portal.core.cms.BinaryDescription;
import org.osivia.portal.core.cms.BinaryDescription.Type;
import org.osivia.portal.core.cms.CMSBinaryContent;
import org.osivia.portal.core.cms.CMSServiceCtx;
import org.osivia.portal.core.cms.ICMSService;
import org.osivia.portal.core.error.IPortalLogger;
import org.osivia.portal.core.page.PageProperties;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoException;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;


/**
 * Portlet unifié de gestion de ressources.
 * Suppose que les droits ait élé calculés en amont.
 * Un mécanisme de délégation permet d'éviter un nouveau contrôle de droits.
 *
 * @author Jean-Sébastien Steux
 * @see HttpServlet
 */
public class BinaryServlet extends HttpServlet {

    /** Default serial version ID. */
    private static final long serialVersionUID = 1L;

    /** Default buffer size. */
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    /** Binary timeout. */
    private static final long BINARY_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    /** Multipart boundary. */
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";


    /** Portlet context. */
    private static PortletContext portletContext;


    /** Log. */
    private final Log log;


    public BinaryServlet() {
        super();
        this.log = LogFactory.getLog(this.getClass());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portletContext);
        nuxeoController.setServletRequest(request);
        nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_USER);
        nuxeoController.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        // CMS service
        ICMSService cmsService = NuxeoController.getCMSService();
        // CMS context
        CMSServiceCtx cmsContext = nuxeoController.getCMSCtx();


        // Réinitialisation des propriétes des windows
        PageProperties.init();
        PageProperties pageProperties = PageProperties.getProperties();

        // Portal name
        String portalName = request.getParameter("portalName");
        if (portalName == null) {
            portalName = "default";
        }
        pageProperties.getPagePropertiesMap().put(Constants.PORTAL_NAME, portalName);

        // Output
        OutputStream output = response.getOutputStream();
        try {
            // Request parameters
            String index = request.getParameter("index");
            String pictureContent = request.getParameter("content");
            String fieldName = request.getParameter("fieldName");

            // Document path
            String path = request.getParameter("path");
            path = URLDecoder.decode(path, "UTF-8");

            // Live state indicator
            boolean liveState = BooleanUtils.toBoolean(request.getParameter("liveState"));
            if (liveState) {
                nuxeoController.setDisplayLiveVersion("1");
            }

            // Scope
            String scope = request.getParameter("scope");
            if (scope != null) {
                nuxeoController.setScope(scope);
            }

            // Forced scope
            String forcedScope = request.getParameter("forcedScope");
            if (forcedScope != null) {
                nuxeoController.setForcePublicationInfosScope(forcedScope);
            }

            // Binary delegation
            BinaryDelegation delegation = cmsService.validateBinaryDelegation(cmsContext, path);
            if (delegation != null) {
                request.setAttribute("osivia.delegation.userName", delegation.getUserName());

                if (delegation.isGrantedAccess()) {
                    nuxeoController.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
                    nuxeoController.setForcePublicationInfosScope("superuser_context");
                }

                request.setAttribute("osivia.isAdmin", delegation.isAdmin());

                PageProperties.getProperties().setBinarySubject(delegation.getSubject());
            }

            // Binary type
            String type = request.getParameter("type");
            Type binaryType = BinaryDescription.Type.valueOf(type);

            // Force portal cache refresh
            if (BooleanUtils.toBoolean(request.getParameter("reload"))) {
                PageProperties.getProperties().setRefreshingPage(true);
            }

            // Binary content
            CMSBinaryContent content = null;
            
            String loggerPath = "";
            if (Type.ATTACHED_PICTURE.equals(binaryType)) {
                content = nuxeoController.fetchAttachedPicture(path, index);
            } else if (Type.PICTURE.equals(binaryType)) {
                content = nuxeoController.fetchPicture(path, pictureContent);
            } else if (Type.ATTACHED_FILE.equals(binaryType)) {
                nuxeoController.setStreamingSupport(true);
                content = nuxeoController.fetchFileContent(path, "files:files/"+index+"/file");
                loggerPath = path+ "/"+"files:files/"+index+"/file";
                
            } else if (Type.BLOB.equals(binaryType)) {
                content = ResourceUtil.getBlobHolderContent(nuxeoController, path, index);
            } else if (Type.FILE.equals(binaryType)) {
                nuxeoController.setStreamingSupport(true);
                content = nuxeoController.fetchFileContent(path, fieldName);
            } else if (Type.FILE_OF_VERSION.equals(binaryType)) {
                nuxeoController.setStreamingSupport(true);
                nuxeoController.setDisplayContext("downloadVersion");
                // Path is an uuid here
                content = nuxeoController.fetchFileContent(path, fieldName);
            }

            if (content.getStream() != null) {
                
                IPortalLogger.logger.info( new LoggerMessage("streaming download start " + loggerPath));
                
                long begin = System.currentTimeMillis();
                
                this.stream(request, response, content, output);
                
                long end = System.currentTimeMillis();
                
                List<Object> params = new ArrayList<Object>();
                params.add(content.getFileSize());
                params.add(end - begin);
                
                IPortalLogger.logger.info( new LoggerMessage("streaming download end " + loggerPath + " " + content.getFileSize() + " " + (end - begin)));
                
            } else {
                // Last modified
                long lastModified = System.currentTimeMillis();
                // Expires
                long expires = lastModified + BINARY_TIMEOUT;

                response.setContentType(content.getMimeType());
                response.setHeader("Content-Disposition", this.getHeaderContentDisposition(request, content));
                response.setHeader("Cache-Control", "max-age=" + BINARY_TIMEOUT);
                response.setHeader("Content-Length", String.valueOf(content.getFileSize()));

                String fileName = request.getParameter("fileName");
                if (fileName == null) {
                    fileName = content.getName();
                }

                response.setDateHeader("Last-Modified", lastModified);
                response.setDateHeader("Expires", expires);

                ResourceUtil.copy(new FileInputStream(content.getFile()), response.getOutputStream(), 4096);
            }

        } catch (NuxeoException e) {
            if (e.getErrorCode() == NuxeoException.ERROR_NOTFOUND) {
                String message = "Resource BinaryServlet " + request.getParameterMap() + " not found (error 404).";
                this.log.error(message);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                request.setAttribute("osivia.no_redirection", "1");
            } else if (e.getErrorCode() == NuxeoException.ERROR_FORBIDDEN) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute("osivia.no_redirection", "1");
            } else if (e.getErrorCode() == NuxeoException.ERROR_UNAVAILAIBLE) {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                request.setAttribute("osivia.no_redirection", "1");
            }
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }


    /**
     * Set portlet context.
     *
     * @param portletContext portlet context
     */
    public static void setPortletContext(PortletContext portletContext) {
        BinaryServlet.portletContext = portletContext;
    }


    /**
     * Get header content disposition value.
     *
     * @param request HTTP servlet request
     * @param content CMS binary content
     * @return content disposition
     */
    private String getHeaderContentDisposition(HttpServletRequest request, CMSBinaryContent content) {
        String fileName = request.getParameter("fileName");
        if (fileName == null) {
            fileName = content.getName();
        }

        StringBuilder builder = new StringBuilder();
        if ("application/pdf".equals(content.getMimeType())) {
            // Open inside navigator
            builder.append("inline; ");
        } else {
            // Force download
            builder.append("attachment; "); // FIXME
        }
        builder.append("filename=\"");
        builder.append(fileName);
        builder.append("\"");
        return builder.toString();
    }


    private void stream(HttpServletRequest request, HttpServletResponse response, CMSBinaryContent content, OutputStream output) throws IOException {
        // Length
        long length = content.getFileSize();
        // Last modified
        long lastModified = System.currentTimeMillis();
        // Expires
        long expires = lastModified + BINARY_TIMEOUT;
        // Content type
        String contentType = content.getMimeType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        // Content disposition
        String disposition = this.getHeaderContentDisposition(request, content);


        // Validate request headers for resume : If-Unmodified-Since header should be greater than LastModified.
        // If not, then return 412.
        long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
        if ((ifUnmodifiedSince != -1) && ((ifUnmodifiedSince + 1000) <= lastModified)) {
            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }


        // Prepare some variables.
        // The full Range represents the complete file.
        BytesRange full = new BytesRange(0, length - 1, length);
        List<BytesRange> ranges = new ArrayList<BytesRange>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader("Range");
        if (range != null) {
            // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // If any valid If-Range header, then process each part of byte range.
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                    NumberUtils.toInt(StringUtils.substringBefore(part, "-"), -1);

                    long start = NumberUtils.toInt(StringUtils.substringBefore(part, "-"), -1);
                    long end = NumberUtils.toInt(StringUtils.substringAfter(part, "-"), -1);

                    if (start == -1) {
                        start = length - end;
                        end = length - 1;
                    } else if ((end == -1) || (end > (length - 1))) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then return 416.
                    if (start > end) {
                        response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                        response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }

                    // Add range.
                    ranges.add(new BytesRange(start, end, length));
                }
            }
        }


        // Initialize response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Disposition", disposition);
        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", expires);


        if (range == null) {
            response.setContentType(contentType);
            response.setHeader("Content-Length", String.valueOf(content.getFileSize()));

            // Copy.
            copy(content, output, 0, content.getFileSize());
        } else {
            response.setHeader("Accept-Ranges", "bytes");
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.


            // Send requested file (part(s)) to client
            if (ranges.isEmpty() || full.equals(ranges.get(0))) {

                // Return full file.
                BytesRange r = full;
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.getStart() + "-" + r.getEnd() + "/" + r.getTotal());

                // Content length is not directly predictable in case of GZIP.
                // So only add it if there is no means of GZIP, else browser will hang.
                response.setHeader("Content-Length", String.valueOf(r.getLength()));

                // Copy full range.
                copy(content, output, r.getStart(), r.getLength());

            } else if (ranges.size() == 1) {

                // Return single part of file.
                BytesRange r = ranges.get(0);
                response.setContentType(contentType);
                response.setHeader("Content-Range", "bytes " + r.getStart() + "-" + r.getEnd() + "/" + r.getTotal());
                response.setHeader("Content-Length", String.valueOf(r.getLength()));

                // Copy single part range.
                copy(content, output, r.getStart(), r.getLength());

            } else {

                // Return multiple parts of file.
                response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);

                // Cast back to ServletOutputStream to get the easy println methods.
                ServletOutputStream sos = (ServletOutputStream) output;

                // Copy multi part range.
                for (BytesRange r : ranges) {
                    // Add multipart boundary and header fields for every range.
                    sos.println();
                    sos.println("--" + MULTIPART_BOUNDARY);
                    sos.println("Content-Type: " + contentType);
                    sos.println("Content-Range: bytes " + r.getStart() + "-" + r.getEnd() + "/" + r.getTotal());

                    // Copy single part range of multi part range.
                    copy(content, output, r.getStart(), r.getLength());
                }

                // End with multipart boundary.
                sos.println();
                sos.println("--" + MULTIPART_BOUNDARY + "--");
            }
        }
    }


    private void copy(CMSBinaryContent content, OutputStream output, long start, long length) throws IOException {
        InputStream input = content.getStream();
        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;

            if ((start > 0) || (length != content.getFileSize())) {
                // Write partial range
                input = new ConstrainedInputStream(input, length);
                input.skip(start);
            }

            while ((read = input.read(buffer)) > 0) {
                output.write(buffer, 0, read);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

}
