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
 *
 *
 *    
 */
package fr.toutatice.portail.cms.nuxeo.portlets.avatar;

import java.io.File;
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

import org.apache.commons.io.FileUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetUserProfileCommand;


public class AvatarServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -5291928343907068474L;

    private static PortletContext portletCtx;

    public static void setPortletContext(PortletContext documentPortletCtx) {
        portletCtx = documentPortletCtx;
    }

    private static final int AVATAR_TIMEOUT = 3600;

    public boolean isResourceExpired(String sOriginalDate) {

        boolean isExpired = true;

        if (sOriginalDate != null) {

            SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date originalDate = inputFormater.parse(sOriginalDate);
                if (System.currentTimeMillis() < originalDate.getTime() + AVATAR_TIMEOUT * 1000)
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

    public String formatResourceLastModified() {

        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }

    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException, ServletException {

        OutputStream output = theResponse.getOutputStream();
        try {

            if (serveResourceByCache(theRequest, theResponse))
                return;

            String username = theRequest.getParameter("username");
            username = URLDecoder.decode(username, "UTF-8");

            NuxeoController ctx = new NuxeoController(portletCtx);

            ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_ANONYMOUS);
            ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

            Document userProfile = (Document) ctx.executeNuxeoCommand(new GetUserProfileCommand(username));

            Document fetchedUserProfile = (Document) ctx.executeNuxeoCommand(new DocumentFetchLiveCommand(userProfile.getPath(), "Read"));

            theResponse.setHeader("Cache-Control", "max-age=" + AVATAR_TIMEOUT);

            theResponse.setHeader("Last-Modified", formatResourceLastModified());

            if (fetchedUserProfile.getProperties().get("userprofile:avatar") != null) {
                CMSBinaryContent content = (CMSBinaryContent) ctx.executeNuxeoCommand(new FileContentCommand(fetchedUserProfile, "userprofile:avatar"));

                // Les headers doivent être positionnées avant la réponse
                theResponse.setContentType(content.getMimeType());

                ResourceUtil.copy(new FileInputStream(content.getFile()), theResponse.getOutputStream(), 4096);
            } else {

                File file = new File(portletCtx.getRealPath("/img/guest.png"));

                byte[] data = FileUtils.readFileToByteArray(file);

                // Mime type
                theResponse.setContentType("image/png");

                // Length
                int length = Long.valueOf(file.length()).intValue();
                theResponse.setContentLength(length);

                theResponse.getOutputStream().write(data);
            }



        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            output.close();
        }

    }

}
