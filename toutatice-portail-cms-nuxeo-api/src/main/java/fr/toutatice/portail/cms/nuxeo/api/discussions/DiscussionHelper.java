package fr.toutatice.portail.cms.nuxeo.api.discussions;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jboss.portal.theme.impl.render.dynamic.DynaRenderOptions;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.internationalization.Bundle;
import org.osivia.portal.api.internationalization.IBundleFactory;
import org.osivia.portal.api.internationalization.IInternationalizationService;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
import org.osivia.portal.core.constants.InternalConstants;

/**
 * The Class DiscussionHelper.
 */
public class DiscussionHelper {

    
    public static String ATTR_LOCAL_PUBLICATION_CACHE = "osivia.cms.discussions.publication.infos";

    /** The portal url factory. */
    private static IPortalUrlFactory portalUrlFactory;

    /** The bundle factory. */
    private static IBundleFactory bundleFactory;


    
    /**
     * Gets the url factory.
     *
     * @return the url factory
     */
    private static IPortalUrlFactory getUrlFactory() {
        if (portalUrlFactory == null)
            portalUrlFactory = Locator.getService(IPortalUrlFactory.class);
        return portalUrlFactory;
    }



    /**
     * Gets the discussion url.
     *
     * @param portalControllerContext the portal controller context
     * @param participant the participant
     * @param publicationId the publication id
     * @return the discussion url
     */
    private static String getDiscussionUrl(PortalControllerContext portalControllerContext, String id, String participant, String publicationId, String messageId, String mode)  {

        Map<String, String> properties = new HashMap<>();

        properties.put("osivia.hideTitle", "1");

        Map<String, String> params = new HashMap<>();
        params.put("view", "detail");
        if( id != null)
            params.put("id", id);        
        if( participant != null)
            params.put("participant", participant);
        if( publicationId != null)
            params.put("publicationId", publicationId);       
        if( messageId != null)
            params.put("messageId", messageId);
        if( mode != null)
            params.put("mode", mode);

        // URL
        String url;
        try {
            url = getUrlFactory().getStartPortletInNewPage(portalControllerContext, "discussion", "Discussion", "index-cloud-ens-discussion-instance",
                    properties, params);
        } catch (PortalException e) {
            url = "#";
        }

        return url;
    }

    

    
    /**
     * Gets the discussion url by participant.
     *
     * @param portalControllerContext the portal controller context
     * @param participant the participant
     * @return the discussion url by participant
     */
    public static String getDiscussionUrlByParticipant(PortalControllerContext portalControllerContext, String participant, String publicationId)  {
        return getDiscussionUrl(portalControllerContext, null, participant, publicationId, null, null); 
    }

    /**
     * Gets the discussion url by publication.
     *
     * @param portalControllerContext the portal controller context
     * @param publicationId the publication id
     * @return the discussion url by publication
     */
    public static String getDiscussionUrlByPublication(PortalControllerContext portalControllerContext, String publicationId)  {
        return getDiscussionUrl(portalControllerContext,null, null, publicationId, null, null); 
    }
    
    
    /**
     * Gets the discussion url by id.
     *
     * @param portalControllerContext the portal controller context
     * @param id the id
     * @return the discussion url by id
     */
    public static String getDiscussionUrlById(PortalControllerContext portalControllerContext, String id)  {
        return getDiscussionUrl(portalControllerContext,id, null, null, null, null); 
    }
    
    
    /**
     * Gets the discussion url by id.
     *
     * @param portalControllerContext the portal controller context
     * @param id the id
     * @return the discussion url by id
     */
    public static String getDiscussionAdminUrl(PortalControllerContext portalControllerContext, String discussionId, String messageId)  {
        return getDiscussionUrl(portalControllerContext,discussionId, null, null,  messageId, "admin"); 
    }
    
    
    
    /**
     * Gets the discussion url by id.
     *
     * @param portalControllerContext the portal controller context
     * @param id the id
     * @return the discussion url by id
     */
    public static void resetLocalPublications(PortalControllerContext portalControllerContext)  {
        HttpSession session = portalControllerContext.getHttpServletRequest().getSession();
        session.removeAttribute(ATTR_LOCAL_PUBLICATION_CACHE);

    }
    
}
