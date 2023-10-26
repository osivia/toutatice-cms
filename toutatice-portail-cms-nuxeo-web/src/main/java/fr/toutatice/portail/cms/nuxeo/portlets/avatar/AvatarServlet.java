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
package fr.toutatice.portail.cms.nuxeo.portlets.avatar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Avatar servlet.
 *
 * @author Loïc Billon
 * @author Cédric Krommenhoek
 * @see HttpServlet
 */
public class AvatarServlet extends HttpServlet {

    /**
     * Avatar servlet URL prefix.
     */
    public static final String SERVLET_URL_PREFIX = "/toutatice-portail-cms-nuxeo-web/avatar/";

    /**
     * Avatar URL default segment.
     */
    public static final String DEFAULT_SEGMENT = "default";

    /**
     * Cache duration.
     */
    public static final long CACHE_DURATION = TimeUnit.HOURS.toMillis(1L);


    /**
     * Log.
     */
    private final Log log;


    /**
     * Constructor.
     */
    public AvatarServlet() {
        super();

        // Log
        this.log = LogFactory.getLog(this.getClass());
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // URL segment
        String segment = StringUtils.substringBefore(StringUtils.removeStart(request.getPathInfo(), "/"), "/");

        // Avatar URL
        String avatarUrl;
        if (StringUtils.isEmpty(segment) || StringUtils.equals(DEFAULT_SEGMENT, segment)) {
            avatarUrl = null;
        } else {
            avatarUrl = new String(Base64.getUrlDecoder().decode(segment));
        }

        if (StringUtils.isEmpty(avatarUrl)) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); // Tomcat required current thread class loader, not current class loader
            String resourceName = "../../img/guest.png"; // Resource root is /src/main/resources, deployed in /WEB-INF/classes
            try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
                // Size
                int size = Objects.requireNonNull(inputStream).available();
                response.setContentLength(size);

                // Content type
                String contentType = "image/png";
                response.setContentType(contentType);

                // Content disposition
                String contentDisposition = "inline; filename=\"default.png\"";
                response.setHeader("Content-Disposition", contentDisposition);

                IOUtils.copy(inputStream, response.getOutputStream());
            } catch (Exception e) {
                this.log.error("Unable to get default avatar", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            File file = new File(avatarUrl);
            if (file.exists()) {
                // Size
                long size = FileUtils.sizeOf(file);
                response.setContentLength(Long.valueOf(size).intValue());

                // Content type
                String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
                contentType = StringUtils.defaultIfEmpty(contentType, "application/octet-stream");
                response.setContentType(contentType);

                // Content disposition
                String contentDisposition = "inline; filename=\"" + file.getName() + "\"";
                response.setHeader("Content-Disposition", contentDisposition);

                // Cache
                response.setHeader("Cache-Control", "max-age=" + CACHE_DURATION);

                FileUtils.copyFile(file, response.getOutputStream());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

}
