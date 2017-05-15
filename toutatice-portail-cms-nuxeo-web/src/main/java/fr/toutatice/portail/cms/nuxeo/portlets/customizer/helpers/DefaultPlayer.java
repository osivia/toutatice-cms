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

import org.osivia.portal.api.Constants;
import org.osivia.portal.api.player.Player;

import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoPublicationInfos;
import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;

/**
 * Default player (use view document portlet).
 *
 * @see INuxeoPlayerModule
 */
public class DefaultPlayer implements INuxeoPlayerModule {

    /**
     * Constructor.
     */
    public DefaultPlayer() {
        super();
    }


    /**
     * {@inheritDoc}
     */
	@Override
    public Player getCMSPlayer(NuxeoDocumentContext documentContext) {
        NuxeoPublicationInfos publicationInfos = documentContext.getPublicationInfos();

		Map<String, String> windowProperties = new HashMap<String, String>();
        windowProperties.put(Constants.WINDOW_PROP_VERSION, documentContext.getDocumentState().toString());
        windowProperties.put(Constants.WINDOW_PROP_URI, publicationInfos.getPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance");

        return linkProps;
	}

}
