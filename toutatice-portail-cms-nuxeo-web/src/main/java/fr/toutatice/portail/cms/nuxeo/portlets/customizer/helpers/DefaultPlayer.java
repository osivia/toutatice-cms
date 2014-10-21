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
package fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.core.cms.CMSHandlerProperties;
import org.osivia.portal.core.cms.CMSServiceCtx;

import fr.toutatice.portail.cms.nuxeo.portlets.customizer.DefaultCMSCustomizer;

/**
 * Default player (use view document portlet).
 *
 * @see IPlayer
 */
public class DefaultPlayer implements IPlayer {

    /** Default CMS customizer. */
    private final DefaultCMSCustomizer customizer;


    /**
     * Constructor.
     *
     * @param customizer default CMS customizer
     */
    public DefaultPlayer(DefaultCMSCustomizer customizer) {
        super();
        this.customizer = customizer;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSHandlerProperties play(CMSServiceCtx cmsContext, Document document) throws Exception {
        Map<String, String> windowProperties = new HashMap<String, String>();
        return this.play(cmsContext, document, windowProperties);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CMSHandlerProperties play(CMSServiceCtx cmsContext, Document document, Map<String, String> windowProperties) throws Exception {
        windowProperties.put(Constants.WINDOW_PROP_VERSION, cmsContext.getDisplayLiveVersion());
        windowProperties.put("osivia.document.metadata", this.customizer.computeMetadataDisplayIndicator(cmsContext));
        windowProperties.put(Constants.WINDOW_PROP_URI, document.getPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");

        CMSHandlerProperties linkProps = new CMSHandlerProperties();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance");

        return linkProps;
    }

}
