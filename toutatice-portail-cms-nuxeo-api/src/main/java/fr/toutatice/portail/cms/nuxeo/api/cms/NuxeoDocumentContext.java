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
package fr.toutatice.portail.cms.nuxeo.api.cms;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.cms.DocumentContext;


/**
 * Nuxeo document context.
 * 
 * @author Loïc Billon
 * @author Cédric Krommenhoek
 * @see DocumentContext
 */
public interface NuxeoDocumentContext extends DocumentContext {

    /**
     * {@inheritDoc}
     */
    @Override
    NuxeoPermissions getPermissions();


    /**
     * {@inheritDoc}
     */
    @Override
    NuxeoPublicationInfos getPublicationInfos();


    /**
     * {@inheritDoc}
     */
    @Override
    Document getDocument();


    /**
     * Get display context.
     * 
     * @return display context
     */
    String getDisplayContext();


    /**
     * Check if the document is contextualized.
     * 
     * @return true if the document is contextualized
     */
    boolean isContextualized();

}
