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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cache.services.CacheInfo;
import org.osivia.portal.api.directory.v2.IDirProvider;
import org.osivia.portal.api.directory.v2.model.Person;
import org.osivia.portal.api.directory.v2.service.PersonService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.core.cms.CMSBinaryContent;

import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.ResourceUtil;
import fr.toutatice.portail.cms.nuxeo.api.services.NuxeoCommandContext;
import fr.toutatice.portail.cms.nuxeo.portlets.customizer.AvatarUtils;
import fr.toutatice.portail.cms.nuxeo.portlets.document.DocumentFetchLiveCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.document.FileContentCommand;
import fr.toutatice.portail.cms.nuxeo.portlets.service.GetUserProfileCommand;

/**
 * Servlet for caching and displaying avatar's from Nuxeo.
 * 
 * @author lbillon
 * @see HttpServlet
 */
public class AvatarServlet extends HttpServlet {


    /** Logger. */
    protected static final Log logger = LogFactory.getLog(AvatarServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = -5291928343907068474L;

    private static PortletContext portletCtx;

    public static void setPortletContext(PortletContext documentPortletCtx) {
        portletCtx = documentPortletCtx;
        
        // Directory service
    	IDirProvider provider = Locator.findMBean(IDirProvider.class, IDirProvider.MBEAN_NAME);
    	
    	PERSON_SERVICE = provider.getDirService(PersonService.class);        
    }

    private static final int AVATAR_TIMEOUT = 3600;

	private static PersonService PERSON_SERVICE;


    public String formatResourceLastModified() {

        SimpleDateFormat inputFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        inputFormater.setTimeZone(TimeZone.getTimeZone("GMT"));
        return inputFormater.format(new Date(System.currentTimeMillis()));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void doGet(HttpServletRequest theRequest, HttpServletResponse theResponse) throws IOException, ServletException {
        // Nuxeo controller
        NuxeoController ctx = new NuxeoController(portletCtx);
        ctx.setAuthType(NuxeoCommandContext.AUTH_TYPE_SUPERUSER);
        ctx.setCacheType(CacheInfo.CACHE_SCOPE_PORTLET_CONTEXT);

        OutputStream output = theResponse.getOutputStream();
        try {
            // Username
            String username = theRequest.getParameter("username");

            // Person
            Person person = PERSON_SERVICE.getPerson(username);

            // User identifier
            String userId;
            if (person == null) {
                userId = null;
            } else {
                userId = person.getUid();
            }

            
            boolean genericAvatar = true;

            theResponse.setHeader("Cache-Control", "max-age=" + AVATAR_TIMEOUT);
            theResponse.setHeader("Last-Modified", formatResourceLastModified());


            if (userId != null) {
                Document fetchedUserProfile = AvatarUtils.getUserProfile(portletCtx, userId, theRequest.getParameter("t"));

                if (fetchedUserProfile != null && fetchedUserProfile.getProperties().get("userprofile:avatar") != null) {
                    FileContentCommand command = new FileContentCommand(fetchedUserProfile, "userprofile:avatar");
                    command.setTimestamp(theRequest.getParameter("t"));

                    CMSBinaryContent content = (CMSBinaryContent) ctx.executeNuxeoCommand(command);

                    // Les headers doivent être positionnées avant la réponse
                    theResponse.setContentType(content.getMimeType());

                    ResourceUtil.copy(new FileInputStream(content.getFile()), theResponse.getOutputStream(), 4096);

                    genericAvatar = false;
                }
            }
            



            if (genericAvatar) {
            	
            	File file = new File(portletCtx.getRealPath("/img/guest.png"));
                theResponse.setContentType("image/png");


                byte[] data = FileUtils.readFileToByteArray(file);


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
