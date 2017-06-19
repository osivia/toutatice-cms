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
import org.osivia.portal.api.cms.DocumentContext;
import org.osivia.portal.api.cms.impl.BasicPublicationInfos;
import org.osivia.portal.api.player.Player;

import fr.toutatice.portail.cms.nuxeo.api.player.INuxeoPlayerModule;
import fr.toutatice.portail.cms.nuxeo.portlets.document.ViewDocumentPortlet;

/**
 * Default player (use view document portlet).
 *
 * @see IPlayer
 */
public class DefaultPlayer implements INuxeoPlayerModule {



	/* (non-Javadoc)
	 * @see org.osivia.portal.api.cms.IPlayerModule#getCMSPlayer(org.osivia.portal.api.cms.DocumentContext)
	 */
	@Override
	public Player getCMSPlayer(DocumentContext<Document> docCtx) {
		
		Map<String, String> windowProperties = new HashMap<String, String>();
		BasicPublicationInfos navigationInfos = docCtx.getPublicationInfos(BasicPublicationInfos.class);
		
        windowProperties.put(Constants.WINDOW_PROP_VERSION, navigationInfos.getState().toString());

        String hideMetadatas = docCtx.getDoc().getString("ttc:hideMetadatas");
        if(Boolean.valueOf(hideMetadatas)) {
			windowProperties.put(ViewDocumentPortlet.HIDE_METADATA_WINDOW_PROPERTY, "1");
        }
        
        windowProperties.put(Constants.WINDOW_PROP_URI, navigationInfos.getContentPath());
        windowProperties.put("osivia.cms.publishPathAlreadyConverted", "1");
        windowProperties.put("osivia.hideDecorators", "1");

        Player linkProps = new Player();
        linkProps.setWindowProperties(windowProperties);
        linkProps.setPortletInstance("toutatice-portail-cms-nuxeo-viewDocumentPortletInstance");

        return linkProps;
	}

}
